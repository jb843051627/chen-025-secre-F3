package com.fc.v2.model.custom.countersign;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 一道闸的四项回话：该几名落字、按什么算放过、当下到了几名、整单停在哪一截。
 * 四项全由系统顺着受理簿点出来；「到了几名」不设格子，屏上只是回显。
 *
 * @author fuce
 * @date 2026-10-05
 */
public class GateState implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 第几闸 0头闸 1次闸 2末闸 */
    private Integer nodeNo;

    /** 闸名 */
    private String nodeName;

    /** 该几名落字 */
    private Integer needCount;

    /** 按什么算放过：ONE_PASS 一名即过 / FIRST_WINS 头名定论两名留底 / BOTH_AGREE 两名同可 */
    private String passRule;

    /** 当下到了几名（本轮有效笔数；只回显，没有格子给人填） */
    private Integer arrivedCount;

    /** 本闸本轮是否已放过 */
    private Boolean passed;

    /** 头名说法（次闸定论取这笔；末闸作同判比对之一） */
    private Integer firstVerdict;

    /** 本闸逐笔回显（本轮与旧轮各放各格，两行都摆着） */
    private List<SignLine> lines = new ArrayList<>();

    public Integer getNodeNo() {
        return nodeNo;
    }

    public void setNodeNo(Integer nodeNo) {
        this.nodeNo = nodeNo;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public Integer getNeedCount() {
        return needCount;
    }

    public void setNeedCount(Integer needCount) {
        this.needCount = needCount;
    }

    public String getPassRule() {
        return passRule;
    }

    public void setPassRule(String passRule) {
        this.passRule = passRule;
    }

    public Integer getArrivedCount() {
        return arrivedCount;
    }

    public void setArrivedCount(Integer arrivedCount) {
        this.arrivedCount = arrivedCount;
    }

    public Boolean getPassed() {
        return passed;
    }

    public void setPassed(Boolean passed) {
        this.passed = passed;
    }

    public Integer getFirstVerdict() {
        return firstVerdict;
    }

    public void setFirstVerdict(Integer firstVerdict) {
        this.firstVerdict = firstVerdict;
    }

    public List<SignLine> getLines() {
        return lines;
    }

    public void setLines(List<SignLine> lines) {
        this.lines = lines;
    }
}
