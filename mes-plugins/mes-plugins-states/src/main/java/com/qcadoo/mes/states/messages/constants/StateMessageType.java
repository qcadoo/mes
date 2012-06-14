package com.qcadoo.mes.states.messages.constants;

import com.google.common.base.Preconditions;

public enum StateMessageType {

    SUCCESS("01success"), INFO("02info"), FAILURE("03failure"), VALIDATION_ERROR("04validationError");

    private final String stringValue;

    private StateMessageType(final String stringValue) {
        this.stringValue = stringValue;
    }

    public String getStringValue() {
        return this.stringValue;
    }

    public static StateMessageType parseString(final String string) {
        StateMessageType parsedValue = null;
        for (StateMessageType status : StateMessageType.values()) {
            if (status.getStringValue().equals(string)) {
                parsedValue = status;
                break;
            }
        }
        Preconditions.checkArgument(parsedValue != null, "Couldn't parse string '" + string + "'");
        return parsedValue;
    }

    @Override
    public String toString() {
        return stringValue;
    }

}
