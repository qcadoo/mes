package com.qcadoo.mes.materialFlowResources.palletBalance;

import java.util.Date;

public class PalletBalanceRowDto {

    private String typeOfPallet;

    private Date day;

    private int palletsCount;

    public String getTypeOfPallet() {
        return typeOfPallet;
    }

    public void setTypeOfPallet(String typeOfPallet) {
        this.typeOfPallet = typeOfPallet;
    }

    public Date getDay() {
        return day;
    }

    public void setDay(Date day) {
        this.day = day;
    }

    public int getPalletsCount() {
        return palletsCount;
    }

    public void setPalletsCount(int palletsCount) {
        this.palletsCount = palletsCount;
    }

    public PalletBalanceRowDto() {
        palletsCount = 0;
    }

    public PalletBalanceRowDto(String typeOfPallet, Date day, int palletsCount) {
        this.typeOfPallet = typeOfPallet;
        this.day = day;
        this.palletsCount = palletsCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        PalletBalanceRowDto that = (PalletBalanceRowDto) o;

        if (palletsCount != that.palletsCount)
            return false;
        if (!typeOfPallet.equals(that.typeOfPallet))
            return false;
        return day.equals(that.day);
    }

    @Override
    public int hashCode() {
        int result = typeOfPallet.hashCode();
        result = 31 * result + day.hashCode();
        result = 31 * result + palletsCount;
        return result;
    }
}
