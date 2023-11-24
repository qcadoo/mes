package com.qcadoo.mes.materialFlowResources.dto;

import com.qcadoo.mes.advancedGenealogy.constants.BatchFields;
import com.qcadoo.mes.basic.constants.PalletNumberFields;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.materialFlow.constants.LocationFields;
import com.qcadoo.mes.materialFlowResources.constants.ResourceFields;
import com.qcadoo.mes.materialFlowResources.constants.StorageLocationFields;
import com.qcadoo.model.api.Entity;

import java.math.BigDecimal;
import java.util.Date;

public class ResourceToRepackDto {
    private Long id;
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
    private Boolean blockedForQualityControl;
    private String qualityRating;

    public ResourceToRepackDto(Entity resource) {
        this.id = resource.getId();
        Entity product = resource.getBelongsToField(ResourceFields.PRODUCT);
        this.productNumber = product.getStringField(ProductFields.NUMBER);
        this.productName = product.getStringField(ProductFields.NAME);
        this.resourceNumber = resource.getStringField(ResourceFields.NUMBER);
        Entity batch = resource.getBelongsToField(ResourceFields.BATCH);
        this.batchNumber = batch != null ? batch.getStringField(BatchFields.NUMBER) : "";
        Entity storageLocation = resource.getBelongsToField(ResourceFields.STORAGE_LOCATION);
        this.storageLocationNumber = storageLocation != null ? storageLocation.getStringField(StorageLocationFields.NUMBER) : "";
        this.quantity = resource.getDecimalField(ResourceFields.QUANTITY);
        this.unit = product.getStringField(ProductFields.UNIT);
        this.additionalQuantity = resource.getDecimalField(ResourceFields.QUANTITY_IN_ADDITIONAL_UNIT);
        this.additionalUnit = resource.getStringField(ResourceFields.GIVEN_UNIT);
        this.expirationDate = resource.getDateField(ResourceFields.EXPIRATION_DATE);
        this.locationNumber = resource.getBelongsToField(ResourceFields.LOCATION).getStringField(LocationFields.NUMBER);
        Entity palletNr = resource.getBelongsToField(ResourceFields.PALLET_NUMBER);
        this.palletNumber = palletNr != null ? palletNr.getStringField(PalletNumberFields.NUMBER) : "";
        this.palletType = resource.getStringField(ResourceFields.TYPE_OF_PALLET);
    }

    public String getQualityRating() {
        return qualityRating;
    }

    public void setQualityRating(String qualityRating) {
        this.qualityRating = qualityRating;
    }

    public Boolean getBlockedForQualityControl() {
        return blockedForQualityControl;
    }

    public void setBlockedForQualityControl(Boolean blockedForQualityControl) {
        this.blockedForQualityControl = blockedForQualityControl;
    }

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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
