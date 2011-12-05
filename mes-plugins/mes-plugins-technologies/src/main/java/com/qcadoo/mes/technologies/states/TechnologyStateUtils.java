package com.qcadoo.mes.technologies.states;

import com.qcadoo.mes.technologies.constants.TechnologyState;

public final class TechnologyStateUtils {

    private TechnologyStateUtils() {
    }

    public static TechnologyState getStateFromField(final String fieldValue) {
        if ("01draft".equals(fieldValue)) {
            return TechnologyState.DRAFT;
        }
        if ("02accepted".equals(fieldValue)) {
            return TechnologyState.ACCEPTED;
        }
        if ("03declined".equals(fieldValue)) {
            return TechnologyState.DECLINED;
        }
        if ("04outdated".equals(fieldValue)) {
            return TechnologyState.OUTDATED;
        }

        throw new IllegalArgumentException("Unsupported or unspecified technology state " + fieldValue);
    }
}
