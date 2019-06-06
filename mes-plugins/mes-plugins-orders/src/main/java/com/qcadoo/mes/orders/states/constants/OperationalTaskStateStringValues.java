package com.qcadoo.mes.orders.states.constants;

import com.qcadoo.mes.states.aop.RunForStateTransitionAspect;

public final class OperationalTaskStateStringValues {

    private OperationalTaskStateStringValues() {

    }

    public static final String WILDCARD_STATE = RunForStateTransitionAspect.WILDCARD_STATE;

    public static final String PENDING = "01pending";

    public static final String STARTED = "02started";

    public static final String FINISHED = "03finished";

    public static final String REJECTED = "04rejected";
}
