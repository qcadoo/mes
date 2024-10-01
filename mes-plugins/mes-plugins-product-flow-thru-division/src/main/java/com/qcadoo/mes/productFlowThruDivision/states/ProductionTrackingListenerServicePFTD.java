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

import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.qcadoo.mes.advancedGenealogy.constants.BatchFields;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.basic.constants.UnitConversionItemFieldsB;
import com.qcadoo.mes.materialFlow.constants.MaterialFlowConstants;
import com.qcadoo.mes.materialFlowResources.constants.*;
import com.qcadoo.mes.materialFlowResources.service.DocumentBuilder;
import com.qcadoo.mes.materialFlowResources.service.DocumentManagementService;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.states.constants.OrderState;
import com.qcadoo.mes.productFlowThruDivision.constants.DocumentFieldsPFTD;
import com.qcadoo.mes.productFlowThruDivision.constants.OrderFieldsPFTD;
import com.qcadoo.mes.productFlowThruDivision.constants.OrderProductResourceReservationFields;
import com.qcadoo.mes.productFlowThruDivision.constants.TrackingProductResourceReservationFields;
import com.qcadoo.mes.productFlowThruDivision.reservation.OrderReservationsService;
import com.qcadoo.mes.productFlowThruDivision.service.ProductionCountingDocumentService;
import com.qcadoo.mes.productionCounting.ProductionTrackingService;
import com.qcadoo.mes.productionCounting.constants.*;
import com.qcadoo.mes.productionCounting.states.constants.ProductionTrackingStateStringValues;
import com.qcadoo.mes.productionCounting.utils.OrderClosingHelper;
import com.qcadoo.mes.productionCounting.utils.ProductionTrackingDocumentsHelper;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.model.api.search.JoinType;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
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

