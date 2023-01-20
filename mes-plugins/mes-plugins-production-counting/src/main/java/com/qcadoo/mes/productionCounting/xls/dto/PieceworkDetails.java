package com.qcadoo.mes.productionCounting.xls.dto;

import java.math.BigDecimal;
import java.util.Objects;

public class PieceworkDetails {

    private String orderNumber;

    private String operationNumber;

    private String worker;

    private BigDecimal producedQuantity;
    private String pieceRate;
    private BigDecimal rate;
    private BigDecimal cost;


    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public String getOperationNumber() {
        return operationNumber;
    }

    public void setOperationNumber(String operationNumber) {
        this.operationNumber = operationNumber;
    }

    public String getWorker() {
        return worker;
    }

    public void setWorker(String worker) {
        this.worker = worker;
    }

    public BigDecimal getProducedQuantity() {
        return producedQuantity;
    }

    public void setProducedQuantity(BigDecimal producedQuantity) {
        this.producedQuantity = producedQuantity;
    }

    public String getPieceRate() {
        return pieceRate;
    }

    public void setPieceRate(String pieceRate) {
        this.pieceRate = pieceRate;
    }

    public BigDecimal getRate() {
        return rate;
    }

    public void setRate(BigDecimal rate) {
        this.rate = rate;
    }

    public BigDecimal getCost() {
        return cost;
    }

    public void setCost(BigDecimal cost) {
        this.cost = cost;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PieceworkDetails that = (PieceworkDetails) o;
        return orderNumber.equals(that.orderNumber) && operationNumber.equals(that.operationNumber) && Objects.equals(worker, that.worker) && producedQuantity.equals(that.producedQuantity) && pieceRate.equals(that.pieceRate) && rate.equals(that.rate) && cost.equals(that.cost);
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderNumber, operationNumber, worker, producedQuantity, pieceRate, rate, cost);
    }
}
