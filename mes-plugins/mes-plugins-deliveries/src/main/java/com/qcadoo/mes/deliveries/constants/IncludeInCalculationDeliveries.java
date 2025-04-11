package com.qcadoo.mes.deliveries.constants;

import com.qcadoo.mes.deliveries.states.constants.DeliveryStateStringValues;

import java.util.ArrayList;
import java.util.List;

public enum IncludeInCalculationDeliveries {

    CONFIRMED_DELIVERIES("01confirmedDeliveries"),
    UNCONFIRMED_DELIVERIES("02unconfirmedDeliveries"),
    NON_DRAFT_DELIVERIES("03nonDraftDeliveries");

    private final String mode;

    IncludeInCalculationDeliveries(final String mode) {
        this.mode = mode;
    }

    public String getStringValue() {
        return mode;
    }

    public static List<String> getStates(String includeInCalculationDeliveries) {
        List<String> states = new ArrayList<>();
        if (CONFIRMED_DELIVERIES.getStringValue().equals(includeInCalculationDeliveries)) {
            states.add(DeliveryStateStringValues.APPROVED);
            states.add(DeliveryStateStringValues.ACCEPTED);
        } else if (UNCONFIRMED_DELIVERIES.getStringValue().equals(includeInCalculationDeliveries)) {
            states.add(DeliveryStateStringValues.APPROVED);
            states.add(DeliveryStateStringValues.ACCEPTED);
            states.add(DeliveryStateStringValues.PREPARED);
            states.add(DeliveryStateStringValues.DURING_CORRECTION);
            states.add(DeliveryStateStringValues.DRAFT);
        } else {
            states.add(DeliveryStateStringValues.APPROVED);
            states.add(DeliveryStateStringValues.ACCEPTED);
            states.add(DeliveryStateStringValues.PREPARED);
            states.add(DeliveryStateStringValues.DURING_CORRECTION);
        }
        return states;
    }
}
