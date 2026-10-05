package com.fc.v2.model.auto;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.util.Date;

/**
 * 三闸单逐笔落字流水对象 t_secre_level_gate_sign
 *
 * <p>已签数的唯一出处：顺着这张单上谁先谁后一笔一笔点出来。序与时刻以落库为准，
 * 报文里写了第几段、第几人，进到库里不算数。
 *
 * @author fuce
 * @date 2026-10-05
 */
@TableName("t_secre_level_gate_sign")
@ApiModel(value = "TSecreLevelGateSign", description = "三闸单逐笔落字流水")
public class TSecreLevelGateSign implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 主键 */
    @TableId(type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    @ApiModelProperty(value = "主键")
    private Long id;

    /** 对哪张三闸单 */
    @TableField("bill_id")
    @JsonSerialize(using = ToStringSerializer.class)
    @ApiModelProperty(value = "对哪张三闸单")
    private Long billId;

    /** 落在哪一闸 0/1/2 */
    @TableField("gate_no")
    @ApiModelProperty(value = "落在哪一闸")
    private Integer gateNo;

    /** 落在哪一回（本轮与上轮各放各格） */
    @TableField("round_no")
    @ApiModelProperty(value = "回次")
    private Integer roundNo;

    /** 本闸本回里第几笔（点出来的序） */
    @TableField("sign_seq")
    @ApiModelProperty(value = "本闸本回里第几笔")
    private Integer signSeq;

    /** 落字岗位/人员代号 */
    @TableField("signer_no")
    @ApiModelProperty(value = "落字岗位/人员代号")
    private String signerNo;

    /** 写下的字 1可 0不可 */
    @TableField("verdict")
    @ApiModelProperty(value = "写下的字 1可 0不可")
    private Integer verdict;

    /** 写下的原话（两方的字都留底） */
    @TableField("word")
    @ApiModelProperty(value = "写下的原话")
    private String word;

    /** 各自落字那一刻（日后复核只认这一刻；同瞬两名各记各的，不并成一句） */
    @TableField("signed_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS", timezone = "GMT+8")
    @ApiModelProperty(value = "落字那一刻")
    private Date signedAt;

    /** 创建者 */
    @TableField(value = "create_by", fill = FieldFill.INSERT)
    @ApiModelProperty(value = "创建者")
    private String createBy;

    /** 创建时间 */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getBillId() {
        return billId;
    }

    public void setBillId(Long billId) {
        this.billId = billId;
    }

    public Integer getGateNo() {
        return gateNo;
    }

    public void setGateNo(Integer gateNo) {
        this.gateNo = gateNo;
    }

    public Integer getRoundNo() {
        return roundNo;
    }

    public void setRoundNo(Integer roundNo) {
        this.roundNo = roundNo;
    }

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

    public String getCreateBy() {
        return createBy;
    }

    public void setCreateBy(String createBy) {
        this.createBy = createBy;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}
