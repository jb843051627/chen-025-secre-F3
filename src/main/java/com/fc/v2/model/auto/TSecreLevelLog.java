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
 * 载体密级变更履历对象 t_secre_level_log
 * 齐闸后叠在载体那一行上：从哪级到哪级、凭哪张单、谁落的字、哪一刻，一段一段倒着捋。
 * 与册面密级栏改动出自同一回计算（同一事务同一引擎），不许一处取计算一处人手填。
 *
 * @author fuce
 * @date 2026-10-05
 */
@TableName("t_secre_level_log")
@ApiModel(value = "TSecreLevelLog", description = "载体密级变更履历")
public class TSecreLevelLog implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 主键 */
    @TableId(type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    @ApiModelProperty(value = "主键")
    private Long id;

    /** 载体行（与册面那一行改动出自同一回计算） */
    @TableField("carrier_id")
    @JsonSerialize(using = ToStringSerializer.class)
    @ApiModelProperty(value = "载体行")
    private Long carrierId;

    /** 载体件码 */
    @TableField("carrier_no")
    @ApiModelProperty(value = "载体件码")
    private String carrierNo;

    /** 凭的是哪一张会签单 */
    @TableField("cs_id")
    @JsonSerialize(using = ToStringSerializer.class)
    @ApiModelProperty(value = "凭的是哪一张会签单")
    private Long csId;

    /** 会签单号 */
    @TableField("cs_no")
    @ApiModelProperty(value = "会签单号")
    private String csNo;

    /** 从哪一级 */
    @TableField("level_from")
    @ApiModelProperty(value = "从哪一级")
    private Integer levelFrom;

    /** 到哪一级（解开记 0） */
    @TableField("level_to")
    @ApiModelProperty(value = "到哪一级（解开记0）")
    private Integer levelTo;

    /** 三闸都有谁落的字（逐闸点出来串成） */
    @TableField("signers")
    @ApiModelProperty(value = "三闸都有谁落的字")
    private String signers;

    /** 齐闸落定那一刻 */
    @TableField("seal_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = "齐闸落定那一刻")
    private Date sealTime;

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

    public Long getCsId() {
        return csId;
    }

    public void setCsId(Long csId) {
        this.csId = csId;
    }

    public String getCsNo() {
        return csNo;
    }

    public void setCsNo(String csNo) {
        this.csNo = csNo;
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

    public String getSigners() {
        return signers;
    }

    public void setSigners(String signers) {
        this.signers = signers;
    }

    public Date getSealTime() {
        return sealTime;
    }

    public void setSealTime(Date sealTime) {
        this.sealTime = sealTime;
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
