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

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import com.google.common.base.Strings;
import com.qcadoo.mes.basic.FaultTypesService;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.basic.constants.SubassemblyFields;
import com.qcadoo.mes.basic.constants.WorkstationFields;
import com.qcadoo.mes.cmmsMachineParts.MaintenanceEventContextService;
import com.qcadoo.mes.cmmsMachineParts.MaintenanceEventService;
import com.qcadoo.mes.cmmsMachineParts.SourceCostService;
import com.qcadoo.mes.cmmsMachineParts.constants.CmmsMachinePartsConstants;
import com.qcadoo.mes.cmmsMachineParts.constants.MaintenanceEventContextFields;
import com.qcadoo.mes.cmmsMachineParts.constants.MaintenanceEventFields;
import com.qcadoo.mes.cmmsMachineParts.constants.MaintenanceEventType;
import com.qcadoo.mes.cmmsMachineParts.constants.ParameterFieldsCMP;
import com.qcadoo.mes.cmmsMachineParts.constants.PlannedEventBasedOn;
import com.qcadoo.mes.cmmsMachineParts.constants.PlannedEventFields;
import com.qcadoo.mes.cmmsMachineParts.constants.PlannedEventType;
import com.qcadoo.mes.cmmsMachineParts.roles.EventRoles;
import com.qcadoo.mes.cmmsMachineParts.states.constants.MaintenanceEventState;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.security.api.SecurityService;
import com.qcadoo.security.api.UserService;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.CheckBoxComponent;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;
import com.qcadoo.view.api.ribbon.Ribbon;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.api.ribbon.RibbonGroup;
import com.qcadoo.view.api.utils.NumberGeneratorService;

@Service
public class EventHooks {

    private static final String L_FORM = "form";

    private static final String L_WINDOW = "window";

    @Autowired
    private MaintenanceEventService maintenanceEventService;

    @Autowired
    private FaultTypesService faultTypesService;

    @Autowired
    private NumberGeneratorService numberGeneratorService;

    @Autowired
    private MaintenanceEventContextService maintenanceEventContextService;

    @Autowired
    private SourceCostService sourceCostService;

    @Autowired
    private UserService userService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private ParameterService parameterService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void maintenanceEventBeforeRender(final ViewDefinitionState view) {
        FieldComponent acceptanceEvents = (FieldComponent) view.getComponentByReference(MaintenanceEventFields.ACCEPTANCE_EVENTS);
        acceptanceEvents.setFieldValue(parameterService.getParameter().getBooleanField(ParameterFieldsCMP.ACCEPTANCE_EVENTS));
        setUpFaultTypeLookup(view);
        setFieldsRequired(view);
        fillDefaultFields(view);
        fillDefaultFieldsFromContext(view, MaintenanceEventFields.MAINTENANCE_EVENT_CONTEXT);
        toggleEnabledViewComponents(view, MaintenanceEventFields.MAINTENANCE_EVENT_CONTEXT);
        disableFieldsForState(view);
        toggleOldSolutionsButton(view);
        enableShowPlannedEvent(view);
        hideAccordingToRole(view);
        setEventCriteriaModifiers(view);
        lockNumberField(view);
        fetchNumberFromDatabase(view);
        manageSoundNotificationsField(view);
    }

    private void manageSoundNotificationsField(final ViewDefinitionState view) {
        FormComponent form = (FormComponent) view.getComponentByReference(L_FORM);
        Entity event = form.getPersistedEntityWithIncludedFormValues();
        String type = event.getStringField(MaintenanceEventFields.TYPE);
        String eventState = event.getStringField(MaintenanceEventFields.STATE);
        boolean canEnableSoundNotificationsField = canEnableSoundNotificationsField(view, type, eventState);
        CheckBoxComponent soundNotificationComponent = (CheckBoxComponent) view
                .getComponentByReference(MaintenanceEventFields.SOUND_NOTIFICATIONS);
        if (form.getEntityId() == null && view.isViewAfterRedirect() && canEnableSoundNotificationsField) {
            soundNotificationComponent.setChecked(true);
        } else if (MaintenanceEventType.PROPOSAL.getStringValue().equals(type)) {
            soundNotificationComponent.setChecked(false);
        }
        soundNotificationComponent.setEnabled(canEnableSoundNotificationsField);
        soundNotificationComponent.requestComponentUpdateState();
    }

