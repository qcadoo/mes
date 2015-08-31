package com.qcadoo.mes.cmmsMachineParts.listeners;

import com.qcadoo.mes.cmmsMachineParts.constants.DocumentFieldsCMP;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.materialFlowResources.constants.DocumentFields;
import com.qcadoo.mes.materialFlowResources.constants.DocumentType;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;

@Service public class DocumentDetailsListenersCMP {

    public void clearEvents(final ViewDefinitionState viewDefinitionState, final ComponentState triggerState,
            final String args[]) {

        FormComponent form = (FormComponent) viewDefinitionState.getComponentByReference("form");
        Entity document = form.getPersistedEntityWithIncludedFormValues();
        String type = document.getStringField(DocumentFields.TYPE);

        if (type.compareTo(DocumentType.INTERNAL_OUTBOUND.getStringValue()) != 0) {
            LookupComponent mEventLookup = (LookupComponent) viewDefinitionState
                    .getComponentByReference(DocumentFieldsCMP.MAINTENANCE_EVENT);
            mEventLookup.setFieldValue(null);
            LookupComponent pEventLookup = (LookupComponent) viewDefinitionState
                    .getComponentByReference(DocumentFieldsCMP.PLANNED_EVENT);
            pEventLookup.setFieldValue(null);
        }
    }
}
