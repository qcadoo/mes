package com.qcadoo.mes.cmmsMachineParts.listeners;

import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Maps;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.basic.constants.WorkstationFields;
import com.qcadoo.mes.cmmsMachineParts.FaultTypesService;
import com.qcadoo.mes.cmmsMachineParts.constants.CmmsMachinePartsConstants;
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
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;

@Service
public class EventListeners {

    private Long factoryStructureId;

    @Autowired
    private TranslationService translationService;

    @Autowired
    private FactoryStructureForEventHooks factoryStructureForEventHooks;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private FaultTypesService faultTypesService;

    @Autowired
    private FactoryStructureElementsService factoryStructureElementsService;

    public void addEvent(final ViewDefinitionState viewDefinitionState, final ComponentState triggerState, final String args[]) {
        String eventType = args[0];
        factoryStructureId = Long.parseLong(args[1]);

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
            maintenanceEvent.setField(MaintenanceEventFields.FAULT_TYPE, faultTypesService.getDefaultFaultType());
        }
        fillEventFieldsFromSelectedElement(maintenanceEvent, selectedElement);
        maintenanceEvent.setField(MaintenanceEventFields.TYPE, eventType);
        // maintenanceEvent.setField(MaintenanceEventFields.NUMBER, numberGeneratorService.generateNumber(
        // CmmsMachinePartsConstants.PLUGIN_IDENTIFIER, CmmsMachinePartsConstants.MAINTENANCE_EVENT));
        maintenanceEvent = dataDefinition.save(maintenanceEvent);

        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put("form.id", maintenanceEvent.getId());
        viewDefinitionState.redirectTo(
                "../page/" + CmmsMachinePartsConstants.PLUGIN_IDENTIFIER + "/maintenanceEventDetails.html", false, true,
                parameters);
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
        if (state.getFieldValue() == null) {
            clearFilterForFaultType(view, MaintenanceEventFields.WORKSTATION);
        }
    }

    public void subassemblyChanged(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        if (state.getFieldValue() == null) {
            clearFilterForFaultType(view, MaintenanceEventFields.SUBASSEMBLY);
        }
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

    private void clearFilterForFaultType(final ViewDefinitionState view, final String field) {
        LookupComponent faultType = (LookupComponent) view.getComponentByReference(MaintenanceEventFields.FAULT_TYPE);
        FilterValueHolder filter = faultType.getFilterValue();
        if (filter.has(field)) {
            filter.remove(field);
            filter.remove(WorkstationFields.WORKSTATION_TYPE);
        }
        faultType.setFilterValue(filter);
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

    public void fillFieldValues(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        if (args.length < 2) {
            return;
        }
        String field = args[0];
        String value = args[1];
        if (field.equals(MaintenanceEventFields.TYPE)) {
            FieldComponent type = (FieldComponent) view.getComponentByReference(MaintenanceEventFields.TYPE);
            type.setFieldValue(value);

        }
    }

}
