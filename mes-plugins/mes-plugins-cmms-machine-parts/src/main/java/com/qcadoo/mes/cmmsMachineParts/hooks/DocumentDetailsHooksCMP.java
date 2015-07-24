package com.qcadoo.mes.cmmsMachineParts.hooks;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.materialFlowResources.constants.DocumentFields;
import com.qcadoo.mes.materialFlowResources.constants.DocumentState;
import com.qcadoo.mes.materialFlowResources.constants.DocumentType;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;

@Service
public class DocumentDetailsHooksCMP {

    public void toggleEnabledForEventLookup(final ViewDefinitionState view) {
        FormComponent form = (FormComponent) view.getComponentByReference("form");
        Entity document = form.getPersistedEntityWithIncludedFormValues();
        LookupComponent eventLookup = (LookupComponent) view.getComponentByReference("maintenanceEvent");
        String state = document.getStringField(DocumentFields.STATE);
        String type = document.getStringField(DocumentFields.TYPE);
        if (StringUtils.isEmpty(state) || StringUtils.isEmpty(type)) {
            eventLookup.setEnabled(false);
        } else {
            if (state.equals(DocumentState.DRAFT.getStringValue())
                    && type.equals(DocumentType.INTERNAL_OUTBOUND.getStringValue())) {
                eventLookup.setEnabled(true);
            } else {
                eventLookup.setEnabled(false);
            }
        }

    }
}
