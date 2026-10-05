package com.fc.v2.service.levelgate;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fc.v2.mapper.auto.TSecreCarrierLevelLogMapper;
import com.fc.v2.mapper.auto.TSecreCarrierMapper;
import com.fc.v2.mapper.auto.TSecreLevelGateBillMapper;
import com.fc.v2.mapper.auto.TSecreLevelGateSignMapper;
import com.fc.v2.mapper.custom.LevelGateDao;
import com.fc.v2.model.auto.TSecreCarrier;
import com.fc.v2.model.auto.TSecreCarrierLevelLog;
import com.fc.v2.model.auto.TSecreLevelGateBill;
import com.fc.v2.model.auto.TSecreLevelGateSign;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

/**
 * 三闸账本的 MyBatis 实现。段位、名数、序次、时刻只从这里的存底点出来；
 * 控制器与引擎都不另开一口取数。
 *
 * @author fuce
 * @date 2026-10-05
 */
@Repository
public class MybatisLevelGateRepository implements LevelGateRepository {

    @Resource
    private LevelGateDao levelGateDao;
    @Resource
    private TSecreCarrierMapper carrierMapper;
    @Resource
    private TSecreLevelGateBillMapper billMapper;
    @Resource
    private TSecreLevelGateSignMapper signMapper;
    @Resource
    private TSecreCarrierLevelLogMapper logMapper;

    @Override
    public TSecreCarrier lockCarrier(Long carrierId) {
        return levelGateDao.lockCarrier(carrierId);
    }

    @Override
    public void updateCarrierLevel(Long carrierId, Integer newLevel) {
        TSecreCarrier update = new TSecreCarrier();
        update.setId(carrierId);
        update.setLevelNo(newLevel);
        carrierMapper.updateById(update);
    }

    @Override
    public TSecreLevelGateBill lockBill(Long billId) {
        return levelGateDao.lockBill(billId);
    }

    @Override
    public boolean existsOpenBill(Long carrierId) {
        return !levelGateDao.lockOpenBillsByCarrier(carrierId).isEmpty();
    }

    @Override
    public void insertBill(TSecreLevelGateBill bill) {
        billMapper.insert(bill);
    }

    @Override
    public void updateBill(TSecreLevelGateBill bill) {
        billMapper.updateById(bill);
    }

    @Override
    public List<TSecreLevelGateBill> listAllBills() {
        QueryWrapper<TSecreLevelGateBill> qw = new QueryWrapper<>();
        qw.eq("del_flag", 0).orderByAsc("create_time");
        return billMapper.selectList(qw);
    }

    @Override
    public int countOpenBills() {
        return levelGateDao.countOpenBills();
    }

    @Override
    public void insertSign(TSecreLevelGateSign sign, int alreadyInGate) {
        // sign_seq 由调用方在单据行锁内点出；同瞬并发也只排得出一个先后，不并成一句。
        sign.setSignSeq(alreadyInGate);
        signMapper.insert(sign);
    }

    @Override
    public boolean existsSign(Long billId, int gateNo, int roundNo, String signerNo) {
        QueryWrapper<TSecreLevelGateSign> qw = new QueryWrapper<>();
        qw.eq("bill_id", billId).eq("gate_no", gateNo).eq("round_no", roundNo).eq("signer_no", signerNo);
        return signMapper.selectCount(qw) > 0;
    }

    @Override
    public List<TSecreLevelGateSign> listSigns(Long billId) {
        return levelGateDao.listSigns(billId);
    }

    @Override
    public int deleteRoundSigns(Long billId, int gateNo, int roundNo) {
        // 压回往回只收一闸：只抹判不同意这一闸本轮的字，别闸、别回的旧字一个不动。
        QueryWrapper<TSecreLevelGateSign> qw = new QueryWrapper<>();
        qw.eq("bill_id", billId).eq("gate_no", gateNo).eq("round_no", roundNo);
        return signMapper.delete(qw);
    }

    @Override
    public void insertLog(TSecreCarrierLevelLog log) {
        logMapper.insert(log);
    }

    @Override
    public int countLogs(Long billId) {
        QueryWrapper<TSecreCarrierLevelLog> qw = new QueryWrapper<>();
        qw.eq("bill_id", billId);
        return logMapper.selectCount(qw);
    }
}
