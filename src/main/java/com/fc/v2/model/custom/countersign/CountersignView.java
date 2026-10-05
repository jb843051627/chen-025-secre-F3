package com.fc.v2.model.custom.countersign;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 一张会签单的按闸分截回话。
 * 停在哪一截由系统点出；各闸四项（该几名／怎么算过／到了几名／停在哪）都在这一处回话里，
 * 报文写了第几段、几人已签，进到库里一概不算数。
 *
 * @author fuce
 * @date 2026-10-05
 */
public class CountersignView implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 会签单主键 */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /** 会签单号 */
    private String csNo;

    /** 载体行 */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long carrierId;

    /** 载体件码 */
    private String carrierNo;

    /** 变更路数 1抬 2压 3解开 */
    private Integer changeKind;

    /** 从哪一级 */
    private Integer levelFrom;

    /** 到哪一级 */
    private Integer levelTo;

    /** 走到第几轮 */
    private Integer roundNo;

    /** 情形 0在签 1待议 2齐闸封住 3已压回重走 */
    private Integer status;

    /** 情形原话 */
    private String statusText;

    /** 整单停在哪一闸（系统点） */
    private Integer currentNode;

    /** 是否已封住（封住后一笔进不来、一字改不了、整张抽不走） */
    private Boolean sealed;

    /** 齐闸那一刻 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date sealTime;

    /** 三道闸，各数各的 */
    private List<GateState> gates = new ArrayList<>();

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

    public String getStatusText() {
        return statusText;
    }

    public void setStatusText(String statusText) {
        this.statusText = statusText;
    }

    public Integer getCurrentNode() {
        return currentNode;
    }

    public void setCurrentNode(Integer currentNode) {
        this.currentNode = currentNode;
    }

    public Boolean getSealed() {
        return sealed;
    }

    public void setSealed(Boolean sealed) {
        this.sealed = sealed;
    }

    public Date getSealTime() {
        return sealTime;
    }

    public void setSealTime(Date sealTime) {
        this.sealTime = sealTime;
    }

    public List<GateState> getGates() {
        return gates;
    }

    public void setGates(List<GateState> gates) {
        this.gates = gates;
    }
}
