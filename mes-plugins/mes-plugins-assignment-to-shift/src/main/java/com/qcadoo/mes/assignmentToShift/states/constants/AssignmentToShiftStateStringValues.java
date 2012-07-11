package com.qcadoo.mes.assignmentToShift.states.constants;

import com.qcadoo.mes.states.aop.RunForStateTransitionAspect;

public final class AssignmentToShiftStateStringValues {

    private AssignmentToShiftStateStringValues() {

    }

    public static final String WILDCARD_STATE = RunForStateTransitionAspect.WILDCARD_STATE;

    public static final String DRAFT = "01draft";

    public static final String ACCEPTED = "02accepted";

    public static final String DURING_CORRECTION = "03duringCorrection";

    public static final String CORRECTED = "04corrected";

}
