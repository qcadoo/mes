/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo Framework
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
package com.qcadoo.mes.productFlowThruDivision.states;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import com.qcadoo.mes.materialFlow.constants.LocationFields;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.qcadoo.mes.advancedGenealogy.constants.BatchFields;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.basic.constants.CurrencyFields;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.basic.constants.UnitConversionItemFieldsB;
import com.qcadoo.mes.basic.util.CurrencyService;
import com.qcadoo.mes.costNormsForMaterials.CostNormsForMaterialsService;
import com.qcadoo.mes.costNormsForMaterials.constants.OrderFieldsCNFM;
import com.qcadoo.mes.costNormsForMaterials.orderRawMaterialCosts.domain.ProductWithQuantityAndCost;
import com.qcadoo.mes.materialFlow.constants.MaterialFlowConstants;
import com.qcadoo.mes.materialFlowResources.constants.DocumentFields;
import com.qcadoo.mes.materialFlowResources.constants.DocumentState;
import com.qcadoo.mes.materialFlowResources.constants.DocumentType;
import com.qcadoo.mes.materialFlowResources.constants.MaterialFlowResourcesConstants;
import com.qcadoo.mes.materialFlowResources.constants.PositionAttributeValueFields;
import com.qcadoo.mes.materialFlowResources.constants.PositionFields;
import com.qcadoo.mes.materialFlowResources.service.DocumentBuilder;
import com.qcadoo.mes.materialFlowResources.service.DocumentManagementService;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.states.constants.OrderState;
import com.qcadoo.mes.productFlowThruDivision.constants.DocumentFieldsPFTD;
import com.qcadoo.mes.productionCounting.ProductionTrackingService;
import com.qcadoo.mes.productionCounting.constants.*;
import com.qcadoo.mes.productionCounting.states.constants.ProductionTrackingStateStringValues;
import com.qcadoo.mes.productionCounting.utils.OrderClosingHelper;
import com.qcadoo.mes.productionCounting.utils.ProductionTrackingDocumentsHelper;
import com.qcadoo.model.api.BigDecimalUtils;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.model.api.search.SearchQueryBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.api.search.SearchResult;
import com.qcadoo.model.api.units.PossibleUnitConversions;
import com.qcadoo.model.api.units.UnitConversionService;
import com.qcadoo.model.api.validators.ErrorMessage;

@Service
public final class ProductionTrackingListenerServicePFTD {

    private static final String L_ERROR_NOT_ENOUGH_RESOURCES = "materialFlow.error.position.quantity.notEnoughResources";

    private static final String L_USER = "user";

    private static final String L_QUALITY_RATING = "qualityRating";

    @Autowired
    private CostNormsForMaterialsService costNormsForMaterialsService;

    @Autowired
    private DocumentManagementService documentManagementService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private ParameterService parameterService;

    @Autowired
    private NumberService numberService;

    @Autowired
    private UnitConversionService unitConversionService;

    @Autowired
    private OrderClosingHelper orderClosingHelper;

    @Autowired
    private ProductionTrackingDocumentsHelper productionTrackingDocumentsHelper;

    @Autowired
    private ProductionTrackingService productionTrackingService;

    @Autowired
    private CurrencyService currencyService;

    public Entity onAccept(final Entity productionTracking, final String sourceState) {
        boolean isCorrection = productionTracking.getBooleanField(ProductionTrackingFields.IS_CORRECTION);

        if (!isCorrection && !ProductionTrackingStateStringValues.CORRECTED.equals(sourceState)) {
            createWarehouseDocuments(productionTracking);
        }

        return productionTracking;
    }

