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

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.qcadoo.mes.advancedGenealogy.constants.BatchFields;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.basic.constants.CurrencyFields;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.basic.constants.UnitConversionItemFieldsB;
import com.qcadoo.mes.basic.util.CurrencyService;
import com.qcadoo.mes.basicProductionCounting.constants.BasicProductionCountingConstants;
import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityFields;
import com.qcadoo.mes.costNormsForMaterials.CostNormsForMaterialsService;
import com.qcadoo.mes.costNormsForMaterials.constants.OrderFieldsCNFM;
import com.qcadoo.mes.costNormsForMaterials.orderRawMaterialCosts.domain.ProductWithQuantityAndCost;
import com.qcadoo.mes.materialFlow.constants.LocationFields;
import com.qcadoo.mes.materialFlow.constants.MaterialFlowConstants;
import com.qcadoo.mes.materialFlowResources.constants.*;
import com.qcadoo.mes.materialFlowResources.service.DocumentBuilder;
import com.qcadoo.mes.materialFlowResources.service.DocumentManagementService;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.states.constants.OrderState;
import com.qcadoo.mes.productFlowThruDivision.constants.DocumentFieldsPFTD;
import com.qcadoo.mes.productFlowThruDivision.constants.ProductionCountingQuantityFieldsPFTD;
import com.qcadoo.mes.productFlowThruDivision.constants.ProductionFlowComponent;
import com.qcadoo.mes.productFlowThruDivision.realProductionCost.RealProductionCostService;
import com.qcadoo.mes.productionCounting.ProductionTrackingService;
import com.qcadoo.mes.productionCounting.constants.*;
import com.qcadoo.mes.productionCounting.states.constants.ProductionTrackingStateStringValues;
import com.qcadoo.mes.productionCounting.utils.OrderClosingHelper;
import com.qcadoo.mes.productionCounting.utils.ProductionTrackingDocumentsHelper;
import com.qcadoo.model.api.*;
import com.qcadoo.model.api.search.SearchQueryBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.api.units.PossibleUnitConversions;
import com.qcadoo.model.api.units.UnitConversionService;
import com.qcadoo.model.api.validators.ErrorMessage;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public final class ProductionTrackingListenerServicePFTD {

    private static final String L_ERROR_NOT_ENOUGH_RESOURCES = "materialFlow.error.position.quantity.notEnoughResources";

    private static final String L_USER = "user";

    private static final String L_QUALITY_RATING = "qualityRating";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NumberService numberService;

    @Autowired
    private CurrencyService currencyService;

    @Autowired
    private UnitConversionService unitConversionService;

    @Autowired
    private ParameterService parameterService;

    @Autowired
    private CostNormsForMaterialsService costNormsForMaterialsService;

    @Autowired
    private DocumentManagementService documentManagementService;

    @Autowired
    private OrderClosingHelper orderClosingHelper;

    @Autowired
    private ProductionTrackingDocumentsHelper productionTrackingDocumentsHelper;

    @Autowired
    private ProductionTrackingService productionTrackingService;

    @Autowired
    private RealProductionCostService realProductionCostService;

    public Entity onAccept(final Entity productionTracking, final String sourceState) {
        boolean isCorrection = productionTracking.getBooleanField(ProductionTrackingFields.IS_CORRECTION);

        if (!isCorrection && !ProductionTrackingStateStringValues.CORRECTED.equals(sourceState)) {
            createWarehouseDocuments(productionTracking);
        }

        return productionTracking;
    }

    public boolean notCreateDocumentsForIntermediateRecords(final Entity productionTracking) {
        Entity order = productionTracking.getBelongsToField(ProductionTrackingFields.ORDER);

        boolean forEach = TypeOfProductionRecording.FOR_EACH.getStringValue().equals(
                order.getStringField(OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING));

        if (!forEach) {
            return false;
        }

        String receiptOfProducts = parameterService.getParameter().getStringField(ParameterFieldsPC.RECEIPT_OF_PRODUCTS);
        String releaseOfMaterials = parameterService.getParameter().getStringField(ParameterFieldsPC.RELEASE_OF_MATERIALS);

        boolean documentsOnEndOfTheOrder = ReceiptOfProducts.END_OF_THE_ORDER.getStringValue().equals(receiptOfProducts)
                && ReleaseOfMaterials.END_OF_THE_ORDER.getStringValue().equals(releaseOfMaterials);

        if (!documentsOnEndOfTheOrder) {
            return false;
        }

        Entity toc = productionTracking.getBelongsToField(ProductionTrackingFields.TECHNOLOGY_OPERATION_COMPONENT);

        if (Objects.isNull(toc)) {
            return false;
        }

        List<Entity> intermediateRecords = getProductionCountingQuantityDD().find()
                .add(SearchRestrictions.belongsTo(ProductionCountingQuantityFields.ORDER, order))
                .add(SearchRestrictions.belongsTo(ProductionCountingQuantityFields.TECHNOLOGY_OPERATION_COMPONENT, toc))
                .add(SearchRestrictions.eq(ProductionCountingQuantityFieldsPFTD.PRODUCTION_FLOW, ProductionFlowComponent.WAREHOUSE.getStringValue()))
                .list().getEntities();

        return !intermediateRecords.isEmpty();
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

        boolean releaseMaterials = ReleaseOfMaterials.ON_ACCEPTANCE_REGISTRATION_RECORD.getStringValue()
                .equals(parameterService.getParameter().getStringField(ParameterFieldsPC.RELEASE_OF_MATERIALS));

        boolean cumulated = TypeOfProductionRecording.CUMULATED.getStringValue().equals(
                order.getStringField(OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING));

        if (cumulated) {
            List<Long> productIds = groupedRecordInProducts.values().stream().map(topic -> topic.getBelongsToField(TrackingOperationProductInComponentFields.PRODUCT).getId()).collect(Collectors.toList());
            Set<Long> duplicatedProducts = productIds.stream()
                    .filter(i -> Collections.frequency(productIds, i) > 1)
                    .collect(Collectors.toSet());

            if (!duplicatedProducts.isEmpty()) {
                productionTracking.addGlobalError(
                        "productFlowThruDivision.location.components.locationsAreDifferent");

                return;
            }
        }

        if (!groupedRecordInProducts.isEmpty() && releaseMaterials &&
                !checkIfProductsAvailableInStock(productionTracking, groupedRecordInProducts)) {
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

    private boolean checkIfProductsAvailableInStock(final Entity productionTracking, final Multimap<Long, Entity> groupedRecordInProducts) {
        DataDefinition warehouseDD = getLocationDD();

        for (Long warehouseId : groupedRecordInProducts.keySet()) {
            List<Entity> trackingOperationProductInComponents = (List<Entity>) groupedRecordInProducts.get(warehouseId);
            Entity warehouse = warehouseDD.get(warehouseId);

            Map<Long, Map<String, BigDecimal>> productAndQuantities =
                    productionTrackingDocumentsHelper.getQuantitiesForProductsAndLocation(trackingOperationProductInComponents, warehouse);

            checkIfResourcesAreSufficient(productionTracking, productAndQuantities, trackingOperationProductInComponents, warehouse);
        }

        return productionTracking.isValid();
    }

    private boolean checkIfResourcesAreSufficient(final Entity productionTracking, final Map<Long, Map<String, BigDecimal>> quantitiesInWarehouse,
                                                  final Collection<Entity> trackingOperationProductInComponents, final Entity warehouse) {
        List<String> errorProducts = Lists.newArrayList();
        StringBuilder errorMessage = new StringBuilder();

        String warehouseNumber = warehouse.getStringField(LocationFields.NUMBER);

        for (Entity trackingOperationProductInComponent : trackingOperationProductInComponents) {
            Entity product = trackingOperationProductInComponent.getBelongsToField(TrackingOperationProductInComponentFields.PRODUCT);
            List<Entity> usedBatches = trackingOperationProductInComponent.getHasManyField(TrackingOperationProductInComponentFields.USED_BATCHES);

            Map<String, BigDecimal> batchQuantities = quantitiesInWarehouse.get(product.getId());

            if (Objects.isNull(batchQuantities)) {
                errorProducts.add(product.getStringField(ProductFields.NUMBER));
            } else {
                if (usedBatches.isEmpty()) {
                    BigDecimal quantity = trackingOperationProductInComponent.getDecimalField(TrackingOperationProductInComponentFields.USED_QUANTITY);

                    BigDecimal availableQuantity = batchQuantities.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);

                    if (Objects.isNull(availableQuantity) || quantity.compareTo(availableQuantity) > 0) {
                        errorProducts.add(product.getStringField(ProductFields.NUMBER));
                    }
                } else {
                    usedBatches.forEach(usedBatch -> {
                        String batchNumber = usedBatch.getBelongsToField(UsedBatchFields.BATCH).getStringField(BatchFields.NUMBER);
                        BigDecimal quantity = usedBatch.getDecimalField(UsedBatchFields.QUANTITY);

                        BigDecimal availableQuantity = batchQuantities.get(batchNumber);

                        if (Objects.isNull(availableQuantity) || quantity.compareTo(availableQuantity) > 0) {
                            errorProducts.add(product.getStringField(ProductFields.NUMBER));
                        }
                    });
                }
            }
        }

        if (errorProducts.isEmpty()) {
            return true;
        }

        errorMessage.append(errorProducts.stream().distinct().collect(Collectors.joining(", ")));

        if (errorMessage.length() + warehouseNumber.length() < 255) {
            productionTracking.addGlobalError("materialFlow.error.position.quantity.notEnoughResources", false,
                    errorMessage.toString(), warehouseNumber);
        } else {
            errorProducts.forEach(errorProduct ->
                    productionTracking.addGlobalError("materialFlow.error.position.quantity.notEnoughResources", false,
                            errorProduct, warehouseNumber)
            );
        }

        return false;
    }

    public Entity createInternalOutboundDocumentForComponents(final Entity locationFrom, final Entity order,
                                                              final Collection<Entity> trackingOperationProductInComponents, final Entity user) {
        DocumentBuilder internalOutboundBuilder = documentManagementService.getDocumentBuilder(user);

        internalOutboundBuilder.internalOutbound(locationFrom);

        HashSet<Entity> productsWithoutDuplicates = Sets.newHashSet();

        DataDefinition positionDD = getPositionDD();

        for (Entity trackingOperationProductInComponent : trackingOperationProductInComponents) {
            List<Entity> usedBatches = trackingOperationProductInComponent.getHasManyField(TrackingOperationProductInComponentFields.USED_BATCHES);

            Entity product = trackingOperationProductInComponent.getBelongsToField(TrackingOperationProductInComponentFields.PRODUCT);

            if (!productsWithoutDuplicates.contains(product)) {
                if (usedBatches.isEmpty()) {
                    Entity position = preparePositionForInProduct(positionDD, trackingOperationProductInComponent, product);

                    internalOutboundBuilder.addPosition(position);
                } else {
                    for (Entity usedBatch : usedBatches) {
                        Entity position = preparePositionForUsedBatch(positionDD, trackingOperationProductInComponent, product, usedBatch);

                        internalOutboundBuilder.addPosition(position);
                    }
                }
            }

            productsWithoutDuplicates.add(product);
        }

        internalOutboundBuilder.setField(DocumentFieldsPFTD.ORDER, order);

        return internalOutboundBuilder.setAccepted().buildWithEntityRuntimeException();
    }

    private Entity preparePositionForUsedBatch(final DataDefinition positionDD, final Entity trackingOperationProductInComponent,
                                               final Entity product, final Entity usedBatch) {
        Entity position = positionDD.create();

        BigDecimal usedQuantity = usedBatch.getDecimalField(UsedBatchFields.QUANTITY);
        BigDecimal givenQuantity = productionTrackingService.calculateGivenQuantity(trackingOperationProductInComponent, usedQuantity)
                .orElse(usedQuantity);
        BigDecimal conversion = BigDecimal.ONE;
        String unit = product.getStringField(ProductFields.UNIT);
        String givenUnit = trackingOperationProductInComponent.getStringField(TrackingOperationProductOutComponentFields.GIVEN_UNIT);

        if (Objects.nonNull(usedQuantity)) {
            PossibleUnitConversions unitConversions = unitConversionService.getPossibleConversions(unit,
                    searchCriteriaBuilder -> searchCriteriaBuilder
                            .add(SearchRestrictions.belongsTo(UnitConversionItemFieldsB.PRODUCT, product)));

            if (unitConversions.isDefinedFor(givenUnit)) {
                conversion = numberService.setScaleWithDefaultMathContext(unitConversions.asUnitToConversionMap().get(givenUnit));
            }
        }

        position.setField(PositionFields.GIVEN_UNIT,
                trackingOperationProductInComponent.getStringField(TrackingOperationProductInComponentFields.GIVEN_UNIT));
        position.setField(PositionFields.PRODUCT, product);
        position.setField(PositionFields.QUANTITY, usedQuantity);
        position.setField(PositionFields.GIVEN_QUANTITY, givenQuantity);
        position.setField(PositionFields.CONVERSION, conversion);
        position.setField(PositionFields.BATCH, usedBatch.getBelongsToField(UsedBatchFields.BATCH).getId());

        return position;
    }

    private Entity preparePositionForInProduct(final DataDefinition positionDD, final Entity trackingOperationProductInComponent,
                                               final Entity product) {
        Entity position = positionDD.create();

        BigDecimal usedQuantity = trackingOperationProductInComponent.getDecimalField(TrackingOperationProductInComponentFields.USED_QUANTITY);
        BigDecimal givenQuantity = trackingOperationProductInComponent.getDecimalField(TrackingOperationProductInComponentFields.GIVEN_QUANTITY);
        BigDecimal conversion = BigDecimal.ONE;
        String unit = product.getStringField(ProductFields.UNIT);
        String givenUnit = trackingOperationProductInComponent.getStringField(TrackingOperationProductOutComponentFields.GIVEN_UNIT);

        if (Objects.nonNull(usedQuantity) && Objects.nonNull(givenQuantity)) {
            PossibleUnitConversions unitConversions = unitConversionService.getPossibleConversions(unit,
                    searchCriteriaBuilder -> searchCriteriaBuilder
                            .add(SearchRestrictions.belongsTo(UnitConversionItemFieldsB.PRODUCT, product)));

            if (unitConversions.isDefinedFor(givenUnit)) {
                conversion = numberService.setScaleWithDefaultMathContext(unitConversions.asUnitToConversionMap().get(givenUnit));
            }
        }

        position.setField(PositionFields.GIVEN_UNIT,
                trackingOperationProductInComponent.getStringField(TrackingOperationProductInComponentFields.GIVEN_UNIT));
        position.setField(PositionFields.PRODUCT, product);
        position.setField(PositionFields.QUANTITY, usedQuantity);
        position.setField(PositionFields.GIVEN_QUANTITY, givenQuantity);
        position.setField(PositionFields.CONVERSION, conversion);

        return position;
    }

    private Entity createOrUpdateInternalInboundDocumentForFinalProducts(final Entity locationTo, final Entity order,
                                                                         final Collection<Entity> trackingOperationProductOutComponents, final Entity user) {
        String receiptOfProducts = parameterService.getParameter().getStringField(ParameterFieldsPC.RECEIPT_OF_PRODUCTS);

        List<Entity> finalProductRecord = null;

        Collection<Entity> intermediateRecords = Lists.newArrayList();

        for (Entity trackingOperationProductOutComponent : trackingOperationProductOutComponents) {
            if (isFinalProductForOrder(order,
                    trackingOperationProductOutComponent.getBelongsToField(TrackingOperationProductOutComponentFields.PRODUCT))) {
                finalProductRecord = Lists.newArrayList(trackingOperationProductOutComponent);
            } else {
                intermediateRecords.add(trackingOperationProductOutComponent);
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
                            finalProductRecord, true, false);
                } else {
                    return updateInternalInboundDocumentForFinalProducts(order, existingInboundDocument,
                            intermediateRecords, false, false);
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

            Optional<Entity> optionalEntity = intermediateRecords.stream().findFirst();

            if (optionalEntity.isPresent()) {
                Entity trackingOperationProductOutComponent = optionalEntity.get();
                Entity productionTracking = trackingOperationProductOutComponent.getBelongsToField(TrackingOperationProductOutComponentFields.PRODUCTION_TRACKING);

                if (notCreateDocumentsForIntermediateRecords(productionTracking)) {
                    return null;
                }
            }

            return createInternalInboundDocumentForFinalProducts(locationTo, order, intermediateRecords, user);
        } else {
            return null;
        }
    }

    public Entity updateInternalInboundDocumentForFinalProducts(final Entity order, final Entity existingInboundDocument,
                                                                final Collection<Entity> trackingOperationProductOutComponents,
                                                                boolean isFinalProduct, boolean cleanPositionsQuantity) {
        DataDefinition positionDD = getPositionDD();

        List<Entity> positions = Lists.newArrayList(existingInboundDocument.getHasManyField(DocumentFields.POSITIONS));

        String priceBasedOn = parameterService.getParameter().getStringField(ParameterFieldsPC.PRICE_BASED_ON);

        boolean isNominalProductCost = Objects.nonNull(priceBasedOn)
                && PriceBasedOn.NOMINAL_PRODUCT_COST.getStringValue().equals(priceBasedOn)
                || !Strings.isNullOrEmpty(order.getStringField(OrderFields.ADDITIONAL_FINAL_PRODUCTS));

        if (cleanPositionsQuantity) {
            positions.forEach(position -> {
                position.setField(PositionFields.QUANTITY, BigDecimal.ZERO);
                position.setField(PositionFields.GIVEN_QUANTITY, BigDecimal.ZERO);
            });
        }

        for (Entity trackingOperationProductOutComponent : trackingOperationProductOutComponents) {
            Entity product = trackingOperationProductOutComponent.getBelongsToField(TrackingOperationProductOutComponentFields.PRODUCT);
            Entity productionTracking = trackingOperationProductOutComponent.getBelongsToField(TrackingOperationProductOutComponentFields.PRODUCTION_TRACKING);
            Entity batch = productionTracking.getBelongsToField(ProductionTrackingFields.BATCH);
            Entity storageLocation = trackingOperationProductOutComponent.getBelongsToField(TrackingOperationProductOutComponentFields.STORAGE_LOCATION);
            Entity palletNumber = trackingOperationProductOutComponent.getBelongsToField(TrackingOperationProductOutComponentFields.PALLET_NUMBER);
            Date expirationDate = productionTracking.getDateField(ProductionTrackingFields.EXPIRATION_DATE);

            Optional<BigDecimal> usedQuantity = Optional
                    .ofNullable(trackingOperationProductOutComponent.getDecimalField(TrackingOperationProductInComponentFields.USED_QUANTITY));
            Optional<BigDecimal> givenQuantity = Optional
                    .ofNullable(trackingOperationProductOutComponent.getDecimalField(TrackingOperationProductInComponentFields.GIVEN_QUANTITY));
            Optional<String> givenUnit = Optional
                    .ofNullable(trackingOperationProductOutComponent.getStringField(TrackingOperationProductInComponentFields.GIVEN_UNIT));

            BigDecimal price;

            if (isFinalProduct) {
                if (isNominalProductCost) {
                    price = getNominalCost(product);
                } else {
                    price = realProductionCostService.calculateRealProductionCost(order);
                }
            } else {
                price = getNominalCost(product);
                batch = null;
            }

            Entity existingPosition = filterPosition(positions, product, givenUnit.orElse(null), batch, storageLocation, palletNumber);

            if (Objects.nonNull(existingPosition)) {
                Optional<BigDecimal> quantity = Optional.ofNullable(existingPosition.getDecimalField(PositionFields.QUANTITY));
                Optional<BigDecimal> givenQuantityFromPosition = Optional.ofNullable(existingPosition.getDecimalField(PositionFields.GIVEN_QUANTITY));

                existingPosition.setField(PositionFields.QUANTITY,
                        quantity.orElse(BigDecimal.ZERO).add(usedQuantity.orElse(BigDecimal.ZERO)));

                if (givenQuantity.isPresent()) {
                    existingPosition.setField(PositionFields.GIVEN_QUANTITY,
                            givenQuantity.orElse(BigDecimal.ZERO).add(givenQuantityFromPosition.orElse(BigDecimal.ZERO)));
                }

                existingPosition.setField(PositionFields.GIVEN_UNIT, givenUnit.orElse(null));

                fillAttributes(trackingOperationProductOutComponent, existingPosition);
            } else {
                Entity position = positionDD.create();

                BigDecimal conversion = BigDecimal.ONE;
                String unit = product.getStringField(ProductFields.UNIT);

                if (givenQuantity.isPresent()) {
                    PossibleUnitConversions unitConversions = unitConversionService.getPossibleConversions(unit,
                            searchCriteriaBuilder -> searchCriteriaBuilder
                                    .add(SearchRestrictions.belongsTo(UnitConversionItemFieldsB.PRODUCT, product)));

                    if (givenUnit.isPresent() && unitConversions.isDefinedFor(givenUnit.get())) {
                        conversion = numberService
                                .setScaleWithDefaultMathContext(unitConversions.asUnitToConversionMap().get(givenUnit.get()));
                    }

                    position.setField(PositionFields.GIVEN_QUANTITY, givenQuantity.get());
                }

                position.setField(PositionFields.PRODUCT, product);
                position.setField(PositionFields.QUANTITY, usedQuantity.orElse(null));
                position.setField(PositionFields.CONVERSION, conversion);
                position.setField(PositionFields.GIVEN_UNIT, givenUnit.orElse(null));
                position.setField(PositionFields.PRICE, price);
                position.setField(PositionFields.PRODUCTION_DATE, new Date());

                batch = productionTracking.getBelongsToField(ProductionTrackingFields.BATCH);

                if (Objects.nonNull(batch) && batch.getBelongsToField(BatchFields.PRODUCT).getId().equals(product.getId())) {
                    position.setField(PositionFields.BATCH, productionTracking.getBelongsToField(ProductionTrackingFields.BATCH).getId());
                    position.setField(PositionFields.QUALITY_RATING, productionTracking.getStringField(L_QUALITY_RATING));
                } else if (order.getBelongsToField(OrderFields.PRODUCT).getId().equals(product.getId())) {
                    position.setField(PositionFields.QUALITY_RATING, productionTracking.getStringField(L_QUALITY_RATING));
                }

                if (Objects.nonNull(storageLocation)) {
                    position.setField(PositionFields.STORAGE_LOCATION, storageLocation.getId());
                }

                if (Objects.nonNull(palletNumber)) {
                    position.setField(PositionFields.PALLET_NUMBER, palletNumber.getId());
                }

                if (order.getBelongsToField(OrderFields.PRODUCT).getId().equals(product.getId())) {
                    position.setField(PositionFields.EXPIRATION_DATE, expirationDate);
                }

                fillAttributes(trackingOperationProductOutComponent, position);

                positions.add(position);
            }
        }

        existingInboundDocument.setField(DocumentFields.POSITIONS, positions);

        return existingInboundDocument.getDataDefinition().save(existingInboundDocument);
    }

    private void fillAttributes(final Entity trackingOperationProductOutComponent, final Entity position) {
        List<Entity> positionAttributeValues = Lists.newArrayList();

        trackingOperationProductOutComponent.getHasManyField(TrackingOperationProductOutComponentFields.PROD_OUT_RESOURCE_ATTR_VALS).forEach(aVal -> {
            Entity positionAttributeValue = getPositionAttributeValueDD().create();

            positionAttributeValue.setField(PositionAttributeValueFields.ATTRIBUTE,
                    aVal.getBelongsToField(ProdOutResourceAttrValFields.ATTRIBUTE).getId());

            if (Objects.nonNull(aVal.getBelongsToField(PositionAttributeValueFields.ATTRIBUTE_VALUE))) {
                positionAttributeValue.setField(PositionAttributeValueFields.ATTRIBUTE_VALUE,
                        aVal.getBelongsToField(ProdOutResourceAttrValFields.ATTRIBUTE_VALUE).getId());
            }

            positionAttributeValue.setField(PositionAttributeValueFields.VALUE,
                    aVal.getStringField(ProdOutResourceAttrValFields.VALUE));

            positionAttributeValues.add(positionAttributeValue);
        });

        position.setField(PositionFields.POSITION_ATTRIBUTE_VALUES, positionAttributeValues);
    }

    private Entity filterPosition(final List<Entity> positions, final Entity product, final String givenUnit,
                                  final Entity batch, final Entity storageLocation, Entity palletNumber) {
        for (Entity position : positions) {
            if (checkPositionConditions(position, product, givenUnit, batch, storageLocation, palletNumber)) {
                return position;
            }
        }

        return null;
    }

    private boolean checkPositionConditions(final Entity position, final Entity product, final String givenUnit,
                                            final Entity batch, final Entity storageLocation, final Entity palletNumber) {
        Entity positionProduct = position.getBelongsToField(PositionFields.PRODUCT);
        String positionGivenUnit = position.getStringField(PositionFields.GIVEN_UNIT);
        Entity positionBatch = position.getBelongsToField(PositionFields.BATCH);
        Entity positionStorageLocation = position.getBelongsToField(PositionFields.STORAGE_LOCATION);
        Entity positionPalletNumber = position.getBelongsToField(PositionFields.PALLET_NUMBER);

        boolean isPosition = true;

        if (!positionProduct.getId().equals(product.getId())) {
            isPosition = false;
        }

        if (StringUtils.isNoneBlank(givenUnit) && StringUtils.isNoneBlank(positionGivenUnit)) {
            if (!positionGivenUnit.equals(givenUnit)) {
                isPosition = false;
            }
        }

        if (Objects.nonNull(batch) && Objects.nonNull(positionBatch)) {
            if (!positionBatch.getId().equals(batch.getId())) {
                isPosition = false;
            }
        } else if ((Objects.isNull(batch) && Objects.nonNull(positionBatch))
                || (Objects.nonNull(batch) && Objects.isNull(positionBatch))) {
            isPosition = false;
        }

        if (Objects.nonNull(storageLocation) && Objects.nonNull(positionStorageLocation)) {
            if (!positionStorageLocation.getId().equals(storageLocation.getId())) {
                isPosition = false;
            }
        } else if ((Objects.isNull(storageLocation) && Objects.nonNull(positionStorageLocation))
                || (Objects.nonNull(storageLocation) && Objects.isNull(positionStorageLocation))) {
            isPosition = false;
        }

        if (Objects.nonNull(palletNumber) && Objects.nonNull(positionPalletNumber)) {
            if (!positionPalletNumber.getId().equals(palletNumber.getId())) {
                isPosition = false;
            }
        } else if ((Objects.isNull(palletNumber) && Objects.nonNull(positionPalletNumber))
                || (Objects.nonNull(palletNumber) && Objects.isNull(positionPalletNumber))) {
            isPosition = false;
        }

        return isPosition;
    }

    public Entity createInternalInboundDocumentForFinalProducts(final Entity locationTo, final Entity order,
                                                                final Collection<Entity> trackingOperationProductOutComponents, Entity user) {
        String priceBasedOn = parameterService.getParameter().getStringField(ParameterFieldsPC.PRICE_BASED_ON);

        boolean isNominalProductCost = Objects.nonNull(priceBasedOn)
                && priceBasedOn.equals(PriceBasedOn.NOMINAL_PRODUCT_COST.getStringValue())
                || !Strings.isNullOrEmpty(order.getStringField(OrderFields.ADDITIONAL_FINAL_PRODUCTS));


        return createInternalInboundDocumentForFinalProducts(locationTo, order, trackingOperationProductOutComponents, isNominalProductCost, user);
    }

    private Entity createInternalInboundDocumentForFinalProducts(final Entity locationTo, final Entity order,
                                                                 final Collection<Entity> trackingOperationProductOutComponents, final boolean isBasedOnNominalCost, Entity user) {
        DocumentBuilder internalInboundBuilder = documentManagementService.getDocumentBuilder(user);

        internalInboundBuilder.internalInbound(locationTo);

        boolean isFinalProduct = false;

        Entity productionTracking = null;

        for (Entity trackingOperationProductOutComponent : trackingOperationProductOutComponents) {
            Entity product = trackingOperationProductOutComponent.getBelongsToField(TrackingOperationProductOutComponentFields.PRODUCT);

            if (Objects.isNull(productionTracking)) {
                productionTracking = trackingOperationProductOutComponent
                        .getBelongsToField(TrackingOperationProductOutComponentFields.PRODUCTION_TRACKING);
            }

            if (isFinalProductForOrder(order, product)) {
                isFinalProduct = true;
            }

            Entity position = getPositionDD().create();

            BigDecimal usedQuantity = trackingOperationProductOutComponent.getDecimalField(TrackingOperationProductOutComponentFields.USED_QUANTITY);
            BigDecimal givenQuantity = trackingOperationProductOutComponent.getDecimalField(TrackingOperationProductOutComponentFields.GIVEN_QUANTITY);
            BigDecimal conversion = BigDecimal.ONE;
            String unit = product.getStringField(ProductFields.UNIT);
            String givenUnit = trackingOperationProductOutComponent.getStringField(TrackingOperationProductOutComponentFields.GIVEN_UNIT);
            Entity batch = productionTracking.getBelongsToField(ProductionTrackingFields.BATCH);
            Entity storageLocation = trackingOperationProductOutComponent.getBelongsToField(TrackingOperationProductOutComponentFields.STORAGE_LOCATION);
            Entity palletNumber = trackingOperationProductOutComponent.getBelongsToField(TrackingOperationProductOutComponentFields.PALLET_NUMBER);
            Date expirationDate = productionTracking.getDateField(ProductionTrackingFields.EXPIRATION_DATE);

            if (Objects.nonNull(usedQuantity) && Objects.nonNull(givenQuantity)) {
                PossibleUnitConversions unitConversions = unitConversionService.getPossibleConversions(unit,
                        searchCriteriaBuilder -> searchCriteriaBuilder
                                .add(SearchRestrictions.belongsTo(UnitConversionItemFieldsB.PRODUCT, product)));

                if (unitConversions.isDefinedFor(givenUnit)) {
                    conversion = numberService
                            .setScaleWithDefaultMathContext(unitConversions.asUnitToConversionMap().get(givenUnit));
                }
            }

            BigDecimal price;

            if (isBasedOnNominalCost) {
                price = getNominalCost(product);
            } else {
                price = realProductionCostService.calculateRealProductionCost(order);
            }

            position.setField(PositionFields.PRODUCT, product);
            position.setField(PositionFields.QUANTITY, usedQuantity);
            position.setField(PositionFields.CONVERSION, conversion);
            position.setField(PositionFields.GIVEN_QUANTITY, givenQuantity);
            position.setField(PositionFields.GIVEN_UNIT, givenUnit);
            position.setField(PositionFields.PRICE, price);
            position.setField(PositionFields.PRODUCTION_DATE, new Date());

            if (Objects.nonNull(batch) && batch.getBelongsToField(BatchFields.PRODUCT).getId().equals(product.getId())) {
                position.setField(PositionFields.BATCH, productionTracking.getBelongsToField(ProductionTrackingFields.BATCH).getId());
                position.setField(PositionFields.QUALITY_RATING, productionTracking.getStringField(L_QUALITY_RATING));
            } else if (order.getBelongsToField(OrderFields.PRODUCT).getId().equals(product.getId())) {
                position.setField(PositionFields.QUALITY_RATING, productionTracking.getStringField(L_QUALITY_RATING));
            }

            if (Objects.nonNull(storageLocation)) {
                position.setField(PositionFields.STORAGE_LOCATION, storageLocation.getId());
            }

            if (Objects.nonNull(palletNumber)) {
                position.setField(PositionFields.PALLET_NUMBER, palletNumber.getId());
            }

            if (order.getBelongsToField(OrderFields.PRODUCT).getId().equals(product.getId())) {
                position.setField(PositionFields.EXPIRATION_DATE, expirationDate);
            }

            fillAttributes(trackingOperationProductOutComponent, position);

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

    private BigDecimal getNominalCost(final Entity product) {
        BigDecimal nominalCost = BigDecimalUtils.convertNullToZero(product.getDecimalField("nominalCost"));
        Entity currency = product.getBelongsToField("nominalCostCurrency");

        if (Objects.nonNull(currency) && CurrencyService.PLN.equals(currencyService.getCurrencyAlphabeticCode())
                && !CurrencyService.PLN.equals(currency.getStringField(CurrencyFields.ALPHABETIC_CODE))) {
            nominalCost = currencyService.getConvertedValue(nominalCost, currency);
        }

        return nominalCost;
    }

    private boolean isFinalProductForOrder(final Entity order, final Entity product) {
        return order.getBelongsToField(OrderFields.PRODUCT).getId().equals(product.getId());
    }

    public void updateCostsForOrder(final Entity order) {
        SearchQueryBuilder searchQueryBuilder = getPositionDD()
                .find("SELECT prod.id AS product, SUM(position.quantity) AS quantity, SUM(position.quantity * position.price) AS price "
                        + "FROM #materialFlowResources_position position "
                        + "JOIN position.document AS document "
                        + "JOIN position.product AS prod "
                        + "WHERE document.order = :order_id AND document.type = :type "
                        + "GROUP BY document.order, document.type, prod.id");

        searchQueryBuilder.setLong("order_id", order.getId());
        searchQueryBuilder.setString("type", DocumentType.INTERNAL_OUTBOUND.getStringValue());

        List<Entity> positions = searchQueryBuilder.list().getEntities();

        if (!positions.isEmpty()) {
            List<ProductWithQuantityAndCost> productsWithQuantitiesAndCosts = Lists.newArrayList();

            for (Entity position : positions) {
                Long product = (Long) position.getField(PositionFields.PRODUCT);
                BigDecimal quantity = position.getDecimalField(PositionFields.QUANTITY);
                BigDecimal cost = position.getDecimalField(PositionFields.PRICE);

                productsWithQuantitiesAndCosts.add(new ProductWithQuantityAndCost(product, quantity, cost));
            }

            List<Entity> technologyInstOperProductInComps = costNormsForMaterialsService.updateCostsForProductInOrder(order,
                    productsWithQuantitiesAndCosts);

            order.setField(OrderFieldsCNFM.TECHNOLOGY_INST_OPER_PRODUCT_IN_COMPS, technologyInstOperProductInComps);
        }
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

    private DataDefinition getProductionCountingQuantityDD() {
        return dataDefinitionService.get(BasicProductionCountingConstants.PLUGIN_IDENTIFIER, BasicProductionCountingConstants.MODEL_PRODUCTION_COUNTING_QUANTITY);
    }

    private DataDefinition getPositionAttributeValueDD() {
        return dataDefinitionService.get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER,
                MaterialFlowResourcesConstants.MODEL_POSITION_ATTRIBUTE_VALUE);
    }

}
