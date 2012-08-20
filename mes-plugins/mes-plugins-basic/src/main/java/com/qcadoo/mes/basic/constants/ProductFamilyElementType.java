package com.qcadoo.mes.basic.constants;

public enum ProductFamilyElementType {
    PARTICULAR_PRODUCT("01particularProduct"), PRODUCTS_FAMILY("02productsFamily");

    private final String type;

    private ProductFamilyElementType(final String type) {
        this.type = type;
    }

    public String getStringValue() {
        return type;
    }

    public static ProductFamilyElementType parseString(final String string) {
        if ("01particularProduct".equals(string)) {
            return PARTICULAR_PRODUCT;
        } else if ("02productsFamily".equals(string)) {
            return PRODUCTS_FAMILY;
        }

        throw new IllegalStateException("Unsupported ElementHierarchyInFamilyEnumStringValue: " + string);
    }
}