    public void createWarehouseDocuments(final Entity productionTracking) {
        Entity order = productionTracking.getBelongsToField(ProductionTrackingFields.ORDER);
        Entity technologyOperationComponent = productionTracking
                .getBelongsToField(ProductionTrackingFields.TECHNOLOGY_OPERATION_COMPONENT);

        List<Entity> trackingOperationProductOutComponents = productionTracking
                .getHasManyField(ProductionTrackingFields.TRACKING_OPERATION_PRODUCT_OUT_COMPONENTS);
        Multimap<Long, Entity> groupedRecordOutProducts = productionTrackingDocumentsHelper
                .fillFromBPCProductOut(trackingOperationProductOutComponents, order, true);
        List<Entity> trackingOperationProductInComponents = productionTracking
                .getHasManyField(ProductionTrackingFields.TRACKING_OPERATION_PRODUCT_IN_COMPONENTS);
        Multimap<Long, Entity> groupedRecordInProducts = productionTrackingDocumentsHelper
                .fillFromBPCProductIn(trackingOperationProductInComponents, order, technologyOperationComponent, true);

        if (!checkIfProductsAvailableInStock(productionTracking, groupedRecordInProducts)) {
            return;
        }

        String receiptOfProducts = parameterService.getParameter().getStringField(ParameterFieldsPC.RECEIPT_OF_PRODUCTS);

        if (ReceiptOfProducts.ON_ACCEPTANCE_REGISTRATION_RECORD.getStringValue().equals(receiptOfProducts)
                || ReceiptOfProducts.END_OF_THE_ORDER.getStringValue().equals(receiptOfProducts)) {
            for (Long warehouseId : groupedRecordOutProducts.keySet()) {
                Entity locationTo = getLocationDD().get(warehouseId);
                Entity inboundDocument = createOrUpdateInternalInboundDocumentForFinalProducts(locationTo, order,
                        groupedRecordOutProducts.get(warehouseId), productionTracking.getBelongsToField(L_USER));

                if (Objects.nonNull(inboundDocument) && !inboundDocument.isValid()) {
                    for (ErrorMessage error : inboundDocument.getGlobalErrors()) {
                        productionTracking.addGlobalError(error.getMessage(), error.getVars());
                    }

                    productionTracking.addGlobalError(
                            "productFlowThruDivision.productionTracking.productionTrackingError.createInternalInboundDocument");

                    return;
                }
            }

            TransactionAspectSupport.currentTransactionStatus().flush();
        }
        String releaseOfMaterials = parameterService.getParameter().getStringField(ParameterFieldsPC.RELEASE_OF_MATERIALS);
        if (ReleaseOfMaterials.ON_ACCEPTANCE_REGISTRATION_RECORD.getStringValue().equals(releaseOfMaterials)) {

            boolean errorsDisplayed = false;

            for (Long warehouseId : groupedRecordInProducts.keySet()) {
                Entity locationFrom = getLocationDD().get(warehouseId);
                Entity outboundDocument = createInternalOutboundDocumentForComponents(locationFrom, order,
                        groupedRecordInProducts.get(warehouseId), productionTracking.getBelongsToField(L_USER));

                if (Objects.nonNull(outboundDocument) && !outboundDocument.isValid()) {
                    for (ErrorMessage error : outboundDocument.getGlobalErrors()) {
                        if (error.getMessage().equalsIgnoreCase(L_ERROR_NOT_ENOUGH_RESOURCES)) {
                            productionTracking.addGlobalError(error.getMessage(), false, error.getVars());
                        } else if (!errorsDisplayed) {
                            productionTracking.addGlobalError(error.getMessage(), error.getVars());
                        }
                    }

                    if (!errorsDisplayed) {
                        productionTracking.addGlobalError(
                                "productFlowThruDivision.productionTracking.productionTrackingError.createInternalOutboundDocument");

                        errorsDisplayed = true;
                    }
                }
            }

            if (errorsDisplayed) {
                return;
            }

            updateCostsForOrder(order);
        }
    }

    private boolean checkIfProductsAvailableInStock(Entity productionTracking, Multimap<Long, Entity> groupedRecordInProducts) {
        DataDefinition warehouseDD = dataDefinitionService.get(MaterialFlowConstants.PLUGIN_IDENTIFIER,
                MaterialFlowConstants.MODEL_LOCATION);
        for (Long warehouseId : groupedRecordInProducts.keySet()) {
            Collection<Entity> inProductsRecords = groupedRecordInProducts.get(warehouseId);
            Entity warehouse = warehouseDD.get(warehouseId);
            List<Entity> products = inProductsRecords.stream()
                    .map(tpin -> tpin.getBelongsToField(TrackingOperationProductInComponentFields.PRODUCT))
                    .collect(Collectors.toList());
            Map<Long, BigDecimal> productAndQuantities =
                    productionTrackingDocumentsHelper.getQuantitiesForProductsAndLocation(products, warehouse);
            return checkIfResourcesAreSufficient(productionTracking, productAndQuantities, inProductsRecords, warehouse);
        }

        return false;
    }

    private boolean checkIfResourcesAreSufficient(final Entity productionTracking, Map<Long, BigDecimal> quantitiesInWarehouse,
                                                  Collection<Entity> inProductsRecords, final Entity warehouse) {
        StringBuilder errorMessage = new StringBuilder();
        String warehouseNumber = warehouse.getStringField(LocationFields.NUMBER);
        List<String> errorProducts = Lists.newArrayList();
        for (Entity inProduct : inProductsRecords) {
            Entity product = inProduct.getBelongsToField(TrackingOperationProductInComponentFields.PRODUCT);
            BigDecimal quantity = inProduct.getDecimalField(TrackingOperationProductInComponentFields.USED_QUANTITY);
            BigDecimal availableQuantity = quantitiesInWarehouse.get(product.getId());
            if (availableQuantity == null || quantity.compareTo(availableQuantity) > 0) {
                errorProducts.add(product.getStringField(ProductFields.NUMBER));
            }
        }
        if (errorProducts.isEmpty()) {
            return true;
        }
        errorMessage.append(errorProducts.stream().distinct().collect(Collectors.joining(", ")));
        if (errorMessage.length() + warehouseNumber.length() < 255) {
            productionTracking.addGlobalError("materialFlow.error.position.quantity.notEnoughResources", false,
                    warehouseNumber, errorMessage.toString());
        } else {
            errorProducts.forEach(p -> {
                productionTracking.addGlobalError("materialFlow.error.position.quantity.notEnoughResources", false,
                        warehouseNumber, p);
            });
        }
        return false;
    }

