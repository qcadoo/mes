package com.qcadoo.mes.cmmsMachineParts.listeners;

import com.qcadoo.mes.cmmsMachineParts.constants.MaintenanceEventFields;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ContextEventListeners {

    private static final Logger LOG = LoggerFactory.getLogger(ContextEventListeners.class);

    public void factoryChanged(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        clearSelectionOnDivision(view);
    }

    private void clearSelectionOnDivision(final ViewDefinitionState view) {
        clearFieldIfExists(view, MaintenanceEventFields.DIVISION);
        clearSelectionOnProductionLine(view);
    }

    private void clearSelectionOnProductionLine(final ViewDefinitionState view) {
        clearFieldIfExists(view, MaintenanceEventFields.PRODUCTION_LINE);
        clearSelectionOnWorkstation(view);
    }

    private void clearSelectionOnWorkstation(final ViewDefinitionState view) {
        clearFieldIfExists(view, MaintenanceEventFields.WORKSTATION);
        clearSelectionOnSubassembly(view);
    }

    private void clearSelectionOnSubassembly(final ViewDefinitionState view) {
        clearFieldIfExists(view, MaintenanceEventFields.SUBASSEMBLY);
    }

    private void clearFieldIfExists(ViewDefinitionState view, String reference) {
        FieldComponent fieldComponent = (FieldComponent) view.getComponentByReference(reference);
        if (fieldComponent != null) {
            fieldComponent.setFieldValue(null);
            fieldComponent.requestComponentUpdateState();
        }
    }


}
