package com.qcadoo.mes.cmmsMachineParts.hooks;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.cmmsMachineParts.constants.PlannedEventRealizationFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;

@Service
public class PlannedEventRealizationDetailsHooks {

    public static final String L_EVENT = "plannedEvent";

    public void setFilterValues(final ViewDefinitionState view) {
        FormComponent form = (FormComponent) view.getComponentByReference("form");
        Entity plannedEventRealization = form.getPersistedEntityWithIncludedFormValues();
        if (plannedEventRealization != null) {
            Long plannedEventId = plannedEventRealization.getBelongsToField(PlannedEventRealizationFields.PLANNED_EVENT).getId();
            LookupComponent actionsLookup = (LookupComponent) view.getComponentByReference("action");

            FilterValueHolder actionsFVH = actionsLookup.getFilterValue();

            actionsFVH.put(L_EVENT, plannedEventId);
            actionsLookup.setFilterValue(actionsFVH);
            actionsLookup.requestComponentUpdateState();
        }
    }
}
