package com.qcadoo.mes.cmmsMachineParts.hooks;

import java.util.List;

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
        List<String> fieldsToClear = fieldsForType.getHiddenFields();
        List<String> hasManyToClear = fieldsForType.getGridsToClear();
        for (String fieldName : fieldsToClear) {
            event.setField(fieldName, null);
        }

        for (String fieldName : hasManyToClear) {
            List<Entity> fields = event.getHasManyField(fieldName);

            if (!fields.isEmpty()) {
                DataDefinition dataDefinition = fields.get(0).getDataDefinition();
                Long[] ids = fields.stream().map(entity -> entity.getId()).toArray(size -> new Long[size]);
                dataDefinition.delete(ids);
                event.setField(fieldName, null);
            }
        }

    }
}
