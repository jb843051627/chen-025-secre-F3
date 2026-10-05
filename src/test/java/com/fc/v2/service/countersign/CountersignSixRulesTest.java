package com.fc.v2.service.countersign;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
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
import com.fc.v2.service.ITSecreCountersignService;
import com.fc.v2.service.ITSecreCarrierService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import javax.sql.DataSource;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 三道闸会签六则——真库（H2/MySQL 模式）端到端。
 */
@SpringJUnitConfig(CountersignTestConfig.class)
class CountersignSixRulesTest {

    @Autowired
    private DataSource dataSource;
    @Autowired
    private ITSecreCountersignService cs;
    @Autowired
    private TSecreCountersignMapper countersignMapper;
    @Autowired
    private TSecreCountersignSignMapper signMapper;
    @Autowired
    private TSecreLevelLogMapper levelLogMapper;
    @Autowired
    private TSecreCarrierMapper carrierMapper;
    @Autowired
    private ITSecreCarrierService carrierService;

    @BeforeEach
    void reset() throws Exception {
        try (Connection c = dataSource.getConnection()) {
            c.createStatement().execute("DROP TABLE IF EXISTS t_secre_level_log");
            c.createStatement().execute("DROP TABLE IF EXISTS t_secre_countersign_sign");
            c.createStatement().execute("DROP TABLE IF EXISTS t_secre_countersign");
            c.createStatement().execute("DROP TABLE IF EXISTS t_secre_carrier");
        }
        ScriptUtils.executeSqlScript(dataSource.getConnection(),
                new ClassPathResource("schema-h2-countersign.sql"));
    }

    private Long carrier(long id, String no, Integer level) {
        TSecreCarrier c = new TSecreCarrier();
        c.setId(id);
        c.setCarrierNo(no);
        c.setLevelNo(level);
        c.setDelFlag(0);
        carrierMapper.insert(c);
        return id;
    }

    private TSecreCountersign open(Long carrierId, int kind, Integer to) {
        return cs.openForCarrier(carrierId, kind, to, "起");
    }

    // ── 第一则：有关 ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("有关：空件、册面上没有的行带不出单")
    void rule1_openMustComeFromCarrierRow() {
        assertThrows(CountersignRuleException.class, () -> cs.openForCarrier(null, 1, 2, null));
        assertThrows(CountersignRuleException.class, () -> cs.openForCarrier(999L, 1, 2, null));
    }

    @Test
    @DisplayName("有关：同一件载体未齐闸的单只带得出一张")
    void rule1_oneOpenBillPerCarrier() {
        carrier(1L, "ZT-1", 1);
        TSecreCountersign first = open(1L, 1, 2);
        assertNotNull(first);
        assertThrows(CountersignRuleException.class, () -> open(1L, 1, 3));
        assertEquals(1, cs.openCount());
    }

    @Test
    @DisplayName("有关：抬要真抬、压要真压、解开目标级只能是0")
    void rule1_changeKindMustMatchLevels() {
        carrier(1L, "ZT-1", 2);
        assertThrows(CountersignRuleException.class, () -> open(1L, 1, 1)); // 抬却往下
        assertThrows(CountersignRuleException.class, () -> open(1L, 2, 3)); // 压却往上
        TSecreCountersign decrypt = open(1L, 3, null);
        assertEquals(0, decrypt.getLevelTo()); // 解开记0，报文给不给都不算
    }

    // ── 第二则：有人 ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("有人：每闸四项齐备，到了几名只回显、随落字累")
    void rule2_gateFourItemsAndArrivedIsEchoOnly() {
        carrier(1L, "ZT-1", 1);
        Long id = open(1L, 1, 2).getId();

        CountersignView v0 = cs.view(id);
        assertEquals(0, v0.getCurrentNode());
        GateState g0 = v0.getGates().get(0);
        assertEquals(1, g0.getNeedCount());
        assertEquals("ONE_PASS", g0.getPassRule());
        assertEquals(0, g0.getArrivedCount());
        assertFalse(g0.getPassed());

        cs.sign(id, "ban-yuan", true, "承办岗可");
        CountersignView v1 = cs.view(id);
        assertEquals(1, v1.getCurrentNode());
        assertEquals(1, v1.getGates().get(0).getArrivedCount());
        assertTrue(v1.getGates().get(0).getPassed());
        assertEquals(0, v1.getGates().get(1).getArrivedCount());
        assertEquals("ban-yuan", v1.getGates().get(0).getLines().get(0).getSigner());
    }

