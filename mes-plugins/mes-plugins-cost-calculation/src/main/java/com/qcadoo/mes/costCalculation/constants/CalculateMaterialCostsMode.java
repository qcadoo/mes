package com.qcadoo.mes.costCalculation.constants;

public enum CalculateMaterialCostsMode {
    NOMINAL("01nominal"), AVERAGE("02average"), LAST_PURCHASE("03lastPurchase"), COST_FOR_ORDER("04costForOrder");

    private final String calculateMaterialCostsMode;

    private CalculateMaterialCostsMode(final String calculateMaterialCostsMode) {
        this.calculateMaterialCostsMode = calculateMaterialCostsMode;
    }

    public String getStringValue() {
        return calculateMaterialCostsMode;
    }

    public static CalculateMaterialCostsMode parseString(final String string) {
        if ("01nominal".equals(string)) {
            return NOMINAL;
        } else if ("02average".equals(string)) {
            return AVERAGE;
        } else if ("03lastPurchase".equals(string)) {
            return LAST_PURCHASE;
        } else if ("04costForOrder".equals(string)) {
            return COST_FOR_ORDER;
        }

        throw new IllegalStateException("Unsupported calculateMaterialCostsMode: " + string);
    }

}
