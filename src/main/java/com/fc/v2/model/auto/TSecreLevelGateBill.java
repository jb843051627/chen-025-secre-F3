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
 * 三闸密级变更单对象 t_secre_level_gate_bill
 *
 * <p>新名目（level_gate），与旧 t_secre_level_bill 的 approval-chain 形状并存、不相涉：
 * 旧那一套的形状锁住，要添会签算法另起名目承接，不回头改它的样子。
 *
 * <p>本单不设「已签人数」列：到了几名只由流水（t_secre_level_gate_sign）逐笔点出来，
 * 屏上所见只是回显，没有一栏留给人手填。
 *
 * @author fuce
 * @date 2026-10-05
 */
@TableName("t_secre_level_gate_bill")
@ApiModel(value = "TSecreLevelGateBill", description = "三闸密级变更单")
public class TSecreLevelGateBill implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 主键 */
    @TableId(type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    @ApiModelProperty(value = "主键")
    private Long id;

    /** 三闸密级变更单号（一支笔只此一处入口） */
    @TableField("bill_no")
    @ApiModelProperty(value = "三闸密级变更单号")
    private String billNo;

    /** 带出这张单的册面行（t_secre_carrier.id） */
    @TableField("carrier_id")
    @JsonSerialize(using = ToStringSerializer.class)
    @ApiModelProperty(value = "带出这张单的册面行")
    private Long carrierId;

    /** 涉密载体件码（冗余，对册面） */
    @TableField("carrier_no")
    @ApiModelProperty(value = "涉密载体件码")
    private String carrierNo;

    /** 这一回的走法 0往上抬 1往下压 2整个解开 */
    @TableField("change_kind")
    @ApiModelProperty(value = "走法 0往上抬 1往下压 2整个解开")
    private Integer changeKind;

    /** 从哪一级（起单时册面原值快照） */
    @TableField("from_level")
    @ApiModelProperty(value = "从哪一级")
    private Integer fromLevel;

    /** 挪到哪一级（齐闸后才写得进册面） */
    @TableField("to_level")
    @ApiModelProperty(value = "挪到哪一级")
    private Integer toLevel;

    /** 整张单停在哪一截 0承办部门岗 1本机关保密办 2上级主管部门 3齐闸 */
    @TableField("gate_no")
    @ApiModelProperty(value = "停在哪一截 0承办 1保密办 2上级 3齐闸")
    private Integer gateNo;

    /** 本截情形 0候签 2挂待议 3已封住（1为本截已满的瞬态，不落库） */
    @TableField("gate_status")
    @ApiModelProperty(value = "本截情形 0候签 2挂待议 3已封住")
    private Integer gateStatus;

    /** 重头往上走的回数（压回几回就加几） */
    @TableField("round_no")
    @ApiModelProperty(value = "回次")
    private Integer roundNo;

    /** 齐闸封住那一刻 */
    @TableField("seal_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS", timezone = "GMT+8")
    @ApiModelProperty(value = "齐闸封住那一刻")
    private Date sealTime;

    /** 删除标记 0正常 1删除（齐闸后整张也抽不走） */
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

    public Integer getGateNo() {
        return gateNo;
    }

    public void setGateNo(Integer gateNo) {
        this.gateNo = gateNo;
    }

    public Integer getGateStatus() {
        return gateStatus;
    }

    public void setGateStatus(Integer gateStatus) {
        this.gateStatus = gateStatus;
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
