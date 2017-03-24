package com.qcadoo.mes.productionCounting.constants;

public enum FinalProductAnalysisFor {
    BEFORE_ADDITIONAL_ACTIONS("01beforeAdditionalActions"), FINAL_PRODUCTS("02finalProducts");

    private final String finalProductAnalysisFor;

    private FinalProductAnalysisFor(final String finalProductAnalysisFor) {
        this.finalProductAnalysisFor = finalProductAnalysisFor;
    }

    public String getStringValue() {
        return finalProductAnalysisFor;
    }

    public static FinalProductAnalysisFor parseString(final String string) {
        if ("01beforeAdditionalActions".equals(string)) {
            return BEFORE_ADDITIONAL_ACTIONS;
        } else if ("02finalProducts".equals(string)) {
            return FINAL_PRODUCTS;
        }

        throw new IllegalStateException("Unsupported typeOfProductionRecording: " + string);
    }
}
