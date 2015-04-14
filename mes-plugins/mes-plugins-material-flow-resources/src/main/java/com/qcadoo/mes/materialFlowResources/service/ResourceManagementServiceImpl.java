/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.3
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

import static com.qcadoo.mes.materialFlow.constants.StockCorrectionFields.LOCATION;
import static com.qcadoo.mes.materialFlow.constants.TransferFields.TIME;
import static com.qcadoo.mes.materialFlowResources.constants.ResourceFields.PRODUCT;
import static com.qcadoo.mes.materialFlowResources.constants.ResourceFields.QUANTITY;
import static com.qcadoo.mes.materialFlowResources.constants.ResourceFields.RESOURCE_CORRECTIONS;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.materialFlow.constants.LocationFields;
import com.qcadoo.mes.materialFlowResources.constants.AttributeValueFields;
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
import com.qcadoo.model.api.search.SearchOrders;
import com.qcadoo.model.api.search.SearchRestrictions;

@Service
public class ResourceManagementServiceImpl implements ResourceManagementService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NumberService numberService;

    @Override
    @Transactional
    public void createResourcesForReceiptDocuments(final Entity document) {
        Entity warehouse = document.getBelongsToField(DocumentFields.LOCATION_TO);
        Object date = document.getField(DocumentFields.TIME);
        for (Entity position : document.getHasManyField(DocumentFields.POSITIONS)) {
            createResource(warehouse, position, date);
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

    public Entity createResource(final Entity warehouse, final Entity position, final Object date) {

        DataDefinition resourceDD = dataDefinitionService.get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER,
                MaterialFlowResourcesConstants.MODEL_RESOURCE);

        Entity resource = resourceDD.create();
        resource.setField(ResourceFields.TIME, date);
        resource.setField(ResourceFields.LOCATION, warehouse);
        resource.setField(ResourceFields.PRODUCT, position.getBelongsToField(PositionFields.PRODUCT));
        resource.setField(ResourceFields.QUANTITY, position.getField(PositionFields.QUANTITY));
        resource.setField(ResourceFields.PRICE, position.getField(PositionFields.PRICE));
        resource.setField(ResourceFields.BATCH, position.getField(PositionFields.BATCH));
        resource.setField(ResourceFields.EXPIRATION_DATE, position.getField(PositionFields.EXPIRATION_DATE));
        resource.setField(ResourceFields.PRODUCTION_DATE, position.getField(PositionFields.PRODUCTION_DATE));
        resource.setField(ResourceFields.STORAGE_LOCATION, position.getField(PositionFields.STORAGE_LOCATION));
        setResourceAttributesFromPosition(resource, position);
        return resourceDD.save(resource);
    }

    public Entity createResource(final Entity warehouse, final Entity resource, final BigDecimal quantity, Object date) {

        DataDefinition resourceDD = dataDefinitionService.get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER,
                MaterialFlowResourcesConstants.MODEL_RESOURCE);

        Entity newResource = resourceDD.create();
        newResource.setField(ResourceFields.TIME, date);
        newResource.setField(ResourceFields.LOCATION, warehouse);
        newResource.setField(ResourceFields.PRODUCT, resource.getBelongsToField(PositionFields.PRODUCT));
        newResource.setField(ResourceFields.QUANTITY, quantity);
        newResource.setField(ResourceFields.PRICE, resource.getField(PositionFields.PRICE));
        newResource.setField(ResourceFields.BATCH, resource.getField(PositionFields.BATCH));
        newResource.setField(ResourceFields.EXPIRATION_DATE, resource.getField(PositionFields.EXPIRATION_DATE));
        newResource.setField(ResourceFields.PRODUCTION_DATE, resource.getField(PositionFields.PRODUCTION_DATE));
        newResource.setField(ResourceFields.STORAGE_LOCATION, resource.getField(ResourceFields.STORAGE_LOCATION));
        setResourceAttributesFromResource(newResource, resource);
        return resourceDD.save(newResource);
    }

    public Multimap<Long, BigDecimal> getQuantitiesInWarehouse(final Entity warehouse,
            Multimap<Entity, Entity> productsAndPositions) {

        Multimap<Long, BigDecimal> result = ArrayListMultimap.create();
        String algorithm = warehouse.getStringField(LocationFieldsMFR.ALGORITHM);
        for (Map.Entry<Entity, Entity> productAndPosition : productsAndPositions.entries()) {
            Entity resource = productAndPosition.getValue().getBelongsToField(PositionFields.RESOURCE);

            if (algorithm.equalsIgnoreCase(WarehouseAlgorithm.MANUAL.getStringValue()) && resource != null) {
                result.put(productAndPosition.getKey().getId(), resource.getDecimalField(ResourceFields.QUANTITY));
            } else {

                List<Entity> resources = dataDefinitionService
                        .get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER, MaterialFlowResourcesConstants.MODEL_RESOURCE)
                        .find().add(SearchRestrictions.belongsTo(ResourceFields.LOCATION, warehouse))
                        .add(SearchRestrictions.belongsTo(ResourceFields.PRODUCT, productAndPosition.getKey())).list()
                        .getEntities();
                if (result.containsKey(productAndPosition.getKey().getId())) {
                    BigDecimal currentQuantity = result.get(productAndPosition.getKey().getId()).stream()
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    result.put(productAndPosition.getKey().getId(),
                            (resources.stream().map(res -> res.getDecimalField(ResourceFields.QUANTITY)).reduce(BigDecimal.ZERO,
                                    BigDecimal::add)).add(currentQuantity));
                } else {
                    result.put(
                            productAndPosition.getKey().getId(),
                            resources.stream().map(res -> res.getDecimalField(ResourceFields.QUANTITY))
                                    .reduce(BigDecimal.ZERO, BigDecimal::add));
                }
            }
        }
        return result;
    }

    public BigDecimal getQuantityOfProductInWarehouse(final Entity warehouse, final Entity product, Entity position) {
        BigDecimal quantity = BigDecimal.ZERO;
        String algorithm = warehouse.getStringField(LocationFieldsMFR.ALGORITHM);
        Entity resource = position.getBelongsToField(PositionFields.RESOURCE);
        if (algorithm.equalsIgnoreCase(WarehouseAlgorithm.MANUAL.getStringValue()) && resource != null) {
            quantity = resource.getDecimalField(ResourceFields.QUANTITY);
        } else {
            List<Entity> resources = dataDefinitionService
                    .get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER, MaterialFlowResourcesConstants.MODEL_RESOURCE).find()
                    .add(SearchRestrictions.belongsTo(ResourceFields.LOCATION, warehouse))
                    .add(SearchRestrictions.belongsTo(ResourceFields.PRODUCT, product)).list().getEntities();
            for (Entity res : resources) {
                quantity = quantity.add(res.getDecimalField(ResourceFields.QUANTITY));
            }
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
        WarehouseAlgorithm warehouseAlgorithm = WarehouseAlgorithm.parseString(warehouse
                .getStringField(LocationFieldsMFR.ALGORITHM));
        boolean enoughResources = true;

        StringBuilder errorMessage = new StringBuilder();

        Multimap<Long, BigDecimal> quantitiesForWarehouse = getQuantitiesInWarehouse(warehouse,
                getProductsAndPositionsFromDocument(document));
        List<Entity> generatedPositions = Lists.newArrayList();
        for (Entity position : document.getHasManyField(DocumentFields.POSITIONS)) {
            Entity product = position.getBelongsToField(PositionFields.PRODUCT);
            BigDecimal quantityInWarehouse;
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
        //
        // List<Entity> generatedPositions = Lists.newArrayList();
        // for (Entity position : document.getHasManyField(DocumentFields.POSITIONS)) {
        // Entity product = position.getBelongsToField(PositionFields.PRODUCT);
        // BigDecimal quantityInWarehouse = getQuantityOfProductInWarehouse(warehouse, product, position);
        // generatedPositions.addAll(updateResources(warehouse, position, warehouseAlgorithm));
        // enoughResources = enoughResources && position.isValid();
        // if (!position.isValid()) {
        // BigDecimal quantity = position.getDecimalField(QUANTITY);
        // errorMessage.append(product.getStringField(ProductFields.NAME));
        // errorMessage.append(" - ");
        // errorMessage.append(numberService.format(quantity.subtract(quantityInWarehouse)));
        // errorMessage.append(" ");
        // errorMessage.append(product.getStringField(ProductFields.UNIT));
        // errorMessage.append(", ");
        // }
        // }

        if (!enoughResources) {
            addDocumentError(document, warehouse, errorMessage);
        } else {
            document.setField(DocumentFields.POSITIONS, generatedPositions);
            document.getDataDefinition().save(document);
        }
    }

    private List<Entity> updateResources(Entity warehouse, Entity position, WarehouseAlgorithm warehouseAlgorithm) {
        DataDefinition positionDD = dataDefinitionService.get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER,
                MaterialFlowResourcesConstants.MODEL_POSITION);
        List<Entity> newPositions = Lists.newArrayList();

        Entity product = position.getBelongsToField(PositionFields.PRODUCT);
        List<Entity> resources = getResourcesForWarehouseProductAndAlgorithm(warehouse, product, position, warehouseAlgorithm);

        BigDecimal quantity = position.getDecimalField(PositionFields.QUANTITY);
        for (Entity resource : resources) {
            BigDecimal resourceQuantity = resource.getDecimalField(ResourceFields.QUANTITY);

            Entity newPosition = positionDD.create();
            newPosition.setField(PositionFields.PRODUCT, position.getBelongsToField(PositionFields.PRODUCT));
            newPosition.setField(PositionFields.PRICE, resource.getField(ResourceFields.PRICE));
            newPosition.setField(PositionFields.BATCH, resource.getField(ResourceFields.BATCH));
            newPosition.setField(PositionFields.PRODUCTION_DATE, resource.getField(ResourceFields.PRODUCTION_DATE));
            newPosition.setField(PositionFields.EXPIRATION_DATE, resource.getField(ResourceFields.EXPIRATION_DATE));
            newPosition.setField(PositionFields.RESOURCE, resource);
            newPosition.setField(PositionFields.STORAGE_LOCATION, resource.getField(ResourceFields.STORAGE_LOCATION));
            setPositionAttributesFromResource(newPosition, resource);
            if (quantity.compareTo(resourceQuantity) >= 0) {
                quantity = quantity.subtract(resourceQuantity, numberService.getMathContext());

                resource.getDataDefinition().delete(resource.getId());
                newPosition.setField(PositionFields.QUANTITY, numberService.setScale(resourceQuantity));
                newPositions.add(newPosition);

                if (BigDecimal.ZERO.compareTo(quantity) == 0) {
                    return newPositions;
                }
            } else {
                resourceQuantity = resourceQuantity.subtract(quantity, numberService.getMathContext());

                resource.setField(ResourceFields.QUANTITY, numberService.setScale(resourceQuantity));

                resource.getDataDefinition().save(resource);

                newPosition.setField(PositionFields.QUANTITY, numberService.setScale(quantity));
                newPositions.add(newPosition);
                return newPositions;
            }
        }

        position.addError(position.getDataDefinition().getField(PositionFields.QUANTITY),
                "materialFlow.error.position.quantity.notEnough");
        return Lists.newArrayList(position);
    }

    @Override
    @Transactional
    public void moveResourcesForTransferDocument(Entity document) {
        Entity warehouseFrom = document.getBelongsToField(DocumentFields.LOCATION_FROM);
        Entity warehouseTo = document.getBelongsToField(DocumentFields.LOCATION_TO);
        Object date = document.getField(DocumentFields.TIME);
        WarehouseAlgorithm warehouseAlgorithm = WarehouseAlgorithm.parseString(warehouseFrom
                .getStringField(LocationFieldsMFR.ALGORITHM));
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

        //
        // for (Entity position : document.getHasManyField(DocumentFields.POSITIONS)) {
        // Entity product = position.getBelongsToField(PositionFields.PRODUCT);
        // BigDecimal quantityInWarehouse = getQuantityOfProductInWarehouse(warehouseFrom, product, position);
        // moveResources(warehouseFrom, warehouseTo, position, date, warehouseAlgorithm);
        // enoughResources = enoughResources && position.isValid();
        // if (!position.isValid()) {
        // BigDecimal quantity = position.getDecimalField(QUANTITY);
        // errorMessage.append(product.getStringField(ProductFields.NAME));
        // errorMessage.append(" - ");
        // errorMessage.append(numberService.format(quantity.subtract(quantityInWarehouse)));
        // errorMessage.append(" ");
        // errorMessage.append(product.getStringField(ProductFields.UNIT));
        // errorMessage.append(", ");
        // }
        // }

        if (!enoughResources) {
            addDocumentError(document, warehouseFrom, errorMessage);
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
        List<Entity> resources = getResourcesForWarehouseProductAndAlgorithm(warehouseFrom, product, position, warehouseAlgorithm);

        BigDecimal quantity = position.getDecimalField(PositionFields.QUANTITY);
        for (Entity resource : resources) {
            BigDecimal resourceQuantity = resource.getDecimalField(QUANTITY);

            if (quantity.compareTo(resourceQuantity) >= 0) {
                quantity = quantity.subtract(resourceQuantity, numberService.getMathContext());
                resource.getDataDefinition().delete(resource.getId());

                createResource(warehouseTo, resource, resourceQuantity, date);

                if (BigDecimal.ZERO.compareTo(quantity) == 0) {
                    return;
                }
            } else {
                resourceQuantity = resourceQuantity.subtract(quantity, numberService.getMathContext());

                resource.setField(QUANTITY, numberService.setScale(resourceQuantity));

                resource.getDataDefinition().save(resource);

                createResource(warehouseTo, resource, quantity, date);

                return;
            }
        }

        position.addError(position.getDataDefinition().getField(PositionFields.QUANTITY),
                "materialFlow.error.position.quantity.notEnough");
    }

    public List<Entity> getResourcesForWarehouseProductAndAlgorithm(Entity warehouse, Entity product, Entity position,
            WarehouseAlgorithm warehouseAlgorithm) {
        List<Entity> resources = Lists.newArrayList();
        if (WarehouseAlgorithm.FIFO.equals(warehouseAlgorithm)) {
            resources = getResourcesForLocationAndProductFIFO(warehouse, product);
        } else if (WarehouseAlgorithm.LIFO.equals(warehouseAlgorithm)) {
            resources = getResourcesForLocationAndProductLIFO(warehouse, product);
        } else if (WarehouseAlgorithm.FEFO.equals(warehouseAlgorithm)) {
            resources = getResourcesForLocationAndProductFEFO(warehouse, product);
        } else if (WarehouseAlgorithm.LEFO.equals(warehouseAlgorithm)) {
            resources = getResourcesForLocationAndProductLEFO(warehouse, product);
        } else if (WarehouseAlgorithm.MANUAL.equals(warehouseAlgorithm)) {
            resources = getResourcesForLocationAndProductMANUAL(warehouse, product, position);
        }

        return resources;
    }

    private List<Entity> getResourcesForLocationAndProductMANUAL(final Entity warehouse, final Entity product,
            final Entity position) {
        Entity resource = position.getBelongsToField(PositionFields.RESOURCE);
        if (resource != null) {
            return Lists.newArrayList(resource);
        } else {
            return getResourcesForLocationAndProductFIFO(warehouse, product);
        }
    }

    private List<Entity> getResourcesForLocationAndProductFIFO(final Entity warehouse, final Entity product) {
        List<Entity> resources = dataDefinitionService
                .get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER, MaterialFlowResourcesConstants.MODEL_RESOURCE).find()
                .add(SearchRestrictions.belongsTo(LOCATION, warehouse)).add(SearchRestrictions.belongsTo(PRODUCT, product))
                .addOrder(SearchOrders.asc(TIME)).list().getEntities();

        return resources;
    }

    private List<Entity> getResourcesForLocationAndProductLIFO(final Entity warehouse, final Entity product) {
        List<Entity> resources = dataDefinitionService
                .get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER, MaterialFlowResourcesConstants.MODEL_RESOURCE).find()
                .add(SearchRestrictions.belongsTo(LOCATION, warehouse)).add(SearchRestrictions.belongsTo(PRODUCT, product))
                .addOrder(SearchOrders.desc(TIME)).list().getEntities();

        return resources;
    }

    private List<Entity> getResourcesForLocationAndProductFEFO(final Entity warehouse, final Entity product) {
        List<Entity> resources = dataDefinitionService
                .get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER, MaterialFlowResourcesConstants.MODEL_RESOURCE).find()
                .add(SearchRestrictions.belongsTo(LOCATION, warehouse)).add(SearchRestrictions.belongsTo(PRODUCT, product))
                .addOrder(SearchOrders.asc(ResourceFields.EXPIRATION_DATE)).list().getEntities();

        return resources;
    }

    private List<Entity> getResourcesForLocationAndProductLEFO(final Entity warehouse, final Entity product) {
        List<Entity> resources = dataDefinitionService
                .get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER, MaterialFlowResourcesConstants.MODEL_RESOURCE).find()
                .add(SearchRestrictions.belongsTo(LOCATION, warehouse)).add(SearchRestrictions.belongsTo(PRODUCT, product))
                .addOrder(SearchOrders.desc(ResourceFields.EXPIRATION_DATE)).list().getEntities();

        return resources;
    }

}
