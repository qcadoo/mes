package com.qcadoo.mes.costNormsForOperation.constants;

public enum CalculateOperationCostMode {
    HOURLY("01hourly"), PIECEWORK("02piecework");

    private String stringValue;

    private CalculateOperationCostMode(final String stringValue) {
        this.stringValue = stringValue;
    }

    public String getStringValue() {
        return stringValue;
    }

    public static CalculateOperationCostMode parseString(final String string) {
        if ("01hourly".equals(string)) {
            return HOURLY;
        } else if ("02piecework".equals(string)) {
            return PIECEWORK;
        }

        throw new IllegalStateException("Unsupported calculateOperationCostMode: " + string);
    }
}
