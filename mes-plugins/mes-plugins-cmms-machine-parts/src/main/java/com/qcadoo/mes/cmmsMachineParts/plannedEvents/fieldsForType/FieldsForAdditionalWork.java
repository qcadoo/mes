package com.qcadoo.mes.cmmsMachineParts.plannedEvents.fieldsForType;

import com.google.common.collect.Lists;
import com.qcadoo.mes.cmmsMachineParts.constants.PlannedEventFields;

public class FieldsForAdditionalWork extends AbstractFieldsForType {

    public FieldsForAdditionalWork() {
        super(Lists.newArrayList(PlannedEventFields.EFFECTIVE_DURATION, PlannedEventFields.PLANNED_SEPARATELY,
                PlannedEventFields.REQUIRES_SHUTDOWN, PlannedEventFields.COUNTER, PlannedEventFields.DURATION,
                PlannedEventFields.EFFECTIVE_DURATION, PlannedEventFields.COMPANY, PlannedEventFields.EFFECTIVE_COUNTER), Lists
                .newArrayList());
    }

    @Override
    public boolean shouldLockBasedOn() {
        return true;
    }
}
