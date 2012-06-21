package com.qcadoo.mes.technologies.states.constants;

public final class TechnologyStateChangePhase {

    public static final int PRE_VALIDATION = 1;

    public static final int DEFAULT = 3;

    public static final int LAST = DEFAULT + 1;

    private TechnologyStateChangePhase() {
    }

    public static final int getNumOfPhases() {
        return LAST;
    }

}
