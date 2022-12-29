package com.qcadoo.mes.orderSupplies.constants;

public enum IncludeInCalculationDeliveries {

    CONFIRMED_DELIVERIES("01confirmedDeliveries"),
    UNCONFIRMED_DELIVERIES("02unconfirmedDeliveries"),
    NON_DRAFT_DELIVERIES("03nonDraftDeliveries");

    private final String mode;

    private IncludeInCalculationDeliveries(final String mode) {
        this.mode = mode;
    }

    public String getStringValue() {
        return mode;
    }


}
