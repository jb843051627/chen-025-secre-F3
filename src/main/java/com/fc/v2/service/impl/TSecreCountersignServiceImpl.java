package com.fc.v2.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.fc.v2.common.exception.file.CountersignRuleException;
import com.fc.v2.mapper.auto.TSecreCarrierMapper;
import com.fc.v2.mapper.auto.TSecreCountersignMapper;
import com.fc.v2.mapper.auto.TSecreCountersignSignMapper;
import com.fc.v2.mapper.auto.TSecreLevelLogMapper;
import com.fc.v2.model.auto.TSecreCarrier;
import com.fc.v2.model.auto.TSecreCountersign;
import com.fc.v2.model.auto.TSecreCountersignSign;
import com.fc.v2.model.auto.TSecreLevelLog;
import com.fc.v2.model.custom.countersign.CountersignView;
import com.fc.v2.model.custom.countersign.GateState;
import com.fc.v2.model.custom.countersign.ReconReport;
import com.fc.v2.model.custom.countersign.SignLine;
import com.fc.v2.service.ITSecreCountersignService;
import com.fc.v2.util.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 密级变更三道闸会签引擎（六则只此一处回话）。
 *
 * 头闸 0 承办部门经办岗：候一名，那一名把字落下即放过（ONE_PASS）。
 * 次闸 1 本机关保密办：候两名，头名说法定论、后名留底数名（FIRST_WINS）；
 *                    满两名而头名落「不可」，本闸判不同意，压回头闸。
 * 末闸 2 上级主管部门：候两名同「可」（BOTH_AGREE）；两名同不可压回次闸，一可一不可挂待议。
 *
 * 唯一口径：{@link #tally} 顺着受理簿 t_secre_countersign_sign 一笔一笔点，
 * 当前轮、落脚闸、各闸到了几名、整单停在哪、什么情形，全从这笔账推出；
 * 单表上的 node_no/round_no/status 只是点完落的账，回数时反过来与它对。
 * 报文不带段位、轮次、已签数任何一格，库里也没有第二处人数口径。
 *
 * @author fuce
 * @date 2026-10-05
 */
@Service
public class TSecreCountersignServiceImpl implements ITSecreCountersignService {

    /** 闸序：0头闸 1次闸 2末闸 */
    private static final int NODE_FIRST = 0;
    private static final int NODE_SECOND = 1;
    private static final int NODE_LAST = 2;

    /** 各闸该几名 */
    private static final int[] NEED = {1, 2, 2};
    /** 各闸算齐路数 */
    private static final String[] RULE = {"ONE_PASS", "FIRST_WINS", "BOTH_AGREE"};
    private static final String[] NAME = {"承办部门经办岗", "本机关保密办", "上级主管部门"};

    /** 落字 */
    private static final int VERDICT_AGREE = 1;
    private static final int VERDICT_DISAGREE = 0;

    /** 情形 */
    private static final int STATUS_RUNNING = 0;
    private static final int STATUS_PENDING = 1;
    private static final int STATUS_SEALED = 2;
    private static final int STATUS_REJECTED = 3;

    @Resource
    private TSecreCountersignMapper countersignMapper;
    @Resource
    private TSecreCountersignSignMapper signMapper;
    @Resource
    private TSecreLevelLogMapper levelLogMapper;
    @Resource
    private TSecreCarrierMapper carrierMapper;

    // ── 第一则：有关 ──────────────────────────────────────────────────────────

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TSecreCountersign openForCarrier(Long carrierId, Integer changeKind, Integer levelTo, String remark) {
        if (carrierId == null) {
            throw new CountersignRuleException("会签单只能由册面上载体那一行带出来，空件不认");
        }
        TSecreCarrier carrier = carrierMapper.selectOne(new QueryWrapper<TSecreCarrier>()
                .eq("id", carrierId).eq("del_flag", 0));
        if (carrier == null) {
            throw new CountersignRuleException("册面上没有这一行载体，带不出会签单");
        }
        // 同一件载体、同一回变更，只带得出那一张：上一张没齐闸，第二张不许起。
        Integer openDup = countersignMapper.selectCount(new QueryWrapper<TSecreCountersign>()
                .eq("carrier_id", carrierId).eq("open_flag", 1).eq("del_flag", 0));
        if (openDup != null && openDup > 0) {
            throw new CountersignRuleException("这件载体已有一张未齐闸的会签单，同一件只带得出一张");
        }
        if (changeKind == null || changeKind < 1 || changeKind > 3) {
            throw new CountersignRuleException("变更路数只认：1往上抬 2往下压 3整个解开");
        }
        Integer levelFrom = carrier.getLevelNo();
        if (changeKind == 3) {
            levelTo = 0; // 整个解开，目标级只有 0 一说
        } else if (levelTo == null || levelTo < 1 || levelTo > 3) {
            throw new CountersignRuleException("密级只认 1秘密 2机密 3绝密；解开走第 3 路");
        } else if (changeKind == 1 && (levelFrom == null || levelTo <= levelFrom)) {
            throw new CountersignRuleException("往上抬：目标级必须高于现级");
        } else if (changeKind == 2 && (levelFrom == null || levelTo >= levelFrom)) {
            throw new CountersignRuleException("往下压：目标级必须低于现级");
        }

        TSecreCountersign cs = new TSecreCountersign();
        cs.setCsNo("CS" + carrier.getId() + "-" + System.currentTimeMillis());
        cs.setCarrierId(carrier.getId());
        cs.setCarrierNo(carrier.getCarrierNo());
        cs.setChangeKind(changeKind);
        cs.setLevelFrom(levelFrom);
        cs.setLevelTo(levelTo);
        cs.setNodeNo(NODE_FIRST);
        cs.setRoundNo(1);
        cs.setStatus(STATUS_RUNNING);
        cs.setOpenFlag(1);
        cs.setDelFlag(0);
        cs.setRemark(remark);
        countersignMapper.insert(cs);
        // 起单只把单挂在头闸候着；头闸那一名落字仍走唯一的 sign 入口，没有第二条起笔路。
        return countersignMapper.selectById(cs.getId());
    }

    // ── 唯一一支笔：第二、三、四、五则都在这一笔里 ─────────────────────────────

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TSecreCountersign sign(Long csId, String signer, boolean agree, String remark) {
        if (StringUtils.isEmpty(signer)) {
            throw new CountersignRuleException("不落字人名的一笔不认");
        }
        TSecreCountersign cs = countersignMapper.selectByIdForUpdate(csId);
        if (cs == null || (cs.getDelFlag() != null && cs.getDelFlag() == 1)) {
            throw new CountersignRuleException("没有这张会签单");
        }
        // 第五则：齐闸以后一笔进不来。
        if (cs.getStatus() != null && cs.getStatus() == STATUS_SEALED) {
            throw new CountersignRuleException("此单已齐闸封住，此后一笔进不来、一字改不了、整张也抽不走");
        }
        // 第三则：末闸一可一不可挂在待议，既不往前挪，也不就此了断，新笔不接。
        if (cs.getStatus() != null && cs.getStatus() == STATUS_PENDING) {
            throw new CountersignRuleException("末闸一可一不可，此单挂在待议，新笔不作数");
        }

        // 停在哪一闸、走到第几轮，由受理簿点，不取报文、不单信册面。
        Tally t = tally(cs.getId());
        int gate = t.acceptingNode;
        int round = t.currentRound;

        // 同一支笔在接笔闸本轮只能一笔（重走的新轮可再落，各放各格）；追补签不留门。
        Integer sameHand = signMapper.selectCount(new QueryWrapper<TSecreCountersignSign>()
                .eq("cs_id", cs.getId()).eq("round_no", round).eq("node_no", gate)
                .eq("signer", signer));
        if (sameHand != null && sameHand > 0) {
            throw new CountersignRuleException("同一支笔在本闸本轮已落过字，不许追补第二笔");
        }

        // 第几笔顺着受理簿点：行锁在单上，同一瞬两笔也只能一前一后，各按先后记，不并成一句。
        Integer seqRows = signMapper.selectCount(new QueryWrapper<TSecreCountersignSign>()
                .eq("cs_id", cs.getId()).eq("round_no", round).eq("node_no", gate));
        int seq = (seqRows == null ? 0 : seqRows) + 1;

        TSecreCountersignSign row = new TSecreCountersignSign();
        row.setCsId(cs.getId());
        row.setRoundNo(round);
        row.setNodeNo(gate);
        row.setSeqNo(seq);
        row.setSigner(signer);
        row.setVerdict(agree ? VERDICT_AGREE : VERDICT_DISAGREE);
        row.setSignTime(new Date());
        row.setVoided(0);
        row.setRemark(remark);
        signMapper.insert(row);

        settle(cs);
        return countersignMapper.selectById(cs.getId());
    }

    /**
     * 本笔落定后重点总账，按接笔闸自己的路数判：放过则只跨紧邻的下一闸，
     * 不同意则往回只收一闸（末→次、次→头），两句分歧则挂待议。
     * 次闸不借头闸数法，末闸不借次闸数法。
     */
    private void settle(TSecreCountersign cs) {
        Tally t = tally(cs.getId());

        // 末闸满两名：先判分歧——一可一不可挂待议。
        List<TSecreCountersignSign> last = t.operative[NODE_LAST];
        if (last.size() >= NEED[NODE_LAST]) {
            int v1 = last.get(0).getVerdict();
            int v2 = last.get(1).getVerdict();
            if (v1 == VERDICT_AGREE && v2 == VERDICT_AGREE) {
                seal(cs, t);
                return;
            }
            if (v1 == VERDICT_DISAGREE && v2 == VERDICT_DISAGREE) {
                pressBack(cs, NODE_LAST, t.currentRound, NODE_SECOND);
                return;
            }
            cs.setNodeNo(NODE_LAST);
            cs.setStatus(STATUS_PENDING);
            countersignMapper.updateById(cs);
            return;
        }

        // 次闸满两名：头一个落字那方的说法就是定论。
        List<TSecreCountersignSign> second = t.operative[NODE_SECOND];
        if (second.size() >= NEED[NODE_SECOND]) {
            if (second.get(0).getVerdict() == VERDICT_DISAGREE) {
                pressBack(cs, NODE_SECOND, t.currentRound, NODE_FIRST);
                return;
            }
            // 头名可、满两名：放过到末闸（后名那笔不空写，名数照累、原话两行都留底）。
            cs.setNodeNo(NODE_LAST);
            cs.setStatus(STATUS_RUNNING);
            countersignMapper.updateById(cs);
            return;
        }

        // 头闸候一名：那一名先把字落下，这道闸即放过。
        if (t.operative[NODE_FIRST].size() >= NEED[NODE_FIRST]
                && t.operative[NODE_SECOND].isEmpty()) {
            cs.setNodeNo(NODE_SECOND);
            cs.setStatus(STATUS_RUNNING);
            countersignMapper.updateById(cs);
            return;
        }

        // 其余（各闸名数未满）：停在点出来的接笔闸不动。
        cs.setNodeNo(t.acceptingNode);
        cs.setStatus(STATUS_RUNNING);
        countersignMapper.updateById(cs);
    }

    /**
     * 第四则：往回只收一闸。抹掉的只是本闸本轮落下的字（voided=1 账内抹，
     * 行还在、原话照旧存底）；此前各闸旧字一个不动。新开一轮从落脚闸重头往上走，
     * 本轮的字与上轮的字各放各格，哪天补的各有凭据。
     */
    private void pressBack(TSecreCountersign cs, int rejectGate, int round, int restNode) {
        signMapper.update(null, new UpdateWrapper<TSecreCountersignSign>()
                .eq("cs_id", cs.getId()).eq("round_no", round).eq("node_no", rejectGate)
                .eq("voided", 0).set("voided", 1));
        cs.setRoundNo(round + 1);
        cs.setNodeNo(restNode);
        cs.setStatus(STATUS_REJECTED);
        countersignMapper.updateById(cs);
    }

    /** 第五则：末闸点满即齐闸——封单、换密级、叠履历，同出一回计算（同一事务）。 */
    private void seal(TSecreCountersign cs, Tally t) {
        Date now = new Date();
        TSecreCarrier carrier = carrierMapper.selectOne(new QueryWrapper<TSecreCarrier>()
                .eq("id", cs.getCarrierId()).eq("del_flag", 0));
        if (carrier == null) {
            throw new CountersignRuleException("册面载体行不见了，封不了单");
        }

        // 密级栏跟着换过去；全库只许这一处换（载体通用 edit 口也塞不进来）。
        carrierMapper.update(null, new UpdateWrapper<TSecreCarrier>()
                .eq("id", carrier.getId()).eq("del_flag", 0)
                .set("level_no", cs.getLevelTo()));

        // 叠一条变更履历：从哪级到哪级、凭哪张单、谁落的字、落在哪一刻。
        TSecreLevelLog log = new TSecreLevelLog();
        log.setCarrierId(carrier.getId());
        log.setCarrierNo(carrier.getCarrierNo());
        log.setCsId(cs.getId());
        log.setCsNo(cs.getCsNo());
        log.setLevelFrom(cs.getLevelFrom());
        log.setLevelTo(cs.getLevelTo());
        log.setSigners(buildSigners(t));
        log.setSealTime(now);
        levelLogMapper.insert(log);

        cs.setStatus(STATUS_SEALED);
        cs.setNodeNo(NODE_LAST);
        cs.setSealTime(now);
        cs.setOpenFlag(null); // 齐闸出账：在办张数随减一
        // open_flag 要置成 NULL（updateById 会跳过 null 字段），故显式 set，否则齐了单还挂在账上。
        countersignMapper.update(null, new UpdateWrapper<TSecreCountersign>()
                .eq("id", cs.getId())
                .set("status", STATUS_SEALED)
                .set("node_no", NODE_LAST)
                .set("seal_time", now)
                .set("open_flag", null));
    }

    /** 三闸落字人逐闸点出来串成：闸名[a,b]；只点各闸生效笔集。 */
    private String buildSigners(Tally t) {
        StringBuilder sb = new StringBuilder();
        for (int node = NODE_FIRST; node <= NODE_LAST; node++) {
            if (node > NODE_FIRST) {
                sb.append('；');
            }
            sb.append(NAME[node]).append('[');
            List<TSecreCountersignSign> lines = t.operative[node];
            for (int i = 0; i < lines.size(); i++) {
                if (i > 0) {
                    sb.append(',');
                }
                sb.append(lines.get(i).getSigner());
            }
            sb.append(']');
        }
        return sb.toString();
    }

    // ── 唯一总账：段位/轮次/名数/情形只从受理簿点出 ─────────────────────────────

    /**
     * 顺着受理簿点一张单的账：
 * <ul>
     *   <li>有账内抹除（voided=1）的笔，即发生过压回；最近一遭抹在第 R 轮第 G 闸，
     *       当前轮就是 R+1，落脚闸是 G-1（往回只收一闸）。</li>
     *   <li>各闸生效笔集＝本闸最晚一轮未落抹的字；落脚闸及以后若还停在上一轮，
     *       生效集为空（等本轮重头补）；落脚闸以前各闸旧字照旧算数、照旧存底。</li>
     *   <li>头闸满 1、次闸满 2 且头名可、末闸满 2 且两句同可，叫一路放过；
     *       末闸两句分歧为待议；最近压回后落脚闸本轮尚未补字为压回重走。</li>
     * </ul>
     */
    private Tally tally(Long csId) {
        List<TSecreCountersignSign> all = signMapper.selectList(new QueryWrapper<TSecreCountersignSign>()
                .eq("cs_id", csId)
                .orderByAsc("round_no", "node_no", "seq_no"));

        Tally t = new Tally();
        t.currentRound = 1;
        t.landing = -1;

        // 最近一遭账内抹除：轮次最大那一笔（一轮只可能压回一次）。
        TSecreCountersignSign latestVoid = null;
        for (TSecreCountersignSign s : all) {
            if (s.getVoided() != null && s.getVoided() == 1) {
                if (latestVoid == null
                        || gt(s.getRoundNo(), s.getNodeNo(), latestVoid.getRoundNo(), latestVoid.getNodeNo())) {
                    latestVoid = s;
                }
            }
        }
        if (latestVoid != null) {
            t.currentRound = latestVoid.getRoundNo() + 1;
            t.landing = latestVoid.getNodeNo() - 1;
        }

        for (int node = NODE_FIRST; node <= NODE_LAST; node++) {
            List<TSecreCountersignSign> nonVoid = new ArrayList<>();
            int latestRound = 0;
            for (TSecreCountersignSign s : all) {
                if (s.getNodeNo() == null || s.getNodeNo() != node) {
                    continue;
                }
                if (s.getVoided() == null || s.getVoided() == 0) {
                    nonVoid.add(s);
                    if (s.getRoundNo() != null && s.getRoundNo() > latestRound) {
                        latestRound = s.getRoundNo();
                    }
                }
            }
            List<TSecreCountersignSign> op = new ArrayList<>();
            // 落脚闸及以后，上一轮的字已被新一轮压住，只等本轮补；落脚闸以前旧字照旧算数。
            boolean superseded = node >= t.landing && latestRound < t.currentRound;
            if (!superseded) {
                for (TSecreCountersignSign s : nonVoid) {
                    if (s.getRoundNo() != null && s.getRoundNo() == latestRound) {
                        op.add(s);
                    }
                }
            }
            op.sort(Comparator.comparing(TSecreCountersignSign::getSeqNo));
            t.operative[node] = op;
            t.arrived[node] = op.size();
            t.firstVerdict[node] = op.isEmpty() ? null : op.get(0).getVerdict();
        }

        t.passed[NODE_FIRST] = t.arrived[NODE_FIRST] >= NEED[NODE_FIRST];
        t.passed[NODE_SECOND] = t.arrived[NODE_SECOND] >= NEED[NODE_SECOND]
                && t.firstVerdict[NODE_SECOND] != null
                && t.firstVerdict[NODE_SECOND] == VERDICT_AGREE;
        boolean lastFull = t.arrived[NODE_LAST] >= NEED[NODE_LAST];
        boolean lastAgree = lastFull
                && t.operative[NODE_LAST].get(0).getVerdict() == VERDICT_AGREE
                && t.operative[NODE_LAST].get(1).getVerdict() == VERDICT_AGREE;
        boolean lastSplit = lastFull
                && !t.operative[NODE_LAST].get(0).getVerdict().equals(
                        t.operative[NODE_LAST].get(1).getVerdict());
        t.passed[NODE_LAST] = lastAgree;

        if (lastAgree && t.passed[NODE_FIRST] && t.passed[NODE_SECOND]) {
            t.phase = STATUS_SEALED;
            t.acceptingNode = null;
        } else if (lastSplit) {
            t.phase = STATUS_PENDING;
            t.acceptingNode = NODE_LAST;
        } else if (latestVoid != null && t.operative[t.landing].isEmpty()) {
            // 最近一遭压回后，落脚闸本轮一个字还没补：还压在那等着重走。
            t.phase = STATUS_REJECTED;
            t.acceptingNode = t.landing;
        } else {
            t.phase = STATUS_RUNNING;
            t.acceptingNode = t.passed[NODE_FIRST]
                    ? (t.passed[NODE_SECOND] ? NODE_LAST : NODE_SECOND)
                    : NODE_FIRST;
        }
        return t;
    }

    private boolean gt(int r1, int n1, int r2, int n2) {
        return r1 > r2 || (r1 == r2 && n1 > n2);
    }

    /** 一本总账的回话（内部用）。 */
    private static final class Tally {
        private int currentRound = 1;
        private int landing = -1;
        private final List<TSecreCountersignSign>[] operative = new List[] {
                new ArrayList<TSecreCountersignSign>(),
                new ArrayList<TSecreCountersignSign>(),
                new ArrayList<TSecreCountersignSign>()};
        private final int[] arrived = new int[3];
        private final boolean[] passed = new boolean[3];
        private final Integer[] firstVerdict = new Integer[3];
        private int phase = STATUS_RUNNING;
        private Integer acceptingNode = NODE_FIRST;
    }

    // ── 回显与台账 ────────────────────────────────────────────────────────────

    @Override
    public CountersignView view(Long csId) {
        TSecreCountersign cs = countersignMapper.selectById(csId);
        if (cs == null || (cs.getDelFlag() != null && cs.getDelFlag() == 1)) {
            throw new CountersignRuleException("没有这张会签单");
        }
        Tally t = tally(csId);

        CountersignView view = new CountersignView();
        view.setId(cs.getId());
        view.setCsNo(cs.getCsNo());
        view.setCarrierId(cs.getCarrierId());
        view.setCarrierNo(cs.getCarrierNo());
        view.setChangeKind(cs.getChangeKind());
        view.setLevelFrom(cs.getLevelFrom());
        view.setLevelTo(cs.getLevelTo());
        view.setRoundNo(t.currentRound);
        view.setStatus(t.phase);
        view.setStatusText(statusText(t.phase));
        view.setSealTime(cs.getSealTime());
        boolean sealed = t.phase == STATUS_SEALED;
        view.setSealed(sealed);
        view.setCurrentNode(sealed ? null : t.acceptingNode);

        List<TSecreCountersignSign> all = signMapper.selectList(new QueryWrapper<TSecreCountersignSign>()
                .eq("cs_id", csId)
                .orderByAsc("round_no", "node_no", "seq_no"));
        for (int node = NODE_FIRST; node <= NODE_LAST; node++) {
            GateState gate = new GateState();
            gate.setNodeNo(node);
            gate.setNodeName(NAME[node]);
            gate.setNeedCount(NEED[node]);
            gate.setPassRule(RULE[node]);
            gate.setArrivedCount(t.arrived[node]); // 只回显，没有格子给人填
            gate.setFirstVerdict(t.firstVerdict[node]);
            gate.setPassed(t.passed[node]);

            // 本轮与上轮各放各格，账内抹了的旧字也摆着（voided 标出来），两行原话都在。
            Set<Long> operativeIds = t.operative[node].stream()
                    .map(TSecreCountersignSign::getId).collect(Collectors.toSet());
            List<SignLine> lines = new ArrayList<>();
            for (TSecreCountersignSign s : all) {
                if (s.getNodeNo() == null || s.getNodeNo() != node) {
                    continue;
                }
                SignLine line = new SignLine();
                line.setRoundNo(s.getRoundNo());
                line.setSeqNo(s.getSeqNo());
                line.setSigner(s.getSigner());
                line.setVerdict(s.getVerdict());
                line.setSignTime(s.getSignTime());
                line.setRemark(s.getRemark());
                line.setVoided(s.getVoided());
                line.setOperative(operativeIds.contains(s.getId()));
                lines.add(line);
            }
            gate.setLines(lines);
            view.getGates().add(gate);
        }
        return view;
    }

    @Override
    public List<TSecreCountersign> list(boolean openOnly) {
        QueryWrapper<TSecreCountersign> w = new QueryWrapper<TSecreCountersign>()
                .eq("del_flag", 0).orderByDesc("create_time");
        if (openOnly) {
            w.eq("open_flag", 1);
        }
        return countersignMapper.selectList(w);
    }

    // ── 第六则：数得合 ─────────────────────────────────────────────────────────

    @Override
    public int openCount() {
        Integer n = countersignMapper.selectCount(new QueryWrapper<TSecreCountersign>()
                .eq("open_flag", 1).eq("del_flag", 0));
        return n == null ? 0 : n;
    }

    @Override
    public ReconReport reconcile(Long csId) {
        TSecreCountersign cs = countersignMapper.selectById(csId);
        if (cs == null || (cs.getDelFlag() != null && cs.getDelFlag() == 1)) {
            throw new CountersignRuleException("没有这张会签单");
        }
        ReconReport report = new ReconReport();
        report.setBooked(1);
        report.setCounted(1);
        reconcileOne(cs, tally(csId), report);
        report.setOk(report.getProblems().isEmpty());
        return report;
    }

    @Override
    public ReconReport reconcileAll() {
        ReconReport report = new ReconReport();
        // 账的两头：一头是挂账 open_flag（随起单加一、齐闸减一），一头是把每张单逐闸点一遍。
        List<TSecreCountersign> booked = countersignMapper.selectList(new QueryWrapper<TSecreCountersign>()
                .eq("open_flag", 1).eq("del_flag", 0));
        List<TSecreCountersign> all = countersignMapper.selectList(new QueryWrapper<TSecreCountersign>()
                .eq("del_flag", 0));
        int countedOpen = 0;
        for (TSecreCountersign cs : all) {
            if (tally(cs.getId()).phase != STATUS_SEALED) {
                countedOpen++;
            }
        }
        report.setBooked(booked.size());
        report.setCounted(countedOpen);
        if (booked.size() != countedOpen) {
            report.getProblems().add("在办张数对不齐：挂账点到 " + booked.size()
                    + " 张，逐闸点出 " + countedOpen + " 张，点名报出");
        }
        // 在办的、齐闸封住的，单拎一张翻出来都要站得住。
        for (TSecreCountersign cs : all) {
            reconcileOne(cs, tally(cs.getId()), report);
        }
        report.setOk(report.getProblems().isEmpty());
        return report;
    }

    /** 从末闸回数回头闸：各闸名数、整单停留闸、轮次、情形，与受理簿点出的总账一笔一笔对。 */
    private void reconcileOne(TSecreCountersign cs, Tally t, ReconReport report) {
        for (int node = NODE_FIRST; node <= NODE_LAST; node++) {
            if (t.arrived[node] > NEED[node]) {
                report.getProblems().add("单 " + cs.getCsNo() + "（" + cs.getCarrierNo() + "）第 " + (node + 1)
                        + " 闸点出 " + t.arrived[node] + " 笔，超过该闸该落的 " + NEED[node] + " 名");
            }
        }

        ReconReport.Item item = new ReconReport.Item();
        item.setCsId(cs.getId());
        item.setCsNo(cs.getCsNo());
        item.setCarrierNo(cs.getCarrierNo());
        item.setBookedNode(cs.getNodeNo());
        item.setCountedNode(t.phase == STATUS_SEALED ? NODE_LAST : t.acceptingNode);
        item.setBookedStatus(cs.getStatus());
        item.setCountedStatus(t.phase);
        report.getItems().add(item);

        if (cs.getRoundNo() == null || cs.getRoundNo() != t.currentRound) {
            report.getProblems().add("单 " + cs.getCsNo() + "（" + cs.getCarrierNo()
                    + "）轮次对不齐：册面记第 " + cs.getRoundNo()
                    + " 轮，受理簿点出第 " + t.currentRound + " 轮");
        }
        if (cs.getStatus() == null || cs.getStatus() != t.phase) {
            report.getProblems().add("单 " + cs.getCsNo() + "（" + cs.getCarrierNo()
                    + "）情形对不齐：册面记「" + statusText(cs.getStatus())
                    + "」，受理簿点出「" + statusText(t.phase) + "」");
        }
        Integer expectNode = bookedNodeOf(t);
        if (expectNode != null && (cs.getNodeNo() == null || cs.getNodeNo() != expectNode)) {
            report.getProblems().add("单 " + cs.getCsNo() + "（" + cs.getCarrierNo()
                    + "）停留闸对不齐：册面记第 " + (cs.getNodeNo() == null ? -1 : cs.getNodeNo() + 1)
                    + " 闸，受理簿点出第 " + (expectNode + 1) + " 闸");
        }
        // 履历与册面密级栏出自同一回计算：齐闸的单，载体密级必须已是目标级、履历必须对得上一张。
        if (t.phase == STATUS_SEALED) {
            TSecreCarrier carrier = carrierMapper.selectById(cs.getCarrierId());
            if (carrier == null) {
                report.getProblems().add("单 " + cs.getCsNo() + " 齐闸了，载体行却不见了");
            } else if (carrier.getLevelNo() == null || !carrier.getLevelNo().equals(cs.getLevelTo())) {
                report.getProblems().add("单 " + cs.getCsNo() + "（" + cs.getCarrierNo()
                        + "）齐闸封了，册面密级栏却没换到 " + levelText(cs.getLevelTo()));
            }
            Integer logCnt = levelLogMapper.selectCount(new QueryWrapper<TSecreLevelLog>()
                    .eq("cs_id", cs.getId()));
            if (logCnt == null || logCnt != 1) {
                report.getProblems().add("单 " + cs.getCsNo() + "（" + cs.getCarrierNo()
                        + "）履历条数对不齐：该叠 1 条，点到 " + logCnt + " 条");
            }
        }
    }

    /** 册面 node_no 应记的闸：待议/封住记末闸，压回记落脚闸，在签记接笔闸。 */
    private Integer bookedNodeOf(Tally t) {
        if (t.phase == STATUS_SEALED || t.phase == STATUS_PENDING) {
            return NODE_LAST;
        }
        return t.acceptingNode;
    }

    @Override
    public List<TSecreLevelLog> historyOfCarrier(Long carrierId) {
        if (carrierId == null) {
            throw new CountersignRuleException("没有载体行，点不出履历");
        }
        return levelLogMapper.selectList(new QueryWrapper<TSecreLevelLog>()
                .eq("carrier_id", carrierId).orderByDesc("seal_time").orderByDesc("id"));
    }

    private String statusText(Integer status) {        if (status == null) {
            return "未起";
        }
        switch (status) {
            case STATUS_RUNNING:
                return "在签";
            case STATUS_PENDING:
                return "待议";
            case STATUS_SEALED:
                return "齐闸封住";
            case STATUS_REJECTED:
                return "已压回重走";
            default:
                return "未知";
        }
    }

    private String levelText(Integer level) {
        if (level == null) {
            return "未定";
        }
        switch (level) {
            case 0:
                return "已解密";
            case 1:
                return "秘密";
            case 2:
                return "机密";
            case 3:
                return "绝密";
            default:
                return "未知";
        }
    }
}
