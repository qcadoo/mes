package com.qcadoo.mes.cmmsMachineParts.validators;

import org.springframework.stereotype.Service;

import com.google.common.base.Strings;
import com.qcadoo.mes.cmmsMachineParts.states.constants.PlannedEventStateChangeFields;
import com.qcadoo.mes.cmmsMachineParts.states.constants.PlannedEventStateStringValues;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class PlannedEventStateChangeValidators {

    public boolean validate(final DataDefinition evenStateChangetDD, final Entity eventStateChange) {
        String eventStatus = eventStateChange.getStringField(PlannedEventStateChangeFields.TARGET_STATE);

        switch (eventStatus) {
            case PlannedEventStateStringValues.CANCELED:
                return validateForRevokedStatus(evenStateChangetDD, eventStateChange);
        }

        return true;
    }

    private boolean validateForRevokedStatus(final DataDefinition evenStateChangetDD, final Entity eventStateChange) {
        String comment = eventStateChange.getStringField(PlannedEventStateChangeFields.COMMENT);

        if (eventStateChange.getBooleanField(PlannedEventStateChangeFields.COMMENT_REQUIRED) && Strings.isNullOrEmpty(comment)) {
            eventStateChange.addError(evenStateChangetDD.getField(PlannedEventStateChangeFields.COMMENT),
                    "cmmsMachineParts.plannedEvent.state.commentRequired");
            return false;
        }

        return true;
    }
}
