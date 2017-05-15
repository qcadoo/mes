package com.qcadoo.mes.deliveries.report.deliveryByPalletType;

import java.util.Date;

public class DeliveryByPalletTypeEntry {

    private Long id;
    private String number;
    private String palletType;
    private Date date;
    private Integer numberOfPallets;

    public DeliveryByPalletTypeEntry() {

    }

    public DeliveryByPalletTypeEntry(Long id, String number, String palletType, Date date, Integer numberOfPallets) {
        this.id = id;
        this.number = number;
        this.palletType = palletType;
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

    public String getPalletType() {
        return palletType;
    }

    public void setPalletType(String palletType) {
        this.palletType = palletType;
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
