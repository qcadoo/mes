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

    public static NumberPatternElement parseString(final String numberPatternElement) {
        if ("01dd".equals(numberPatternElement)) {
            return DD;
        } else if ("02mm".equals(numberPatternElement)) {
            return MM;
        } else if ("03rr".equals(numberPatternElement)) {
            return RR;
        } else if ("04rrrr".equals(numberPatternElement)) {
            return RRRR;
        } else if ("05999".equals(numberPatternElement)) {
            return N999;
        } else if ("069999".equals(numberPatternElement)) {
            return N9999;
        } else if ("0799999".equals(numberPatternElement)) {
            return N99999;
        } else if ("08xx".equals(numberPatternElement)) {
            return XX;
        }

        throw new IllegalStateException("Unsupported NumberPatternElement: " + numberPatternElement);
    }
}
