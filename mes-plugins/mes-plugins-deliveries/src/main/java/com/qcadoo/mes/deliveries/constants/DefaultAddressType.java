package com.qcadoo.mes.deliveries.constants;

public enum DefaultAddressType {

    COMPANY_ADDRESS("01companyAddress"), OTHER("02other");

    private final String state;

    private DefaultAddressType(final String state) {
        this.state = state;
    }

    public String getStringValue() {
        return state;
    }

}
