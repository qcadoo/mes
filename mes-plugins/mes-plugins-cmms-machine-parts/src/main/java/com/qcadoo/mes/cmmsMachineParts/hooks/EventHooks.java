package com.qcadoo.mes.cmmsMachineParts.hooks;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.constants.SubassemblyFields;
import com.qcadoo.mes.basic.constants.WorkstationFields;
import com.qcadoo.mes.cmmsMachineParts.FaultTypesService;
import com.qcadoo.mes.cmmsMachineParts.MaintenanceEventContextService;
import com.qcadoo.mes.cmmsMachineParts.MaintenanceEventService;
import com.qcadoo.mes.cmmsMachineParts.constants.CmmsMachinePartsConstants;
import com.qcadoo.mes.cmmsMachineParts.constants.MaintenanceEventContextFields;
import com.qcadoo.mes.cmmsMachineParts.constants.MaintenanceEventFields;
import com.qcadoo.mes.cmmsMachineParts.constants.MaintenanceEventType;
import com.qcadoo.mes.cmmsMachineParts.constants.PlannedEventBasedOn;
import com.qcadoo.mes.cmmsMachineParts.constants.PlannedEventFields;
import com.qcadoo.mes.cmmsMachineParts.states.constants.MaintenanceEventState;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
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

    @Autowired
    private MaintenanceEventService maintenanceEventService;

    @Autowired
    private FaultTypesService faultTypesService;

    @Autowired
    private NumberGeneratorService numberGeneratorService;

    @Autowired
    private MaintenanceEventContextService maintenanceEventContextService;

    public void maintenanceEventBeforeRender(final ViewDefinitionState view) {
        setEventCriteriaModifiers(view);
        setUpFaultTypeLookup(view);
        setFieldsRequired(view);
        fillDefaultFields(view);
        generateNumber(view, CmmsMachinePartsConstants.MODEL_MAINTENANCE_EVENT, MaintenanceEventFields.NUMBER);
        fillDefaultFieldsFromContext(view, MaintenanceEventFields.MAINTENANCE_EVENT_CONTEXT);
        toggleEnabledViewComponents(view, MaintenanceEventFields.MAINTENANCE_EVENT_CONTEXT);
        disableFieldsForState(view);
        toggleOldSolutionsButton(view);
        enableShowPlannedEvent(view);
    }

    public void plannedEventBeforeRender(final ViewDefinitionState view) {
        setEventCriteriaModifiers(view);
        generateNumber(view, CmmsMachinePartsConstants.MODEL_PLANNED_EVENT, PlannedEventFields.NUMBER);
        fillDefaultFieldsFromContext(view, PlannedEventFields.PLANNED_EVENT_CONTEXT);
        toggleEnabledViewComponents(view, PlannedEventFields.PLANNED_EVENT_CONTEXT);
        toggleEnabledFromBasedOn(view);
        enableShowMaintenanceEvent(view);
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

    private void enableShowPlannedEvent(final ViewDefinitionState view) {
        FormComponent form = (FormComponent) view.getComponentByReference(L_FORM);
        Entity event = form.getPersistedEntityWithIncludedFormValues();
        Optional<Entity> plannedEvent = maintenanceEventService.getPlannedEventForMaintenanceEvent(event);

        if (plannedEvent.isPresent()) {
            enableRibbonButtion(view, "plannedEvents", "showPlannedEvent");
        }
    }

    private void enableShowMaintenanceEvent(final ViewDefinitionState view) {
        FormComponent form = (FormComponent) view.getComponentByReference(L_FORM);
        Entity event = form.getPersistedEntityWithIncludedFormValues();

        Entity maintenanceEvent = event.getBelongsToField(PlannedEventFields.MAINTENANCE_EVENT);
        if (maintenanceEvent != null) {
            enableRibbonButtion(view, "maintenanceEvents", "showMaintenanceEvent");
        }
    }

    private void enableRibbonButtion(final ViewDefinitionState view, String groupName, String itemName) {

        WindowComponent window = (WindowComponent) view.getComponentByReference("window");
        Ribbon ribbon = window.getRibbon();
        RibbonGroup group = ribbon.getGroupByName(groupName);
        RibbonActionItem item = group.getItemByName(itemName);
        item.setEnabled(true);
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

    private void fillDefaultFieldsFromContext(final ViewDefinitionState view, String contextField) {
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

    private void toggleEnabledViewComponents(final ViewDefinitionState view, String contextField) {
        FormComponent form = (FormComponent) view.getComponentByReference(L_FORM);
        Entity eventEntity = form.getPersistedEntityWithIncludedFormValues();

        toggleEnabledForWorkstation(view, form, eventEntity);
        toggleEnabledForFactory(view, form, eventEntity, contextField);
        toggleEnabledForDivision(view, form, eventEntity, contextField);
    }

    private void toggleEnabledForWorkstation(final ViewDefinitionState view, final FormComponent form, final Entity eventEntity) {
        boolean enabled = eventEntity.getBelongsToField(MaintenanceEventFields.PRODUCTION_LINE) != null;
        LookupComponent workstation = (LookupComponent) view.getComponentByReference(MaintenanceEventFields.WORKSTATION);
        workstation.setEnabled(enabled);
    }

    private void toggleEnabledForFactory(final ViewDefinitionState view, final FormComponent form, final Entity eventEntity,
            String contextField) {
        if (eventEntity.getBelongsToField(contextField) == null) {
            return;
        }
        boolean enabled = eventEntity.getBelongsToField(contextField).getBelongsToField(MaintenanceEventContextFields.FACTORY) == null;
        LookupComponent factoryLookup = (LookupComponent) view.getComponentByReference(MaintenanceEventFields.FACTORY);
        factoryLookup.setEnabled(enabled);
    }

    private void toggleEnabledForDivision(final ViewDefinitionState view, final FormComponent form, final Entity eventEntity,
            String contextField) {
        if (eventEntity.getBelongsToField(contextField) == null) {
            return;
        }
        boolean enabled = eventEntity.getBelongsToField(contextField).getBelongsToField(MaintenanceEventContextFields.DIVISION) == null;
        LookupComponent divisionLookup = (LookupComponent) view.getComponentByReference(MaintenanceEventFields.DIVISION);
        divisionLookup.setEnabled(enabled);
    }

    private void setFieldsRequired(final ViewDefinitionState view) {
        FieldComponent factory = (FieldComponent) view.getComponentByReference(MaintenanceEventFields.FACTORY);
        FieldComponent division = (FieldComponent) view.getComponentByReference(MaintenanceEventFields.DIVISION);
        FieldComponent faultType = (FieldComponent) view.getComponentByReference(MaintenanceEventFields.FAULT_TYPE);

        factory.setRequired(true);
        division.setRequired(true);
        faultType.setRequired(true);
    }

    private void setEventCriteriaModifiers(ViewDefinitionState view) {
        FormComponent formComponent = (FormComponent) view.getComponentByReference("form");
        Entity event = formComponent.getEntity();

        setEventCriteriaModifier(view, event, MaintenanceEventFields.FACTORY, MaintenanceEventFields.DIVISION);
        setEventCriteriaModifier(view, event, MaintenanceEventFields.DIVISION, MaintenanceEventFields.WORKSTATION);
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
        FormComponent formComponent = (FormComponent) view.getComponentByReference("form");
        Entity event = formComponent.getPersistedEntityWithIncludedFormValues();
        Entity workstation = event.getBelongsToField(MaintenanceEventFields.WORKSTATION);
        Entity subassembly = event.getBelongsToField(MaintenanceEventFields.SUBASSEMBLY);
        if (workstation != null) {

            LookupComponent faultTypeLookup = (LookupComponent) view.getComponentByReference(MaintenanceEventFields.FAULT_TYPE);

            FilterValueHolder filter = faultTypeLookup.getFilterValue();
            filter.put(MaintenanceEventFields.WORKSTATION, workstation.getId());

            if (subassembly != null) {
                Entity workstationType = subassembly.getBelongsToField(SubassemblyFields.WORKSTATION_TYPE);
                filter.put(MaintenanceEventFields.SUBASSEMBLY, subassembly.getId());
                filter.put(WorkstationFields.WORKSTATION_TYPE, workstationType.getId());
            } else {
                Entity workstationType = workstation.getBelongsToField(WorkstationFields.WORKSTATION_TYPE);
                filter.put(WorkstationFields.WORKSTATION_TYPE, workstationType.getId());
            }
            faultTypeLookup.setFilterValue(filter);
        }
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

        FormComponent formComponent = (FormComponent) view.getComponentByReference("form");
        Entity event = formComponent.getPersistedEntityWithIncludedFormValues();

        showSolutionsRibbonActionItem.setEnabled(event.getId() != null);
        showSolutionsRibbonActionItem.requestUpdate(true);
    }

    public final void onBeforeRenderListView(final ViewDefinitionState view) {
        maintenanceEventContextService.beforeRenderListView(view);
    }
}
