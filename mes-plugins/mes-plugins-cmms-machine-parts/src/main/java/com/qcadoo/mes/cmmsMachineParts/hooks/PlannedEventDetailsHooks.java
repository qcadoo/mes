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

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.cmmsMachineParts.constants.ParameterFieldsCMP;
import com.qcadoo.mes.cmmsMachineParts.constants.PlannedEventBasedOn;
import com.qcadoo.mes.cmmsMachineParts.constants.PlannedEventFields;
import com.qcadoo.mes.cmmsMachineParts.constants.PlannedEventType;
import com.qcadoo.mes.cmmsMachineParts.plannedEvents.factory.EventFieldsForTypeFactory;
import com.qcadoo.mes.cmmsMachineParts.plannedEvents.fieldsForType.*;
import com.qcadoo.mes.cmmsMachineParts.roles.PlannedEventRoles;
import com.qcadoo.mes.cmmsMachineParts.states.constants.PlannedEventState;
import com.qcadoo.mes.cmmsMachineParts.states.constants.PlannedEventStateStringValues;
import com.qcadoo.model.api.Entity;
import com.qcadoo.security.api.SecurityService;
import com.qcadoo.security.api.UserService;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.*;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;
import com.qcadoo.view.api.ribbon.Ribbon;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.api.ribbon.RibbonGroup;
import com.qcadoo.view.internal.components.select.SelectComponentState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

@Service
public class PlannedEventDetailsHooks {

    private static final String L_GRID = "grid";

    private static final String L_FORM = "form";

    private static final String L_WINDOW = "window";

    private static final String L_ACTIONS = "actions";

    private static final String L_STATUS = "status";

    private static final String L_REALIZED_EVENT = "realizedEvent";

    private static final String L_RELATED_EVENT_LOOKUP = "relatedEventLookup";

    private static final List<String> L_GRIDS = Lists.newArrayList(PlannedEventFields.RELATED_EVENTS, PlannedEventFields.ACTIONS,
            PlannedEventFields.RESPONSIBLE_WORKERS, PlannedEventFields.REALIZATIONS, PlannedEventFields.MACHINE_PARTS_FOR_EVENT);
    public static final String L_EVENT_ID_FOR_MULTI_UPLOAD = "eventIdForMultiUpload";
    public static final String L_EVENT_MULTI_UPLOAD_LOCALE = "eventMultiUploadLocale";

    private List<String> previouslyHiddenTabs = Lists.newArrayList();

    @Autowired
    private EventFieldsForTypeFactory eventFieldsForTypeFactory;

    @Autowired
    private EventHooks eventHooks;

    @Autowired
    private UserService userService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private TranslationService translationService;

    @Autowired
    private ParameterService parameterService;

    public void plannedEventBeforeRender(final ViewDefinitionState view) {
        FormComponent plannedEventForm = (FormComponent) view.getComponentByReference(L_FORM);
        plannedEventForm.setFormEnabled(true);

        FieldComponent acceptanceEventsField = (FieldComponent) view.getComponentByReference(PlannedEventFields.ACCEPTANCE_EVENTS);
        acceptanceEventsField.setFieldValue(parameterService.getParameter().getBooleanField(ParameterFieldsCMP.ACCEPTANCE_EVENTS));

        eventHooks.plannedEventBeforeRender(view);

        Entity plannedEvent = plannedEventForm.getEntity();

        FieldComponent typeField = (FieldComponent) view.getComponentByReference(PlannedEventFields.TYPE);

        typeField.setEnabled(plannedEvent.getId() == null);

        setCriteriaModifiers(view, plannedEvent);

        // TODO dev_team - very very ugly way to fix issue GOODFOOD-742, should be fixed more properly
        // if problem with manyToMany in related entity will happen once more
        Object relatedEvents = plannedEvent.getField(PlannedEventFields.RELATED_EVENTS);

        if ((relatedEvents != null) && relatedEvents.getClass().equals(Integer.class)) {
            lockView(view);
        }

        processRoles(view);
        disableFieldsForState(view);
        setUnit(view);
    }

