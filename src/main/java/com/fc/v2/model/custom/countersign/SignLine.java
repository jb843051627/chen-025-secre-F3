package com.fc.v2.model.custom.countersign;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * 闸内一行落字（受理簿一笔的回显）。
 * 屏上两行都摆着：本轮字与上轮字各放各格，voided 只作账内抹、原话照旧留底。
 *
 * @author fuce
 * @date 2026-10-05
 */
public class SignLine implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 第几轮 */
    private Integer roundNo;

    /** 本闸本轮第几笔（同一瞬两笔各按先后，不并成一句） */
    private Integer seqNo;

    /** 落字人 */
    private String signer;

    /** 落字 1可 0不可 */
    private Integer verdict;

    /** 落字那一刻（复核只认这一刻） */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date signTime;

    /** 落字原话 */
    private String remark;

    /** 是否本轮账内抹除（旧轮旧字一个不动，仍摆着） */
    private Integer voided;

    /** 是否本闸当前算数的一笔（旧轮存底但已被新一轮压住的，摆着而不计名数） */
    private Boolean operative;

    public Integer getRoundNo() {
        return roundNo;
    }

    public void setRoundNo(Integer roundNo) {
        this.roundNo = roundNo;
    }

    public Integer getSeqNo() {
        return seqNo;
    }

    public void setSeqNo(Integer seqNo) {
        this.seqNo = seqNo;
    }

    public String getSigner() {
        return signer;
    }

    public void setSigner(String signer) {
        this.signer = signer;
    }

    public Integer getVerdict() {
        return verdict;
    }

    public void setVerdict(Integer verdict) {
        this.verdict = verdict;
    }

    public Date getSignTime() {
        return signTime;
    }

    public void setSignTime(Date signTime) {
        this.signTime = signTime;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Integer getVoided() {
        return voided;
    }

    public void setVoided(Integer voided) {
        this.voided = voided;
    }

    public Boolean getOperative() {
        return operative;
    }

    public void setOperative(Boolean operative) {
        this.operative = operative;
    }
}
