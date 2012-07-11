package com.qcadoo.mes.assignmentToShift.states.constants;

import com.google.common.base.Preconditions;
import com.qcadoo.mes.states.StateEnum;

public enum AssignmentToShiftState implements StateEnum {

    DRAFT(AssignmentToShiftStateStringValues.DRAFT) {

        @Override
        public boolean canChangeTo(final StateEnum targetState) {
            return ACCEPTED.equals(targetState);
        }
    },
    ACCEPTED(AssignmentToShiftStateStringValues.ACCEPTED) {

        @Override
        public boolean canChangeTo(final StateEnum targetState) {
            return DURING_CORRECTION.equals(targetState);
        }
    },
    DURING_CORRECTION(AssignmentToShiftStateStringValues.DURING_CORRECTION) {

        @Override
        public boolean canChangeTo(final StateEnum targetState) {
            return CORRECTED.equals(targetState);
        }
    },
    CORRECTED(AssignmentToShiftStateStringValues.CORRECTED) {

        @Override
        public boolean canChangeTo(final StateEnum targetState) {
            return DURING_CORRECTION.equals(targetState);
        }
    };

    private final String stringValue;

    private AssignmentToShiftState(final String state) {
        this.stringValue = state;
    }

    @Override
    public String getStringValue() {
        return stringValue;
    }

    public static AssignmentToShiftState parseString(final String string) {
        AssignmentToShiftState parsedStatus = null;
        for (AssignmentToShiftState status : AssignmentToShiftState.values()) {
            if (status.getStringValue().equals(string)) {
                parsedStatus = status;
                break;
            }
        }
        Preconditions.checkArgument(parsedStatus != null, "Couldn't parse AssignmentToShiftState from string '" + string + "'");
        return parsedStatus;
    }

    public abstract boolean canChangeTo(final StateEnum targetState);
}
