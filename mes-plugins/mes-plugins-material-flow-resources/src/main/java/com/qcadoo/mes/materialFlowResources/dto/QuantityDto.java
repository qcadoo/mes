package com.qcadoo.mes.materialFlowResources.dto;

import java.math.BigDecimal;

public class QuantityDto {
    String productNumber;
    BigDecimal quantity;
    BigDecimal additionalQuantity;

    public QuantityDto() {
    }

    public QuantityDto(String productNumber, BigDecimal quantity, BigDecimal additionalQuantity) {
        this.productNumber = productNumber;
        this.quantity = quantity;
        this.additionalQuantity = additionalQuantity;
    }

    public String getProductNumber() {
        return productNumber;
    }

    public void setProductNumber(String productNumber) {
        this.productNumber = productNumber;
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
}
