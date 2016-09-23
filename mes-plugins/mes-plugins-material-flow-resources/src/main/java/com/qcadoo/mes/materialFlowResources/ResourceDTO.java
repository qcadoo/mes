package com.qcadoo.mes.materialFlowResources;

import java.math.BigDecimal;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.qcadoo.mes.basic.controllers.dataProvider.dto.AbstractDTO;


public class ResourceDTO implements AbstractDTO{

    private Long id;

    private String number;

    private String additionalCode;

    private String ac;

    private String batch;

    private BigDecimal quantity;

    private BigDecimal quantityInAdditionalUnit;

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

    private String typeOfPallet;

    @JsonFormat(shape = JsonFormat.Shape.BOOLEAN)
    private Boolean waste;

    private String wasteString;

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

    public String getAdditionalCode() {
        return additionalCode;
    }

    public void setAdditionalCode(String additionalCode) {
        this.additionalCode = additionalCode;
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

    public String getTypeOfPallet() {
        return typeOfPallet;
    }

    public void setTypeOfPallet(String typeOfPallet) {
        this.typeOfPallet = typeOfPallet;
    }

    public String getAc() {
        return ac;
    }

    public void setAc(String ac) {
        this.ac = ac;
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
}
