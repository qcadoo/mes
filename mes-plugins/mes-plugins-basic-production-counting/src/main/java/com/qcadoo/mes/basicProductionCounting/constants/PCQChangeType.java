package com.qcadoo.mes.basicProductionCounting.constants;

public enum PCQChangeType {

    ADDING_PRODUCT("01addingProduct"), PRODUCT_REMOVAL("02productRemoval"), CHANGE_QUANTITY("03changeQuantity");

    private final String code;

    private PCQChangeType(final String code) {
        this.code = code;
    }

    public String getStringValue() {
        return code;
    }

    public static PCQChangeType parseString(final String string) {
        for (PCQChangeType role : values()) {
            if (role.getStringValue().equals(string)) {
                return role;
            }
        }
        throw new IllegalStateException("Unsupported PCQChangeType: " + string);
    }
}
