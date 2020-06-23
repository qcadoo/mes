package com.qcadoo.mes.orders.constants;

public enum OrderStartDateBasedOn {

    CURRENT_DATE("01currentDate"), BEGINNING_FIRST_SHIFT_NEXT_DAY("02beginningFirstShiftNextDay"), END_DATE_LAST_ORDER_ON_THE_LINE(
            "03endDateLastOrderOnTheLine");

    private final String value;

    OrderStartDateBasedOn(final String value) {
        this.value = value;
    }

    public String getStringValue() {
        return value;
    }

    public static OrderStartDateBasedOn parseString(final String string) {
        for (OrderStartDateBasedOn val : values()) {
            if (val.getStringValue().equals(string)) {
                return val;
            }
        }

        throw new IllegalStateException("Unsupported OrderStartDateBasedOn: " + string);
    }
}
