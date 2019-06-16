package com.qcadoo.mes.orders.states.constants;

import com.google.common.base.Preconditions;
import com.qcadoo.mes.states.StateEnum;

public enum OperationalTaskState implements StateEnum {

    PENDING(OperationalTaskStateStringValues.PENDING) {

        @Override
        public boolean canChangeTo(final StateEnum targetState) {
            return STARTED.equals(targetState) || REJECTED.equals(targetState);
        }
    },

    STARTED(OperationalTaskStateStringValues.STARTED) {

        @Override
        public boolean canChangeTo(final StateEnum targetState) {
            return FINISHED.equals(targetState) || REJECTED.equals(targetState);

        }
    },

    FINISHED(OperationalTaskStateStringValues.FINISHED) {

        @Override
        public boolean canChangeTo(final StateEnum targetState) {
            return false;
        }
    },

    REJECTED(OperationalTaskStateStringValues.REJECTED) {

        @Override
        public boolean canChangeTo(final StateEnum targetState) {
            return false;
        }
    };

    private final String stringValue;

    OperationalTaskState(final String stringValue) {
        this.stringValue = stringValue;
    }

    public static OperationalTaskState parseString(final String string) {
        OperationalTaskState parsedStatus = null;
        for (OperationalTaskState status : OperationalTaskState.values()) {
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
