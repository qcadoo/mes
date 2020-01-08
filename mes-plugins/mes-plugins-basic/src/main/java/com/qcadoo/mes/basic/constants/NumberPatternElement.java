package com.qcadoo.mes.basic.constants;

public enum NumberPatternElement {

    DD("01dd"), MM("02mm"), RR("03rr"), RRRR("04rrrr"), N999("05999"), N9999("069999"), N99999("0799999"), XX("08xx");

    private final String value;

    NumberPatternElement(final String element) {
        this.value = element;
    }

    public String getStringValue() {
        return value;
    }
}
