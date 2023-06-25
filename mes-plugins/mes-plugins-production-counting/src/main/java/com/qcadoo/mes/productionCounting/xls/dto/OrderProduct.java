package com.qcadoo.mes.productionCounting.xls.dto;

import java.math.BigDecimal;
import java.util.Objects;

public class OrderProduct {

    private String orderNumber;

    private String productType;

    private String productNumber;

    private String productName;

    private BigDecimal plannedQuantity;

    private BigDecimal producedQuantity;

    private BigDecimal deviation;

    private String productUnit;

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public String getProductType() {
        return productType;
    }

    public void setProductType(String productType) {
        this.productType = productType;
    }

    public String getProductNumber() {
        return productNumber;
    }

    public void setProductNumber(String productNumber) {
        this.productNumber = productNumber;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public BigDecimal getPlannedQuantity() {
        return plannedQuantity;
    }

    public void setPlannedQuantity(BigDecimal plannedQuantity) {
        this.plannedQuantity = plannedQuantity;
    }

    public BigDecimal getProducedQuantity() {
        return producedQuantity;
    }

    public void setProducedQuantity(BigDecimal producedQuantity) {
        this.producedQuantity = producedQuantity;
    }

    public BigDecimal getDeviation() {
        return deviation;
    }

    public void setDeviation(BigDecimal deviation) {
        this.deviation = deviation;
    }

    public String getProductUnit() {
        return productUnit;
    }

    public void setProductUnit(String productUnit) {
        this.productUnit = productUnit;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderProduct that = (OrderProduct) o;
        return Objects.equals(orderNumber, that.orderNumber) && Objects.equals(productType, that.productType) && Objects.equals(productNumber, that.productNumber) && Objects.equals(productName, that.productName) && Objects.equals(plannedQuantity, that.plannedQuantity) && Objects.equals(producedQuantity, that.producedQuantity) && Objects.equals(deviation, that.deviation) && Objects.equals(productUnit, that.productUnit);
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderNumber, productType, productNumber, productName, plannedQuantity, producedQuantity, deviation, productUnit);
    }
}
