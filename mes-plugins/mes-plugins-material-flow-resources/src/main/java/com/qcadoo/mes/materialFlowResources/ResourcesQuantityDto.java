package com.qcadoo.mes.materialFlowResources;

import java.math.BigDecimal;

public class ResourcesQuantityDto {
    private String resourceNumber;
    private BigDecimal quantity;
    private BigDecimal additionalQuantity;
    private String productUnit;
    private String productAdditionalUnit;

    public String getResourceNumber() {
        return resourceNumber;
    }

    public void setResourceNumber(String resourceNumber) {
        this.resourceNumber = resourceNumber;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getAdditionalQuantity() {
        return additionalQuantity;
    }

    public void setAdditionalQuantity(BigDecimal additionalQuantity) {
        this.additionalQuantity = additionalQuantity;
    }

    public String getProductUnit() {
        return productUnit;
    }

    public void setProductUnit(String productUnit) {
        this.productUnit = productUnit;
    }

    public String getProductAdditionalUnit() {
        return productAdditionalUnit;
    }

    public void setProductAdditionalUnit(String productAdditionalUnit) {
        this.productAdditionalUnit = productAdditionalUnit;
    }
}
