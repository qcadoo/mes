package com.qcadoo.mes.workPlans.constants;

public enum WorkPlanType {
    ALL_OPERATIONS("01allOperations"), BY_END_PRODUCT("02byEndProduct"), BY_WORKSTATION_TYPE("03byWorkstationType"), BY_DIVISION(
            "04byDivision");

    private String stringValue;

    private WorkPlanType(final String stringValue) {
        this.stringValue = stringValue;
    }

    public String getStringValue() {
        return stringValue;
    }

    public static WorkPlanType parseString(final String string) {
        if ("01allOperations".equals(string)) {
            return ALL_OPERATIONS;
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
