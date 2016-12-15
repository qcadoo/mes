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
package com.qcadoo.mes.materialFlowResources.service;

import static com.qcadoo.mes.materialFlow.constants.TransferFields.TIME;
import static com.qcadoo.mes.materialFlowResources.constants.ResourceFields.QUANTITY;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.basic.constants.UnitConversionItemFieldsB;
import com.qcadoo.mes.materialFlow.constants.LocationFields;
import com.qcadoo.mes.materialFlowResources.constants.AttributeValueFields;
import com.qcadoo.mes.materialFlowResources.constants.DocumentFields;
import com.qcadoo.mes.materialFlowResources.constants.LocationFieldsMFR;
import com.qcadoo.mes.materialFlowResources.constants.MaterialFlowResourcesConstants;
import com.qcadoo.mes.materialFlowResources.constants.PositionFields;
import com.qcadoo.mes.materialFlowResources.constants.ReservationFields;
import com.qcadoo.mes.materialFlowResources.constants.ResourceFields;
import com.qcadoo.mes.materialFlowResources.constants.WarehouseAlgorithm;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchOrders;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.api.units.PossibleUnitConversions;
import com.qcadoo.model.api.units.UnitConversionService;

@Service
public class ResourceManagementServiceImpl implements ResourceManagementService {

    private static final String _FIRST_NAME = "firstName";

    private static final String L_LAST_NAME = "lastName";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NumberService numberService;

    @Autowired
    private UnitConversionService unitConversionService;

    @Autowired
    private ResourceStockService resourceStockService;

    @Autowired
    private ReservationsService reservationsService;

    public ResourceManagementServiceImpl() {

    }

    public ResourceManagementServiceImpl(DataDefinitionService dataDefinitionService, NumberService numberService,
            UnitConversionService unitConversionService) {
        this.dataDefinitionService = dataDefinitionService;
        this.numberService = numberService;
        this.unitConversionService = unitConversionService;
    }

    @Override
    @Transactional
    public void createResourcesForReceiptDocuments(final Entity document) {
        Entity warehouse = document.getBelongsToField(DocumentFields.LOCATION_TO);
        Object date = document.getField(DocumentFields.TIME);

        for (Entity position : document.getHasManyField(DocumentFields.POSITIONS)) {
            createResource(document, warehouse, position, date);
        }
    }

    private void setResourceAttributesFromPosition(final Entity resource, final Entity position) {
        List<Entity> attributes = position.getHasManyField(PositionFields.ATRRIBUTE_VALUES);

        for (Entity attribute : attributes) {
            attribute.setField(AttributeValueFields.RESOURCE, resource);
        }

        resource.setField(ResourceFields.ATRRIBUTE_VALUES, attributes);
    }

    private void setPositionAttributesFromResource(final Entity position, final Entity resource) {
        List<Entity> attributes = resource.getHasManyField(ResourceFields.ATRRIBUTE_VALUES);

        List<Entity> newAttributes = Lists.newArrayList();

        DataDefinition attributeDD = dataDefinitionService.get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER,
                MaterialFlowResourcesConstants.MODEL_ATTRIBUTE_VALUE);

        for (Entity attribute : attributes) {
            List<Entity> newAttribute = attributeDD.copy(attribute.getId());

            newAttribute.get(0).setField(AttributeValueFields.POSITION, position);
            newAttribute.get(0).setField(AttributeValueFields.RESOURCE, null);

            newAttributes.addAll(newAttribute);
        }

