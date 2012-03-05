package com.qcadoo.mes.productionCounting.internal.constants;

public enum ProductionRecordState {
    DRAFT("01draft"), ACCEPTED("02accepted"), DECLINED("03declined");

    private final String state;

    private ProductionRecordState(final String state) {
        this.state = state;
    }

    public String getStringValue() {
        return state;
    }
}
