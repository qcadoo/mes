package com.qcadoo.mes.cmmsMachineParts.validators;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.cmmsMachineParts.ActionsService;
import com.qcadoo.mes.cmmsMachineParts.constants.ActionForPlannedEventFields;
import com.qcadoo.mes.cmmsMachineParts.constants.ActionForPlannedEventState;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class ActionForPlannedEventValidators {

    @Autowired
    private ActionsService actionsService;

    public boolean validateRequiredFields(final DataDefinition actionDD, final Entity actionForPlannedEvent) {

        Entity defaultAction = actionsService.getDefaultAction();
        Entity action = actionForPlannedEvent.getBelongsToField(ActionForPlannedEventFields.ACTION);
        boolean correct = true;
        if (defaultAction != null && action != null && defaultAction.getId().equals(action.getId())) {
            if (StringUtils.isEmpty(actionForPlannedEvent.getStringField(ActionForPlannedEventFields.DESCRIPTION))) {
                actionForPlannedEvent.addError(actionDD.getField(ActionForPlannedEventFields.DESCRIPTION),
                        "cmmsMachineParts.actionForPlannedEvent.error.descriptionRequired");
                correct = false;
            }
        }
        String state = actionForPlannedEvent.getStringField(ActionForPlannedEventFields.STATE);
        if (state.equals(ActionForPlannedEventState.INCORRECT.getStringValue())) {
            if (StringUtils.isEmpty(actionForPlannedEvent.getStringField(ActionForPlannedEventFields.REASON))) {
                actionForPlannedEvent.addError(actionDD.getField(ActionForPlannedEventFields.REASON),
                        "cmmsMachineParts.actionForPlannedEvent.error.reasonRequired");
                correct = false;
            }
        }
        return correct;
    }
}
