package com.qcadoo.mes.productionCounting.xls.dto;

import java.math.BigDecimal;

public class ExternalServiceCost {

    private String orderNumber;

    private String operationNumber;

    private String productNumber;

    private BigDecimal unitCost;

    private BigDecimal quantity;

    private String productUnit;

    private BigDecimal totalCost;

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(final String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public String getOperationNumber() {
        return operationNumber;
    }

    public void setOperationNumber(final String operationNumber) {
        this.operationNumber = operationNumber;
    }

    public String getProductNumber() {
        return productNumber;
    }

    public void setProductNumber(final String productNumber) {
        this.productNumber = productNumber;
    }

    public BigDecimal getUnitCost() {
        return unitCost;
    }

    public void setUnitCost(final BigDecimal unitCost) {
        this.unitCost = unitCost;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(final BigDecimal quantity) {
        this.quantity = quantity;
    }

    public String getProductUnit() {
        return productUnit;
    }

    public void setProductUnit(final String productUnit) {
        this.productUnit = productUnit;
    }

    public BigDecimal getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(final BigDecimal totalCost) {
        this.totalCost = totalCost;
    }

}
