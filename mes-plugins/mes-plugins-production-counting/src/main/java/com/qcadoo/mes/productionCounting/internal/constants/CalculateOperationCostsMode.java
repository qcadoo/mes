package com.qcadoo.mes.productionCounting.internal.constants;

public enum CalculateOperationCostsMode {
    HOURLY("01hourly"), PIECEWORK("02piecework");

    private String calculateOperationCostsMode;

    private CalculateOperationCostsMode(final String calculateOperationCostsMode) {
        this.calculateOperationCostsMode = calculateOperationCostsMode;
    }

    public String getStringValue() {
        return calculateOperationCostsMode;
    }

    public static CalculateOperationCostsMode parseString(final String string) {
        if ("01hourly".equals(string)) {
            return HOURLY;
        } else if ("02piecework".equals(string)) {
            return PIECEWORK;
        }

        throw new IllegalStateException("Unsupported calculateOperationCostMode: " + string);
    }
}
