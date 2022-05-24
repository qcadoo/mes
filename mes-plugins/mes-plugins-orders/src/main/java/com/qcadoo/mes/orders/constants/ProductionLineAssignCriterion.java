package com.qcadoo.mes.orders.constants;

public enum ProductionLineAssignCriterion {
    LEAST_PRODUCTION_LINES("01leastProductionLines"), SHORTEST_TIME("02shortestTime"), LEAST_CHANGEOVERS("03leastChangeovers");

    private final String assignCriterion;

    ProductionLineAssignCriterion(final String assignCriterion) {
        this.assignCriterion = assignCriterion;
    }

    public String getStringValue() {
        return assignCriterion;
    }
}
