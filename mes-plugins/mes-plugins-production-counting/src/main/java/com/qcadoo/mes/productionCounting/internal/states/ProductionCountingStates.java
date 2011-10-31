package com.qcadoo.mes.productionCounting.internal.states;

public enum ProductionCountingStates {
    DRAFT("01draft"), ACCEPTED("02accepted"), DECLINED("03declined");

    private final String state;

    private ProductionCountingStates(final String state) {
        this.state = state;
    }

    public String getStringValue() {
        return state;
    }
}
