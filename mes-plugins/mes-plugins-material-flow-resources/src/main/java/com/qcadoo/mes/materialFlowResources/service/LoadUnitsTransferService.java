package com.qcadoo.mes.materialFlowResources.service;

import com.qcadoo.mes.materialFlowResources.constants.*;
import com.qcadoo.model.api.*;
import com.qcadoo.model.api.exception.EntityRuntimeException;
import com.qcadoo.model.api.search.SearchRestrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Objects;

import static com.qcadoo.model.api.search.SearchOrders.asc;
import static com.qcadoo.model.api.search.SearchProjections.*;

@Service
public class LoadUnitsTransferService {

    @Autowired
    private NumberService numberService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public Entity createPosition(final Entity document, final Entity resource) {
        DataDefinition positionDD = getPositionDD();

        Entity newPosition = positionDD.create();

        BigDecimal conversion = resource.getDecimalField(ResourceFields.CONVERSION);
        BigDecimal availableQuantity = resource.getDecimalField(ResourceFields.AVAILABLE_QUANTITY);
        BigDecimal givenQuantity = availableQuantity.multiply(conversion, numberService.getMathContext());

        newPosition.setField(PositionFields.DOCUMENT, document);
        newPosition.setField(PositionFields.PRODUCT, resource.getBelongsToField(ResourceFields.PRODUCT));
        newPosition.setField(PositionFields.QUANTITY, availableQuantity);
        newPosition.setField(PositionFields.GIVEN_QUANTITY, givenQuantity);
        newPosition.setField(PositionFields.GIVEN_UNIT, resource.getStringField(ResourceFields.GIVEN_UNIT));
        newPosition.setField(PositionFields.PRICE, resource.getField(ResourceFields.PRICE));
        newPosition.setField(PositionFields.BATCH, resource.getField(ResourceFields.BATCH));
        newPosition.setField(PositionFields.PRODUCTION_DATE, resource.getField(ResourceFields.PRODUCTION_DATE));
        newPosition.setField(PositionFields.EXPIRATION_DATE, resource.getField(ResourceFields.EXPIRATION_DATE));
        newPosition.setField(PositionFields.RESOURCE, resource);
        newPosition.setField(PositionFields.RESOURCE_NUMBER, resource.getStringField(ResourceFields.NUMBER));
        newPosition.setField(PositionFields.STORAGE_LOCATION, resource.getField(ResourceFields.STORAGE_LOCATION));
        newPosition.setField(PositionFields.CONVERSION, conversion);
        newPosition.setField(PositionFields.PALLET_NUMBER, resource.getField(ResourceFields.PALLET_NUMBER));
        newPosition.setField(PositionFields.TYPE_OF_LOAD_UNIT, resource.getField(ResourceFields.TYPE_OF_LOAD_UNIT));
        newPosition.setField(PositionFields.WASTE, resource.getField(ResourceFields.WASTE));

        if (!validateAvailableQuantity(document, newPosition)) {
            throw new EntityRuntimeException(newPosition);
        }
        return positionDD.save(newPosition);
    }


    private boolean validateAvailableQuantity(Entity document, Entity position) {
        String type = document.getStringField(DocumentFields.TYPE);
        if (DocumentType.isOutbound(type)) {
            Entity location = document.getBelongsToField(DocumentFields.LOCATION_FROM);
            boolean enabled = location.getBooleanField(LocationFieldsMFR.DRAFT_MAKES_RESERVATION);
            if (enabled) {
                BigDecimal availableQuantity = getAvailableQuantityForProductAndLocation(
                        position.getBelongsToField(PositionFields.PRODUCT), location);
                BigDecimal quantity = position.getDecimalField(PositionFields.QUANTITY);
                if (availableQuantity == null || quantity.compareTo(availableQuantity) > 0) {
                    return false;
                } else if (Objects.nonNull(position.getBelongsToField(PositionFields.RESOURCE))) {
                    BigDecimal resourceAvailableQuantity = getAvailableQuantityForResource(
                            position.getBelongsToField(PositionFields.RESOURCE));
                    return resourceAvailableQuantity != null && quantity.compareTo(resourceAvailableQuantity) <= 0;
                }
            }
        }
        return true;
    }

    private BigDecimal getAvailableQuantityForResource(Entity resource) {
        return resource.getDecimalField(ResourceFields.AVAILABLE_QUANTITY);
    }

    private BigDecimal getAvailableQuantityForProductAndLocation(Entity product, Entity location) {
        Entity resourceStockDto = dataDefinitionService
                .get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER, MaterialFlowResourcesConstants.MODEL_RESOURCE_STOCK_DTO)
                .find().add(SearchRestrictions.eq(ResourceStockDtoFields.PRODUCT_ID, product.getId().intValue()))
                .add(SearchRestrictions.eq(ResourceStockDtoFields.LOCATION_ID, location.getId().intValue())).setMaxResults(1)
                .uniqueResult();
        BigDecimal quantity = BigDecimal.ZERO;
        if (Objects.nonNull(resourceStockDto)) {
            quantity = BigDecimalUtils.convertNullToZero(resourceStockDto.getDecimalField(ResourceStockDtoFields.QUANTITY));
        }

        Entity reservationsQuantity = dataDefinitionService
                .get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER, MaterialFlowResourcesConstants.MODEL_RESERVATION)
                .find().add(SearchRestrictions.belongsTo(ReservationFields.PRODUCT, product))
                .add(SearchRestrictions.belongsTo(ReservationFields.LOCATION, location))
                .setProjection(list().add(alias(sum(ReservationFields.QUANTITY), "sum")).add(rowCount()))
                .addOrder(asc("sum")).setMaxResults(1).uniqueResult();

        if (Objects.nonNull(reservationsQuantity)) {
            quantity = quantity.subtract(BigDecimalUtils.convertNullToZero(reservationsQuantity.getDecimalField("sum")));
        }
        return quantity;
    }

    private DataDefinition getPositionDD() {
        return dataDefinitionService.get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER,
                MaterialFlowResourcesConstants.MODEL_POSITION);
    }
}