    public Entity createInternalOutboundDocumentForComponents(final Entity locationFrom, final Entity order,
                                                              final Collection<Entity> inProductsRecords, final Entity user) {
        DocumentBuilder internalOutboundBuilder = documentManagementService.getDocumentBuilder(user);

        internalOutboundBuilder.internalOutbound(locationFrom);

        HashSet<Entity> inProductsWithoutDuplicates = Sets.newHashSet();

        DataDefinition positionDD = getPositionDD();

        for (Entity inProductRecord : inProductsRecords) {
            List<Entity> usedBatches = inProductRecord.getHasManyField(TrackingOperationProductInComponentFields.USED_BATCHES);

            Entity inProduct = inProductRecord.getBelongsToField(TrackingOperationProductInComponentFields.PRODUCT);

            if (!inProductsWithoutDuplicates.contains(inProduct)) {
                if (usedBatches.isEmpty()) {
                    Entity position = preparePositionForInProduct(positionDD, inProductRecord, inProduct);

                    internalOutboundBuilder.addPosition(position);
                } else {
                    for (Entity usedBatch : usedBatches) {
                        Entity position = preparePositionForUsedBatch(positionDD, inProductRecord, inProduct, usedBatch);

                        internalOutboundBuilder.addPosition(position);
                    }
                }
            }

            inProductsWithoutDuplicates.add(inProduct);
        }

        internalOutboundBuilder.setField(DocumentFieldsPFTD.ORDER, order);

        return internalOutboundBuilder.setAccepted().buildWithEntityRuntimeException();
    }

    private Entity preparePositionForUsedBatch(final DataDefinition positionDD, final Entity inProductRecord,
                                               final Entity inProduct, final Entity usedBatch) {
        Entity position = positionDD.create();

        BigDecimal usedQuantity = usedBatch.getDecimalField(UsedBatchFields.QUANTITY);
        BigDecimal givenQuantity = productionTrackingService.calculateGivenQuantity(inProductRecord, usedQuantity)
                .orElse(usedQuantity);
        BigDecimal conversion = BigDecimal.ONE;
        String unit = inProduct.getStringField(ProductFields.UNIT);
        String givenUnit = inProductRecord.getStringField(TrackingOperationProductOutComponentFields.GIVEN_UNIT);

        if (Objects.nonNull(usedQuantity)) {
            PossibleUnitConversions unitConversions = unitConversionService.getPossibleConversions(unit,
                    searchCriteriaBuilder -> searchCriteriaBuilder
                            .add(SearchRestrictions.belongsTo(UnitConversionItemFieldsB.PRODUCT, inProduct)));

            if (unitConversions.isDefinedFor(givenUnit)) {
                conversion = numberService.setScaleWithDefaultMathContext(unitConversions.asUnitToConversionMap().get(givenUnit));
            }
        }

        position.setField(PositionFields.GIVEN_UNIT,
                inProductRecord.getStringField(TrackingOperationProductInComponentFields.GIVEN_UNIT));
        position.setField(PositionFields.PRODUCT, inProduct);
        position.setField(PositionFields.QUANTITY, usedQuantity);
        position.setField(PositionFields.GIVEN_QUANTITY, givenQuantity);
        position.setField(PositionFields.CONVERSION, conversion);
        position.setField(PositionFields.BATCH, usedBatch.getBelongsToField(UsedBatchFields.BATCH).getId());

        return position;
    }

