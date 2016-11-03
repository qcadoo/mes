package com.qcadoo.mes.productionPerShift.constants;

public enum PpsAlgorithm {

    STANDARD_TECHNOLOGY("01standardTechnology", "AutomaticPpsTechNormService"),
    STANDARD_TECHNOLOGY_AND_AMOUNT_OF_CHANGE("02standardTechnologyAndAmountOfChange", "AutomaticPpsTechNormAndWorkersService"),
    USER("03user", "");

    private final String ppsAlgorithm;
    private final String algorithmClass;

    private PpsAlgorithm(final String ppsAlgorithm, final String algorithmClass) {
        this.ppsAlgorithm = ppsAlgorithm;
        this.algorithmClass = algorithmClass;
    }

    public String getStringValue() {
        return ppsAlgorithm;
    }

    public String getAlgorithmClass() {
        return algorithmClass;
    }

    public static PpsAlgorithm fromStringValue(String code) {
        for (PpsAlgorithm ppsAlgorithm : values()) {
            if (ppsAlgorithm.getStringValue().equals(code)) {
                return ppsAlgorithm;
            }
        }

        throw new IllegalArgumentException(String.format("Unknown code: '%s' from enum: 'ppsAlgorithm'", code));
    }

}
