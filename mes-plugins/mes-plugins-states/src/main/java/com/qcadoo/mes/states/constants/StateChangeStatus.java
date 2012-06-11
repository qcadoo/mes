package com.qcadoo.mes.states.constants;

import com.google.common.base.Preconditions;

public enum StateChangeStatus {

    IN_PROGRESS("01inProgress"), PAUSED("02paused"), SUCCESSFUL("03successful"), FAILURE("04failure");

    private final String stringValue;

    public boolean canContinue() {
        return this.equals(IN_PROGRESS);
    }

    private StateChangeStatus(final String stringValue) {
        this.stringValue = stringValue;
    }

    public static StateChangeStatus parseString(final String string) {
        StateChangeStatus parsedStatus = null;
        for (StateChangeStatus status : StateChangeStatus.values()) {
            if (status.getStringValue().equals(string)) {
                parsedStatus = status;
                break;
            }
        }
        Preconditions.checkArgument(parsedStatus != null, "Couldn't parse StateChangeStatus from string '" + string + "'");
        return parsedStatus;
    }

    public String getStringValue() {
        return stringValue;
    }

    @Override
    public String toString() {
        return stringValue;
    }
}
