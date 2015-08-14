package com.qcadoo.mes.orders.print;

public class HeaderPair {

    private String label;

    private String value;

    public HeaderPair(final String label, final String value) {
        this.label = label;
        this.value = value;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}