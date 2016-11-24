package com.qcadoo.mes.materialFlowResources.print.helper;

import com.google.common.base.Objects;

import java.math.BigDecimal;

public class Position {

    private String index;
    private Long product;
    private String storageLocation;
    private String typeOfPallet;
    private String palletNumber;
    private BigDecimal quantity;
    private String additionalCode;
    private String productName;
    private String unit;
    private String targetPallet;

    public Position(String index, Long product, String storageLocation, String typeOfPallet, String palletNumber,
            BigDecimal quantity, String additionalCode, String productName, String unit, String targetPallet) {
        this.index = index;
        this.product = product;
        this.storageLocation = storageLocation;
        this.typeOfPallet = typeOfPallet;
        this.palletNumber = palletNumber;
        this.quantity = quantity;
        this.additionalCode = additionalCode;
        this.productName = productName;
        this.unit = unit;
        this.targetPallet = targetPallet;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public String getIndex() {
        return index;
    }

    public String getTargetPallet() {
        return targetPallet;
    }

    public Long getProduct() {
        return product;
    }

    public String getStorageLocation() {
        return storageLocation;
    }

    public String getTypeOfPallet() {
        return typeOfPallet;
    }

    public String getPalletNumber() {
        return palletNumber;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public String getAdditionalCode() {
        return additionalCode;
    }

    public String getProductName() {
        return productName;
    }

    public String getUnit() {
        return unit;
    }

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Position that = (Position) o;
        return Objects.equal(product, that.product) &&
                Objects.equal(storageLocation, that.storageLocation) &&
                Objects.equal(typeOfPallet, that.typeOfPallet) &&
                Objects.equal(palletNumber, that.palletNumber);
    }

    @Override public int hashCode() {
        return Objects.hashCode(product, storageLocation, typeOfPallet, palletNumber);
    }
}
