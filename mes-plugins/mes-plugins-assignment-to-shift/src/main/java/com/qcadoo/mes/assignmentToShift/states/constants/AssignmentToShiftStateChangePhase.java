package com.qcadoo.mes.assignmentToShift.states.constants;

public class AssignmentToShiftStateChangePhase {

    public static final int PRE_VALIDATION = 1;

    public static final int DEFAULT = 3;

    public static final int LAST = DEFAULT + 1;

    private AssignmentToShiftStateChangePhase() {
    }

    public static int getNumOfPhases() {
        return LAST;
    }
}
