package com.qcadoo.mes.cmmsMachineParts.states;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.cmmsMachineParts.constants.MachinePartForEventFields;
import com.qcadoo.mes.cmmsMachineParts.constants.MaintenanceEventFields;
import com.qcadoo.mes.materialFlow.constants.LocationFields;
import com.qcadoo.mes.materialFlow.constants.MaterialFlowConstants;
import com.qcadoo.mes.materialFlowResources.MaterialFlowResourcesService;
import com.qcadoo.mes.materialFlowResources.service.DocumentBuilder;
import com.qcadoo.mes.materialFlowResources.service.DocumentManagementService;
import com.qcadoo.mes.states.StateChangeContext;
import com.qcadoo.mes.states.constants.StateChangeStatus;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;

@Service
public class MaintenanceEventDocumentsService {

    @Autowired
    private MaterialFlowResourcesService materialFlowResourcesService;

    @Autowired
    private DocumentManagementService documentManagementService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void createDocumentsForMachineParts(final StateChangeContext stateChangeContext) {

        Entity maintenanceEvent = stateChangeContext.getOwner();
        createDocuments(maintenanceEvent);
        if (!maintenanceEvent.isValid()) {
            stateChangeContext.setStatus(StateChangeStatus.FAILURE);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private void createDocuments(final Entity maintenanceEvent) {

        List<Entity> machinePartsForEvent = maintenanceEvent.getHasManyField(MaintenanceEventFields.MACHINE_PARTS_FOR_EVENT);
        if (machinePartsForEvent.isEmpty()) {
            return;
        }

        DataDefinition warehouseDD = dataDefinitionService.get(MaterialFlowConstants.PLUGIN_IDENTIFIER,
                MaterialFlowConstants.MODEL_LOCATION);
        Multimap<Long, Entity> groupedMachinePartsForEvent = groupMachinePartsByLocation(machinePartsForEvent);
        for (Long warehouseId : groupedMachinePartsForEvent.keySet()) {
            Collection<Entity> machinePartsForLocation = groupedMachinePartsForEvent.get(warehouseId);
            List<Entity> machineParts = machinePartsForLocation.stream()
                    .map(part -> part.getBelongsToField(MachinePartForEventFields.MACHINE_PART)).distinct()
                    .collect(Collectors.toList());
            Entity warehouse = warehouseDD.get(warehouseId);
            Map<Long, BigDecimal> quantitiesInWarehouse = materialFlowResourcesService.getQuantitiesForProductsAndLocation(
                    machineParts, warehouse);

            if (!checkIfResourcesAreSufficient(maintenanceEvent, quantitiesInWarehouse, machinePartsForLocation, warehouse)) {
                return;
            }
            Entity document = createDocumentForLocation(maintenanceEvent, warehouse, machinePartsForLocation);
            if (!document.isValid()) {
                maintenanceEvent.addGlobalError("cmmsMachineParts.maintenanceEvent.state.documentNotCreated");
                return;
            }
        }

    }

    private Entity createDocumentForLocation(final Entity maintenanceEvent, final Entity warehouse,
            final Collection<Entity> machinePartsForLocation) {
        DocumentBuilder documentBuilder = documentManagementService.getDocumentBuilder().internalOutbound(warehouse);
        for (Entity machinePartForLocation : machinePartsForLocation) {
            documentBuilder.addPosition(machinePartForLocation.getBelongsToField(MachinePartForEventFields.MACHINE_PART),
                    machinePartForLocation.getDecimalField(MachinePartForEventFields.PLANNED_QUANTITY));
        }
        documentBuilder.setField("maintenanceEvent", maintenanceEvent);
        return documentBuilder.setAccepted().build();
    }

    private Multimap<Long, Entity> groupMachinePartsByLocation(final List<Entity> machinePartsForEvent) {
        Multimap<Long, Entity> groupedMachineParts = ArrayListMultimap.create();

        machinePartsForEvent.stream().forEach(
                part -> groupedMachineParts.put(part.getBelongsToField(MachinePartForEventFields.WAREHOUSE).getId(), part));
        return groupedMachineParts;
    }

    private boolean checkIfResourcesAreSufficient(final Entity maintenanceEvent, Map<Long, BigDecimal> quantitiesInWarehouse,
            Collection<Entity> machinePartsForLocation, final Entity warehouse) {
        StringBuilder errorMessage = new StringBuilder();
        String warehouseNumber = warehouse.getStringField(LocationFields.NUMBER);
        List<String> errorProducts = Lists.newArrayList();
        for (Entity machinePartForEvent : machinePartsForLocation) {
            Entity machinePart = machinePartForEvent.getBelongsToField(MachinePartForEventFields.MACHINE_PART);
            BigDecimal plannedQuantity = machinePartForEvent.getDecimalField(MachinePartForEventFields.PLANNED_QUANTITY);
            BigDecimal availableQuantity = quantitiesInWarehouse.get(machinePart.getId());
            if ((availableQuantity != null && plannedQuantity.compareTo(availableQuantity) > 0) || availableQuantity == null) {
                errorProducts.add(machinePart.getStringField(ProductFields.NUMBER));
            }
        }
        if (errorProducts.isEmpty()) {
            return true;
        }
        errorMessage.append(errorProducts.stream().distinct().collect(Collectors.joining(", ")));
        if (errorMessage.length() + warehouseNumber.length() < 255) {
            maintenanceEvent.addGlobalError("cmmsMachineParts.maintenanceEvent.state.notSufficientResources", false,
                    warehouseNumber, errorMessage.toString());
        } else {
            maintenanceEvent.addGlobalError("cmmsMachineParts.maintenanceEvent.state.notSufficientResourcesLong", false);
        }
        return false;
    }
}
