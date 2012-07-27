package com.qcadoo.mes.assignmentToShift.constants;

public enum OccupationTypeEnumStringValue {

    WORK_ON_LINE("01workOnLine"), OTHER_CASE("02otherCase"), MIX("03mix"), SICKNESS("04sickness");

    private final String occupationTypeEnumStringValue;

    private OccupationTypeEnumStringValue(final String occupationTypeEnumStringValue) {
        this.occupationTypeEnumStringValue = occupationTypeEnumStringValue;
    }

    public String getStringValue() {
        return occupationTypeEnumStringValue;
    }

    public static OccupationTypeEnumStringValue parseString(final String string) {
        if ("01workOnLine".equals(string)) {
            return WORK_ON_LINE;
        } else if ("02otherCase".equals(string)) {
            return OTHER_CASE;
        } else if ("03mix".equals(string)) {
            return MIX;
        } else if ("04sickness".equals(string)) {
            return SICKNESS;
        }

        throw new IllegalStateException("Unsupported occupationTypeEnumStringValue: " + string);
    }
}
