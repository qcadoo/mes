package com.qcadoo.mes.orders.constants;

public enum ScheduleWorkstationAssignCriterion {
    SHORTEST_TIME("01shortestTime"), LEAST_WORKSTATIONS("02leastWorkstations");

    private final String workstationAssignCriterion;

    ScheduleWorkstationAssignCriterion(final String workstationAssignCriterion) {
        this.workstationAssignCriterion = workstationAssignCriterion;
    }

    public String getStringValue() {
        return workstationAssignCriterion;
    }
}
