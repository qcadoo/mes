package com.qcadoo.mes.cmmsMachineParts.plannedEvents.fieldsForType;

import com.google.common.collect.Lists;
import com.qcadoo.mes.cmmsMachineParts.constants.PlannedEventFields;

public class FieldsForRepairs extends AbstractFieldsForType {

    public FieldsForRepairs() {
        super(Lists.newArrayList(PlannedEventFields.EFFECTIVE_DURATION, PlannedEventFields.COMPANY), Lists.newArrayList(), Lists
                .newArrayList());
    }
}
