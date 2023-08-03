package com.qcadoo.mes.productionCounting.xls.dto;

import java.math.BigDecimal;

public class MaterialCost {

    private Long orderId;

    private String orderNumber;

    private String operationNumber;

    private String productNumber;

    private String productName;

    private String replacementTo;

    private BigDecimal plannedQuantity;

    private BigDecimal usedQuantity;

    private BigDecimal quantitativeDeviation;

    private String productUnit;

    private BigDecimal plannedCost;

    private BigDecimal realCost;

    private BigDecimal averagePriceSubcontractor;

    private BigDecimal valueDeviation;

    private BigDecimal usedWasteQuantity;

    private String usedWasteUnit;

    private Long costCurrencyId;

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

    public BigDecimal getUsedQuantity() {
        return usedQuantity;
    }

    public void setUsedQuantity(BigDecimal usedQuantity) {
        this.usedQuantity = usedQuantity;
    }

    public BigDecimal getQuantitativeDeviation() {
        return quantitativeDeviation;
    }

    public void setQuantitativeDeviation(BigDecimal quantitativeDeviation) {
        this.quantitativeDeviation = quantitativeDeviation;
    }

    public String getProductUnit() {
        return productUnit;
    }

    public void setProductUnit(String productUnit) {
        this.productUnit = productUnit;
    }

    public BigDecimal getPlannedCost() {
        return plannedCost;
    }

    public void setPlannedCost(BigDecimal plannedCost) {
        this.plannedCost = plannedCost;
    }

    public BigDecimal getRealCost() {
        return realCost;
    }

    public void setRealCost(BigDecimal realCost) {
        this.realCost = realCost;
    }

    public BigDecimal getValueDeviation() {
        return valueDeviation;
    }

    public void setValueDeviation(BigDecimal valueDeviation) {
        this.valueDeviation = valueDeviation;
    }

    public BigDecimal getUsedWasteQuantity() {
        return usedWasteQuantity;
    }

    public void setUsedWasteQuantity(BigDecimal usedWasteQuantity) {
        this.usedWasteQuantity = usedWasteQuantity;
    }

    public String getUsedWasteUnit() {
        return usedWasteUnit;
    }

    public void setUsedWasteUnit(String usedWasteUnit) {
        this.usedWasteUnit = usedWasteUnit;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getReplacementTo() {
        return replacementTo;
    }

    public void setReplacementTo(String replacementTo) {
        this.replacementTo = replacementTo;
    }

    public Long getCostCurrencyId() {
        return costCurrencyId;
    }

    public void setCostCurrencyId(Long costCurrencyId) {
        this.costCurrencyId = costCurrencyId;
    }

    public BigDecimal getAveragePriceSubcontractor() {
        return averagePriceSubcontractor;
    }

    public void setAveragePriceSubcontractor(BigDecimal averagePriceSubcontractor) {
        this.averagePriceSubcontractor = averagePriceSubcontractor;
    }
}
