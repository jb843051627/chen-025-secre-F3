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
 * 载体密级变更履历对象 t_secre_carrier_level_log
 *
 * <p>密级栏的改动与履历一条，出自同一回计算、同一事务：两处口径一致，
 * 不许一处从计算里取、一处由人手上填。
 *
 * @author fuce
 * @date 2026-10-05
 */
@TableName("t_secre_carrier_level_log")
@ApiModel(value = "TSecreCarrierLevelLog", description = "载体密级变更履历")
public class TSecreCarrierLevelLog implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 主键 */
    @TableId(type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    @ApiModelProperty(value = "主键")
    private Long id;

    /** 叠在哪一行 */
    @TableField("carrier_id")
    @JsonSerialize(using = ToStringSerializer.class)
    @ApiModelProperty(value = "叠在哪一行")
    private Long carrierId;

    /** 凭的是哪一张单 */
    @TableField("bill_id")
    @JsonSerialize(using = ToStringSerializer.class)
    @ApiModelProperty(value = "凭的是哪一张单")
    private Long billId;

    /** 单号留底 */
    @TableField("bill_no")
    @ApiModelProperty(value = "单号留底")
    private String billNo;

    /** 走法 0往上抬 1往下压 2整个解开 */
    @TableField("change_kind")
    @ApiModelProperty(value = "走法 0往上抬 1往下压 2整个解开")
    private Integer changeKind;

    /** 从哪一级 */
    @TableField("from_level")
    @ApiModelProperty(value = "从哪一级")
    private Integer fromLevel;

    /** 挪到哪一级 */
    @TableField("to_level")
    @ApiModelProperty(value = "挪到哪一级")
    private Integer toLevel;

    /** 各闸各笔的字：一条收口的账（闸次、回次、序、谁、可不可、原话、时刻） */
    @TableField("write_ups")
    @ApiModelProperty(value = "各闸各笔的字")
    private String writeUps;

    /** 齐闸那一刻 */
    @TableField("sealed_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS", timezone = "GMT+8")
    @ApiModelProperty(value = "齐闸那一刻")
    private Date sealedAt;

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

    public Long getCarrierId() {
        return carrierId;
    }

    public void setCarrierId(Long carrierId) {
        this.carrierId = carrierId;
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

    public String getWriteUps() {
        return writeUps;
    }

    public void setWriteUps(String writeUps) {
        this.writeUps = writeUps;
    }

    public Date getSealedAt() {
        return sealedAt;
    }

    public void setSealedAt(Date sealedAt) {
        this.sealedAt = sealedAt;
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
