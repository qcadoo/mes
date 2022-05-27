package com.qcadoo.mes.orders.constants;

public enum DurationOfOrderCalculatedOnBasis {
    TIME_CONSUMING_TECHNOLOGY("01timeConsumingTechnology"), PLAN_FOR_SHIFT("02planForShift");

    private final String basis;

    DurationOfOrderCalculatedOnBasis(final String basis) {
        this.basis = basis;
    }

    public String getStringValue() {
        return basis;
    }
}
