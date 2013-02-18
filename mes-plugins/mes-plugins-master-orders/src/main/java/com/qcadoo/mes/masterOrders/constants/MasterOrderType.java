package com.qcadoo.mes.masterOrders.constants;

public enum MasterOrderType {

    UNDEFINED("01undefined"), ONE_PRODUCT("02oneProduct"), MANY_PRODUCTS("03manyProducts");

    private final String masterOrderType;

    private MasterOrderType(final String masterOrderType) {
        this.masterOrderType = masterOrderType;
    }

    public String getStringValue() {
        return masterOrderType;
    }

    public static MasterOrderType parseString(final String string) {
        if ("01undefined".equals(string)) {
            return UNDEFINED;
        } else if ("02oneProduct".equals(string)) {
            return ONE_PRODUCT;
        } else if ("03manyProducts".equals(string)) {
            return MANY_PRODUCTS;
        }

        throw new IllegalStateException("Unsupported masterOrderType: " + string);
    }
}
