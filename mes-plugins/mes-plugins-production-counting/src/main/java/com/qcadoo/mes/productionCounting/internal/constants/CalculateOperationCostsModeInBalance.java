package com.qcadoo.mes.productionCounting.internal.constants;

public enum CalculateOperationCostsModeInBalance {
    HOURLY("01hourly"), PIECEWORK("02piecework");

    private String stringValue;

    private CalculateOperationCostsModeInBalance(final String stringValue) {
        this.stringValue = stringValue;
    }

    public String getStringValue() {
        return stringValue;
    }

    public static CalculateOperationCostsModeInBalance parseString(final String string) {
        if ("01hourly".equals(string)) {
            return HOURLY;
        } else if ("02piecework".equals(string)) {
            return PIECEWORK;
        }

        throw new IllegalStateException("Unsupported calculateOperationCostMode: " + string);
    }
}
