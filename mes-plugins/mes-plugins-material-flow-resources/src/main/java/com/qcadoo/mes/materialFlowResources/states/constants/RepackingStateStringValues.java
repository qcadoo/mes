package com.qcadoo.mes.materialFlowResources.states.constants;

import com.qcadoo.mes.states.aop.RunForStateTransitionAspect;

public class RepackingStateStringValues {

    private RepackingStateStringValues() {

    }

    public static final String WILDCARD_STATE = RunForStateTransitionAspect.WILDCARD_STATE;

    public static final String DRAFT = "01draft";

    public static final String ACCEPTED = "02accepted";

    public static final String REJECTED = "03rejected";

}
