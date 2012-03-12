package com.qcadoo.mes.productionCounting.internal.constants;

public enum TypeOfProductionRecording {
    BASIC("01basic"), CUMULATED("02cumulated"), FOR_EACH("03forEach");

    private final String typeOfProductionRecording;

    private TypeOfProductionRecording(final String typeOfProductionRecording) {
        this.typeOfProductionRecording = typeOfProductionRecording;
    }

    public String getStringValue() {
        return typeOfProductionRecording;
    }

    public static TypeOfProductionRecording parseString(final String string) {
        if ("01basic".equals(string)) {
            return BASIC;
        } else if ("02cumulated".equals(string)) {
            return CUMULATED;
        } else if ("03forEach".equals(string)) {
            return FOR_EACH;
        }

        throw new IllegalStateException("Unsupported typeOfProductionRecording: " + string);
    }
}
