package com.qcadoo.mes.deliveries.states.constants;

import com.qcadoo.mes.states.aop.RunForStateTransitionAspect;

public final class DeliveryStateStringValues {

    private DeliveryStateStringValues() {
    }

    public static final String WILDCARD_STATE = RunForStateTransitionAspect.WILDCARD_STATE;

    public static final String DRAFT = "01draft";

    public static final String PREPARED = "02prepared";

    public static final String DURING_CORRECTION = "03duringCorrection";

    public static final String DECLINED = "04declined";

    public static final String APPROVED = "05approved";

    public static final String RECEIVED = "06received";

}
