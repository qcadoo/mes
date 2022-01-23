package com.qcadoo.mes.basic.listeners;

import com.qcadoo.mes.basic.constants.StaffFields;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.LookupComponent;

import org.springframework.stereotype.Service;

@Service
public class StaffDetailsListeners {

    public final void onProductionLineChange(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        clearLookup(view, StaffFields.WORKSTATION);
    }

    public final void onDivisionChange(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        clearLookup(view, StaffFields.WORKSTATION);
        clearLookup(view, StaffFields.PRODUCTION_LINE);
    }

    private void clearLookup(ViewDefinitionState view, String reference) {
        LookupComponent lookupComponent = (LookupComponent) view.getComponentByReference(reference);
        lookupComponent.setFieldValue(null);
        lookupComponent.requestComponentUpdateState();
    }
}
