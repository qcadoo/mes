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
import com.qcadoo.mes.cmmsMachineParts.plannedEvents.fieldsForType.FieldsForType;
import com.qcadoo.mes.cmmsMachineParts.roles.PlannedEventRoles;
import com.qcadoo.mes.cmmsMachineParts.states.constants.PlannedEventState;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class PlannedEventDetailsHooks {

    private static final String L_FORM = "form";

    private static final String L_WINDOW = "window";

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

    private List<String> previouslyHiddenTabs = Lists.newArrayList();

    private static final List<String> GRIDS = Lists.newArrayList(PlannedEventFields.RELATED_EVENTS, PlannedEventFields.ACTIONS,
            PlannedEventFields.RESPONSIBLE_WORKERS, PlannedEventFields.REALIZATIONS, PlannedEventFields.MACHINE_PARTS_FOR_EVENT);

    public void plannedEventBeforeRender(final ViewDefinitionState view) {
        FormComponent form = (FormComponent) view.getComponentByReference(L_FORM);
        form.setFormEnabled(true);

        FieldComponent acceptanceEvents = (FieldComponent) view.getComponentByReference(PlannedEventFields.ACCEPTANCE_EVENTS);
        acceptanceEvents.setFieldValue(parameterService.getParameter().getBooleanField(ParameterFieldsCMP.ACCEPTANCE_EVENTS));

        eventHooks.plannedEventBeforeRender(view);

        Entity plannedEvent = form.getEntity();
        FieldComponent type = (FieldComponent) view.getComponentByReference(PlannedEventFields.TYPE);
        type.setEnabled(plannedEvent.getId() == null);
        setCriteriaModifiers(view, plannedEvent);

        // TODO dev_team - very very ugly way to fix issue GOODFOOD-742, should be fixed more properly
        // if problem with manyToMany in related entity will happen once more
        Object relatedEvents = plannedEvent.getField(PlannedEventFields.RELATED_EVENTS);
        if (relatedEvents != null && relatedEvents.getClass().equals(Integer.class)) {
            lockView(view);
        }

        processRoles(view);
        disableFieldsForState(view);
        setUnit(view);
    }

    private void lockView(final ViewDefinitionState view) {
        FormComponent form = (FormComponent) view.getComponentByReference(L_FORM);
        WindowComponent window = (WindowComponent) view.getComponentByReference("window");
        Ribbon ribbon = window.getRibbon();
        RibbonGroup actions = ribbon.getGroupByName("actions");
        RibbonGroup status = ribbon.getGroupByName("status");
        List<RibbonActionItem> items = actions.getItems();
        items.addAll(status.getItems());
        for (RibbonActionItem item : items) {
            item.setEnabled(false);
            item.requestUpdate(true);
        }
        for (String reference : GRIDS) {
            lockGrid(view, reference);
        }

        form.setFormEnabled(false);
    }

    private void lockGrid(final ViewDefinitionState view, final String reference) {
        GridComponent grid = (GridComponent) view.getComponentByReference(reference);
        grid.setEnabled(false);
    }

    private void setCriteriaModifiers(final ViewDefinitionState view, final Entity plannedEvent) {
        LookupComponent relatedEventLookup = (LookupComponent) view.getComponentByReference("relatedEventLookup");
        FilterValueHolder filter = relatedEventLookup.getFilterValue();
        filter.put(PlannedEventFields.NUMBER, plannedEvent.getStringField(PlannedEventFields.NUMBER));
        relatedEventLookup.setFilterValue(filter);

    }

    public void toggleFieldsVisible(final ViewDefinitionState view) {

        FormComponent form = (FormComponent) view.getComponentByReference(L_FORM);
        Entity plannedEvent = form.getPersistedEntityWithIncludedFormValues();
        PlannedEventType type = PlannedEventType.from(plannedEvent);
        FieldsForType fieldsForType = eventFieldsForTypeFactory.createFieldsForType(type);
        if (fieldsForType == null) {
            return;
        }

        hideFields(view, plannedEvent, fieldsForType);
        hideTabs(view, fieldsForType);
        clearGrids(view, fieldsForType);
        setAndLockBasedOn(view, fieldsForType);
    }

    public void hideFields(final ViewDefinitionState view, final Entity plannedEvent, final FieldsForType fieldsForType) {
        Set<String> allFields = plannedEvent.getDataDefinition().getFields().keySet();

        List<String> hiddenFields = fieldsForType.getHiddenFields();

        for (String fieldName : allFields) {
            Optional<ComponentState> maybeFieldComponent = view.tryFindComponentByReference(fieldName);

            if (maybeFieldComponent.isPresent() && !fieldName.equals(PlannedEventFields.STATE)) {
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

        List<String> gridsToClear = fieldsForType.getGridsToClear();
        for (String grid : gridsToClear) {
            GridComponent gridComponent = (GridComponent) view.getComponentByReference(grid);
            gridComponent.setEntities(Lists.newArrayList());
        }
    }

    public void setAndLockBasedOn(final ViewDefinitionState view, final FieldsForType fieldsForType) {
        FieldComponent basedOn = (FieldComponent) view.getComponentByReference(PlannedEventFields.BASED_ON);
        if (fieldsForType.shouldLockBasedOn()) {
            basedOn.setFieldValue(PlannedEventBasedOn.DATE.getStringValue());
            basedOn.setEnabled(false);
        } else {
            basedOn.setEnabled(true);
        }
    }

    private void disableFieldsForState(final ViewDefinitionState view) {
        FormComponent form = (FormComponent) view.getComponentByReference(L_FORM);
        Entity event = form.getPersistedEntityWithIncludedFormValues();
        PlannedEventState state = PlannedEventState.of(event);
        if (state.compareTo(PlannedEventState.CANCELED) == 0 || state.compareTo(PlannedEventState.REALIZED) == 0) {
            form.setFormEnabled(false);
            lockGrids(view, Lists.newArrayList(PlannedEventFields.RESPONSIBLE_WORKERS, PlannedEventFields.ACTIONS,
                    PlannedEventFields.RELATED_EVENTS, PlannedEventFields.REALIZATIONS,
                    PlannedEventFields.MACHINE_PARTS_FOR_EVENT));
        }
    }

    private void lockGrids(ViewDefinitionState view, ArrayList<String> gridNames) {
        gridNames.stream().forEach(gridName -> {
            GridComponent grid = (GridComponent) view.getComponentByReference(gridName);
            grid.setEnabled(false);
        });
    }

    public void setEventIdForMultiUploadField(final ViewDefinitionState view) {
        FormComponent plannedEvent = (FormComponent) view.getComponentByReference(L_FORM);
        FieldComponent plannedEventIdForMultiUpload = (FieldComponent) view.getComponentByReference("eventIdForMultiUpload");
        FieldComponent plannedEventMultiUploadLocale = (FieldComponent) view.getComponentByReference("eventMultiUploadLocale");

        if (plannedEvent.getEntityId() != null) {
            plannedEventIdForMultiUpload.setFieldValue(plannedEvent.getEntityId());
            plannedEventIdForMultiUpload.requestComponentUpdateState();
        } else {
            plannedEventIdForMultiUpload.setFieldValue("");
            plannedEventIdForMultiUpload.requestComponentUpdateState();
        }
        plannedEventMultiUploadLocale.setFieldValue(LocaleContextHolder.getLocale());
        plannedEventMultiUploadLocale.requestComponentUpdateState();

    }

    public void processRoles(ViewDefinitionState view) {
        Entity user = userService.getCurrentUserEntity();
        for (PlannedEventRoles role : PlannedEventRoles.values()) {
            if (!securityService.hasRole(user, role.toString())) {
                role.disableFieldsWhenNotInRole(view);
            }
        }

        if(!parameterService.getParameter().getBooleanField(ParameterFieldsCMP.ACCEPTANCE_EVENTS)){
            if(!securityService.hasRole(user, PlannedEventRoles.ROLE_PLANNED_EVENTS_STATES_START_STOP.toString())){
                enableRealizationEvents(view, false);
            } else {
                enableRealizationEvents(view, true);
            }
        }
    }

    private void enableRealizationEvents(final ViewDefinitionState view, final boolean enable) {
        if(eventInState(view, PlannedEventState.IN_EDITING) || eventInState(view, PlannedEventState.ACCEPTED)){
            enableFromRibbonGroup(view, enable, "status", "realizedEvent");
        }
    }

    private boolean eventInState(final ViewDefinitionState view, final PlannedEventState state) {
        FormComponent form = (FormComponent) view.getComponentByReference(L_FORM);
        Entity event = form.getEntity();
        String eventState = event.getStringField(PlannedEventFields.STATE);
        if (eventState == null) {
            GridComponent grid = (GridComponent) view.getComponentByReference("grid");
            List<Entity> entities = grid.getSelectedEntities();
            if (entities.isEmpty()){
                return false;
            }
            return entities.stream().allMatch(e -> state.getStringValue().equals(e.getStringField(PlannedEventFields.STATE)));
        }
        return state.getStringValue().equals(eventState);
    }

    private void enableFromRibbonGroup(final ViewDefinitionState view, final boolean enable, final String groupName, String... items) {
        WindowComponent window = (WindowComponent) view.getComponentByReference(L_WINDOW);
        Ribbon ribbon = window.getRibbon();
        RibbonGroup ribbonGroup = ribbon.getGroupByName(groupName);
        if(ribbonGroup != null) {
            for (String item : items) {
                RibbonActionItem ribbonItem = ribbonGroup.getItemByName(item);
                if (ribbonItem != null) {
                    ribbonItem.setEnabled(enable);
                    ribbonItem.requestUpdate(true);
                }
            }
        }
    }

    public void setUnit(final ViewDefinitionState view) {
        SelectComponentState basedOnSelect = (SelectComponentState) view.getComponentByReference(PlannedEventFields.BASED_ON);
        FieldComponent unitLabel = (FieldComponent) view.getComponentByReference("toleranceUnit");
        switch (PlannedEventBasedOn.parseString((String) basedOnSelect.getFieldValue())) {
            case COUNTER:
                unitLabel.setFieldValue(translationService.translate("cmmsMachineParts.plannedEvent.toleranceUnit.mh",
                        view.getLocale()));
                break;
            case DATE:
                unitLabel.setFieldValue(translationService.translate("cmmsMachineParts.plannedEvent.toleranceUnit.days",
                        view.getLocale()));
                break;
        }
    }
}
