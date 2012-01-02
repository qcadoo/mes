package com.qcadoo.mes.workPlans.constants;

public enum WorkPlanType {
    NO_DISTINCTION("01noDistinction"), BY_END_PRODUCT("02byEndProduct"), BY_WORKSTATION_TYPE("03byWorkstationType"), BY_DIVISION(
            "04byDivision");

    private String stringValue;

    private WorkPlanType(final String stringValue) {
        this.stringValue = stringValue;
    }

    public String getStringValue() {
        return stringValue;
    }

    public static WorkPlanType parseString(final String string) {
        if ("01noDistinction".equals(string)) {
            return NO_DISTINCTION;
        } else if ("02byEndProduct".equals(string)) {
            return BY_END_PRODUCT;
        } else if ("03byWorkstationType".equals(string)) {
            return BY_WORKSTATION_TYPE;
        } else if ("04byDivision".equals(string)) {
            return BY_DIVISION;
        }

        throw new IllegalStateException("Unsuported workPlan type: " + string);
    }
}