    private boolean canEnableSoundNotificationsField(ViewDefinitionState view, String type, String eventState) {
        return (MaintenanceEventType.FAILURE.getStringValue().equals(type) || MaintenanceEventType.ISSUE.getStringValue().equals(
                type))
                && MaintenanceEventState.NEW.getStringValue().equals(eventState);
    }

    private void lockNumberField(final ViewDefinitionState view) {
        FieldComponent numberFieldComponent = (FieldComponent) view.getComponentByReference(MaintenanceEventFields.NUMBER);
        numberFieldComponent.setEnabled(false);
    }

    private void fetchNumberFromDatabase(final ViewDefinitionState view) {
        FormComponent formComponent = (FormComponent) view.getComponentByReference(L_FORM);

        if (formComponent.getEntityId() != null) {
            ComponentState numberField = view.getComponentByReference(MaintenanceEventFields.NUMBER);

            String numberFieldValue = (String) numberField.getFieldValue();

            if (Strings.isNullOrEmpty(numberFieldValue)) {
                DataDefinition maintenanceEventDD = dataDefinitionService.get(CmmsMachinePartsConstants.PLUGIN_IDENTIFIER,
                        CmmsMachinePartsConstants.MODEL_MAINTENANCE_EVENT);

                Entity maintenanceEvent = maintenanceEventDD.get(formComponent.getEntityId());

                numberField.setFieldValue(maintenanceEvent.getField(MaintenanceEventFields.NUMBER));
            }
        }
    }

    public void plannedEventBeforeRender(final ViewDefinitionState view) {
        generateNumber(view, CmmsMachinePartsConstants.MODEL_PLANNED_EVENT, PlannedEventFields.NUMBER);
        fillDefaultFieldsFromContext(view, PlannedEventFields.PLANNED_EVENT_CONTEXT);
        toggleEnabledViewComponents(view, PlannedEventFields.PLANNED_EVENT_CONTEXT);
        toggleEnabledFromBasedOn(view);
        enableShowMaintenanceEvent(view);
        disableCopyButtonForAfterReview(view);
        setEventCriteriaModifiers(view);
    }

    public void toggleEnabledFromBasedOn(final ViewDefinitionState view) {
        FormComponent form = (FormComponent) view.getComponentByReference(L_FORM);
        Entity event = form.getPersistedEntityWithIncludedFormValues();
        String basedOn = event.getStringField(PlannedEventFields.BASED_ON);
        FieldComponent date = (FieldComponent) view.getComponentByReference(PlannedEventFields.DATE);
        FieldComponent counter = (FieldComponent) view.getComponentByReference(PlannedEventFields.COUNTER);

        if (basedOn.equals(PlannedEventBasedOn.DATE.getStringValue())) {
            date.setEnabled(true);
            counter.setEnabled(false);
            counter.setFieldValue(null);
        } else if (basedOn.equals(PlannedEventBasedOn.COUNTER.getStringValue())) {
            date.setEnabled(false);
            date.setFieldValue(null);
            counter.setEnabled(true);
        }
    }

    private void disableCopyButtonForAfterReview(final ViewDefinitionState view) {
        FormComponent form = (FormComponent) view.getComponentByReference(L_FORM);
        Entity event = form.getPersistedEntityWithIncludedFormValues();

        if (PlannedEventType.from(event).equals(PlannedEventType.AFTER_REVIEW)) {
            toggleRibbonButton(view, "actions", "copy", false);
        }
    }

    private void enableShowPlannedEvent(final ViewDefinitionState view) {
        FormComponent form = (FormComponent) view.getComponentByReference(L_FORM);

        Entity event = form.getPersistedEntityWithIncludedFormValues();

        Optional<Entity> plannedEvent = maintenanceEventService.getPlannedEventForMaintenanceEvent(event);

        if (plannedEvent.isPresent()) {
            toggleRibbonButton(view, "plannedEvents", "showPlannedEvent", true);
        } else {
            toggleRibbonButton(view, "plannedEvents", "showPlannedEvent", false);
        }
    }

