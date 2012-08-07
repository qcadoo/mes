package com.qcadoo.mes.assignmentToShift.constants;

public enum StaffAssignmentToShiftState {

    SIMPLE("01simple"), ACCEPTED("02accepted"), CORRECTED("03corrected");

    private final String state;

    private StaffAssignmentToShiftState(final String state) {
        this.state = state;
    }

    public String getStringValue() {
        return state;
    }

    public static StaffAssignmentToShiftState parseString(final String string) {
        if ("01simple".equals(string)) {
            return SIMPLE;
        } else if ("02accepted".equals(string)) {
            return ACCEPTED;
        } else if ("03corrected".equals(string)) {
            return CORRECTED;
        }

        throw new IllegalStateException("Unsupported StaffAssignmentToShiftState: " + string);
    }

}
