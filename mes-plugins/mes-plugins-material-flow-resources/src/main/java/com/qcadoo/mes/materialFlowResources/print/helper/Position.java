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

    private String productName;

    private String unit;

    private String batch;

    public Position(final String index, final Long product, final String storageLocation, final String typeOfPallet,
                    final String palletNumber, final BigDecimal quantity, final String productName, final String unit,
                    final String batch) {
        this.index = index;
        this.product = product;
        this.storageLocation = storageLocation;
        this.typeOfPallet = typeOfPallet;
        this.palletNumber = palletNumber;
        this.quantity = quantity;
        this.productName = productName;
        this.unit = unit;
        this.batch = batch;
    }

    public void setQuantity(final BigDecimal quantity) {
        this.quantity = quantity;
    }

    public String getIndex() {
        return index;
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

    public String getProductName() {
        return productName;
    }

    public String getUnit() {
        return unit;
    }

    public String getBatch() {
        return batch;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Position that = (Position) o;

        return Objects.equal(product, that.product) && Objects.equal(storageLocation, that.storageLocation)
                && Objects.equal(typeOfPallet, that.typeOfPallet) && Objects.equal(palletNumber, that.palletNumber)
                && Objects.equal(batch, that.batch);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(product, storageLocation, typeOfPallet, palletNumber, batch);
    }

}
