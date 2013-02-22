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

    public static MasterOrderType parseString(final String masterOrderType) {
        if ("01undefined".equals(masterOrderType)) {
            return UNDEFINED;
        } else if ("02oneProduct".equals(masterOrderType)) {
            return ONE_PRODUCT;
        } else if ("03manyProducts".equals(masterOrderType)) {
            return MANY_PRODUCTS;
        }

        throw new IllegalStateException("Unsupported masterOrderType: " + masterOrderType);
    }

}
