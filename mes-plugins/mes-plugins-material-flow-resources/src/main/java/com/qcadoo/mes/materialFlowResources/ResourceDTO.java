package com.qcadoo.mes.materialFlowResources;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.google.common.collect.Maps;
import com.qcadoo.mes.basic.controllers.dataProvider.dto.AbstractDTO;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

public class ResourceDTO implements AbstractDTO {

    private Long id;

    private String number;

    private String batch;

    private Long batchId;

    private BigDecimal quantity;

    private BigDecimal quantityInAdditionalUnit;

    private BigDecimal reservedQuantity;

    private BigDecimal availableQuantity;

    private BigDecimal price;

    private BigDecimal conversion;

    private String givenUnit;

    private String unit;

    private String product;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "CET")
    private Date expirationDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "CET")
    private Date productionDate;

    private String storageLocation;

    private String palletNumber;

    private String typeOfLoadUnit;

    @JsonFormat(shape = JsonFormat.Shape.BOOLEAN)
    private Boolean waste;

    @JsonFormat(shape = JsonFormat.Shape.BOOLEAN)
    private Boolean lastResource;

    private String lastResourceString;

    private String wasteString;

    private Map<String, Object> attrs = Maps.newHashMap();

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

    public String getBatch() {
        return batch;
    }

    public void setBatch(String batch) {
        this.batch = batch;
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

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getConversion() {
        return conversion;
    }

    public void setConversion(BigDecimal conversion) {
        this.conversion = conversion;
    }

    public String getGivenUnit() {
        return givenUnit;
    }

    public void setGivenUnit(String givenUnit) {
        this.givenUnit = givenUnit;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public Date getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(Date expirationDate) {
        this.expirationDate = expirationDate;
    }

    public Date getProductionDate() {
        return productionDate;
    }

    public void setProductionDate(Date productionDate) {
        this.productionDate = productionDate;
    }

    public String getStorageLocation() {
        return storageLocation;
    }

    public void setStorageLocation(String storageLocation) {
        this.storageLocation = storageLocation;
    }

    public String getPalletNumber() {
        return palletNumber;
    }

    public void setPalletNumber(String palletNumber) {
        this.palletNumber = palletNumber;
    }

    public String getTypeOfLoadUnit() {
        return typeOfLoadUnit;
    }

    public void setTypeOfLoadUnit(String typeOfLoadUnit) {
        this.typeOfLoadUnit = typeOfLoadUnit;
    }

    public Boolean isWaste() {
        return waste;
    }

    public void setWaste(Boolean isWaste) {
        this.waste = isWaste;
    }

    public String getWasteString() {
        return wasteString;
    }

    public void setWasteString(String wasteString) {
        this.wasteString = wasteString;
    }

    public BigDecimal getReservedQuantity() {
        return reservedQuantity;
    }

    public void setReservedQuantity(BigDecimal reservedQuantity) {
        this.reservedQuantity = reservedQuantity;
    }

    public BigDecimal getAvailableQuantity() {
        return availableQuantity;
    }

    public void setAvailableQuantity(BigDecimal availableQuantity) {
        this.availableQuantity = availableQuantity;
    }

    public Boolean getLastResource() {
        return lastResource;
    }

    public void setLastResource(Boolean lastResource) {
        this.lastResource = lastResource;
    }

    public String getLastResourceString() {
        return lastResourceString;
    }

    public void setLastResourceString(String lastResourceString) {
        this.lastResourceString = lastResourceString;
    }

    public Map<String, Object> getAttrs() {
        return attrs;
    }

    public void setAttrs(Map<String, Object> attrs) {
        this.attrs = attrs;
    }

    public Long getBatchId() {
        return batchId;
    }

    public void setBatchId(Long batchId) {
        this.batchId = batchId;
    }
}
