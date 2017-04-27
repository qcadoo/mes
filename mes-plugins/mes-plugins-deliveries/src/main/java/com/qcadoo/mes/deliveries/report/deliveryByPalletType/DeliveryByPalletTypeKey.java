package com.qcadoo.mes.deliveries.report.deliveryByPalletType;

import java.util.Date;
import java.util.Objects;

class DeliveryByPalletTypeKey {

    private final Long id;

    private final String number;

    private final Date date;

    public DeliveryByPalletTypeKey(final DeliveryByPalletTypeEntry entry) {
        this.id = entry.getId();
        this.number = entry.getNumber();
        this.date = entry.getDate();
    }

    public Long getId() {
        return id;
    }

    public String getNumber() {
        return number;
    }

    public Date getDate() {
        return date;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof DeliveryByPalletTypeKey))
            return false;
        DeliveryByPalletTypeKey that = (DeliveryByPalletTypeKey) o;
        return Objects.equals(id, that.id) && Objects.equals(number, that.number) && Objects.equals(date, that.date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, number, date);
    }
}
