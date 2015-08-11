package com.qcadoo.mes.cmmsMachineParts.hooks;

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.qcadoo.mes.cmmsMachineParts.constants.PlannedEventBasedOn;
import com.qcadoo.mes.cmmsMachineParts.constants.PlannedEventFields;
import com.qcadoo.mes.cmmsMachineParts.constants.PlannedEventType;
import com.qcadoo.mes.cmmsMachineParts.plannedEvents.factory.EventFieldsForTypeFactory;
import com.qcadoo.mes.cmmsMachineParts.plannedEvents.fieldsForType.FieldsForType;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;

@Service
public class PlannedEventDetailsHooks {

    private static final String L_FORM = "form";

    @Autowired
    private EventFieldsForTypeFactory eventFieldsForTypeFactory;

    @Autowired
    private EventHooks eventHooks;

    private List<String> previouslyHiddenTabs = Lists.newArrayList();

    public void plannedEventBeforeRender(final ViewDefinitionState view) {
        eventHooks.plannedEventBeforeRender(view);
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

    private void hideFields(final ViewDefinitionState view, final Entity plannedEvent, final FieldsForType fieldsForType) {
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

    private void setAndLockBasedOn(final ViewDefinitionState view, final FieldsForType fieldsForType) {
        FieldComponent basedOn = (FieldComponent) view.getComponentByReference(PlannedEventFields.BASED_ON);
        if (fieldsForType.shouldLockBasedOn()) {
            basedOn.setFieldValue(PlannedEventBasedOn.DATE.getStringValue());
            basedOn.setEnabled(false);
        } else {
            basedOn.setEnabled(true);
        }
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
}