    // ── 第三则：分得清路子 ─────────────────────────────────────────────────────

    @Test
    @DisplayName("路子：头闸一名即过")
    void rule3_gate0OnePass() {
        carrier(1L, "ZT-1", 1);
        Long id = open(1L, 1, 2).getId();
        cs.sign(id, "ban", true, null);
        CountersignView v = cs.view(id);
        assertTrue(v.getGates().get(0).getPassed());
        assertEquals(1, v.getCurrentNode());
    }

    @Test
    @DisplayName("路子：次闸头名定论，头名可、后名不可也放过；两名的字两行都留底")
    void rule3_gate1FirstWinsBothKept() {
        carrier(1L, "ZT-1", 1);
        Long id = open(1L, 1, 2).getId();
        cs.sign(id, "ban", true, null);
        cs.sign(id, "bao-A", true, "我先看，可");
        cs.sign(id, "bao-B", false, "我有保留"); // 后名不空写：名数照累、原话留底
        CountersignView v = cs.view(id);
        GateState g1 = v.getGates().get(1);
        assertEquals(2, g1.getArrivedCount());
        assertTrue(g1.getPassed());
        assertEquals(1, g1.getFirstVerdict());
        assertEquals(2, g1.getLines().size());
        assertEquals(2, v.getCurrentNode());
    }

    @Test
    @DisplayName("路子：次闸候满两名，头名落不可即定论为不同意，压回头闸")
    void rule3_gate1FirstRejects() {
        carrier(1L, "ZT-1", 1);
        Long id = open(1L, 1, 2).getId();
        cs.sign(id, "ban", true, null);
        cs.sign(id, "bao-A", false, "不可");
        cs.sign(id, "bao-B", true, "我看可以"); // 候两名：后名这笔不空写，满了才按头名说法压回
        TSecreCountersign bill = countersignMapper.selectById(id);
        assertEquals(3, bill.getStatus().intValue());
        assertEquals(0, bill.getNodeNo().intValue());
        assertEquals(2, bill.getRoundNo().intValue());
        assertEquals(0, cs.view(id).getCurrentNode().intValue());
    }

    @Test
    @DisplayName("路子：末闸一名可不算过——末闸不兴拿头闸数法")
    void rule3_gate2NeedsTwo() {
        carrier(1L, "ZT-1", 1);
        Long id = fullToGate2(id_of(open(1L, 1, 2)));
        cs.sign(id, "ji-A", true, null);
        CountersignView v = cs.view(id);
        assertEquals(1, v.getGates().get(2).getArrivedCount());
        assertFalse(v.getGates().get(2).getPassed());
        assertEquals(2, v.getCurrentNode());
    }

    @Test
    @DisplayName("路子：末闸一可一不可挂待议，不挪不了断，新笔不作数")
    void rule3_gate2SplitHangs() {
        carrier(1L, "ZT-1", 1);
        Long id = fullToGate2(id_of(open(1L, 1, 2)));
        cs.sign(id, "ji-A", true, null);
        cs.sign(id, "ji-B", false, null);
        CountersignView v = cs.view(id);
        assertEquals(1, v.getStatus().intValue());
        assertEquals("待议", v.getStatusText());
        assertEquals(2, v.getCurrentNode());
        assertFalse(v.getSealed());
        assertThrows(CountersignRuleException.class, () -> cs.sign(id, "ji-C", true, null));
    }

