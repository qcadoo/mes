package com.qcadoo.mes.orders.states.constants;

import com.google.common.base.Preconditions;
import com.qcadoo.mes.states.StateEnum;

public enum OrderPackState implements StateEnum {

    PENDING(OrderPackStateStringValues.PENDING) {

        @Override
        public boolean canChangeTo(final StateEnum targetState) {
            return DURING_PRODUCTION.equals(targetState);
        }
    },

    DURING_PRODUCTION(OrderPackStateStringValues.DURING_PRODUCTION) {

        @Override
        public boolean canChangeTo(final StateEnum targetState) {
            return FINISHED_PRODUCTION.equals(targetState);

        }
    },

    FINISHED_PRODUCTION(OrderPackStateStringValues.FINISHED_PRODUCTION) {

        @Override
        public boolean canChangeTo(final StateEnum targetState) {
            return false;
        }
    };

    private final String stringValue;

    OrderPackState(final String stringValue) {
        this.stringValue = stringValue;
    }

    public static OrderPackState parseString(final String string) {
        OrderPackState parsedStatus = null;
        for (OrderPackState status : OrderPackState.values()) {
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
