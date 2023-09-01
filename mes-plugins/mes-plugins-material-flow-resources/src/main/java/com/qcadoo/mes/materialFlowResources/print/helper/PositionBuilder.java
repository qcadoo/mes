package com.qcadoo.mes.materialFlowResources.print.helper;

import java.math.BigDecimal;

public class PositionBuilder {

    private String index;

    private Long product;

    private String storageLocation;

    private String typeOfPallet;

    private String palletNumber;

    private BigDecimal quantity;

    private String productName;

    private String unit;

    private String targetPallet;

    private String batch;

    public PositionBuilder setIndex(String index) {
        this.index = index;
        return this;
    }

    public PositionBuilder setProduct(Long product) {
        this.product = product;
        return this;
    }

    public PositionBuilder setStorageLocation(String storageLocation) {
        this.storageLocation = storageLocation;
        return this;
    }

    public PositionBuilder setTypeOfPallet(String typeOfPallet) {
        this.typeOfPallet = typeOfPallet;
        return this;
    }

    public PositionBuilder setPalletNumber(String palletNumber) {
        this.palletNumber = palletNumber;
        return this;
    }

    public PositionBuilder setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
        return this;
    }

    public PositionBuilder setProductName(String productName) {
        this.productName = productName;
        return this;
    }

    public PositionBuilder setUnit(String unit) {
        this.unit = unit;
        return this;
    }

    public PositionBuilder setTargetPallet(String targetPallet) {
        this.targetPallet = targetPallet;
        return this;
    }

    public PositionBuilder setBatch(String batch) {
        this.batch = batch;
        return this;
    }

    public Position createPosition() {
        return new Position(index, product, storageLocation, typeOfPallet, palletNumber, quantity, productName,
                unit, targetPallet, batch);
    }
}
