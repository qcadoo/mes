package com.qcadoo.mes.materialFlowResources.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.hibernate.exception.LockAcquisitionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;
import com.qcadoo.mes.basic.constants.ProductFields;
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
import com.qcadoo.model.api.validators.ErrorMessage;
import com.qcadoo.security.api.UserService;
import com.qcadoo.security.constants.UserFields;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;

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

    @Autowired
    private UserService userService;

    private static final Logger logger = LoggerFactory.getLogger(ResourceReservationsService.class);

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
            resource = dataDefinitionService.get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER,
                    MaterialFlowResourcesConstants.MODEL_RESOURCE).get(resource.getId());
            if (resource != null) {
                BigDecimal reservedQuantity = resource.getDecimalField(ResourceFields.RESERVED_QUANTITY).add(quantityToAdd);
                BigDecimal quantity = resource.getDecimalField(ResourceFields.QUANTITY);
                resource.setField(ResourceFields.AVAILABLE_QUANTITY, quantity.subtract(reservedQuantity));
                resource.setField(ResourceFields.RESERVED_QUANTITY, reservedQuantity);
                resource.getDataDefinition().save(resource);
            }
        }
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void fillResourcesInDocument(final ViewDefinitionState view, final Entity document) throws LockAcquisitionException {
        logger.info("FILL RESOURCES STARTED IN DOCUMENT: id = " + document.getId() + " number = "
                + document.getStringField(DocumentFields.NUMBER));
        logger.info("USER STARTED IN DOCUMENT: id = " + document.getId() + ": "
                + userService.getCurrentUserEntity().getStringField(UserFields.USER_NAME));
        List<Entity> positions = document.getHasManyField(DocumentFields.POSITIONS);
        logger.info("INITIAL POSITIONS IN DOCUMENT: id = " + document.getId() + ": size = " + positions.size());
        logger.info(positions.toString());
        Entity warehouse = document.getBelongsToField(DocumentFields.LOCATION_FROM);
        WarehouseAlgorithm warehouseAlgorithm = WarehouseAlgorithm.parseString(warehouse
                .getStringField(LocationFieldsMFR.ALGORITHM));
        List<ErrorMessage> errors = Lists.newArrayList();
        boolean valid = true;

        for (Entity position : positions) {
            if (position.getBelongsToField(PositionFields.RESOURCE) == null) {
                List<Entity> newPositions = matchResourcesToPosition(position, warehouse, warehouseAlgorithm);
                if (!newPositions.isEmpty()) {
                    logger.info("GENERATED POSITIONS IN DOCUMENT: id = " + document.getId() + ", FOR POSITION: id = " + position.getId() + ", size = " + newPositions.size());
                    logger.info(newPositions.toString());
                    if(newPositions.size() > 1) {
                        position.getDataDefinition().delete(position.getId());
                        for (Entity newPosition : newPositions) {
                            newPosition.setField(PositionFields.DOCUMENT, document);
                            Entity saved = newPosition.getDataDefinition().save(newPosition);
                            valid = valid && saved.isValid();
                            errors.addAll(saved.getGlobalErrors());
                            if(!saved.getErrors().isEmpty()){
                                view.addMessage("materialFlow.document.fillResources.global.error.positionNotValid", ComponentState.MessageType.FAILURE, false, position.getBelongsToField(PositionFields.PRODUCT).getStringField(ProductFields.NUMBER));
                            }
                        }
                    } else {
                        Entity newPosition = newPositions.get(0);
                        position.setField(PositionFields.PRICE, newPosition.getField(PositionFields.PRICE));
                        position.setField(PositionFields.BATCH, newPosition.getField(PositionFields.BATCH));
                        position.setField(PositionFields.PRODUCTION_DATE, newPosition.getField(PositionFields.PRODUCTION_DATE));
                        position.setField(PositionFields.EXPIRATION_DATE, newPosition.getField(PositionFields.EXPIRATION_DATE));
                        position.setField(PositionFields.RESOURCE, newPosition.getField(PositionFields.RESOURCE));
                        position.setField(PositionFields.STORAGE_LOCATION, newPosition.getField(PositionFields.STORAGE_LOCATION));
                        position.setField(PositionFields.ADDITIONAL_CODE, newPosition.getField(PositionFields.ADDITIONAL_CODE));
                        position.setField(PositionFields.PALLET_NUMBER, newPosition.getField(PositionFields.PALLET_NUMBER));
                        position.setField(PositionFields.TYPE_OF_PALLET, newPosition.getField(PositionFields.TYPE_OF_PALLET));
                        position.setField(PositionFields.WASTE, newPosition.getField(PositionFields.WASTE));
                        position.setField(PositionFields.QUANTITY, newPosition.getField(PositionFields.QUANTITY));
                        position.setField(PositionFields.GIVEN_QUANTITY, newPosition.getField(PositionFields.GIVEN_QUANTITY));
                        Entity saved = position.getDataDefinition().save(position);
                        valid = valid && saved.isValid();
                        errors.addAll(saved.getGlobalErrors());
                        if(!saved.getErrors().isEmpty()){
                            view.addMessage("materialFlow.document.fillResources.global.error.positionNotValid", ComponentState.MessageType.FAILURE, false, position.getBelongsToField(PositionFields.PRODUCT).getStringField(ProductFields.NUMBER));
                        }
                    }
                }
            }
        }
        if (valid) {
            logger.info("FILL RESOURCES ENDED SUCCESSFULLY FOR DOCUMENT: id = " + document.getId() + " number = "
                    + document.getStringField(DocumentFields.NUMBER));
            return;
        } else {
            errors.forEach(view::addMessage);
        }

        logger.warn("FILL RESOURCES ENDED WITH ERRORS FOR DOCUMENT: id = " + document.getId() + " number = "
                + document.getStringField(DocumentFields.NUMBER));
        throw new IllegalStateException("Unable to fill resources in document.");
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
            if(resource.getBooleanField(ResourceFields.WASTE)){
                continue;
            }
            logger.info("DOCUMENT: " + position.getBelongsToField(PositionFields.DOCUMENT).getId() + " POSITION: "
                    + position.toString());
            logger.info("RESOURCE USED: " + resource.toString());

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
            newPosition.setField(PositionFields.WASTE, resource.getField(ResourceFields.WASTE));

            if (quantity.compareTo(resourceAvailableQuantity) >= 0) {
                quantity = quantity.subtract(resourceAvailableQuantity, numberService.getMathContext());

                newPosition.setField(PositionFields.QUANTITY, numberService.setScale(resourceAvailableQuantity));

                BigDecimal givenResourceQuantity = resourceManagementService.convertToGivenUnit(resourceAvailableQuantity,
                        position);

                newPosition.setField(PositionFields.GIVEN_QUANTITY, numberService.setScale(givenResourceQuantity));

                newPositions.add(newPosition);

                if (BigDecimal.ZERO.compareTo(quantity) == 0) {
                    return newPositions;
                }
            } else {

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