    private void lockView(final ViewDefinitionState view) {
        FormComponent plannedEventForm = (FormComponent) view.getComponentByReference(L_FORM);
        WindowComponent window = (WindowComponent) view.getComponentByReference(L_WINDOW);

        Ribbon ribbon = window.getRibbon();
        RibbonGroup actionsRibbonGroup = ribbon.getGroupByName(L_ACTIONS);
        RibbonGroup statusRibbonGroup = ribbon.getGroupByName(L_STATUS);

        List<RibbonActionItem> ribbonActionItems = actionsRibbonGroup.getItems();

        ribbonActionItems.addAll(statusRibbonGroup.getItems());

        for (RibbonActionItem ribbonActionItem : ribbonActionItems) {
            ribbonActionItem.setEnabled(false);
            ribbonActionItem.requestUpdate(true);
        }

        for (String referenceName : L_GRIDS) {
            lockGrid(view, referenceName);
        }

        plannedEventForm.setFormEnabled(false);
    }

    private void lockGrid(final ViewDefinitionState view, final String referenceName) {
        GridComponent gridComponent = (GridComponent) view.getComponentByReference(referenceName);
        gridComponent.setEnabled(false);
    }

    private void setCriteriaModifiers(final ViewDefinitionState view, final Entity plannedEvent) {
        LookupComponent relatedEventLookup = (LookupComponent) view.getComponentByReference(L_RELATED_EVENT_LOOKUP);

        FilterValueHolder filterValueHolder = relatedEventLookup.getFilterValue();
        filterValueHolder.put(PlannedEventFields.NUMBER, plannedEvent.getStringField(PlannedEventFields.NUMBER));

        relatedEventLookup.setFilterValue(filterValueHolder);
    }

    public void toggleFieldsVisible(final ViewDefinitionState view) {
        FormComponent plannedEventForm = (FormComponent) view.getComponentByReference(L_FORM);

        Entity plannedEvent = plannedEventForm.getPersistedEntityWithIncludedFormValues();

        PlannedEventType type = PlannedEventType.from(plannedEvent);

        FieldsForType fieldsForType = eventFieldsForTypeFactory.createFieldsForType(type);

        if (fieldsForType == null) {
            return;
        }

        hideFields(view, plannedEvent, fieldsForType);
        hideTabs(view, fieldsForType);
        clearGrids(view, fieldsForType);
        setAndLockBasedOn(view, fieldsForType);
        actionsButtonProcess(plannedEvent, view, fieldsForType);
    }

    private void actionsButtonProcess(Entity plannedEvent, ViewDefinitionState view, FieldsForType ftype) {
        WindowComponent window = (WindowComponent) view.getComponentByReference(L_WINDOW);
        PlannedEventType type = PlannedEventType.from(plannedEvent);
        Ribbon ribbon = window.getRibbon();
        RibbonGroup actionsGroup = ribbon.getGroupByName("actionsGroup");
        RibbonActionItem addActionsItem = actionsGroup.getItemByName("actions");
        if(plannedEvent.getId() == null){
            addActionsItem.setEnabled(false);
            addActionsItem.requestUpdate(true);
            window.requestRibbonRender();
            return;
        }

        boolean enable = false;

        if (type.compareTo(PlannedEventType.REVIEW) == 0) {
            enable = true;
        } else if (type.compareTo(PlannedEventType.REPAIRS) == 0) {
            enable = true;
        } else if (type.compareTo(PlannedEventType.EXTERNAL_SERVICE) == 0) {
            enable = false;
        } else if (type.compareTo(PlannedEventType.ADDITIONAL_WORK) == 0) {
            enable = true;
        } else if (type.compareTo(PlannedEventType.MANUAL) == 0) {
            enable = true;
        } else if (type.compareTo(PlannedEventType.METER_READING) == 0) {
            enable = false;
        } else if (type.compareTo(PlannedEventType.UDT_REVIEW) == 0) {
            enable = false;
        } else if (type.compareTo(PlannedEventType.AFTER_REVIEW) == 0) {
            enable = false;
        }

        addActionsItem.setEnabled(enable);
        addActionsItem.requestUpdate(true);
        window.requestRibbonRender();
    }

