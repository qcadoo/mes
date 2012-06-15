package com.qcadoo.mes.orders.states.constants;

public final class OrderStateChangePhase {

    public static final int PRE_VALIDATION = 1;

    public static final int DEFAULT = 3;

    public static final int FILL_REASON = 5;

    public static final int EXT_SYNC = 7;

    private OrderStateChangePhase() {
    }

    public static int getNumOfPhases() {
        return 7;
    }

}
