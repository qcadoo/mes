package com.qcadoo.mes.materialFlowResources.print.helper;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Objects;

public class ResourceDto {

    private Long id;
    private String number;
    private BigDecimal quantity;
    private BigDecimal price;
    private Date productionDate;
    private Date expirationDate;
    private BigDecimal quantityInAdditionalUnit;
    private BigDecimal conversion;
    private BigDecimal availableQuantity;
    private BigDecimal reservedQuantity;
    private String givenUnit;
    private String productUnit;
    private Long storageLocationId;
    private String storageLocationNumber;
    private Long productId;
    private String productNumber;
    private String productName;
    private Long palletNumberId;
    private String palletNumberNumber;
    private String batch;

    private Long batchId;

    private Long typeOfLoadUnitId;

    public BigDecimal getAvailableQuantity() {
        return availableQuantity;
    }

    public void setAvailableQuantity(BigDecimal availableQuantity) {
        this.availableQuantity = availableQuantity;
    }

    public BigDecimal getConversion() {
        return conversion;
    }

    public void setConversion(BigDecimal conversion) {
        this.conversion = conversion;
    }

    public Date getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(Date expirationDate) {
        this.expirationDate = expirationDate;
    }

    public String getGivenUnit() {
        return givenUnit;
    }

    public void setGivenUnit(String givenUnit) {
        this.givenUnit = givenUnit;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public Long getPalletNumberId() {
        return palletNumberId;
    }

    public void setPalletNumberId(Long palletNumberId) {
        this.palletNumberId = palletNumberId;
    }

    public String getPalletNumberNumber() {
        return palletNumberNumber;
    }

    public void setPalletNumberNumber(String palletNumberNumber) {
        this.palletNumberNumber = palletNumberNumber;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Date getProductionDate() {
        return productionDate;
    }

    public void setProductionDate(Date productionDate) {
        this.productionDate = productionDate;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
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

    public BigDecimal getQuantityInAdditionalUnit() {
        return quantityInAdditionalUnit;
    }

    public void setQuantityInAdditionalUnit(BigDecimal quantityInAdditionalUnit) {
        this.quantityInAdditionalUnit = quantityInAdditionalUnit;
    }

    public BigDecimal getReservedQuantity() {
        return reservedQuantity;
    }

    public void setReservedQuantity(BigDecimal reservedQuantity) {
        this.reservedQuantity = reservedQuantity;
    }

    public Long getStorageLocationId() {
        return storageLocationId;
    }

    public void setStorageLocationId(Long storageLocationId) {
        this.storageLocationId = storageLocationId;
    }

    public String getStorageLocationNumber() {
        return storageLocationNumber;
    }

    public void setStorageLocationNumber(String storageLocationNumber) {
        this.storageLocationNumber = storageLocationNumber;
    }

    public String getBatch() {
        return batch;
    }

    public void setBatch(String batch) {
        this.batch = batch;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof ResourceDto))
            return false;
        ResourceDto that = (ResourceDto) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(number, that.number) &&
                Objects.equals(storageLocationId, that.storageLocationId) &&
                Objects.equals(storageLocationNumber, that.storageLocationNumber) &&
                Objects.equals(productId, that.productId) &&
                Objects.equals(productNumber, that.productNumber) &&
                Objects.equals(palletNumberId, that.palletNumberId) &&
                Objects.equals(batch, that.batch) &&
                Objects.equals(batchId, that.batchId) &&
                Objects.equals(palletNumberNumber, that.palletNumberNumber) &&
                Objects.equals(typeOfLoadUnitId, that.typeOfLoadUnitId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, number, storageLocationId, storageLocationNumber, productId,
                productNumber, palletNumberId, batch, batchId, palletNumberNumber, typeOfLoadUnitId);
    }

    public String getProductUnit() {
        return productUnit;
    }

    public void setProductUnit(String productUnit) {
        this.productUnit = productUnit;
    }

    public Long getBatchId() {
        return batchId;
    }

    public void setBatchId(Long batchId) {
        this.batchId = batchId;
    }

    public Long getTypeOfLoadUnitId() {
        return typeOfLoadUnitId;
    }

    public void setTypeOfLoadUnitId(Long typeOfLoadUnitId) {
        this.typeOfLoadUnitId = typeOfLoadUnitId;
    }
}