    private Entity preparePositionForInProduct(final DataDefinition positionDD, final Entity inProductRecord,
                                               final Entity inProduct) {
        Entity position = positionDD.create();

        BigDecimal usedQuantity = inProductRecord.getDecimalField(TrackingOperationProductInComponentFields.USED_QUANTITY);
        BigDecimal givenQuantity = inProductRecord.getDecimalField(TrackingOperationProductInComponentFields.GIVEN_QUANTITY);
        BigDecimal conversion = BigDecimal.ONE;
        String unit = inProduct.getStringField(ProductFields.UNIT);
        String givenUnit = inProductRecord.getStringField(TrackingOperationProductOutComponentFields.GIVEN_UNIT);

        if (Objects.nonNull(usedQuantity) && Objects.nonNull(givenQuantity)) {
            PossibleUnitConversions unitConversions = unitConversionService.getPossibleConversions(unit,
                    searchCriteriaBuilder -> searchCriteriaBuilder
                            .add(SearchRestrictions.belongsTo(UnitConversionItemFieldsB.PRODUCT, inProduct)));

            if (unitConversions.isDefinedFor(givenUnit)) {
                conversion = numberService.setScaleWithDefaultMathContext(unitConversions.asUnitToConversionMap().get(givenUnit));
            }
        }

        position.setField(PositionFields.GIVEN_UNIT,
                inProductRecord.getStringField(TrackingOperationProductInComponentFields.GIVEN_UNIT));
        position.setField(PositionFields.PRODUCT, inProduct);
        position.setField(PositionFields.QUANTITY, usedQuantity);
        position.setField(PositionFields.GIVEN_QUANTITY, givenQuantity);
        position.setField(PositionFields.CONVERSION, conversion);

        return position;
    }

    private Entity createOrUpdateInternalInboundDocumentForFinalProducts(final Entity locationTo, final Entity order,
                                                                         final Collection<Entity> outProductsRecords, final Entity user) {
        String receiptOfProducts = parameterService.getParameter().getStringField(ParameterFieldsPC.RECEIPT_OF_PRODUCTS);

        List<Entity> finalProductRecord = null;

        Collection<Entity> intermediateRecords = Lists.newArrayList();

        for (Entity outProductRecord : outProductsRecords) {
            if (isFinalProductForOrder(order,
                    outProductRecord.getBelongsToField(TrackingOperationProductOutComponentFields.PRODUCT))) {
                finalProductRecord = Lists.newArrayList(outProductRecord);
            } else {
                intermediateRecords.add(outProductRecord);
            }
        }

        if (ReceiptOfProducts.ON_ACCEPTANCE_REGISTRATION_RECORD.getStringValue().equals(receiptOfProducts)) {
            if (Objects.nonNull(finalProductRecord)) {
                Entity inboundForFinalProduct = createInternalInboundDocumentForFinalProducts(locationTo, order,
                        finalProductRecord, true, user);

                if (Objects.nonNull(inboundForFinalProduct) && !inboundForFinalProduct.isValid()
                        || intermediateRecords.isEmpty()) {
                    return inboundForFinalProduct;
                }
            }

            return createInternalInboundDocumentForFinalProducts(locationTo, order, intermediateRecords, true, user);
        } else if (ReceiptOfProducts.END_OF_THE_ORDER.getStringValue().equals(receiptOfProducts)) {
            Entity existingInboundDocument = getDocumentDD().find()
                    .add(SearchRestrictions.belongsTo(DocumentFieldsPFTD.ORDER, order))
                    .add(SearchRestrictions.belongsTo(DocumentFields.LOCATION_TO, locationTo))
                    .add(SearchRestrictions.eq(DocumentFields.STATE, DocumentState.DRAFT.getStringValue()))
                    .add(SearchRestrictions.eq(DocumentFields.TYPE, DocumentType.INTERNAL_INBOUND.getStringValue()))
                    .setMaxResults(1).uniqueResult();

            if (Objects.nonNull(existingInboundDocument)) {
                if (Objects.nonNull(finalProductRecord)) {
                    return updateInternalInboundDocumentForFinalProducts(order, existingInboundDocument,
                            finalProductRecord, true);
                } else {
                    return updateInternalInboundDocumentForFinalProducts(order, existingInboundDocument,
                            intermediateRecords, false);
                }
            } else {
                if (Objects.nonNull(finalProductRecord)) {
                    Entity inboundForFinalProduct = createInternalInboundDocumentForFinalProducts(locationTo, order,
                            finalProductRecord, user);

                    if (Objects.nonNull(inboundForFinalProduct) && !inboundForFinalProduct.isValid()
                            || intermediateRecords.isEmpty()) {
                        return inboundForFinalProduct;
                    }
                }
            }

            return createInternalInboundDocumentForFinalProducts(locationTo, order, intermediateRecords, user);
        } else {
            return null;
        }
    }

