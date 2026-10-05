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
 * 密级变更三道闸会签单对象 t_secre_countersign
 * 新名目 countersign：承接「抬/压/解开」六则会签算法；旧 t_secre_level_bill 形状锁住不改。
 *
 * @author fuce
 * @date 2026-10-05
 */
@TableName("t_secre_countersign")
@ApiModel(value = "TSecreCountersign", description = "密级变更三道闸会签单")
public class TSecreCountersign implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 主键 */
    @TableId(type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    @ApiModelProperty(value = "主键")
    private Long id;

    /** 密级变更会签单号（一载体一行变更只带得出这一张） */
    @TableField("cs_no")
    @ApiModelProperty(value = "密级变更会签单号")
    private String csNo;

    /** 册面载体行（同一件载体、同一回变更只带一张） */
    @TableField("carrier_id")
    @JsonSerialize(using = ToStringSerializer.class)
    @ApiModelProperty(value = "册面载体行")
    private Long carrierId;

    /** 载体件码（冗余，受理簿对件用） */
    @TableField("carrier_no")
    @ApiModelProperty(value = "载体件码")
    private String carrierNo;

    /** 往上抬/往下压/整个解开 1抬 2压 3解密 */
    @TableField("change_kind")
    @ApiModelProperty(value = "变更路数 1抬 2压 3解密")
    private Integer changeKind;

    /** 自哪一级挪来 */
    @TableField("level_from")
    @ApiModelProperty(value = "自哪一级挪来")
    private Integer levelFrom;

    /** 挪到哪一级（解开记 0） */
    @TableField("level_to")
    @ApiModelProperty(value = "挪到哪一级（解开记0）")
    private Integer levelTo;

    /** 当前停在哪一闸 0头闸 1次闸 2末闸（系统点，不留手填格） */
    @TableField("node_no")
    @ApiModelProperty(value = "当前停在哪一闸（系统点，不留手填格）")
    private Integer nodeNo;

    /** 走到第几轮（每压回一次加一，本轮与上轮各放各格） */
    @TableField("round_no")
    @ApiModelProperty(value = "走到第几轮")
    private Integer roundNo;

    /** 情形 0在签 1待议(末闸一可一不可挂住) 2齐闸封住 3已压回重走 */
    @TableField("status")
    @ApiModelProperty(value = "情形 0在签 1待议 2齐闸封住 3已压回重走")
    private Integer status;

    /** 未齐闸挂账记1，齐闸置NULL（唯一索引挡一件载体两张在办单） */
    @TableField("open_flag")
    @ApiModelProperty(value = "未齐闸挂账标记")
    private Integer openFlag;

    /** 齐闸封单那一刻 */
    @TableField("seal_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = "齐闸封单那一刻")
    private Date sealTime;

    /** 删除标记 0正常 1删除（会签单不许删，仅留列对齐基线） */
    @TableField("del_flag")
    @ApiModelProperty(value = "删除标记 0正常 1删除")
    private Integer delFlag;

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

    /** 备注 */
    @TableField("remark")
    @ApiModelProperty(value = "备注")
    private String remark;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCsNo() {
        return csNo;
    }

    public void setCsNo(String csNo) {
        this.csNo = csNo;
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

    public Integer getLevelFrom() {
        return levelFrom;
    }

    public void setLevelFrom(Integer levelFrom) {
        this.levelFrom = levelFrom;
    }

    public Integer getLevelTo() {
        return levelTo;
    }

    public void setLevelTo(Integer levelTo) {
        this.levelTo = levelTo;
    }

    public Integer getNodeNo() {
        return nodeNo;
    }

    public void setNodeNo(Integer nodeNo) {
        this.nodeNo = nodeNo;
    }

    public Integer getRoundNo() {
        return roundNo;
    }

    public void setRoundNo(Integer roundNo) {
        this.roundNo = roundNo;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getOpenFlag() {
        return openFlag;
    }

    public void setOpenFlag(Integer openFlag) {
        this.openFlag = openFlag;
    }

    public Date getSealTime() {
        return sealTime;
    }

    public void setSealTime(Date sealTime) {
        this.sealTime = sealTime;
    }

    public Integer getDelFlag() {
        return delFlag;
    }

    public void setDelFlag(Integer delFlag) {
        this.delFlag = delFlag;
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
