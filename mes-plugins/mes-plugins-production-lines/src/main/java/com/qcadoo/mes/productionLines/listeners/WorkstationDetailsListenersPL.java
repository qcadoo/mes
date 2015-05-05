package com.qcadoo.mes.productionLines.listeners;

import org.springframework.stereotype.Service;

import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;

@Service
public class WorkstationDetailsListenersPL {

    public void generateFactoryStructure(final ViewDefinitionState view, final ComponentState state, final String[] args) {

    }

    public void clearProductionLine(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        LookupComponent productionLineLookup = (LookupComponent) view.getComponentByReference("productionLinesLookup");
        productionLineLookup.setFieldValue(null);
        productionLineLookup.requestComponentUpdateState();
    }
}
