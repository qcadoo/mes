package com.qcadoo.mes.masterOrders.states.constants;

import com.qcadoo.mes.states.aop.RunForStateTransitionAspect;

public final class SalesPlanStateStringValues {

    private SalesPlanStateStringValues() {

    }

    public static final String WILDCARD_STATE = RunForStateTransitionAspect.WILDCARD_STATE;
    public static final String DRAFT = "01draft";
    public static final String REJECTED = "02rejected";
    public static final String COMPLETED = "03completed";

}
