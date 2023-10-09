package com.qcadoo.mes.materialFlowResources.dto;

import java.math.BigDecimal;
import java.util.Date;

public class ResourceToRepackDto {
    private Long locationId;
    private String resourceNumber;
    private String productName;
    private String locationNumber;
    private String storageLocationNumber;
    private String productNumber;
    private String conversionValue;
    private String palletNumber;
    private String palletType;
    private String batchNumber;
    private Date productionDate;
    private Date expirationDate;
    private String additionalUnit;
    private BigDecimal quantity;
    private String unit;
    private BigDecimal additionalQuantity;

    public String getResourceNumber() {
        return resourceNumber;
    }

    public void setResourceNumber(String resourceNumber) {
        this.resourceNumber = resourceNumber;
    }

    public Long getLocationId() {
        return locationId;
    }

    public void setLocationId(Long locationId) {
        this.locationId = locationId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getLocationNumber() {
        return locationNumber;
    }

    public void setLocationNumber(String locationNumber) {
        this.locationNumber = locationNumber;
    }

    public String getStorageLocationNumber() {
        return storageLocationNumber;
    }

    public void setStorageLocationNumber(String storageLocationNumber) {
        this.storageLocationNumber = storageLocationNumber;
    }

    public String getProductNumber() {
        return productNumber;
    }

    public void setProductNumber(String productNumber) {
        this.productNumber = productNumber;
    }

    public String getConversionValue() {
        return conversionValue;
    }

    public void setConversionValue(String conversionValue) {
        this.conversionValue = conversionValue;
    }

    public String getPalletNumber() {
        return palletNumber;
    }

    public void setPalletNumber(String palletNumber) {
        this.palletNumber = palletNumber;
    }

    public String getPalletType() {
        return palletType;
    }

    public void setPalletType(String palletType) {
        this.palletType = palletType;
    }

    public String getBatchNumber() {
        return batchNumber;
    }

    public void setBatchNumber(String batchNumber) {
        this.batchNumber = batchNumber;
    }

    public Date getProductionDate() {
        return productionDate;
    }

    public void setProductionDate(Date productionDate) {
        this.productionDate = productionDate;
    }

    public Date getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(Date expirationDate) {
        this.expirationDate = expirationDate;
    }

    public String getAdditionalUnit() {
        return additionalUnit;
    }

    public void setAdditionalUnit(String additionalUnit) {
        this.additionalUnit = additionalUnit;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public BigDecimal getAdditionalQuantity() {
        return additionalQuantity;
    }

    public void setAdditionalQuantity(BigDecimal additionalQuantity) {
        this.additionalQuantity = additionalQuantity;
    }
}
