package com.qcadoo.mes.cmmsMachineParts.plannedEvents.factory;

import com.qcadoo.mes.cmmsMachineParts.constants.PlannedEventType;
import com.qcadoo.mes.cmmsMachineParts.plannedEvents.fieldsForType.FieldsForType;

public interface EventFieldsForTypeFactory {

    public FieldsForType createFieldsForType(final PlannedEventType type);

}
