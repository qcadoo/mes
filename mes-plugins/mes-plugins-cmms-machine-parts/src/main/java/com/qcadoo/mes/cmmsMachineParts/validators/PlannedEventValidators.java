package com.qcadoo.mes.cmmsMachineParts.validators;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.qcadoo.mes.cmmsMachineParts.ActionsService;
import com.qcadoo.mes.cmmsMachineParts.constants.ActionFields;
import com.qcadoo.mes.cmmsMachineParts.constants.ActionForPlannedEventFields;
import com.qcadoo.mes.cmmsMachineParts.constants.PlannedEventFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class PlannedEventValidators {

    @Autowired
    private ActionsService actionsService;

    public boolean validatesWith(final DataDefinition plannedEventDD, final Entity plannedEvent) {

        if (!checkOperatorWorkTime(plannedEventDD, plannedEvent) || !validateActions(plannedEvent)) {
            return false;
        }

        return true;
    }

    private boolean checkOperatorWorkTime(final DataDefinition plannedEventDD, final Entity plannedEvent) {
        Date startDate = plannedEvent.getDateField(PlannedEventFields.START_DATE);
        Date finishDate = plannedEvent.getDateField(PlannedEventFields.FINISH_DATE);

        if (startDate == null || finishDate == null || finishDate.after(startDate)) {
            return true;
        }
        plannedEvent.addError(plannedEventDD.getField(PlannedEventFields.FINISH_DATE),
                "cmmsMachineParts.plannedEventDetails.error.wrongDateOrder");
        return false;
    }

    private boolean validateActions(final Entity plannedEvent) {

        List<Entity> actionsForEvent = plannedEvent.getHasManyField(PlannedEventFields.ACTIONS);
        Entity subassembly = plannedEvent.getBelongsToField(PlannedEventFields.SUBASSEMBLY);
        Entity workstation = plannedEvent.getBelongsToField(PlannedEventFields.WORKSTATION);
        List<String> invalidActions = Lists.newArrayList();
        if (subassembly != null) {
            for (Entity actionForEvent : actionsForEvent) {
                Entity action = actionForEvent.getBelongsToField(ActionForPlannedEventFields.ACTION);
                if (!actionsService.checkIfActionAppliesToSubassembly(action, subassembly)) {
                    if (!invalidActions.contains(action.getStringField(ActionFields.NAME))) {
                        invalidActions.add(action.getStringField(ActionFields.NAME));
                    }
                }
            }
        } else if (workstation != null) {
            for (Entity actionForEvent : actionsForEvent) {
                Entity action = actionForEvent.getBelongsToField(ActionForPlannedEventFields.ACTION);
                if (!actionsService.checkIfActionAppliesToWorkstation(action, workstation)) {
                    if (!invalidActions.contains(action.getStringField(ActionFields.NAME))) {
                        invalidActions.add(action.getStringField(ActionFields.NAME));
                    }
                }
            }
        }
        if (!invalidActions.isEmpty()) {
            String actions = invalidActions.stream().collect(Collectors.joining(", "));
            if (actions.length() < 200) {
                plannedEvent.addGlobalError("cmmsMachineParts.plannedEventDetails.error.invalidActions", false, actions);
            } else {
                plannedEvent.addGlobalError("cmmsMachineParts.plannedEventDetails.error.invalidActionsShort", false);
            }
            return false;
        }
        return true;
    }
}
