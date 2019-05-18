package com.qcadoo.mes.orders.states.constants;

import com.google.common.base.Preconditions;
import com.qcadoo.mes.states.StateEnum;

public enum ScheduleState implements StateEnum {

    DRAFT(ScheduleStateStringValues.DRAFT) {

        @Override
        public boolean canChangeTo(final StateEnum targetState) {
            return APPROVED.equals(targetState) || REJECTED.equals(targetState);
        }
    },

    APPROVED(ScheduleStateStringValues.APPROVED) {

        @Override
        public boolean canChangeTo(final StateEnum targetState) {
            return REJECTED.equals(targetState);
        }
    },

    REJECTED(ScheduleStateStringValues.REJECTED) {

        @Override
        public boolean canChangeTo(final StateEnum targetState) {
            return false;
        }
    };

    private final String stringValue;

    ScheduleState(final String stringValue) {
        this.stringValue = stringValue;
    }

    public static ScheduleState parseString(final String string) {
        ScheduleState parsedStatus = null;
        for (ScheduleState status : ScheduleState.values()) {
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
