package com.fc.v2.service.levelgate;

import com.fc.v2.model.auto.TSecreCarrier;
import com.fc.v2.model.auto.TSecreCarrierLevelLog;
import com.fc.v2.model.auto.TSecreLevelGateBill;
import com.fc.v2.model.auto.TSecreLevelGateSign;

import java.util.List;

/**
 * 三闸单的账本口。
 *
 * <p>引擎只认这一处给出的回话：段位、人数、序次、时刻全由这里的存底点出来。
 * MyBatis 实现须在单笔事务内先锁册面行、再锁单据行，顺序点笔，不许跨口取数。
 *
 * @author fuce
 * @date 2026-10-05
 */
public interface LevelGateRepository {

    /** 锁册面那一行（起单、齐闸都先锁它）；查无返回 null */
    TSecreCarrier lockCarrier(Long carrierId);

    /** 齐闸同一回计算里改册面密级栏（与叠履历同一事务） */
    void updateCarrierLevel(Long carrierId, Integer newLevel);

    /** 按主键锁单据行；查无返回 null */
    TSecreLevelGateBill lockBill(Long billId);

    /** 这件载体名下有没有还没封住的单（同一回变更只带得出一张）；实现须加锁读 */
    boolean existsOpenBill(Long carrierId);

    /** 落一张新单 */
    void insertBill(TSecreLevelGateBill bill);

    /** 改单（挪截、挂待议、加封、回次） */
    void updateBill(TSecreLevelGateBill bill);

    /** 列出全部未删的单（对账用） */
    List<TSecreLevelGateBill> listAllBills();

    /** 这本账上在办（未封住）单的张数——屏上那个数与逐闸点的同出于这一口 */
    int countOpenBills();

    /**
     * 落一笔字。
     *
     * @param alreadyInGate 本闸本回已有几笔（调用方在单据行锁内点出来的序，从 0 起）
     */
    void insertSign(TSecreLevelGateSign sign, int alreadyInGate);

    /** 同一人在本闸本回落过没有（不许追补第二笔） */
    boolean existsSign(Long billId, int gateNo, int roundNo, String signerNo);

    /** 把这张单这笔流水全部按闸次、回次、序、时刻点出来 */
    List<TSecreLevelGateSign> listSigns(Long billId);

    /** 抹掉这一闸本轮落下的字（压回只收这一段，别闸旧字一个不动） */
    int deleteRoundSigns(Long billId, int gateNo, int roundNo);

    /** 叠一条变更履历 */
    void insertLog(TSecreCarrierLevelLog log);

    /** 点一张单叠过几条履历（齐闸后应恰为一条） */
    int countLogs(Long billId);
}
