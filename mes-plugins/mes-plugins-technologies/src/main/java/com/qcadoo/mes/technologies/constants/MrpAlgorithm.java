package com.qcadoo.mes.technologies.constants;

public enum MrpAlgorithm {

    ONLY_COMPONENTS("01onlyComponents"), ALL_PRODUCTS_IN("02allProductsIn"), COMPONENTS_AND_SUBCONTRACTORS_PRODUCTS(
            "03componentsAndSubcontractorsProducts");

    private final String typeOfProductionRecording;

    private MrpAlgorithm(final String algoritmOfMaterialRequirements) {
        this.typeOfProductionRecording = algoritmOfMaterialRequirements;
    }

    public String getStringValue() {
        return typeOfProductionRecording;
    }

    public static MrpAlgorithm parseString(final String string) {
        if ("01onlyComponents".equals(string)) {
            return ONLY_COMPONENTS;
        } else if ("02allProductsIn".equals(string)) {
            return ALL_PRODUCTS_IN;
        } else if ("03componentsAndSubcontractorsProducts".equals(string)) {
            return COMPONENTS_AND_SUBCONTRACTORS_PRODUCTS;
        }

        throw new IllegalStateException("Unsupported mrpAlgorithm attribute: " + string);
    }

}
