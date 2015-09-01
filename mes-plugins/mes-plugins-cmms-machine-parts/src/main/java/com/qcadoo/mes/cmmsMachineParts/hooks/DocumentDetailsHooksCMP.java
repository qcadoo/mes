package com.qcadoo.mes.cmmsMachineParts.hooks;

import com.qcadoo.mes.cmmsMachineParts.constants.DocumentFieldsCMP;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.materialFlowResources.constants.DocumentFields;
import com.qcadoo.mes.materialFlowResources.constants.DocumentState;
import com.qcadoo.mes.materialFlowResources.constants.DocumentType;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;

@Service public class DocumentDetailsHooksCMP {

    private static final String L_FORM = "form";

    public void toggleEnabledForEventLookup(final ViewDefinitionState view) {
        FormComponent form = (FormComponent) view.getComponentByReference(L_FORM);
        Entity document = form.getPersistedEntityWithIncludedFormValues();
        LookupComponent eventLookup = (LookupComponent) view.getComponentByReference(DocumentFieldsCMP.MAINTENANCE_EVENT);
        LookupComponent plannedEventLookup = (LookupComponent) view.getComponentByReference(DocumentFieldsCMP.PLANNED_EVENT);

        String state = document.getStringField(DocumentFields.STATE);
        String type = document.getStringField(DocumentFields.TYPE);
        if (StringUtils.isEmpty(state) || StringUtils.isEmpty(type)) {
            eventLookup.setEnabled(false);
            plannedEventLookup.setEnabled(false);
        } else {
            if (state.equals(DocumentState.DRAFT.getStringValue()) && type
                    .equals(DocumentType.INTERNAL_OUTBOUND.getStringValue())) {
                eventLookup.setEnabled(true);
                plannedEventLookup.setEnabled(true);
            } else {
                eventLookup.setEnabled(false);
                plannedEventLookup.setEnabled(false);
            }
        }

    }
}
