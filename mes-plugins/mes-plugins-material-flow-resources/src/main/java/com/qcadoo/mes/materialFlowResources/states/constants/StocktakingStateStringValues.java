package com.qcadoo.mes.materialFlowResources.states.constants;

import com.qcadoo.mes.states.aop.RunForStateTransitionAspect;

public class StocktakingStateStringValues {

    private StocktakingStateStringValues() {

    }

    public static final String WILDCARD_STATE = RunForStateTransitionAspect.WILDCARD_STATE;

    public static final String DRAFT = "01draft";

    public static final String IN_PROGRESS = "02inProgress";

    public static final String FINALIZED = "03finalized";

    public static final String FINISHED = "04finished";

    public static final String REJECTED = "05rejected";

}
