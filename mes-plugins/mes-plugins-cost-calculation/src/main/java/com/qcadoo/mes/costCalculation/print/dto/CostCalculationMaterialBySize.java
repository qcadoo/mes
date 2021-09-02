package com.qcadoo.mes.costCalculation.print.dto;

import java.math.BigDecimal;

import com.qcadoo.mes.costNormsForProduct.constants.ProductFieldsCNFP;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

public class CostCalculationMaterialBySize {

    private String technologyNumber;

    private String productNumber;

    private String technologyInputProductType;

    private String materialNumber;

    private String sizeGroupNumber;

    private String unit;

    private BigDecimal nominalCost;

    private String nominalCostCurrency;

    private BigDecimal averageCost;

    private BigDecimal lastPurchaseCost;

    private String lastPurchaseCostCurrency;

    private BigDecimal averageOfferCost;

    private BigDecimal lastOfferCost;

    private BigDecimal costForNumber;

    private BigDecimal quantity;

    public Entity getProductEntity(DataDefinition productDataDefinition) {
        Entity product = productDataDefinition.create();
        product.setField(ProductFieldsCNFP.NOMINAL_COST, nominalCost);
        product.setField(ProductFieldsCNFP.AVERAGE_COST, averageCost);
        product.setField(ProductFieldsCNFP.LAST_PURCHASE_COST, lastPurchaseCost);
        product.setField(ProductFieldsCNFP.AVERAGE_OFFER_COST, averageOfferCost);
        product.setField(ProductFieldsCNFP.LAST_OFFER_COST, lastOfferCost);
        product.setField(ProductFieldsCNFP.COST_FOR_NUMBER, costForNumber);
        return product;
    }

    public String getTechnologyNumber() {
        return technologyNumber;
    }

    public void setTechnologyNumber(String technologyNumber) {
        this.technologyNumber = technologyNumber;
    }

    public String getProductNumber() {
        return productNumber;
    }

    public void setProductNumber(String productNumber) {
        this.productNumber = productNumber;
    }

    public String getTechnologyInputProductType() {
        return technologyInputProductType;
    }

    public void setTechnologyInputProductType(String technologyInputProductType) {
        this.technologyInputProductType = technologyInputProductType;
    }

    public String getMaterialNumber() {
        return materialNumber;
    }

    public void setMaterialNumber(String materialNumber) {
        this.materialNumber = materialNumber;
    }

    public String getSizeGroupNumber() {
        return sizeGroupNumber;
    }

    public void setSizeGroupNumber(String sizeGroupNumber) {
        this.sizeGroupNumber = sizeGroupNumber;
    }

    public BigDecimal getNominalCost() {
        return nominalCost;
    }

    public void setNominalCost(BigDecimal nominalCost) {
        this.nominalCost = nominalCost;
    }

    public BigDecimal getAverageCost() {
        return averageCost;
    }

    public void setAverageCost(BigDecimal averageCost) {
        this.averageCost = averageCost;
    }

    public BigDecimal getLastPurchaseCost() {
        return lastPurchaseCost;
    }

    public void setLastPurchaseCost(BigDecimal lastPurchaseCost) {
        this.lastPurchaseCost = lastPurchaseCost;
    }

    public BigDecimal getAverageOfferCost() {
        return averageOfferCost;
    }

    public void setAverageOfferCost(BigDecimal averageOfferCost) {
        this.averageOfferCost = averageOfferCost;
    }

    public BigDecimal getLastOfferCost() {
        return lastOfferCost;
    }

    public void setLastOfferCost(BigDecimal lastOfferCost) {
        this.lastOfferCost = lastOfferCost;
    }

    public BigDecimal getCostForNumber() {
        return costForNumber;
    }

    public void setCostForNumber(BigDecimal costForNumber) {
        this.costForNumber = costForNumber;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public String getNominalCostCurrency() {
        return nominalCostCurrency;
    }

    public void setNominalCostCurrency(String nominalCostCurrency) {
        this.nominalCostCurrency = nominalCostCurrency;
    }

    public String getLastPurchaseCostCurrency() {
        return lastPurchaseCostCurrency;
    }

    public void setLastPurchaseCostCurrency(String lastPurchaseCostCurrency) {
        this.lastPurchaseCostCurrency = lastPurchaseCostCurrency;
    }
}
