package com.qcadoo.mes.technologies.listeners;

import com.qcadoo.mes.technologies.constants.WorkstationChangeoverNormFields;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.LookupComponent;
import org.springframework.stereotype.Service;

@Service
public class WorkstationChangeoverNormDetailsListeners {

    public void clearAttributeValueLookups(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        LookupComponent fromAttributeValueLookup = (LookupComponent) view.getComponentByReference(WorkstationChangeoverNormFields.FROM_ATTRIBUTE_VALUE);
        LookupComponent toAttributeValueLookup = (LookupComponent) view.getComponentByReference(WorkstationChangeoverNormFields.TO_ATTRIBUTE_VALUE);

        fromAttributeValueLookup.setFieldValue(null);
        fromAttributeValueLookup.requestComponentUpdateState();
        toAttributeValueLookup.setFieldValue(null);
        toAttributeValueLookup.requestComponentUpdateState();
    }

}
