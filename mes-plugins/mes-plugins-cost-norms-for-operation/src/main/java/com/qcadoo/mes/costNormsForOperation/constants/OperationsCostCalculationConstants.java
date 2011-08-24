package com.qcadoo.mes.costNormsForOperation.constants;

public enum OperationsCostCalculationConstants {
    PIECEWORK("piecework"), HOURLY("hourly");

    private final String strValue;

    private OperationsCostCalculationConstants(final String strValue) {
        this.strValue = strValue;
    }

    public String getStrValue() {
        return strValue;
    }
}
