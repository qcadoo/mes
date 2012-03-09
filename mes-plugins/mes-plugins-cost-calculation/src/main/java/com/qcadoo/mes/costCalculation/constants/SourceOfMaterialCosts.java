package com.qcadoo.mes.costCalculation.constants;

public enum SourceOfMaterialCosts {
    CURRENT_GLOBAL_DEFINITIONS_IN_PRODUCT("01currentGlobalDefinitionsInProduct"), FROM_ORDERS_MATERIAL_COSTS(
            "02fromOrdersMaterialCosts");

    private String sourceOfMaterialCosts;

    private SourceOfMaterialCosts(final String sourceOfMaterialCosts) {
        this.sourceOfMaterialCosts = sourceOfMaterialCosts;
    }

    public String getStringValue() {
        return sourceOfMaterialCosts;
    }

    public static SourceOfMaterialCosts parseString(final String string) {
        if ("01currentGlobalDefinitionsInProduct".equals(string)) {
            return CURRENT_GLOBAL_DEFINITIONS_IN_PRODUCT;
        } else if ("02fromOrdersMaterialCosts".equals(string)) {
            return FROM_ORDERS_MATERIAL_COSTS;
        }

        throw new IllegalStateException("Unsupported sourceOfMaterialCosts: " + string);
    }
}