    public void toggleActionsFieldsVisible(final ViewDefinitionState view) {
        FormComponent plannedEventForm = (FormComponent) view.getComponentByReference(L_FORM);

        Entity plannedEvent = plannedEventForm.getPersistedEntityWithIncludedFormValues();

        PlannedEventType type = PlannedEventType.from(plannedEvent);

        FieldsForType fieldsForType = eventFieldsForTypeFactory.createFieldsForType(type);

        if (fieldsForType == null) {
            return;
        }
        toggleAddMultipleActions(view, fieldsForType, plannedEvent);
    }
    public void hideFields(final ViewDefinitionState view, final Entity plannedEvent, final FieldsForType fieldsForType) {
        Set<String> allFields = plannedEvent.getDataDefinition().getFields().keySet();

        List<String> hiddenFields = fieldsForType.getHiddenFields();

        for (String fieldName : allFields) {
            Optional<ComponentState> maybeFieldComponent = view.tryFindComponentByReference(fieldName);

            if (maybeFieldComponent.isPresent() && !PlannedEventFields.STATE.equals(fieldName)) {
                ComponentState fieldComponent = maybeFieldComponent.get();

                if (hiddenFields.contains(fieldName)) {
                    fieldComponent.setVisible(false);
                } else {
                    fieldComponent.setVisible(true);
                }
            }
        }
    }

    private void hideTabs(final ViewDefinitionState view, final FieldsForType fieldsForType) {
        List<String> hiddenTabs = fieldsForType.getHiddenTabs();

        for (String tab : previouslyHiddenTabs) {
            ComponentState tabComponent = view.getComponentByReference(tab);

            if (tabComponent != null) {
                tabComponent.setVisible(true);
            }
        }

        for (String tab : hiddenTabs) {
            ComponentState tabComponent = view.getComponentByReference(tab);

            if (tabComponent != null) {
                tabComponent.setVisible(false);
            }
        }

        previouslyHiddenTabs = hiddenTabs;
    }

    private void clearGrids(final ViewDefinitionState view, final FieldsForType fieldsForType) {
        List<String> referenceNames = fieldsForType.getGridsToClear();

        referenceNames.stream().forEach(referenceName -> {
            GridComponent gridComponent = (GridComponent) view.getComponentByReference(referenceName);

            if (gridComponent != null) {
                gridComponent.setEntities(Lists.newArrayList());
            }
        });
    }

    public void setAndLockBasedOn(final ViewDefinitionState view, final FieldsForType fieldsForType) {
        FieldComponent basedOnField = (FieldComponent) view.getComponentByReference(PlannedEventFields.BASED_ON);

        if (fieldsForType.shouldLockBasedOn()) {
            basedOnField.setFieldValue(PlannedEventBasedOn.DATE.getStringValue());
            basedOnField.setEnabled(false);
        } else {
            basedOnField.setEnabled(true);
        }
    }

    private void disableFieldsForState(final ViewDefinitionState view) {
        FormComponent plannedEventForm = (FormComponent) view.getComponentByReference(L_FORM);

        Entity event = plannedEventForm.getPersistedEntityWithIncludedFormValues();

        PlannedEventState state = PlannedEventState.of(event);

        if (PlannedEventState.CANCELED.equals(state) || PlannedEventState.REALIZED.equals(state)) {
            plannedEventForm.setFormEnabled(false);

            lockGrids(view, Lists.newArrayList(PlannedEventFields.RESPONSIBLE_WORKERS, PlannedEventFields.RELATED_EVENTS,
                    PlannedEventFields.REALIZATIONS, PlannedEventFields.MACHINE_PARTS_FOR_EVENT));
        }
    }

    private void lockGrids(final ViewDefinitionState view, final List<String> referenceNames) {
        referenceNames.stream().forEach(referenceName -> {
            GridComponent gridComponent = (GridComponent) view.getComponentByReference(referenceName);

            if (gridComponent != null) {
                gridComponent.setEnabled(false);
            }
        });
    }

    public void setEventIdForMultiUploadField(final ViewDefinitionState view) {
        FormComponent plannedEventForm = (FormComponent) view.getComponentByReference(L_FORM);
        FieldComponent plannedEventIdForMultiUpload = (FieldComponent) view.getComponentByReference(L_EVENT_ID_FOR_MULTI_UPLOAD);
        FieldComponent plannedEventMultiUploadLocale = (FieldComponent) view.getComponentByReference(L_EVENT_MULTI_UPLOAD_LOCALE);

        Long plannedEventId = plannedEventForm.getEntityId();

        if (plannedEventId == null) {
            plannedEventIdForMultiUpload.setFieldValue("");
            plannedEventIdForMultiUpload.requestComponentUpdateState();
        } else {
            plannedEventIdForMultiUpload.setFieldValue(plannedEventId);
            plannedEventIdForMultiUpload.requestComponentUpdateState();
        }

        plannedEventMultiUploadLocale.setFieldValue(LocaleContextHolder.getLocale());
        plannedEventMultiUploadLocale.requestComponentUpdateState();
    }

