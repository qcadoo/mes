package com.qcadoo.mes.orders.constants;

public enum OrderStates {

    PENDING("01pending"), ACCEPTED("02accepted"), IN_PROGRESS("03inProgress"), COMPLETED("04completed"), DECLINED("05declined"), INTERRUPTED(
            "06interrupted"), ABANDONED("07abandoned");

    private final String state;

    private OrderStates(final String state) {
        this.state = state;
    }

    public String getStringValue() {
        return state;
    }

}