    private void enableShowMaintenanceEvent(final ViewDefinitionState view) {
        FormComponent form = (FormComponent) view.getComponentByReference(L_FORM);

        Entity event = form.getPersistedEntityWithIncludedFormValues();

        Entity maintenanceEvent = event.getBelongsToField(PlannedEventFields.MAINTENANCE_EVENT);

        if (maintenanceEvent != null) {
            toggleRibbonButton(view, "maintenanceEvents", "showMaintenanceEvent", true);
        } else {
            toggleRibbonButton(view, "maintenanceEvents", "showMaintenanceEvent", false);
        }
    }

    private void toggleRibbonButton(final ViewDefinitionState view, String groupName, String itemName, boolean enabled) {
        WindowComponent window = (WindowComponent) view.getComponentByReference("window");
        Ribbon ribbon = window.getRibbon();
        RibbonGroup group = ribbon.getGroupByName(groupName);
        RibbonActionItem item = group.getItemByName(itemName);

        item.setEnabled(enabled);
        item.requestUpdate(true);
    }

    private void disableFieldsForState(final ViewDefinitionState view) {
        FormComponent form = (FormComponent) view.getComponentByReference(L_FORM);
        Entity event = form.getPersistedEntityWithIncludedFormValues();
        MaintenanceEventState state = MaintenanceEventState.of(event);

        if (state.compareTo(MaintenanceEventState.CLOSED) == 0 || state.compareTo(MaintenanceEventState.REVOKED) == 0
                || state.compareTo(MaintenanceEventState.PLANNED) == 0) {
            form.setFormEnabled(false);

            GridComponent staffWorkTimes = (GridComponent) view.getComponentByReference(MaintenanceEventFields.STAFF_WORK_TIMES);
            GridComponent machineParts = (GridComponent) view
                    .getComponentByReference(MaintenanceEventFields.MACHINE_PARTS_FOR_EVENT);

            staffWorkTimes.setEnabled(false);
            machineParts.setEnabled(false);
        }
    }

    private void fillDefaultFields(final ViewDefinitionState view) {
        FormComponent form = (FormComponent) view.getComponentByReference(L_FORM);
        Entity event = form.getPersistedEntityWithIncludedFormValues();
        String type = event.getStringField(MaintenanceEventFields.TYPE);
        LookupComponent faultType = (LookupComponent) view.getComponentByReference(MaintenanceEventFields.FAULT_TYPE);

        if (type.compareTo(MaintenanceEventType.PROPOSAL.getStringValue()) == 0) {
            if (faultType.getFieldValue() == null) {
                faultType.setFieldValue(faultTypesService.getDefaultFaultType().getId());
            }

            faultType.setEnabled(false);
        }
    }

    private void generateNumber(final ViewDefinitionState view, String modelName, String fieldName) {
        if (numberGeneratorService.checkIfShouldInsertNumber(view, L_FORM, fieldName)) {
            numberGeneratorService.generateAndInsertNumber(view, CmmsMachinePartsConstants.PLUGIN_IDENTIFIER, modelName, L_FORM,
                    fieldName);
        }
    }

    public void fillDefaultFieldsFromContext(final ViewDefinitionState view, String contextField) {
        FormComponent form = (FormComponent) view.getComponentByReference(L_FORM);

        if (form.getEntityId() == null) {
            Entity event = form.getEntity();
            Entity eventContext = event.getBelongsToField(contextField);

            if (eventContext != null) {
                Entity factoryEntity = eventContext.getBelongsToField(MaintenanceEventContextFields.FACTORY);

                if (factoryEntity != null) {
                    FieldComponent factoryField = (FieldComponent) view.getComponentByReference(MaintenanceEventFields.FACTORY);

                    factoryField.setFieldValue(factoryEntity.getId());
                    factoryField.requestComponentUpdateState();

                    fillSourceCost(view, factoryEntity);
                } else {
                    LookupComponent sc = (LookupComponent) view.getComponentByReference("sourceCost");

                    if (sc.isEmpty()) {
                        fillSourceCost(view);
                    }
                }

                Entity divisionEntity = eventContext.getBelongsToField(MaintenanceEventContextFields.DIVISION);

                if (divisionEntity != null) {
                    FieldComponent divisionField = (FieldComponent) view.getComponentByReference(MaintenanceEventFields.DIVISION);

                    divisionField.setFieldValue(divisionEntity.getId());
                    divisionField.requestComponentUpdateState();
                }
            }
        }
    }

