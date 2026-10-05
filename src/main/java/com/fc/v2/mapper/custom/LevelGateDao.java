package com.fc.v2.mapper.custom;

import com.fc.v2.model.auto.TSecreCarrier;
import com.fc.v2.model.auto.TSecreLevelGateBill;
import com.fc.v2.model.auto.TSecreLevelGateSign;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 三闸单的行锁与对账查询。
 *
 * <p>锁只在引擎那一笔事务里持得住：先锁册面行、再锁单据行，
 * 同一张单上并发递进来的两笔排队，序次由锁内先后点出。
 *
 * @author fuce
 * @date 2026-10-05
 */
public interface LevelGateDao {

    /** 锁册面那一行 */
    @Select("SELECT * FROM t_secre_carrier WHERE id = #{id} FOR UPDATE")
    TSecreCarrier lockCarrier(@Param("id") Long id);

    /** 锁单据行 */
    @Select("SELECT * FROM t_secre_level_gate_bill WHERE id = #{id} FOR UPDATE")
    TSecreLevelGateBill lockBill(@Param("id") Long id);

    /**
     * 这件载体名下有没有未封住的单（加锁读）。
     *
     * 起单事务已先锁册面行；这里再对在办单的索引段加锁：
     * 并发的第二张单会在这一句上等到头一张落库，随后点得到它而被挡回，
     * 不靠可重复读的快照说话。
     */
    @Select("SELECT * FROM t_secre_level_gate_bill "
            + "WHERE carrier_id = #{carrierId} AND del_flag = 0 AND gate_no < 3 FOR UPDATE")
    List<TSecreLevelGateBill> lockOpenBillsByCarrier(@Param("carrierId") Long carrierId);

    /** 账上在办张数（屏上那个数就取这一口） */
    @Select("SELECT COUNT(1) FROM t_secre_level_gate_bill WHERE del_flag = 0 AND gate_no < 3")
    int countOpenBills();

    /** 把一张单的流水按闸、回、序、时刻点出来（同瞬两名不并成一句） */
    @Select("SELECT * FROM t_secre_level_gate_sign WHERE bill_id = #{billId} "
            + "ORDER BY gate_no ASC, round_no ASC, sign_seq ASC, signed_at ASC")
    List<TSecreLevelGateSign> listSigns(@Param("billId") Long billId);
}
