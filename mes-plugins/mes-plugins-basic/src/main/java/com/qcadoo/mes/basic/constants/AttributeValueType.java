package com.qcadoo.mes.basic.constants;

public enum AttributeValueType {

    TEXT("01text"), NUMERIC("02numeric");

    private final String value;

    private AttributeValueType(final String type) {
        this.value = type;
    }

    public String getStringValue() {
        return value;
    }

}
