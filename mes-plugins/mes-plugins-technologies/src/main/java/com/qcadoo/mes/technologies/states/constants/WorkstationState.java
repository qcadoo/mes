package com.qcadoo.mes.technologies.states.constants;

import com.google.common.base.Preconditions;
import com.qcadoo.mes.basic.states.constants.WorkstationStateStringValues;
import com.qcadoo.mes.states.StateEnum;

public enum WorkstationState implements StateEnum {

    STOPPED(WorkstationStateStringValues.STOPPED) {

        @Override
        public boolean canChangeTo(final StateEnum targetState) {
            return LAUNCHED.equals(targetState);
        }
    },

    LAUNCHED(WorkstationStateStringValues.LAUNCHED) {

        @Override
        public boolean canChangeTo(final StateEnum targetState) {
            return STOPPED.equals(targetState);
        }
    };

    private final String stringValue;

    WorkstationState(final String stringValue) {
        this.stringValue = stringValue;
    }

    public static WorkstationState parseString(final String string) {
        WorkstationState parsedStatus = null;
        for (WorkstationState status : WorkstationState.values()) {
            if (status.getStringValue().equals(string)) {
                parsedStatus = status;
                break;
            }
        }
        Preconditions.checkArgument(parsedStatus != null, "Couldn't parse '" + string + "'");
        return parsedStatus;
    }

    @Override
    public String getStringValue() {
        return stringValue;
    }

    @Override
    public boolean canChangeTo(StateEnum targetState) {
        return false;
    }
}
