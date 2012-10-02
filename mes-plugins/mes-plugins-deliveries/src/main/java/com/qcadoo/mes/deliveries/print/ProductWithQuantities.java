package com.qcadoo.mes.deliveries.print;

import java.math.BigDecimal;

import com.qcadoo.model.api.Entity;

public class ProductWithQuantities {

    public ProductWithQuantities() {

    }

    private Entity product;

    private BigDecimal orderedQuantity;

    private BigDecimal deliveredQuantity;

    private BigDecimal damagedQuantity;

    public final Entity getProduct() {
        return product;
    }

    public final void setProduct(final Entity product) {
        this.product = product;
    }

    public final BigDecimal getOrderedQuantity() {
        return orderedQuantity;
    }

    public final void setOrderedQuantity(final BigDecimal orderedQuantity) {
        this.orderedQuantity = orderedQuantity;
    }

    public final BigDecimal getDeliveredQuantity() {
        return deliveredQuantity;
    }

    public final void setDeliveredQuantity(final BigDecimal deliveredQuantity) {
        this.deliveredQuantity = deliveredQuantity;
    }

    public final BigDecimal getDamagedQuantity() {
        return damagedQuantity;
    }

    public final void setDamagedQuantity(final BigDecimal damagedQuantity) {
        this.damagedQuantity = damagedQuantity;
    }

}
