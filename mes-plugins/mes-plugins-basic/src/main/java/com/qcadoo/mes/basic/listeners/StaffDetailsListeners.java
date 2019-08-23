package com.qcadoo.mes.basic.listeners;

import com.qcadoo.mes.basic.constants.StaffFields;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.LookupComponent;

import org.springframework.stereotype.Service;

@Service
public class StaffDetailsListeners {

    //to fire beforeRenders
    public final void onOnDivisionChange(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        LookupComponent workstation = (LookupComponent) view.getComponentByReference(StaffFields.WORKSTATION);
        workstation.setFieldValue(null);
        workstation.requestComponentUpdateState();
    }
}
