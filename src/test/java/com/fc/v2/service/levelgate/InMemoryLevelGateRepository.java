package com.fc.v2.service.levelgate;

import com.fc.v2.model.auto.TSecreCarrier;
import com.fc.v2.model.auto.TSecreCarrierLevelLog;
import com.fc.v2.model.auto.TSecreLevelGateBill;
import com.fc.v2.model.auto.TSecreLevelGateSign;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 内存账本：不挂库、不挂 Spring，把引擎的六则规矩在测试里一五一十走一遍。
 * 方法同步，模拟「先锁册面行、再锁单据行」之后锁内点笔的先后。
 *
 * @author fuce
 * @date 2026-10-05
 */
public class InMemoryLevelGateRepository implements LevelGateRepository {

    final Map<Long, TSecreCarrier> carriers = new LinkedHashMap<>();
    final Map<Long, TSecreLevelGateBill> bills = new LinkedHashMap<>();
    final List<TSecreLevelGateSign> signs = new ArrayList<>();
    final List<TSecreCarrierLevelLog> logs = new ArrayList<>();

    public TSecreCarrier putCarrier(long id, String carrierNo, Integer level) {
        TSecreCarrier c = new TSecreCarrier();
        c.setId(id);
        c.setCarrierNo(carrierNo);
        c.setLevelNo(level);
        c.setDelFlag(0);
        carriers.put(id, c);
        return c;
    }

    @Override
    public synchronized TSecreCarrier lockCarrier(Long carrierId) {
        return carriers.get(carrierId);
    }

    @Override
    public synchronized void updateCarrierLevel(Long carrierId, Integer newLevel) {
        TSecreCarrier c = carriers.get(carrierId);
        if (c != null) {
            c.setLevelNo(newLevel);
        }
    }

    @Override
    public synchronized TSecreLevelGateBill lockBill(Long billId) {
        return bills.get(billId);
    }

    @Override
    public synchronized boolean existsOpenBill(Long carrierId) {
        return bills.values().stream().anyMatch(b -> carrierId.equals(b.getCarrierId())
                && (b.getDelFlag() == null || b.getDelFlag() == 0)
                && b.getGateNo() != null && b.getGateNo() < LevelGateRule.GATE_SEALED);
    }

    @Override
    public synchronized void insertBill(TSecreLevelGateBill bill) {
        bills.put(bill.getId(), bill);
    }

    @Override
    public synchronized void updateBill(TSecreLevelGateBill bill) {
        bills.put(bill.getId(), bill);
    }

    @Override
    public synchronized List<TSecreLevelGateBill> listAllBills() {
        return new ArrayList<>(bills.values());
    }

    @Override
    public synchronized int countOpenBills() {
        int n = 0;
        for (TSecreLevelGateBill b : bills.values()) {
            if ((b.getDelFlag() == null || b.getDelFlag() == 0)
                    && b.getGateNo() != null && b.getGateNo() < LevelGateRule.GATE_SEALED) {
                n++;
            }
        }
        return n;
    }

    @Override
    public synchronized void insertSign(TSecreLevelGateSign sign, int alreadyInGate) {
        signs.add(sign);
    }

    @Override
    public synchronized boolean existsSign(Long billId, int gateNo, int roundNo, String signerNo) {
        return signs.stream().anyMatch(s -> billId.equals(s.getBillId()) && s.getGateNo() == gateNo
                && s.getRoundNo() == roundNo && signerNo.equals(s.getSignerNo()));
    }

    @Override
    public synchronized List<TSecreLevelGateSign> listSigns(Long billId) {
        List<TSecreLevelGateSign> list = new ArrayList<>();
        for (TSecreLevelGateSign s : signs) {
            if (billId.equals(s.getBillId())) {
                list.add(s);
            }
        }
        list.sort(Comparator
                .comparingInt(TSecreLevelGateSign::getGateNo)
                .thenComparingInt(TSecreLevelGateSign::getRoundNo)
                .thenComparingInt(TSecreLevelGateSign::getSignSeq)
                .thenComparing(TSecreLevelGateSign::getSignedAt,
                        Comparator.nullsLast(Comparator.naturalOrder())));
        return list;
    }

    @Override
    public synchronized int deleteRoundSigns(Long billId, int gateNo, int roundNo) {
        int before = signs.size();
        signs.removeIf(s -> billId.equals(s.getBillId()) && s.getGateNo() == gateNo
                && s.getRoundNo() == roundNo);
        return before - signs.size();
    }

    @Override
    public synchronized void insertLog(TSecreCarrierLevelLog log) {
        logs.add(log);
    }

    @Override
    public synchronized int countLogs(Long billId) {
        int n = 0;
        for (TSecreCarrierLevelLog l : logs) {
            if (billId.equals(l.getBillId())) {
                n++;
            }
        }
        return n;
    }
}
