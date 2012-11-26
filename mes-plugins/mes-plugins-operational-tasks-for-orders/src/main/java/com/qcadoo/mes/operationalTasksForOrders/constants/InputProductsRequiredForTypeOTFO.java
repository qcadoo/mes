package com.qcadoo.mes.operationalTasksForOrders.constants;

public enum InputProductsRequiredForTypeOTFO {
    START_OPERATIONAL_TASK("02startOperationalTask");

    private final String inputProductsRequiredForType;

    private InputProductsRequiredForTypeOTFO(final String inputProductsRequiredForType) {
        this.inputProductsRequiredForType = inputProductsRequiredForType;
    }

    public String getStringValue() {
        return inputProductsRequiredForType;
    }

    public static InputProductsRequiredForTypeOTFO parseString(final String string) {
        if ("02startOperationalTask".equals(string)) {
            return START_OPERATIONAL_TASK;
        }

        throw new IllegalStateException("Unsupported inputProductsRequiredForType: " + string);
    }

}
