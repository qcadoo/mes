package com.qcadoo.mes.deliveries.report.deliveryByPalletType;

import java.util.Date;

public class DeliveryByPalletTypeEntry {

    private Long id;
    private String number;
    private String typeOfLoadUnit;
    private Date date;
    private Integer numberOfPallets;

    public DeliveryByPalletTypeEntry() {
    }

    public DeliveryByPalletTypeEntry(Long id, String number, String typeOfLoadUnit, Date date, Integer numberOfPallets) {
        this.id = id;
        this.number = number;
        this.typeOfLoadUnit = typeOfLoadUnit;
        this.date = date;
        this.numberOfPallets = numberOfPallets;
    }

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

    public String getTypeOfLoadUnit() {
        return typeOfLoadUnit;
    }

    public void setTypeOfLoadUnit(String typeOfLoadUnit) {
        this.typeOfLoadUnit = typeOfLoadUnit;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Integer getNumberOfPallets() {
        return numberOfPallets;
    }

    public void setNumberOfPallets(Integer numberOfPallets) {
        this.numberOfPallets = numberOfPallets;
    }
}
