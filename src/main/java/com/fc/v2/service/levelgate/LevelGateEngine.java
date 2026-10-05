package com.fc.v2.service.levelgate;

import com.fc.v2.model.auto.TSecreCarrier;
import com.fc.v2.model.auto.TSecreCarrierLevelLog;
import com.fc.v2.model.auto.TSecreLevelGateBill;
import com.fc.v2.model.auto.TSecreLevelGateSign;
import com.fc.v2.util.SnowflakeIdWorker;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 三闸密级变更引擎——六则规矩只在此一处落子。
 *
 * <ol>
 * <li>要有关：一张单只由册面那一行带出；截次由单据点出来，报文里写第几段不算数。
 * <li>要有人：应签/算齐办法钉死在 {@link LevelGateRule}；已到几名只顺流水点，屏上只是回显。
 * <li>要分得清路子：一名即过／头名定论后笔留痕／两名同可方过，各闸各数各的。
 * <li>要退得干净：只往前跨紧邻一闸（跳关落的字不作数）；压回只抹本闸本轮，旧字照旧存底。
 * <li>要封得住：末闸点满即齐闸，密级栏与履历同一回计算、同一事务落下。
 * <li>要数得合：在办张数随起单加一、随齐闸减一，与逐闸点数同出一口；对不齐点名报出。
 * </ol>
 *
 * <p>一支笔只此一处入口：{@link #sign}。不起单以外的手工单、不改旧笔、不追补签。
 *
 * <p>回次口径：单据上的 round_no 是「重头往上走」的回数。压回只抹判不同意那一闸本轮的字，
 * 此前各闸旧字各认各的旧回次、照旧存底；被收那一截起在新回次里重走，本轮与上轮各放各格。
 *
 * @author fuce
 * @date 2026-10-05
 */
public class LevelGateEngine {

    private final LevelGateRepository repo;
    private final TimeSource timeSource;

    public LevelGateEngine(LevelGateRepository repo, TimeSource timeSource) {
        this.repo = repo;
        this.timeSource = timeSource;
    }

    /**
     * 起单：只由册面上那一行带出来。同一载体有在办单时，第二张带不出来；手工另起的一张不认。
     */
    @Transactional(rollbackFor = Exception.class)
    public LevelGateView open(Long carrierId, int changeKind, String remark) {
        if (carrierId == null) {
            throw new LevelGateException("CARRIER_REQUIRED", "单只能由册面上那一行带出来");
        }
        TSecreCarrier carrier = repo.lockCarrier(carrierId);
        if (carrier == null || (carrier.getDelFlag() != null && carrier.getDelFlag() == 1)) {
            throw new LevelGateException("CARRIER_NOT_FOUND", "册面那一行点不到，单带不出来");
        }
        Integer fromLevel = carrier.getLevelNo();
        LevelGateRule.checkChangeAllowed(changeKind, fromLevel);
        if (repo.existsOpenBill(carrierId)) {
            throw new LevelGateException("OPEN_BILL_EXISTS", "这件载体已有在办的三闸单；同一件、同一回只带得出一张");
        }

        Date now = new Date(timeSource.getAsLong());
        TSecreLevelGateBill bill = new TSecreLevelGateBill();
        bill.setId(Long.valueOf(SnowflakeIdWorker.getUUID()));
        bill.setBillNo("MJ" + new SimpleDateFormat("yyyyMMddHHmmssSSS").format(now)
                + SnowflakeIdWorker.getUUID().substring(10));
        bill.setCarrierId(carrierId);
        bill.setCarrierNo(carrier.getCarrierNo());
        bill.setChangeKind(changeKind);
        bill.setFromLevel(fromLevel);
        bill.setGateNo(LevelGateRule.GATE_HANDLING);
        bill.setGateStatus(LevelGateRule.ST_WAITING);
        bill.setRoundNo(1);
        bill.setDelFlag(0);
        bill.setRemark(remark);
        repo.insertBill(bill);
        return view(bill.getId());
    }

    /**
     * 递笔——全机关唯一的落字入口。报文中自称的闸次/段位一概不取，单停在哪一截就只认那一截：
     * 越过次一闸直接从末闸递进来的那一笔不算，单仍停在次一闸。
     */
    @Transactional(rollbackFor = Exception.class)
    public LevelGateView sign(Long billId, String signerNo, int verdict, String word) {
        if (billId == null) {
            throw new LevelGateException("BILL_REQUIRED", "没点到单");
        }
        if (signerNo == null || signerNo.trim().isEmpty()) {
            throw new LevelGateException("SIGNER_REQUIRED", "落字须点得出是谁");
        }
        if (verdict != LevelGateRule.WORD_YES && verdict != LevelGateRule.WORD_NO) {
            throw new LevelGateException("VERDICT", "写下的字只认「可」与「不可」");
        }
        TSecreLevelGateBill bill = repo.lockBill(billId);
        if (bill == null || (bill.getDelFlag() != null && bill.getDelFlag() == 1)) {
            throw new LevelGateException("BILL_NOT_FOUND", "这张单点不到");
        }
        if (bill.getGateNo() != null && bill.getGateNo() == LevelGateRule.GATE_SEALED) {
            throw new LevelGateException("ALREADY_SEALED", "齐闸以后一笔进不来、一字改不了、整张也抽不走");
        }
        if (bill.getGateStatus() != null && bill.getGateStatus() == LevelGateRule.ST_SEALED) {
            throw new LevelGateException("ALREADY_SEALED", "齐闸以后一笔进不来、一字改不了、整张也抽不走");
        }
        if (bill.getGateStatus() != null && bill.getGateStatus() == LevelGateRule.ST_PENDING) {
            throw new LevelGateException("GATE_PENDING",
                    "末闸一可一不可，单挂在待议上：不往前挪，也不就此了断，本回不再进笔");
        }

        int g = bill.getGateNo();
        int round = bill.getRoundNo() == null ? 1 : bill.getRoundNo();
        int need = LevelGateRule.NEED_COUNT[g];
        int mode = LevelGateRule.GATE_MODE[g];

        if (repo.existsSign(billId, g, round, signerNo)) {
            throw new LevelGateException("DUPLICATE_SIGN",
                    "这一闸这一回已经点过你的名；落下的字不改，追补签一道暗门也不留");
        }
        List<TSecreLevelGateSign> roundSigns = signsOf(repo.listSigns(billId), g, round);
        if (roundSigns.size() >= need) {
            throw new LevelGateException("GATE_FULL", "本闸该到的名数已经点满，多余的笔不作数");
        }

        TSecreLevelGateSign sign = new TSecreLevelGateSign();
        sign.setId(Long.valueOf(SnowflakeIdWorker.getUUID()));
        sign.setBillId(billId);
        sign.setGateNo(g);
        sign.setRoundNo(round);
        // 序由单据行锁内点出来的先后定：两名挤在同一瞬也各按各的先后记，不并成一句。
        sign.setSignSeq(roundSigns.size());
        sign.setSignerNo(signerNo);
        sign.setVerdict(verdict);
        sign.setWord(word);
        sign.setSignedAt(new Date(timeSource.getAsLong()));
        repo.insertSign(sign, roundSigns.size());

        List<TSecreLevelGateSign> after = signsOf(repo.listSigns(billId), g, round);
        int arrived = after.size();
        if (arrived < need) {
            // 名数没到一般多：本闸没满，单仍停在本截，不动别的。
            return view(billId);
        }

        // 本闸点满，按本闸自己的路数定论——次一闸不兴拿头一闸的数法去解，末一闸不兴拿次一闸的数法去压。
        Integer gateVerdict;
        if (mode == LevelGateRule.MODE_BOTH_AGREE) {
            int yes = 0;
            int no = 0;
            for (TSecreLevelGateSign s : after) {
                if (s.getVerdict() == LevelGateRule.WORD_YES) {
                    yes++;
                } else {
                    no++;
                }
            }
            if (yes == need) {
                gateVerdict = LevelGateRule.WORD_YES;
            } else if (no == need) {
                gateVerdict = LevelGateRule.WORD_NO;
            } else {
                // 一句可、一句不可：挂待议，既不往前挪，也不就此了断。
                bill.setGateStatus(LevelGateRule.ST_PENDING);
                repo.updateBill(bill);
                return view(billId);
            }
        } else {
            // MODE_ONE 与 MODE_FIRST_WINS：头一个落字那方的说法就是定论（序只认流水）。
            gateVerdict = after.get(0).getVerdict();
        }

        if (gateVerdict == LevelGateRule.WORD_NO) {
            pressBack(bill, g, round);
            return view(billId);
        }

        // 可：往前只跨紧邻的下一闸；已是末闸则齐闸封存。
        if (g == LevelGateRule.GATE_SUPERIOR) {
            seal(bill);
        } else {
            bill.setGateNo(g + 1);
            bill.setGateStatus(LevelGateRule.ST_WAITING);
            repo.updateBill(bill);
        }
        return view(billId);
    }

    /**
     * 压回：往回只收一闸。抹掉的只是判不同意这一闸本轮的字，
     * 此前各闸旧字一个不动、照旧存底；被压回的那一闸在新回次里重头往上走。
     */
    private void pressBack(TSecreLevelGateBill bill, int rejectGate, int round) {
        repo.deleteRoundSigns(bill.getId(), rejectGate, round);
        int backTo = Math.max(LevelGateRule.GATE_HANDLING, rejectGate - 1);
        bill.setGateNo(backTo);
        bill.setGateStatus(LevelGateRule.ST_WAITING);
        bill.setRoundNo(round + 1);
        repo.updateBill(bill);
    }

    /**
     * 齐闸封存：密级栏与履历出自同一回计算、同一事务；此后一笔进不来、一字改不了。
     */
    private void seal(TSecreLevelGateBill bill) {
        int toLevel = LevelGateRule.computeToLevel(bill.getChangeKind(), bill.getFromLevel());
        List<TSecreLevelGateSign> all = ordered(repo.listSigns(bill.getId()));
        Date now = new Date(timeSource.getAsLong());

        TSecreCarrier carrier = repo.lockCarrier(bill.getCarrierId());
        if (carrier == null) {
            throw new LevelGateException("CARRIER_NOT_FOUND", "齐闸时册面那一行点不到，封存作罢");
        }
        // 起单时已验过走向；此处以同一算法重算，不信任何手填值——两处口径必须一致。
        LevelGateRule.checkChangeAllowed(bill.getChangeKind(), carrier.getLevelNo());
        int recomputed = LevelGateRule.computeToLevel(bill.getChangeKind(), carrier.getLevelNo());
        if (recomputed != toLevel) {
            throw new LevelGateException("LEVEL_DRIFT",
                    "册面密级与起单快照对不上（" + toLevel + " vs " + recomputed + "），这一回不算");
        }
        if (repo.countLogs(bill.getId()) != 0) {
            throw new LevelGateException("DOUBLE_SEAL", "这张单已叠过履历，不许重复齐闸");
        }

        TSecreCarrierLevelLog log = new TSecreCarrierLevelLog();
        log.setId(Long.valueOf(SnowflakeIdWorker.getUUID()));
        log.setCarrierId(bill.getCarrierId());
        log.setBillId(bill.getId());
        log.setBillNo(bill.getBillNo());
        log.setChangeKind(bill.getChangeKind());
        log.setFromLevel(carrier.getLevelNo());
        log.setToLevel(toLevel);
        log.setWriteUps(renderWriteUps(all));
        log.setSealedAt(now);

        repo.updateCarrierLevel(bill.getCarrierId(), toLevel);
        repo.insertLog(log);

        bill.setToLevel(toLevel);
        bill.setGateNo(LevelGateRule.GATE_SEALED);
        bill.setGateStatus(LevelGateRule.ST_SEALED);
        bill.setSealTime(now);
        repo.updateBill(bill);
    }

    /** 单视图：四项（该几名、按什么算放过、当下到了几名、停在哪一截）全是点出来的回显。 */
    public LevelGateView view(Long billId) {
        TSecreLevelGateBill bill = repo.lockBill(billId);
        if (bill == null) {
            throw new LevelGateException("BILL_NOT_FOUND", "这张单点不到");
        }
        List<TSecreLevelGateSign> all = ordered(repo.listSigns(billId));
        int epoch = bill.getRoundNo() == null ? 1 : bill.getRoundNo();
        boolean sealed = bill.getGateNo() != null && bill.getGateNo() == LevelGateRule.GATE_SEALED;

        LevelGateView v = new LevelGateView();
        v.setBillId(bill.getId());
        v.setBillNo(bill.getBillNo());
        v.setCarrierId(bill.getCarrierId());
        v.setCarrierNo(bill.getCarrierNo());
        v.setChangeKind(bill.getChangeKind());
        v.setFromLevel(bill.getFromLevel());
        v.setToLevel(bill.getToLevel());
        v.setStopGateNo(bill.getGateNo());
        v.setStopGateName(sealed ? "齐闸" : LevelGateRule.GATE_NAME[bill.getGateNo()]);
        v.setGateStatus(bill.getGateStatus());
        v.setGateStatusName(statusName(bill.getGateStatus()));
        v.setRoundNo(bill.getRoundNo());
        v.setSealTime(bill.getSealTime());

        for (int g = 0; g < LevelGateRule.GATE_COUNT; g++) {
            LevelGateView.GateView gv = new LevelGateView.GateView();
            gv.setGateNo(g);
            gv.setGateName(LevelGateRule.GATE_NAME[g]);
            gv.setNeedCount(LevelGateRule.NEED_COUNT[g]);
            gv.setModeName(LevelGateRule.MODE_NAME[LevelGateRule.GATE_MODE[g]]);

            Map<Integer, List<TSecreLevelGateSign>> byRound = new LinkedHashMap<>();
            for (TSecreLevelGateSign s : all) {
                if (s.getGateNo() == g) {
                    byRound.computeIfAbsent(s.getRoundNo(), k -> new ArrayList<>()).add(s);
                }
            }
            for (Map.Entry<Integer, List<TSecreLevelGateSign>> e : byRound.entrySet()) {
                List<TSecreLevelGateSign> list = ordered(e.getValue());
                LevelGateView.RoundView rv = new LevelGateView.RoundView();
                rv.setRoundNo(e.getKey());
                rv.setArrivedCount(list.size());
                for (TSecreLevelGateSign s : list) {
                    LevelGateView.SignView sv = new LevelGateView.SignView();
                    sv.setSignSeq(s.getSignSeq());
                    sv.setSignerNo(s.getSignerNo());
                    sv.setVerdict(s.getVerdict());
                    sv.setWord(s.getWord());
                    sv.setSignedAt(s.getSignedAt());
                    rv.getSigns().add(sv);
                }
                gv.getRounds().add(rv);
            }

            // 本截「当下到了几名」取哪一回：
            //  · 停截只看单据当下回次（可能一笔还没落，便是 0）；
            //  · 已走过的截看存底里最新一回（旧回旧字照旧算，压回不动它）；
            //  · 本回还没走到的身前截，回显 0，旧回的字只在 rounds 里留底摆着。
            Integer headRound;
            if (sealed || g < bill.getGateNo()) {
                headRound = maxRound(byRound);
            } else if (g == bill.getGateNo()) {
                headRound = epoch;
            } else {
                headRound = null;
            }
            List<TSecreLevelGateSign> head = headRound == null ? null : byRound.get(headRound);
            int arrived = head == null ? 0 : head.size();
            gv.setArrivedCount(arrived);
            // 点到的名数与该闸该落的名数一般多，才叫这一闸放过；一般多不到，便是本闸没满。
            gv.setFull(arrived >= LevelGateRule.NEED_COUNT[g]);
            gv.setFirstVerdict(head != null && !head.isEmpty() ? ordered(head).get(0).getVerdict() : null);
            v.getGates().add(gv);
        }
        return v;
    }

    /** 屏上那个数：随起单加一、随齐闸减一——与逐闸点数同出于账本一口。 */
    public int openCount() {
        return repo.countOpenBills();
    }

    /**
     * 对账：从末一闸回数回头一闸，逐张单与流水、册面、履历轧；单拎一闸翻出来看也站得住。
     * 对得齐返回空；对不齐把单号与差处一条条点名报出。
     */
    public List<String> reconcile() {
        List<String> issues = new ArrayList<>();
        List<TSecreLevelGateBill> bills = repo.listAllBills();
        int openRecount = 0;
        for (TSecreLevelGateBill bill : bills) {
            if (bill.getDelFlag() != null && bill.getDelFlag() == 1) {
                continue;
            }
            String no = bill.getBillNo();
            int stop = bill.getGateNo() == null ? -1 : bill.getGateNo();
            List<TSecreLevelGateSign> all = ordered(repo.listSigns(bill.getId()));
            boolean sealed = stop == LevelGateRule.GATE_SEALED
                    || (bill.getGateStatus() != null && bill.getGateStatus() == LevelGateRule.ST_SEALED);
            boolean pending = bill.getGateStatus() != null && bill.getGateStatus() == LevelGateRule.ST_PENDING;
            if (!sealed) {
                openRecount++;
            }

            for (int g = 0; g < LevelGateRule.GATE_COUNT; g++) {
                if (g < (sealed ? LevelGateRule.GATE_COUNT : stop)) {
                    // 已放过的截：存底最新一回应点满，且按本截路数定论是「可」，才站得住。
                    Map<Integer, List<TSecreLevelGateSign>> byRound = roundsOf(all, g);
                    Integer latest = maxRound(byRound);
                    int arrived = latest == null ? 0 : byRound.get(latest).size();
                    Integer verdict = currentVerdict(all, g, latest == null ? -1 : latest);
                    if (arrived < LevelGateRule.NEED_COUNT[g]) {
                        issues.add(no + " 闸" + g + "（" + LevelGateRule.GATE_NAME[g]
                                + "）已放过，但该回只点到 " + arrived + "/" + LevelGateRule.NEED_COUNT[g] + " 名");
                    } else if (verdict == null || verdict != LevelGateRule.WORD_YES) {
                        issues.add(no + " 闸" + g + "（" + LevelGateRule.GATE_NAME[g] + "）已放过，但定论不是「可」");
                    }
                } else if (!sealed && g == stop) {
                    int round = bill.getRoundNo() == null ? 1 : bill.getRoundNo();
                    int arrived = countRound(all, g, round);
                    if (arrived > LevelGateRule.NEED_COUNT[g]) {
                        issues.add(no + " 闸" + g + " 在办却点进 " + arrived + " 笔，多过应签 "
                                + LevelGateRule.NEED_COUNT[g] + " 名");
                    }
                    if (pending) {
                        if (arrived != LevelGateRule.NEED_COUNT[g]) {
                            issues.add(no + " 闸" + g + " 挂待议，但本回名数不是应签名数（"
                                    + arrived + "/" + LevelGateRule.NEED_COUNT[g] + "）");
                        }
                        if (currentVerdict(all, g, round) != null) {
                            issues.add(no + " 闸" + g + " 挂待议却又点得出齐整定论，账不自洽");
                        }
                    }
                }
            }

            if (sealed) {
                int logs = repo.countLogs(bill.getId());
                if (logs != 1) {
                    issues.add(no + " 已齐闸，履历却有 " + logs + " 条（应恰为 1 条）");
                }
                TSecreCarrier carrier = repo.lockCarrier(bill.getCarrierId());
                if (carrier == null) {
                    issues.add(no + " 已齐闸，但册面那一行点不到了");
                } else if (bill.getToLevel() == null || carrier.getLevelNo() == null
                        || carrier.getLevelNo().intValue() != bill.getToLevel().intValue()) {
                    issues.add(no + " 册面密级（" + carrier.getLevelNo()
                            + "）与齐闸定级（" + bill.getToLevel() + "）两处口径不一致");
                }
            }
        }
        int screenCount = repo.countOpenBills();
        if (screenCount != openRecount) {
            issues.add("在办张数屏上账 " + screenCount + " 与逐张回数 " + openRecount + " 对不齐");
        }
        return issues;
    }

    private static String statusName(Integer st) {
        if (st == null) {
            return "未知";
        }
        switch (st) {
            case LevelGateRule.ST_WAITING:
                return "候签";
            case LevelGateRule.ST_PENDING:
                return "挂待议";
            case LevelGateRule.ST_SEALED:
                return "已封住";
            default:
                return "未知";
        }
    }

    /** 某截某回按其路数点出的定论；点不满或（同可截）两句不齐、一可一不可，返回 null。 */
    private static Integer currentVerdict(List<TSecreLevelGateSign> all, int g, int round) {
        List<TSecreLevelGateSign> list = signsOf(all, g, round);
        int need = LevelGateRule.NEED_COUNT[g];
        int mode = LevelGateRule.GATE_MODE[g];
        if (list.size() < need) {
            return null;
        }
        if (mode == LevelGateRule.MODE_BOTH_AGREE) {
            int yes = 0;
            int no = 0;
            for (TSecreLevelGateSign s : list) {
                if (s.getVerdict() == LevelGateRule.WORD_YES) {
                    yes++;
                } else {
                    no++;
                }
            }
            return yes == need ? LevelGateRule.WORD_YES : no == need ? LevelGateRule.WORD_NO : null;
        }
        return ordered(list).get(0).getVerdict();
    }

    private static Map<Integer, List<TSecreLevelGateSign>> roundsOf(List<TSecreLevelGateSign> all, int g) {
        Map<Integer, List<TSecreLevelGateSign>> byRound = new LinkedHashMap<>();
        for (TSecreLevelGateSign s : all) {
            if (s.getGateNo() == g) {
                byRound.computeIfAbsent(s.getRoundNo(), k -> new ArrayList<>()).add(s);
            }
        }
        return byRound;
    }

    private static Integer maxRound(Map<Integer, List<TSecreLevelGateSign>> byRound) {
        Integer max = null;
        for (Integer r : byRound.keySet()) {
            if (max == null || r > max) {
                max = r;
            }
        }
        return max;
    }

    private static List<TSecreLevelGateSign> signsOf(List<TSecreLevelGateSign> all, int g, int round) {
        List<TSecreLevelGateSign> list = new ArrayList<>();
        for (TSecreLevelGateSign s : all) {
            if (s.getGateNo() == g && s.getRoundNo() == round) {
                list.add(s);
            }
        }
        return ordered(list);
    }

    private static int countRound(List<TSecreLevelGateSign> all, int g, int round) {
        int n = 0;
        for (TSecreLevelGateSign s : all) {
            if (s.getGateNo() == g && s.getRoundNo() == round) {
                n++;
            }
        }
        return n;
    }

    /** 序次只认闸→回→序；时刻并摆（同瞬两名不并成一句，复核各认各的时刻）。 */
    private static List<TSecreLevelGateSign> ordered(List<TSecreLevelGateSign> list) {
        List<TSecreLevelGateSign> copy = new ArrayList<>(list);
        Collections.sort(copy, Comparator
                .comparingInt(TSecreLevelGateSign::getGateNo)
                .thenComparingInt(TSecreLevelGateSign::getRoundNo)
                .thenComparingInt(TSecreLevelGateSign::getSignSeq)
                .thenComparing(TSecreLevelGateSign::getSignedAt, Comparator.nullsLast(Comparator.naturalOrder())));
        return copy;
    }

    private static String renderWriteUps(List<TSecreLevelGateSign> all) {
        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        StringBuilder sb = new StringBuilder();
        for (TSecreLevelGateSign s : all) {
            sb.append("闸").append(s.getGateNo()).append('/')
                    .append("回").append(s.getRoundNo()).append('/')
                    .append("序").append(s.getSignSeq() + 1).append(' ')
                    .append(s.getSignerNo()).append(' ')
                    .append(s.getVerdict() == LevelGateRule.WORD_YES ? "可" : "不可");
            if (s.getWord() != null && !s.getWord().trim().isEmpty()) {
                sb.append("：").append(s.getWord().trim());
            }
            sb.append(' ').append(f.format(s.getSignedAt())).append('\n');
        }
        return sb.toString();
    }
}
