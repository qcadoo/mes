package com.qcadoo.mes.productionCounting.constants;

public enum ReleaseOfMaterials {

    ON_ACCEPTANCE_REGISTRATION_RECORD("01onAcceptanceRegistrationRecord"), END_OF_THE_ORDER("02endOfTheOrder"), MANUALLY_TO_ORDER_OR_GROUP("03manuallyToOrderOrGroup");

    private final String type;

    private ReleaseOfMaterials(final String type) {
        this.type = type;
    }

    public String getStringValue() {
        return type;
    }

    public static ReleaseOfMaterials parseString(final String string) {
        if ("01onAcceptanceRegistrationRecord".equals(string)) {
            return ON_ACCEPTANCE_REGISTRATION_RECORD;
        } else if ("02endOfTheOrder".equals(string)) {
            return END_OF_THE_ORDER;
        } else if ("03manuallyToOrderOrGroup".equals(string)) {
            return MANUALLY_TO_ORDER_OR_GROUP;
        }

        throw new IllegalStateException("Unsupported releaseOfMaterials: " + string);
    }
}
