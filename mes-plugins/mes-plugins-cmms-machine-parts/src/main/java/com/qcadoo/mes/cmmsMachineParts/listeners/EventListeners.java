package com.qcadoo.mes.cmmsMachineParts.listeners;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Maps;
import com.qcadoo.mes.cmmsMachineParts.constants.CmmsMachinePartsConstants;
import com.qcadoo.mes.cmmsMachineParts.constants.FaultTypeFields;
import com.qcadoo.mes.cmmsMachineParts.constants.MaintenanceEventFields;
import com.qcadoo.mes.cmmsMachineParts.constants.MaintenanceEventType;
import com.qcadoo.mes.cmmsMachineParts.hooks.FactoryStructureForEventHooks;
import com.qcadoo.mes.productionLines.constants.FactoryStructureElementFields;
import com.qcadoo.mes.productionLines.constants.FactoryStructureElementType;
import com.qcadoo.mes.productionLines.factoryStructure.FactoryStructureElementsService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityTree;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.ribbon.Ribbon;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.api.ribbon.RibbonGroup;
import com.qcadoo.view.api.utils.NumberGeneratorService;
import com.qcadoo.view.internal.components.tree.TreeComponentState;

@Service
public class EventListeners {

    private static final String L_OTHER = "Inne";

    private Long factoryStructureId;

    @Autowired
    private FactoryStructureForEventHooks factoryStructureForEventHooks;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NumberGeneratorService numberGeneratorService;

    @Autowired
    private FactoryStructureElementsService factoryStructureElementsService;

    public void addEvent(final ViewDefinitionState viewDefinitionState, final ComponentState triggerState, final String args[]) {
        String eventType = args[0];

        EntityTree tree = factoryStructureForEventHooks.getGeneratedTree();
        Optional<Entity> maybeElement = tree.stream().filter(element -> element.getId() == factoryStructureId).findFirst();
        // getSelectedStructureElement(tree);
        if (!maybeElement.isPresent()) {
            viewDefinitionState.addMessage("cmmsMachineParts.error.elementNotSelected", ComponentState.MessageType.FAILURE);
            return;
        }
        Entity selectedElement = maybeElement.get();
        FactoryStructureElementType elementType = FactoryStructureElementType.of(selectedElement);
        if (elementType.compareTo(FactoryStructureElementType.COMPANY) == 0) {
            viewDefinitionState.addMessage("cmmsMachineParts.error.companySelected", ComponentState.MessageType.INFO);
            return;
        }
        if (elementType.compareTo(FactoryStructureElementType.FACTORY) == 0) {
            viewDefinitionState.addMessage("cmmsMachineParts.error.factorySelected", ComponentState.MessageType.INFO);
            return;
        }

        DataDefinition dataDefinition = dataDefinitionService.get(CmmsMachinePartsConstants.PLUGIN_IDENTIFIER,
                CmmsMachinePartsConstants.MAINTENANCE_EVENT);
        Entity maintenanceEvent = dataDefinition.create();
        if (elementType.compareTo(FactoryStructureElementType.DIVISION) == 0
                || elementType.compareTo(FactoryStructureElementType.PRODUCTION_LINE) == 0
                || MaintenanceEventType.parseString(eventType).compareTo(MaintenanceEventType.PROPOSAL) == 0) {
            maintenanceEvent.setField(MaintenanceEventFields.FAULT_TYPE, getDefaultFaultType());
        }
        fillEventFieldsFromSelectedElement(maintenanceEvent, selectedElement);
        maintenanceEvent.setField(MaintenanceEventFields.TYPE, eventType);
        maintenanceEvent.setField(MaintenanceEventFields.NUMBER, numberGeneratorService.generateNumber(
                CmmsMachinePartsConstants.PLUGIN_IDENTIFIER, CmmsMachinePartsConstants.MAINTENANCE_EVENT));
        maintenanceEvent = dataDefinition.save(maintenanceEvent);

        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put("form.id", maintenanceEvent.getId());
        viewDefinitionState.redirectTo(
                "../page/" + CmmsMachinePartsConstants.PLUGIN_IDENTIFIER + "/maintenanceEventDetails.html", false, true,
                parameters);
    }

    public void selectOnTree(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        TreeComponentState treeComponentState = (TreeComponentState) state;
        factoryStructureId = treeComponentState.getSelectedEntityId();
        WindowComponent window = (WindowComponent) view.getComponentByReference("window");
        Ribbon ribbon = window.getRibbon();
        RibbonGroup addEvents = ribbon.getGroupByName("addEvents");
        List<RibbonActionItem> items = addEvents.getItems();
        for (RibbonActionItem item : items) {
            item.setEnabled(true);
            item.requestUpdate(true);
        }
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

    private void fillEventFieldsFromSelectedElement(Entity event, final Entity selectedElement) {

        Entity currentElement = selectedElement;
        while (currentElement != null
                && FactoryStructureElementType.of(currentElement).compareTo(FactoryStructureElementType.COMPANY) != 0) {
            Entity relatedEntity = factoryStructureElementsService.getRelatedEntity(currentElement);
            event.setField(currentElement.getStringField(FactoryStructureElementFields.ENTITY_TYPE), relatedEntity);
            currentElement = currentElement.getBelongsToField(FactoryStructureElementFields.PARENT);
        }
    }

    private Entity getDefaultFaultType() {
        return dataDefinitionService.get(CmmsMachinePartsConstants.PLUGIN_IDENTIFIER, CmmsMachinePartsConstants.MODEL_FAULT_TYPE)
                .find().add(SearchRestrictions.eq(FaultTypeFields.NAME, L_OTHER)).setMaxResults(1).uniqueResult();
    }

}
