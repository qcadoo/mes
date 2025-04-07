package com.qcadoo.mes.productFlowThruDivision.states;

import com.google.common.collect.Lists;
import com.qcadoo.mes.productionCounting.constants.ProdOutResourceAttrValFields;
import com.qcadoo.model.api.Entity;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class InboundPositionHolder {

    private Entity product;
    private Long productId;
    private BigDecimal usedQuantity;
    private BigDecimal conversion;
    private BigDecimal givenQuantity;
    private String givenUnit;
    private Entity batch;
    private Long batchId;
    private String qualityRating;
    private BigDecimal price;
    private Date expirationDate;
    private Long storageLocationId;
    private Long palletNumberId;
    private Long typeOfLoadUnitId;
    private List<Entity> positionAttributeValues = Lists.newArrayList();

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Long getBatchId() {
        return batchId;
    }

    public void setBatchId(Long batchId) {
        this.batchId = batchId;
    }

    public BigDecimal getUsedQuantity() {
        return usedQuantity;
    }

    public void setUsedQuantity(BigDecimal usedQuantity) {
        this.usedQuantity = usedQuantity;
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

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Date getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(Date expirationDate) {
        this.expirationDate = expirationDate;
    }

    public String getQualityRating() {
        return qualityRating;
    }

    public void setQualityRating(String qualityRating) {
        this.qualityRating = qualityRating;
    }

    public Long getPalletNumberId() {
        return palletNumberId;
    }

    public void setPalletNumberId(Long palletNumberId) {
        this.palletNumberId = palletNumberId;
    }

    public Long getStorageLocationId() {
        return storageLocationId;
    }

    public void setStorageLocationId(Long storageLocationId) {
        this.storageLocationId = storageLocationId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        InboundPositionHolder that = (InboundPositionHolder) o;
        return Objects.equals(productId, that.productId) && Objects.equals(batchId, that.batchId)
                && Objects.equals(storageLocationId, that.storageLocationId) && Objects.equals(palletNumberId, that.palletNumberId)
                && Objects.equals(typeOfLoadUnitId, that.typeOfLoadUnitId) && Objects.equals(givenUnit, that.givenUnit)
                && Objects.equals(positionAttributeValues.stream().sorted(Comparator.comparing(e -> e.getBelongsToField(ProdOutResourceAttrValFields.ATTRIBUTE).getId())).collect(Collectors.toList()),
                that.positionAttributeValues.stream().sorted(Comparator.comparing(e -> e.getBelongsToField(ProdOutResourceAttrValFields.ATTRIBUTE).getId())).collect(Collectors.toList()));
    }

    @Override
    public int hashCode() {
        return Objects.hash(productId, batchId, storageLocationId, palletNumberId, typeOfLoadUnitId, givenUnit, positionAttributeValues.stream().sorted(Comparator.comparing(e -> e.getBelongsToField(ProdOutResourceAttrValFields.ATTRIBUTE).getId())).collect(Collectors.toList()));
    }

    public List<Entity> getPositionAttributeValues() {
        return positionAttributeValues;
    }

    public void setPositionAttributeValues(List<Entity> positionAttributeValues) {
        this.positionAttributeValues = positionAttributeValues;
    }

    public Entity getProduct() {
        return product;
    }

    public void setProduct(Entity product) {
        this.product = product;
    }

    public Entity getBatch() {
        return batch;
    }

    public void setBatch(Entity batch) {
        this.batch = batch;
    }

    public Long getTypeOfLoadUnitId() {
        return typeOfLoadUnitId;
    }

    public void setTypeOfLoadUnitId(Long typeOfLoadUnitId) {
        this.typeOfLoadUnitId = typeOfLoadUnitId;
    }
}