    public void fillSourceCost(final ViewDefinitionState view, final Entity factoryEntity) {
        Optional<Entity> costForFactory = sourceCostService.findDefaultSourceCodeForFactory(factoryEntity);

        if (costForFactory.isPresent()) {
            FieldComponent costField = (FieldComponent) view.getComponentByReference(MaintenanceEventFields.SOURCE_COST);

            costField.setFieldValue(costForFactory.get().getId());
            costField.requestComponentUpdateState();
        } else {
            fillSourceCost(view);
        }
    }

    public void fillSourceCost(final ViewDefinitionState view) {
        Optional<Entity> costForFactory = sourceCostService.findDefaultSourceCode();

        if (costForFactory.isPresent()) {
            FieldComponent costField = (FieldComponent) view.getComponentByReference(MaintenanceEventFields.SOURCE_COST);

            costField.setFieldValue(costForFactory.get().getId());
            costField.requestComponentUpdateState();
        } else {
            FieldComponent costField = (FieldComponent) view.getComponentByReference(MaintenanceEventFields.SOURCE_COST);

            costField.setFieldValue(null);
            costField.requestComponentUpdateState();
        }
    }

    public void toggleEnabledViewComponents(final ViewDefinitionState view, String contextField) {
        FormComponent form = (FormComponent) view.getComponentByReference(L_FORM);

        Entity event = form.getPersistedEntityWithIncludedFormValues();

        toggleEnabledForWorkstation(view, event);
        toggleEnabledForFactory(view, event, contextField);
        toggleEnabledForDivision(view, event, contextField);
        toggleEnabledForSubassembly(view, event);
        toggleEnabledForProductionLine(view, event);
    }

    private void toggleEnabledForWorkstation(final ViewDefinitionState view, final Entity event) {
        boolean enabled = event.getBelongsToField(MaintenanceEventFields.PRODUCTION_LINE) != null;

        LookupComponent workstation = (LookupComponent) view.getComponentByReference(MaintenanceEventFields.WORKSTATION);
        workstation.setEnabled(enabled);
    }

    private void toggleEnabledForProductionLine(final ViewDefinitionState view, final Entity event) {
        boolean enabled = event.getBelongsToField(MaintenanceEventFields.DIVISION) != null;

        LookupComponent productionLine = (LookupComponent) view.getComponentByReference(MaintenanceEventFields.PRODUCTION_LINE);
        productionLine.setEnabled(enabled);
    }

    private void toggleEnabledForFactory(final ViewDefinitionState view, final Entity event, String contextField) {
        if (event.getBelongsToField(contextField) == null) {
            return;
        }

        boolean enabled = event.getBelongsToField(contextField).getBelongsToField(MaintenanceEventContextFields.FACTORY) == null;

        LookupComponent factoryLookup = (LookupComponent) view.getComponentByReference(MaintenanceEventFields.FACTORY);
        factoryLookup.setEnabled(enabled);
    }

    private void toggleEnabledForDivision(final ViewDefinitionState view, final Entity event, String contextField) {
        if (event.getBelongsToField(contextField) == null) {
            return;
        }

        boolean enabled = event.getBelongsToField(contextField).getBelongsToField(MaintenanceEventContextFields.DIVISION) == null;

        LookupComponent divisionLookup = (LookupComponent) view.getComponentByReference(MaintenanceEventFields.DIVISION);
        divisionLookup.setEnabled(enabled);
    }

    private void toggleEnabledForSubassembly(final ViewDefinitionState view, final Entity event) {
        boolean enabled = event.getBelongsToField(MaintenanceEventFields.WORKSTATION) != null;

        LookupComponent subassemblyLookup = (LookupComponent) view.getComponentByReference(MaintenanceEventFields.SUBASSEMBLY);
        subassemblyLookup.setEnabled(enabled);
    }

