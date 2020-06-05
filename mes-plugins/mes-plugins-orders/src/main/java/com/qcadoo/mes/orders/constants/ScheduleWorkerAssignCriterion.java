package com.qcadoo.mes.orders.constants;

public enum ScheduleWorkerAssignCriterion {

    WORKSTATION_LAST_OPERATOR_LATEST_FINISHED(
            "01workstationLastOperatorLatestFinished"), WORKSTATION_LAST_OPERATOR_EARLIEST_FINISHED(
                    "02workstationLastOperatorEarliestFinished"), WORKSTATION_DEFAULT_OPERATOR("03workstationDefaultOperator");

    private final String workerAssignCriterion;

    ScheduleWorkerAssignCriterion(final String workerAssignCriterion) {
        this.workerAssignCriterion = workerAssignCriterion;
    }

    public String getStringValue() {
        return workerAssignCriterion;
    }
}
