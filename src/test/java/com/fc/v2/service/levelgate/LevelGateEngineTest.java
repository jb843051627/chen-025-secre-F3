package com.fc.v2.service.levelgate;

import com.fc.v2.model.auto.TSecreCarrier;
import com.fc.v2.model.auto.TSecreLevelGateBill;
import com.fc.v2.model.auto.TSecreLevelGateSign;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 三闸规矩六条与石桥区事故情形的验证。不挂库、不挂 Spring。
 *
 * @author fuce
 * @date 2026-10-05
 */
class LevelGateEngineTest {

    private InMemoryLevelGateRepository repo;
    private AtomicLong clock;
    private LevelGateEngine engine;

    @BeforeEach
    void setUp() {
        repo = new InMemoryLevelGateRepository();
        repo.putCarrier(1L, "ZT-001", LevelGateRule.LVL_SECRET);
        clock = new AtomicLong(1_700_000_000_000L);
        engine = new LevelGateEngine(repo, clock::get);
    }

    private LevelGateView openBill(int changeKind) {
        return engine.open(1L, changeKind, null);
    }

    private void tick() {
        clock.incrementAndGet();
    }

    private long onlyBillId() {
        assertEquals(1, repo.bills.size());
        return repo.bills.keySet().iterator().next();
    }

    // —— 要有关 ——

    @Test
    @DisplayName("要有关：单只能由册面行带出；点不到的行带不出单")
    void openMustComeFromCarrierRow() {
        LevelGateException e = assertThrows(LevelGateException.class,
                () -> engine.open(999L, LevelGateRule.CHANGE_UP, null));
        assertEquals("CARRIER_NOT_FOUND", e.getCode());
        assertTrue(repo.bills.isEmpty());
    }

    @Test
    @DisplayName("要有关：同一载体在办时，第二张单带不出来（手工另起的一张不认）")
    void onlyOneOpenBillPerCarrier() {
        openBill(LevelGateRule.CHANGE_UP);
        LevelGateException e = assertThrows(LevelGateException.class,
                () -> openBill(LevelGateRule.CHANGE_UP));
        assertEquals("OPEN_BILL_EXISTS", e.getCode());
    }

    // —— 头闸 ——

    @Test
    @DisplayName("头闸只候一名：那一名落「可」，这道闸即放过，单挪到次闸")
    void gateOneOneSignerPasses() {
        long id = onlyBillIdAfterOpen();
        tick();
        LevelGateView v = engine.sign(id, "banshi-yuan", LevelGateRule.WORD_YES, "拟抬为机密");
        assertEquals(1, v.getStopGateNo());
        assertEquals(1, v.getGates().get(0).getArrivedCount());
        assertTrue(v.getGates().get(0).isFull());
    }

    private long onlyBillIdAfterOpen() {
        openBill(LevelGateRule.CHANGE_UP);
        return onlyBillId();
    }

    // —— 石桥区事故 ——

    @Test
    @DisplayName("石桥区事故：次闸的字落了、末闸还空着，绝不许当整张已过；册面不动、不封存")
    void shiqiaoCaseLastGateEmptyIsNotSealed() {
        long id = onlyBillIdAfterOpen();
        // 闸0
        tick(); engine.sign(id, "banshi-yuan", LevelGateRule.WORD_YES, null);
        // 闸1 两名
        tick(); engine.sign(id, "baomi-A", LevelGateRule.WORD_YES, "同意");
        tick(); engine.sign(id, "baomi-B", LevelGateRule.WORD_NO, "我留个痕");
        // 末闸一个字没落 —— 单必须停在末闸候签
        LevelGateView v = engine.view(id);
        assertEquals(2, v.getStopGateNo());
        assertEquals(LevelGateRule.ST_WAITING, v.getGateStatus());
        assertNull(v.getToLevel());
        assertEquals(LevelGateRule.LVL_SECRET, repo.lockCarrier(1L).getLevelNo());
        assertEquals(1, engine.openCount());
        assertTrue(engine.reconcile().isEmpty());
    }

