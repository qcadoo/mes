package com.qcadoo.mes.materialFlowResources.dto;

public class MovedPalletDto {

    private String palletNumber;

    private String storageLocationNumber;

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

        if (palletNumber != null ? !palletNumber.equals(that.palletNumber) : that.palletNumber != null)
            return false;
        return storageLocationNumber != null ? storageLocationNumber.equals(that.storageLocationNumber)
                : that.storageLocationNumber == null;
    }

    @Override
    public int hashCode() {
        int result = palletNumber != null ? palletNumber.hashCode() : 0;
        result = 31 * result + (storageLocationNumber != null ? storageLocationNumber.hashCode() : 0);
        return result;
    }
}