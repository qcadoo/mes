package com.qcadoo.mes.states;

import com.google.common.base.Preconditions;

public enum TestState implements StateEnum {

    DRAFT("01draft") {

        @Override
        public boolean canChangeTo(final StateEnum targetState) {
            return ACCEPTED.equals(targetState) || DECLINED.equals(targetState);
        }
    },
    ACCEPTED("02accepted") {

        @Override
        public boolean canChangeTo(final StateEnum targetState) {
            return DECLINED.equals(targetState);
        }
    },
    DECLINED("03declined") {

        @Override
        public boolean canChangeTo(final StateEnum targetState) {
            return false;
        }
    };

    private final String stringValue;

    private TestState(final String stringValue) {
        this.stringValue = stringValue;
    }

    public static final TestState parseString(final String stringValue) {
        TestState parsedStatus = null;
        for (TestState status : TestState.values()) {
            if (status.getStringValue().equals(stringValue)) {
                parsedStatus = status;
                break;
            }
        }
        Preconditions.checkArgument(parsedStatus != null, "Couldn't parse from string '" + stringValue + "'");
        return parsedStatus;
    }

    @Override
    public String getStringValue() {
        return stringValue;
    }

    @Override
    public abstract boolean canChangeTo(final StateEnum targetState);

}
