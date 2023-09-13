package com.qcadoo.mes.materialFlowResources.constants;

import java.math.BigDecimal;

public class StorageLocationsForProductDto {
    private Integer locationId;
    private Integer productId;
    private String locationNumber;
    private String productName;
    private String unit;
    private BigDecimal quantity;
    private String additionalUnit;
    private BigDecimal additionalQuantity;

    public Integer getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    public Integer getLocationId() {
        return locationId;
    }

    public void setLocationId(Integer locationId) {
        this.locationId = locationId;
    }

    public String getLocationNumber() {
        return locationNumber;
    }

    public void setLocationNumber(String locationNumber) {
        this.locationNumber = locationNumber;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
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

    public String getAdditionalUnit() {
        return additionalUnit;
    }

    public void setAdditionalUnit(String additionalUnit) {
        this.additionalUnit = additionalUnit;
    }

    public BigDecimal getAdditionalQuantity() {
        return additionalQuantity;
    }

    public void setAdditionalQuantity(BigDecimal additionalQuantity) {
        this.additionalQuantity = additionalQuantity;
    }
}
