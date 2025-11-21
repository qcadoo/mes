/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.4
 * <p>
 * This file is part of Qcadoo.
 * <p>
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */
package com.qcadoo.mes.materialFlowResources.service;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.qcadoo.commons.functional.Either;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.basic.CalculationQuantityService;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.basic.constants.ProductAttributeValueFields;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.basic.constants.StaffFields;
import com.qcadoo.mes.basic.constants.UnitConversionItemFieldsB;
import com.qcadoo.mes.materialFlow.constants.LocationFields;
import com.qcadoo.mes.materialFlowResources.DocumentPositionService;
import com.qcadoo.mes.materialFlowResources.PalletValidatorService;
import com.qcadoo.mes.materialFlowResources.constants.*;
import com.qcadoo.mes.materialFlowResources.exceptions.InvalidResourceException;
import com.qcadoo.mes.materialFlowResources.helpers.NotEnoughResourcesErrorMessageCopyToEntityHelper;
import com.qcadoo.mes.materialFlowResources.helpers.NotEnoughResourcesErrorMessageHolder;
import com.qcadoo.mes.materialFlowResources.helpers.NotEnoughResourcesErrorMessageHolderFactory;
import com.qcadoo.model.api.*;
import com.qcadoo.model.api.exception.EntityRuntimeException;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchOrder;
import com.qcadoo.model.api.search.SearchOrders;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.api.units.PossibleUnitConversions;
import com.qcadoo.model.api.units.UnitConversionService;
import com.qcadoo.model.api.validators.ErrorMessage;
import com.qcadoo.security.api.UserService;
import com.qcadoo.security.constants.UserFields;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.exception.LockAcquisitionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Service
public class ResourceManagementServiceImpl implements ResourceManagementService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceManagementServiceImpl.class);

    private static final String L_ORDER = "order";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NumberService numberService;

    @Autowired
    private ParameterService parameterService;

    @Autowired
    private UserService userService;

    @Autowired
    private PalletNumberDisposalService palletNumberDisposalService;

    @Autowired
    private ResourceStockService resourceStockService;

    @Autowired
    private ReservationsService reservationsService;

    @Autowired
    private NotEnoughResourcesErrorMessageHolderFactory notEnoughResourcesErrorMessageHolderFactory;

    @Autowired
    private CalculationQuantityService calculationQuantityService;

    @Autowired
    private DocumentPositionService documentPositionService;

    @Autowired
    private UnitConversionService unitConversionService;

    @Autowired
    private PalletValidatorService palletValidatorService;

    @Autowired
    private TranslationService translationService;

    @Override
    @Transactional
    public void createResources(final Entity document) {
        DocumentType documentType = DocumentType.of(document);

        if (DocumentType.RECEIPT.equals(documentType) || DocumentType.INTERNAL_INBOUND.equals(documentType)) {
            createResourcesForReceiptDocuments(document);
        } else if (DocumentType.INTERNAL_OUTBOUND.equals(documentType) || DocumentType.RELEASE.equals(documentType)) {
            updateResourcesForReleaseDocuments(document);
        } else if (DocumentType.TRANSFER.equals(documentType)) {
            moveResourcesForTransferDocument(document);
        } else {
            throw new IllegalStateException("Unsupported document type");
        }
    }

    @Override
    @Transactional
    public void createResourcesForReceiptDocuments(final Entity document) {
        Entity warehouse = document.getBelongsToField(DocumentFields.LOCATION_TO);
        Object date = document.getField(DocumentFields.TIME);

        for (Entity position : document.getHasManyField(DocumentFields.POSITIONS)) {
            createResource(document, warehouse, position, date);

            position = position.getDataDefinition().save(position);

            if (!position.isValid()) {
                document.setNotValid();

                position.getGlobalErrors().forEach(e -> document.addGlobalError(e.getMessage(), e.getAutoClose(), e.getVars()));
                position.getErrors().values()
                        .forEach(e -> document.addGlobalError(e.getMessage(), e.getAutoClose(), e.getVars()));
            }
        }
    }

    private void createResource(final Entity document, final Entity warehouse, final Entity position,
                                final Object date) {
        DataDefinition resourceDD = dataDefinitionService.get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER,
                MaterialFlowResourcesConstants.MODEL_RESOURCE);

        Entity product = position.getBelongsToField(PositionFields.PRODUCT);
        Entity resource = resourceDD.create();
        Entity user = document.getBelongsToField(DocumentFields.USER);
        Entity delivery = document.getBelongsToField(ResourceFields.DELIVERY);

        resource.setField(ResourceFields.USER_NAME,
                user.getStringField(UserFields.FIRST_NAME) + " " + user.getStringField(UserFields.LAST_NAME));
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
        resource.setField(ResourceFields.PALLET_NUMBER, position.getField(PositionFields.PALLET_NUMBER));
        resource.setField(ResourceFields.TYPE_OF_LOAD_UNIT, position.getField(PositionFields.TYPE_OF_LOAD_UNIT));
        resource.setField(ResourceFields.WASTE, position.getField(PositionFields.WASTE));
        resource.setField(ResourceFields.DOCUMENT_NUMBER, document.getStringField(DocumentFields.NUMBER));
        resource.setField(ResourceFields.QUALITY_RATING, position.getField(PositionFields.QUALITY_RATING));

        if (Objects.nonNull(delivery)) {
            resource.setField(ResourceFields.DELIVERY_NUMBER, delivery.getStringField("number"));
        } else if (Objects.nonNull(position.getStringField(PositionFields.DELIVERY_NUMBER))) {
            resource.setField(ResourceFields.DELIVERY_NUMBER, position.getStringField(PositionFields.DELIVERY_NUMBER));
        }

        if (StringUtils.isEmpty(product.getStringField(ProductFields.ADDITIONAL_UNIT))) {
            resource.setField(ResourceFields.GIVEN_UNIT, product.getField(ProductFields.UNIT));
            resource.setField(ResourceFields.QUANTITY_IN_ADDITIONAL_UNIT, position.getField(PositionFields.QUANTITY));
            resource.setField(ResourceFields.CONVERSION, BigDecimal.ONE);
        } else {
            resource.setField(ResourceFields.GIVEN_UNIT, position.getField(PositionFields.GIVEN_UNIT));
            resource.setField(ResourceFields.QUANTITY_IN_ADDITIONAL_UNIT, position.getField(PositionFields.GIVEN_QUANTITY));
            resource.setField(ResourceFields.CONVERSION, position.getField(PositionFields.CONVERSION));
        }

        resourceStockService.createResourceStock(resource);

        resource = resourceDD.save(resource);

        if (!resource.isValid()) {
            throw new InvalidResourceException(resource);
        }

        createAttributeValues(position, resource);

        position.setField(PositionFields.RESOURCE_NUMBER, resource.getStringField(ResourceFields.NUMBER));
        position.setField(PositionFields.RESOURCE_RECEIPT_DOCUMENT, resource.getId().toString());
    }

    private void createAttributeValues(final Entity position, final Entity resource) {
        List<Entity> attributePortionValues = position.getHasManyField(PositionFields.POSITION_ATTRIBUTE_VALUES);

        attributePortionValues.forEach(apv -> {
            Entity resourceAttributeValue = dataDefinitionService.get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER,
                    MaterialFlowResourcesConstants.MODEL_RESOURCE_ATTRIBUTE_VALUE).create();

            resourceAttributeValue.setField(ResourceAttributeValueFields.RESOURCE, resource.getId());
            resourceAttributeValue.setField(ResourceAttributeValueFields.ATTRIBUTE,
                    apv.getBelongsToField(ProductAttributeValueFields.ATTRIBUTE).getId());

            if (Objects.nonNull(apv.getBelongsToField(ProductAttributeValueFields.ATTRIBUTE_VALUE))) {
                resourceAttributeValue.setField(ResourceAttributeValueFields.ATTRIBUTE_VALUE,
                        apv.getBelongsToField(ProductAttributeValueFields.ATTRIBUTE_VALUE).getId());
            }

            resourceAttributeValue.setField(ResourceAttributeValueFields.VALUE,
                    apv.getStringField(ProductAttributeValueFields.VALUE));

            resourceAttributeValue.getDataDefinition().save(resourceAttributeValue);

            resourceAttributeValue.isValid();
        });
    }

    private Entity createResourceForRepacking(final Entity repacking, final Entity resource,
                                              final BigDecimal quantity, DataDefinition resourceDD, BigDecimal conversion) {
        Entity newResource = resourceDD.create();

        Entity staff = repacking.getBelongsToField(RepackingFields.STAFF);
        if (staff != null) {
            newResource.setField(ResourceFields.USER_NAME,
                    staff.getStringField(StaffFields.NAME) + " " + staff.getStringField(StaffFields.SURNAME));
        }
        newResource.setField(ResourceFields.TIME, repacking.getField(RepackingFields.TIME));
        newResource.setField(ResourceFields.LOCATION, repacking.getBelongsToField(RepackingFields.LOCATION));
        newResource.setField(ResourceFields.PRODUCT, resource.getBelongsToField(ResourceFields.PRODUCT));
        newResource.setField(ResourceFields.QUANTITY, quantity);
        newResource.setField(ResourceFields.AVAILABLE_QUANTITY, quantity);
        newResource.setField(ResourceFields.RESERVED_QUANTITY, BigDecimal.ZERO);
        newResource.setField(ResourceFields.PRICE, resource.getField(ResourceFields.PRICE));
        newResource.setField(ResourceFields.BATCH, resource.getField(ResourceFields.BATCH));
        newResource.setField(ResourceFields.EXPIRATION_DATE, resource.getField(ResourceFields.EXPIRATION_DATE));
        newResource.setField(ResourceFields.PRODUCTION_DATE, resource.getField(ResourceFields.PRODUCTION_DATE));
        newResource.setField(ResourceFields.STORAGE_LOCATION, repacking.getBelongsToField(RepackingFields.STORAGE_LOCATION));
        newResource.setField(ResourceFields.PALLET_NUMBER, repacking.getBelongsToField(RepackingFields.PALLET_NUMBER));
        newResource.setField(ResourceFields.TYPE_OF_LOAD_UNIT, repacking.getBelongsToField(RepackingFields.TYPE_OF_LOAD_UNIT));
        newResource.setField(ResourceFields.CONVERSION, conversion);
        newResource.setField(ResourceFields.GIVEN_UNIT, resource.getField(ResourceFields.GIVEN_UNIT));
        newResource.setField(ResourceFields.DELIVERY_NUMBER, resource.getField(ResourceFields.DELIVERY_NUMBER));
        newResource.setField(ResourceFields.QUALITY_RATING, resource.getField(ResourceFields.QUALITY_RATING));

        BigDecimal quantityInAdditionalUnit = calculationQuantityService.calculateAdditionalQuantity(quantity,
                conversion, resource.getStringField(ResourceFields.GIVEN_UNIT));

        newResource.setField(ResourceFields.QUANTITY_IN_ADDITIONAL_UNIT, quantityInAdditionalUnit);

        List<Entity> attributeValues = Lists.newArrayList();

        resource.getHasManyField(ResourceFields.RESOURCE_ATTRIBUTE_VALUES).forEach(resourceAttributeValue -> {
            Entity newResourceAttributeValue = resourceAttributeValue.getDataDefinition().create();

            newResourceAttributeValue.setField(ResourceAttributeValueFields.VALUE, resourceAttributeValue.getStringField(ResourceAttributeValueFields.VALUE));
            newResourceAttributeValue.setField(ResourceAttributeValueFields.ATTRIBUTE, resourceAttributeValue.getBelongsToField(ResourceAttributeValueFields.ATTRIBUTE));
            newResourceAttributeValue.setField(ResourceAttributeValueFields.ATTRIBUTE_VALUE,
                    resourceAttributeValue.getBelongsToField(ResourceAttributeValueFields.ATTRIBUTE_VALUE));

            attributeValues.add(newResourceAttributeValue);
        });

        newResource.setField(ResourceFields.RESOURCE_ATTRIBUTE_VALUES, attributeValues);

        resourceStockService.createResourceStock(newResource);

        return resourceDD.save(newResource);
    }

    private Entity createResource(final Entity position, final Entity warehouse, final Entity resource,
                                  final BigDecimal quantity,
                                  final Object date) {
        Entity newResource = resource.getDataDefinition().create();

        if (Objects.nonNull(position)) {
            Entity document = position.getBelongsToField(PositionFields.DOCUMENT);

            if (Objects.nonNull(document)) {
                Entity user = document.getBelongsToField(DocumentFields.USER);

                newResource.setField(ResourceFields.USER_NAME,
                        user.getStringField(UserFields.FIRST_NAME) + " " + user.getStringField(UserFields.LAST_NAME));
                newResource.setField(ResourceFields.DOCUMENT_NUMBER, document.getField(DocumentFields.NUMBER));
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
        newResource.setField(ResourceFields.STORAGE_LOCATION, warehouse.getBelongsToField(LocationFieldsMFR.TRANSFER_STORAGE_LOCATION));
        newResource.setField(ResourceFields.PALLET_NUMBER, null);
        newResource.setField(ResourceFields.TYPE_OF_LOAD_UNIT, null);
        newResource.setField(ResourceFields.CONVERSION, resource.getField(ResourceFields.CONVERSION));
        newResource.setField(ResourceFields.GIVEN_UNIT, resource.getField(ResourceFields.GIVEN_UNIT));
        newResource.setField(ResourceFields.DELIVERY_NUMBER, resource.getField(ResourceFields.DELIVERY_NUMBER));
        newResource.setField(ResourceFields.QUALITY_RATING, resource.getField(ResourceFields.QUALITY_RATING));

        BigDecimal quantityInAdditionalUnit = calculationQuantityService.calculateAdditionalQuantity(quantity,
                resource.getDecimalField(ResourceFields.CONVERSION), resource.getStringField(ResourceFields.GIVEN_UNIT));

        newResource.setField(ResourceFields.QUANTITY_IN_ADDITIONAL_UNIT, quantityInAdditionalUnit);

        List<Entity> attributeValues = Lists.newArrayList();

        resource.getHasManyField(ResourceFields.RESOURCE_ATTRIBUTE_VALUES).forEach(resourceAttributeValue -> {
            Entity newResourceAttributeValue = resourceAttributeValue.getDataDefinition().create();

            newResourceAttributeValue.setField(ResourceAttributeValueFields.VALUE, resourceAttributeValue.getStringField(ResourceAttributeValueFields.VALUE));
            newResourceAttributeValue.setField(ResourceAttributeValueFields.ATTRIBUTE, resourceAttributeValue.getBelongsToField(ResourceAttributeValueFields.ATTRIBUTE));
            newResourceAttributeValue.setField(ResourceAttributeValueFields.ATTRIBUTE_VALUE,
                    resourceAttributeValue.getBelongsToField(ResourceAttributeValueFields.ATTRIBUTE_VALUE));

            attributeValues.add(newResourceAttributeValue);
        });

        newResource.setField(ResourceFields.RESOURCE_ATTRIBUTE_VALUES, attributeValues);

        resourceStockService.createResourceStock(newResource);

        return resource.getDataDefinition().save(newResource);
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
                                                               final Multimap<Entity, Entity> productsAndPositions) {
        Multimap<Long, BigDecimal> result = ArrayListMultimap.create();

        for (Map.Entry<Entity, Entity> productAndPosition : productsAndPositions.entries()) {
            BigDecimal conversion = productAndPosition.getValue().getDecimalField(PositionFields.CONVERSION);
            Entity batch = productAndPosition.getValue().getBelongsToField(PositionFields.BATCH);

            SearchCriteriaBuilder searchCriteriaBuilder = getSearchCriteriaForResourceForProductAndWarehouse(productAndPosition.getKey(),
                    warehouse);

            if (Objects.nonNull(batch)) {
                searchCriteriaBuilder.add(SearchRestrictions.belongsTo(ResourceFields.BATCH, batch));
            }

            if (!StringUtils.isEmpty(productAndPosition.getKey().getStringField(ProductFields.ADDITIONAL_UNIT))) {
                searchCriteriaBuilder.add(SearchRestrictions.eq(ResourceFields.CONVERSION, conversion));
            } else {
                searchCriteriaBuilder.add(SearchRestrictions.eq(ResourceFields.CONVERSION, BigDecimal.ONE));
            }

            List<Entity> resources = searchCriteriaBuilder.list().getEntities();

            if (result.containsKey(productAndPosition.getKey().getId())) {
                BigDecimal currentQuantity = result.get(productAndPosition.getKey().getId()).stream().reduce(BigDecimal.ZERO,
                        BigDecimal::add);

                result.put(productAndPosition.getKey().getId(),
                        (resources.stream().map(res -> res.getDecimalField(ResourceFields.AVAILABLE_QUANTITY))
                                .reduce(BigDecimal.ZERO, BigDecimal::add)).add(currentQuantity));
            } else {
                result.put(productAndPosition.getKey().getId(),
                        resources.stream().map(res -> res.getDecimalField(ResourceFields.AVAILABLE_QUANTITY))
                                .reduce(BigDecimal.ZERO, BigDecimal::add));
            }
        }

        return result;
    }

    private void updateResourcesForReleaseDocuments(final Entity document) {
        Entity warehouse = document.getBelongsToField(DocumentFields.LOCATION_FROM);

        WarehouseAlgorithm warehouseAlgorithm = WarehouseAlgorithm
                .parseString(warehouse.getStringField(LocationFieldsMFR.ALGORITHM));

        boolean enoughResources = true;

        NotEnoughResourcesErrorMessageHolder errorMessageHolder = notEnoughResourcesErrorMessageHolderFactory.create();

        boolean isFromOrder = Objects.nonNull(document.getBelongsToField(L_ORDER));
        boolean updatePositionsNumbers = false;

        List<Entity> newPositions = new ArrayList<>();
        for (Entity position : document.getHasManyField(DocumentFields.POSITIONS)) {
            Entity product = position.getBelongsToField(PositionFields.PRODUCT);

            Either<BigDecimal, List<Entity>> eitherPositions = updateResources(warehouse, position, warehouseAlgorithm,
                    isFromOrder);

            enoughResources = enoughResources && position.isValid();

            if (!position.isValid()) {
                BigDecimal missingResourceAmount = eitherPositions.getLeft();

                errorMessageHolder.addErrorEntry(product, position.getBelongsToField(PositionFields.BATCH),
                        missingResourceAmount, null);
            } else {
                List<Entity> generatedPositions = eitherPositions.getRight();

                if (generatedPositions.size() > 1) {
                    if (Objects.nonNull(position.getId())) {
                        position.getDataDefinition().delete(position.getId());
                    }

                    for (Entity newPosition : generatedPositions) {
                        newPosition.setField(PositionFields.DOCUMENT, document);

                        Entity saved = newPosition.getDataDefinition().save(newPosition);
                        newPositions.add(saved);

                        addPositionErrors(document, saved);
                    }

                    updatePositionsNumbers = true;
                } else {
                    copyPositionValues(position, generatedPositions.get(0));

                    Entity saved = position.getDataDefinition().save(position);
                    newPositions.add(saved);

                    addPositionErrors(document, saved);
                }
            }
        }
        document.setField(DocumentFields.POSITIONS, newPositions);

        if (updatePositionsNumbers) {
            documentPositionService.updateDocumentPositionsNumbers(document.getId());
        }

        if (!enoughResources) {
            NotEnoughResourcesErrorMessageCopyToEntityHelper.addError(document, warehouse, errorMessageHolder);
        }
    }

    private void addPositionErrors(final Entity document, final Entity saved) {
        if (!saved.isValid()) {
            document.setNotValid();

            saved.getGlobalErrors().forEach(e -> document.addGlobalError(e.getMessage(), e.getAutoClose(), e.getVars()));

            if (!saved.getErrors().isEmpty()) {
                document.addGlobalError("materialFlow.document.fillResources.global.error.positionNotValid", false,
                        saved.getBelongsToField(PositionFields.PRODUCT).getStringField(ProductFields.NUMBER));
            }
        }
    }

    private void copyPositionValues(final Entity position, final Entity newPosition) {
        position.setField(PositionFields.PRICE, newPosition.getField(PositionFields.PRICE));
        position.setField(PositionFields.SELLING_PRICE, newPosition.getField(PositionFields.SELLING_PRICE));
        position.setField(PositionFields.BATCH, newPosition.getField(PositionFields.BATCH));
        position.setField(PositionFields.PRODUCTION_DATE, newPosition.getField(PositionFields.PRODUCTION_DATE));
        position.setField(PositionFields.EXPIRATION_DATE, newPosition.getField(PositionFields.EXPIRATION_DATE));
        position.setField(PositionFields.RESOURCE, newPosition.getField(PositionFields.RESOURCE));
        position.setField(PositionFields.RESOURCE_NUMBER, newPosition.getField(PositionFields.RESOURCE_NUMBER));
        position.setField(PositionFields.DELIVERY_NUMBER, newPosition.getField(PositionFields.DELIVERY_NUMBER));
        position.setField(PositionFields.TRANSFER_RESOURCE_NUMBER, newPosition.getField(PositionFields.TRANSFER_RESOURCE_NUMBER));
        position.setField(PositionFields.STORAGE_LOCATION, newPosition.getField(PositionFields.STORAGE_LOCATION));
        position.setField(PositionFields.CONVERSION, newPosition.getField(PositionFields.CONVERSION));
        position.setField(PositionFields.GIVEN_UNIT, newPosition.getField(PositionFields.GIVEN_UNIT));
        position.setField(PositionFields.PALLET_NUMBER, newPosition.getField(PositionFields.PALLET_NUMBER));
        position.setField(PositionFields.TYPE_OF_LOAD_UNIT, newPosition.getField(PositionFields.TYPE_OF_LOAD_UNIT));
        position.setField(PositionFields.WASTE, newPosition.getField(PositionFields.WASTE));
        position.setField(PositionFields.QUANTITY, newPosition.getField(PositionFields.QUANTITY));
        position.setField(PositionFields.GIVEN_QUANTITY, newPosition.getField(PositionFields.GIVEN_QUANTITY));
        position.setField(PositionFields.QUALITY_RATING, newPosition.getField(PositionFields.QUALITY_RATING));

        if (position.getHasManyField(PositionFields.POSITION_ATTRIBUTE_VALUES).isEmpty()) {
            position.setField(PositionFields.POSITION_ATTRIBUTE_VALUES,
                    newPosition.getField(PositionFields.POSITION_ATTRIBUTE_VALUES));
        }
    }

    private Either<BigDecimal, List<Entity>> updateResources(final Entity warehouse,
                                                             final Entity position,
                                                             final WarehouseAlgorithm warehouseAlgorithm,
                                                             boolean isFromOrder) {
        List<Entity> newPositions = Lists.newArrayList();

        Entity product = position.getBelongsToField(PositionFields.PRODUCT);

        List<Entity> resources = getResourcesForWarehouseProductAndAlgorithm(warehouse, product, position, warehouseAlgorithm);

        reservationsService.deleteReservationFromDocumentPosition(position);

        BigDecimal quantity = position.getDecimalField(PositionFields.QUANTITY);
        BigDecimal conversion = BigDecimalUtils.convertNullToOne(position.getDecimalField(PositionFields.CONVERSION));
        String givenUnit = position.getStringField(PositionFields.GIVEN_UNIT);
        String unit = product.getStringField(ProductFields.UNIT);

        for (Entity resource : resources) {
            Entity newPosition = createNewPosition(position, product, resource);

            if (!isFromOrder) {
                quantity = recalculateQuantity(quantity, conversion, givenUnit, resource.getDecimalField(ResourceFields.CONVERSION),
                        unit);
            }

            conversion = resource.getDecimalField(ResourceFields.CONVERSION);
            givenUnit = resource.getStringField(ResourceFields.GIVEN_UNIT);

            BigDecimal resourceQuantity = resource.getDecimalField(ResourceFields.QUANTITY);
            BigDecimal resourceAvailableQuantity = resource.getDecimalField(ResourceFields.AVAILABLE_QUANTITY);
            PossibleUnitConversions unitConversions = unitConversionService.getPossibleConversions(unit, searchCriteriaBuilder -> searchCriteriaBuilder.add(SearchRestrictions.belongsTo(UnitConversionItemFieldsB.PRODUCT, product)));

            BigDecimal givenQuantity;
            BigDecimal givenResourceAvailableQuantity;
            if (unitConversions.isDefinedFor(givenUnit)) {
                givenQuantity = unitConversions.convertTo(quantity, givenUnit, BigDecimal.ROUND_FLOOR);
                givenResourceAvailableQuantity = unitConversions.convertTo(resourceAvailableQuantity, givenUnit, BigDecimal.ROUND_FLOOR);
            } else {
                givenQuantity = calculationQuantityService.calculateAdditionalQuantity(quantity, conversion, givenUnit);
                givenResourceAvailableQuantity = calculationQuantityService
                        .calculateAdditionalQuantity(resourceAvailableQuantity, conversion, givenUnit);
            }

            if (Objects.nonNull(position.getBelongsToField(PositionFields.RESOURCE))
                    && warehouse.getBooleanField(LocationFieldsMFR.DRAFT_MAKES_RESERVATION)
                    && !isFromOrder) {
                BigDecimal reservedQuantity = resource.getDecimalField(ResourceFields.RESERVED_QUANTITY).subtract(quantity,
                        numberService.getMathContext());

                resource.setField(ResourceFields.RESERVED_QUANTITY, reservedQuantity);
            }

            if (quantity.compareTo(resourceAvailableQuantity) >= 0
                    || givenQuantity.compareTo(givenResourceAvailableQuantity) == 0) {
                quantity = quantity.subtract(resourceAvailableQuantity, numberService.getMathContext());

                if (resourceQuantity.compareTo(resourceAvailableQuantity) <= 0) {
                    Entity palletNumberToDispose = resource.getBelongsToField(ResourceFields.PALLET_NUMBER);

                    resource.getDataDefinition().delete(resource.getId());

                    palletNumberDisposalService.tryToDispose(palletNumberToDispose);
                } else {
                    BigDecimal newResourceQuantity = resourceQuantity.subtract(resourceAvailableQuantity);
                    BigDecimal quantityInAdditionalUnit = calculationQuantityService
                            .calculateAdditionalQuantity(newResourceQuantity, conversion, givenUnit);

                    resource.setField(ResourceFields.AVAILABLE_QUANTITY, BigDecimal.ZERO);
                    resource.setField(ResourceFields.QUANTITY, newResourceQuantity);
                    resource.setField(ResourceFields.QUANTITY_IN_ADDITIONAL_UNIT, quantityInAdditionalUnit);

                    Entity savedResource = resource.getDataDefinition().save(resource);

                    if (!savedResource.isValid()) {
                        throw new InvalidResourceException(savedResource);
                    }
                }

                newPosition.setField(PositionFields.QUANTITY,
                        numberService.setScaleWithDefaultMathContext(resourceAvailableQuantity));
                newPosition.setField(PositionFields.GIVEN_QUANTITY, givenResourceAvailableQuantity);

                newPositions.add(newPosition);

                if (BigDecimal.ZERO.compareTo(quantity) == 0 || BigDecimal.ZERO.compareTo(
                        calculationQuantityService.calculateAdditionalQuantity(quantity, conversion, givenUnit)) == 0) {
                    return Either.right(newPositions);
                }
            } else {
                resourceQuantity = resourceQuantity.subtract(quantity, numberService.getMathContext());
                resourceAvailableQuantity = resourceAvailableQuantity.subtract(quantity, numberService.getMathContext());

                BigDecimal quantityInAdditionalUnit = calculationQuantityService.calculateAdditionalQuantity(resourceQuantity,
                        conversion, givenUnit);

                resource.setField(ResourceFields.QUANTITY_IN_ADDITIONAL_UNIT, quantityInAdditionalUnit);
                resource.setField(ResourceFields.QUANTITY, numberService.setScaleWithDefaultMathContext(resourceQuantity));
                resource.setField(ResourceFields.AVAILABLE_QUANTITY, resourceAvailableQuantity);

                Entity savedResource = resource.getDataDefinition().save(resource);

                if (!savedResource.isValid()) {
                    throw new InvalidResourceException(savedResource);
                }

                newPosition.setField(PositionFields.QUANTITY, numberService.setScaleWithDefaultMathContext(quantity));
                newPosition.setField(PositionFields.GIVEN_QUANTITY, givenQuantity);

                newPositions.add(newPosition);

                return Either.right(newPositions);
            }
        }

        position.addError(position.getDataDefinition().getField(PositionFields.QUANTITY),
                "materialFlow.error.position.quantity.notEnough");

        return Either.left(quantity);
    }

    @Override
    @Transactional
    public void repackageResources(Entity repacking) {
        boolean enoughResources = true;
        NotEnoughResourcesErrorMessageHolder errorMessageHolder = notEnoughResourcesErrorMessageHolderFactory.create();

        for (Entity position : repacking.getHasManyField(RepackingFields.POSITIONS)) {
            Entity product = position.getBelongsToField(RepackingPositionFields.PRODUCT);

            Optional<BigDecimal> optionalMissingResourceAmount = repackResources(repacking, position);

            if (!position.isValid()) {
                if (optionalMissingResourceAmount.isPresent()) {
                    enoughResources = false;
                    BigDecimal missingResourceAmount = optionalMissingResourceAmount.get();
                    errorMessageHolder.addErrorEntry(product, position.getBelongsToField(RepackingPositionFields.BATCH),
                            missingResourceAmount, position.getStringField(RepackingPositionFields.RESOURCE_NUMBER));
                } else {
                    addRepackingPositionErrors(repacking, position);
                }
            } else {
                position = position.getDataDefinition().save(position);

                if (!position.isValid()) {
                    addRepackingPositionErrors(repacking, position);
                }
            }
        }

        if (!enoughResources) {
            NotEnoughResourcesErrorMessageCopyToEntityHelper.addError(repacking, repacking.getBelongsToField(RepackingFields.LOCATION), errorMessageHolder);
        }
    }

    private void addRepackingPositionErrors(Entity repacking, Entity position) {
        position.getGlobalErrors().forEach(e -> repacking.addGlobalError(e.getMessage(), e.getAutoClose(), e.getVars()));
        position.getErrors().forEach((key, value) -> {
            FieldDefinition field = position.getDataDefinition().getField(key);
            if (Objects.nonNull(field)) {
                repacking.addGlobalError("materialFlowResources.error.repackingResources.fieldError", translationService.translate("materialFlowResources.repackingPosition." + field.getName() + ".label", LocaleContextHolder.getLocale()),
                        translationService.translate(value.getMessage(), LocaleContextHolder.getLocale(), value.getVars()));
            } else {
                repacking.addGlobalError(value.getMessage(), value.getAutoClose(), value.getVars());
            }
        });
    }

    private void moveResourcesForTransferDocument(final Entity document) {
        Entity warehouseFrom = document.getBelongsToField(DocumentFields.LOCATION_FROM);
        Entity warehouseTo = document.getBelongsToField(DocumentFields.LOCATION_TO);
        Object date = document.getField(DocumentFields.TIME);

        WarehouseAlgorithm warehouseAlgorithm = WarehouseAlgorithm
                .parseString(warehouseFrom.getStringField(LocationFieldsMFR.ALGORITHM));

        boolean enoughResources = true;

        NotEnoughResourcesErrorMessageHolder errorMessageHolder = notEnoughResourcesErrorMessageHolderFactory.create();

        boolean isFromOrder = Objects.nonNull(document.getBelongsToField(L_ORDER));

        for (Entity position : document.getHasManyField(DocumentFields.POSITIONS)) {
            Entity product = position.getBelongsToField(PositionFields.PRODUCT);

            Either<BigDecimal, List<Entity>> eitherPositions = moveResources(warehouseFrom, warehouseTo, position, date,
                    warehouseAlgorithm, isFromOrder);

            enoughResources = enoughResources && position.isValid();

            if (!position.isValid()) {
                BigDecimal missingResourceAmount = eitherPositions.getLeft();

                errorMessageHolder.addErrorEntry(product, position.getBelongsToField(PositionFields.BATCH),
                        missingResourceAmount, null);
            } else {
                List<Entity> generatedPositions = eitherPositions.getRight();

                if (generatedPositions.size() > 1) {
                    if (Objects.nonNull(position.getId())) {
                        position.getDataDefinition().delete(position.getId());
                    }

                    for (Entity newPosition : generatedPositions) {
                        newPosition.setField(PositionFields.DOCUMENT, document);

                        Entity saved = newPosition.getDataDefinition().save(newPosition);

                        addPositionErrors(document, saved);
                    }
                } else {
                    copyPositionValues(position, generatedPositions.get(0));
                    copyPositionErrors(position, generatedPositions.get(0));

                    Entity saved = position.getDataDefinition().save(position);

                    addPositionErrors(document, saved);
                }
            }
        }

        if (!enoughResources) {
            NotEnoughResourcesErrorMessageCopyToEntityHelper.addError(document, warehouseFrom, errorMessageHolder);
        }
    }

    private void copyPositionErrors(final Entity position, final Entity newPosition) {
        for (Map.Entry<String, ErrorMessage> error : newPosition.getErrors().entrySet()) {
            position.addError(position.getDataDefinition().getField(error.getKey()), error.getValue().getMessage());
        }
    }

    private Optional<BigDecimal> repackResources(final Entity repacking,
                                                 final Entity position) {
        Entity product = position.getBelongsToField(RepackingPositionFields.PRODUCT);

        BigDecimal quantity = position.getDecimalField(RepackingPositionFields.QUANTITY);
        BigDecimal conversion = BigDecimalUtils.convertNullToOne(position.getDecimalField(RepackingPositionFields.CONVERSION));

        Entity resource = position.getBelongsToField(RepackingPositionFields.RESOURCE);

        if (resource == null) {
            throw new InvalidResourceException(position);
        }

        String givenUnit = resource.getStringField(ResourceFields.GIVEN_UNIT);

        BigDecimal resourceConversion = resource.getDecimalField(ResourceFields.CONVERSION);
        quantity = recalculateQuantity(quantity, conversion, givenUnit, resourceConversion,
                product.getStringField(ProductFields.UNIT));

        BigDecimal resourceQuantity = resource.getDecimalField(ResourceFields.QUANTITY);
        BigDecimal resourceAvailableQuantity = resource.getDecimalField(ResourceFields.AVAILABLE_QUANTITY);
        BigDecimal givenQuantity = calculationQuantityService.calculateAdditionalQuantity(quantity, resourceConversion, givenUnit);
        BigDecimal givenResourceAvailableQuantity = calculationQuantityService
                .calculateAdditionalQuantity(resourceAvailableQuantity, resourceConversion, givenUnit);

        DataDefinition resourceDD = dataDefinitionService.get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER,
                MaterialFlowResourcesConstants.MODEL_RESOURCE);

        if (quantity.compareTo(resourceAvailableQuantity) == 0
                || givenQuantity.compareTo(givenResourceAvailableQuantity) == 0) {
            if (resourceQuantity.compareTo(resourceAvailableQuantity) <= 0) {
                Entity palletNumberToDispose = resource.getBelongsToField(ResourceFields.PALLET_NUMBER);

                resourceDD.delete(resource.getId());

                palletNumberDisposalService.tryToDispose(palletNumberToDispose);
            } else {
                BigDecimal newResourceQuantity = resourceQuantity.subtract(resourceAvailableQuantity);
                BigDecimal quantityInAdditionalUnit = calculationQuantityService
                        .calculateAdditionalQuantity(newResourceQuantity, resourceConversion, givenUnit);

                resource.setField(ResourceFields.AVAILABLE_QUANTITY, BigDecimal.ZERO);
                resource.setField(ResourceFields.QUANTITY, newResourceQuantity);
                resource.setField(ResourceFields.QUANTITY_IN_ADDITIONAL_UNIT, quantityInAdditionalUnit);

                Entity savedResource = resourceDD.save(resource);

                if (!savedResource.isValid()) {
                    throw new InvalidResourceException(savedResource);
                }
            }

            Entity newResource = createResourceForRepacking(repacking, resource, resourceAvailableQuantity, resourceDD, conversion);

            position.setField(RepackingPositionFields.RESOURCE, null);
            position.setField(RepackingPositionFields.CREATED_RESOURCE_NUMBER, newResource.getStringField(ResourceFields.NUMBER));

            checkAndCopyResourceErrors(position, newResource, resourceDD);

            return Optional.empty();
        } else if (quantity.compareTo(resourceAvailableQuantity) < 0) {
            resourceQuantity = resourceQuantity.subtract(quantity, numberService.getMathContext());
            resourceAvailableQuantity = resourceAvailableQuantity.subtract(quantity, numberService.getMathContext());

            BigDecimal quantityInAdditionalUnit = calculationQuantityService.calculateAdditionalQuantity(resourceQuantity,
                    resourceConversion, givenUnit);

            resource.setField(ResourceFields.QUANTITY, numberService.setScaleWithDefaultMathContext(resourceQuantity));
            resource.setField(ResourceFields.QUANTITY_IN_ADDITIONAL_UNIT, quantityInAdditionalUnit);
            resource.setField(ResourceFields.AVAILABLE_QUANTITY, resourceAvailableQuantity);

            Entity savedResource = resourceDD.save(resource);

            if (!savedResource.isValid()) {
                throw new InvalidResourceException(savedResource);
            }

            Entity newResource = createResourceForRepacking(repacking, resource, quantity, resourceDD, conversion);

            position.setField(RepackingPositionFields.RESOURCE, null);
            position.setField(RepackingPositionFields.CREATED_RESOURCE_NUMBER, newResource.getStringField(ResourceFields.NUMBER));

            checkAndCopyResourceErrors(position, newResource, resourceDD);

            return Optional.empty();
        }

        position.setNotValid();
        return Optional.of(quantity.subtract(resourceAvailableQuantity, numberService.getMathContext()));
    }

    private void checkAndCopyResourceErrors(Entity position, Entity newResource, DataDefinition resourceDD) {
        if (palletValidatorService.checkMaximumNumberOfPallets(newResource.getBelongsToField(ResourceFields.STORAGE_LOCATION), newResource)) {
            newResource.addError(resourceDD.getField(ResourceFields.STORAGE_LOCATION), "materialFlowResources.storageLocation.maximumNumberOfPallets.toManyPallets");
        }

        if (!newResource.isValid()) {
            copyResourceErrorsToPosition(position, newResource);
        }
    }

    private Either<BigDecimal, List<Entity>> moveResources(final Entity warehouseFrom, final Entity warehouseTo,
                                                           final Entity position, final Object date,
                                                           final WarehouseAlgorithm warehouseAlgorithm,
                                                           boolean isFromOrder) {
        List<Entity> newPositions = Lists.newArrayList();

        Entity product = position.getBelongsToField(PositionFields.PRODUCT);

        List<Entity> resources = getResourcesForWarehouseProductAndAlgorithm(warehouseFrom, product, position,
                warehouseAlgorithm);

        reservationsService.deleteReservationFromDocumentPosition(position);

        BigDecimal quantity = position.getDecimalField(PositionFields.QUANTITY);
        BigDecimal conversion = BigDecimalUtils.convertNullToOne(position.getDecimalField(PositionFields.CONVERSION));
        String givenUnit = position.getStringField(PositionFields.GIVEN_UNIT);

        for (Entity resource : resources) {
            Entity newPosition = createNewPosition(position, product, resource);

            if (!isFromOrder) {
                quantity = recalculateQuantity(quantity, conversion, givenUnit, resource.getDecimalField(ResourceFields.CONVERSION),
                        product.getStringField(ProductFields.UNIT));
            }

            conversion = resource.getDecimalField(ResourceFields.CONVERSION);
            givenUnit = resource.getStringField(ResourceFields.GIVEN_UNIT);

            BigDecimal resourceQuantity = resource.getDecimalField(ResourceFields.QUANTITY);
            BigDecimal resourceAvailableQuantity = resource.getDecimalField(ResourceFields.AVAILABLE_QUANTITY);
            BigDecimal givenQuantity = calculationQuantityService.calculateAdditionalQuantity(quantity, conversion, givenUnit);
            BigDecimal givenResourceAvailableQuantity = calculationQuantityService
                    .calculateAdditionalQuantity(resourceAvailableQuantity, conversion, givenUnit);

            if (Objects.nonNull(position.getBelongsToField(PositionFields.RESOURCE))
                    && warehouseFrom.getBooleanField(LocationFieldsMFR.DRAFT_MAKES_RESERVATION)) {
                BigDecimal reservedQuantity = resource.getDecimalField(ResourceFields.RESERVED_QUANTITY).subtract(quantity,
                        numberService.getMathContext());

                resource.setField(ResourceFields.RESERVED_QUANTITY, reservedQuantity);
            }

            if (quantity.compareTo(resourceAvailableQuantity) >= 0
                    || givenQuantity.compareTo(givenResourceAvailableQuantity) == 0) {
                quantity = quantity.subtract(resourceAvailableQuantity, numberService.getMathContext());

                if (resourceQuantity.compareTo(resourceAvailableQuantity) <= 0) {
                    Entity palletNumberToDispose = resource.getBelongsToField(ResourceFields.PALLET_NUMBER);

                    resource.getDataDefinition().delete(resource.getId());

                    palletNumberDisposalService.tryToDispose(palletNumberToDispose);
                } else {
                    BigDecimal newResourceQuantity = resourceQuantity.subtract(resourceAvailableQuantity);
                    BigDecimal quantityInAdditionalUnit = calculationQuantityService
                            .calculateAdditionalQuantity(newResourceQuantity, conversion, givenUnit);

                    resource.setField(ResourceFields.AVAILABLE_QUANTITY, BigDecimal.ZERO);
                    resource.setField(ResourceFields.QUANTITY, newResourceQuantity);
                    resource.setField(ResourceFields.QUANTITY_IN_ADDITIONAL_UNIT, quantityInAdditionalUnit);

                    Entity savedResource = resource.getDataDefinition().save(resource);

                    if (!savedResource.isValid()) {
                        throw new InvalidResourceException(savedResource);
                    }
                }

                Entity newResource = createResource(position, warehouseTo, resource, resourceAvailableQuantity, date);

                newPosition.setField(PositionFields.QUANTITY,
                        numberService.setScaleWithDefaultMathContext(resourceAvailableQuantity));
                newPosition.setField(PositionFields.GIVEN_QUANTITY, givenResourceAvailableQuantity);
                newPosition.setField(PositionFields.TRANSFER_RESOURCE_NUMBER, newResource.getStringField(ResourceFields.NUMBER));

                if (BigDecimal.ZERO.compareTo(quantity) == 0 || BigDecimal.ZERO.compareTo(
                        calculationQuantityService.calculateAdditionalQuantity(quantity, conversion, givenUnit)) == 0) {
                    if (!newResource.isValid()) {
                        copyResourceErrorsToPosition(newPosition, newResource);
                    }

                    newPositions.add(newPosition);

                    return Either.right(newPositions);
                } else {
                    newPositions.add(newPosition);
                }
            } else {
                resourceQuantity = resourceQuantity.subtract(quantity, numberService.getMathContext());
                resourceAvailableQuantity = resourceAvailableQuantity.subtract(quantity, numberService.getMathContext());

                BigDecimal quantityInAdditionalUnit = calculationQuantityService.calculateAdditionalQuantity(resourceQuantity,
                        conversion, givenUnit);

                resource.setField(ResourceFields.QUANTITY_IN_ADDITIONAL_UNIT, quantityInAdditionalUnit);
                resource.setField(ResourceFields.QUANTITY, numberService.setScaleWithDefaultMathContext(resourceQuantity));
                resource.setField(ResourceFields.AVAILABLE_QUANTITY, resourceAvailableQuantity);

                Entity savedResource = resource.getDataDefinition().save(resource);

                if (!savedResource.isValid()) {
                    throw new InvalidResourceException(savedResource);
                }

                Entity newResource = createResource(position, warehouseTo, resource, quantity, date);

                newPosition.setField(PositionFields.QUANTITY, numberService.setScaleWithDefaultMathContext(quantity));
                newPosition.setField(PositionFields.GIVEN_QUANTITY, givenQuantity);
                newPosition.setField(PositionFields.TRANSFER_RESOURCE_NUMBER, newResource.getStringField(ResourceFields.NUMBER));

                if (!newResource.isValid()) {
                    copyResourceErrorsToPosition(newPosition, newResource);
                }

                newPositions.add(newPosition);

                return Either.right(newPositions);
            }
        }

        position.addError(position.getDataDefinition().getField(PositionFields.QUANTITY),
                "materialFlow.error.position.quantity.notEnough");

        return Either.left(quantity);
    }

    private void copyResourceErrorsToPosition(final Entity position, final Entity newResource) {
        for (Map.Entry<String, ErrorMessage> error : newResource.getErrors().entrySet()) {
            if (!error.getKey().equals(ResourceFields.QUANTITY_IN_ADDITIONAL_UNIT)) {
                position.addError(position.getDataDefinition().getField(error.getKey()), error.getValue().getMessage(), error.getValue().getVars());
            } else {
                position.addError(position.getDataDefinition().getField(PositionFields.GIVEN_UNIT),
                        error.getValue().getMessage(), error.getValue().getVars());
            }
        }
    }

    private List<Entity> getResourcesForWarehouseProductAndAlgorithm(final Entity warehouse, final Entity product,
                                                                     final Entity position,
                                                                     final WarehouseAlgorithm warehouseAlgorithm) {
        List<Entity> resources = Lists.newArrayList();

        Entity resource = position.getBelongsToField(PositionFields.RESOURCE);

        if (Objects.nonNull(resource) && Objects.nonNull(resource.getId())) {
            resource = dataDefinitionService
                    .get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER, MaterialFlowResourcesConstants.MODEL_RESOURCE)
                    .get(resource.getId());
        }

        if (Objects.nonNull(resource)) {
            Entity reservation = reservationsService.getReservationForPosition(position);

            if (Objects.nonNull(reservation)) {
                BigDecimal reservationQuantity = reservation.getDecimalField(ReservationFields.QUANTITY);
                BigDecimal resourceAvailableQuantity = resource.getDecimalField(ResourceFields.AVAILABLE_QUANTITY);

                resource.setField(ResourceFields.AVAILABLE_QUANTITY, resourceAvailableQuantity.add(reservationQuantity));
            }

            resources.add(resource);
        } else if (WarehouseAlgorithm.FIFO.equals(warehouseAlgorithm)) {
            resources = getResourcesForLocationCommonCode(warehouse, product, position,
                    SearchOrders.asc(ResourceFields.TIME));
        } else if (WarehouseAlgorithm.LIFO.equals(warehouseAlgorithm)) {
            resources = getResourcesForLocationCommonCode(warehouse, product, position,
                    SearchOrders.desc(ResourceFields.TIME));
        } else if (WarehouseAlgorithm.FEFO.equals(warehouseAlgorithm)) {
            resources = getResourcesForLocationCommonCode(warehouse, product, position,
                    SearchOrders.asc(ResourceFields.EXPIRATION_DATE), SearchOrders.asc(ResourceFields.AVAILABLE_QUANTITY));
        } else if (WarehouseAlgorithm.LEFO.equals(warehouseAlgorithm)) {
            resources = getResourcesForLocationCommonCode(warehouse, product, position,
                    SearchOrders.desc(ResourceFields.EXPIRATION_DATE), SearchOrders.asc(ResourceFields.AVAILABLE_QUANTITY));
        }

        return resources;
    }

    private List<Entity> getResourcesForLocationCommonCodeConversion(final Entity warehouse, final Entity product,
                                                                     final Entity position,
                                                                     final boolean resourceIrrespectiveOfConversion,
                                                                     final SearchOrder... searchOrders) {

        class SearchCriteriaHelper {

            private List<Entity> getAll() {
                SearchCriteriaBuilder scb = getSearchCriteriaForResourceForProductAndWarehouse(product, warehouse);

                if (resourceIrrespectiveOfConversion) {
                    if (StringUtils.isNotEmpty(product.getStringField(ProductFields.ADDITIONAL_UNIT))) {
                        scb.add(SearchRestrictions.ne(PositionFields.CONVERSION,
                                position.getDecimalField(PositionFields.CONVERSION)));
                    } else {
                        scb.add(SearchRestrictions.ne(ResourceFields.CONVERSION, BigDecimal.ONE));
                    }
                } else {
                    if (StringUtils.isNotEmpty(product.getStringField(ProductFields.ADDITIONAL_UNIT))) {
                        scb.add(SearchRestrictions.eq(PositionFields.CONVERSION,
                                position.getDecimalField(PositionFields.CONVERSION)));
                    } else {
                        scb.add(SearchRestrictions.eq(ResourceFields.CONVERSION, BigDecimal.ONE));
                    }
                }

                if (Objects.nonNull(position.getBelongsToField(PositionFields.BATCH))) {
                    scb.add(SearchRestrictions.belongsTo(ResourceFields.BATCH, position.getBelongsToField(PositionFields.BATCH)));
                }

                scb.add(SearchRestrictions.eq(ResourceFields.BLOCKED_FOR_QUALITY_CONTROL, false));

                for (SearchOrder searchOrder : searchOrders) {
                    scb.addOrder(searchOrder);
                }

                return scb.list().getEntities();
            }

        }

        return new SearchCriteriaHelper().getAll();
    }

    private List<Entity> getResourcesForLocationCommonCode(final Entity warehouse, final Entity product,
                                                           final Entity position, final SearchOrder... searchOrders) {
        List<Entity> resources = getResourcesForLocationCommonCodeConversion(warehouse, product, position, false,
                searchOrders);

        Entity documentPositionParameters = parameterService.getParameter()
                .getBelongsToField(ParameterFieldsMFR.DOCUMENT_POSITION_PARAMETERS);

        boolean fillResourceIrrespectiveOfConversion = documentPositionParameters
                .getBooleanField(DocumentPositionParametersFields.FILL_RESOURCE_IRRESPECTIVE_OF_CONVERSION);

        if (fillResourceIrrespectiveOfConversion) {
            resources.addAll(getResourcesForLocationCommonCodeConversion(warehouse, product, position, true,
                    searchOrders));
        }

        return resources;
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void fillResourcesInStocktaking(final Entity document) throws LockAcquisitionException {
        LOGGER.info("FILL RESOURCES STARTED IN DOCUMENT: id = " + document.getId() + " number = "
                + document.getStringField(DocumentFields.NUMBER));
        LOGGER.info("USER STARTED IN DOCUMENT: id = " + document.getId() + ": "
                + userService.getCurrentUserEntity().getStringField(UserFields.USER_NAME));

        List<Entity> positions = document.getHasManyField(DocumentFields.POSITIONS);

        LOGGER.info("INITIAL POSITIONS IN DOCUMENT: id = " + document.getId() + ": size = " + positions.size());
        LOGGER.info(positions.toString());

        Entity warehouse = document.getBelongsToField(DocumentFields.LOCATION_FROM);
        WarehouseAlgorithm warehouseAlgorithm = WarehouseAlgorithm
                .parseString(warehouse.getStringField(LocationFieldsMFR.ALGORITHM));
        boolean valid = true;
        boolean updatePositionsNumbers = false;

        for (Entity position : positions) {
            List<Entity> newPositions = matchResourcesToPositionForStocktaking(document, position, warehouse, warehouseAlgorithm);

            LOGGER.info("GENERATED POSITIONS IN DOCUMENT: id = " + document.getId() + ", size = " + newPositions.size());
            LOGGER.info(newPositions.toString());

            if (newPositions.size() > 1) {
                position.getDataDefinition().delete(position.getId());

                for (Entity newPosition : newPositions) {
                    newPosition.setField(PositionFields.DOCUMENT, document);

                    Entity saved = newPosition.getDataDefinition().save(newPosition);

                    valid = valid && saved.isValid();
                }

                updatePositionsNumbers = true;
            } else {
                copyPositionValues(position, newPositions.get(0));

                Entity saved = position.getDataDefinition().save(position);

                valid = valid && saved.isValid();
            }
        }

        if (updatePositionsNumbers) {
            documentPositionService.updateDocumentPositionsNumbers(document.getId());
        }

        if (valid) {
            LOGGER.info("FILL RESOURCES ENDED SUCCESSFULLY FOR DOCUMENT: id = " + document.getId() + " number = "
                    + document.getStringField(DocumentFields.NUMBER));
            return;
        }

        LOGGER.warn("FILL RESOURCES ENDED WITH ERRORS FOR DOCUMENT: id = " + document.getId() + " number = "
                + document.getStringField(DocumentFields.NUMBER));

        throw new IllegalStateException("Unable to fill resources in document.");
    }

    private List<Entity> matchResourcesToPositionForStocktaking(final Entity document, final Entity position, final Entity warehouse,
                                                                final WarehouseAlgorithm warehouseAlgorithm) {
        List<Entity> newPositions = Lists.newArrayList();

        Entity product = position.getBelongsToField(PositionFields.PRODUCT);

        List<Entity> resources = getResourcesForStocktaking(warehouse, product, position, warehouseAlgorithm);

        BigDecimal quantity = position.getDecimalField(PositionFields.QUANTITY);

        for (Entity resource : resources) {
            if (resource.getBooleanField(ResourceFields.WASTE)) {
                continue;
            }

            LOGGER.info("DOCUMENT: " + document.getId() + " POSITION: " + position);
            LOGGER.info("RESOURCE USED: " + resource);

            Entity newPosition = createNewPosition(position, product, resource);

            newPosition.setField(PositionFields.RESOURCE, resource);

            BigDecimal resourceAvailableQuantity = resource.getDecimalField(ResourceFields.AVAILABLE_QUANTITY);

            if (quantity.compareTo(resourceAvailableQuantity) > 0) {
                quantity = quantity.subtract(resourceAvailableQuantity, numberService.getMathContext());

                setPositionQuantityAndGivenQuantity(resourceAvailableQuantity, newPosition);
                newPositions.add(newPosition);
            } else {
                setPositionQuantityAndGivenQuantity(quantity, newPosition);
                newPositions.add(newPosition);

                return newPositions;
            }
        }

        LOGGER.warn("FILL RESOURCES ENDED WITH ERRORS FOR DOCUMENT: id = " + document.getId() + " number = "
                + document.getStringField(DocumentFields.NUMBER));

        position.addGlobalError("materialFlow.error.position.quantity.notEnoughResources",
                product.getStringField(ProductFields.NUMBER), warehouse.getStringField(LocationFields.NUMBER));

        throw new EntityRuntimeException(position);
    }

    private List<Entity> getResourcesForStocktaking(final Entity warehouse, final Entity product,
                                                    final Entity position,
                                                    final WarehouseAlgorithm warehouseAlgorithm) {
        List<Entity> resources = Lists.newArrayList();

        if (WarehouseAlgorithm.FIFO.equals(warehouseAlgorithm)) {
            resources = getResourcesForLocationConversionForStocktaking(warehouse, product, position,
                    SearchOrders.asc(ResourceFields.TIME));
        } else if (WarehouseAlgorithm.LIFO.equals(warehouseAlgorithm)) {
            resources = getResourcesForLocationConversionForStocktaking(warehouse, product, position,
                    SearchOrders.desc(ResourceFields.TIME));
        } else if (WarehouseAlgorithm.FEFO.equals(warehouseAlgorithm)) {
            resources = getResourcesForLocationConversionForStocktaking(warehouse, product, position,
                    SearchOrders.asc(ResourceFields.EXPIRATION_DATE), SearchOrders.asc(ResourceFields.AVAILABLE_QUANTITY));
        } else if (WarehouseAlgorithm.LEFO.equals(warehouseAlgorithm)) {
            resources = getResourcesForLocationConversionForStocktaking(warehouse, product, position,
                    SearchOrders.desc(ResourceFields.EXPIRATION_DATE), SearchOrders.asc(ResourceFields.AVAILABLE_QUANTITY));
        }

        return resources;
    }

    private List<Entity> getResourcesForLocationConversionForStocktaking(final Entity warehouse, final Entity product,
                                                                         final Entity position,
                                                                         final SearchOrder... searchOrders) {

        class SearchCriteriaHelperForStocktaking {

            private List<Entity> getAll() {
                SearchCriteriaBuilder scb = dataDefinitionService
                        .get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER, MaterialFlowResourcesConstants.MODEL_RESOURCE).find()
                        .add(SearchRestrictions.belongsTo(ResourceFields.LOCATION, warehouse))
                        .add(SearchRestrictions.belongsTo(ResourceFields.PRODUCT, product))
                        .add(SearchRestrictions.gt(ResourceFields.AVAILABLE_QUANTITY, BigDecimal.ZERO));

                if (StringUtils.isNotEmpty(product.getStringField(ProductFields.ADDITIONAL_UNIT))) {
                    scb.add(SearchRestrictions.eq(ResourceFields.CONVERSION,
                            position.getDecimalField(PositionFields.CONVERSION)));
                } else {
                    scb.add(SearchRestrictions.eq(ResourceFields.CONVERSION, BigDecimal.ONE));
                }

                if (Objects.nonNull(position.getBelongsToField(PositionFields.BATCH))) {
                    scb.add(SearchRestrictions.belongsTo(ResourceFields.BATCH, position.getBelongsToField(PositionFields.BATCH)));
                } else {
                    scb.add(SearchRestrictions.isNull(ResourceFields.BATCH));
                }

                if (Objects.nonNull(position.getBelongsToField(PositionFields.STORAGE_LOCATION))) {
                    scb.add(SearchRestrictions.belongsTo(ResourceFields.STORAGE_LOCATION, position.getBelongsToField(PositionFields.STORAGE_LOCATION)));
                } else {
                    scb.add(SearchRestrictions.isNull(ResourceFields.STORAGE_LOCATION));
                }

                if (Objects.nonNull(position.getBelongsToField(PositionFields.PALLET_NUMBER))) {
                    scb.add(SearchRestrictions.belongsTo(ResourceFields.PALLET_NUMBER, position.getBelongsToField(PositionFields.PALLET_NUMBER)));
                } else {
                    scb.add(SearchRestrictions.isNull(ResourceFields.PALLET_NUMBER));
                }

                if (Objects.nonNull(position.getBelongsToField(PositionFields.TYPE_OF_LOAD_UNIT))) {
                    scb.add(SearchRestrictions.belongsTo(ResourceFields.TYPE_OF_LOAD_UNIT, position.getBelongsToField(PositionFields.TYPE_OF_LOAD_UNIT)));
                } else {
                    scb.add(SearchRestrictions.isNull(ResourceFields.TYPE_OF_LOAD_UNIT));
                }

                if (Objects.nonNull(position.getDateField(PositionFields.EXPIRATION_DATE))) {
                    scb.add(SearchRestrictions.eq(ResourceFields.EXPIRATION_DATE, position.getDateField(PositionFields.EXPIRATION_DATE)));
                } else {
                    scb.add(SearchRestrictions.isNull(ResourceFields.EXPIRATION_DATE));
                }

                scb.add(SearchRestrictions.eq(ResourceFields.BLOCKED_FOR_QUALITY_CONTROL, false));

                for (SearchOrder searchOrder : searchOrders) {
                    scb.addOrder(searchOrder);
                }

                return scb.list().getEntities();
            }

        }

        return new SearchCriteriaHelperForStocktaking().getAll();
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void fillResourcesInDocument(final ViewDefinitionState view,
                                        final Entity document) throws LockAcquisitionException {
        LOGGER.info("FILL RESOURCES STARTED IN DOCUMENT: id = " + document.getId() + " number = "
                + document.getStringField(DocumentFields.NUMBER));
        LOGGER.info("USER STARTED IN DOCUMENT: id = " + document.getId() + ": "
                + userService.getCurrentUserEntity().getStringField(UserFields.USER_NAME));

        List<Entity> positions = document.getHasManyField(DocumentFields.POSITIONS);

        LOGGER.info("INITIAL POSITIONS IN DOCUMENT: id = " + document.getId() + ": size = " + positions.size());
        LOGGER.info(positions.toString());

        Entity warehouse = document.getBelongsToField(DocumentFields.LOCATION_FROM);
        WarehouseAlgorithm warehouseAlgorithm = WarehouseAlgorithm
                .parseString(warehouse.getStringField(LocationFieldsMFR.ALGORITHM));
        boolean isFromOrder = Objects.nonNull(document.getBelongsToField(L_ORDER));
        boolean valid = true;
        boolean updatePositionsNumbers = false;

        for (Entity position : positions) {
            if (Objects.isNull(position.getBelongsToField(PositionFields.RESOURCE))) {
                List<Entity> newPositions = matchResourcesToPosition(position, warehouse, warehouseAlgorithm, isFromOrder);

                if (!newPositions.isEmpty()) {
                    LOGGER.info("GENERATED POSITIONS IN DOCUMENT: id = " + document.getId() + ", FOR POSITION: id = "
                            + position.getId() + ", size = " + newPositions.size());
                    LOGGER.info(newPositions.toString());

                    if (newPositions.size() > 1) {
                        position.getDataDefinition().delete(position.getId());

                        for (Entity newPosition : newPositions) {
                            newPosition.setField(PositionFields.DOCUMENT, document);

                            Entity saved = newPosition.getDataDefinition().save(newPosition);

                            valid = valid && saved.isValid();

                            addPositionErrors(view, saved);
                        }

                        updatePositionsNumbers = true;
                    } else {
                        copyPositionValues(position, newPositions.get(0));

                        Entity saved = position.getDataDefinition().save(position);

                        valid = valid && saved.isValid();

                        addPositionErrors(view, saved);
                    }
                }
            }
        }

        if (updatePositionsNumbers) {
            documentPositionService.updateDocumentPositionsNumbers(document.getId());
        }

        if (valid) {
            LOGGER.info("FILL RESOURCES ENDED SUCCESSFULLY FOR DOCUMENT: id = " + document.getId() + " number = "
                    + document.getStringField(DocumentFields.NUMBER));
            return;
        }

        LOGGER.warn("FILL RESOURCES ENDED WITH ERRORS FOR DOCUMENT: id = " + document.getId() + " number = "
                + document.getStringField(DocumentFields.NUMBER));

        throw new IllegalStateException("Unable to fill resources in document.");
    }

    private void addPositionErrors(final ViewDefinitionState view, final Entity saved) {
        saved.getGlobalErrors().forEach(view::addMessage);

        if (!saved.getErrors().isEmpty()) {
            view.addMessage("materialFlow.document.fillResources.global.error.positionNotValid",
                    ComponentState.MessageType.FAILURE, false,
                    saved.getBelongsToField(PositionFields.PRODUCT).getStringField(ProductFields.NUMBER));
        }
    }

    private List<Entity> matchResourcesToPosition(final Entity position, final Entity warehouse,
                                                  final WarehouseAlgorithm warehouseAlgorithm, boolean isFromOrder) {
        DataDefinition positionDD = dataDefinitionService.get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER,
                MaterialFlowResourcesConstants.MODEL_POSITION);

        List<Entity> newPositions = Lists.newArrayList();

        Entity product = position.getBelongsToField(PositionFields.PRODUCT);

        List<Entity> resources = getResourcesForWarehouseProductAndAlgorithm(warehouse, product, position, warehouseAlgorithm);

        BigDecimal quantity = position.getDecimalField(PositionFields.QUANTITY);
        BigDecimal conversion = BigDecimalUtils.convertNullToOne(position.getDecimalField(PositionFields.CONVERSION));
        String givenUnit = position.getStringField(PositionFields.GIVEN_UNIT);

        for (Entity resource : resources) {
            if (resource.getBooleanField(ResourceFields.WASTE)) {
                continue;
            }

            LOGGER.info("DOCUMENT: " + position.getBelongsToField(PositionFields.DOCUMENT).getId() + " POSITION: " + position);
            LOGGER.info("RESOURCE USED: " + resource);

            Entity newPosition = createNewPosition(position, product, resource);

            newPosition.setField(PositionFields.RESOURCE, resource);

            if (!isFromOrder) {
                quantity = recalculateQuantity(quantity, conversion, givenUnit, resource.getDecimalField(ResourceFields.CONVERSION),
                        product.getStringField(ProductFields.UNIT));
            }

            conversion = resource.getDecimalField(ResourceFields.CONVERSION);
            givenUnit = resource.getStringField(ResourceFields.GIVEN_UNIT);

            BigDecimal resourceAvailableQuantity = resource.getDecimalField(ResourceFields.AVAILABLE_QUANTITY);

            if (quantity.compareTo(resourceAvailableQuantity) > 0) {
                quantity = quantity.subtract(resourceAvailableQuantity, numberService.getMathContext());

                setPositionQuantityAndGivenQuantity(resourceAvailableQuantity, newPosition);
                newPositions.add(newPosition);
            } else {
                setPositionQuantityAndGivenQuantity(quantity, newPosition);
                newPositions.add(newPosition);

                return newPositions;
            }
        }

        newPositions.add(createPositionWithoutResourceForMissingQuantity(position, positionDD, quantity));

        return newPositions;
    }

    private Entity createNewPosition(final Entity position, final Entity product, final Entity resource) {
        Entity newPosition = position.getDataDefinition().create();

        newPosition.setField(PositionFields.PRODUCT, product);
        newPosition.setField(PositionFields.GIVEN_UNIT, resource.getStringField(ResourceFields.GIVEN_UNIT));
        newPosition.setField(PositionFields.PRICE, resource.getField(ResourceFields.PRICE));
        newPosition.setField(PositionFields.BATCH, resource.getField(ResourceFields.BATCH));
        newPosition.setField(PositionFields.PRODUCTION_DATE, resource.getField(ResourceFields.PRODUCTION_DATE));
        newPosition.setField(PositionFields.EXPIRATION_DATE, resource.getField(ResourceFields.EXPIRATION_DATE));
        newPosition.setField(PositionFields.RESOURCE, null);
        newPosition.setField(PositionFields.RESOURCE_NUMBER, resource.getStringField(ResourceFields.NUMBER));
        newPosition.setField(PositionFields.DELIVERY_NUMBER, resource.getStringField(ResourceFields.DELIVERY_NUMBER));
        newPosition.setField(PositionFields.STORAGE_LOCATION, resource.getBelongsToField(ResourceFields.STORAGE_LOCATION));
        newPosition.setField(PositionFields.CONVERSION, resource.getField(ResourceFields.CONVERSION));
        newPosition.setField(PositionFields.PALLET_NUMBER, resource.getField(ResourceFields.PALLET_NUMBER));
        newPosition.setField(PositionFields.TYPE_OF_LOAD_UNIT, resource.getField(ResourceFields.TYPE_OF_LOAD_UNIT));
        newPosition.setField(PositionFields.QUALITY_RATING, resource.getField(ResourceFields.QUALITY_RATING));
        newPosition.setField(PositionFields.WASTE, resource.getField(ResourceFields.WASTE));
        newPosition.setField(PositionFields.SELLING_PRICE, position.getField(PositionFields.SELLING_PRICE));
        newPosition.setField(PositionFields.PICKING_WORKER, position.getField(PositionFields.PICKING_WORKER));
        newPosition.setField(PositionFields.PICKING_DATE, position.getField(PositionFields.PICKING_DATE));
        newPosition.setField(PositionFields.POSITION_ATTRIBUTE_VALUES, prepareAttributes(resource));

        return newPosition;
    }

    private List<Entity> prepareAttributes(final Entity resource) {
        List<Entity> attributes = Lists.newArrayList();

        resource.getHasManyField(ResourceFields.RESOURCE_ATTRIBUTE_VALUES).forEach(resourceAttributeValue -> {
            Entity positionAttributeValue = dataDefinitionService.get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER,
                    MaterialFlowResourcesConstants.MODEL_POSITION_ATTRIBUTE_VALUE).create();

            positionAttributeValue.setField(PositionAttributeValueFields.ATTRIBUTE,
                    resourceAttributeValue.getBelongsToField(ResourceAttributeValueFields.ATTRIBUTE).getId());

            if (Objects.nonNull(resourceAttributeValue.getBelongsToField(PositionAttributeValueFields.ATTRIBUTE_VALUE))) {
                positionAttributeValue.setField(PositionAttributeValueFields.ATTRIBUTE_VALUE,
                        resourceAttributeValue.getBelongsToField(ResourceAttributeValueFields.ATTRIBUTE_VALUE).getId());
            }

            positionAttributeValue.setField(PositionAttributeValueFields.VALUE,
                    resourceAttributeValue.getStringField(ResourceAttributeValueFields.VALUE));

            attributes.add(positionAttributeValue);
        });

        return attributes;
    }

    private BigDecimal recalculateQuantity(final BigDecimal quantity,
                                           final BigDecimal conversion, final String givenUnit,
                                           final BigDecimal resourceConversion, final String unit) {
        if (conversion.compareTo(resourceConversion) != 0) {
            BigDecimal additional = calculationQuantityService.calculateAdditionalQuantity(quantity, resourceConversion,
                    givenUnit);
            return calculationQuantityService.calculateQuantity(additional, resourceConversion, unit);
        }

        return quantity;
    }

    private void setPositionQuantityAndGivenQuantity(final BigDecimal quantity, final Entity newPosition) {
        newPosition.setField(PositionFields.QUANTITY, numberService.setScaleWithDefaultMathContext(quantity));

        BigDecimal givenQuantity = calculationQuantityService.calculateAdditionalQuantity(quantity,
                newPosition.getDecimalField(PositionFields.CONVERSION), newPosition.getStringField(PositionFields.GIVEN_UNIT));

        newPosition.setField(PositionFields.GIVEN_QUANTITY, givenQuantity);
    }

    private Entity createPositionWithoutResourceForMissingQuantity(final Entity position,
                                                                   final DataDefinition positionDD,
                                                                   final BigDecimal quantity) {
        Entity newPosition = positionDD.create();

        newPosition.setField(PositionFields.PRODUCT, position.getBelongsToField(PositionFields.PRODUCT));
        newPosition.setField(PositionFields.GIVEN_UNIT, position.getStringField(PositionFields.GIVEN_UNIT));
        newPosition.setField(PositionFields.CONVERSION, position.getField(PositionFields.CONVERSION));

        if (Objects.nonNull(position.getBelongsToField(PositionFields.BATCH))) {
            newPosition.setField(PositionFields.BATCH, position.getBelongsToField(PositionFields.BATCH));
        }

        setPositionQuantityAndGivenQuantity(quantity, newPosition);

        return newPosition;
    }

}
