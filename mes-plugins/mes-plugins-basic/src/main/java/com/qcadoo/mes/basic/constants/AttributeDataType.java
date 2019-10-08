package com.qcadoo.mes.basic.constants;

public enum AttributeDataType {

    CALCULATED("01calculated"), CONTINUOUS("02continuous");

    private final String value;

    private AttributeDataType(final String type) {
        this.value = type;
    }

    public String getStringValue() {
        return value;
    }
}
