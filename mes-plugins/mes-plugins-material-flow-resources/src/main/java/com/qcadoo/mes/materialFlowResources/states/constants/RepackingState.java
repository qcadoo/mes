package com.qcadoo.mes.materialFlowResources.states.constants;

import com.google.common.base.Preconditions;
import com.qcadoo.mes.states.StateEnum;

public enum RepackingState implements StateEnum {

    DRAFT(RepackingStateStringValues.DRAFT) {

        @Override
        public boolean canChangeTo(final StateEnum targetState) {
            return ACCEPTED.equals(targetState) || REJECTED.equals(targetState);
        }
    },

    ACCEPTED(RepackingStateStringValues.ACCEPTED) {

        @Override
        public boolean canChangeTo(final StateEnum targetState) {
            return false;
        }
    },

    REJECTED(RepackingStateStringValues.REJECTED) {

        @Override
        public boolean canChangeTo(final StateEnum targetState) {
            return false;
        }
    };

    private final String stringValue;

    RepackingState(final String stringValue) {
        this.stringValue = stringValue;
    }

    @Override
    public abstract boolean canChangeTo(final StateEnum targetState);

    public static RepackingState parseString(final String string) {
        RepackingState parsedStatus = null;
        for (RepackingState status : RepackingState.values()) {
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

}
