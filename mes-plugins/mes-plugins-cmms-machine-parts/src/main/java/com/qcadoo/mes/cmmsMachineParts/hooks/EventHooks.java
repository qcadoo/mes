package com.qcadoo.mes.cmmsMachineParts.hooks;

import com.qcadoo.mes.cmmsMachineParts.constants.MaintenanceEventFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;
import org.springframework.stereotype.Service;

@Service
public class EventHooks {


    public void maintenanceEventBeforeRender(final ViewDefinitionState view) {
        setEventCriteriaModifiers(view);
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
}
