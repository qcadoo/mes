package com.qcadoo.mes.deliveries.states.constants;

public final class DeliveryStateChangePhase {

    public static final int PRE_VALIDATION = 1;

    public static final int DEFAULT = 3;

    public static final int FILL_REASON = 5;

    public static final int EXT_SYNC = 7;

    public static final int LAST = EXT_SYNC + 1;

    private DeliveryStateChangePhase() {
    }

    public static int getNumOfPhases() {
        return LAST;
    }
}
