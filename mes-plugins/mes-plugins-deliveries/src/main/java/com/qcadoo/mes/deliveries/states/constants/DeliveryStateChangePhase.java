package com.qcadoo.mes.deliveries.states.constants;

public final class DeliveryStateChangePhase {

    public static final int PRE_VALIDATION = 1;

    public static final int DEFAULT = 3;

    public static final int LAST = DEFAULT + 1;

    private DeliveryStateChangePhase() {
    }

    public static int getNumOfPhases() {
        return LAST;
    }
}