    private Entity updateInternalInboundDocumentForFinalProducts(final Entity order, final Entity existingInboundDocument,
                                                                 final Collection<Entity> outProductsRecords, boolean isFinalProduct) {
        DataDefinition positionDD = getPositionDD();

        List<Entity> positions = Lists.newArrayList(existingInboundDocument.getHasManyField(DocumentFields.POSITIONS));

        for (Entity outProductRecord : outProductsRecords) {
            Entity outProduct = outProductRecord.getBelongsToField(TrackingOperationProductOutComponentFields.PRODUCT);
            Entity outBatch = outProductRecord.getBelongsToField(TrackingOperationProductOutComponentFields.PRODUCTION_TRACKING)
                    .getBelongsToField(ProductionTrackingFields.BATCH);
            if (!isFinalProduct) {
                outBatch = null;
            }

            Entity storageLocation = outProductRecord
                    .getBelongsToField(TrackingOperationProductOutComponentFields.STORAGE_LOCATION);
            Entity palletNumber = outProductRecord
                    .getBelongsToField(TrackingOperationProductOutComponentFields.PALLET_NUMBER);

            java.util.Optional<BigDecimal> usedQuantity = Optional
                    .ofNullable(outProductRecord.getDecimalField(TrackingOperationProductInComponentFields.USED_QUANTITY));
            java.util.Optional<BigDecimal> givenQuantity = Optional
                    .ofNullable(outProductRecord.getDecimalField(TrackingOperationProductInComponentFields.GIVEN_QUANTITY));
            java.util.Optional<String> givenUnit = Optional
                    .ofNullable(outProductRecord.getStringField(TrackingOperationProductInComponentFields.GIVEN_UNIT));

            Entity existingPosition = filterPosition(positions, outProduct, givenUnit.orElse(null), outBatch, storageLocation, palletNumber);

            if (Objects.nonNull(existingPosition)) {
                java.util.Optional<BigDecimal> quantity = Optional
                        .ofNullable(existingPosition.getDecimalField(PositionFields.QUANTITY));
                java.util.Optional<BigDecimal> givenQuantityFromPosition = Optional
                        .ofNullable(existingPosition.getDecimalField(PositionFields.GIVEN_QUANTITY));

                existingPosition.setField(PositionFields.QUANTITY,
                        quantity.orElse(BigDecimal.ZERO).add(usedQuantity.orElse(BigDecimal.ZERO)));

                if (givenQuantity.isPresent()) {
                    existingPosition.setField(PositionFields.GIVEN_QUANTITY,
                            givenQuantity.orElse(BigDecimal.ZERO).add(givenQuantityFromPosition.orElse(BigDecimal.ZERO)));
                }

                fillAttributes(outProductRecord, existingPosition);
                existingPosition.setField(PositionFields.GIVEN_UNIT, givenUnit.get());
            } else {
                Entity productionTracking = outProductRecord
                        .getBelongsToField(TrackingOperationProductOutComponentFields.PRODUCTION_TRACKING);

                Entity position = positionDD.create();

                position.setField(PositionFields.PRODUCT, outProduct);
                position.setField(PositionFields.QUANTITY, usedQuantity.get());

                BigDecimal conversion = BigDecimal.ONE;
                String unit = outProduct.getStringField(ProductFields.UNIT);

                if (givenQuantity.isPresent()) {
                    PossibleUnitConversions unitConversions = unitConversionService.getPossibleConversions(unit,
                            searchCriteriaBuilder -> searchCriteriaBuilder
                                    .add(SearchRestrictions.belongsTo(UnitConversionItemFieldsB.PRODUCT, outProduct)));

                    if (unitConversions.isDefinedFor(givenUnit.get())) {
                        conversion = numberService
                                .setScaleWithDefaultMathContext(unitConversions.asUnitToConversionMap().get(givenUnit.get()));
                    }

                    position.setField(PositionFields.GIVEN_QUANTITY, givenQuantity.get());
                }

                position.setField(PositionFields.GIVEN_UNIT, givenUnit.get());
                position.setField(PositionFields.CONVERSION, conversion);
                position.setField(PositionFields.PRODUCTION_DATE, new Date());
                String priceBasedOn = parameterService.getParameter().getStringField(ParameterFieldsPC.PRICE_BASED_ON);
                boolean isNominalProductCost = priceBasedOn != null
                        && priceBasedOn.equals(PriceBasedOn.NOMINAL_PRODUCT_COST.getStringValue());
                if (isNominalProductCost) {
                    position.setField(PositionFields.PRICE, getNominalCost(outProduct));
                }
                Entity batch = productionTracking.getBelongsToField(ProductionTrackingFields.BATCH);

                if (Objects.nonNull(batch) && batch.getBelongsToField(BatchFields.PRODUCT).getId().equals(outProduct.getId())) {
                    position.setField(PositionFields.BATCH,
                            productionTracking.getBelongsToField(ProductionTrackingFields.BATCH).getId());
                    position.setField(PositionFields.QUALITY_RATING, productionTracking.getStringField(L_QUALITY_RATING));
                } else if (order.getBelongsToField(OrderFields.PRODUCT).getId().equals(outProduct.getId())) {
                    position.setField(PositionFields.QUALITY_RATING, productionTracking.getStringField(L_QUALITY_RATING));
                }

                if (Objects.nonNull(
                        outProductRecord.getBelongsToField(TrackingOperationProductOutComponentFields.STORAGE_LOCATION))) {
                    position.setField(PositionFields.STORAGE_LOCATION, outProductRecord
                            .getBelongsToField(TrackingOperationProductOutComponentFields.STORAGE_LOCATION).getId());
                }

                if (Objects.nonNull(
                        outProductRecord.getBelongsToField(TrackingOperationProductOutComponentFields.PALLET_NUMBER))) {
                    position.setField(PositionFields.PALLET_NUMBER, outProductRecord
                            .getBelongsToField(TrackingOperationProductOutComponentFields.PALLET_NUMBER).getId());
                }

                if (order.getBelongsToField(OrderFields.PRODUCT).getId().equals(outProduct.getId())) {
                    position.setField(PositionFields.EXPIRATION_DATE,
                            productionTracking.getDateField(ProductionTrackingFields.EXPIRATION_DATE));
                }

                fillAttributes(outProductRecord, position);

                positions.add(position);
            }
        }

        existingInboundDocument.setField(DocumentFields.POSITIONS, positions);

        return existingInboundDocument.getDataDefinition().save(existingInboundDocument);
    }

