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

import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.qcadoo.mes.advancedGenealogy.constants.BatchFields;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.basic.constants.UnitConversionItemFieldsB;
import com.qcadoo.mes.materialFlow.constants.MaterialFlowConstants;
import com.qcadoo.mes.materialFlowResources.constants.MaterialFlowResourcesConstants;
import com.qcadoo.mes.materialFlowResources.constants.PositionFields;
import com.qcadoo.mes.materialFlowResources.service.DocumentBuilder;
import com.qcadoo.mes.materialFlowResources.service.DocumentManagementService;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.productFlowThruDivision.constants.DocumentFieldsPFTD;
import com.qcadoo.mes.productFlowThruDivision.constants.OrderProductResourceReservationFields;
import com.qcadoo.mes.productFlowThruDivision.constants.TrackingProductResourceReservationFields;
import com.qcadoo.mes.productFlowThruDivision.reservation.OrderReservationsService;
import com.qcadoo.mes.productFlowThruDivision.service.ProductionCountingDocumentService;
import com.qcadoo.mes.productionCounting.ProductionTrackingService;
import com.qcadoo.mes.productionCounting.constants.*;
import com.qcadoo.mes.productionCounting.states.constants.ProductionTrackingStateStringValues;
import com.qcadoo.mes.productionCounting.utils.ProductionTrackingDocumentsHelper;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.api.units.PossibleUnitConversions;
import com.qcadoo.model.api.units.UnitConversionService;
import com.qcadoo.model.api.validators.ErrorMessage;
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
    private ProductionTrackingDocumentsHelper productionTrackingDocumentsHelper;

    @Autowired
    private ProductionTrackingService productionTrackingService;

    @Autowired
    private OrderReservationsService orderReservationsService;

    @Autowired
    private ProductionCountingDocumentService productionCountingDocumentService;

    public Entity onAccept(final Entity productionTracking, final String sourceState) {
        boolean isCorrection = productionTracking.getBooleanField(ProductionTrackingFields.IS_CORRECTION);

        if (!ProductionTrackingStateStringValues.CORRECTED.equals(sourceState) && !isCorrection) {
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
        Multimap<Long, Entity> groupedRecordOutFinalProducts = productionTrackingDocumentsHelper
                .fillFromBPCProductOut(trackingOperationProductOutComponents, order, technologyOperationComponent, true, false, true);
        Multimap<Long, Entity> groupedRecordOutIntermediates = productionTrackingDocumentsHelper
                .fillFromBPCProductOut(trackingOperationProductOutComponents, order, technologyOperationComponent, false, true, false);
        List<Entity> trackingOperationProductInComponents = productionTracking
                .getHasManyField(ProductionTrackingFields.TRACKING_OPERATION_PRODUCT_IN_COMPONENTS);
        Multimap<Long, Entity> groupedRecordInComponents = productionTrackingDocumentsHelper
                .fillFromBPCProductIn(trackingOperationProductInComponents, order, technologyOperationComponent, true, false);
        Multimap<Long, Entity> groupedRecordInIntermediates = productionTrackingDocumentsHelper
                .fillFromBPCProductIn(trackingOperationProductInComponents, order, technologyOperationComponent, false, true);

        Entity parameter = parameterService.getParameter();

        String receiptOfProducts = parameter.getStringField(ParameterFieldsPC.RECEIPT_OF_PRODUCTS);
        String releaseOfMaterials = parameter.getStringField(ParameterFieldsPC.RELEASE_OF_MATERIALS);

        if (TypeOfProductionRecording.CUMULATED.getStringValue().equals(
                order.getStringField(OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING))) {
            List<Long> productIds = groupedRecordInComponents.values().stream().map(topic -> topic.getBelongsToField(TrackingOperationProductInComponentFields.PRODUCT).getId()).collect(Collectors.toList());
            Set<Long> duplicatedProducts = productIds.stream()
                    .filter(i -> Collections.frequency(productIds, i) > 1)
                    .collect(Collectors.toSet());

            if (!duplicatedProducts.isEmpty()) {
                productionTracking.addGlobalError(
                        "productFlowThruDivision.location.components.locationsAreDifferent");

                return;
            }
        }

        boolean releaseOnAcceptanceRegistrationRecord = ReleaseOfMaterials.ON_ACCEPTANCE_REGISTRATION_RECORD.getStringValue()
                .equals(releaseOfMaterials);
        boolean releaseOnEndOfTheOrder = ReleaseOfMaterials.END_OF_THE_ORDER.getStringValue()
                .equals(releaseOfMaterials);
        if (!groupedRecordInComponents.isEmpty() && releaseOnAcceptanceRegistrationRecord
                && !productionTrackingDocumentsHelper.checkIfProductsAvailableInStock(productionTracking, groupedRecordInComponents)) {
            return;
        }

        if (!groupedRecordInIntermediates.isEmpty() && (releaseOnAcceptanceRegistrationRecord || releaseOnEndOfTheOrder)
                && !productionTrackingDocumentsHelper.checkIfProductsAvailableInStock(productionTracking, groupedRecordInIntermediates)) {
            return;
        }

        if (releaseOnAcceptanceRegistrationRecord) {
            boolean errorsDisplayed = createInternalOutboundDocument(productionTracking, groupedRecordInComponents, order);

            if (errorsDisplayed) {
                return;
            }

            if (!groupedRecordInComponents.isEmpty()) {
                productionCountingDocumentService.updateCostsForOrder(order);
            }
        }

        if (releaseOnAcceptanceRegistrationRecord || releaseOnEndOfTheOrder) {
            boolean errorsDisplayed = createInternalOutboundDocument(productionTracking, groupedRecordInIntermediates, order);

            if (errorsDisplayed) {
                return;
            }

            TransactionAspectSupport.currentTransactionStatus().flush();
        }

        if (ReceiptOfProducts.ON_ACCEPTANCE_REGISTRATION_RECORD.getStringValue().equals(receiptOfProducts)) {
            boolean errorsDisplayed = createInternalInboundDocument(productionTracking, groupedRecordOutFinalProducts, order);

            if (errorsDisplayed) {
                return;
            }
        }

        if (ReceiptOfProducts.ON_ACCEPTANCE_REGISTRATION_RECORD.getStringValue().equals(receiptOfProducts)
                || ReceiptOfProducts.END_OF_THE_ORDER.getStringValue().equals(receiptOfProducts)) {
            createInternalInboundDocument(productionTracking, groupedRecordOutIntermediates, order);
        }
    }

    private boolean createInternalInboundDocument(Entity productionTracking,
                                                  Multimap<Long, Entity> groupedRecordOutProducts,
                                                  Entity order) {
        boolean errorsDisplayed = false;
        final Entity user = productionTracking.getBelongsToField(L_USER);
        for (Long locationId : groupedRecordOutProducts.keySet()) {
            Entity locationTo = getLocationDD().get(locationId);
            final Collection<Entity> trackingOperationProductOutComponents = groupedRecordOutProducts.get(locationId);
            Entity inboundDocument = createInternalInboundDocumentForFinalProducts(locationTo, order,
                    trackingOperationProductOutComponents, user);

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

    private Entity createInternalInboundDocumentForFinalProducts(final Entity locationTo, final Entity order,
                                                                 final Collection<Entity> trackingOperationProductOutComponents,
                                                                 final Entity user) {
        DocumentBuilder internalInboundBuilder = documentManagementService.getDocumentBuilder(user);

        internalInboundBuilder.internalInbound(locationTo);

        Entity productionTracking = null;

        for (Entity trackingOperationProductOutComponent : trackingOperationProductOutComponents) {
            Entity product = trackingOperationProductOutComponent.getBelongsToField(TrackingOperationProductOutComponentFields.PRODUCT);

            if (Objects.isNull(productionTracking)) {
                productionTracking = trackingOperationProductOutComponent
                        .getBelongsToField(TrackingOperationProductOutComponentFields.PRODUCTION_TRACKING);
            }

            BigDecimal usedQuantity = trackingOperationProductOutComponent.getDecimalField(TrackingOperationProductOutComponentFields.USED_QUANTITY);
            BigDecimal givenQuantity = trackingOperationProductOutComponent.getDecimalField(TrackingOperationProductOutComponentFields.GIVEN_QUANTITY);
            BigDecimal conversion = BigDecimal.ONE;
            String unit = product.getStringField(ProductFields.UNIT);
            String givenUnit = trackingOperationProductOutComponent.getStringField(TrackingOperationProductOutComponentFields.GIVEN_UNIT);
            Entity batch = productionTracking.getBelongsToField(ProductionTrackingFields.BATCH);
            Entity storageLocation = trackingOperationProductOutComponent.getBelongsToField(TrackingOperationProductOutComponentFields.STORAGE_LOCATION);
            Entity palletNumber = trackingOperationProductOutComponent.getBelongsToField(TrackingOperationProductOutComponentFields.PALLET_NUMBER);
            Entity typeOfLoadUnit = trackingOperationProductOutComponent.getBelongsToField(TrackingOperationProductOutComponentFields.TYPE_OF_LOAD_UNIT);
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

            BigDecimal price = productionCountingDocumentService.getPositionPrice(order, trackingOperationProductOutComponent, true, product);

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
                position.setField(PositionFields.QUALITY_RATING, productionTracking.getStringField(PositionFields.QUALITY_RATING));
            } else if (order.getBelongsToField(OrderFields.PRODUCT).getId().equals(product.getId())) {
                position.setField(PositionFields.QUALITY_RATING, productionTracking.getStringField(PositionFields.QUALITY_RATING));
            }

            if (Objects.nonNull(storageLocation)) {
                position.setField(PositionFields.STORAGE_LOCATION, storageLocation.getId());
            }

            if (Objects.nonNull(palletNumber)) {
                position.setField(PositionFields.PALLET_NUMBER, palletNumber.getId());
            }

            if (Objects.nonNull(typeOfLoadUnit)) {
                position.setField(PositionFields.TYPE_OF_LOAD_UNIT, typeOfLoadUnit);
            }

            if (order.getBelongsToField(OrderFields.PRODUCT).getId().equals(product.getId())) {
                position.setField(PositionFields.EXPIRATION_DATE, expirationDate);
            }
            position.setField(PositionFields.POSITION_ATTRIBUTE_VALUES, productionTrackingDocumentsHelper.getAttributeValues(trackingOperationProductOutComponent));

            internalInboundBuilder.addPosition(position);
        }

        internalInboundBuilder.setField(DocumentFieldsPFTD.ORDER, order);

        internalInboundBuilder.setAccepted();

        return internalInboundBuilder.buildWithEntityRuntimeException();
    }

    private DataDefinition getPositionDD() {
        return dataDefinitionService.get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER,
                MaterialFlowResourcesConstants.MODEL_POSITION);
    }

    private DataDefinition getLocationDD() {
        return dataDefinitionService.get(MaterialFlowConstants.PLUGIN_IDENTIFIER, MaterialFlowConstants.MODEL_LOCATION);
    }
}
