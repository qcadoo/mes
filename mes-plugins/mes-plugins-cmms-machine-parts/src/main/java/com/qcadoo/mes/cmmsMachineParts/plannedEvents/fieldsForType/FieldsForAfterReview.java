package com.qcadoo.mes.cmmsMachineParts.plannedEvents.fieldsForType;

import com.google.common.collect.Lists;
import com.qcadoo.mes.cmmsMachineParts.constants.PlannedEventFields;

public class FieldsForAfterReview extends AbstractFieldsForType {

    public FieldsForAfterReview() {
        super(Lists.newArrayList(PlannedEventFields.EFFECTIVE_DURATION, PlannedEventFields.PLANNED_SEPARATELY,
                PlannedEventFields.COUNTER, PlannedEventFields.DURATION, PlannedEventFields.COMPANY), Lists
                .newArrayList(PlannedEventFields.ACTIONS_TAB), Lists.newArrayList());
    }

    @Override
    public boolean shouldLockBasedOn() {
        return true;
    }
}
