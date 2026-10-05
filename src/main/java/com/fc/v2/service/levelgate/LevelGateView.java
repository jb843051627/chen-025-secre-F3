package com.fc.v2.service.levelgate;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 一张三闸单点出来的回话（屏上所摆全是回显，没有一栏是人手填得进去的）。
 *
 * @author fuce
 * @date 2026-10-05
 */
public class LevelGateView implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 单主键 */
    private Long billId;
    /** 单号 */
    private String billNo;
    /** 册面行 */
    private Long carrierId;
    /** 载体件码 */
    private String carrierNo;
    /** 走法 0抬 1压 2解开 */
    private Integer changeKind;
    /** 从哪一级 */
    private Integer fromLevel;
    /** 挪到哪一级（齐闸前为空） */
    private Integer toLevel;
    /** 整张单停在哪一截 0/1/2，3=齐闸 */
    private Integer stopGateNo;
    /** 停截名目 */
    private String stopGateName;
    /** 本截情形 0候签 2挂待议 3已封住 4已注销 */
    private Integer gateStatus;
    /** 本截情形名目 */
    private String gateStatusName;
    /** 回次 */
    private Integer roundNo;
    /** 齐闸时刻 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS", timezone = "GMT+8")
    private Date sealTime;
    /** 三道闸逐项回话 */
    private List<GateView> gates = new ArrayList<>();

    public static class GateView implements Serializable {
        private static final long serialVersionUID = 1L;
        /** 第几闸 */
        private Integer gateNo;
        /** 闸名目 */
        private String gateName;
        /** 该几名落字 */
        private Integer needCount;
        /** 按什么算放过（名目） */
        private String modeName;
        /** 当下到了几名（顺着流水点出来的，回显） */
        private Integer arrivedCount;
        /** 本闸满没满（点到名数与应签名数一般多方算满） */
        private boolean full;
        /** 本闸定论 1可 0不可 null未定（头名定论闸看头一笔；同可闸看两句） */
        private Integer firstVerdict;
        /** 各回各笔的字（本轮与上轮各放各格，两行都摆着） */
        private List<RoundView> rounds = new ArrayList<>();

        public Integer getGateNo() {
            return gateNo;
        }

        public void setGateNo(Integer gateNo) {
            this.gateNo = gateNo;
        }

        public String getGateName() {
            return gateName;
        }

        public void setGateName(String gateName) {
            this.gateName = gateName;
        }

        public Integer getNeedCount() {
            return needCount;
        }

        public void setNeedCount(Integer needCount) {
            this.needCount = needCount;
        }

        public String getModeName() {
            return modeName;
        }

        public void setModeName(String modeName) {
            this.modeName = modeName;
        }

        public Integer getArrivedCount() {
            return arrivedCount;
        }

        public void setArrivedCount(Integer arrivedCount) {
            this.arrivedCount = arrivedCount;
        }

        public boolean isFull() {
            return full;
        }

        public void setFull(boolean full) {
            this.full = full;
        }

        public Integer getFirstVerdict() {
            return firstVerdict;
        }

        public void setFirstVerdict(Integer firstVerdict) {
            this.firstVerdict = firstVerdict;
        }

        public List<RoundView> getRounds() {
            return rounds;
        }

        public void setRounds(List<RoundView> rounds) {
            this.rounds = rounds;
        }
    }

    public static class RoundView implements Serializable {
        private static final long serialVersionUID = 1L;
        /** 第几回 */
        private Integer roundNo;
        /** 本回到了几名 */
        private Integer arrivedCount;
        /** 一笔一笔点出来的字 */
        private List<SignView> signs = new ArrayList<>();

        public Integer getRoundNo() {
            return roundNo;
        }

        public void setRoundNo(Integer roundNo) {
            this.roundNo = roundNo;
        }

        public Integer getArrivedCount() {
            return arrivedCount;
        }

        public void setArrivedCount(Integer arrivedCount) {
            this.arrivedCount = arrivedCount;
        }

        public List<SignView> getSigns() {
            return signs;
        }

        public void setSigns(List<SignView> signs) {
            this.signs = signs;
        }
    }

    public static class SignView implements Serializable {
        private static final long serialVersionUID = 1L;
        /** 本闸本回第几笔 */
        private Integer signSeq;
        /** 谁落的字 */
        private String signerNo;
        /** 可不可 */
        private Integer verdict;
        /** 原话 */
        private String word;
        /** 落字那一刻 */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS", timezone = "GMT+8")
        private Date signedAt;

        public Integer getSignSeq() {
            return signSeq;
        }

        public void setSignSeq(Integer signSeq) {
            this.signSeq = signSeq;
        }

        public String getSignerNo() {
            return signerNo;
        }

        public void setSignerNo(String signerNo) {
            this.signerNo = signerNo;
        }

        public Integer getVerdict() {
            return verdict;
        }

        public void setVerdict(Integer verdict) {
            this.verdict = verdict;
        }

        public String getWord() {
            return word;
        }

        public void setWord(String word) {
            this.word = word;
        }

        public Date getSignedAt() {
            return signedAt;
        }

        public void setSignedAt(Date signedAt) {
            this.signedAt = signedAt;
        }
    }

    public Long getBillId() {
        return billId;
    }

    public void setBillId(Long billId) {
        this.billId = billId;
    }

    public String getBillNo() {
        return billNo;
    }

    public void setBillNo(String billNo) {
        this.billNo = billNo;
    }

    public Long getCarrierId() {
        return carrierId;
    }

    public void setCarrierId(Long carrierId) {
        this.carrierId = carrierId;
    }

    public String getCarrierNo() {
        return carrierNo;
    }

    public void setCarrierNo(String carrierNo) {
        this.carrierNo = carrierNo;
    }

    public Integer getChangeKind() {
        return changeKind;
    }

    public void setChangeKind(Integer changeKind) {
        this.changeKind = changeKind;
    }

    public Integer getFromLevel() {
        return fromLevel;
    }

    public void setFromLevel(Integer fromLevel) {
        this.fromLevel = fromLevel;
    }

    public Integer getToLevel() {
        return toLevel;
    }

    public void setToLevel(Integer toLevel) {
        this.toLevel = toLevel;
    }

    public Integer getStopGateNo() {
        return stopGateNo;
    }

    public void setStopGateNo(Integer stopGateNo) {
        this.stopGateNo = stopGateNo;
    }

    public String getStopGateName() {
        return stopGateName;
    }

    public void setStopGateName(String stopGateName) {
        this.stopGateName = stopGateName;
    }

    public Integer getGateStatus() {
        return gateStatus;
    }

    public void setGateStatus(Integer gateStatus) {
        this.gateStatus = gateStatus;
    }

    public String getGateStatusName() {
        return gateStatusName;
    }

    public void setGateStatusName(String gateStatusName) {
        this.gateStatusName = gateStatusName;
    }

    public Integer getRoundNo() {
        return roundNo;
    }

    public void setRoundNo(Integer roundNo) {
        this.roundNo = roundNo;
    }

    public Date getSealTime() {
        return sealTime;
    }

    public void setSealTime(Date sealTime) {
        this.sealTime = sealTime;
    }

    public List<GateView> getGates() {
        return gates;
    }

    public void setGates(List<GateView> gates) {
        this.gates = gates;
    }
}
