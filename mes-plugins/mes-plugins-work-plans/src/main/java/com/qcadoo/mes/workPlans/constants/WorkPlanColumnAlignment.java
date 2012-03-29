package com.qcadoo.mes.workPlans.constants;


public enum WorkPlanColumnAlignment {
    LEFT("01left"), RIGHT("02right");

    private String stringValue;

    private WorkPlanColumnAlignment(final String stringValue) {
        this.stringValue = stringValue;
    }

    public String getStringValue() {
        return stringValue;
    }

    public static WorkPlanColumnAlignment parseString(final String string) {
        if ("01left".equals(string)) {
            return LEFT;
        } else if ("02right".equals(string)) {
            return RIGHT;
        }

        throw new IllegalStateException("Unsupported column alignment: " + string);
    }
}
