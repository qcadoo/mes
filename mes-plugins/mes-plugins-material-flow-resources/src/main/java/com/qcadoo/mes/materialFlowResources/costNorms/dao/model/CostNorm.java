package com.qcadoo.mes.materialFlowResources.costNorms.dao.model;

import java.math.BigDecimal;

public class CostNorm {

    private Long productId;

    private BigDecimal lastPurchaseCost;

    private BigDecimal averageCost;

    private BigDecimal nominalCost;

    private BigDecimal costForNumber;

    public CostNorm() {
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public BigDecimal getLastPurchaseCost() {
        return lastPurchaseCost;
    }

    public void setLastPurchaseCost(BigDecimal value) {
        this.lastPurchaseCost = value;
    }

    public BigDecimal getAverageCost() {
        return averageCost;
    }

    public void setAverageCost(BigDecimal averageCost) {
        this.averageCost = averageCost;
    }

    public BigDecimal getNominalCost() {
        return nominalCost;
    }

    public void setNominalCost(BigDecimal nominalCost) {
        this.nominalCost = nominalCost;
    }

    public BigDecimal getCostForNumber() {
        return costForNumber;
    }

    public void setCostForNumber(BigDecimal costForNumber) {
        this.costForNumber = costForNumber;
    }
}
