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
 * 会签逐闸受理簿对象 t_secre_countersign_sign
 * 一笔一行：同一瞬两笔也各按先后记，不并成一句；压回只把本轮本闸置 voided，旧轮旧字一个不动。
 * 段位、已签数、回数核对、在办张数，全从这一本逐笔点出，没有第二处口径。
 *
 * @author fuce
 * @date 2026-10-05
 */
@TableName("t_secre_countersign_sign")
@ApiModel(value = "TSecreCountersignSign", description = "会签逐闸受理簿")
public class TSecreCountersignSign implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 主键 */
    @TableId(type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    @ApiModelProperty(value = "主键")
    private Long id;

    /** 所属会签单 */
    @TableField("cs_id")
    @JsonSerialize(using = ToStringSerializer.class)
    @ApiModelProperty(value = "所属会签单")
    private Long csId;

    /** 第几轮落下（本轮与上轮各放各格，哪天补的各有凭据） */
    @TableField("round_no")
    @ApiModelProperty(value = "第几轮落下")
    private Integer roundNo;

    /** 落在哪一闸 0头闸 1次闸 2末闸 */
    @TableField("node_no")
    @ApiModelProperty(value = "落在哪一闸 0头闸 1次闸 2末闸")
    private Integer nodeNo;

    /** 本闸本轮第几笔（同一瞬两笔也各按先后记，不并成一句） */
    @TableField("seq_no")
    @ApiModelProperty(value = "本闸本轮第几笔")
    private Integer seqNo;

    /** 落字人（一支笔只认本人那一刻） */
    @TableField("signer")
    @ApiModelProperty(value = "落字人")
    private String signer;

    /** 落字 1可 0不可 */
    @TableField("verdict")
    @ApiModelProperty(value = "落字 1可 0不可")
    private Integer verdict;

    /** 落字那一刻（日后复核只认这一刻） */
    @TableField("sign_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = "落字那一刻")
    private Date signTime;

    /** 本笔是否随本闸压回作账内抹除 0留底 1本轮抹（旧轮旧字一个不动） */
    @TableField("voided")
    @ApiModelProperty(value = "是否本轮账内抹除 0留底 1本轮抹")
    private Integer voided;

    /** 创建者 */
    @TableField(value = "create_by", fill = FieldFill.INSERT)
    @ApiModelProperty(value = "创建者")
    private String createBy;

    /** 创建时间 */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    /** 更新者 */
    @TableField(value = "update_by", fill = FieldFill.UPDATE)
    @ApiModelProperty(value = "更新者")
    private String updateBy;

    /** 更新时间 */
    @TableField(value = "update_time", fill = FieldFill.UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = "更新时间")
    private Date updateTime;

    /** 备注（落字原话留底，两行都摆着） */
    @TableField("remark")
    @ApiModelProperty(value = "备注（落字原话留底）")
    private String remark;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCsId() {
        return csId;
    }

    public void setCsId(Long csId) {
        this.csId = csId;
    }

    public Integer getRoundNo() {
        return roundNo;
    }

    public void setRoundNo(Integer roundNo) {
        this.roundNo = roundNo;
    }

    public Integer getNodeNo() {
        return nodeNo;
    }

    public void setNodeNo(Integer nodeNo) {
        this.nodeNo = nodeNo;
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

    public Integer getVoided() {
        return voided;
    }

    public void setVoided(Integer voided) {
        this.voided = voided;
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

    public String getUpdateBy() {
        return updateBy;
    }

    public void setUpdateBy(String updateBy) {
        this.updateBy = updateBy;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
