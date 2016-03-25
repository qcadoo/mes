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
package com.qcadoo.mes.cmmsMachineParts.listeners;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.cmmsMachineParts.MaintenanceEventContextService;
import com.qcadoo.mes.cmmsMachineParts.constants.CmmsMachinePartsConstants;
import com.qcadoo.mes.cmmsMachineParts.constants.MaintenanceEventFields;
import com.qcadoo.mes.cmmsMachineParts.constants.PlannedEventBasedOn;
import com.qcadoo.mes.cmmsMachineParts.constants.PlannedEventFields;
import com.qcadoo.mes.cmmsMachineParts.states.MaintenanceEventStateChangeViewClient;
import com.qcadoo.mes.cmmsMachineParts.states.aop.MaintenanceEventStateChangeAspect;
import com.qcadoo.mes.cmmsMachineParts.states.constants.MaintenanceEventStateChangeFields;
import com.qcadoo.mes.cmmsMachineParts.validators.MaintenanceEventStateChangeValidators;
import com.qcadoo.mes.states.StateChangeContext;
import com.qcadoo.mes.states.constants.StateChangeStatus;
import com.qcadoo.mes.states.messages.constants.StateMessageType;
import com.qcadoo.mes.states.service.StateChangeContextBuilder;
import com.qcadoo.mes.states.service.client.util.ViewContextHolder;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.utils.NumberGeneratorService;

@Service
public class MaintenanceEventToPlannedEventListeners {

    @Autowired
    private MaintenanceEventStateChangeAspect maintenanceEventStateChangeAspect;

    @Autowired
    private StateChangeContextBuilder stateChangeContextBuilder;

    @Autowired
    private MaintenanceEventStateChangeViewClient maintenanceEventStateChangeViewClient;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NumberGeneratorService numberGeneratorService;

    @Autowired
    private MaintenanceEventContextService maintenanceEventContextService;

    @Autowired
    private MaintenanceEventStateChangeValidators maintenanceEventStateChangeValidators;

    public void continueStateChange(final ViewDefinitionState view, final ComponentState component, final String[] args) {
        final FormComponent form = (FormComponent) component;
        Entity eventStateChange = form.getPersistedEntityWithIncludedFormValues();
        maintenanceEventStateChangeValidators.validate(eventStateChange.getDataDefinition(), eventStateChange);
        form.setEntity(eventStateChange);
        if (!eventStateChange.getErrors().isEmpty()) {
            return;
        }

        form.performEvent(view, "save");
        if (!form.isValid()) {
            return;
        }

        final Entity stateChangeEntity = ((FormComponent) form).getEntity();
        final String plannedEventType = stateChangeEntity.getStringField(MaintenanceEventStateChangeFields.PLANNED_EVENT_TYPE);
        final StateChangeContext stateContext = stateChangeContextBuilder.build(
                maintenanceEventStateChangeAspect.getChangeEntityDescriber(), stateChangeEntity);
        if (plannedEventType.isEmpty()) {

            stateContext.setStatus(StateChangeStatus.FAILURE);
            stateContext.addFieldMessage("cmmsMachineParts.maintenanceEventStateChange.plannedEventType.required",
                    StateMessageType.FAILURE, MaintenanceEventStateChangeFields.PLANNED_EVENT_TYPE);
            stateContext.save();
            maintenanceEventStateChangeViewClient.showMessages(new ViewContextHolder(view, form), stateContext);
            return;
        }
        String plannedEventCorrect = createPlannedEvent(stateChangeEntity, plannedEventType);
        if (!StringUtils.isEmpty(plannedEventCorrect)) {
            stateContext.setStatus(StateChangeStatus.IN_PROGRESS);
            stateContext.addMessage("cmmsMachineParts.maintenanceEventStateChange.success.plannedEventCreated",
                    StateMessageType.INFO, plannedEventCorrect);
            maintenanceEventStateChangeAspect.changeState(stateContext);
            maintenanceEventStateChangeViewClient.showMessages(new ViewContextHolder(view, form), stateContext);
        } else {
            stateContext.setStatus(StateChangeStatus.FAILURE);
            stateContext.addMessage("cmmsMachineParts.maintenanceEventStateChange.error.plannedEventNotCreated",
                    StateMessageType.FAILURE);
            stateContext.save();
            maintenanceEventStateChangeViewClient.showMessages(new ViewContextHolder(view, form), stateContext);
        }
    }

