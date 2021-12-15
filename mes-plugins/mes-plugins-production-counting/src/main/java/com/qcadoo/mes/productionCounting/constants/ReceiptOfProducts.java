package com.qcadoo.mes.productionCounting.constants;

public enum ReceiptOfProducts {

    ON_ACCEPTANCE_REGISTRATION_RECORD("01onAcceptanceRegistrationRecord"), END_OF_THE_ORDER(
            "02endOfTheOrder"), MANUALLY_TO_ORDER_GROUP(
                    "03manuallyToOrderGroup"), DO_NOT_CREATE_DOCUMENTS("04doNotCreateDocuments");

    private final String type;

    private ReceiptOfProducts(final String type) {
        this.type = type;
    }

    public String getStringValue() {
        return type;
    }

    public static ReceiptOfProducts parseString(final String string) {
        if ("01onAcceptanceRegistrationRecord".equals(string)) {
            return ON_ACCEPTANCE_REGISTRATION_RECORD;
        } else if ("02endOfTheOrder".equals(string)) {
            return END_OF_THE_ORDER;
        } else if ("03manuallyToOrderGroup".equals(string)) {
            return MANUALLY_TO_ORDER_GROUP;
        } else if ("04doNotCreateDocuments".equals(string)) {
            return DO_NOT_CREATE_DOCUMENTS;
        }

        throw new IllegalStateException("Unsupported receiptOfProducts: " + string);
    }
}
