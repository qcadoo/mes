package com.qcadoo.mes.orders.controllers.dto;

import java.math.BigDecimal;

public class MaterialDto {

    private Long productId;

    private BigDecimal quantity;

    private BigDecimal quantityPerUnit;

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getQuantityPerUnit() {
        return quantityPerUnit;
    }

    public void setQuantityPerUnit(BigDecimal quantityPerUnit) {
        this.quantityPerUnit = quantityPerUnit;
    }
}
