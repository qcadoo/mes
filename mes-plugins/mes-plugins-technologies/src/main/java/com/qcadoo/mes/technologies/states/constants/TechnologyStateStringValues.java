package com.qcadoo.mes.technologies.states.constants;

import com.qcadoo.mes.states.aop.RunForStateTransitionAspect;

public final class TechnologyStateStringValues {

    private TechnologyStateStringValues() {
    }

    public static final String DRAFT = "01draft";

    public static final String ACCEPTED = "02accepted";

    public static final String DECLINED = "03declined";

    public static final String OUTDATED = "04outdated";

    public static final String CHECKED = "05checked";

    public static final String WILDCARD_STATE = RunForStateTransitionAspect.WILDCARD_STATE;

}