    // —— 次闸：头名定论、后笔留痕 ——

    @Test
    @DisplayName("次闸头名「可」即为定论；后一笔不空写：名数照累、两行都摆着，单往前挪")
    void gateTwoFirstWinsSecondKeepsTrace() {
        long id = onlyBillIdAfterOpen();
        tick(); engine.sign(id, "banshi-yuan", LevelGateRule.WORD_YES, null);
        tick(); LevelGateView v1 = engine.sign(id, "baomi-A", LevelGateRule.WORD_YES, "头一个说可");
        assertEquals(1, v1.getGates().get(1).getArrivedCount());
        tick(); LevelGateView v = engine.sign(id, "baomi-B", LevelGateRule.WORD_NO, "后一笔说不可");
        assertEquals(2, v.getGates().get(1).getArrivedCount());
        assertEquals(2, v.getGates().get(1).getRounds().get(0).getSigns().size());
        assertEquals(2, v.getStopGateNo());
        // 头名定论是「可」
        assertEquals(LevelGateRule.WORD_YES, v.getGates().get(1).getFirstVerdict());
    }

    @Test
    @DisplayName("次闸头名「不可」即定论：压回头闸，本闸本轮两字抹净、回次加一，头闸旧字留底")
    void gateTwoFirstNoPressesBackOnce() {
        long id = onlyBillIdAfterOpen();
        tick(); engine.sign(id, "banshi-yuan", LevelGateRule.WORD_YES, "头闸旧字");
        tick(); engine.sign(id, "baomi-A", LevelGateRule.WORD_NO, "头一个就说不可");
        tick(); engine.sign(id, "baomi-B", LevelGateRule.WORD_YES, "后来说可也没用");

        LevelGateView v = engine.view(id);
        assertEquals(0, v.getStopGateNo(), "往回只收一闸：停回头闸");
        assertEquals(2, v.getRoundNo(), "重头往上走，回次加一");
        // 次闸本轮的字抹了个干净
        assertEquals(0, v.getGates().get(1).getArrivedCount());
        assertTrue(repo.listSigns(id).stream().noneMatch(s -> s.getGateNo() == 1 && s.getRoundNo() == 1));
        // 头闸旧字一个不动、照旧存底（回1）
        LevelGateView.RoundView g0old = v.getGates().get(0).getRounds().stream()
                .filter(r -> r.getRoundNo() == 1).findFirst().orElseThrow(AssertionError::new);
        assertEquals(1, g0old.getSigns().size());
        assertEquals("头闸旧字", g0old.getSigns().get(0).getWord());
    }

    // —— 末闸：同可 / 分歧待议 / 同不可压回 ——

    private long passToGate2() {
        long id = onlyBillIdAfterOpen();
        tick(); engine.sign(id, "banshi-yuan", LevelGateRule.WORD_YES, null);
        tick(); engine.sign(id, "baomi-A", LevelGateRule.WORD_YES, null);
        tick(); engine.sign(id, "baomi-B", LevelGateRule.WORD_YES, null);
        return id;
    }

    @Test
    @DisplayName("末闸两句都写「可」才齐闸：密级换、履历叠、计数减一、此后一笔进不来")
    void gateThreeBothAgreeSeals() {
        long id = passToGate2();
        tick(); engine.sign(id, "shangji-A", LevelGateRule.WORD_YES, "可");
        tick(); LevelGateView v = engine.sign(id, "shangji-B", LevelGateRule.WORD_YES, "可");

        assertEquals(3, v.getStopGateNo());
        assertEquals(LevelGateRule.ST_SEALED, v.getGateStatus());
        assertEquals(LevelGateRule.LVL_CONFIDENTIAL, repo.lockCarrier(1L).getLevelNo());
        assertEquals(1, repo.countLogs(id), "履历恰为一条");
        assertEquals(0, engine.openCount(), "齐闸随减一");
        assertTrue(engine.reconcile().isEmpty());

        LevelGateException sealed = assertThrows(LevelGateException.class,
                () -> engine.sign(id, "shangji-A", LevelGateRule.WORD_YES, null));
        assertEquals("ALREADY_SEALED", sealed.getCode());
    }

