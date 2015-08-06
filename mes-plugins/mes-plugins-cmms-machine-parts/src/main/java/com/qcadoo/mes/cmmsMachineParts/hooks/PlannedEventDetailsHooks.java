package com.qcadoo.mes.cmmsMachineParts.hooks;

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.cmmsMachineParts.constants.PlannedEventBasedOn;
import com.qcadoo.mes.cmmsMachineParts.constants.PlannedEventFields;
import com.qcadoo.mes.cmmsMachineParts.constants.PlannedEventType;
import com.qcadoo.mes.cmmsMachineParts.plannedEvents.factory.EventFieldsForTypeFactory;
import com.qcadoo.mes.cmmsMachineParts.plannedEvents.fieldsForType.FieldsForType;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;

@Service
public class PlannedEventDetailsHooks {

    private static final String L_FORM = "form";

    @Autowired
    private EventFieldsForTypeFactory eventFieldsForTypeFactory;

    @Autowired
    private EventHooks eventHooks;

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

        Set<String> allFields = plannedEvent.getDataDefinition().getFields().keySet();

        List<String> hiddenFields = fieldsForType.getHiddenFields();

        for (String fieldName : allFields) {
            FieldComponent fieldComponent = (FieldComponent) view.getComponentByReference(fieldName);

            if (fieldComponent != null && !fieldName.equals(PlannedEventFields.STATE)) {
                if (hiddenFields.contains(fieldName)) {
                    fieldComponent.setVisible(false);
                } else {
                    fieldComponent.setVisible(true);
                }
            }

        }
        setAndLockBasedOn(view, fieldsForType);
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
}
