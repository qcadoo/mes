package com.qcadoo.mes.cmmsMachineParts.hooks;

import org.springframework.stereotype.Service;

import com.google.common.base.Strings;
import com.qcadoo.mes.basic.constants.StaffFields;
import com.qcadoo.mes.cmmsMachineParts.constants.PlannedEventFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class PlannedEventHooks {

    public void onSave(final DataDefinition eventDD, final Entity event) {
        Entity owner = event.getBelongsToField(PlannedEventFields.OWNER);
        if (owner != null) {
            String person = Strings.nullToEmpty(owner.getStringField(StaffFields.NAME)) + " "
                    + Strings.nullToEmpty(owner.getStringField(StaffFields.SURNAME));
            event.setField(PlannedEventFields.OWNER_NAME, person);
        }
    }
}
