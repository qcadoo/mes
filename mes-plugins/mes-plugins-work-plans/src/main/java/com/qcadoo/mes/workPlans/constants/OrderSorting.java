package com.qcadoo.mes.workPlans.constants;

public enum OrderSorting {
    ASC("01asc"), DESC("02desc");

    private String type;

    private OrderSorting(final String type) {
        this.type = type;
    }

    public String getStringValue() {
        return type;
    }

    public static OrderSorting parseString(final String type) {
        if ("01asc".equals(type)) {
            return ASC;
        } else if ("02desc".equals(type)) {
            return DESC;
        }

        throw new IllegalStateException("Unsupported orderSorting type '" + type + "'");
    }

}
