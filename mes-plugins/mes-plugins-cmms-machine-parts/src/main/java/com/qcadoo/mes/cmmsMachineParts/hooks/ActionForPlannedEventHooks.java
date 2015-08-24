package com.qcadoo.mes.cmmsMachineParts.hooks;

import org.springframework.stereotype.Service;

import com.google.common.base.Strings;
import com.qcadoo.mes.basic.constants.StaffFields;
import com.qcadoo.mes.cmmsMachineParts.constants.ActionForPlannedEventFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class ActionForPlannedEventHooks {

    public void onSave(final DataDefinition actionDD, final Entity actionForPlannedEvent) {
        if (actionForPlannedEvent.getBelongsToField(ActionForPlannedEventFields.RESPONSIBLE_WORKER) != null) {
            String person = Strings.nullToEmpty(actionForPlannedEvent.getBelongsToField(
                    ActionForPlannedEventFields.RESPONSIBLE_WORKER).getStringField(StaffFields.NAME))
                    + " "
                    + Strings.nullToEmpty(actionForPlannedEvent.getBelongsToField(ActionForPlannedEventFields.RESPONSIBLE_WORKER)
                            .getStringField(StaffFields.SURNAME));
            actionForPlannedEvent.setField(ActionForPlannedEventFields.RESPONSIBLE_WORKER_NAME, person);
        }
    }

}