    @Test
    @DisplayName("路子：同瞬两笔各按先后，seq 不并成一句；同一人本轮不许追第二笔")
    void rule3_seqAndNoDoubleSign() {
        carrier(1L, "ZT-1", 1);
        Long id = open(1L, 1, 2).getId();
        cs.sign(id, "ban", true, null);
        cs.sign(id, "bao-A", true, null);
        // 次闸还只点到一名时，bao-A 当场再补一笔须挡住（追补签不留门）
        assertThrows(CountersignRuleException.class, () -> cs.sign(id, "bao-A", true, "再补一笔"));
        cs.sign(id, "bao-B", true, null);
        List<TSecreCountersignSign> rows = signMapper.selectList(new QueryWrapper<TSecreCountersignSign>()
                .eq("cs_id", id).eq("node_no", 1).orderByAsc("seq_no"));
        assertEquals(2, rows.size());
        assertEquals(1, rows.get(0).getSeqNo().intValue());
        assertEquals(2, rows.get(1).getSeqNo().intValue());
        assertEquals("bao-A", rows.get(0).getSigner());
        assertEquals("bao-B", rows.get(1).getSigner());
    }

    @Test
    @DisplayName("路子：次闸没点满，受理簿里末闸一笔也不会有，单钉在次闸")
    void rule3_noLeapingWithoutPriorGateFull() {
        carrier(2L, "ZT-2", 1);
        TSecreCountersign bill = open(2L, 1, 2);
        cs.sign(bill.getId(), "ban", true, null);     // 头闸放过
        cs.sign(bill.getId(), "anyone", true, null);  // 再来一笔只落在次闸
        CountersignView v = cs.view(bill.getId());
        assertEquals(1, v.getCurrentNode().intValue());
        assertEquals(1, v.getGates().get(1).getArrivedCount());
        assertEquals(0, v.getGates().get(2).getArrivedCount());
        // 末闸受理簿一行都没有——越过次闸从末闸递的笔，库里根本无处落脚
        assertEquals(0, signMapper.selectCount(new QueryWrapper<TSecreCountersignSign>()
                .eq("cs_id", bill.getId()).eq("node_no", 2)).intValue());
    }

    // ── 第四则：退得干净 ──────────────────────────────────────────────────────

    @Test
    @DisplayName("退干净：末闸同不可只抹本闸本轮、压回次闸，旧字一个不动")
    void rule4_pressBackOneGateKeepsOld() {
        carrier(1L, "ZT-1", 1);
        TSecreCountersign bill0 = open(1L, 1, 2);
        Long id = bill0.getId();
        fullToGate2(id);
        cs.sign(id, "ji-A", false, null);
        cs.sign(id, "ji-B", false, null); // 两名同不可

        TSecreCountersign bill = countersignMapper.selectById(id);
        assertEquals(3, bill.getStatus().intValue());
        assertEquals(1, bill.getNodeNo().intValue()); // 往回只收一闸：回到次闸
        assertEquals(2, bill.getRoundNo().intValue());

        // 本闸本轮两笔账内抹除，行还在
        List<TSecreCountersignSign> lastRound1 = signMapper.selectList(new QueryWrapper<TSecreCountersignSign>()
                .eq("cs_id", id).eq("round_no", 1).eq("node_no", 2));
        assertEquals(2, lastRound1.size());
        assertTrue(lastRound1.stream().allMatch(r -> r.getVoided() == 1));
        // 此前各闸旧字照旧存底、未抹
        assertEquals(1, countSign(id, 1, 0, 0));
        assertEquals(2, countSign(id, 1, 1, 0));
        // 单停回次闸，本轮重头补；次闸旧轮字不再算数
        assertEquals(1, cs.view(id).getCurrentNode().intValue());
        assertEquals(0, cs.view(id).getGates().get(1).getArrivedCount());
    }

