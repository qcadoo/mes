package com.qcadoo.mes.productionCounting.xls.dto;

import java.math.BigDecimal;

public class OrderBalance {

    private Long orderId;

    private Long rootId;

    private Long productId;

    private String orderNumber;

    private String productNumber;

    private String productName;

    private BigDecimal producedQuantity;

    private BigDecimal materialCosts;

    private BigDecimal productionCosts;

    private BigDecimal technicalProductionCosts;

    private BigDecimal materialCostMargin;

    private BigDecimal materialCostMarginValue;

    private BigDecimal productionCostMargin;

    private BigDecimal productionCostMarginValue;

    private BigDecimal additionalOverhead;

    private BigDecimal directAdditionalCost;

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

    public BigDecimal getProducedQuantity() {
        return producedQuantity;
    }

    public void setProducedQuantity(BigDecimal producedQuantity) {
        this.producedQuantity = producedQuantity;
    }

    public BigDecimal getMaterialCosts() {
        return materialCosts;
    }

    public void setMaterialCosts(BigDecimal materialCosts) {
        this.materialCosts = materialCosts;
    }

    public BigDecimal getProductionCosts() {
        return productionCosts;
    }

    public void setProductionCosts(BigDecimal productionCosts) {
        this.productionCosts = productionCosts;
    }

    public BigDecimal getTechnicalProductionCosts() {
        return technicalProductionCosts;
    }

    public void setTechnicalProductionCosts(BigDecimal technicalProductionCosts) {
        this.technicalProductionCosts = technicalProductionCosts;
    }

    public BigDecimal getMaterialCostMargin() {
        return materialCostMargin;
    }

    public void setMaterialCostMargin(BigDecimal materialCostMargin) {
        this.materialCostMargin = materialCostMargin;
    }

    public BigDecimal getMaterialCostMarginValue() {
        return materialCostMarginValue;
    }

    public void setMaterialCostMarginValue(BigDecimal materialCostMarginValue) {
        this.materialCostMarginValue = materialCostMarginValue;
    }

    public BigDecimal getProductionCostMargin() {
        return productionCostMargin;
    }

    public void setProductionCostMargin(BigDecimal productionCostMargin) {
        this.productionCostMargin = productionCostMargin;
    }

    public BigDecimal getProductionCostMarginValue() {
        return productionCostMarginValue;
    }

    public void setProductionCostMarginValue(BigDecimal productionCostMarginValue) {
        this.productionCostMarginValue = productionCostMarginValue;
    }

    public BigDecimal getAdditionalOverhead() {
        return additionalOverhead;
    }

    public void setAdditionalOverhead(BigDecimal additionalOverhead) {
        this.additionalOverhead = additionalOverhead;
    }

    public BigDecimal getDirectAdditionalCost() {
        return directAdditionalCost;
    }

    public void setDirectAdditionalCost(BigDecimal directAdditionalCost) {
        this.directAdditionalCost = directAdditionalCost;
    }

    public BigDecimal getTotalCosts() {
        return totalCosts;
    }

    public void setTotalCosts(BigDecimal totalCosts) {
        this.totalCosts = totalCosts;
    }

    public BigDecimal getRegistrationPrice() {
        return registrationPrice;
    }

    public void setRegistrationPrice(BigDecimal registrationPrice) {
        this.registrationPrice = registrationPrice;
    }

    public BigDecimal getRegistrationPriceOverhead() {
        return registrationPriceOverhead;
    }

    public void setRegistrationPriceOverhead(BigDecimal registrationPriceOverhead) {
        this.registrationPriceOverhead = registrationPriceOverhead;
    }

    public BigDecimal getRegistrationPriceOverheadValue() {
        return registrationPriceOverheadValue;
    }

    public void setRegistrationPriceOverheadValue(BigDecimal registrationPriceOverheadValue) {
        this.registrationPriceOverheadValue = registrationPriceOverheadValue;
    }

    public BigDecimal getRealProductionCosts() {
        return realProductionCosts;
    }

    public void setRealProductionCosts(BigDecimal realProductionCosts) {
        this.realProductionCosts = realProductionCosts;
    }

    public BigDecimal getProfit() {
        return profit;
    }

    public void setProfit(BigDecimal profit) {
        this.profit = profit;
    }

    public BigDecimal getProfitValue() {
        return profitValue;
    }

    public void setProfitValue(BigDecimal profitValue) {
        this.profitValue = profitValue;
    }

    public BigDecimal getSellPrice() {
        return sellPrice;
    }

    public void setSellPrice(BigDecimal sellPrice) {
        this.sellPrice = sellPrice;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Long getRootId() {
        return rootId;
    }

    public void setRootId(Long rootId) {
        this.rootId = rootId;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public BigDecimal getTechnicalProductionCostOverhead() {
        return technicalProductionCostOverhead;
    }

    public void setTechnicalProductionCostOverhead(BigDecimal technicalProductionCostOverhead) {
        this.technicalProductionCostOverhead = technicalProductionCostOverhead;
    }

    public BigDecimal getTechnicalProductionCostOverheadValue() {
        return technicalProductionCostOverheadValue;
    }

    public void setTechnicalProductionCostOverheadValue(BigDecimal technicalProductionCostOverheadValue) {
        this.technicalProductionCostOverheadValue = technicalProductionCostOverheadValue;
    }

    public BigDecimal getTotalManufacturingCost() {
        return totalManufacturingCost;
    }

    public void setTotalManufacturingCost(BigDecimal totalManufacturingCost) {
        this.totalManufacturingCost = totalManufacturingCost;
    }
}
