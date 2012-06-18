package com.qcadoo.mes.technologies.states.constants;

public class TechnologyStateChangePhase {

    public static final int PRE_VALIDATION = 1;

    public static final int DEFAULT = 3;

    private TechnologyStateChangePhase() {
    }

    public static final int getNumOfPhases() {
        return 3;
    }

}
