package com.qcadoo.mes.productionPerShift.constants;

public enum PlannedProgressType {

    PLANNED("01planned"), CORRECTED("02corrected");

    private final String plannedProgressType;

    private PlannedProgressType(final String plannedProgressType) {
        this.plannedProgressType = plannedProgressType;
    }

    public String getStringValue() {
        return plannedProgressType;
    }

}