    private void setFieldsRequired(final ViewDefinitionState view) {
        FieldComponent factory = (FieldComponent) view.getComponentByReference(MaintenanceEventFields.FACTORY);
        FieldComponent division = (FieldComponent) view.getComponentByReference(MaintenanceEventFields.DIVISION);
        FieldComponent faultType = (FieldComponent) view.getComponentByReference(MaintenanceEventFields.FAULT_TYPE);

        factory.setRequired(true);
        division.setRequired(true);
        faultType.setRequired(true);
    }

    public void setEventCriteriaModifiers(ViewDefinitionState view) {
        FormComponent formComponent = (FormComponent) view.getComponentByReference(L_FORM);

        Entity event = formComponent.getPersistedEntityWithIncludedFormValues();

        setEventCriteriaModifier(view, event, MaintenanceEventFields.FACTORY, MaintenanceEventFields.DIVISION);
        setEventCriteriaModifier(view, event, MaintenanceEventFields.DIVISION, MaintenanceEventFields.WORKSTATION);
        setEventCriteriaModifier(view, event, MaintenanceEventFields.DIVISION, MaintenanceEventFields.PRODUCTION_LINE);
        setEventCriteriaModifier(view, event, MaintenanceEventFields.PRODUCTION_LINE, MaintenanceEventFields.WORKSTATION);
        setEventCriteriaModifier(view, event, MaintenanceEventFields.WORKSTATION, MaintenanceEventFields.SUBASSEMBLY);
    }

    private void setEventCriteriaModifier(ViewDefinitionState view, Entity event, String fieldFrom, String fieldTo) {
        LookupComponent lookupComponent = (LookupComponent) view.getComponentByReference(fieldTo);

        Entity value = event.getBelongsToField(fieldFrom);

        if (value != null) {
            FilterValueHolder holder = lookupComponent.getFilterValue();

            holder.put(fieldFrom, value.getId());

            lookupComponent.setFilterValue(holder);
        }
    }

    private void setUpFaultTypeLookup(final ViewDefinitionState view) {
        FormComponent formComponent = (FormComponent) view.getComponentByReference(L_FORM);
        LookupComponent faultTypeLookup = (LookupComponent) view.getComponentByReference(MaintenanceEventFields.FAULT_TYPE);

        Entity event = formComponent.getPersistedEntityWithIncludedFormValues();
        Entity workstation = event.getBelongsToField(MaintenanceEventFields.WORKSTATION);
        Entity subassembly = event.getBelongsToField(MaintenanceEventFields.SUBASSEMBLY);

        FilterValueHolder filter = faultTypeLookup.getFilterValue();

        if (workstation == null) {
            filter.remove(MaintenanceEventFields.WORKSTATION);
            filter.remove(WorkstationFields.WORKSTATION_TYPE);
            filter.remove(MaintenanceEventFields.SUBASSEMBLY);
        } else {
            filter.put(MaintenanceEventFields.WORKSTATION, workstation.getId());

            if (subassembly == null) {
                filter.remove(MaintenanceEventFields.SUBASSEMBLY);

                Entity workstationType = workstation.getBelongsToField(WorkstationFields.WORKSTATION_TYPE);

                filter.put(WorkstationFields.WORKSTATION_TYPE, workstationType.getId());
            } else {
                Entity workstationType = subassembly.getBelongsToField(SubassemblyFields.WORKSTATION_TYPE);

                filter.put(MaintenanceEventFields.SUBASSEMBLY, subassembly.getId());
                filter.put(WorkstationFields.WORKSTATION_TYPE, workstationType.getId());
            }
        }

        faultTypeLookup.setFilterValue(filter);
    }

    public void setEventIdForMultiUploadField(final ViewDefinitionState view) {
        FormComponent technology = (FormComponent) view.getComponentByReference(L_FORM);
        FieldComponent technologyIdForMultiUpload = (FieldComponent) view.getComponentByReference("eventIdForMultiUpload");
        FieldComponent technologyMultiUploadLocale = (FieldComponent) view.getComponentByReference("eventMultiUploadLocale");

        if (technology.getEntityId() != null) {
            technologyIdForMultiUpload.setFieldValue(technology.getEntityId());
            technologyIdForMultiUpload.requestComponentUpdateState();
        } else {
            technologyIdForMultiUpload.setFieldValue("");
            technologyIdForMultiUpload.requestComponentUpdateState();
        }

        technologyMultiUploadLocale.setFieldValue(LocaleContextHolder.getLocale());
        technologyMultiUploadLocale.requestComponentUpdateState();
    }

