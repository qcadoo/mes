package com.qcadoo.mes.cmmsMachineParts.constants;

import com.qcadoo.model.api.Entity;

public enum PlannedEventType {
    REVIEW("01review"), REPAIRS("02repairs"), EXTERNAL_SERVICE("03externalService"), UDT_REVIEW("04udtReview"), METER_READING(
            "05meterReading"), MANUAL("06manual"), ADDITIONAL_WORK("07additionalWork");

    private final String type;

    public static PlannedEventType from(final Entity entity) {
        return parseString(entity.getStringField(PlannedEventFields.TYPE));
    }

    private PlannedEventType(final String type) {
        this.type = type;
    }

    public String getStringValue() {
        return type;
    }

    public static PlannedEventType parseString(final String type) {
        if ("01review".equals(type)) {
            return REVIEW;
        } else if ("02repairs".equals(type)) {
            return REPAIRS;
        } else if ("03externalService".equals(type)) {
            return EXTERNAL_SERVICE;
        } else if ("04udtReview".equals(type)) {
            return UDT_REVIEW;
        } else if ("05meterReading".equals(type)) {
            return METER_READING;
        } else if ("06manual".equals(type)) {
            return MANUAL;
        } else if ("07additionalWork".equals(type)) {
            return ADDITIONAL_WORK;
        }

        throw new IllegalStateException("Unsupported type: " + type);
    }

}
