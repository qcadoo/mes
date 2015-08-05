package com.qcadoo.mes.cmmsMachineParts.constants;

import com.qcadoo.model.api.Entity;

public enum PlannedEventBasedOn {
    DATE("01date"), COUNTER("02counter");

    private final String basedOn;

    public static PlannedEventBasedOn from(final Entity entity) {
        return parseString(entity.getStringField(PlannedEventFields.BASED_ON));
    }

    private PlannedEventBasedOn(final String appliesTo) {
        this.basedOn = appliesTo;
    }

    public String getStringValue() {
        return basedOn;
    }

    public static PlannedEventBasedOn parseString(final String basedOn) {
        if ("01date".equals(basedOn)) {
            return DATE;
        } else if ("02counter".equals(basedOn)) {
            return COUNTER;
        }

        throw new IllegalStateException("Unsupported basedOn: " + basedOn);
    }

}
