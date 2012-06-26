package com.qcadoo.mes.productionCounting.states.constants;

public final class ProductionRecordStateChangePhase {

    public static final int PRE_VALIDATION = 1;

    public static final int DEFAULT = 3;

    public static final int LAST = DEFAULT + 1;

    private ProductionRecordStateChangePhase() {
    }

    public static int getNumOfPhases() {
        return LAST;
    }
}
