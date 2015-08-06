package com.qcadoo.mes.cmmsMachineParts.plannedEvents.fieldsForType;

import com.google.common.collect.Lists;
import com.qcadoo.mes.cmmsMachineParts.constants.PlannedEventFields;

public class FieldsForReview extends AbstractFieldsForType {

    public FieldsForReview() {
        super(Lists.newArrayList(PlannedEventFields.EFFECTIVE_DURATION, PlannedEventFields.COMPANY), Lists.newArrayList());
    }
}
