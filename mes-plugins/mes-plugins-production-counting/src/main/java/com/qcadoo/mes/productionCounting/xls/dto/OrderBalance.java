package com.qcadoo.mes.productionCounting.xls.dto;

import java.math.BigDecimal;

public class OrderBalance {

    private Long orderId;

    private Long rootId;

    private Long productId;

    private String orderNumber;

    private String productNumber;

    private String productName;

    private String productUnit;

    private BigDecimal plannedQuantity;

    private BigDecimal producedQuantity;

    private BigDecimal deviation;

    private BigDecimal plannedMaterialCosts;
    private BigDecimal materialCosts;

    private BigDecimal materialCostsDeviation;

    private BigDecimal plannedProductionCosts;
    private BigDecimal productionCosts;

    private BigDecimal productionCostsDeviation;

    private BigDecimal technicalProductionCosts;

    private BigDecimal materialCostMargin;

    private BigDecimal materialCostMarginValue;

    private BigDecimal productionCostMargin;

    private BigDecimal productionCostMarginValue;

    private BigDecimal additionalOverhead;

    private BigDecimal directAdditionalCost;

    private BigDecimal externalServicesCost;

    private BigDecimal totalCosts;

    private BigDecimal registrationPrice;

    private BigDecimal registrationPriceOverhead;

    private BigDecimal registrationPriceOverheadValue;

    private BigDecimal realProductionCosts;

    private BigDecimal technicalProductionCostOverhead;

    private BigDecimal technicalProductionCostOverheadValue;

    private BigDecimal totalManufacturingCost;

    private BigDecimal profit;

    private BigDecimal profitValue;

    private BigDecimal sellPrice;

    private String additionalFinalProducts;

    public String getAdditionalFinalProducts() {
        return additionalFinalProducts;
    }

