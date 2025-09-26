package com.qcadoo.mes.materialFlowResources.states.constants;

import com.google.common.base.Preconditions;
import com.qcadoo.mes.states.StateEnum;


public enum StocktakingState implements StateEnum {

    DRAFT(StocktakingStateStringValues.DRAFT) {

        @Override
        public boolean canChangeTo(final StateEnum targetState) {
            return IN_PROGRESS.equals(targetState) || REJECTED.equals(targetState);
        }
    },

    IN_PROGRESS(StocktakingStateStringValues.IN_PROGRESS) {

        @Override
        public boolean canChangeTo(final StateEnum targetState) {
            return FINALIZED.equals(targetState) || REJECTED.equals(targetState);
        }
    },

    FINALIZED(StocktakingStateStringValues.FINALIZED) {

        @Override
        public boolean canChangeTo(final StateEnum targetState) {
            return FINISHED.equals(targetState) || REJECTED.equals(targetState);
        }
    },
    FINISHED(StocktakingStateStringValues.FINISHED) {

        @Override
        public boolean canChangeTo(final StateEnum targetState) {
            return false;
        }
    },

    REJECTED(StocktakingStateStringValues.REJECTED) {

        @Override
        public boolean canChangeTo(final StateEnum targetState) {
            return false;
        }
    };

    private final String stringValue;

    StocktakingState(final String stringValue) {
        this.stringValue = stringValue;
    }

    @Override
    public abstract boolean canChangeTo(final StateEnum targetState);

    public static StocktakingState parseString(final String string) {
        StocktakingState parsedStatus = null;
        for (StocktakingState status : StocktakingState.values()) {
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
