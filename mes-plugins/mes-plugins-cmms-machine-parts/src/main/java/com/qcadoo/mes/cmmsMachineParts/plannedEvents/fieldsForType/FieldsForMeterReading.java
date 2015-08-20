package com.qcadoo.mes.cmmsMachineParts.plannedEvents.fieldsForType;

import com.google.common.collect.Lists;
import com.qcadoo.mes.cmmsMachineParts.constants.PlannedEventFields;

public class FieldsForMeterReading extends AbstractFieldsForType {

    public FieldsForMeterReading() {
        super(Lists.newArrayList(PlannedEventFields.EFFECTIVE_DURATION, PlannedEventFields.PLANNED_SEPARATELY,
                PlannedEventFields.REQUIRES_SHUTDOWN, PlannedEventFields.COUNTER, PlannedEventFields.DURATION,
                PlannedEventFields.EFFECTIVE_DURATION, PlannedEventFields.COMPANY), Lists.newArrayList(
                PlannedEventFields.ACTIONS_TAB, PlannedEventFields.MACHINE_PARTS_TAB,
                PlannedEventFields.SOLUTION_DESCRIPTION_TAB, PlannedEventFields.RELATED_EVENTS_TAB), Lists.newArrayList(
                PlannedEventFields.ACTIONS, PlannedEventFields.MACHINE_PARTS_FOR_EVENT));
    }

    @Override
    public boolean shouldLockBasedOn() {
        return true;
    }
}
