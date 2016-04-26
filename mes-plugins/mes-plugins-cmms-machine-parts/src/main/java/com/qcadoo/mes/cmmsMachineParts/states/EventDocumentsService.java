/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.4
 *
 * This file is part of Qcadoo.
 *
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */
package com.qcadoo.mes.cmmsMachineParts.states;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.basic.constants.UnitConversionItemFieldsB;
import com.qcadoo.mes.cmmsMachineParts.constants.CmmsMachinePartsConstants;
import com.qcadoo.mes.cmmsMachineParts.constants.DocumentFieldsCMP;
import com.qcadoo.mes.cmmsMachineParts.constants.EventType;
import com.qcadoo.mes.cmmsMachineParts.constants.MachinePartForEventFields;
import com.qcadoo.mes.materialFlow.constants.LocationFields;
import com.qcadoo.mes.materialFlow.constants.MaterialFlowConstants;
import com.qcadoo.mes.materialFlowResources.MaterialFlowResourcesService;
import com.qcadoo.mes.materialFlowResources.constants.MaterialFlowResourcesConstants;
import com.qcadoo.mes.materialFlowResources.constants.PositionFields;
import com.qcadoo.mes.materialFlowResources.service.DocumentBuilder;
import com.qcadoo.mes.materialFlowResources.service.DocumentManagementService;
import com.qcadoo.mes.states.StateChangeContext;
import com.qcadoo.mes.states.constants.StateChangeStatus;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.api.units.PossibleUnitConversions;
import com.qcadoo.model.api.units.UnitConversionService;

@Service
public class EventDocumentsService {

    @Autowired
    private MaterialFlowResourcesService materialFlowResourcesService;

    @Autowired
    private DocumentManagementService documentManagementService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private UnitConversionService unitConversionService;

    @Autowired
    private NumberService numberService;

    public void createDocumentsForMachineParts(final StateChangeContext stateChangeContext) {

        Entity maintenanceEvent = stateChangeContext.getOwner();
        createDocuments(maintenanceEvent);
        if (!maintenanceEvent.isValid()) {
            stateChangeContext.setStatus(StateChangeStatus.FAILURE);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private void createDocuments(final Entity event) {

        EventType eventType = EventType.of(event);

        List<Entity> machinePartsForEvent = event.getHasManyField(eventType.getMachinePartsName());
        if (machinePartsForEvent.isEmpty()) {
            return;
        }

        DataDefinition warehouseDD = dataDefinitionService.get(MaterialFlowConstants.PLUGIN_IDENTIFIER,
                MaterialFlowConstants.MODEL_LOCATION);
        Multimap<Long, Entity> groupedMachinePartsForEvent = groupMachinePartsByLocation(machinePartsForEvent);

        boolean resourcesSufficient = true;
        for (Long warehouseId : groupedMachinePartsForEvent.keySet()) {
            Collection<Entity> machinePartsForLocation = groupedMachinePartsForEvent.get(warehouseId);
            List<Entity> machineParts = machinePartsForLocation.stream()
                    .map(part -> part.getBelongsToField(MachinePartForEventFields.MACHINE_PART)).distinct()
                    .collect(Collectors.toList());
            Entity warehouse = warehouseDD.get(warehouseId);
            Map<Long, BigDecimal> quantitiesInWarehouse = materialFlowResourcesService
                    .getQuantitiesForProductsAndLocation(machineParts, warehouse);

            if (!checkIfResourcesAreSufficient(event, quantitiesInWarehouse, machinePartsForLocation, warehouse)) {
                resourcesSufficient = false;
            }
        }
        if (!resourcesSufficient) {
            return;
        }
        for (Long warehouseId : groupedMachinePartsForEvent.keySet()) {
            Collection<Entity> machinePartsForLocation = groupedMachinePartsForEvent.get(warehouseId);

            Entity warehouse = warehouseDD.get(warehouseId);

            Entity document = createDocumentForLocation(event, eventType, warehouse, machinePartsForLocation);
            if (!document.isValid()) {
                event.addGlobalError("cmmsMachineParts.maintenanceEvent.state.documentNotCreated");
                return;
            }
        }

    }

    private Entity createDocumentForLocation(final Entity event, final EventType eventType, final Entity warehouse,
            final Collection<Entity> machinePartsForLocation) {
        DocumentBuilder documentBuilder = documentManagementService.getDocumentBuilder().internalOutbound(warehouse);
        DataDefinition positionDD = dataDefinitionService.get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER,
                MaterialFlowResourcesConstants.MODEL_POSITION);
        for (Entity machinePartForLocation : machinePartsForLocation) {
            Entity position = positionDD.create();
            Entity product = machinePartForLocation.getBelongsToField(MachinePartForEventFields.MACHINE_PART);
            BigDecimal quantity = machinePartForLocation.getDecimalField(MachinePartForEventFields.PLANNED_QUANTITY);

            String additionalUnit = product.getStringField(ProductFields.ADDITIONAL_UNIT);
            String unit = product.getStringField(ProductFields.UNIT);
            BigDecimal conversion = BigDecimal.ONE;
            if (!StringUtils.isEmpty(additionalUnit)) {
                PossibleUnitConversions unitConversions = unitConversionService.getPossibleConversions(unit,
                        searchCriteriaBuilder -> searchCriteriaBuilder
                                .add(SearchRestrictions.belongsTo(UnitConversionItemFieldsB.PRODUCT, product)));
                if (unitConversions.isDefinedFor(additionalUnit)) {
                    BigDecimal convertedQuantity = unitConversions.convertTo(quantity, additionalUnit);
                    conversion = convertedQuantity.divide(quantity, numberService.getMathContext());
                    position.setField(PositionFields.GIVEN_QUANTITY, convertedQuantity);
                    position.setField(PositionFields.GIVEN_UNIT, additionalUnit);
                }
            } else {
                position.setField(PositionFields.GIVEN_UNIT, unit);
                position.setField(PositionFields.GIVEN_QUANTITY, quantity);
            }
            position.setField(PositionFields.PRODUCT, product);
            position.setField(PositionFields.CONVERSION, conversion);
            position.setField(PositionFields.QUANTITY, quantity);
            documentBuilder.addPosition(position);
        }
        if (eventType.getModelName().equals(CmmsMachinePartsConstants.MODEL_MAINTENANCE_EVENT)) {
            documentBuilder.setField(DocumentFieldsCMP.MAINTENANCE_EVENT, event);

        } else if (eventType.getModelName().equals(CmmsMachinePartsConstants.MODEL_PLANNED_EVENT)) {
            documentBuilder.setField(DocumentFieldsCMP.PLANNED_EVENT, event);

        } else {
            throw new IllegalArgumentException(String.format("Unsupported model type: '%s'", eventType.getModelName()));
        }
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
