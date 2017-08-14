package com.qcadoo.mes.productionCounting.xls.dto;

import java.math.BigDecimal;
import java.util.Objects;

public class PieceworkDetails {

    private String orderNumber;

    private String operationNumber;

    private BigDecimal totalExecutedOperationCycles;

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        PieceworkDetails that = (PieceworkDetails) o;
        return Objects.equals(orderNumber, that.orderNumber) && Objects.equals(operationNumber, that.operationNumber)
                && Objects.equals(totalExecutedOperationCycles, that.totalExecutedOperationCycles);
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderNumber, operationNumber, totalExecutedOperationCycles);
    }

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

    public BigDecimal getTotalExecutedOperationCycles() {

        return totalExecutedOperationCycles;
    }

    public void setTotalExecutedOperationCycles(BigDecimal totalExecutedOperationCycles) {
        this.totalExecutedOperationCycles = totalExecutedOperationCycles;
    }
}
