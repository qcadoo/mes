package com.qcadoo.mes.productFlowThruDivision.service;

import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityFields;
import com.qcadoo.model.api.BigDecimalUtils;
import com.qcadoo.model.api.Entity;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

public class ProductionCountingQuantityHolder {

    private Long productId;
    private Long batchId;
    private Long resourceId;
    private Entity product;
    private Entity batch;
    private Entity resource;
    private BigDecimal usedQuantity;
    private BigDecimal conversion;
    private BigDecimal givenQuantity;
    private String givenUnit;
    private BigDecimal plannedQuantity;

    public ProductionCountingQuantityHolder(Entity pcq, boolean useUsedQuantity) {
        this.productId = pcq.getBelongsToField(ProductionCountingQuantityFields.PRODUCT).getId();
        this.product = pcq.getBelongsToField(ProductionCountingQuantityFields.PRODUCT);
        this.usedQuantity = BigDecimalUtils.convertNullToZero(pcq.getDecimalField(ProductionCountingQuantityFields.USED_QUANTITY));
        this.plannedQuantity = pcq.getDecimalField(ProductionCountingQuantityFields.PLANNED_QUANTITY);
        if(!useUsedQuantity) {
            List<Entity> batches = pcq.getHasManyField(ProductionCountingQuantityFields.BATCHES);
            if(batches.size() == 1) {
                this.batch = batches.get(0);
                this.batchId = getBatch().getId();
            }
        }
    }

    public ProductionCountingQuantityHolder() {

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

    public Long getBatchId() {
        return batchId;
    }

    public void setBatchId(Long batchId) {
        this.batchId = batchId;
    }

    public Entity getBatch() {
        return batch;
    }

    public void setBatch(Entity batch) {
        this.batch = batch;
    }

    public BigDecimal getConversion() {
        return conversion;
    }

    public void setConversion(BigDecimal conversion) {
        this.conversion = conversion;
    }

    public BigDecimal getGivenQuantity() {
        return givenQuantity;
    }

    public void setGivenQuantity(BigDecimal givenQuantity) {
        this.givenQuantity = givenQuantity;
    }

    public String getGivenUnit() {
        return givenUnit;
    }

    public void setGivenUnit(String givenUnit) {
        this.givenUnit = givenUnit;
    }

    public Long getResourceId() {
        return resourceId;
    }

    public void setResourceId(Long resourceId) {
        this.resourceId = resourceId;
    }

    public Entity getResource() {
        return resource;
    }

    public void setResource(Entity resource) {
        this.resource = resource;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductionCountingQuantityHolder that = (ProductionCountingQuantityHolder) o;
        return Objects.equals(productId, that.productId) && Objects.equals(batchId, that.batchId) && Objects.equals(resourceId, that.resourceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productId, batchId, resourceId);
    }
}
