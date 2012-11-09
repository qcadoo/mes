package com.qcadoo.mes.materialRequirements.internal;

import com.google.common.base.Preconditions;

public enum InputProductsRequiredForType {
    START_ORDER("01startOrder"), START_OPERATIONAL_TASK("02startOperationalTask");

    private final String stringValue;

    private InputProductsRequiredForType(final String state) {
        this.stringValue = state;
    }

    public String getStringValue() {
        return stringValue;
    }

    public static InputProductsRequiredForType parseString(final String string) {
        InputProductsRequiredForType parsedStatus = null;
        for (InputProductsRequiredForType status : InputProductsRequiredForType.values()) {
            if (status.getStringValue().equals(string)) {
                parsedStatus = status;
                break;
            }
        }
        Preconditions.checkArgument(parsedStatus != null, "Couldn't parse InputProductsRequiredForType from string '" + string
                + "'");
        return parsedStatus;
    }
}
