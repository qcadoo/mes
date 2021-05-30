package com.qcadoo.mes.masterOrders.states.constants;

import com.google.common.base.Preconditions;
import com.qcadoo.mes.states.StateEnum;

public enum SalesPlanState implements StateEnum {

    DRAFT(SalesPlanStateStringValues.DRAFT) {

        @Override
        public boolean canChangeTo(final StateEnum targetState) {
            return REJECTED.equals(targetState) || COMPLETED.equals(targetState);
        }
    },

    REJECTED(SalesPlanStateStringValues.REJECTED) {

        @Override
        public boolean canChangeTo(final StateEnum targetState) {
            return false;
        }
    },

    COMPLETED(SalesPlanStateStringValues.COMPLETED) {

        @Override
        public boolean canChangeTo(final StateEnum targetState) {
            return false;
        }
    };

    private final String stringValue;

    SalesPlanState(final String stringValue) {
        this.stringValue = stringValue;
    }

    public static SalesPlanState parseString(final String string) {
        SalesPlanState parsedStatus = null;
        for (SalesPlanState status : SalesPlanState.values()) {
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
