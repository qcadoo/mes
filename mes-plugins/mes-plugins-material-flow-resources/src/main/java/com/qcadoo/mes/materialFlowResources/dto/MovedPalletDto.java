package com.qcadoo.mes.materialFlowResources.dto;

import java.util.Objects;

public class MovedPalletDto {

    private String palletNumber;

    private String storageLocationNumber;

    private String locationNumber;

    private String typeOfPallet;

    public String getTypeOfPallet() {
        return typeOfPallet;
    }

    public void setTypeOfPallet(String typeOfPallet) {
        this.typeOfPallet = typeOfPallet;
    }

    public String getLocationNumber() {
        return locationNumber;
    }

    public void setLocationNumber(String locationNumber) {
        this.locationNumber = locationNumber;
    }

    public String getPalletNumber() {
        return palletNumber;
    }

    public void setPalletNumber(String palletNumber) {
        this.palletNumber = palletNumber;
    }

    public String getStorageLocationNumber() {
        return storageLocationNumber;
    }

    public void setStorageLocationNumber(String storageLocationNumber) {
        this.storageLocationNumber = storageLocationNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        MovedPalletDto that = (MovedPalletDto) o;
        return Objects.equals(palletNumber, that.palletNumber)
                && Objects.equals(storageLocationNumber, that.storageLocationNumber)
                && Objects.equals(locationNumber, that.locationNumber) && Objects.equals(typeOfPallet, that.typeOfPallet);
    }

    @Override
    public int hashCode() {
        return Objects.hash(palletNumber, storageLocationNumber, locationNumber, typeOfPallet);
    }
}