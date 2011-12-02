package com.qcadoo.mes.technologies.states;

import org.springframework.util.StringUtils;

import com.qcadoo.mes.technologies.constants.TechnologyState;

public final class TechnologyStateUtils {

    private TechnologyStateUtils() {
    }

    public static TechnologyState getStateFromField(final String fieldValue) {
        if (!StringUtils.hasText(fieldValue)) {
            return null;
        }

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

        return TechnologyState.DRAFT;
    }
}
