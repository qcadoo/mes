package com.qcadoo.mes.orders.constants;

public enum ScheduleSortOrder {
    DESCENDING("01desc"), ASCENDING("02asc");

    private final String sortOrder;

    ScheduleSortOrder(final String sortOrder) {
        this.sortOrder = sortOrder;
    }

    public String getStringValue() {
        return sortOrder;
    }
}