    private void toggleOldSolutionsButton(ViewDefinitionState view) {
        WindowComponent windowComponent = (WindowComponent) view.getComponentByReference("window");
        Ribbon ribbon = windowComponent.getRibbon();
        RibbonGroup solutionsRibbonGroup = ribbon.getGroupByName("solutions");
        RibbonActionItem showSolutionsRibbonActionItem = solutionsRibbonGroup.getItemByName("showSolutions");

        FormComponent formComponent = (FormComponent) view.getComponentByReference(L_FORM);
        Entity event = formComponent.getPersistedEntityWithIncludedFormValues();

        showSolutionsRibbonActionItem.setEnabled(event.getId() != null);
        showSolutionsRibbonActionItem.requestUpdate(true);
    }

    public final void onBeforeRenderListView(final ViewDefinitionState view) {
        FieldComponent acceptanceEvents = (FieldComponent) view.getComponentByReference(PlannedEventFields.ACCEPTANCE_EVENTS);
        acceptanceEvents.setFieldValue(parameterService.getParameter().getBooleanField(ParameterFieldsCMP.ACCEPTANCE_EVENTS));

        maintenanceEventContextService.beforeRenderListView(view);
        hideAccordingToRole(view);
    }

    public final void onBeforeRenderPlannedListView(final ViewDefinitionState view) {
        FieldComponent acceptanceEvents = (FieldComponent) view.getComponentByReference(PlannedEventFields.ACCEPTANCE_EVENTS);
        acceptanceEvents.setFieldValue(parameterService.getParameter().getBooleanField(ParameterFieldsCMP.ACCEPTANCE_EVENTS));

        maintenanceEventContextService.beforeRenderListView(view);
    }

    public void hideAccordingToRole(ViewDefinitionState view) {
        Entity user = userService.getCurrentUserEntity();

        for (EventRoles role : EventRoles.values()) {
            if (!securityService.hasRole(user, role.toString())) {
                role.disableFieldsWhenNotInRole(view);
            }
        }

        if (!parameterService.getParameter().getBooleanField(ParameterFieldsCMP.ACCEPTANCE_EVENTS)) {
            if (!securityService.hasRole(user, EventRoles.ROLE_EVENTS_CLOSE.toString())) {
                enableCloseEvents(view, false);
            } else {
                enableCloseEvents(view, true);
            }
        }
    }

    private void enableCloseEvents(final ViewDefinitionState view, final boolean enable) {
        if (eventInState(view, MaintenanceEventState.EDITED) || eventInState(view, MaintenanceEventState.ACCEPTED)) {
            enableFromRibbonGroup(view, enable, "status", "closeEvent");
        }
    }

    private boolean eventInState(final ViewDefinitionState view, final MaintenanceEventState state) {
        FormComponent form = (FormComponent) view.getComponentByReference(L_FORM);

        Entity event = form.getEntity();
        String eventState = event.getStringField(MaintenanceEventFields.STATE);

        if (eventState == null) {
            GridComponent grid = (GridComponent) view.getComponentByReference("grid");
            List<Entity> entities = grid.getSelectedEntities();

            if (entities.isEmpty()) {
                return false;
            }

            return entities.stream().allMatch(e -> state.getStringValue().equals(e.getStringField(MaintenanceEventFields.STATE)));
        }

        return state.getStringValue().equals(eventState);
    }

    private void enableFromRibbonGroup(final ViewDefinitionState view, final boolean enable, final String groupName,
            String... items) {
        WindowComponent window = (WindowComponent) view.getComponentByReference(L_WINDOW);
        Ribbon ribbon = window.getRibbon();
        RibbonGroup ribbonGroup = ribbon.getGroupByName(groupName);

        if (ribbonGroup != null) {
            for (String item : items) {
                RibbonActionItem ribbonItem = ribbonGroup.getItemByName(item);

                if (ribbonItem != null) {
                    ribbonItem.setEnabled(enable);
                    ribbonItem.requestUpdate(true);
                }
            }
        }
    }

}
