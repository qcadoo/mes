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
package com.qcadoo.mes.cmmsMachineParts.states;

import com.qcadoo.mes.cmmsMachineParts.constants.*;
import com.qcadoo.mes.cmmsMachineParts.plannedEvents.factory.EventFieldsForTypeFactory;
import com.qcadoo.mes.cmmsMachineParts.plannedEvents.fieldsForType.FieldsForType;
import com.qcadoo.mes.states.StateChangeContext;
import com.qcadoo.model.api.Entity;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PlannedEventStateValidationService {

    @Autowired
    private EventFieldsForTypeFactory eventFieldsForTypeFactory;

    public void validationOnInPlan(final StateChangeContext stateChangeContext) {
        Entity event = stateChangeContext.getOwner();
        checkIfOwnerIsSet(event, stateChangeContext);

    }

    private void checkIfOwnerIsSet(Entity event, StateChangeContext stateChangeContext) {
        if (event.getBelongsToField(PlannedEventFields.OWNER) == null) {
            stateChangeContext.addFieldValidationError(PlannedEventFields.OWNER,
                    "cmmsMachineParts.plannedEvent.state.fieldRequired");
            stateChangeContext.addValidationError("cmmsMachineParts.plannedEvent.state.ownerNotFound");
        }
    }

    public void validationOnPlanned(StateChangeContext stateChangeContext) {
        Entity event = stateChangeContext.getOwner();
        checkIfRequiredFieldsAreSet(event, stateChangeContext);
    }

    public void validationOnInRealization(StateChangeContext stateChangeContext) {
        Entity event = stateChangeContext.getOwner();
        checkIfRequiredFieldsAreSet(event, stateChangeContext);
    }

    public void validationOnRealized(StateChangeContext stateChangeContext) {
        Entity event = stateChangeContext.getOwner();
        checkIfActionsHaveStatus(event, stateChangeContext);
        checkSourceCost(event, stateChangeContext);
        checkEffectiveCounter(event, stateChangeContext);
        checkIfRealizationExists(event, stateChangeContext);
        checkIfSolutionDescriptionExists(event, stateChangeContext);
        checkIfRequiredFieldsAreSet(event, stateChangeContext);
    }

    private void checkIfSolutionDescriptionExists(Entity event, StateChangeContext stateChangeContext) {
        if (event.getStringField(PlannedEventFields.SOLUTION_DESCRIPTION) == null
                || event.getStringField(PlannedEventFields.SOLUTION_DESCRIPTION).isEmpty()) {
            if (solutionDescriptionIsNotHidden(event) && hasNoActions(event)) {
                stateChangeContext.addFieldValidationError(PlannedEventFields.SOLUTION_DESCRIPTION,
                        "cmmsMachineParts.plannedEvent.state.fieldRequired");
            }
        }
    }

    private void checkIfRealizationExists(Entity event, StateChangeContext stateChangeContext) {
        if (event.getHasManyField(PlannedEventFields.REALIZATIONS) == null
                || event.getHasManyField(PlannedEventFields.REALIZATIONS).isEmpty()) {
            stateChangeContext.addFieldValidationError(PlannedEventFields.REALIZATIONS,
                    "cmmsMachineParts.plannedEvent.state.fieldRequired");
        }
    }

    private void checkSourceCost(final Entity event, StateChangeContext stateChangeContext) {
        if (event.getBelongsToField(MaintenanceEventFields.SOURCE_COST) == null) {
            stateChangeContext.addFieldValidationError(MaintenanceEventFields.SOURCE_COST,
                    "cmmsMachineParts.maintenanceEvent.state.fieldRequired");
        }
    }

    private void checkEffectiveCounter(final Entity event, StateChangeContext stateChangeContext) {
        String basedOn = event.getStringField(PlannedEventFields.BASED_ON);
        String type = event.getStringField(PlannedEventFields.TYPE);
        if (event.getDecimalField(PlannedEventFields.EFFECTIVE_COUNTER) == null
                && (PlannedEventBasedOn.COUNTER.getStringValue().equals(basedOn) || PlannedEventType.METER_READING
                        .getStringValue().equals(type))) {
            stateChangeContext.addFieldValidationError(PlannedEventFields.EFFECTIVE_COUNTER,
                    "cmmsMachineParts.maintenanceEvent.state.fieldRequired");
        }
    }

    private void checkIfActionsHaveStatus(final Entity event, StateChangeContext stateChangeContext) {
        List<Entity> actionsForEvent = event.getHasManyField(PlannedEventFields.ACTIONS);
        if (actionsForEvent.stream().anyMatch(
                action -> StringUtils.isEmpty(action.getStringField(ActionForPlannedEventFields.STATE)))) {
            stateChangeContext.addValidationError("cmmsMachineParts.plannedEvent.state.actionsWithoutState");
        }
    }

    private void checkIfRequiredFieldsAreSet(Entity event, StateChangeContext stateChangeContext) {
        boolean areSet = true;
        if (event.getBelongsToField(PlannedEventFields.OWNER) == null) {
            stateChangeContext.addFieldValidationError(PlannedEventFields.OWNER,
                    "cmmsMachineParts.plannedEvent.state.fieldRequired");
            areSet = false;
        }
        if (event.getIntegerField(PlannedEventFields.DURATION) == null
                || event.getIntegerField(PlannedEventFields.DURATION).equals(0)) {
            if (durationIsNotHidden(event)) {
                stateChangeContext.addFieldValidationError(PlannedEventFields.DURATION,
                        "cmmsMachineParts.plannedEvent.state.fieldRequired");
                areSet = false;
            }
        }
        String basedOn = event.getStringField(PlannedEventFields.BASED_ON);
        if (basedOn.equals(PlannedEventBasedOn.DATE.getStringValue())) {
            if (event.getDateField(PlannedEventFields.DATE) == null) {
                stateChangeContext.addFieldValidationError(PlannedEventFields.DATE,
                        "cmmsMachineParts.plannedEvent.state.fieldRequired");
                areSet = false;
            }
        } else if (basedOn.equals(PlannedEventBasedOn.COUNTER.getStringValue())) {
            if (event.getDecimalField(PlannedEventFields.COUNTER) == null) {
                stateChangeContext.addFieldValidationError(PlannedEventFields.COUNTER,
                        "cmmsMachineParts.plannedEvent.state.fieldRequired");
                areSet = false;
            }
        }
        if (event.getHasManyField(PlannedEventFields.RESPONSIBLE_WORKERS).isEmpty()) {
            stateChangeContext.addFieldValidationError(PlannedEventFields.RESPONSIBLE_WORKERS,
                    "cmmsMachineParts.plannedEvent.state.fieldRequired");
            areSet = false;
        }
        if (!areSet) {
            stateChangeContext.addValidationError("cmmsMachineParts.plannedEvent.state.fillRequiredFields");
        }
    }

    private boolean durationIsNotHidden(Entity plannedEvent) {
        return fieldIsNotHidden(plannedEvent, PlannedEventFields.DURATION);
    }

    private boolean solutionDescriptionIsNotHidden(Entity plannedEvent) {
        return tabIsNotHidden(plannedEvent, PlannedEventFields.SOLUTION_DESCRIPTION_TAB);
    }

    private boolean hasNoActions(Entity event) {
        return event.getHasManyField(PlannedEventFields.ACTIONS).isEmpty();
    }

    private boolean fieldIsNotHidden(Entity plannedEvent, String fieldName) {
        PlannedEventType type = PlannedEventType.from(plannedEvent);
        FieldsForType fieldsForType = eventFieldsForTypeFactory.createFieldsForType(type);
        return !fieldsForType.getHiddenFields().contains(fieldName);
    }

    private boolean tabIsNotHidden(Entity plannedEvent, String tabName) {
        PlannedEventType type = PlannedEventType.from(plannedEvent);
        FieldsForType fieldsForType = eventFieldsForTypeFactory.createFieldsForType(type);
        return !fieldsForType.getHiddenTabs().contains(tabName);
    }

    public void validationOnCanceled(StateChangeContext stateChangeContext) {

    }

    public void validationOnInEditing(StateChangeContext stateChangeContext) {
        Entity event = stateChangeContext.getOwner();
        checkIfActionsHaveStatus(event, stateChangeContext);
    }
}