    private void fillAttributes(final Entity outProductRecord, final Entity position) {
        List<Entity> attributes = Lists.newArrayList();

        outProductRecord.getHasManyField(TrackingOperationProductOutComponentFields.PROD_OUT_RESOURCE_ATTR_VALS).forEach(aVal -> {
            Entity docPositionAtrrVal = dataDefinitionService.get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER,
                    MaterialFlowResourcesConstants.MODEL_POSITION_ATTRIBUTE_VALUE).create();
            docPositionAtrrVal.setField(PositionAttributeValueFields.ATTRIBUTE,
                    aVal.getBelongsToField(ProdOutResourceAttrValFields.ATTRIBUTE).getId());

            if (Objects.nonNull(aVal.getBelongsToField(PositionAttributeValueFields.ATTRIBUTE_VALUE))) {
                docPositionAtrrVal.setField(PositionAttributeValueFields.ATTRIBUTE_VALUE,
                        aVal.getBelongsToField(ProdOutResourceAttrValFields.ATTRIBUTE_VALUE).getId());
            }

            docPositionAtrrVal.setField(PositionAttributeValueFields.VALUE,
                    aVal.getStringField(ProdOutResourceAttrValFields.VALUE));

            attributes.add(docPositionAtrrVal);
        });

        position.setField(PositionFields.POSITION_ATTRIBUTE_VALUES, attributes);
    }

    private Entity filterPosition(final List<Entity> existingPositions, final Entity outProduct, final String givenUnit,
                                  final Entity outBatch, final Entity storageLocation, Entity palletNumber) {
        for (Entity position : existingPositions) {
            if (checkPositionConditions(position, outProduct, givenUnit, outBatch, storageLocation, palletNumber)) {
                return position;
            }
        }

        return null;
    }

    private boolean checkPositionConditions(final Entity position, final Entity outProduct, final String givenUnit,
                                            final Entity outBatch, final Entity storageLocation, final Entity palletNumber) {
        boolean isPosition = true;

        if (!position.getBelongsToField(PositionFields.PRODUCT).getId().equals(outProduct.getId())) {
            isPosition = false;
        }

        if (StringUtils.isNoneBlank(givenUnit) && !position.getStringField(PositionFields.GIVEN_UNIT).equals(givenUnit)) {
            isPosition = false;
        }

        if (Objects.nonNull(outBatch) && Objects.nonNull(position.getBelongsToField(PositionFields.BATCH))) {
            if (!position.getBelongsToField(PositionFields.BATCH).getId().equals(outBatch.getId())) {
                isPosition = false;
            }
        } else if ((Objects.isNull(outBatch) && Objects.nonNull(position.getBelongsToField(PositionFields.BATCH)))
                || (Objects.nonNull(outBatch) && Objects.isNull(position.getBelongsToField(PositionFields.BATCH)))) {
            isPosition = false;
        }

        if (Objects.nonNull(storageLocation) && Objects.nonNull(position.getBelongsToField(PositionFields.STORAGE_LOCATION))) {
            if (!position.getBelongsToField(PositionFields.STORAGE_LOCATION).getId().equals(storageLocation.getId())) {
                isPosition = false;
            }
        } else if ((Objects.isNull(storageLocation)
                && Objects.nonNull(position.getBelongsToField(PositionFields.STORAGE_LOCATION)))
                || (Objects.nonNull(storageLocation)
                && Objects.isNull(position.getBelongsToField(PositionFields.STORAGE_LOCATION)))) {
            isPosition = false;
        }

        if (Objects.nonNull(palletNumber) && Objects.nonNull(position.getBelongsToField(PositionFields.PALLET_NUMBER))) {
            if (!position.getBelongsToField(PositionFields.PALLET_NUMBER).getId().equals(palletNumber.getId())) {
                isPosition = false;
            }
        } else if ((Objects.isNull(palletNumber)
                && Objects.nonNull(position.getBelongsToField(PositionFields.PALLET_NUMBER)))
                || (Objects.nonNull(palletNumber)
                && Objects.isNull(position.getBelongsToField(PositionFields.PALLET_NUMBER)))) {
            isPosition = false;
        }

        return isPosition;
    }

    public Entity createInternalInboundDocumentForFinalProducts(final Entity locationTo, final Entity order,
                                                                final Collection<Entity> outProductsRecords, Entity user) {
        String priceBasedOn = parameterService.getParameter().getStringField(ParameterFieldsPC.PRICE_BASED_ON);
        boolean isNominalProductCost = priceBasedOn != null
                && priceBasedOn.equals(PriceBasedOn.NOMINAL_PRODUCT_COST.getStringValue());
        return createInternalInboundDocumentForFinalProducts(locationTo, order, outProductsRecords, isNominalProductCost, user);
    }

    private Entity createInternalInboundDocumentForFinalProducts(final Entity locationTo, final Entity order,
                                                                 final Collection<Entity> outProductsRecords, final boolean isBasedOnNominalCost, Entity user) {
        DocumentBuilder internalInboundBuilder = documentManagementService.getDocumentBuilder(user);

        internalInboundBuilder.internalInbound(locationTo);

        boolean isFinalProduct = false;

        Entity productionTracking = null;

        for (Entity outProductRecord : outProductsRecords) {
            Entity outProduct = outProductRecord.getBelongsToField(TrackingOperationProductOutComponentFields.PRODUCT);

            if (Objects.isNull(productionTracking)) {
                productionTracking = outProductRecord
                        .getBelongsToField(TrackingOperationProductOutComponentFields.PRODUCTION_TRACKING);
            }

            if (isFinalProductForOrder(order, outProduct)) {
                isFinalProduct = true;
            }

            Entity position = getPositionDD().create();

            BigDecimal usedQuantity = outProductRecord.getDecimalField(TrackingOperationProductOutComponentFields.USED_QUANTITY);
            BigDecimal givenQuantity = outProductRecord
                    .getDecimalField(TrackingOperationProductOutComponentFields.GIVEN_QUANTITY);
            BigDecimal conversion = BigDecimal.ONE;
            String unit = outProduct.getStringField(ProductFields.UNIT);
            String givenUnit = outProductRecord.getStringField(TrackingOperationProductOutComponentFields.GIVEN_UNIT);

            if (Objects.nonNull(usedQuantity) && Objects.nonNull(givenQuantity)) {
                PossibleUnitConversions unitConversions = unitConversionService.getPossibleConversions(unit,
                        searchCriteriaBuilder -> searchCriteriaBuilder
                                .add(SearchRestrictions.belongsTo(UnitConversionItemFieldsB.PRODUCT, outProduct)));

                if (unitConversions.isDefinedFor(givenUnit)) {
                    conversion = numberService
                            .setScaleWithDefaultMathContext(unitConversions.asUnitToConversionMap().get(givenUnit));
                }
            }

            position.setField(PositionFields.PRODUCT, outProduct);
            position.setField(PositionFields.QUANTITY, usedQuantity);
            position.setField(PositionFields.CONVERSION, conversion);
            position.setField(PositionFields.GIVEN_QUANTITY, givenQuantity);
            position.setField(PositionFields.GIVEN_UNIT,
                    outProductRecord.getStringField(TrackingOperationProductOutComponentFields.GIVEN_UNIT));

            if (isBasedOnNominalCost) {
                position.setField(PositionFields.PRICE, getNominalCost(outProduct));
            }

            position.setField(PositionFields.PRODUCTION_DATE, new Date());

            Entity batch = productionTracking.getBelongsToField(ProductionTrackingFields.BATCH);

            if (Objects.nonNull(batch) && batch.getBelongsToField(BatchFields.PRODUCT).getId().equals(outProduct.getId())) {
                position.setField(PositionFields.BATCH,
                        productionTracking.getBelongsToField(ProductionTrackingFields.BATCH).getId());
                position.setField(PositionFields.QUALITY_RATING, productionTracking.getStringField(L_QUALITY_RATING));
            } else if (order.getBelongsToField(OrderFields.PRODUCT).getId().equals(outProduct.getId())) {
                position.setField(PositionFields.QUALITY_RATING, productionTracking.getStringField(L_QUALITY_RATING));
            }

            if (Objects
                    .nonNull(outProductRecord.getBelongsToField(TrackingOperationProductOutComponentFields.STORAGE_LOCATION))) {
                position.setField(PositionFields.STORAGE_LOCATION,
                        outProductRecord.getBelongsToField(TrackingOperationProductOutComponentFields.STORAGE_LOCATION).getId());
            }

            if (Objects
                    .nonNull(outProductRecord.getBelongsToField(TrackingOperationProductOutComponentFields.PALLET_NUMBER))) {
                position.setField(PositionFields.PALLET_NUMBER,
                        outProductRecord.getBelongsToField(TrackingOperationProductOutComponentFields.PALLET_NUMBER).getId());
            }

            if (order.getBelongsToField(OrderFields.PRODUCT).getId().equals(outProduct.getId())) {
                position.setField(PositionFields.EXPIRATION_DATE,
                        productionTracking.getDateField(ProductionTrackingFields.EXPIRATION_DATE));
            }

            fillAttributes(outProductRecord, position);

            internalInboundBuilder.addPosition(position);
        }

        internalInboundBuilder.setField(DocumentFieldsPFTD.ORDER, order);

        String receiptOfProducts = parameterService.getParameter().getStringField(ParameterFieldsPC.RECEIPT_OF_PRODUCTS);

        if (ReceiptOfProducts.ON_ACCEPTANCE_REGISTRATION_RECORD.getStringValue().equals(receiptOfProducts)
                && (OrderState.COMPLETED.equals(OrderState.of(order)) || !isFinalProduct || isBasedOnNominalCost
                || orderClosingHelper.orderShouldBeClosedWithRecalculate(productionTracking))
                || orderClosingHelper.orderShouldBeClosedWithRecalculate(productionTracking)) {
            internalInboundBuilder.setAccepted();
        }

        return internalInboundBuilder.buildWithEntityRuntimeException();
    }

    private BigDecimal getNominalCost(final Entity outProduct) {
        BigDecimal nominalCost = BigDecimalUtils.convertNullToZero(outProduct.getDecimalField("nominalCost"));
        Entity currency = outProduct.getBelongsToField("nominalCostCurrency");

        if (currency != null && CurrencyService.PLN.equals(currencyService.getCurrencyAlphabeticCode())
                && !CurrencyService.PLN.equals(currency.getStringField(CurrencyFields.ALPHABETIC_CODE))) {
            nominalCost = currencyService.getConvertedValue(nominalCost, currency);
        }

        return nominalCost;
    }

    private boolean isFinalProductForOrder(final Entity order, final Entity product) {
        return order.getBelongsToField(OrderFields.PRODUCT).getId().equals(product.getId());
    }

    public void updateCostsForOrder(final Entity order) {
        DataDefinition positionDD = getPositionDD();

        SearchQueryBuilder searchQueryBuilder = positionDD
                .find("SELECT pr.id AS product, SUM(p.quantity) AS quantity, SUM(p.quantity * p.price) AS price "
                        + "FROM #materialFlowResources_position p JOIN p.document AS d join p.product AS pr "
                        + "WHERE d.order = :order_id AND d.type = :type " + "GROUP BY d.order, d.type, pr.id");

        searchQueryBuilder.setLong("order_id", order.getId());
        searchQueryBuilder.setString("type", DocumentType.INTERNAL_OUTBOUND.getStringValue());

        SearchResult result = searchQueryBuilder.list();

        List<ProductWithQuantityAndCost> productsWithQuantitiesAndCosts = Lists.newArrayList();

        for (Entity costsForProduct : result.getEntities()) {
            Long product = (Long) costsForProduct.getField(PositionFields.PRODUCT);
            BigDecimal quantity = costsForProduct.getDecimalField(PositionFields.QUANTITY);
            BigDecimal cost = costsForProduct.getDecimalField(PositionFields.PRICE);

            productsWithQuantitiesAndCosts.add(new ProductWithQuantityAndCost(product, quantity, cost));
        }

        List<Entity> updatedCosts = costNormsForMaterialsService.updateCostsForProductInOrder(order,
                productsWithQuantitiesAndCosts);

        order.setField(OrderFieldsCNFM.TECHNOLOGY_INST_OPER_PRODUCT_IN_COMPS, updatedCosts);
    }

    private DataDefinition getDocumentDD() {
        return dataDefinitionService.get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER,
                MaterialFlowResourcesConstants.MODEL_DOCUMENT);
    }

    private DataDefinition getPositionDD() {
        return dataDefinitionService.get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER,
                MaterialFlowResourcesConstants.MODEL_POSITION);
    }

    private DataDefinition getLocationDD() {
        return dataDefinitionService.get(MaterialFlowConstants.PLUGIN_IDENTIFIER, MaterialFlowConstants.MODEL_LOCATION);
    }

}