    public void processRoles(final ViewDefinitionState view) {
        Entity user = userService.getCurrentUserEntity();

        for (PlannedEventRoles role : PlannedEventRoles.values()) {
            if (!securityService.hasRole(user, role.toString())) {
                role.disableFieldsWhenNotInRole(view);
            }
        }

        if (!parameterService.getParameter().getBooleanField(ParameterFieldsCMP.ACCEPTANCE_EVENTS)) {
            if (!securityService.hasRole(user, PlannedEventRoles.ROLE_PLANNED_EVENTS_STATES_START_STOP.toString())) {
                enableRealizationEvents(view, false);
            } else {
                enableRealizationEvents(view, true);
            }
        }
    }

    private void enableRealizationEvents(final ViewDefinitionState view, final boolean enable) {
        if (eventInState(view, PlannedEventState.IN_EDITING) || eventInState(view, PlannedEventState.ACCEPTED)) {
            enableFromRibbonGroup(view, enable, L_STATUS, L_REALIZED_EVENT);
        }
    }

    private boolean eventInState(final ViewDefinitionState view, final PlannedEventState state) {
        FormComponent plannedEventForm = (FormComponent) view.getComponentByReference(L_FORM);

        Entity event = plannedEventForm.getEntity();

        String eventState = event.getStringField(PlannedEventFields.STATE);

        if (eventState == null) {
            GridComponent gridComponent = (GridComponent) view.getComponentByReference(L_GRID);

            List<Entity> entities = gridComponent.getSelectedEntities();

            if (entities.isEmpty()) {
                return false;
            }

            return entities.stream().allMatch(e -> state.getStringValue().equals(e.getStringField(PlannedEventFields.STATE)));
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
                RibbonActionItem ribbonActionItem = ribbonGroup.getItemByName(item);

                if (ribbonActionItem != null) {
                    ribbonActionItem.setEnabled(enable);
                    ribbonActionItem.requestUpdate(true);
                }
            }
        }
    }

    public void setUnit(final ViewDefinitionState view) {
        SelectComponentState basedOnSelect = (SelectComponentState) view.getComponentByReference(PlannedEventFields.BASED_ON);
        FieldComponent toleranceUnitField = (FieldComponent) view.getComponentByReference("toleranceUnit");

        switch (PlannedEventBasedOn.parseString((String) basedOnSelect.getFieldValue())) {
            case COUNTER:
                toleranceUnitField.setFieldValue(translationService.translate("cmmsMachineParts.plannedEvent.toleranceUnit.mh",
                        view.getLocale()));

                break;
            case DATE:
                toleranceUnitField.setFieldValue(translationService.translate("cmmsMachineParts.plannedEvent.toleranceUnit.days",
                        view.getLocale()));

                break;
        }
    }

    private void toggleAddMultipleActions(ViewDefinitionState view, FieldsForType fieldsForType, Entity plannedEvent) {
        WindowComponent window = (WindowComponent) view.getComponentByReference(L_WINDOW);

        Ribbon ribbon = window.getRibbon();
        RibbonGroup actionsGroup = ribbon.getGroupByName("actionsGroup");
        RibbonActionItem addActionsItem = actionsGroup.getItemByName("addActions");

        String state = plannedEvent.getStringField(PlannedEventFields.STATE);

        List<String> statesToDisable = Arrays.asList(PlannedEventStateStringValues.REALIZED, PlannedEventStateStringValues.CANCELED);

        boolean enableAddActions = plannedEvent.getId() != null && !statesToDisable.contains(state) && !fieldsForType.getHiddenTabs().contains(PlannedEventFields.ACTIONS_TAB);

        addActionsItem.setEnabled(enableAddActions);
        addActionsItem.requestUpdate(true);
    }

}
