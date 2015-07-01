package com.qcadoo.mes.cmmsMachineParts.hooks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.constants.SubassemblyFields;
import com.qcadoo.mes.basic.constants.WorkstationFields;
import com.qcadoo.mes.cmmsMachineParts.constants.MaintenanceEventFields;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;

@Service
public class EventHooks {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void maintenanceEventBeforeRender(final ViewDefinitionState view) {
        setEventCriteriaModifiers(view);
        setUpFaultTypeLookup(view);
        setFieldsRequired(view);
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

}
