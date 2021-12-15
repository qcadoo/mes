package com.qcadoo.mes.productFlowThruDivision.service;

import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityFields;
import com.qcadoo.model.api.BigDecimalUtils;
import com.qcadoo.model.api.Entity;

import java.math.BigDecimal;
import java.util.Objects;

public class ProductionCountingQuantityHolder {

    private Long productId;
    private Entity product;
    private BigDecimal usedQuantity;
    private BigDecimal plannedQuantity;

    public ProductionCountingQuantityHolder(Entity pcq) {
        this.productId = pcq.getBelongsToField(ProductionCountingQuantityFields.PRODUCT).getId();
        this.product = pcq.getBelongsToField(ProductionCountingQuantityFields.PRODUCT);
        this.usedQuantity = BigDecimalUtils.convertNullToZero(pcq.getDecimalField(ProductionCountingQuantityFields.USED_QUANTITY));
        this.plannedQuantity = pcq.getDecimalField(ProductionCountingQuantityFields.PLANNED_QUANTITY);
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Entity getProduct() {
        return product;
    }

    public void setProduct(Entity product) {
        this.product = product;
    }

    public BigDecimal getUsedQuantity() {
        return usedQuantity;
    }

    public void setUsedQuantity(BigDecimal usedQuantity) {
        this.usedQuantity = usedQuantity;
    }

    public BigDecimal getPlannedQuantity() {
        return plannedQuantity;
    }

    public void setPlannedQuantity(BigDecimal plannedQuantity) {
        this.plannedQuantity = plannedQuantity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductionCountingQuantityHolder that = (ProductionCountingQuantityHolder) o;
        return Objects.equals(productId, that.productId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productId);
    }
}
