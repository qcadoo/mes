package com.qcadoo.mes.productionCounting.internal.constants;

public enum TypeOfProductionRecording {
    BASIC("01basic"), CUMULATED("02cumulated"), FOR_EACH("03forEach");

    private final String type;

    private TypeOfProductionRecording(final String type) {
        this.type = type;
    }

    public String getStringValue() {
        return type;
    }
}