    @Test
    @DisplayName("退干净：重走时本轮与上轮各放各格，补齐后照常齐闸，旧笔仍摆着")
    void rule4_rewalkThenSeal() {
        carrier(1L, "ZT-1", 1);
        Long id = fullToGate2(id_of(open(1L, 1, 2)));
        cs.sign(id, "ji-A", false, null);
        cs.sign(id, "ji-B", false, null); // 压回次闸，进第2轮

        // 单停回次闸：头闸旧字仍算数，次闸本轮重头补两名
        assertEquals(1, cs.view(id).getCurrentNode().intValue());
        cs.sign(id, "bao-C", true, null);
        cs.sign(id, "bao-D", true, null);
        // 末闸本轮两名同可
        cs.sign(id, "ji-C", true, null);
        cs.sign(id, "ji-D", true, null);

        CountersignView v = cs.view(id);
        assertTrue(v.getSealed());
        // 次闸两行旧轮字（未抹，因压回的是末闸）+本轮新字各放各格
        List<GateState> gates = v.getGates();
        Set<Integer> roundsAtGate1 = gates.get(1).getLines().stream()
                .map(l -> l.getRoundNo()).collect(Collectors.toSet());
        assertTrue(roundsAtGate1.contains(1));
        assertTrue(roundsAtGate1.contains(2));
        // 到了几名只点本轮生效的两名
        assertEquals(2, gates.get(1).getArrivedCount());
        // 末闸旧轮两笔账内抹、新轮两笔生效，屏上四行都摆着
        assertEquals(4, gates.get(2).getLines().size());
        assertEquals(2, gates.get(2).getArrivedCount());
    }

    @Test
    @DisplayName("退干净：次闸驳回压回头闸后，头闸须本轮重落，旧轮次闸字只留底不算数")
    void rule4_rejectAtGate1RestartsAtGate0() {
        carrier(1L, "ZT-1", 1);
        Long id = id_of(open(1L, 1, 2));
        cs.sign(id, "ban", true, null);
        cs.sign(id, "bao-A", false, "不可");
        cs.sign(id, "bao-B", true, "我看可以"); // 候满两名才按头名说法压回头闸，第2轮

        CountersignView v = cs.view(id);
        assertEquals(0, v.getCurrentNode().intValue());
        assertEquals(0, v.getGates().get(0).getArrivedCount()); // 头闸本轮重头
        assertEquals(0, v.getGates().get(1).getArrivedCount());
        // 旧字照旧存底：头闸旧笔未抹、次闸本轮两笔账内抹
        assertEquals(1, countSign(id, 1, 0, 0));
        assertEquals(2, countSign(id, 1, 1, 1));

        cs.sign(id, "ban2", true, null);
        assertEquals(1, cs.view(id).getCurrentNode().intValue());
    }

    @Test
    @DisplayName("退干净：连着两遭被压回（末闸、次闸），第三轮仍能一路走齐，旧字全留底")
    void rule4_twoRejectionsThenSeal() {
        carrier(1L, "ZT-1", 1);
        Long id = id_of(open(1L, 1, 2));
        // 第1轮：走到末闸，同不可，压回次闸
        fullToGate2(id);
        cs.sign(id, "ji-A", false, null);
        cs.sign(id, "ji-B", false, null);
        // 第2轮：次闸头名又不可，压回头闸
        cs.sign(id, "bao-C", false, null);
        cs.sign(id, "bao-D", true, null);
        assertEquals(0, cs.view(id).getCurrentNode().intValue());
        assertEquals(3, countersignMapper.selectById(id).getRoundNo().intValue());
        // 第3轮：一路同可，齐闸
        fullSealed(id);
        CountersignView v = cs.view(id);
        assertTrue(v.getSealed());
        assertEquals(3, v.getRoundNo().intValue());
        assertEquals(2, carrierMapper.selectById(1L).getLevelNo().intValue());
        // 三轮的字都在：末闸第1轮两笔账内抹、次闸第2轮两笔账内抹、第3轮各闸生效
        assertEquals(2, countSign(id, 1, 2, 1));
        assertEquals(2, countSign(id, 2, 1, 1));
        assertEquals(1, v.getGates().get(0).getArrivedCount());
        assertEquals(2, v.getGates().get(1).getArrivedCount());
        assertEquals(2, v.getGates().get(2).getArrivedCount());
    }

