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

}
