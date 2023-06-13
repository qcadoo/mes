package com.qcadoo.mes.productionCounting.xls.dto;

import java.math.BigDecimal;

public class ProducedQuantity {

    private String orderNumber;

    private String additionalFinalProducts;

    private String productNumber;

    private String productName;

    private BigDecimal plannedQuantity;

    private BigDecimal producedQuantity;

    private BigDecimal wastesQuantity;
    private BigDecimal additionalFinalProductsQuantity;

    private BigDecimal producedWastes;

    private BigDecimal deviation;

    private String productUnit;

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
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

    public BigDecimal getWastesQuantity() {
        return wastesQuantity;
    }

    public void setWastesQuantity(BigDecimal wastesQuantity) {
        this.wastesQuantity = wastesQuantity;
    }

    public BigDecimal getProducedWastes() {
        return producedWastes;
    }

    public void setProducedWastes(BigDecimal producedWastes) {
        this.producedWastes = producedWastes;
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

    public BigDecimal getAdditionalFinalProductsQuantity() {
        return additionalFinalProductsQuantity;
    }

    public void setAdditionalFinalProductsQuantity(BigDecimal additionalFinalProductsQuantity) {
        this.additionalFinalProductsQuantity = additionalFinalProductsQuantity;
    }

    public String getAdditionalFinalProducts() {
        return additionalFinalProducts;
    }

    public void setAdditionalFinalProducts(String additionalFinalProducts) {
        this.additionalFinalProducts = additionalFinalProducts;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        ProducedQuantity that = (ProducedQuantity) o;

        if (!orderNumber.equals(that.orderNumber))
            return false;
        if (!productNumber.equals(that.productNumber))
            return false;
        if (!productName.equals(that.productName))
            return false;
        if (plannedQuantity != null ? !plannedQuantity.equals(that.plannedQuantity) : that.plannedQuantity != null)
            return false;
        if (producedQuantity != null ? !producedQuantity.equals(that.producedQuantity) : that.producedQuantity != null)
            return false;
        if (wastesQuantity != null ? !wastesQuantity.equals(that.wastesQuantity) : that.wastesQuantity != null)
            return false;
        if (producedWastes != null ? !producedWastes.equals(that.producedWastes) : that.producedWastes != null)
            return false;
        if (deviation != null ? !deviation.equals(that.deviation) : that.deviation != null)
            return false;
        return productUnit.equals(that.productUnit);
    }

    @Override
    public int hashCode() {
        int result = orderNumber.hashCode();
        result = 31 * result + productNumber.hashCode();
        result = 31 * result + productName.hashCode();
        result = 31 * result + (plannedQuantity != null ? plannedQuantity.hashCode() : 0);
        result = 31 * result + (producedQuantity != null ? producedQuantity.hashCode() : 0);
        result = 31 * result + (wastesQuantity != null ? wastesQuantity.hashCode() : 0);
        result = 31 * result + (producedWastes != null ? producedWastes.hashCode() : 0);
        result = 31 * result + (deviation != null ? deviation.hashCode() : 0);
        result = 31 * result + productUnit.hashCode();
        return result;
    }
}
