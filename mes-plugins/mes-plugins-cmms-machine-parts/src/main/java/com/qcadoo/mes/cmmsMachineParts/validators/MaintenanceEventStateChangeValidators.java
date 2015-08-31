package com.qcadoo.mes.cmmsMachineParts.validators;

import org.springframework.stereotype.Service;

import com.google.common.base.Strings;
import com.qcadoo.mes.cmmsMachineParts.states.constants.MaintenanceEventStateChangeFields;
import com.qcadoo.mes.cmmsMachineParts.states.constants.MaintenanceEventStateStringValues;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class MaintenanceEventStateChangeValidators {

    public boolean validate(final DataDefinition evenStateChangetDD, final Entity eventStateChange) {
        String eventStatus = eventStateChange.getStringField(MaintenanceEventStateChangeFields.TARGET_STATE);

        switch (eventStatus) {
            case MaintenanceEventStateStringValues.REVOKED:
                return validateForRevokedStatus(evenStateChangetDD, eventStateChange);
            case MaintenanceEventStateStringValues.PLANNED:
                return validateForPlannedtatus(evenStateChangetDD, eventStateChange);
        }

        return true;
    }

    private boolean validateForRevokedStatus(final DataDefinition evenStateChangetDD, final Entity eventStateChange) {
        String comment = eventStateChange.getStringField(MaintenanceEventStateChangeFields.COMMENT);

        if (eventStateChange.getBooleanField(MaintenanceEventStateChangeFields.COMMENT_REQUIRED)
                && Strings.isNullOrEmpty(comment)) {
            eventStateChange.addError(evenStateChangetDD.getField(MaintenanceEventStateChangeFields.COMMENT),
                    "cmmsMachineParts.maintenanceEvent.state.commentRequired");
            return false;
        }

        return true;
    }

    private boolean validateForPlannedtatus(final DataDefinition evenStateChangetDD, final Entity eventStateChange) {
        String type = eventStateChange.getStringField(MaintenanceEventStateChangeFields.PLANNED_EVENT_TYPE);

        if (eventStateChange.getBooleanField(MaintenanceEventStateChangeFields.PLANNED_EVENT_TYPE_REQUIRED)
                && Strings.isNullOrEmpty(type)) {
            eventStateChange.addError(evenStateChangetDD.getField(MaintenanceEventStateChangeFields.PLANNED_EVENT_TYPE),
                    "cmmsMachineParts.maintenanceEventStateChange.plannedEventType.required.error");
            return false;
        }

        return true;
    }
}
