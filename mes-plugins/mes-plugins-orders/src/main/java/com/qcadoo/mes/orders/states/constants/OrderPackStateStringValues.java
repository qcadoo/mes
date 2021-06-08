package com.qcadoo.mes.orders.states.constants;

import com.qcadoo.mes.states.aop.RunForStateTransitionAspect;

public final class OrderPackStateStringValues {

    private OrderPackStateStringValues() {

    }

    public static final String WILDCARD_STATE = RunForStateTransitionAspect.WILDCARD_STATE;
    public static final String PENDING = "01pending";
    public static final String DURING_PRODUCTION = "02duringProduction";
    public static final String FINISHED_PRODUCTION = "03finishedProduction";

}