    // ── 第五则：封得住 ────────────────────────────────────────────────────────
    @Test
    @DisplayName("封得住：末闸两名同可即齐闸，封单+换密级+叠履历同一回办成")
    void rule5_sealChangesLevelAndAppendsLog() {
        carrier(1L, "ZT-1", 1);
        Long id = openAndSeal(1L);
        CountersignView v = cs.view(id);
        assertTrue(v.getSealed());
        assertNull(v.getCurrentNode());
        assertNotNull(v.getSealTime());

        TSecreCarrier c = carrierMapper.selectById(1L);
        assertEquals(2, c.getLevelNo().intValue()); // 密级栏跟着换过去
        List<TSecreLevelLog> logs = levelLogMapper.selectList(new QueryWrapper<TSecreLevelLog>()
                .eq("carrier_id", 1L).orderByDesc("seal_time"));
        assertEquals(1, logs.size());
        TSecreLevelLog log = logs.get(0);
        assertEquals(1, log.getLevelFrom().intValue());
        assertEquals(2, log.getLevelTo().intValue());
        assertEquals(id, log.getCsId());
        assertTrue(log.getSigners().contains("ban"));
        assertTrue(log.getSigners().contains("bao-A"));
        assertTrue(log.getSigners().contains("ji-A"));
        assertEquals(0, cs.openCount()); // 齐闸出账
    }

    @Test
    @DisplayName("封得住：齐闸后一笔进不来；借载体通用编辑口手改密级不作数")
    void rule5_sealedIsImmutableAndNoManualLevelEdit() {
        carrier(1L, "ZT-1", 1);
        Long id = openAndSeal(1L);
        assertThrows(CountersignRuleException.class, () -> cs.sign(id, "someone", true, null));

        TSecreCarrier edit = new TSecreCarrier();
        edit.setId(1L);
        edit.setLevelNo(3);
        assertThrows(CountersignRuleException.class, () -> carrierService.updateTSecreCarrier(edit));
        assertEquals(2, carrierMapper.selectById(1L).getLevelNo().intValue());
    }

    @Test
    @DisplayName("封得住：封单途中载体行不见，整回算作废（密级不换、单不封、签字回滚）")
    void rule5_sealFailureRollsBackTogether() {
        carrier(1L, "ZT-1", 1);
        Long id = fullToGate2(id_of(open(1L, 1, 2)));
        cs.sign(id, "ji-A", true, null);
        carrierMapper.deleteById(1L); // 齐闸前一刻载体行没了
        assertThrows(CountersignRuleException.class, () -> cs.sign(id, "ji-B", true, null));

        TSecreCountersign bill = countersignMapper.selectById(id);
        assertNotEquals(2, bill.getStatus()); // 没封成
        // ji-B 那笔随事务回滚，末闸仍只点到一名
        assertEquals(1, cs.view(id).getGates().get(2).getArrivedCount());
        assertEquals(0, levelLogMapper.selectCount(new QueryWrapper<TSecreLevelLog>().eq("cs_id", id)));
    }

    @Test
    @DisplayName("封得住：同件载体齐闸后可再起一张，履历一段段倒着捋")
    void rule5_reopenAfterSealAndHistory() {
        carrier(1L, "ZT-1", 1);
        openAndSeal(1L); // 1→2
        TSecreCountersign second = open(1L, 1, 3); // 2→3
        fullSealed(second.getId());
        assertEquals(3, carrierMapper.selectById(1L).getLevelNo().intValue());
        List<TSecreLevelLog> logs = cs.historyOfCarrier(1L);
        assertEquals(2, logs.size());
        assertEquals(3, logs.get(0).getLevelTo().intValue()); // 最新在头里
        assertEquals(2, logs.get(1).getLevelTo().intValue());
    }

    // ── 第六则：数得合 ────────────────────────────────────────────────────────

    @Test
    @DisplayName("数得合：在办各张回数都对得齐，齐闸的单拎出来也站得住")
    void rule6_reconcileClean() {
        carrier(1L, "ZT-1", 1);
        carrier(2L, "ZT-2", 2);
        openAndSeal(1L);
        Long open1 = id_of(open(2L, 2, 1));
        cs.sign(open1, "ban", true, null); // 停在次闸

        ReconReport report = cs.reconcileAll();
        assertTrue(report.getOk(), () -> String.join(";", report.getProblems()));
        assertEquals(1, report.getBooked().intValue());
        assertEquals(1, report.getCounted().intValue());
        assertTrue(cs.reconcile(open1).getOk());
    }

