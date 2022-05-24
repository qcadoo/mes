package com.qcadoo.mes.orders.constants;

public enum ProductionLineScheduleSortOrder {
    LONGEST_ORDERS("01longestOrders"), SHORTEST_ORDERS("02shortestOrders"), IMPORTANT_CLIENTS("03importantClients"),
    GREATEST_ORDERED_QUANTITY("04greatestOrderedQuantity"), SMALLEST_ORDERED_QUANTITY("05smallestOrderedQuantity"),
    EARLIEST_DEADLINE("06earliestDeadline");

    private final String sortOrder;

    ProductionLineScheduleSortOrder(final String sortOrder) {
        this.sortOrder = sortOrder;
    }

    public String getStringValue() {
        return sortOrder;
    }
}
