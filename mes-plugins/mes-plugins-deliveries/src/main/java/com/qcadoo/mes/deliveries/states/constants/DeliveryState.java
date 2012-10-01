package com.qcadoo.mes.deliveries.states.constants;

import com.google.common.base.Preconditions;
import com.qcadoo.mes.states.StateEnum;

public enum DeliveryState implements StateEnum {

    DRAFT(DeliveryStateStringValues.DRAFT) {

        @Override
        public boolean canChangeTo(final StateEnum targetState) {
            return PREPARED.equals(targetState) || APPROVED.equals(targetState) || DECLINED.equals(targetState);
        }
    },
    PREPARED(DeliveryStateStringValues.PREPARED) {

        @Override
        public boolean canChangeTo(final StateEnum targetState) {
            return DURING_CORRECTION.equals(targetState) || APPROVED.equals(targetState) || DECLINED.equals(targetState);
        }
    },
    DURING_CORRECTION(DeliveryStateStringValues.DURING_CORRECTION) {

        @Override
        public boolean canChangeTo(final StateEnum targetState) {
            return APPROVED.equals(targetState) || DECLINED.equals(targetState);
        }
    },
    DECLINED(DeliveryStateStringValues.DECLINED) {

        @Override
        public boolean canChangeTo(final StateEnum targetState) {
            return false;
        }
    },
    APPROVED(DeliveryStateStringValues.APPROVED) {

        @Override
        public boolean canChangeTo(final StateEnum targetState) {
            return RECEIVED.equals(targetState);
        }
    },
    RECEIVED(DeliveryStateStringValues.RECEIVED) {

        @Override
        public boolean canChangeTo(final StateEnum targetState) {
            return false;
        }
    };

    private final String stringValue;

    private DeliveryState(final String state) {
        this.stringValue = state;
    }

    @Override
    public String getStringValue() {
        return stringValue;
    }

    public static DeliveryState parseString(final String string) {
        DeliveryState parsedStatus = null;
        for (DeliveryState status : DeliveryState.values()) {
            if (status.getStringValue().equals(string)) {
                parsedStatus = status;
                break;
            }
        }
        Preconditions.checkArgument(parsedStatus != null, "Couldn't parse DeliveryState from string '" + string + "'");
        return parsedStatus;
    }

    public abstract boolean canChangeTo(final StateEnum targetState);

}
