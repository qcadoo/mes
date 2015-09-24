/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.4
 *
 * This file is part of Qcadoo.
 *
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */
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
import com.qcadoo.mes.cmmsMachineParts.constants.PlannedEventType;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class PlannedEventValidators {

    @Autowired
    private ActionsService actionsService;

    public boolean validatesWith(final DataDefinition plannedEventDD, final Entity plannedEvent) {

        if (!checkOperatorWorkTime(plannedEventDD, plannedEvent) || !validateActions(plannedEvent)
                || !validateType(plannedEventDD, plannedEvent)) {
            return false;
        }

        return true;
    }

    private boolean validateType(final DataDefinition plannedEventDD, final Entity plannedEvent) {

        if (!plannedEvent.getBooleanField(PlannedEventFields.AFTER_REVIEW)) {
            if (PlannedEventType.AFTER_REVIEW.equals(PlannedEventType.from(plannedEvent))) {

                plannedEvent.addError(plannedEventDD.getField(PlannedEventFields.TYPE),
                        "cmmsMachineParts.plannedEvent.error.wrongType");
                return false;
            }
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