    public void setAdditionalFinalProducts(final String additionalFinalProducts) {
        this.additionalFinalProducts = additionalFinalProducts;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(final String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public String getProductNumber() {
        return productNumber;
    }

    public void setProductNumber(final String productNumber) {
        this.productNumber = productNumber;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(final String productName) {
        this.productName = productName;
    }

    public BigDecimal getProducedQuantity() {
        return producedQuantity;
    }

    public void setProducedQuantity(final BigDecimal producedQuantity) {
        this.producedQuantity = producedQuantity;
    }

    public BigDecimal getMaterialCosts() {
        return materialCosts;
    }

    public void setMaterialCosts(final BigDecimal materialCosts) {
        this.materialCosts = materialCosts;
    }

    public BigDecimal getProductionCosts() {
        return productionCosts;
    }

    public void setProductionCosts(final BigDecimal productionCosts) {
        this.productionCosts = productionCosts;
    }

    public BigDecimal getTechnicalProductionCosts() {
        return technicalProductionCosts;
    }

    public void setTechnicalProductionCosts(final BigDecimal technicalProductionCosts) {
        this.technicalProductionCosts = technicalProductionCosts;
    }

    public BigDecimal getMaterialCostMargin() {
        return materialCostMargin;
    }

    public void setMaterialCostMargin(final BigDecimal materialCostMargin) {
        this.materialCostMargin = materialCostMargin;
    }

    public BigDecimal getMaterialCostMarginValue() {
        return materialCostMarginValue;
    }

    public void setMaterialCostMarginValue(final BigDecimal materialCostMarginValue) {
        this.materialCostMarginValue = materialCostMarginValue;
    }

    public BigDecimal getProductionCostMargin() {
        return productionCostMargin;
    }

    public void setProductionCostMargin(final BigDecimal productionCostMargin) {
        this.productionCostMargin = productionCostMargin;
    }

    public BigDecimal getProductionCostMarginValue() {
        return productionCostMarginValue;
    }

    public void setProductionCostMarginValue(final BigDecimal productionCostMarginValue) {
        this.productionCostMarginValue = productionCostMarginValue;
    }

    public BigDecimal getAdditionalOverhead() {
        return additionalOverhead;
    }

    public void setAdditionalOverhead(final BigDecimal additionalOverhead) {
        this.additionalOverhead = additionalOverhead;
    }

    public BigDecimal getDirectAdditionalCost() {
        return directAdditionalCost;
    }

    public void setDirectAdditionalCost(final BigDecimal directAdditionalCost) {
        this.directAdditionalCost = directAdditionalCost;
    }

    public BigDecimal getExternalServicesCost() {
        return externalServicesCost;
    }

    public void setExternalServicesCost(final BigDecimal externalServicesCost) {
        this.externalServicesCost = externalServicesCost;
    }

    public BigDecimal getTotalCosts() {
        return totalCosts;
    }

    public void setTotalCosts(final BigDecimal totalCosts) {
        this.totalCosts = totalCosts;
    }

    public BigDecimal getRegistrationPrice() {
        return registrationPrice;
    }

    public void setRegistrationPrice(final BigDecimal registrationPrice) {
        this.registrationPrice = registrationPrice;
    }

    public BigDecimal getRegistrationPriceOverhead() {
        return registrationPriceOverhead;
    }

    public void setRegistrationPriceOverhead(final BigDecimal registrationPriceOverhead) {
        this.registrationPriceOverhead = registrationPriceOverhead;
    }

    public BigDecimal getRegistrationPriceOverheadValue() {
        return registrationPriceOverheadValue;
    }

    public void setRegistrationPriceOverheadValue(final BigDecimal registrationPriceOverheadValue) {
        this.registrationPriceOverheadValue = registrationPriceOverheadValue;
    }

    public BigDecimal getRealProductionCosts() {
        return realProductionCosts;
    }

    public void setRealProductionCosts(final BigDecimal realProductionCosts) {
        this.realProductionCosts = realProductionCosts;
    }

    public BigDecimal getProfit() {
        return profit;
    }

    public void setProfit(final BigDecimal profit) {
        this.profit = profit;
    }

    public BigDecimal getProfitValue() {
        return profitValue;
    }

    public void setProfitValue(final BigDecimal profitValue) {
        this.profitValue = profitValue;
    }

    public BigDecimal getSellPrice() {
        return sellPrice;
    }

    public void setSellPrice(final BigDecimal sellPrice) {
        this.sellPrice = sellPrice;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(final Long orderId) {
        this.orderId = orderId;
    }

    public Long getRootId() {
        return rootId;
    }

    public void setRootId(final Long rootId) {
        this.rootId = rootId;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(final Long productId) {
        this.productId = productId;
    }

    public BigDecimal getTechnicalProductionCostOverhead() {
        return technicalProductionCostOverhead;
    }

    public void setTechnicalProductionCostOverhead(final BigDecimal technicalProductionCostOverhead) {
        this.technicalProductionCostOverhead = technicalProductionCostOverhead;
    }

    public BigDecimal getTechnicalProductionCostOverheadValue() {
        return technicalProductionCostOverheadValue;
    }

    public void setTechnicalProductionCostOverheadValue(final BigDecimal technicalProductionCostOverheadValue) {
        this.technicalProductionCostOverheadValue = technicalProductionCostOverheadValue;
    }

    public BigDecimal getTotalManufacturingCost() {
        return totalManufacturingCost;
    }

    public void setTotalManufacturingCost(final BigDecimal totalManufacturingCost) {
        this.totalManufacturingCost = totalManufacturingCost;
    }

    public String getProductUnit() {
        return productUnit;
    }

    public void setProductUnit(final String productUnit) {
        this.productUnit = productUnit;
    }

    public BigDecimal getPlannedQuantity() {
        return plannedQuantity;
    }

    public void setPlannedQuantity(final BigDecimal plannedQuantity) {
        this.plannedQuantity = plannedQuantity;
    }

    public BigDecimal getDeviation() {
        return deviation;
    }

    public void setDeviation(final BigDecimal deviation) {
        this.deviation = deviation;
    }

    public BigDecimal getPlannedMaterialCosts() {
        return plannedMaterialCosts;
    }

    public void setPlannedMaterialCosts(final BigDecimal plannedMaterialCosts) {
        this.plannedMaterialCosts = plannedMaterialCosts;
    }

    public BigDecimal getMaterialCostsDeviation() {
        return materialCostsDeviation;
    }

    public void setMaterialCostsDeviation(final BigDecimal materialCostsDeviation) {
        this.materialCostsDeviation = materialCostsDeviation;
    }

    public BigDecimal getPlannedProductionCosts() {
        return plannedProductionCosts;
    }

    public void setPlannedProductionCosts(final BigDecimal plannedProductionCosts) {
        this.plannedProductionCosts = plannedProductionCosts;
    }

    public BigDecimal getProductionCostsDeviation() {
        return productionCostsDeviation;
    }

    public void setProductionCostsDeviation(final BigDecimal productionCostsDeviation) {
        this.productionCostsDeviation = productionCostsDeviation;
    }

}
