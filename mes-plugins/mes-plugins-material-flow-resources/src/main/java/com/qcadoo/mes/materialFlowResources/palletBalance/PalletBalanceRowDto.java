package com.qcadoo.mes.materialFlowResources.palletBalance;

import java.util.Date;

public class PalletBalanceRowDto {

    private String typeOfLoadUnit;

    private Date day;

    private int palletsCount;

    public String getTypeOfLoadUnit() {
        return typeOfLoadUnit;
    }

    public void setTypeOfLoadUnit(String typeOfLoadUnit) {
        this.typeOfLoadUnit = typeOfLoadUnit;
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

    public PalletBalanceRowDto(String typeOfLoadUnit, Date day, int palletsCount) {
        this.typeOfLoadUnit = typeOfLoadUnit;
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
        if (!typeOfLoadUnit.equals(that.typeOfLoadUnit))
            return false;
        return day.equals(that.day);
    }

    @Override
    public int hashCode() {
        int result = typeOfLoadUnit.hashCode();
        result = 31 * result + day.hashCode();
        result = 31 * result + palletsCount;
        return result;
    }
}
