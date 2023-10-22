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

    private String batch;

    public PositionBuilder setIndex(final String index) {
        this.index = index;
        return this;
    }

    public PositionBuilder setProduct(final Long product) {
        this.product = product;
        return this;
    }

    public PositionBuilder setStorageLocation(final String storageLocation) {
        this.storageLocation = storageLocation;
        return this;
    }

    public PositionBuilder setTypeOfPallet(final String typeOfPallet) {
        this.typeOfPallet = typeOfPallet;
        return this;
    }

    public PositionBuilder setPalletNumber(final String palletNumber) {
        this.palletNumber = palletNumber;
        return this;
    }

    public PositionBuilder setQuantity(final BigDecimal quantity) {
        this.quantity = quantity;
        return this;
    }

    public PositionBuilder setProductName(final String productName) {
        this.productName = productName;
        return this;
    }

    public PositionBuilder setUnit(final String unit) {
        this.unit = unit;
        return this;
    }

    public PositionBuilder setBatch(final String batch) {
        this.batch = batch;
        return this;
    }

    public Position createPosition() {
        return new Position(index, product, storageLocation, typeOfPallet, palletNumber, quantity, productName,
                unit, batch);
    }

}