        position.setField(PositionFields.ATRRIBUTE_VALUES, newAttributes);
    }

    private void setResourceAttributesFromResource(final Entity resource, final Entity baseResource) {
        List<Entity> attributes = baseResource.getHasManyField(ResourceFields.ATRRIBUTE_VALUES);

        List<Entity> newAttributes = Lists.newArrayList();

        DataDefinition attributeDD = dataDefinitionService.get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER,
                MaterialFlowResourcesConstants.MODEL_ATTRIBUTE_VALUE);

        for (Entity attribute : attributes) {
            List<Entity> newAttribute = attributeDD.copy(attribute.getId());

            newAttribute.get(0).setField(AttributeValueFields.RESOURCE, resource);

            newAttributes.addAll(newAttribute);
        }

        resource.setField(ResourceFields.ATRRIBUTE_VALUES, newAttributes);
    }

    public Entity createResource(final Entity document, final Entity warehouse, final Entity position, final Object date) {
        DataDefinition resourceDD = dataDefinitionService.get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER,
                MaterialFlowResourcesConstants.MODEL_RESOURCE);

        Entity product = position.getBelongsToField(PositionFields.PRODUCT);
        Entity resource = resourceDD.create();
        Entity user = document.getBelongsToField(DocumentFields.USER);

        resource.setField(ResourceFields.USER_NAME, user.getStringField(_FIRST_NAME) + " " + user.getStringField(L_LAST_NAME));
        resource.setField(ResourceFields.TIME, date);
        resource.setField(ResourceFields.LOCATION, warehouse);
        resource.setField(ResourceFields.PRODUCT, position.getBelongsToField(PositionFields.PRODUCT));
        resource.setField(ResourceFields.QUANTITY, position.getField(PositionFields.QUANTITY));
        resource.setField(ResourceFields.RESERVED_QUANTITY, BigDecimal.ZERO);
        resource.setField(ResourceFields.AVAILABLE_QUANTITY, position.getDecimalField(PositionFields.QUANTITY));
        resource.setField(ResourceFields.PRICE, position.getField(PositionFields.PRICE));
        resource.setField(ResourceFields.BATCH, position.getField(PositionFields.BATCH));
        resource.setField(ResourceFields.EXPIRATION_DATE, position.getField(PositionFields.EXPIRATION_DATE));
        resource.setField(ResourceFields.PRODUCTION_DATE, position.getField(PositionFields.PRODUCTION_DATE));
        resource.setField(ResourceFields.STORAGE_LOCATION, position.getField(PositionFields.STORAGE_LOCATION));
        resource.setField(ResourceFields.ADDITIONAL_CODE, position.getField(PositionFields.ADDITIONAL_CODE));
        resource.setField(ResourceFields.PALLET_NUMBER, position.getField(PositionFields.PALLET_NUMBER));
        resource.setField(ResourceFields.TYPE_OF_PALLET, position.getField(PositionFields.TYPE_OF_PALLET));
        resource.setField(ResourceFields.WASTE, position.getField(PositionFields.WASTE));

        if (StringUtils.isEmpty(product.getStringField(ProductFields.ADDITIONAL_UNIT))) {
            resource.setField(ResourceFields.GIVEN_UNIT, product.getField(ProductFields.UNIT));
            resource.setField(ResourceFields.QUANTITY_IN_ADDITIONAL_UNIT, position.getField(PositionFields.QUANTITY));
            resource.setField(ResourceFields.CONVERSION, BigDecimal.ONE);
        } else {
            resource.setField(ResourceFields.GIVEN_UNIT, position.getField(PositionFields.GIVEN_UNIT));
            resource.setField(ResourceFields.QUANTITY_IN_ADDITIONAL_UNIT, position.getField(PositionFields.GIVEN_QUANTITY));
            resource.setField(ResourceFields.CONVERSION, position.getField(PositionFields.CONVERSION));
        }

        setResourceAttributesFromPosition(resource, position);

        resourceStockService.addResourceStock(resource);
        return resourceDD.save(resource);
    }

    public Entity createResource(final Entity position, final Entity warehouse, final Entity resource, final BigDecimal quantity,
            Object date) {
        DataDefinition resourceDD = dataDefinitionService.get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER,
                MaterialFlowResourcesConstants.MODEL_RESOURCE);

        Entity newResource = resourceDD.create();

        if (position != null) {
            Entity document = position.getBelongsToField(PositionFields.DOCUMENT);

            if (document != null) {
                Entity user = document.getBelongsToField(DocumentFields.USER);

                newResource.setField(ResourceFields.USER_NAME,
                        user.getStringField(_FIRST_NAME) + " " + user.getStringField(L_LAST_NAME));
            }
        }

        newResource.setField(ResourceFields.TIME, date);
        newResource.setField(ResourceFields.LOCATION, warehouse);
        newResource.setField(ResourceFields.PRODUCT, resource.getBelongsToField(PositionFields.PRODUCT));
        newResource.setField(ResourceFields.QUANTITY, quantity);
        newResource.setField(ResourceFields.AVAILABLE_QUANTITY, quantity);
        newResource.setField(ResourceFields.RESERVED_QUANTITY, BigDecimal.ZERO);
        newResource.setField(ResourceFields.PRICE, resource.getField(PositionFields.PRICE));
        newResource.setField(ResourceFields.BATCH, resource.getField(PositionFields.BATCH));
        newResource.setField(ResourceFields.EXPIRATION_DATE, resource.getField(PositionFields.EXPIRATION_DATE));
        newResource.setField(ResourceFields.PRODUCTION_DATE, resource.getField(PositionFields.PRODUCTION_DATE));
        newResource.setField(ResourceFields.STORAGE_LOCATION, resource.getField(ResourceFields.STORAGE_LOCATION));
        newResource.setField(ResourceFields.ADDITIONAL_CODE, resource.getField(ResourceFields.ADDITIONAL_CODE));
        newResource.setField(ResourceFields.CONVERSION, resource.getField(ResourceFields.CONVERSION));
        newResource.setField(ResourceFields.PALLET_NUMBER, resource.getField(ResourceFields.PALLET_NUMBER));
        newResource.setField(ResourceFields.TYPE_OF_PALLET, resource.getField(ResourceFields.TYPE_OF_PALLET));
        newResource.setField(ResourceFields.GIVEN_UNIT, resource.getField(ResourceFields.GIVEN_UNIT));

        BigDecimal quantityInAdditionalUnit = numberService
                .setScale(quantity.multiply(resource.getDecimalField(ResourceFields.CONVERSION)));

        newResource.setField(ResourceFields.QUANTITY_IN_ADDITIONAL_UNIT, quantityInAdditionalUnit);

        setResourceAttributesFromResource(newResource, resource);

        resourceStockService.addResourceStock(newResource);
        return resourceDD.save(newResource);
    }

    public Entity createResource(final Entity warehouse, final Entity resource, final BigDecimal quantity, Object date) {
        return createResource(null, warehouse, resource, quantity, date);
    }

    private SearchCriteriaBuilder getSearchCriteriaForResourceForProductAndWarehouse(final Entity product,
            final Entity warehouse) {
        return dataDefinitionService
                .get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER, MaterialFlowResourcesConstants.MODEL_RESOURCE).find()
                .add(SearchRestrictions.belongsTo(ResourceFields.LOCATION, warehouse))
                .add(SearchRestrictions.belongsTo(ResourceFields.PRODUCT, product))
                .add(SearchRestrictions.gt(ResourceFields.AVAILABLE_QUANTITY, BigDecimal.ZERO));

    }

    public Multimap<Long, BigDecimal> getQuantitiesInWarehouse(final Entity warehouse,
            Multimap<Entity, Entity> productsAndPositions) {
        Multimap<Long, BigDecimal> result = ArrayListMultimap.create();

        String algorithm = warehouse.getStringField(LocationFieldsMFR.ALGORITHM);

        for (Map.Entry<Entity, Entity> productAndPosition : productsAndPositions.entries()) {
            Entity resource = productAndPosition.getValue().getBelongsToField(PositionFields.RESOURCE);

            if (algorithm.equalsIgnoreCase(WarehouseAlgorithm.MANUAL.getStringValue()) && resource != null) {
                result.put(productAndPosition.getKey().getId(), resource.getDecimalField(ResourceFields.AVAILABLE_QUANTITY));
            } else {
                Entity additionalCode = productAndPosition.getValue().getBelongsToField(PositionFields.ADDITIONAL_CODE);
                BigDecimal conversion = productAndPosition.getValue().getDecimalField(PositionFields.CONVERSION);
                Entity reservation = reservationsService.getReservationForPosition(productAndPosition.getValue());
                List<Entity> resources = Lists.newArrayList();

                if (additionalCode != null) {
                    SearchCriteriaBuilder scb = getSearchCriteriaForResourceForProductAndWarehouse(productAndPosition.getKey(),
                            warehouse);

                    if (!StringUtils.isEmpty(productAndPosition.getKey().getStringField(ProductFields.ADDITIONAL_UNIT))) {
                        scb.add(SearchRestrictions.eq(ResourceFields.CONVERSION, conversion));
                    } else {
                        scb.add(SearchRestrictions.eq(ResourceFields.CONVERSION, BigDecimal.ONE));
                    }

                    resources = scb.add(SearchRestrictions.belongsTo(ResourceFields.ADDITIONAL_CODE, additionalCode)).list()
                            .getEntities();

                    scb = getSearchCriteriaForResourceForProductAndWarehouse(productAndPosition.getKey(), warehouse);

                    if (!StringUtils.isEmpty(productAndPosition.getKey().getStringField(ProductFields.ADDITIONAL_UNIT))) {
                        scb.add(SearchRestrictions.eq(ResourceFields.CONVERSION, conversion));
                    } else {
                        scb.add(SearchRestrictions.eq(ResourceFields.CONVERSION, BigDecimal.ONE));
                    }

                    resources
                            .addAll(scb
                                    .add(SearchRestrictions.or(SearchRestrictions.isNull(ResourceFields.ADDITIONAL_CODE),
                                            SearchRestrictions.ne("additionalCode.id", additionalCode.getId())))
                                    .list().getEntities());
                }

                if (resources.isEmpty()) {
                    SearchCriteriaBuilder scb = getSearchCriteriaForResourceForProductAndWarehouse(productAndPosition.getKey(),
                            warehouse);

                    if (!StringUtils.isEmpty(productAndPosition.getKey().getStringField(ProductFields.ADDITIONAL_UNIT))) {
                        scb.add(SearchRestrictions.eq(ResourceFields.CONVERSION, conversion));
                    } else {
                        scb.add(SearchRestrictions.eq(ResourceFields.CONVERSION, BigDecimal.ONE));
                    }

                    resources = scb.list().getEntities();
                }

                BigDecimal reservedQuantity = BigDecimal.ZERO;
                if (reservation != null) {
                    reservedQuantity = reservation.getDecimalField(ReservationFields.QUANTITY);
                }
                if (result.containsKey(productAndPosition.getKey().getId())) {
                    BigDecimal currentQuantity = result.get(productAndPosition.getKey().getId()).stream().reduce(reservedQuantity,
                            BigDecimal::add);

                    result.put(productAndPosition.getKey().getId(),
                            (resources.stream().map(res -> res.getDecimalField(ResourceFields.AVAILABLE_QUANTITY))
                                    .reduce(BigDecimal.ZERO, BigDecimal::add)).add(currentQuantity));
                } else {
                    result.put(productAndPosition.getKey().getId(),
                            resources.stream().map(res -> res.getDecimalField(ResourceFields.AVAILABLE_QUANTITY))
                                    .reduce(reservedQuantity, BigDecimal::add));
                }
            }
        }

        return result;
    }

    public BigDecimal getQuantityOfProductInWarehouse(final Entity warehouse, final Entity product, Entity position) {
        BigDecimal quantity = BigDecimal.ZERO;

        Entity resource = position.getBelongsToField(PositionFields.RESOURCE);
        Entity reservation = reservationsService.getReservationForPosition(position);
        if (resource != null) {
            quantity = resource.getDecimalField(ResourceFields.AVAILABLE_QUANTITY);
        } else {
            List<Entity> resources = dataDefinitionService
                    .get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER, MaterialFlowResourcesConstants.MODEL_RESOURCE).find()
                    .add(SearchRestrictions.belongsTo(ResourceFields.LOCATION, warehouse))
                    .add(SearchRestrictions.belongsTo(ResourceFields.PRODUCT, product)).list().getEntities();

            for (Entity res : resources) {
                quantity = quantity.add(res.getDecimalField(ResourceFields.AVAILABLE_QUANTITY));
            }
        }

        if (reservation != null) {
            quantity = quantity.add(reservation.getDecimalField(ReservationFields.QUANTITY));
        }
        return quantity;
    }

    private Multimap<Entity, Entity> getProductsAndPositionsFromDocument(final Entity document) {
        Multimap<Entity, Entity> map = ArrayListMultimap.create();

        List<Entity> positions = document.getHasManyField(DocumentFields.POSITIONS);

        positions.stream().forEach(position -> map.put(position.getBelongsToField(PositionFields.PRODUCT), position));

        return map;
    }

    private BigDecimal getQuantityOfProductFromMultimap(final Multimap<Long, BigDecimal> quantitiesForWarehouse,
            final Entity product) {
        List<BigDecimal> quantities = Lists.newArrayList(quantitiesForWarehouse.get(product.getId()));

        return quantities.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    @Transactional
    public void updateResourcesForReleaseDocuments(final Entity document) {
        Entity warehouse = document.getBelongsToField(DocumentFields.LOCATION_FROM);
        WarehouseAlgorithm warehouseAlgorithm;

        boolean enoughResources = true;

        StringBuilder errorMessage = new StringBuilder();

        Multimap<Long, BigDecimal> quantitiesForWarehouse = getQuantitiesInWarehouse(warehouse,
                getProductsAndPositionsFromDocument(document));

        List<Entity> generatedPositions = Lists.newArrayList();

        for (Entity position : document.getHasManyField(DocumentFields.POSITIONS)) {
            Entity product = position.getBelongsToField(PositionFields.PRODUCT);
            Entity resource = position.getBelongsToField(PositionFields.RESOURCE);

            BigDecimal quantityInWarehouse;

            if (resource != null) {
                warehouse = resource.getBelongsToField(ResourceFields.LOCATION);
                warehouseAlgorithm = WarehouseAlgorithm.MANUAL;
            } else {
                warehouse = document.getBelongsToField(DocumentFields.LOCATION_FROM);
                warehouseAlgorithm = WarehouseAlgorithm.parseString(warehouse.getStringField(LocationFieldsMFR.ALGORITHM));
            }

            if (warehouseAlgorithm.equals(WarehouseAlgorithm.MANUAL)) {
                quantityInWarehouse = getQuantityOfProductInWarehouse(warehouse, product, position);
            } else {
                quantityInWarehouse = getQuantityOfProductFromMultimap(quantitiesForWarehouse, product);
            }

            generatedPositions.addAll(updateResources(warehouse, position, warehouseAlgorithm));

            enoughResources = enoughResources && position.isValid();

            if (!position.isValid()) {
                BigDecimal quantity = position.getDecimalField(QUANTITY);

                errorMessage.append(product.getStringField(ProductFields.NAME));
                errorMessage.append(" - ");
                errorMessage.append(numberService.format(quantity.subtract(quantityInWarehouse)));
                errorMessage.append(" ");
                errorMessage.append(product.getStringField(ProductFields.UNIT));
                errorMessage.append(", ");
            }
        }

        if (!enoughResources) {
            addDocumentError(document, warehouse, errorMessage);
        } else {
            deleteReservations(document);
            document.setField(DocumentFields.POSITIONS, generatedPositions);
        }
    }

    private void deleteReservations(Entity document) {

        List<Entity> positions = document.getHasManyField(DocumentFields.POSITIONS);
        for (Entity position : positions) {

            reservationsService.deleteReservationFromDocumentPosition(position);
        }
    }

    private List<Entity> updateResources(Entity warehouse, Entity position, WarehouseAlgorithm warehouseAlgorithm) {
        DataDefinition positionDD = dataDefinitionService.get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER,
                MaterialFlowResourcesConstants.MODEL_POSITION);

        List<Entity> newPositions = Lists.newArrayList();

        Entity product = position.getBelongsToField(PositionFields.PRODUCT);

        List<Entity> resources = getResourcesForWarehouseProductAndAlgorithm(warehouse, product, position, warehouseAlgorithm);

        BigDecimal quantity = position.getDecimalField(PositionFields.QUANTITY);

        resourceStockService.removeResourceStock(product, warehouse, quantity);
        for (Entity resource : resources) {
            BigDecimal resourceQuantity = resource.getDecimalField(ResourceFields.QUANTITY);
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
            // newPosition.setField(PositionFields.GIVEN_UNIT, resource.getField(ResourceFields.GIVEN_UNIT));

            setPositionAttributesFromResource(newPosition, resource);

            if (quantity.compareTo(resourceAvailableQuantity) >= 0) {
                quantity = quantity.subtract(resourceAvailableQuantity, numberService.getMathContext());
                if (resourceQuantity.compareTo(resourceAvailableQuantity) <= 0) {
                    resource.getDataDefinition().delete(resource.getId());
                } else {
                    BigDecimal newResourceQuantity = resourceQuantity.subtract(resourceAvailableQuantity);
                    BigDecimal resourceConversion = resource.getDecimalField(ResourceFields.CONVERSION);
                    BigDecimal quantityInAdditionalUnit = newResourceQuantity.multiply(resourceConversion);
                    resource.setField(ResourceFields.AVAILABLE_QUANTITY, BigDecimal.ZERO);
                    resource.setField(ResourceFields.QUANTITY, newResourceQuantity);
                    resource.setField(ResourceFields.QUANTITY_IN_ADDITIONAL_UNIT,
                            numberService.setScale(quantityInAdditionalUnit));
                    resource.getDataDefinition().save(resource);
                }

                newPosition.setField(PositionFields.QUANTITY, numberService.setScale(resourceAvailableQuantity));

                BigDecimal givenResourceQuantity = convertToGivenUnit(resourceAvailableQuantity, position);

                newPosition.setField(PositionFields.GIVEN_QUANTITY, numberService.setScale(givenResourceQuantity));

                newPositions.add(newPosition);

                if (BigDecimal.ZERO.compareTo(quantity) == 0) {
                    return newPositions;
                }
            } else {
                resourceQuantity = resourceQuantity.subtract(quantity, numberService.getMathContext());
                resourceAvailableQuantity = resourceAvailableQuantity.subtract(quantity, numberService.getMathContext());
                if (position.getBelongsToField(PositionFields.RESOURCE) != null
                        && reservationsService.reservationsEnabledForDocumentPositions()) {
                    BigDecimal reservedQuantity = resource.getDecimalField(ResourceFields.RESERVED_QUANTITY).subtract(quantity,
                            numberService.getMathContext());
                    resource.setField(ResourceFields.RESERVED_QUANTITY, reservedQuantity);
                }
                BigDecimal resourceConversion = resource.getDecimalField(ResourceFields.CONVERSION);
                BigDecimal quantityInAdditionalUnit = resourceQuantity.multiply(resourceConversion);

                resource.setField(ResourceFields.QUANTITY_IN_ADDITIONAL_UNIT, numberService.setScale(quantityInAdditionalUnit));
                resource.setField(ResourceFields.QUANTITY, numberService.setScale(resourceQuantity));
                resource.setField(ResourceFields.AVAILABLE_QUANTITY, resourceAvailableQuantity);

                resource.getDataDefinition().save(resource);
                newPosition.setField(PositionFields.QUANTITY, numberService.setScale(quantity));

                BigDecimal givenQuantity = convertToGivenUnit(quantity, position);

                newPosition.setField(PositionFields.GIVEN_QUANTITY, numberService.setScale(givenQuantity));
                newPositions.add(newPosition);

                return newPositions;
            }
        }

        position.addError(position.getDataDefinition().getField(PositionFields.QUANTITY),
                "materialFlow.error.position.quantity.notEnough");

        return Lists.newArrayList(position);
    }

    public BigDecimal convertToGivenUnit(BigDecimal quantity, Entity position) {
        Entity product = position.getBelongsToField(PositionFields.PRODUCT);
        String baseUnit = product.getStringField(ProductFields.UNIT);
        String givenUnit = position.getStringField(PositionFields.GIVEN_UNIT);

        if (!baseUnit.equals(givenUnit)) {
            PossibleUnitConversions unitConversions = unitConversionService.getPossibleConversions(baseUnit,
                    searchCriteriaBuilder -> searchCriteriaBuilder
                            .add(SearchRestrictions.belongsTo(UnitConversionItemFieldsB.PRODUCT, product)));

            if (unitConversions.isDefinedFor(givenUnit)) {
                return unitConversions.convertTo(quantity, givenUnit);
            }
        }

        return quantity;
    }

    private BigDecimal convertToGivenUnit(BigDecimal quantity, Entity product, String givenUnit) {
        String baseUnit = product.getStringField(ProductFields.UNIT);

        if (!baseUnit.equals(givenUnit)) {
            PossibleUnitConversions unitConversions = unitConversionService.getPossibleConversions(baseUnit,
                    searchCriteriaBuilder -> searchCriteriaBuilder
                            .add(SearchRestrictions.belongsTo(UnitConversionItemFieldsB.PRODUCT, product)));

            if (unitConversions.isDefinedFor(givenUnit)) {
                return unitConversions.convertTo(quantity, givenUnit);
            }
        }

        return quantity;
    }

    @Override
    @Transactional
    public void moveResourcesForTransferDocument(Entity document) {
        Entity warehouseFrom = document.getBelongsToField(DocumentFields.LOCATION_FROM);
        Entity warehouseTo = document.getBelongsToField(DocumentFields.LOCATION_TO);
        Object date = document.getField(DocumentFields.TIME);

        WarehouseAlgorithm warehouseAlgorithm = WarehouseAlgorithm
                .parseString(warehouseFrom.getStringField(LocationFieldsMFR.ALGORITHM));

        boolean enoughResources = true;

        StringBuilder errorMessage = new StringBuilder();

        Multimap<Long, BigDecimal> quantitiesForWarehouse = getQuantitiesInWarehouse(warehouseFrom,
                getProductsAndPositionsFromDocument(document));

        for (Entity position : document.getHasManyField(DocumentFields.POSITIONS)) {
            Entity product = position.getBelongsToField(PositionFields.PRODUCT);
            BigDecimal quantityInWarehouse;

            if (warehouseAlgorithm.equals(WarehouseAlgorithm.MANUAL)) {
                quantityInWarehouse = getQuantityOfProductInWarehouse(warehouseFrom, product, position);
            } else {
                quantityInWarehouse = getQuantityOfProductFromMultimap(quantitiesForWarehouse, product);
            }

            moveResources(warehouseFrom, warehouseTo, position, date, warehouseAlgorithm);
            enoughResources = enoughResources && position.isValid();

            if (!position.isValid()) {
                BigDecimal quantity = position.getDecimalField(QUANTITY);

                errorMessage.append(product.getStringField(ProductFields.NAME));
                errorMessage.append(" - ");
                errorMessage.append(numberService.format(quantity.subtract(quantityInWarehouse)));
                errorMessage.append(" ");
                errorMessage.append(product.getStringField(ProductFields.UNIT));
                errorMessage.append(", ");
            }
        }

        if (!enoughResources) {
            addDocumentError(document, warehouseFrom, errorMessage);
        } else {
            deleteReservations(document);
        }

    }

    private void updateReservations(Entity document) {

        List<Entity> positions = document.getHasManyField(DocumentFields.POSITIONS);
        for (Entity position : positions) {
            reservationsService.updateReservationFromDocumentPosition(position);
        }
    }

    private void addDocumentError(final Entity document, final Entity warehouseFrom, final StringBuilder errorMessage) {
        String warehouseName = warehouseFrom.getStringField(LocationFields.NAME);

        if ((errorMessage.toString().length() + warehouseName.length()) < 255) {
            document.addGlobalError("materialFlow.error.position.quantity.notEnoughResources", false, errorMessage.toString(),
                    warehouseName);
        } else {
            document.addGlobalError("materialFlow.error.position.quantity.notEnoughResourcesShort", false);
        }
    }

    private void moveResources(Entity warehouseFrom, Entity warehouseTo, Entity position, Object date,
            WarehouseAlgorithm warehouseAlgorithm) {
        Entity product = position.getBelongsToField(PositionFields.PRODUCT);

        List<Entity> resources = getResourcesForWarehouseProductAndAlgorithm(warehouseFrom, product, position,
                warehouseAlgorithm);

        BigDecimal quantity = position.getDecimalField(PositionFields.QUANTITY);

        resourceStockService.removeResourceStock(product, warehouseFrom, quantity);
        for (Entity resource : resources) {
            BigDecimal resourceQuantity = resource.getDecimalField(QUANTITY);

            BigDecimal resourceAvailableQuantity = resource.getDecimalField(ResourceFields.AVAILABLE_QUANTITY);
            if (quantity.compareTo(resourceAvailableQuantity) >= 0) {
                quantity = quantity.subtract(resourceAvailableQuantity, numberService.getMathContext());
                if (resourceQuantity.compareTo(resourceAvailableQuantity) <= 0) {
                    resource.getDataDefinition().delete(resource.getId());
                } else {
                    BigDecimal newResourceQuantity = resourceQuantity.subtract(resourceAvailableQuantity);
                    BigDecimal resourceConversion = resource.getDecimalField(ResourceFields.CONVERSION);
                    BigDecimal quantityInAdditionalUnit = newResourceQuantity.multiply(resourceConversion);
                    resource.setField(ResourceFields.AVAILABLE_QUANTITY, BigDecimal.ZERO);
                    resource.setField(ResourceFields.QUANTITY, newResourceQuantity);
                    resource.setField(ResourceFields.QUANTITY_IN_ADDITIONAL_UNIT,
                            numberService.setScale(quantityInAdditionalUnit));
                    resource.getDataDefinition().save(resource);
                }

                createResource(position, warehouseTo, resource, resourceAvailableQuantity, date);

                if (BigDecimal.ZERO.compareTo(quantity) == 0) {
                    return;
                }
            } else {
                resourceQuantity = resourceQuantity.subtract(quantity, numberService.getMathContext());
                resourceAvailableQuantity = resourceAvailableQuantity.subtract(quantity, numberService.getMathContext());
                String givenUnit = resource.getStringField(ResourceFields.GIVEN_UNIT);
                BigDecimal quantityInAdditionalUnit = convertToGivenUnit(resourceQuantity, product, givenUnit);

                resource.setField(ResourceFields.QUANTITY_IN_ADDITIONAL_UNIT, numberService.setScale(quantityInAdditionalUnit));
                resource.setField(ResourceFields.QUANTITY, numberService.setScale(resourceQuantity));
                resource.setField(ResourceFields.AVAILABLE_QUANTITY, resourceAvailableQuantity);

                resource.getDataDefinition().save(resource);

                createResource(position, warehouseTo, resource, quantity, date);

                return;
            }
        }

        position.addError(position.getDataDefinition().getField(PositionFields.QUANTITY),
                "materialFlow.error.position.quantity.notEnough");
    }

    @Override
    public List<Entity> getResourcesForWarehouseProductAndAlgorithm(Entity warehouse, Entity product, Entity position,
            WarehouseAlgorithm warehouseAlgorithm) {
        List<Entity> resources = Lists.newArrayList();

        Entity resource = position.getBelongsToField(PositionFields.RESOURCE);

        Entity additionalCode = position.getBelongsToField(PositionFields.ADDITIONAL_CODE);

        if (resource != null) {
            Entity reservation = reservationsService.getReservationForPosition(position);
            if (reservation != null) {
                BigDecimal reservationQuantity = reservation.getDecimalField(ReservationFields.QUANTITY);
                BigDecimal resourceAvailableQuantity = resource.getDecimalField(ResourceFields.AVAILABLE_QUANTITY);
                resource.setField(ResourceFields.AVAILABLE_QUANTITY, resourceAvailableQuantity.add(reservationQuantity));
            }
            resources.add(resource);
        } else if (WarehouseAlgorithm.FIFO.equals(warehouseAlgorithm)) {
            resources = getResourcesForLocationAndProductFIFO(warehouse, product, additionalCode, position);
        } else if (WarehouseAlgorithm.LIFO.equals(warehouseAlgorithm)) {
            resources = getResourcesForLocationAndProductLIFO(warehouse, product, additionalCode, position);
        } else if (WarehouseAlgorithm.FEFO.equals(warehouseAlgorithm)) {
            resources = getResourcesForLocationAndProductFEFO(warehouse, product, additionalCode, position);
        } else if (WarehouseAlgorithm.LEFO.equals(warehouseAlgorithm)) {
            resources = getResourcesForLocationAndProductLEFO(warehouse, product, additionalCode, position);
        } else if (WarehouseAlgorithm.MANUAL.equals(warehouseAlgorithm)) {
            resources = getResourcesForLocationAndProductMANUAL(warehouse, product, position, additionalCode);
        }

        return resources;
    }

    private List<Entity> getResourcesForLocationAndProductMANUAL(final Entity warehouse, final Entity product,
            final Entity position, final Entity additionalCode) {
        Entity resource = position.getBelongsToField(PositionFields.RESOURCE);

        if (resource != null) {
            return Lists.newArrayList(resource);
        } else {
            return getResourcesForLocationAndProductFIFO(warehouse, product, additionalCode, position);
        }
    }

    private List<Entity> getResourcesForLocationAndProductFIFO(final Entity warehouse, final Entity product,
            final Entity additionalCode, final Entity position) {
        List<Entity> resources = Lists.newArrayList();

        if (additionalCode != null) {
            SearchCriteriaBuilder scb = getSearchCriteriaForResourceForProductAndWarehouse(product, warehouse);

            if (!StringUtils.isEmpty(product.getStringField(ProductFields.ADDITIONAL_UNIT))) {
                scb.add(SearchRestrictions.eq(PositionFields.CONVERSION, position.getDecimalField(PositionFields.CONVERSION)));
            } else {
                scb.add(SearchRestrictions.eq(ResourceFields.CONVERSION, BigDecimal.ONE));
            }

            resources = scb.add(SearchRestrictions.belongsTo(ResourceFields.ADDITIONAL_CODE, additionalCode))
                    .addOrder(SearchOrders.asc(TIME)).list().getEntities();

            scb = getSearchCriteriaForResourceForProductAndWarehouse(product, warehouse);

            if (!StringUtils.isEmpty(product.getStringField(ProductFields.ADDITIONAL_UNIT))) {
                scb.add(SearchRestrictions.eq(PositionFields.CONVERSION, position.getDecimalField(PositionFields.CONVERSION)));
            } else {
                scb.add(SearchRestrictions.eq(ResourceFields.CONVERSION, BigDecimal.ONE));
            }

            resources.addAll(scb
                    .add(SearchRestrictions.or(SearchRestrictions.isNull(ResourceFields.ADDITIONAL_CODE),
                            SearchRestrictions.ne("additionalCode.id", additionalCode.getId())))
                    .addOrder(SearchOrders.asc(TIME)).list().getEntities());
        }

        if (resources.isEmpty()) {
            SearchCriteriaBuilder scb = getSearchCriteriaForResourceForProductAndWarehouse(product, warehouse);

            if (!StringUtils.isEmpty(product.getStringField(ProductFields.ADDITIONAL_UNIT))) {
                scb.add(SearchRestrictions.eq(PositionFields.CONVERSION, position.getDecimalField(PositionFields.CONVERSION)));
            } else {
                scb.add(SearchRestrictions.eq(ResourceFields.CONVERSION, BigDecimal.ONE));
            }

            resources = scb.addOrder(SearchOrders.asc(TIME)).list().getEntities();
        }

        return resources;
    }

    private List<Entity> getResourcesForLocationAndProductLIFO(final Entity warehouse, final Entity product,
            final Entity additionalCode, final Entity position) {
        List<Entity> resources = Lists.newArrayList();

        if (additionalCode != null) {
            SearchCriteriaBuilder scb = getSearchCriteriaForResourceForProductAndWarehouse(product, warehouse);

            if (!StringUtils.isEmpty(product.getStringField(ProductFields.ADDITIONAL_UNIT))) {
                scb.add(SearchRestrictions.eq(PositionFields.CONVERSION, position.getDecimalField(PositionFields.CONVERSION)));
            } else {
                scb.add(SearchRestrictions.eq(ResourceFields.CONVERSION, BigDecimal.ONE));
            }

            resources = scb.add(SearchRestrictions.belongsTo(ResourceFields.ADDITIONAL_CODE, additionalCode))
                    .addOrder(SearchOrders.desc(TIME)).list().getEntities();

            scb = getSearchCriteriaForResourceForProductAndWarehouse(product, warehouse);

            if (!StringUtils.isEmpty(product.getStringField(ProductFields.ADDITIONAL_UNIT))) {
                scb.add(SearchRestrictions.eq(PositionFields.CONVERSION, position.getDecimalField(PositionFields.CONVERSION)));
            } else {
                scb.add(SearchRestrictions.eq(ResourceFields.CONVERSION, BigDecimal.ONE));
            }

            resources.addAll(scb
                    .add(SearchRestrictions.or(SearchRestrictions.isNull(ResourceFields.ADDITIONAL_CODE),
                            SearchRestrictions.ne("additionalCode.id", additionalCode.getId())))
                    .addOrder(SearchOrders.desc(TIME)).list().getEntities());
        }

        if (resources.isEmpty()) {
            SearchCriteriaBuilder scb = getSearchCriteriaForResourceForProductAndWarehouse(product, warehouse);

            if (!StringUtils.isEmpty(product.getStringField(ProductFields.ADDITIONAL_UNIT))) {
                scb.add(SearchRestrictions.eq(PositionFields.CONVERSION, position.getDecimalField(PositionFields.CONVERSION)));
            } else {
                scb.add(SearchRestrictions.eq(ResourceFields.CONVERSION, BigDecimal.ONE));
            }

            resources = scb.addOrder(SearchOrders.desc(TIME)).list().getEntities();
        }

        return resources;
    }

    private List<Entity> getResourcesForLocationAndProductFEFO(final Entity warehouse, final Entity product,
            final Entity additionalCode, final Entity position) {
        List<Entity> resources = Lists.newArrayList();

        if (additionalCode != null) {
            SearchCriteriaBuilder scb = getSearchCriteriaForResourceForProductAndWarehouse(product, warehouse);

            if (!StringUtils.isEmpty(product.getStringField(ProductFields.ADDITIONAL_UNIT))) {
                scb.add(SearchRestrictions.eq(PositionFields.CONVERSION, position.getDecimalField(PositionFields.CONVERSION)));
            } else {
                scb.add(SearchRestrictions.eq(ResourceFields.CONVERSION, BigDecimal.ONE));
            }

            resources = scb.add(SearchRestrictions.belongsTo(ResourceFields.ADDITIONAL_CODE, additionalCode))
                    .addOrder(SearchOrders.asc(ResourceFields.EXPIRATION_DATE)).list().getEntities();

            scb = getSearchCriteriaForResourceForProductAndWarehouse(product, warehouse);

            if (!StringUtils.isEmpty(product.getStringField(ProductFields.ADDITIONAL_UNIT))) {
                scb.add(SearchRestrictions.eq(PositionFields.CONVERSION, position.getDecimalField(PositionFields.CONVERSION)));
            } else {
                scb.add(SearchRestrictions.eq(ResourceFields.CONVERSION, BigDecimal.ONE));
            }

            resources.addAll(scb
                    .add(SearchRestrictions.or(SearchRestrictions.isNull(ResourceFields.ADDITIONAL_CODE),
                            SearchRestrictions.ne("additionalCode.id", additionalCode.getId())))
                    .addOrder(SearchOrders.asc(ResourceFields.EXPIRATION_DATE)).list().getEntities());
        }

        if (resources.isEmpty()) {
            SearchCriteriaBuilder scb = getSearchCriteriaForResourceForProductAndWarehouse(product, warehouse);

            if (!StringUtils.isEmpty(product.getStringField(ProductFields.ADDITIONAL_UNIT))) {
                scb.add(SearchRestrictions.eq(PositionFields.CONVERSION, position.getDecimalField(PositionFields.CONVERSION)));
            } else {
                scb.add(SearchRestrictions.eq(ResourceFields.CONVERSION, BigDecimal.ONE));
            }

            resources = scb.addOrder(SearchOrders.asc(ResourceFields.EXPIRATION_DATE)).list().getEntities();
        }

        return resources;

    }

    private List<Entity> getResourcesForLocationAndProductLEFO(final Entity warehouse, final Entity product,
            final Entity additionalCode, final Entity position) {
        List<Entity> resources = Lists.newArrayList();

        if (additionalCode != null) {
            SearchCriteriaBuilder scb = getSearchCriteriaForResourceForProductAndWarehouse(product, warehouse);

            if (!StringUtils.isEmpty(product.getStringField(ProductFields.ADDITIONAL_UNIT))) {
                scb.add(SearchRestrictions.eq(PositionFields.CONVERSION, position.getDecimalField(PositionFields.CONVERSION)));
            } else {
                scb.add(SearchRestrictions.eq(ResourceFields.CONVERSION, BigDecimal.ONE));
            }

            resources = scb.add(SearchRestrictions.belongsTo(ResourceFields.ADDITIONAL_CODE, additionalCode))
                    .addOrder(SearchOrders.desc(ResourceFields.EXPIRATION_DATE)).list().getEntities();

            scb = getSearchCriteriaForResourceForProductAndWarehouse(product, warehouse);

            if (!StringUtils.isEmpty(product.getStringField(ProductFields.ADDITIONAL_UNIT))) {
                scb.add(SearchRestrictions.eq(PositionFields.CONVERSION, position.getDecimalField(PositionFields.CONVERSION)));
            } else {
                scb.add(SearchRestrictions.eq(ResourceFields.CONVERSION, BigDecimal.ONE));
            }

            resources.addAll(scb
                    .add(SearchRestrictions.or(SearchRestrictions.isNull(ResourceFields.ADDITIONAL_CODE),
                            SearchRestrictions.ne("additionalCode.id", additionalCode.getId())))
                    .addOrder(SearchOrders.desc(ResourceFields.EXPIRATION_DATE)).list().getEntities());
        }

        if (resources.isEmpty()) {
            SearchCriteriaBuilder scb = getSearchCriteriaForResourceForProductAndWarehouse(product, warehouse);

            if (!StringUtils.isEmpty(product.getStringField(ProductFields.ADDITIONAL_UNIT))) {
                scb.add(SearchRestrictions.eq(PositionFields.CONVERSION, position.getDecimalField(PositionFields.CONVERSION)));
            } else {
                scb.add(SearchRestrictions.eq(ResourceFields.CONVERSION, BigDecimal.ONE));
            }

            resources = scb.addOrder(SearchOrders.desc(ResourceFields.EXPIRATION_DATE)).list().getEntities();
        }

        return resources;
    }

}
