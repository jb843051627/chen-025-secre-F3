package com.fc.v2.model.custom.countersign;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 回数核对回话：事后从末闸回数回头闸，与当日受理簿一笔一笔对得齐。
 * 在办张数也是同一回算的账：单上记的与逐闸点出来的对不上，就点名报出。
 *
 * @author fuce
 * @date 2026-10-05
 */
public class ReconReport implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 对得齐否 */
    private Boolean ok;

    /** 点到的张数（受理簿逐笔 + 册面逐张） */
    private Integer counted;

    /** 账上挂的张数（未齐闸册面） */
    private Integer booked;

    /** 对不齐时逐张点名 */
    private List<Item> items = new ArrayList<>();

    /** 一句句原话报出 */
    private List<String> problems = new ArrayList<>();

    /** 单张核对项 */
    public static class Item implements Serializable {
        private static final long serialVersionUID = 1L;

        /** 会签单 */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long csId;

        /** 会签单号 */
        private String csNo;

        /** 载体件码 */
        private String carrierNo;

        /** 册面停留闸 */
        private Integer bookedNode;

        /** 受理簿点出的停留闸 */
        private Integer countedNode;

        /** 册面情形 */
        private Integer bookedStatus;

        /** 受理簿点出的情形 */
        private Integer countedStatus;

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

        public String getCarrierNo() {
            return carrierNo;
        }

        public void setCarrierNo(String carrierNo) {
            this.carrierNo = carrierNo;
        }

        public Integer getBookedNode() {
            return bookedNode;
        }

        public void setBookedNode(Integer bookedNode) {
            this.bookedNode = bookedNode;
        }

        public Integer getCountedNode() {
            return countedNode;
        }

        public void setCountedNode(Integer countedNode) {
            this.countedNode = countedNode;
        }

        public Integer getBookedStatus() {
            return bookedStatus;
        }

        public void setBookedStatus(Integer bookedStatus) {
            this.bookedStatus = bookedStatus;
        }

        public Integer getCountedStatus() {
            return countedStatus;
        }

        public void setCountedStatus(Integer countedStatus) {
            this.countedStatus = countedStatus;
        }
    }

    public Boolean getOk() {
        return ok;
    }

    public void setOk(Boolean ok) {
        this.ok = ok;
    }

    public Integer getCounted() {
        return counted;
    }

    public void setCounted(Integer counted) {
        this.counted = counted;
    }

    public Integer getBooked() {
        return booked;
    }

    public void setBooked(Integer booked) {
        this.booked = booked;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    public List<String> getProblems() {
        return problems;
    }

    public void setProblems(List<String> problems) {
        this.problems = problems;
    }
}