    @Test
    @DisplayName("末闸一句可、一句不可：挂待议，不往前挪、也不就此了断，本回不再进笔")
    void gateThreeSplitHangsPending() {
        long id = passToGate2();
        tick(); engine.sign(id, "shangji-A", LevelGateRule.WORD_YES, "可");
        tick(); LevelGateView v = engine.sign(id, "shangji-B", LevelGateRule.WORD_NO, "不可");

        assertEquals(2, v.getStopGateNo());
        assertEquals(LevelGateRule.ST_PENDING, v.getGateStatus());
        assertEquals(LevelGateRule.LVL_SECRET, repo.lockCarrier(1L).getLevelNo());
        assertEquals(1, engine.openCount(), "待议的单仍算在办，不作了断");
        LevelGateException e = assertThrows(LevelGateException.class,
                () -> engine.sign(id, "shangji-A", LevelGateRule.WORD_YES, null));
        assertEquals("GATE_PENDING", e.getCode());
    }

    @Test
    @DisplayName("末闸两句都「不可」：压回次一闸，只抹末闸本轮，前两闸旧字照旧存底")
    void gateThreeBothNoPressesBackToGate1() {
        long id = passToGate2();
        tick(); engine.sign(id, "shangji-A", LevelGateRule.WORD_NO, "不可");
        tick(); engine.sign(id, "shangji-B", LevelGateRule.WORD_NO, "也不可");

        LevelGateView v = engine.view(id);
        assertEquals(1, v.getStopGateNo(), "往回只收一闸：停回次闸");
        assertEquals(2, v.getRoundNo());
        assertTrue(repo.listSigns(id).stream().noneMatch(s -> s.getGateNo() == 2 && s.getRoundNo() == 1));
        // 头闸、次闸旧字都还在
        assertEquals(1, countSigns(id, 0, 1));
        assertEquals(2, countSigns(id, 1, 1));
    }

    // —— 追补签 / 重名 ——

    @Test
    @DisplayName("同一人本闸本回不许落第二笔：追补签这道暗门不留")
    void duplicateSignerInSameRoundRejected() {
        long id = onlyBillIdAfterOpen();
        tick(); engine.sign(id, "banshi-yuan", LevelGateRule.WORD_YES, null);
        // 单已到次闸；此人在次闸是新一闸，可以落——但在头闸同回再落不行（直接造不出来，故验次闸同人两笔）
        tick(); engine.sign(id, "baomi-A", LevelGateRule.WORD_YES, null);
        LevelGateException e = assertThrows(LevelGateException.class,
                () -> engine.sign(id, "baomi-A", LevelGateRule.WORD_YES, "再补一笔"));
        assertEquals("DUPLICATE_SIGN", e.getCode());
    }

    // —— 同瞬两名 ——

    @Test
    @DisplayName("两名挤在同一瞬递进来：同一时刻各落各的行，序为 0、1，不并成一句")
    void sameInstantTwoSignersKeptSeparate() {
        long id = passToGate2();
        // 钟停住不动，两笔同一毫秒
        engine.sign(id, "shangji-A", LevelGateRule.WORD_YES, "甲");
        engine.sign(id, "shangji-B", LevelGateRule.WORD_YES, "乙");
        List<TSecreLevelGateSign> gate2 = repo.listSigns(id);
        long n2 = gate2.stream().filter(s -> s.getGateNo() == 2).count();
        assertEquals(2, n2);
        TSecreLevelGateSign a = gate2.stream().filter(s -> s.getGateNo() == 2 && s.getSignSeq() == 0)
                .findFirst().orElseThrow(AssertionError::new);
        TSecreLevelGateSign b = gate2.stream().filter(s -> s.getGateNo() == 2 && s.getSignSeq() == 1)
                .findFirst().orElseThrow(AssertionError::new);
        assertEquals(a.getSignedAt(), b.getSignedAt());
        assertNotEquals(a.getSignerNo(), b.getSignerNo());
        // 齐闸了
        assertEquals(LevelGateRule.GATE_SEALED, repo.lockBill(id).getGateNo());
    }