    private String createPlannedEvent(final Entity stateChange, final String plannedEventType) {
        Entity stateChangeEntity = dataDefinitionService.get(CmmsMachinePartsConstants.PLUGIN_IDENTIFIER,
                CmmsMachinePartsConstants.MODEL_MAINTENANCE_EVENT_STATE_CHANGE).get(stateChange.getId());
        Entity maintenanceEvent = stateChangeEntity.getBelongsToField(MaintenanceEventStateChangeFields.MAINTENANCE_EVENT);

        Entity plannedEvent = dataDefinitionService.get(CmmsMachinePartsConstants.PLUGIN_IDENTIFIER,
                CmmsMachinePartsConstants.MODEL_PLANNED_EVENT).create();

        String number = numberGeneratorService.generateNumber(CmmsMachinePartsConstants.PLUGIN_IDENTIFIER,
                CmmsMachinePartsConstants.MODEL_PLANNED_EVENT);
        String description = maintenanceEvent.getStringField(MaintenanceEventFields.DESCRIPTION);

        plannedEvent.setField(PlannedEventFields.NUMBER, number);
        plannedEvent.setField(PlannedEventFields.TYPE, plannedEventType);
        plannedEvent.setField(PlannedEventFields.FACTORY, maintenanceEvent.getBelongsToField(PlannedEventFields.FACTORY));
        plannedEvent.setField(PlannedEventFields.DIVISION, maintenanceEvent.getBelongsToField(PlannedEventFields.DIVISION));
        plannedEvent.setField(PlannedEventFields.PRODUCTION_LINE,
                maintenanceEvent.getBelongsToField(PlannedEventFields.PRODUCTION_LINE));
        plannedEvent.setField(PlannedEventFields.WORKSTATION, maintenanceEvent.getBelongsToField(PlannedEventFields.WORKSTATION));
        plannedEvent.setField(PlannedEventFields.SUBASSEMBLY, maintenanceEvent.getBelongsToField(PlannedEventFields.SUBASSEMBLY));
        plannedEvent.setField(PlannedEventFields.SOURCE_COST,
                maintenanceEvent.getBelongsToField(MaintenanceEventFields.SOURCE_COST));
        plannedEvent.setField(PlannedEventFields.DESCRIPTION, description);
        plannedEvent.setField(PlannedEventFields.MAINTENANCE_EVENT, maintenanceEvent);
        plannedEvent.setField(PlannedEventFields.BASED_ON, PlannedEventBasedOn.DATE.getStringValue());

        plannedEvent.setField(PlannedEventFields.PLANNED_SEPARATELY, false);
        plannedEvent.setField(PlannedEventFields.REQUIRES_SHUTDOWN, false);
        Entity context = dataDefinitionService.get(CmmsMachinePartsConstants.PLUGIN_IDENTIFIER,
                CmmsMachinePartsConstants.MODEL_PLANNED_EVENT_CONTEXT).create();
        Entity preparedContext = maintenanceEventContextService.prepareContextEntity(context);
        plannedEvent.setField(PlannedEventFields.PLANNED_EVENT_CONTEXT, preparedContext);
        Entity saved = plannedEvent.getDataDefinition().save(plannedEvent);
        saved.setField("createUser", maintenanceEvent.getStringField("createUser"));
        saved.getDataDefinition().save(saved);
        if (saved.isValid()) {
            return number;
        }
        return StringUtils.EMPTY;
    }

    public void cancelStateChange(final ViewDefinitionState view, final ComponentState form, final String[] args) {
        final Entity stateChangeEntity = ((FormComponent) form).getEntity();

        final StateChangeContext stateContext = stateChangeContextBuilder.build(
                maintenanceEventStateChangeAspect.getChangeEntityDescriber(), stateChangeEntity);
        stateContext.setStatus(StateChangeStatus.CANCELED);
        stateContext.save();

        maintenanceEventStateChangeViewClient.showMessages(new ViewContextHolder(view, form), stateContext);
    }

    public void beforeRenderDialog(final ViewDefinitionState view) {
        final FieldComponent type = (FieldComponent) view
                .getComponentByReference(MaintenanceEventStateChangeFields.PLANNED_EVENT_TYPE);
        type.setRequired(true);
        final FieldComponent typeRequired = (FieldComponent) view
                .getComponentByReference(MaintenanceEventStateChangeFields.PLANNED_EVENT_TYPE_REQUIRED);
        typeRequired.setFieldValue(true);
    }

}
