package com.qcadoo.mes.cmmsMachineParts.hooks;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.base.Strings;
import com.qcadoo.mes.basic.constants.StaffFields;
import com.qcadoo.mes.cmmsMachineParts.constants.PlannedEventFields;
import com.qcadoo.mes.cmmsMachineParts.constants.PlannedEventType;
import com.qcadoo.mes.cmmsMachineParts.plannedEvents.factory.EventFieldsForTypeFactory;
import com.qcadoo.mes.cmmsMachineParts.plannedEvents.fieldsForType.FieldsForType;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class PlannedEventHooks {

    @Autowired
    private EventFieldsForTypeFactory fieldsForTypeFactory;

    public void onSave(final DataDefinition eventDD, final Entity event) {
        Entity owner = event.getBelongsToField(PlannedEventFields.OWNER);
        if (owner != null) {
            String person = Strings.nullToEmpty(owner.getStringField(StaffFields.NAME)) + " "
                    + Strings.nullToEmpty(owner.getStringField(StaffFields.SURNAME));
            event.setField(PlannedEventFields.OWNER_NAME, person);
        } else {
            event.setField(PlannedEventFields.OWNER_NAME, StringUtils.EMPTY);
        }
        clearHiddenFields(event);
    }

    private void clearHiddenFields(final Entity event) {
        FieldsForType fieldsForType = fieldsForTypeFactory.createFieldsForType(PlannedEventType.from(event));
        for (String fieldName : fieldsForType.getHiddenFields()) {
            event.setField(fieldName, null);
        }

    }
}