    // —— 压回后重走，各回各格 ——

    @Test
    @DisplayName("压回后重头往上走：本轮与上轮各放各格，哪天补的各有凭据；再齐闸仍只叠一条履历")
    void roundsKeptInSeparateCellsThenSeal() {
        long id = passToGate2();
        tick(); engine.sign(id, "shangji-A", LevelGateRule.WORD_NO, "不可");
        tick(); engine.sign(id, "shangji-B", LevelGateRule.WORD_NO, "不可");
        // 停回闸1 回次2
        LevelGateView back = engine.view(id);
        assertEquals(1, back.getStopGateNo());
        assertEquals(2, back.getRoundNo());

        // 次闸在新回次重走（头名可）
        tick(); engine.sign(id, "baomi-C", LevelGateRule.WORD_YES, "回二·可");
        tick(); engine.sign(id, "baomi-D", LevelGateRule.WORD_NO, "回二·留痕");
        // 末闸新回次两名同可
        tick(); engine.sign(id, "shangji-A", LevelGateRule.WORD_YES, "回二·可");
        tick(); LevelGateView sealed = engine.sign(id, "shangji-B", LevelGateRule.WORD_YES, "回二·可");

        assertEquals(LevelGateRule.GATE_SEALED, sealed.getStopGateNo());
        LevelGateView.GateView g1 = sealed.getGates().get(1);
        assertEquals(2, g1.getRounds().size(), "次闸摆得出两回");
        assertEquals(2, g1.getArrivedCount(), "当下数只点最新一回");
        assertEquals(1, repo.countLogs(id));
        assertEquals(LevelGateRule.LVL_CONFIDENTIAL, repo.lockCarrier(1L).getLevelNo());
        assertTrue(engine.reconcile().isEmpty());
    }

    // —— 走法与定级 ——

    @Test
    @DisplayName("往下压与整个解开都由同一回计算定级；册面与履历口径一致")
    void downAndUnsealLevels() {
        // 往下压：先抬到机密并齐闸
        TSecreCarrier c = repo.lockCarrier(1L);
        c.setLevelNo(LevelGateRule.LVL_CONFIDENTIAL);
        long id = engine.open(1L, LevelGateRule.CHANGE_DOWN, null).getBillId();
        walkAllYes(id);
        assertEquals(LevelGateRule.LVL_SECRET, repo.lockCarrier(1L).getLevelNo());

        // 同一载体齐闸后可再起新单；整个解开
        long id2 = engine.open(1L, LevelGateRule.CHANGE_UNSEAL, null).getBillId();
        walkAllYes(id2);
        assertEquals(LevelGateRule.LVL_DECLASSIFIED, repo.lockCarrier(1L).getLevelNo());

        LevelGateException e = assertThrows(LevelGateException.class,
                () -> engine.open(1L, LevelGateRule.CHANGE_UNSEAL, null));
        assertEquals("CHANGE_KIND", e.getCode());
        assertEquals(0, engine.openCount());
    }

    @Test
    @DisplayName("绝密抬不上去；无密级不许往下压")
    void illegalChangeRejected() {
        repo.lockCarrier(1L).setLevelNo(LevelGateRule.LVL_TOP_SECRET);
        assertEquals("CHANGE_KIND", assertThrows(LevelGateException.class,
                () -> engine.open(1L, LevelGateRule.CHANGE_UP, null)).getCode());
        repo.lockCarrier(1L).setLevelNo(LevelGateRule.LVL_SECRET);
        assertEquals("CHANGE_KIND", assertThrows(LevelGateException.class,
                () -> engine.open(1L, LevelGateRule.CHANGE_DOWN, null)).getCode());
    }

    // —— 已签数不可手填：接口根本不收闸次/名数 ——

