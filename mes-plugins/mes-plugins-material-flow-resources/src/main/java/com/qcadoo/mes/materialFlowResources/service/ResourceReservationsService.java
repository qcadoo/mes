package com.qcadoo.mes.materialFlowResources.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.qcadoo.mes.materialFlowResources.constants.DocumentFields;
import com.qcadoo.mes.materialFlowResources.constants.LocationFieldsMFR;
import com.qcadoo.mes.materialFlowResources.constants.MaterialFlowResourcesConstants;
import com.qcadoo.mes.materialFlowResources.constants.PositionFields;
import com.qcadoo.mes.materialFlowResources.constants.ResourceFields;
import com.qcadoo.mes.materialFlowResources.constants.WarehouseAlgorithm;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;

@Service
public class ResourceReservationsService {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private ResourceManagementService resourceManagementService;

    @Autowired
    private NumberService numberService;

    public void updateResourceQuantites(Map<String, Object> params, BigDecimal quantityToAdd) {
        if (params.get("resource_id") != null) {
            params.put("quantity_to_add", quantityToAdd);
            String query = "UPDATE materialflowresources_resource SET reservedquantity = reservedquantity + :quantity_to_add, "
                    + "availablequantity = availablequantity - :quantity_to_add WHERE id = :resource_id";
            jdbcTemplate.update(query, params);
        }
    }

    public void updateResourceQuantites(Entity position, BigDecimal quantityToAdd) {
        Entity resource = position.getBelongsToField(PositionFields.RESOURCE);
        if (resource != null) {
            resource = dataDefinitionService
                    .get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER, MaterialFlowResourcesConstants.MODEL_RESOURCE)
                    .get(resource.getId());
            if (resource != null) {
                BigDecimal reservedQuantity = resource.getDecimalField(ResourceFields.RESERVED_QUANTITY).add(quantityToAdd);
                BigDecimal quantity = resource.getDecimalField(ResourceFields.QUANTITY);
                resource.setField(ResourceFields.AVAILABLE_QUANTITY, quantity.subtract(reservedQuantity));
                resource.setField(ResourceFields.RESERVED_QUANTITY, reservedQuantity);
                resource.getDataDefinition().save(resource);
            }
        }
    }

    public Entity fillResourcesInDocument(final Entity document) {
        List<Entity> positions = document.getHasManyField(DocumentFields.POSITIONS);
        Entity warehouse = document.getBelongsToField(DocumentFields.LOCATION_FROM);
        WarehouseAlgorithm warehouseAlgorithm = WarehouseAlgorithm
                .parseString(warehouse.getStringField(LocationFieldsMFR.ALGORITHM));
        List<Entity> generatedPositions = Lists.newArrayList();

        for (Entity position : positions) {
            if (position.getBelongsToField(PositionFields.RESOURCE) == null) {
                List<Entity> newPositions = matchResourcesToPosition(position, warehouse, warehouseAlgorithm);
                if (newPositions.isEmpty()) {
                    generatedPositions.add(position);
                } else {
                    position.getDataDefinition().delete(position.getId());
                    generatedPositions.addAll(newPositions);
                }
            } else {
                generatedPositions.add(position);
            }
        }
        document.setField(DocumentFields.POSITIONS, generatedPositions);
        return document.getDataDefinition().save(document);
    }

    private List<Entity> matchResourcesToPosition(final Entity position, final Entity warehouse,
            final WarehouseAlgorithm warehouseAlgorithm) {
        DataDefinition positionDD = dataDefinitionService.get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER,
                MaterialFlowResourcesConstants.MODEL_POSITION);

        List<Entity> newPositions = Lists.newArrayList();

        Entity product = position.getBelongsToField(PositionFields.PRODUCT);

        List<Entity> resources = resourceManagementService.getResourcesForWarehouseProductAndAlgorithm(warehouse, product,
                position, warehouseAlgorithm);
        BigDecimal quantity = position.getDecimalField(PositionFields.QUANTITY);
        for (Entity resource : resources) {
            BigDecimal resourceAvailableQuantity = resource.getDecimalField(ResourceFields.AVAILABLE_QUANTITY);

            Entity newPosition = positionDD.create();

            newPosition.setField(PositionFields.PRODUCT, position.getBelongsToField(PositionFields.PRODUCT));
            newPosition.setField(PositionFields.GIVEN_QUANTITY, position.getDecimalField(PositionFields.GIVEN_QUANTITY));
            newPosition.setField(PositionFields.GIVEN_UNIT, position.getStringField(PositionFields.GIVEN_UNIT));
            newPosition.setField(PositionFields.PRICE, resource.getField(ResourceFields.PRICE));
            newPosition.setField(PositionFields.BATCH, resource.getField(ResourceFields.BATCH));
            newPosition.setField(PositionFields.PRODUCTION_DATE, resource.getField(ResourceFields.PRODUCTION_DATE));
            newPosition.setField(PositionFields.EXPIRATION_DATE, resource.getField(ResourceFields.EXPIRATION_DATE));
            newPosition.setField(PositionFields.RESOURCE, resource);
            newPosition.setField(PositionFields.STORAGE_LOCATION, resource.getField(ResourceFields.STORAGE_LOCATION));
            newPosition.setField(PositionFields.ADDITIONAL_CODE, resource.getField(ResourceFields.ADDITIONAL_CODE));
            newPosition.setField(PositionFields.CONVERSION, position.getField(PositionFields.CONVERSION));
            newPosition.setField(PositionFields.PALLET_NUMBER, resource.getField(ResourceFields.PALLET_NUMBER));
            newPosition.setField(PositionFields.TYPE_OF_PALLET, resource.getField(ResourceFields.TYPE_OF_PALLET));

            if (quantity.compareTo(resourceAvailableQuantity) >= 0) {
                quantity = quantity.subtract(resourceAvailableQuantity, numberService.getMathContext());

                // updateResourceQuantites(newPosition, resourceAvailableQuantity);

                newPosition.setField(PositionFields.QUANTITY, numberService.setScale(resourceAvailableQuantity));

                BigDecimal givenResourceQuantity = resourceManagementService.convertToGivenUnit(resourceAvailableQuantity,
                        position);

                newPosition.setField(PositionFields.GIVEN_QUANTITY, numberService.setScale(givenResourceQuantity));

                newPositions.add(newPosition);

                if (BigDecimal.ZERO.compareTo(quantity) == 0) {
                    return newPositions;
                }
            } else {

                // updateResourceQuantites(newPosition, quantity);

                newPosition.setField(PositionFields.QUANTITY, numberService.setScale(quantity));

                BigDecimal givenQuantity = resourceManagementService.convertToGivenUnit(quantity, position);

                newPosition.setField(PositionFields.GIVEN_QUANTITY, numberService.setScale(givenQuantity));
                newPositions.add(newPosition);

                return newPositions;
            }
        }
        return Lists.newArrayList();
    }
}
