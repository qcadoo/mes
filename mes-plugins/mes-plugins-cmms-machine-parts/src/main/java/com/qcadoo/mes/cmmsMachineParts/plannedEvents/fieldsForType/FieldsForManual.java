package com.qcadoo.mes.cmmsMachineParts.plannedEvents.fieldsForType;

import com.google.common.collect.Lists;
import com.qcadoo.mes.cmmsMachineParts.constants.PlannedEventFields;

public class FieldsForManual extends AbstractFieldsForType {

    public FieldsForManual() {
        super(Lists.newArrayList(PlannedEventFields.EFFECTIVE_DURATION, PlannedEventFields.PLANNED_SEPARATELY,
                PlannedEventFields.COUNTER, PlannedEventFields.DURATION, PlannedEventFields.EFFECTIVE_DURATION,
                PlannedEventFields.COMPANY), Lists.newArrayList());
    }

    @Override
    public boolean shouldLockBasedOn() {
        return true;
    }
}