import static com.qcadoo.mes.orders.constants.OrderFields.DONE_QUANTITY;

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
    private UnitConversionService unitConversionService;

    @Autowired
    private ParameterService parameterService;

    @Autowired
    private DocumentManagementService documentManagementService;

    @Autowired
    private OrderClosingHelper orderClosingHelper;

    @Autowired
    private ProductionTrackingDocumentsHelper productionTrackingDocumentsHelper;

    @Autowired
    private ProductionTrackingService productionTrackingService;

    @Autowired
    private OrderReservationsService orderReservationsService;

    @Autowired
    private ProductionCountingDocumentService productionCountingDocumentService;

    public Entity onAccept(final Entity productionTracking, final String sourceState) {
        boolean isCorrection = productionTracking.getBooleanField(ProductionTrackingFields.IS_CORRECTION);

        if (!ProductionTrackingStateStringValues.CORRECTED.equals(sourceState)) {
            if (isCorrection) {
                createWarehouseDocumentsForCorrection(productionTracking);
            } else {
                createWarehouseDocuments(productionTracking);
            }
        }

        return productionTracking;
    }

    public void createWarehouseDocuments(final Entity productionTracking) {
        Entity order = productionTracking.getBelongsToField(ProductionTrackingFields.ORDER);
        Entity technologyOperationComponent = productionTracking
                .getBelongsToField(ProductionTrackingFields.TECHNOLOGY_OPERATION_COMPONENT);
        List<Entity> trackingOperationProductOutComponents = productionTracking
                .getHasManyField(ProductionTrackingFields.TRACKING_OPERATION_PRODUCT_OUT_COMPONENTS);
        Multimap<Long, Entity> groupedRecordOutFinalProducts = productionTrackingDocumentsHelper
                .fillFromBPCProductOut(trackingOperationProductOutComponents, order, technologyOperationComponent, true, false, true);
        Multimap<Long, Entity> groupedRecordOutIntermediates = productionTrackingDocumentsHelper
                .fillFromBPCProductOut(trackingOperationProductOutComponents, order, technologyOperationComponent, false, true, false);
        List<Entity> trackingOperationProductInComponents = productionTracking
                .getHasManyField(ProductionTrackingFields.TRACKING_OPERATION_PRODUCT_IN_COMPONENTS);
        Multimap<Long, Entity> groupedRecordInProducts = productionTrackingDocumentsHelper
                .fillFromBPCProductIn(trackingOperationProductInComponents, order, technologyOperationComponent, true, true);
        Multimap<Long, Entity> groupedRecordInComponents = productionTrackingDocumentsHelper
                .fillFromBPCProductIn(trackingOperationProductInComponents, order, technologyOperationComponent, true, false);
        Multimap<Long, Entity> groupedRecordInIntermediates = productionTrackingDocumentsHelper
                .fillFromBPCProductIn(trackingOperationProductInComponents, order, technologyOperationComponent, false, true);

        Entity parameter = parameterService.getParameter();

        String receiptOfProducts = parameter.getStringField(ParameterFieldsPC.RECEIPT_OF_PRODUCTS);
        String releaseOfMaterials = parameter.getStringField(ParameterFieldsPC.RELEASE_OF_MATERIALS);

        if (TypeOfProductionRecording.CUMULATED.getStringValue().equals(
                order.getStringField(OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING))) {
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

        if (!groupedRecordInProducts.isEmpty() && (ReleaseOfMaterials.ON_ACCEPTANCE_REGISTRATION_RECORD.getStringValue()
                .equals(releaseOfMaterials) || ReleaseOfMaterials.END_OF_THE_ORDER.getStringValue()
                .equals(releaseOfMaterials) && orderClosingHelper.orderShouldBeClosedWithRecalculate(productionTracking)) &&
                !productionCountingDocumentService.checkIfProductsAvailableInStock(productionTracking, groupedRecordInProducts)) {
            return;
        }

        if ((Objects.isNull(order.getDecimalField(DONE_QUANTITY)) || (Objects.nonNull(order.getDecimalField(DONE_QUANTITY)) && order.getDecimalField(DONE_QUANTITY).compareTo(BigDecimal.ZERO) == 0))
                && trackingOperationProductOutComponents.stream().allMatch(p -> Objects.isNull(p.getDecimalField(TrackingOperationProductOutComponentFields.USED_QUANTITY)) || p.getDecimalField(TrackingOperationProductOutComponentFields.USED_QUANTITY).compareTo(BigDecimal.ZERO) == 0)
                && orderClosingHelper.orderShouldBeClosedWithRecalculate(productionTracking)) {
            productionTracking.addGlobalError("orders.order.orderStates.doneQuantityMustBeGreaterThanZero", false);
            productionTracking.addGlobalError("productionCounting.order.orderCannotBeClosed", false);

            return;
        }

        if (ReceiptOfProducts.ON_ACCEPTANCE_REGISTRATION_RECORD.getStringValue().equals(receiptOfProducts)
                || ReceiptOfProducts.END_OF_THE_ORDER.getStringValue().equals(receiptOfProducts)) {
            boolean errorsDisplayed = createOrUpdateInternalInboundDocumentForFinalProducts(productionTracking, groupedRecordOutFinalProducts, order, receiptOfProducts);

            if (errorsDisplayed) {
                return;
            }

            errorsDisplayed = createInternalInboundDocumentForIntermediates(productionTracking, groupedRecordOutIntermediates, order, receiptOfProducts);

            if (errorsDisplayed) {
                return;
            }

            TransactionAspectSupport.currentTransactionStatus().flush();
        }

        if (ReleaseOfMaterials.ON_ACCEPTANCE_REGISTRATION_RECORD.getStringValue()
                .equals(releaseOfMaterials)) {
            boolean errorsDisplayed = createInternalOutboundDocument(productionTracking, groupedRecordInComponents, order);

            if (errorsDisplayed) {
                return;
            }
            if (!groupedRecordInComponents.isEmpty()) {
                productionCountingDocumentService.updateCostsForOrder(order);
            }
        }

        if (ReleaseOfMaterials.ON_ACCEPTANCE_REGISTRATION_RECORD.getStringValue()
                .equals(releaseOfMaterials) || ReleaseOfMaterials.END_OF_THE_ORDER.getStringValue().equals(releaseOfMaterials)) {
            createInternalOutboundDocument(productionTracking, groupedRecordInIntermediates, order);
        }
    }

    private boolean createOrUpdateInternalInboundDocumentForFinalProducts(Entity productionTracking,
                                                                          Multimap<Long, Entity> groupedRecordOutProducts,
                                                                          Entity order, String receiptOfProducts) {
        boolean errorsDisplayed = false;
        for (Long locationId : groupedRecordOutProducts.keySet()) {
            Entity locationTo = getLocationDD().get(locationId);
            Entity inboundDocument;
            final Collection<Entity> trackingOperationProductOutComponents = groupedRecordOutProducts.get(locationId);
            final Entity user = productionTracking.getBelongsToField(L_USER);
            if (ReceiptOfProducts.ON_ACCEPTANCE_REGISTRATION_RECORD.getStringValue().equals(receiptOfProducts)) {
                inboundDocument = createInternalInboundDocumentForFinalProducts(locationTo, order,
                        trackingOperationProductOutComponents, true, user);
            } else if (ReceiptOfProducts.END_OF_THE_ORDER.getStringValue().equals(receiptOfProducts)) {
                Entity existingInboundDocument = getDocumentDD().find()
                        .add(SearchRestrictions.belongsTo(DocumentFieldsPFTD.ORDER, order))
                        .add(SearchRestrictions.belongsTo(DocumentFields.LOCATION_TO, locationTo))
                        .add(SearchRestrictions.eq(DocumentFields.STATE, DocumentState.DRAFT.getStringValue()))
                        .add(SearchRestrictions.eq(DocumentFields.TYPE, DocumentType.INTERNAL_INBOUND.getStringValue()))
                        .setMaxResults(1).uniqueResult();

                if (Objects.nonNull(existingInboundDocument)) {
                    inboundDocument = updateInternalInboundDocumentForFinalProducts(order, existingInboundDocument,
                            trackingOperationProductOutComponents);
                } else {
                    inboundDocument = createInternalInboundDocumentForFinalProducts(locationTo, order,
                            trackingOperationProductOutComponents, productionCountingDocumentService.isNominalProductCost(order), user);
                }
            } else {
                inboundDocument = null;
            }

            errorsDisplayed = isErrorsDisplayed(productionTracking, inboundDocument, errorsDisplayed);
        }
        return errorsDisplayed;
    }

    private boolean createInternalInboundDocumentForIntermediates(Entity productionTracking,
                                                                  Multimap<Long, Entity> groupedRecordOutProducts,
                                                                  Entity order, String receiptOfProducts) {
        boolean errorsDisplayed = false;
        for (Long locationId : groupedRecordOutProducts.keySet()) {
            Entity locationTo = getLocationDD().get(locationId);
            Entity inboundDocument;
            final Collection<Entity> trackingOperationProductOutComponents = groupedRecordOutProducts.get(locationId);
            final Entity user = productionTracking.getBelongsToField(L_USER);
            if (ReceiptOfProducts.ON_ACCEPTANCE_REGISTRATION_RECORD.getStringValue().equals(receiptOfProducts) ||
                    ReceiptOfProducts.END_OF_THE_ORDER.getStringValue().equals(receiptOfProducts)) {
                inboundDocument = createInternalInboundDocumentForFinalProducts(locationTo, order, trackingOperationProductOutComponents, true, user);
            } else {
                inboundDocument = null;
            }
            errorsDisplayed = isErrorsDisplayed(productionTracking, inboundDocument, errorsDisplayed);
        }
        return errorsDisplayed;
    }

    private boolean isErrorsDisplayed(Entity productionTracking, Entity inboundDocument,
                                      boolean errorsDisplayed) {
        if (Objects.nonNull(inboundDocument) && !inboundDocument.isValid()) {
            for (ErrorMessage error : inboundDocument.getGlobalErrors()) {
                productionTracking.addGlobalError(error.getMessage(), error.getVars());
            }

            if (!errorsDisplayed) {
                productionTracking.addGlobalError(
                        "productFlowThruDivision.productionTracking.productionTrackingError.createInternalInboundDocument");

                errorsDisplayed = true;
            }
        }
        return errorsDisplayed;
    }

    private boolean createInternalOutboundDocument(Entity productionTracking,
                                                   Multimap<Long, Entity> groupedRecordInProducts,
                                                   Entity order) {
        boolean errorsDisplayed = false;

        for (Long locationId : groupedRecordInProducts.keySet()) {
            Entity locationFrom = getLocationDD().get(locationId);
            Entity outboundDocument = createInternalOutboundDocumentForComponents(locationFrom, order,
                    groupedRecordInProducts.get(locationId), productionTracking.getBelongsToField(L_USER));

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
        return errorsDisplayed;
    }

    private void createWarehouseDocumentsForCorrection(Entity productionTracking) {
        Entity parameter = parameterService.getParameter();
        String receiptOfProducts = parameter.getStringField(ParameterFieldsPC.RECEIPT_OF_PRODUCTS);
        if (ReceiptOfProducts.END_OF_THE_ORDER.getStringValue().equals(receiptOfProducts)) {
            Entity order = productionTracking.getBelongsToField(ProductionTrackingFields.ORDER);
            Entity technologyOperationComponent = productionTracking
                    .getBelongsToField(ProductionTrackingFields.TECHNOLOGY_OPERATION_COMPONENT);
            List<Entity> trackingOperationProductOutComponents = productionTracking
                    .getHasManyField(ProductionTrackingFields.TRACKING_OPERATION_PRODUCT_OUT_COMPONENTS);
            Multimap<Long, Entity> groupedRecordOutFinalProducts = productionTrackingDocumentsHelper
                    .fillFromBPCProductOut(trackingOperationProductOutComponents, order, technologyOperationComponent, true, false, true);

            boolean errorsDisplayed = createOrUpdateInternalInboundDocumentForFinalProducts(productionTracking, groupedRecordOutFinalProducts, order, receiptOfProducts);

            if (errorsDisplayed) {
                return;
            }

            for (Entity document : order.getHasManyField(OrderFieldsPFTD.DOCUMENTS)) {
                if (DocumentType.INTERNAL_INBOUND.getStringValue().equals(document.getStringField(DocumentFields.TYPE))
                        && DocumentState.DRAFT.getStringValue().equals(document.getStringField(DocumentFields.STATE))) {
                    boolean warehouseFound = false;
                    for (Long locationId : groupedRecordOutFinalProducts.keySet()) {
                        if (locationId.equals(document.getBelongsToField(DocumentFields.LOCATION_TO).getId())) {
                            List<Entity> positions = document.getHasManyField(DocumentFields.POSITIONS);
                            for (Entity position : positions) {
                                boolean productFound = false;
                                for (Entity topoc : groupedRecordOutFinalProducts.get(locationId)) {
                                    if (position.getBelongsToField(PositionFields.PRODUCT).getId().equals(topoc.getBelongsToField(TrackingOperationProductOutComponentFields.PRODUCT).getId())) {
                                        productFound = true;
                                        break;
                                    }
                                }
                                if (!productFound) {
                                    getPositionDD().delete(position.getId());
                                }
                            }
                            warehouseFound = true;
                        }
                    }
                    if (!warehouseFound) {
                        getDocumentDD().delete(document.getId());
                    }
                }
            }

        }
    }

    public Entity createInternalOutboundDocumentForComponents(final Entity locationFrom, final Entity order,
                                                              final Collection<Entity> trackingOperationProductInComponents,
                                                              final Entity user) {
        DocumentBuilder internalOutboundBuilder = documentManagementService.getDocumentBuilder(user);

        internalOutboundBuilder.internalOutbound(locationFrom);

        HashSet<Entity> productsWithoutDuplicates = Sets.newHashSet();

        DataDefinition positionDD = getPositionDD();

        for (Entity trackingOperationProductInComponent : trackingOperationProductInComponents) {
            List<Entity> usedBatches = trackingOperationProductInComponent.getHasManyField(TrackingOperationProductInComponentFields.USED_BATCHES);
            List<Entity> resourceReservations = trackingOperationProductInComponent.getHasManyField("resourceReservations");

            Entity product = trackingOperationProductInComponent.getBelongsToField(TrackingOperationProductInComponentFields.PRODUCT);

            if (!productsWithoutDuplicates.contains(product)) {
                if (!resourceReservations.isEmpty()) {
                    for (Entity resourceReservation : resourceReservations) {
                        if (Objects.isNull(resourceReservation.getDecimalField(TrackingProductResourceReservationFields.USED_QUANTITY))) {
                            continue;
                        }

                        Entity position = preparePositionForResourceReservation(positionDD, trackingOperationProductInComponent, product, resourceReservation);

                        orderReservationsService.updateReservationOnDocumentCreation(resourceReservation);

                        internalOutboundBuilder.addPosition(position);
                    }

                    BigDecimal usedQuantity = trackingOperationProductInComponent.getDecimalField(TrackingOperationProductInComponentFields.USED_QUANTITY);

                    BigDecimal sumUsedResourceQuantity = resourceReservations.
                            stream()
                            .map(rr -> rr.getDecimalField(TrackingProductResourceReservationFields.USED_QUANTITY))
                            .filter(Objects::nonNull)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    BigDecimal rest = usedQuantity.subtract(sumUsedResourceQuantity, numberService.getMathContext());

                    if (rest.compareTo(BigDecimal.ZERO) > 0) {
                        Entity position = preparePositionForInProduct(positionDD, rest, trackingOperationProductInComponent, product);

                        internalOutboundBuilder.addPosition(position);
                    }
                } else if (!usedBatches.isEmpty()) {
                    for (Entity usedBatch : usedBatches) {
                        Entity position = preparePositionForUsedBatch(positionDD, trackingOperationProductInComponent, product, usedBatch);

                        internalOutboundBuilder.addPosition(position);
                    }
                } else {
                    Entity position = preparePositionForInProduct(positionDD, trackingOperationProductInComponent, product);

                    internalOutboundBuilder.addPosition(position);
                }
            }

            productsWithoutDuplicates.add(product);
        }

        internalOutboundBuilder.setField(DocumentFieldsPFTD.ORDER, order);

        return internalOutboundBuilder.setAccepted().buildWithEntityRuntimeException();
    }

    private Entity preparePositionForResourceReservation(final DataDefinition positionDD,
                                                         final Entity trackingOperationProductInComponent,
                                                         final Entity product,
                                                         final Entity trackingProductResourceReservation) {
        Entity orderProductResourceReservationBT = trackingProductResourceReservation.getBelongsToField(TrackingProductResourceReservationFields.ORDER_PRODUCT_RESOURCE_RESERVATION);
        Entity orderProductResourceReservation = orderProductResourceReservationBT.getDataDefinition().get(orderProductResourceReservationBT.getId());
        Entity resourceBT = orderProductResourceReservation.getBelongsToField(OrderProductResourceReservationFields.RESOURCE);
        Entity resource = resourceBT.getDataDefinition().get(resourceBT.getId());

        BigDecimal usedQuantity = trackingProductResourceReservation.getDecimalField(TrackingProductResourceReservationFields.USED_QUANTITY);
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

        Entity position = positionDD.create();

        position.setField(PositionFields.GIVEN_UNIT,
                trackingOperationProductInComponent.getStringField(TrackingOperationProductInComponentFields.GIVEN_UNIT));
        position.setField(PositionFields.PRODUCT, product);
        position.setField(PositionFields.QUANTITY, usedQuantity);
        position.setField(PositionFields.GIVEN_QUANTITY, givenQuantity);
        position.setField(PositionFields.CONVERSION, conversion);
        position.setField(PositionFields.RESOURCE, resource.getId());

        return position;
    }

    private Entity preparePositionForUsedBatch(final DataDefinition positionDD,
                                               final Entity trackingOperationProductInComponent,
                                               final Entity product, final Entity usedBatch) {
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

        Entity position = positionDD.create();

        position.setField(PositionFields.GIVEN_UNIT,
                trackingOperationProductInComponent.getStringField(TrackingOperationProductInComponentFields.GIVEN_UNIT));
        position.setField(PositionFields.PRODUCT, product);
        position.setField(PositionFields.QUANTITY, usedQuantity);
        position.setField(PositionFields.GIVEN_QUANTITY, givenQuantity);
        position.setField(PositionFields.CONVERSION, conversion);
        position.setField(PositionFields.BATCH, usedBatch.getBelongsToField(UsedBatchFields.BATCH).getId());

        return position;
    }

    private Entity preparePositionForInProduct(final DataDefinition positionDD,
                                               final Entity trackingOperationProductInComponent,
                                               final Entity product) {
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

        Entity position = positionDD.create();

        position.setField(PositionFields.GIVEN_UNIT,
                trackingOperationProductInComponent.getStringField(TrackingOperationProductInComponentFields.GIVEN_UNIT));
        position.setField(PositionFields.PRODUCT, product);
        position.setField(PositionFields.QUANTITY, usedQuantity);
        position.setField(PositionFields.GIVEN_QUANTITY, givenQuantity);
        position.setField(PositionFields.CONVERSION, conversion);

        return position;
    }

    private Entity preparePositionForInProduct(final DataDefinition positionDD, final BigDecimal usedQuantity,
                                               final Entity trackingOperationProductInComponent,
                                               final Entity product) {
        BigDecimal givenQuantity;
        BigDecimal conversion = BigDecimal.ONE;
        String givenUnit = trackingOperationProductInComponent.getStringField(TrackingOperationProductOutComponentFields.GIVEN_UNIT);
        String unit = product.getStringField(ProductFields.UNIT);

        PossibleUnitConversions unitConversions = unitConversionService.getPossibleConversions(unit,
                searchCriteriaBuilder -> searchCriteriaBuilder.add(SearchRestrictions.belongsTo(
                        UnitConversionItemFieldsB.PRODUCT, product)));

        if (unitConversions.isDefinedFor(givenUnit)) {
            givenQuantity = unitConversions.convertTo(usedQuantity, givenUnit, BigDecimal.ROUND_FLOOR);
            conversion = numberService.setScaleWithDefaultMathContext(unitConversions.asUnitToConversionMap().get(givenUnit));
        } else {
            givenQuantity = usedQuantity;
        }

        Entity position = positionDD.create();

        position.setField(PositionFields.GIVEN_UNIT,
                trackingOperationProductInComponent.getStringField(TrackingOperationProductInComponentFields.GIVEN_UNIT));
        position.setField(PositionFields.PRODUCT, product);
        position.setField(PositionFields.QUANTITY, usedQuantity);
        position.setField(PositionFields.GIVEN_QUANTITY, givenQuantity);
        position.setField(PositionFields.CONVERSION, conversion);

        return position;
    }

    private Entity updateInternalInboundDocumentForFinalProducts(final Entity order,
                                                                 final Entity existingInboundDocument,
                                                                 final Collection<Entity> trackingOperationProductOutComponents) {
        List<Entity> positions = Lists.newArrayList(existingInboundDocument.getHasManyField(DocumentFields.POSITIONS));

        boolean isNominalProductCost = productionCountingDocumentService.isNominalProductCost(order);

        for (Entity trackingOperationProductOutComponent : trackingOperationProductOutComponents) {
            Entity product = trackingOperationProductOutComponent.getBelongsToField(TrackingOperationProductOutComponentFields.PRODUCT);
            Entity productionTracking = trackingOperationProductOutComponent.getBelongsToField(TrackingOperationProductOutComponentFields.PRODUCTION_TRACKING);
            Entity storageLocation = trackingOperationProductOutComponent.getBelongsToField(TrackingOperationProductOutComponentFields.STORAGE_LOCATION);
            Entity palletNumber = trackingOperationProductOutComponent.getBelongsToField(TrackingOperationProductOutComponentFields.PALLET_NUMBER);
            String typeOfPallet = trackingOperationProductOutComponent.getStringField(TrackingOperationProductOutComponentFields.TYPE_OF_PALLET);
            Date expirationDate = productionTracking.getDateField(ProductionTrackingFields.EXPIRATION_DATE);

            Optional<BigDecimal> usedQuantity = Optional
                    .ofNullable(trackingOperationProductOutComponent.getDecimalField(TrackingOperationProductInComponentFields.USED_QUANTITY));
            Optional<BigDecimal> givenQuantity = Optional
                    .ofNullable(trackingOperationProductOutComponent.getDecimalField(TrackingOperationProductInComponentFields.GIVEN_QUANTITY));
            Optional<String> givenUnit = Optional
                    .ofNullable(trackingOperationProductOutComponent.getStringField(TrackingOperationProductInComponentFields.GIVEN_UNIT));

            BigDecimal price = productionCountingDocumentService.getPositionPrice(order, trackingOperationProductOutComponent, isNominalProductCost, product);
            Entity batch = null;
            if (isFinalProductForOrder(order, product)) {
                batch = productionTracking.getBelongsToField(ProductionTrackingFields.BATCH);
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
                Entity position = getPositionDD().create();

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

                if (Objects.nonNull(typeOfPallet)) {
                    position.setField(PositionFields.TYPE_OF_PALLET, typeOfPallet);
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

            if (Objects.nonNull(position.getId())) {
                for (Entity pav : position.getHasManyField(PositionFields.POSITION_ATTRIBUTE_VALUES)) {
                    getPositionAttributeValueDD().delete(pav.getId());
                }
            }

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
                                            final Entity batch, final Entity storageLocation,
                                            final Entity palletNumber) {
        Entity positionProduct = position.getBelongsToField(PositionFields.PRODUCT);
        String positionGivenUnit = position.getStringField(PositionFields.GIVEN_UNIT);
        Entity positionBatch = position.getBelongsToField(PositionFields.BATCH);
        Entity positionStorageLocation = position.getBelongsToField(PositionFields.STORAGE_LOCATION);
        Entity positionPalletNumber = position.getBelongsToField(PositionFields.PALLET_NUMBER);

        boolean isPosition = positionProduct.getId().equals(product.getId());

        if (StringUtils.isNoneBlank(givenUnit) && StringUtils.isNoneBlank(positionGivenUnit) && !positionGivenUnit.equals(givenUnit)) {
            isPosition = false;
        }

        if (Objects.nonNull(batch) && Objects.nonNull(positionBatch)) {
            if (!positionBatch.getId().equals(batch.getId())) {
                isPosition = false;
            }
        } else if (Objects.isNull(batch) && Objects.nonNull(positionBatch) || Objects.nonNull(batch)) {
            isPosition = false;
        }

        if (Objects.nonNull(storageLocation) && Objects.nonNull(positionStorageLocation)) {
            if (!positionStorageLocation.getId().equals(storageLocation.getId())) {
                isPosition = false;
            }
        } else if (Objects.isNull(storageLocation) && Objects.nonNull(positionStorageLocation) || Objects.nonNull(storageLocation)) {
            isPosition = false;
        }

        if (Objects.nonNull(palletNumber) && Objects.nonNull(positionPalletNumber)) {
            if (!positionPalletNumber.getId().equals(palletNumber.getId())) {
                isPosition = false;
            }
        } else if (Objects.isNull(palletNumber) && Objects.nonNull(positionPalletNumber) || Objects.nonNull(palletNumber)) {
            isPosition = false;
        }

        return isPosition;
    }

    private Entity createInternalInboundDocumentForFinalProducts(final Entity locationTo, final Entity order,
                                                                 final Collection<Entity> trackingOperationProductOutComponents,
                                                                 final boolean isBasedOnNominalCost,
                                                                 final Entity user) {
        DocumentBuilder internalInboundBuilder = documentManagementService.getDocumentBuilder(user);

        internalInboundBuilder.internalInbound(locationTo);

        boolean isFinalProduct = false;

        boolean isIntermediate = false;

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

            if (productionCountingDocumentService.isIntermediateForOrder(trackingOperationProductOutComponent)) {
                isIntermediate = true;
            }

            BigDecimal usedQuantity = trackingOperationProductOutComponent.getDecimalField(TrackingOperationProductOutComponentFields.USED_QUANTITY);
            BigDecimal givenQuantity = trackingOperationProductOutComponent.getDecimalField(TrackingOperationProductOutComponentFields.GIVEN_QUANTITY);
            BigDecimal conversion = BigDecimal.ONE;
            String unit = product.getStringField(ProductFields.UNIT);
            String givenUnit = trackingOperationProductOutComponent.getStringField(TrackingOperationProductOutComponentFields.GIVEN_UNIT);
            Entity batch = productionTracking.getBelongsToField(ProductionTrackingFields.BATCH);
            Entity storageLocation = trackingOperationProductOutComponent.getBelongsToField(TrackingOperationProductOutComponentFields.STORAGE_LOCATION);
            Entity palletNumber = trackingOperationProductOutComponent.getBelongsToField(TrackingOperationProductOutComponentFields.PALLET_NUMBER);
            String typeOfPallet = trackingOperationProductOutComponent.getStringField(TrackingOperationProductOutComponentFields.TYPE_OF_PALLET);
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

            BigDecimal price = productionCountingDocumentService.getPositionPrice(order, trackingOperationProductOutComponent, isBasedOnNominalCost, product);

            Entity position = getPositionDD().create();

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

            if (Objects.nonNull(typeOfPallet)) {
                position.setField(PositionFields.TYPE_OF_PALLET, typeOfPallet);
            }

            if (order.getBelongsToField(OrderFields.PRODUCT).getId().equals(product.getId())) {
                position.setField(PositionFields.EXPIRATION_DATE, expirationDate);
            }

            fillAttributes(trackingOperationProductOutComponent, position);

            internalInboundBuilder.addPosition(position);
        }

        internalInboundBuilder.setField(DocumentFieldsPFTD.ORDER, order);

        String receiptOfProducts = parameterService.getParameter().getStringField(ParameterFieldsPC.RECEIPT_OF_PRODUCTS);

        if (OrderState.COMPLETED.equals(OrderState.of(order)) || !isFinalProduct || isBasedOnNominalCost
                || orderClosingHelper.orderShouldBeClosedWithRecalculate(productionTracking)) {

            if (ReceiptOfProducts.ON_ACCEPTANCE_REGISTRATION_RECORD.getStringValue().equals(receiptOfProducts) || isIntermediate) {
                internalInboundBuilder.setAccepted();
            } else if (ReceiptOfProducts.END_OF_THE_ORDER.getStringValue().equals(receiptOfProducts) && isFinalProduct && orderClosingHelper.orderShouldBeClosedWithRecalculate(productionTracking)) {
                SearchCriteriaBuilder searchCriteriaBuilder = dataDefinitionService
                        .get(ProductionCountingConstants.PLUGIN_IDENTIFIER,
                                ProductionCountingConstants.MODEL_TRACKING_OPERATION_PRODUCT_OUT_COMPONENT)
                        .find()
                        .createAlias(TrackingOperationProductOutComponentFields.PRODUCTION_TRACKING, "pTracking", JoinType.INNER)
                        .add(SearchRestrictions.belongsTo("pTracking." + ProductionTrackingFields.ORDER, order))
                        .add(SearchRestrictions.in("pTracking." + ProductionTrackingFields.STATE, Lists.newArrayList(
                                ProductionTrackingStateStringValues.ACCEPTED)))
                        .add(SearchRestrictions.isNotNull(TrackingOperationProductOutComponentFields.USED_QUANTITY));

                Entity technologyOperationComponent = productionTracking.getBelongsToField(ProductionTrackingFields.TECHNOLOGY_OPERATION_COMPONENT);

                if (Objects.nonNull(technologyOperationComponent)) {
                    searchCriteriaBuilder.add(SearchRestrictions.belongsTo("pTracking." + ProductionTrackingFields.TECHNOLOGY_OPERATION_COMPONENT, technologyOperationComponent));
                }

                List<Entity> entities = searchCriteriaBuilder.list().getEntities();

                if (entities.isEmpty()) {
                    internalInboundBuilder.setAccepted();
                }
            }
        }

        return internalInboundBuilder.buildWithEntityRuntimeException();
    }

    private boolean isFinalProductForOrder(final Entity order, final Entity product) {
        return order.getBelongsToField(OrderFields.PRODUCT).getId().equals(product.getId());
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

    private DataDefinition getPositionAttributeValueDD() {
        return dataDefinitionService.get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER,
                MaterialFlowResourcesConstants.MODEL_POSITION_ATTRIBUTE_VALUE);
    }

}
