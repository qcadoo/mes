package com.qcadoo.mes.core.data.search;

public final class Order {

    private static final Order DEFAULT_ORDER = Order.asc("id");

    private final String fieldName;

    private final boolean asc;

    private Order(final String fieldName, final boolean asc) {
        this.fieldName = fieldName;
        this.asc = asc;
    }

    public static Order asc(final String fieldName) {
        return new Order(fieldName, true);
    }

    public static Order desc(final String fieldName) {
        return new Order(fieldName, false);
    }

    public boolean isAsc() {
        return asc;
    }

    public boolean isDesc() {
        return !asc;
    }

    public String getFieldName() {
        return fieldName;
    }

    public static Order asc() {
        return DEFAULT_ORDER;
    }

}
