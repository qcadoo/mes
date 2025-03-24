package com.qcadoo.mes.deliveries.constants;

public enum IncludeInCalculationDeliveries {

    CONFIRMED_DELIVERIES("01confirmedDeliveries"),
    UNCONFIRMED_DELIVERIES("02unconfirmedDeliveries"),
    NON_DRAFT_DELIVERIES("03nonDraftDeliveries");

    private final String mode;

    IncludeInCalculationDeliveries(final String mode) {
        this.mode = mode;
    }

    public String getStringValue() {
        return mode;
    }


}
