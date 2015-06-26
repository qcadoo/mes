package com.qcadoo.mes.cmmsMachineParts.listeners;

import com.google.common.collect.Maps;
import com.qcadoo.mes.cmmsMachineParts.constants.CmmsMachinePartsConstants;
import com.qcadoo.mes.cmmsMachineParts.constants.MaintenanceEventFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.utils.NumberGeneratorService;
import com.qcadoo.view.internal.components.tree.TreeComponentState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class EventListeners {

    private Long factoryStructureId;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NumberGeneratorService numberGeneratorService;

    public void addEvent(final ViewDefinitionState viewDefinitionState, final ComponentState triggerState, final String args[]) {
        String eventType = args[0];

        DataDefinition dataDefinition = dataDefinitionService.
                get(CmmsMachinePartsConstants.PLUGIN_IDENTIFIER, CmmsMachinePartsConstants.MAINTENANCE_EVENT);
        Entity maintenanceEvent = dataDefinition.create();

        maintenanceEvent.setField(MaintenanceEventFields.TYPE, eventType);
//        maintenanceEvent.setField(MaintenanceEventFields.FACTORY_STRUCTURE, factoryStructureId);
        maintenanceEvent.setField(MaintenanceEventFields.NUMBER, numberGeneratorService.generateNumber(CmmsMachinePartsConstants.PLUGIN_IDENTIFIER, CmmsMachinePartsConstants.MAINTENANCE_EVENT));
        maintenanceEvent = dataDefinition.save(maintenanceEvent);

        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put("form.id", maintenanceEvent.getId());
        viewDefinitionState.redirectTo("../page/" + CmmsMachinePartsConstants.PLUGIN_IDENTIFIER + "/maintenanceEventDetails.html", false, true, parameters);
    }

    public final void selectOnTree(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        TreeComponentState treeComponentState = (TreeComponentState) state;
        factoryStructureId = treeComponentState.getSelectedEntityId();
    }

    public void factoryChanged(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        clearSelectionOnDivision(view);
    }

    public void divisionChanged(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        clearSelectionOnProductionLine(view);
    }

    public void productionLineChanged(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        clearSelectionOnWorkstation(view);
    }

    public void workstationChanged(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        clearSelectionOnSubassembly(view);
    }

    public void subassemblyChanged(final ViewDefinitionState view, final ComponentState state, final String[] args) {
    }

    private void clearSelectionOnDivision(final ViewDefinitionState view) {
        clearField(view, MaintenanceEventFields.DIVISION);
        clearSelectionOnProductionLine(view);
    }

    private void clearSelectionOnProductionLine(final ViewDefinitionState view) {
        clearField(view, MaintenanceEventFields.PRODUCTION_LINE);
        clearSelectionOnWorkstation(view);
    }

    private void clearSelectionOnWorkstation(final ViewDefinitionState view) {
        clearField(view, MaintenanceEventFields.WORKSTATION);
        clearSelectionOnSubassembly(view);
    }

    private void clearSelectionOnSubassembly(final ViewDefinitionState view) {
        clearField(view, MaintenanceEventFields.SUBASSEMBLY);
    }

    private void clearField(ViewDefinitionState view, String reference) {
        FieldComponent fieldComponent = (FieldComponent) view.getComponentByReference(reference);
        fieldComponent.setFieldValue(null);
        fieldComponent.requestComponentUpdateState();
    }
}
