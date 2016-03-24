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
package com.qcadoo.mes.cmmsMachineParts.hooks;

import com.google.common.base.Strings;
import com.qcadoo.mes.basic.constants.StaffFields;
import com.qcadoo.mes.cmmsMachineParts.constants.ActionForPlannedEventFields;
import com.qcadoo.mes.cmmsMachineParts.constants.PlannedEventFields;
import com.qcadoo.mes.cmmsMachineParts.constants.PlannedEventType;
import com.qcadoo.mes.cmmsMachineParts.plannedEvents.factory.EventFieldsForTypeFactory;
import com.qcadoo.mes.cmmsMachineParts.plannedEvents.fieldsForType.FieldsForType;
import com.qcadoo.mes.cmmsMachineParts.states.constants.PlannedEventState;
import com.qcadoo.mes.cmmsMachineParts.states.constants.PlannedEventStateChangeDescriber;
import com.qcadoo.mes.states.service.StateChangeEntityBuilder;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PlannedEventHooks {

    @Autowired
    private PlannedEventStateChangeDescriber describer;

    @Autowired
    private EventFieldsForTypeFactory fieldsForTypeFactory;

    @Autowired
    private StateChangeEntityBuilder stateChangeEntityBuilder;

    public void onCreate(final DataDefinition eventDD, final Entity event) {
        setInitialState(event);
    }

    public void onCopy(final DataDefinition eventDD, final Entity event) {
        setInitialState(event);
        clearFieldsInCopy(event);
    }

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
        if(event.getId() == null){
            List<Entity> actions =  event.getHasManyField(PlannedEventFields.ACTIONS);
            actions.forEach(a -> clearActions(a));
        }

    }

    private void clearActions(Entity a) {
        a.setField(ActionForPlannedEventFields.STATE, null);
        a.setField(ActionForPlannedEventFields.REASON, null);
    }

    private void clearFieldsInCopy(final Entity event) {

        event.setField(PlannedEventFields.MAINTENANCE_EVENT, null);
        event.setField(PlannedEventFields.RELATED_EVENTS, null);
        event.setField(PlannedEventFields.FINISH_DATE, null);
        event.setField(PlannedEventFields.START_DATE, null);
        event.setField(PlannedEventFields.SOLUTION_DESCRIPTION, null);
        event.setField(PlannedEventFields.IS_DEADLINE, false);
    }

    private void clearHiddenFields(final Entity event) {
        FieldsForType fieldsForType = fieldsForTypeFactory.createFieldsForType(PlannedEventType.from(event));
        List<String> fieldsToClear = fieldsForType.getHiddenFields();
        for (String fieldName : fieldsToClear) {
            if (fieldName.equals(PlannedEventFields.REQUIRES_SHUTDOWN)
                    || fieldName.equals(PlannedEventFields.PLANNED_SEPARATELY)) {
                event.setField(fieldName, false);
            } else {
                event.setField(fieldName, null);
            }
        }

    }

    private void setInitialState(final Entity event) {
        stateChangeEntityBuilder.buildInitial(describer, event, PlannedEventState.NEW);
    }

    public boolean onDelete(final DataDefinition eventDD, final Entity event) {
        if (event.getBelongsToField(PlannedEventFields.MAINTENANCE_EVENT) != null) {
            event.addGlobalError("cmmsMachineParts.plannedEvent.error.cannotDeleteRelatedPlannedEvent");
            return false;
        }
        if (!event.getHasManyField(PlannedEventFields.RELATED_EVENTS).isEmpty()) {
            event.addGlobalError("cmmsMachineParts.plannedEvent.error.cannotDeleteEventWithRelatedEvents");
            return false;
        }
        return true;
    }
}
