package com.qcadoo.mes.materialRequirements.internal.constants;

public enum InputProductsRequiredForType {
    START_ORDER("01startOrder");

    private final String inputProductsRequiredForType;

    private InputProductsRequiredForType(final String inputProductsRequiredForType) {
        this.inputProductsRequiredForType = inputProductsRequiredForType;
    }

    public String getStringValue() {
        return inputProductsRequiredForType;
    }

    public static InputProductsRequiredForType parseString(final String string) {
        if ("01startOrder".equals(string)) {
            return START_ORDER;
        }

        throw new IllegalStateException("Unsupported inputProductsRequiredForType: " + string);
    }

}