    @Test
    @DisplayName("数得合：册面被改得与受理簿不一致，点名报出是哪一张哪一格")
    void rule6_reconcileNamesTheBillWhenTampered() {
        carrier(1L, "ZT-1", 1);
        Long id = id_of(open(1L, 1, 2));
        cs.sign(id, "ban", true, null); // 实际停在次闸
        TSecreCountersign tamper = countersignMapper.selectById(id);
        tamper.setNodeNo(2); // 账面被挪到末闸
        countersignMapper.updateById(tamper);

        ReconReport report = cs.reconcileAll();
        assertFalse(report.getOk());
        assertTrue(report.getProblems().stream().anyMatch(p -> p.contains("停留闸") && p.contains(tamper.getCsNo())));

        TSecreCountersign tamper2 = countersignMapper.selectById(id);
        tamper2.setNodeNo(1);
        tamper2.setStatus(2); // 假装齐闸
        countersignMapper.updateById(tamper2);
        ReconReport report2 = cs.reconcile(id);
        assertFalse(report2.getOk());
        assertTrue(report2.getProblems().stream().anyMatch(p -> p.contains("情形")));
    }

    @Test
    @DisplayName("数得合：齐闸却缺履历/密级没换，对得齐要能报出")
    void rule6_reconcileCatchesMissingLog() {
        carrier(1L, "ZT-1", 1);
        Long id = openAndSeal(1L);
        levelLogMapper.delete(new QueryWrapper<TSecreLevelLog>().eq("cs_id", id));
        ReconReport r1 = cs.reconcile(id);
        assertFalse(r1.getOk());
        assertTrue(r1.getProblems().stream().anyMatch(p -> p.contains("履历条数")));
    }

    @Test
    @DisplayName("数得合：待议的单不挪不了断，仍占一张在办账，回数也对得齐")
    void rule6_pendingStillCountedOpen() {
        carrier(1L, "ZT-1", 1);
        Long id = fullToGate2(id_of(open(1L, 1, 2)));
        cs.sign(id, "ji-A", true, null);
        cs.sign(id, "ji-B", false, null);
        assertEquals(1, cs.openCount());
        assertTrue(cs.reconcileAll().getOk());
    }

    @Test
    @DisplayName("暗门：接口只有起单与落字，没有另起单/改旧笔/追补签/删单的口")
    void rule6_noBackdoorMethods() {
        Set<String> allowed = new HashSet<>(Arrays.asList(
                "openForCarrier", "sign", "view", "list", "openCount",
                "reconcile", "reconcileAll", "historyOfCarrier"));
        Set<String> actual = Arrays.stream(ITSecreCountersignService.class.getMethods())
                .map(Method::getName).collect(Collectors.toSet());
        assertEquals(allowed, actual);
    }

    // ── 造数帮手 ──────────────────────────────────────────────────────────────

    private Long id_of(TSecreCountersign bill) {
        return bill.getId();
    }

    private int countSign(Long csId, int round, int node, int voided) {
        Integer n = signMapper.selectCount(new QueryWrapper<TSecreCountersignSign>()
                .eq("cs_id", csId).eq("round_no", round).eq("node_no", node).eq("voided", voided));
        return n == null ? 0 : n;
    }

    /** 一路点到末闸候签：头闸1名 + 次闸2名（头名可）。 */
    private Long fullToGate2(Long id) {
        cs.sign(id, "ban", true, null);
        cs.sign(id, "bao-A", true, null);
        cs.sign(id, "bao-B", true, null);
        return id;
    }

    /** 起一张抬 1→2 的单并一路点到齐闸。 */
    private Long openAndSeal(Long carrierId) {
        Long newId = open(carrierId, 1, 2).getId();
        return fullSealed(newId);
    }

    /** 从一张在办单当前所停闸起，按各闸该有的名数补齐到齐闸封住。 */
    private Long fullSealed(Long openCsId) {
        String[][] hands = {{"ban"}, {"bao-A", "bao-B"}, {"ji-A", "ji-B"}};
        int[] used = new int[3];
        for (int i = 0; i < 6; i++) {
            CountersignView v = cs.view(openCsId);
            if (Boolean.TRUE.equals(v.getSealed())) {
                break;
            }
            int gate = v.getCurrentNode();
            cs.sign(openCsId, hands[gate][used[gate]++], true, null);
        }
        assertTrue(cs.view(openCsId).getSealed(), "造数：此单应已齐闸");
        return openCsId;
    }
}