    @Test
    @DisplayName("递笔不收闸次：一笔一笔只跨紧邻的下一截，报文想指到末闸也递不进去；怪字不落账")
    void signAlwaysLandsOnStopGate() {
        long id = onlyBillIdAfterOpen();
        tick(); LevelGateView v0 = engine.sign(id, "banshi-yuan", LevelGateRule.WORD_YES, null);
        assertEquals(1, v0.getStopGateNo(), "头闸满，只跨到紧邻的次闸");

        tick(); LevelGateView v1a = engine.sign(id, "baomi-A", LevelGateRule.WORD_YES, null);
        assertEquals(1, v1a.getStopGateNo(), "次闸只到一名，名数没一般多，单不挪");
        assertEquals(1, v1a.getGates().get(1).getArrivedCount());

        tick(); LevelGateView v1b = engine.sign(id, "baomi-B", LevelGateRule.WORD_NO, "留痕");
        assertEquals(2, v1b.getStopGateNo(), "次闸头名定论是可，两名点满才跨末闸");

        assertThrows(LevelGateException.class, () -> engine.sign(id, "shangji-A", 7, null));
        assertEquals(2, engine.view(id).getStopGateNo(), "不认的字不落账，单仍停末闸候签");
    }

    @Test
    @DisplayName("在办张数随起单加一、随齐闸减一，屏上数与逐闸点数同源")
    void openCountTracksLifecycle() {
        assertEquals(0, engine.openCount());
        long id = onlyBillIdAfterOpen();
        assertEquals(1, engine.openCount());
        walkAllYes(id);
        assertEquals(0, engine.openCount());
    }

    @Test
    @DisplayName("履历一条记得齐：从哪级到哪级、凭哪张单、谁的字、什么时刻，可倒着捋")
    void logRecordsFromToBillSignersAndTime() {
        long id = onlyBillIdAfterOpen();
        walkAllYes(id);
        assertEquals(1, repo.logs.size());
        String writeUps = repo.logs.get(0).getWriteUps();
        assertTrue(writeUps.contains("banshi-yuan"));
        assertTrue(writeUps.contains("baomi-A"));
        assertTrue(writeUps.contains("shangji-B"));
        assertEquals(id, repo.logs.get(0).getBillId());
        assertEquals(LevelGateRule.LVL_SECRET, repo.logs.get(0).getFromLevel());
        assertEquals(LevelGateRule.LVL_CONFIDENTIAL, repo.logs.get(0).getToLevel());
    }

    @Test
    @DisplayName("次闸头名不可、后笔可，仍以头名为定论（不许拿末闸数法把次闸压成同可）")
    void gateTwoModesDoNotBleed() {
        long id = onlyBillIdAfterOpen();
        tick(); engine.sign(id, "banshi-yuan", LevelGateRule.WORD_YES, null);
        tick(); engine.sign(id, "baomi-A", LevelGateRule.WORD_NO, "先落：不可");
        tick(); engine.sign(id, "baomi-B", LevelGateRule.WORD_YES, "后落：可");
        // 次闸不兴拿末闸「同可」数法：有可也没用，头名不可即压回
        assertEquals(0, engine.view(id).getStopGateNo());
        assertFalse(repo.lockBill(id).getGateStatus() == LevelGateRule.ST_PENDING,
                "次闸不许出现末闸才有的「挂待议」");
    }

    private void walkAllYes(long id) {
        tick(); engine.sign(id, "banshi-yuan", LevelGateRule.WORD_YES, null);
        tick(); engine.sign(id, "baomi-A", LevelGateRule.WORD_YES, null);
        tick(); engine.sign(id, "baomi-B", LevelGateRule.WORD_YES, null);
        tick(); engine.sign(id, "shangji-A", LevelGateRule.WORD_YES, null);
        tick(); engine.sign(id, "shangji-B", LevelGateRule.WORD_YES, null);
    }

    private int countSigns(long billId, int gate, int round) {
        return (int) repo.listSigns(billId).stream()
                .filter(s -> s.getGateNo() == gate && s.getRoundNo() == round).count();
    }
}
