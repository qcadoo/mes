package com.qcadoo.mes.assignmentToShift.constants;

public enum OccupationType {

    WORK_ON_LINE("01workOnLine"), MIX("02mix"), SICKNESS("03sickness"), OTHER_CASE("04otherCase");

    private final String type;

    private OccupationType(final String type) {
        this.type = type;
    }

    public String getStringValue() {
        return type;
    }

    public static OccupationType parseString(final String string) {
        if ("01workOnLine".equals(string)) {
            return WORK_ON_LINE;
        } else if ("02mix".equals(string)) {
            return MIX;
        } else if ("03sickness".equals(string)) {
            return SICKNESS;
        } else if ("04otherCase".equals(string)) {
            return OTHER_CASE;
        }

        throw new IllegalStateException("Unsupported OccupationTypeEnumStringValue: " + string);
    }

}
