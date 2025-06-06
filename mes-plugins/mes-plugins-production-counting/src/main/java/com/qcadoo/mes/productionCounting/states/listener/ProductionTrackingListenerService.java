/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.4
 * <p>
 * This file is part of Qcadoo.
 * <p>
 * Qcadoo is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Affero General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option) any
 * later version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA
 * ***************************************************************************
 */
package com.qcadoo.mes.productionCounting.states.listener;

import com.google.common.collect.Lists;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.basic.constants.PalletNumberFields;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.basic.constants.TypeOfLoadUnitFields;
import com.qcadoo.mes.basicProductionCounting.BasicProductionCountingService;
import com.qcadoo.mes.basicProductionCounting.constants.*;
import com.qcadoo.mes.materialFlowResources.PalletValidatorService;
import com.qcadoo.mes.materialFlowResources.constants.StorageLocationFields;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.orders.states.aop.OrderStateChangeAspect;
import com.qcadoo.mes.orders.states.constants.OrderState;
import com.qcadoo.mes.productionCounting.ProductionCountingService;
import com.qcadoo.mes.productionCounting.ProductionTrackingService;
import com.qcadoo.mes.productionCounting.constants.*;
import com.qcadoo.mes.productionCounting.states.constants.ProductionTrackingStateStringValues;
import com.qcadoo.mes.productionCounting.utils.OrderClosingHelper;
import com.qcadoo.mes.states.StateChangeContext;
import com.qcadoo.mes.states.constants.StateChangeStatus;
import com.qcadoo.mes.states.service.StateChangeContextBuilder;
import com.qcadoo.model.api.BigDecimalUtils;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchProjections;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.api.validators.ErrorMessage;
import com.qcadoo.plugin.api.PluginManager;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.qcadoo.mes.basicProductionCounting.constants.BasicProductionCountingFields.ORDER;
import static com.qcadoo.mes.orders.constants.OrderFields.STATE;
import static com.qcadoo.mes.orders.states.constants.OrderState.COMPLETED;
import static com.qcadoo.mes.states.messages.util.MessagesUtil.getArgs;
import static com.qcadoo.mes.states.messages.util.MessagesUtil.getKey;
import static com.qcadoo.model.api.search.SearchOrders.asc;

@Service
public final class ProductionTrackingListenerService {

    private static final String L_PRODUCT = "product";

    private static final String L_COUNT = "count";
    public static final String L_RESOURCE_RESERVATIONS = "resourceReservations";
    public static final String L_ORDER_PRODUCT_RESOURCE_RESERVATION = "orderProductResourceReservation";
    public static final String L_USED_QUANTITY = "usedQuantity";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NumberService numberService;

    @Autowired
    private TranslationService translationService;

    @Autowired
    private ParameterService parameterService;

    @Autowired
    private ProductionCountingService productionCountingService;

    @Autowired
    private ProductionTrackingService productionTrackingService;

    @Autowired
    private BasicProductionCountingService basicProductionCountingService;

    @Autowired
    private PalletValidatorService palletValidatorService;

    @Autowired
    private OrderStateChangeAspect orderStateChangeAspect;

    @Autowired
    private OrderClosingHelper orderClosingHelper;

    @Autowired
    private StateChangeContextBuilder stateChangeContextBuilder;

    @Autowired
    private PluginManager pluginManager;

    public void onChangeFromDraftToAny(final Entity productionTracking) {
        productionTracking.setField(ProductionTrackingFields.LAST_STATE_CHANGE_FAILS, false);
        productionTracking.setField(ProductionTrackingFields.LAST_STATE_CHANGE_FAIL_CAUSE, null);
    }

    public void validationOnAccept(final Entity productionTracking, String sourceState) {
        checkIfProductionOrderIsValid(productionTracking);
        checkIfExistsFinalRecord(productionTracking);
        checkIfTimesIsSet(productionTracking);
        checkIfBatchEvidenceSet(productionTracking);
        checkIfStorageLocationsAndPalletNumbersAreSet(productionTracking, sourceState);
    }

    public void validationOnDecline(final Entity productionTracking) {
        if (parameterService.getParameter().getBooleanField(ParameterFieldsPC.JUST_ONE)) {
            productionTracking.addGlobalError("productionCounting.productionTracking.error.registrationRecordCouldNotBeRejectedForJustOne");
        }
    }

    private void checkIfProductionOrderIsValid(final Entity productionTracking) {
        Entity order = productionTracking.getBelongsToField(ProductionTrackingFields.ORDER);
        Entity validatedOrder = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).validate(order);

        if (!validatedOrder.isValid()) {
            productionTracking.addGlobalError("productionCounting.productionTracking.error.orderIsInvalid");
        }
    }

    private void checkIfBatchEvidenceSet(final Entity productionTracking) {
        Entity order = productionTracking.getBelongsToField(ProductionTrackingFields.ORDER);
        Entity product = order.getBelongsToField(OrderFields.PRODUCT);

        if (product.getBooleanField(ProductFields.BATCH_EVIDENCE)
                && Objects.isNull(productionTracking.getBelongsToField(ProductionTrackingFields.BATCH))
                && StringUtils.isEmpty(productionTracking.getStringField(ProductionTrackingFields.BATCH_NUMBER))) {
            productionTracking.addGlobalError("productionCounting.productionTracking.error.batchEvidenceRequiredForFinalProduct",
                    product.getStringField(ProductFields.NUMBER));
        }

        List<Entity> trackingOperationProductInComponents = productionTracking
                .getHasManyField(ProductionTrackingFields.TRACKING_OPERATION_PRODUCT_IN_COMPONENTS);

        trackingOperationProductInComponents.forEach(trackingOperationProductInComponent -> {
            Entity trackingOperationProductInComponentProduct = trackingOperationProductInComponent.getBelongsToField(TrackingOperationProductInComponentFields.PRODUCT);

            if (BigDecimalUtils.convertNullToZero(trackingOperationProductInComponent.getDecimalField(TrackingOperationProductInComponentFields.USED_QUANTITY))
                    .compareTo(BigDecimal.ZERO) > 0
                    && trackingOperationProductInComponentProduct.getBooleanField(ProductFields.BATCH_EVIDENCE)
                    && trackingOperationProductInComponent.getHasManyField(TrackingOperationProductInComponentFields.USED_BATCHES).isEmpty()) {
                productionTracking.addGlobalError("productionCounting.productionTracking.error.batchEvidenceRequiredForProduct",
                        trackingOperationProductInComponentProduct.getStringField(ProductFields.NUMBER));
            }
        });
    }

    private void checkIfStorageLocationsAndPalletNumbersAreSet(final Entity productionTracking, String sourceState) {
        if(!ProductionTrackingStateStringValues.CORRECTED.equals(sourceState)) {
            List<Entity> trackingOperationProductOutComponents = productionTracking
                    .getHasManyField(ProductionTrackingFields.TRACKING_OPERATION_PRODUCT_OUT_COMPONENTS);

            Entity parameter = parameterService.getParameter();

            String receiptOfProducts = parameter.getStringField(ParameterFieldsPC.RECEIPT_OF_PRODUCTS);
            Entity order = productionTracking.getBelongsToField(ProductionTrackingFields.ORDER);

            trackingOperationProductOutComponents.forEach(trackingOperationProductOutComponent -> {
                Entity storageLocation = trackingOperationProductOutComponent.getBelongsToField(TrackingOperationProductOutComponentFields.STORAGE_LOCATION);

                if (Objects.nonNull(storageLocation)) {
                    boolean placeStorageLocation = storageLocation.getBooleanField(StorageLocationFields.PLACE_STORAGE_LOCATION);
                    Entity palletNumber = trackingOperationProductOutComponent.getBelongsToField(TrackingOperationProductOutComponentFields.PALLET_NUMBER);
                    Entity location = storageLocation.getBelongsToField(StorageLocationFields.LOCATION);
                    String storageLocationNumber = storageLocation.getStringField(StorageLocationFields.NUMBER);
                    String palletNumberNumber = Objects.nonNull(palletNumber) ? palletNumber.getStringField(PalletNumberFields.NUMBER) : null;
                    Entity typeOfLoadUnit = trackingOperationProductOutComponent.getBelongsToField(TrackingOperationProductOutComponentFields.TYPE_OF_LOAD_UNIT);
                    String typeOfLoadUnitName = Objects.nonNull(typeOfLoadUnit) ? typeOfLoadUnit.getStringField(TypeOfLoadUnitFields.NAME) : null;
                    Entity product = trackingOperationProductOutComponent.getBelongsToField(TrackingOperationProductOutComponentFields.PRODUCT);

                    if (placeStorageLocation) {
                        if (Objects.isNull(palletNumber)) {
                            productionTracking.addGlobalError("productionCounting.productionTracking.error.trackingOperationOutComponent.palletNumberRequired",
                                    product.getStringField(ProductFields.NUMBER));
                        } else {
                            validateResources(productionTracking, location, palletNumberNumber, product, storageLocationNumber, typeOfLoadUnitName);

                            if (palletValidatorService.existsOtherTrackingOperationProductOutComponentForPalletNumber(storageLocationNumber, palletNumberNumber, typeOfLoadUnitName, trackingOperationProductOutComponent.getId(), order.getId(), productionTracking.getId(), receiptOfProducts)) {
                                productionTracking.addGlobalError("productionCounting.productionTracking.error.existsOtherTrackingOperationProductOutComponentForPalletAndStorageLocation",
                                        product.getStringField(ProductFields.NUMBER));
                            }

                            if (!palletValidatorService.notTooManyPalletsInStorageLocationAndProductionTracking(trackingOperationProductOutComponent, storageLocation, palletNumber, order.getId(), receiptOfProducts)) {
                                productionTracking.addGlobalError("productionCounting.productionTracking.error.trackingOperationOutComponent.morePalletsExists",
                                        product.getStringField(ProductFields.NUMBER));
                            }
                        }
                    } else if (!Objects.isNull(palletNumber)) {
                        validateResources(productionTracking, location, palletNumberNumber, product, storageLocationNumber, typeOfLoadUnitName);
                    }
                }

            });
        }
    }

    private void validateResources(Entity productionTracking, Entity location, String palletNumberNumber,
                                   Entity product,
                                   String storageLocationNumber, String typeOfLoadUnitName) {
        if (palletValidatorService.existsOtherResourceForPalletNumberOnOtherLocations(location.getId(), palletNumberNumber, null)) {
            productionTracking.addGlobalError("productionCounting.productionTracking.error.trackingOperationOutComponent.existsOtherResourceForPallet",
                    product.getStringField(ProductFields.NUMBER));
        } else if (palletValidatorService.existsOtherResourceForPalletNumberWithDifferentStorageLocation(location.getId(), storageLocationNumber,
                palletNumberNumber, null)) {
            productionTracking.addGlobalError("productionCounting.productionTracking.error.trackingOperationOutComponent.existsOtherResourceForPalletAndStorageLocation",
                    palletNumberNumber, product.getStringField(ProductFields.NUMBER));
        } else if (palletValidatorService.existsOtherResourceForPalletNumberWithDifferentType(location.getId(),
                palletNumberNumber, typeOfLoadUnitName, null)) {
            productionTracking.addGlobalError("productionCounting.productionTracking.error.trackingOperationOutComponent.existsOtherResourceForLoadUnitAndTypeOfLoadUnit",
                    palletNumberNumber, product.getStringField(ProductFields.NUMBER));
        }
    }

    public void onLeavingDraft(final Entity productionTracking) {
        productionTracking.setField(ProductionTrackingFields.LAST_STATE_CHANGE_FAILS, false);
        productionTracking.setField(ProductionTrackingFields.LAST_STATE_CHANGE_FAIL_CAUSE, null);

        if (parameterService.getParameter().getBooleanField(
                ParameterFieldsPC.CALCULATE_AMOUNT_TIME_EMPLOYEES_ON_ACCEPTANCE_RECORD)) {
            Integer laborTime = productionTracking.getHasManyField(ProductionTrackingFields.STAFF_WORK_TIMES).stream()
                    .mapToInt(staffWorkTime -> staffWorkTime.getIntegerField(StaffWorkTimeFields.LABOR_TIME)).sum();

            productionTracking.setField(ProductionTrackingFields.LABOR_TIME, laborTime);
        }
    }

    public void onAccept(final Entity productionTracking) {
        updateBasicProductionCounting(productionTracking, new Addition());
        updateProductionCountingQuantity(productionTracking, new Addition());
        setOrderDoneAndWastesQuantity(productionTracking, new Addition());
        closeOrder(productionTracking);
    }

    public void onChangeFromAcceptedToDeclined(final Entity productionTracking) {
        updateBasicProductionCounting(productionTracking, new Subtraction());
        setOrderDoneAndWastesQuantity(productionTracking, new Subtraction());
        fillOrderReportedQuantity(productionTracking, new Subtraction());
    }

    public boolean checkIfUsedQuantitiesWereNotFilled(final Entity productionTracking) {
        Entity order = productionTracking.getBelongsToField(ProductionTrackingFields.ORDER);

        final SearchCriteriaBuilder searchBuilder = productionTracking
                .getHasManyField(ProductionTrackingFields.TRACKING_OPERATION_PRODUCT_IN_COMPONENTS).find()
                .add(SearchRestrictions.isNotNull(TrackingOperationProductInComponentFields.USED_QUANTITY))
                .setProjection(SearchProjections.alias(SearchProjections.rowCount(), L_COUNT)).addOrder(asc(L_COUNT));

        return (order.getBooleanField(OrderFieldsPC.REGISTER_QUANTITY_IN_PRODUCT) && ((Long) searchBuilder.setMaxResults(1)
                .uniqueResult().getField(L_COUNT) <= 0));
    }

    public boolean checkIfUsedOrWastesQuantitiesWereNotFilled(final Entity productionTracking) {
        Entity order = productionTracking.getBelongsToField(ProductionTrackingFields.ORDER);

        final SearchCriteriaBuilder searchBuilder = productionTracking
                .getHasManyField(ProductionTrackingFields.TRACKING_OPERATION_PRODUCT_OUT_COMPONENTS)
                .find()
                .add(SearchRestrictions.or(
                        SearchRestrictions.isNotNull(TrackingOperationProductOutComponentFields.USED_QUANTITY),
                        SearchRestrictions.isNotNull(TrackingOperationProductOutComponentFields.WASTES_QUANTITY)))
                .setProjection(SearchProjections.alias(SearchProjections.rowCount(), L_COUNT)).addOrder(asc(L_COUNT));

        return (order.getBooleanField(OrderFieldsPC.REGISTER_QUANTITY_OUT_PRODUCT) && ((Long) searchBuilder.setMaxResults(1)
                .uniqueResult().getField(L_COUNT) <= 0));
    }

    private void checkIfExistsFinalRecord(final Entity productionTracking) {
        if (productionTracking.getBooleanField(ProductionTrackingFields.IS_CORRECTION)
                && !productionTracking.getBooleanField(ProductionTrackingFields.LAST_TRACKING)) {
            return;
        }

        final Entity order = productionTracking.getBelongsToField(ProductionTrackingFields.ORDER);
        final String typeOfProductionRecording = order.getStringField(OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING);

        SearchCriteriaBuilder searchBuilder = productionTracking.getDataDefinition().find();
        searchBuilder.add(SearchRestrictions.eq(ProductionTrackingFields.STATE, ProductionTrackingStateStringValues.ACCEPTED));
        searchBuilder.add(SearchRestrictions.belongsTo(ProductionTrackingFields.ORDER, order));
        searchBuilder.add(SearchRestrictions.eq(ProductionTrackingFields.LAST_TRACKING, true));

        if (productionCountingService.isTypeOfProductionRecordingForEach(typeOfProductionRecording)) {
            searchBuilder.add(SearchRestrictions.belongsTo(ProductionTrackingFields.TECHNOLOGY_OPERATION_COMPONENT,
                    productionTracking.getBelongsToField(ProductionTrackingFields.TECHNOLOGY_OPERATION_COMPONENT)));
        }

        if (searchBuilder.list().getTotalNumberOfEntities() != 0) {
            productionTracking.addGlobalError("productionCounting.productionTracking.messages.error.finalExists");
        }
    }

    private void closeOrder(final Entity productionTracking) {
        final Entity order = productionTracking.getBelongsToField(ORDER);

        Entity orderFromDB = order.getDataDefinition().get(order.getId());

        if (!orderClosingHelper.orderShouldBeClosed(productionTracking)) {
            return;
        }

        if (order.getStringField(STATE).equals(COMPLETED.getStringValue())) {
            productionTracking.addGlobalMessage("productionCounting.order.orderIsAlreadyClosed", false, false);

            return;
        }

        final StateChangeContext orderStateChangeContext = stateChangeContextBuilder.build(
                orderStateChangeAspect.getChangeEntityDescriber(), orderFromDB, OrderState.COMPLETED.getStringValue());
        orderStateChangeAspect.changeState(orderStateChangeContext);

        orderFromDB = order.getDataDefinition().get(orderStateChangeContext.getOwner().getId());

        if (orderFromDB.getStringField(STATE).equals(COMPLETED.getStringValue())) {
            productionTracking.addGlobalMessage("productionCounting.order.orderClosed", false, false);

        } else if (StateChangeStatus.PAUSED.equals(orderStateChangeContext.getStatus())) {
            productionTracking.addGlobalMessage("productionCounting.order.orderWillBeClosedAfterExtSync", false, false);
        } else {
            productionTracking.addGlobalMessage("productionCounting.order.orderCannotBeClosed", false, false);

            List<ErrorMessage> errors = Lists.newArrayList();

            if (!orderFromDB.getErrors().isEmpty()) {
                errors.addAll(order.getErrors().values());
            }
            if (!orderFromDB.getGlobalErrors().isEmpty()) {
                errors.addAll(order.getGlobalErrors());
            }

            if (!orderStateChangeContext.getAllMessages().isEmpty()) {
                for (Entity entity : orderStateChangeContext.getAllMessages()) {
                    errors.add(new ErrorMessage(getKey(entity), getArgs(entity)));
                }
            }

            if (!errors.isEmpty()) {
                StringBuilder errorMessages = new StringBuilder();

                for (ErrorMessage errorMessage : errors) {
                    String translatedErrorMessage = translationService.translate(errorMessage.getMessage(),
                            LocaleContextHolder.getLocale(), errorMessage.getVars());
                    errorMessages.append(translatedErrorMessage);
                    errorMessages.append(", ");
                }

                productionTracking.addGlobalMessage("orders.order.orderStates.error", errorMessages.toString());
            }
        }
    }

    public void updateOrderReportedQuantity(final Entity productionTracking) {
        Entity order = productionTracking.getBelongsToField(ProductionTrackingFields.ORDER);
        Entity technologyOperationComponent = productionTracking.getBelongsToField(ProductionTrackingFields.TECHNOLOGY_OPERATION_COMPONENT);
        Entity orderProduct = order.getBelongsToField(OrderFields.PRODUCT);

        Entity mainTrackingOperationProductOutComponent = productionTracking
                .getHasManyField(ProductionTrackingFields.TRACKING_OPERATION_PRODUCT_OUT_COMPONENTS).find()
                .add(SearchRestrictions.belongsTo(TrackingOperationProductOutComponentFields.PRODUCT, orderProduct))
                .setMaxResults(1).uniqueResult();

        List<Entity> trackingOperationProductOutComponents = productionTrackingService
                .findTrackingOperationProductOutComponents(order, technologyOperationComponent, orderProduct);

        boolean useTracking = productionTracking.getStringField(ProductionTrackingFields.STATE).equals(
                ProductionTrackingStateStringValues.DRAFT)
                || productionTracking.getStringField(ProductionTrackingFields.STATE).equals(
                ProductionTrackingStateStringValues.ACCEPTED);

        if (productionTracking.getBooleanField(ProductionTrackingFields.IS_CORRECTED)) {
            useTracking = false;
        }

        BigDecimal trackedQuantity = productionTrackingService.getTrackedQuantity(mainTrackingOperationProductOutComponent,
                trackingOperationProductOutComponents, useTracking);

        Entity orderDb = order.getDataDefinition().get(order.getId());

        orderDb.setField(OrderFields.REPORTED_PRODUCTION_QUANTITY, trackedQuantity);
        orderDb.getDataDefinition().fastSave(orderDb);
    }

    public void updateOrderReportedQuantityOnRemove(final Entity productionTracking) {
        fillOrderReportedQuantity(productionTracking, new Subtraction());
    }

    public void updateOrderReportedQuantityAfterRemoveCorrection(final Entity productionTracking) {
        fillOrderReportedQuantity(productionTracking, new Addition());
    }

    private void fillOrderReportedQuantity(final Entity productionTracking, final Operation operation) {
        Entity order = productionTracking.getBelongsToField(ProductionTrackingFields.ORDER);

        order = order.getDataDefinition().get(order.getId());

        Entity mainProduct = order.getBelongsToField(OrderFields.PRODUCT);
        Entity mainTrackingOperationProductOutComponent = productionTracking
                .getHasManyField(ProductionTrackingFields.TRACKING_OPERATION_PRODUCT_OUT_COMPONENTS).find()
                .add(SearchRestrictions.belongsTo(TrackingOperationProductOutComponentFields.PRODUCT, mainProduct))
                .setMaxResults(1).uniqueResult();

        if (Objects.nonNull(mainTrackingOperationProductOutComponent)) {
            BigDecimal trackedQuantity = mainTrackingOperationProductOutComponent
                    .getDecimalField(TrackingOperationProductOutComponentFields.USED_QUANTITY);
            BigDecimal reported = BigDecimalUtils.convertNullToZero(order
                    .getDecimalField(OrderFields.REPORTED_PRODUCTION_QUANTITY));
            BigDecimal newQuantity = operation.perform(reported, trackedQuantity);

            order.setField(OrderFields.REPORTED_PRODUCTION_QUANTITY, newQuantity);

            order.getDataDefinition().save(order);
        }
    }

    private void setOrderDoneAndWastesQuantity(final Entity productionTracking, final Operation operation) {
        Entity order = productionTracking.getBelongsToField(ProductionTrackingFields.ORDER);

        order = order.getDataDefinition().get(order.getId());

        Entity mainProduct = order.getBelongsToField(OrderFields.PRODUCT);
        Entity mainTrackingOperationProductOutComponent = productionTracking
                .getHasManyField(ProductionTrackingFields.TRACKING_OPERATION_PRODUCT_OUT_COMPONENTS).find()
                .add(SearchRestrictions.belongsTo(TrackingOperationProductOutComponentFields.PRODUCT, mainProduct))
                .setMaxResults(1).uniqueResult();

        if (Objects.nonNull(mainTrackingOperationProductOutComponent)) {
            order.setField(OrderFields.DONE_QUANTITY,
                    basicProductionCountingService.getProducedQuantityFromBasicProductionCountings(order));
            order.setField(OrderFields.WASTES_QUANTITY,
                    getWastesQuantity(mainTrackingOperationProductOutComponent, order, operation));
            order.setField(OrderFields.FINAL_PRODUCTION_TRACKING, productionTracking.getBooleanField(ProductionTrackingFields.LAST_TRACKING));

            order.getDataDefinition().save(order);
        }
    }

    private BigDecimal getWastesQuantity(final Entity mainTrackingOperationProductOutComponent, final Entity order,
                                         final Operation operation) {
        BigDecimal mainWastesQuantity = mainTrackingOperationProductOutComponent
                .getDecimalField(TrackingOperationProductOutComponentFields.WASTES_QUANTITY);
        BigDecimal orderWastesQuantity = order.getDecimalField(OrderFields.WASTES_QUANTITY);

        if (Objects.isNull(orderWastesQuantity)) {
            orderWastesQuantity = BigDecimal.ZERO;
        }

        if (Objects.nonNull(mainWastesQuantity)) {

            return operation.perform(orderWastesQuantity, mainWastesQuantity);
        }

        return orderWastesQuantity;
    }

    private void updateProductionCountingQuantitySubtraction(final Entity productionTracking,
                                                             final Operation operation) {
        final Entity order = productionTracking.getBelongsToField(ProductionTrackingFields.ORDER);
        String typeOfProductionRecording = order.getStringField(OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING);

        boolean isForEach = TypeOfProductionRecording.FOR_EACH.getStringValue().equals(typeOfProductionRecording);
        Entity technologyOperationComponent = productionTracking
                .getBelongsToField(ProductionTrackingFields.TECHNOLOGY_OPERATION_COMPONENT);
        final List<Entity> trackingOperationProductInComponents = productionTracking
                .getHasManyField(ProductionTrackingFields.TRACKING_OPERATION_PRODUCT_IN_COMPONENTS);
        final List<Entity> trackingOperationProductOutComponents = productionTracking
                .getHasManyField(ProductionTrackingFields.TRACKING_OPERATION_PRODUCT_OUT_COMPONENTS);

        trackingOperationProductInComponents.forEach(trackingOperationProductInComponent -> {
            List<Entity> productionCountingQuantities = getInProductionCountingQuantities(trackingOperationProductInComponent,
                    order, technologyOperationComponent, isForEach);

            if (productionCountingQuantities.isEmpty()) {
                return;
            }

            if (productionCountingQuantities.size() == 1) {
                Entity productionCountingQuantity = productionCountingQuantities.get(0);

                final BigDecimal usedQuantity = BigDecimalUtils.convertNullToZero(productionCountingQuantity
                        .getDecimalField(ProductionCountingQuantityFields.USED_QUANTITY));
                final BigDecimal productQuantity = BigDecimalUtils.convertNullToZero(trackingOperationProductInComponent
                        .getDecimalField(TrackingOperationProductInComponentFields.USED_QUANTITY));

                final BigDecimal result = operation.perform(usedQuantity, productQuantity);

                productionCountingQuantity.setField(ProductionCountingQuantityFields.USED_QUANTITY, result);

                productionCountingQuantity.getDataDefinition().save(productionCountingQuantity);
            } else {
                BigDecimal productQuantity = BigDecimalUtils.convertNullToZero(trackingOperationProductInComponent
                        .getDecimalField(TrackingOperationProductInComponentFields.USED_QUANTITY));

                for (int i = productionCountingQuantities.size() - 1; i >= 0; i--) {
                    Entity productionCountingQuantity = productionCountingQuantities.get(i);

                    final BigDecimal usedQuantity = BigDecimalUtils.convertNullToZero(productionCountingQuantity
                            .getDecimalField(ProductionCountingQuantityFields.USED_QUANTITY));

                    if (usedQuantity.compareTo(BigDecimal.ZERO) > 0 && productQuantity.compareTo(BigDecimal.ZERO) > 0) {
                        if (usedQuantity.compareTo(productQuantity) <= 0) {
                            productionCountingQuantity.setField(ProductionCountingQuantityFields.USED_QUANTITY, BigDecimal.ZERO);

                            productionCountingQuantity.getDataDefinition().save(productionCountingQuantity);

                            productQuantity = productQuantity.subtract(usedQuantity, numberService.getMathContext());
                        } else {
                            final BigDecimal result = operation.perform(usedQuantity, productQuantity);
                            productionCountingQuantity.setField(ProductionCountingQuantityFields.USED_QUANTITY, result);

                            productionCountingQuantity.getDataDefinition().save(productionCountingQuantity);

                            productQuantity = productQuantity.subtract(productQuantity, numberService.getMathContext());
                        }
                    }
                }
            }
        });

        trackingOperationProductOutComponents.forEach(trackingOperationProductOutComponent -> {

            Entity productionCountingQuantity = getOutProductionCountingQuantity(trackingOperationProductOutComponent, order,
                    technologyOperationComponent, isForEach);
            if (Objects.isNull(productionCountingQuantity)) {
                return;
            }

            final BigDecimal usedQuantity = BigDecimalUtils.convertNullToZero(productionCountingQuantity
                    .getDecimalField(ProductionCountingQuantityFields.PRODUCED_QUANTITY));
            final BigDecimal productQuantity = BigDecimalUtils.convertNullToZero(trackingOperationProductOutComponent
                    .getDecimalField(TrackingOperationProductOutComponentFields.USED_QUANTITY));
            final BigDecimal result = operation.perform(usedQuantity, productQuantity);

            productionCountingQuantity.setField(ProductionCountingQuantityFields.PRODUCED_QUANTITY, result);

            productionCountingQuantity.getDataDefinition().save(productionCountingQuantity);
        });
    }

    private void updateProductionCountingQuantity(final Entity productionTracking, final Operation operation) {
        final Entity order = productionTracking.getBelongsToField(ProductionTrackingFields.ORDER);

        String typeOfProductionRecording = order.getStringField(OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING);

        boolean isForEach = TypeOfProductionRecording.FOR_EACH.getStringValue().equals(typeOfProductionRecording);

        Entity technologyOperationComponent = productionTracking
                .getBelongsToField(ProductionTrackingFields.TECHNOLOGY_OPERATION_COMPONENT);

        final List<Entity> trackingOperationProductInComponents = productionTracking
                .getHasManyField(ProductionTrackingFields.TRACKING_OPERATION_PRODUCT_IN_COMPONENTS);
        final List<Entity> trackingOperationProductOutComponents = productionTracking
                .getHasManyField(ProductionTrackingFields.TRACKING_OPERATION_PRODUCT_OUT_COMPONENTS);

        trackingOperationProductInComponents.forEach(trackingOperationProductInComponent -> {
            List<Entity> productionCountingQuantities = getInProductionCountingQuantities(trackingOperationProductInComponent,
                    order, technologyOperationComponent, isForEach);

            if (productionCountingQuantities.isEmpty()) {
                return;
            }

            if (productionCountingQuantities.size() == 1) {
                Entity productionCountingQuantity = productionCountingQuantities.get(0);

                final BigDecimal usedQuantity = BigDecimalUtils.convertNullToZero(productionCountingQuantity
                        .getDecimalField(ProductionCountingQuantityFields.USED_QUANTITY));
                final BigDecimal productQuantity = BigDecimalUtils.convertNullToZero(trackingOperationProductInComponent
                        .getDecimalField(TrackingOperationProductInComponentFields.USED_QUANTITY));
                final BigDecimal result = operation.perform(usedQuantity, productQuantity);

                productionCountingQuantity.setField(ProductionCountingQuantityFields.USED_QUANTITY, result);

                productionCountingQuantity.getDataDefinition().save(productionCountingQuantity);

            } else {
                int lastIndex = productionCountingQuantities.size() - 1;
                BigDecimal productQuantity = BigDecimalUtils.convertNullToZero(trackingOperationProductInComponent
                        .getDecimalField(TrackingOperationProductInComponentFields.USED_QUANTITY));

                for (int i = 0; i < productionCountingQuantities.size(); i++) {
                    Entity productionCountingQuantity = productionCountingQuantities.get(i);

                    final BigDecimal usedQuantity = BigDecimalUtils.convertNullToZero(productionCountingQuantity
                            .getDecimalField(ProductionCountingQuantityFields.USED_QUANTITY));
                    final BigDecimal plannedQuantity = productionCountingQuantity.getDecimalField(ProductionCountingQuantityFields.PLANNED_QUANTITY);

                    if (i == lastIndex) {
                        final BigDecimal result = operation.perform(usedQuantity, productQuantity);

                        productionCountingQuantity.setField(ProductionCountingQuantityFields.USED_QUANTITY, result);
                        productionCountingQuantity = productionCountingQuantity.getDataDefinition().save(productionCountingQuantity);

                        productQuantity = productQuantity.subtract(productQuantity, numberService.getMathContext());
                    } else {
                        if (usedQuantity.compareTo(plannedQuantity) != 0) {
                            BigDecimal diff = plannedQuantity.subtract(usedQuantity, numberService.getMathContext());

                            if (diff.compareTo(BigDecimal.ZERO) > 0) {
                                if (diff.compareTo(productQuantity) >= 0) {
                                    final BigDecimal result = operation.perform(usedQuantity, productQuantity);

                                    productionCountingQuantity.setField(ProductionCountingQuantityFields.USED_QUANTITY, result);

                                    productionCountingQuantity = productionCountingQuantity.getDataDefinition().save(productionCountingQuantity);

                                    productQuantity = productQuantity.subtract(productQuantity, numberService.getMathContext());
                                } else {
                                    final BigDecimal result = operation.perform(usedQuantity, diff);

                                    productionCountingQuantity.setField(ProductionCountingQuantityFields.USED_QUANTITY, result);

                                    productionCountingQuantity = productionCountingQuantity.getDataDefinition().save(productionCountingQuantity);

                                    productQuantity = productQuantity.subtract(diff, numberService.getMathContext());
                                }
                            }
                        }
                    }
                }
            }

            if (pluginManager.isPluginEnabled("productFlowThruDivision")) {
                for (Entity trackingProductResourceReservation : trackingOperationProductInComponent.getHasManyField(L_RESOURCE_RESERVATIONS)) {
                    Entity orderProductResourceReservation = trackingProductResourceReservation.getBelongsToField(L_ORDER_PRODUCT_RESOURCE_RESERVATION);
                    Entity orderProductResourceReservationDb = orderProductResourceReservation.getDataDefinition().get(orderProductResourceReservation.getId());
                    BigDecimal usedQuantity = BigDecimalUtils.convertNullToZero(orderProductResourceReservationDb.getDecimalField(L_USED_QUANTITY));
                    usedQuantity = usedQuantity.add(BigDecimalUtils.convertNullToZero(trackingProductResourceReservation.getDecimalField(L_USED_QUANTITY)));
                    orderProductResourceReservationDb.setField(L_USED_QUANTITY, usedQuantity);
                    orderProductResourceReservation = orderProductResourceReservationDb.getDataDefinition().fastSave(orderProductResourceReservationDb);
                }
            }
        });

        trackingOperationProductOutComponents.forEach(trackingOperationProductOutComponent -> {
            Entity productionCountingQuantity = getOutProductionCountingQuantity(trackingOperationProductOutComponent, order,
                    technologyOperationComponent, isForEach);

            if (Objects.isNull(productionCountingQuantity)) {
                return;
            }

            final BigDecimal usedQuantity = BigDecimalUtils.convertNullToZero(productionCountingQuantity
                    .getDecimalField(ProductionCountingQuantityFields.PRODUCED_QUANTITY));
            final BigDecimal productQuantity = BigDecimalUtils.convertNullToZero(trackingOperationProductOutComponent
                    .getDecimalField(TrackingOperationProductOutComponentFields.USED_QUANTITY));
            final BigDecimal result = operation.perform(usedQuantity, productQuantity);

            productionCountingQuantity.setField(ProductionCountingQuantityFields.PRODUCED_QUANTITY, result);

            productionCountingQuantity.getDataDefinition().save(productionCountingQuantity);
        });

    }

    private void updateBasicProductionCounting(final Entity productionTracking, final Operation operation) {
        final Entity order = productionTracking.getBelongsToField(ProductionTrackingFields.ORDER);
        final List<Entity> trackingOperationProductInComponents = productionTracking
                .getHasManyField(ProductionTrackingFields.TRACKING_OPERATION_PRODUCT_IN_COMPONENTS);
        final List<Entity> trackingOperationProductOutComponents = productionTracking
                .getHasManyField(ProductionTrackingFields.TRACKING_OPERATION_PRODUCT_OUT_COMPONENTS);

        trackingOperationProductInComponents.forEach(trackingOperationProductInComponent -> {
            Entity basicProductionCounting = getBasicProductionCounting(trackingOperationProductInComponent, order);

            if (Objects.isNull(basicProductionCounting)) {
                return;
            }

            final BigDecimal usedQuantity = basicProductionCounting.getDecimalField(BasicProductionCountingFields.USED_QUANTITY);
            final BigDecimal productQuantity = trackingOperationProductInComponent
                    .getDecimalField(TrackingOperationProductInComponentFields.USED_QUANTITY);
            final BigDecimal result = operation.perform(usedQuantity, productQuantity);

            basicProductionCounting.setField(BasicProductionCountingFields.USED_QUANTITY, result);
            basicProductionCounting.getDataDefinition().save(basicProductionCounting);
        });

        trackingOperationProductOutComponents.forEach(trackingOperationProductOutComponent -> {
            Entity basicProductionCounting = getBasicProductionCounting(trackingOperationProductOutComponent, order);

            if (Objects.isNull(basicProductionCounting)) {
                return;
            }

            final BigDecimal usedQuantity = basicProductionCounting
                    .getDecimalField(BasicProductionCountingFields.PRODUCED_QUANTITY);
            final BigDecimal productQuantity = trackingOperationProductOutComponent
                    .getDecimalField(TrackingOperationProductOutComponentFields.USED_QUANTITY);
            final BigDecimal result = operation.perform(usedQuantity, productQuantity);

            basicProductionCounting.setField(BasicProductionCountingFields.PRODUCED_QUANTITY, result);

            basicProductionCounting.getDataDefinition().save(basicProductionCounting);
        });
    }

    private void checkIfTimesIsSet(final Entity productionTracking) {
        Entity orderEntity = productionTracking.getBelongsToField(ProductionTrackingFields.ORDER);
        Entity parameter = parameterService.getParameter();

        if (parameter.getBooleanField(ParameterFieldsPC.VALIDATE_PRODUCTION_RECORD_TIMES)
                && orderEntity.getBooleanField(OrderFieldsPC.REGISTER_PRODUCTION_TIME)) {
            Integer machineTime = productionTracking.getIntegerField(ProductionTrackingFields.MACHINE_TIME);

            if (Objects.isNull(machineTime) || machineTime == 0) {
                productionTracking.addError(productionTracking.getDataDefinition()
                        .getField(ProductionTrackingFields.MACHINE_TIME), "qcadooView.validate.field.error.missing");
            }

            Integer laborTime = productionTracking.getIntegerField(ProductionTrackingFields.LABOR_TIME);

            if (Objects.isNull(laborTime) || laborTime == 0) {
                productionTracking.addError(productionTracking.getDataDefinition().getField(ProductionTrackingFields.LABOR_TIME),
                        "qcadooView.validate.field.error.missing");
            }
        }

        List<Entity> worTimesWithNotSetEnd = productionTracking
                .getHasManyField(ProductionTrackingFields.STAFF_WORK_TIMES)
                .stream()
                .filter(entity -> Objects.nonNull(entity.getDateField(StaffWorkTimeFields.EFFECTIVE_EXECUTION_TIME_START))
                        && Objects.isNull(entity.getDateField(StaffWorkTimeFields.EFFECTIVE_EXECUTION_TIME_END)))
                .collect(Collectors.toList());

        if (!worTimesWithNotSetEnd.isEmpty()) {
            productionTracking.addGlobalError("productionCounting.productionTracking.messages.error.worTimesWithNotSetTime");
        }

        List<Entity> worTimesWithNotSetStart = productionTracking
                .getHasManyField(ProductionTrackingFields.STAFF_WORK_TIMES)
                .stream()
                .filter(entity -> Objects.nonNull(entity.getDateField(StaffWorkTimeFields.EFFECTIVE_EXECUTION_TIME_END))
                        && Objects.isNull(entity.getDateField(StaffWorkTimeFields.EFFECTIVE_EXECUTION_TIME_START)))
                .collect(Collectors.toList());

        if (!worTimesWithNotSetStart.isEmpty()) {
            productionTracking.addGlobalError("productionCounting.productionTracking.messages.error.worTimesWithNotSetTimeEnd");
        }
    }

    private Entity getBasicProductionCounting(final Entity trackingOperationProductComponent, final Entity order) {
        Entity product = trackingOperationProductComponent.getBelongsToField(L_PRODUCT);

        return order.getHasManyField(OrderFieldsBPC.BASIC_PRODUCTION_COUNTINGS).find()
                .add(SearchRestrictions.belongsTo(BasicProductionCountingFields.PRODUCT, product)).setMaxResults(1)
                .uniqueResult();
    }

    private List<Entity> getInProductionCountingQuantities(final Entity trackingOperationProductComponent,
                                                           final Entity order,
                                                           Entity technologyOperationComponent,
                                                           final boolean isForEach) {
        Entity product = trackingOperationProductComponent.getBelongsToField(L_PRODUCT);

        SearchCriteriaBuilder searchCriteriaBuilder = order
                .getHasManyField(OrderFieldsBPC.PRODUCTION_COUNTING_QUANTITIES)
                .find()
                .add(SearchRestrictions.eq(ProductionCountingQuantityFields.ROLE,
                        ProductionCountingQuantityRole.USED.getStringValue()))
                .add(SearchRestrictions.in(ProductionCountingQuantityFields.TYPE_OF_MATERIAL,
                        Lists.newArrayList(ProductionCountingQuantityTypeOfMaterial.COMPONENT.getStringValue(),
                                ProductionCountingQuantityTypeOfMaterial.INTERMEDIATE.getStringValue())))
                .add(SearchRestrictions.belongsTo(ProductionCountingQuantityFields.PRODUCT, product));

        if (isForEach && Objects.nonNull(technologyOperationComponent)) {
            technologyOperationComponent = technologyOperationComponent.getDataDefinition().get(technologyOperationComponent.getId());

            searchCriteriaBuilder = searchCriteriaBuilder.add(SearchRestrictions.belongsTo(ProductionCountingQuantityFields.TECHNOLOGY_OPERATION_COMPONENT, technologyOperationComponent));
        }

        return searchCriteriaBuilder.list().getEntities();
    }

    private Entity getOutProductionCountingQuantity(final Entity trackingOperationProductOutComponent,
                                                    final Entity order,
                                                    Entity technologyOperationComponent, final boolean isForEach) {
        Entity product = trackingOperationProductOutComponent.getBelongsToField(L_PRODUCT);

        SearchCriteriaBuilder searchCriteriaBuilder = order
                .getHasManyField(OrderFieldsBPC.PRODUCTION_COUNTING_QUANTITIES)
                .find()
                .add(SearchRestrictions.eq(ProductionCountingQuantityFields.ROLE,
                        ProductionCountingQuantityRole.PRODUCED.getStringValue()))
                .add(SearchRestrictions.belongsTo(ProductionCountingQuantityFields.PRODUCT, product));

        if (isForEach && Objects.nonNull(technologyOperationComponent)) {
            technologyOperationComponent = technologyOperationComponent.getDataDefinition().get(technologyOperationComponent.getId());

            searchCriteriaBuilder = searchCriteriaBuilder.add(SearchRestrictions.belongsTo(ProductionCountingQuantityFields.TECHNOLOGY_OPERATION_COMPONENT, technologyOperationComponent));
        }

        return searchCriteriaBuilder.setMaxResults(1).uniqueResult();
    }

    public void onCorrected(final Entity productionTracking) {
        updateBasicProductionCounting(productionTracking, new Subtraction());
        updateProductionCountingQuantitySubtraction(productionTracking, new Subtraction());
        setOrderDoneAndWastesQuantity(productionTracking, new Subtraction());
        fillOrderReportedQuantity(productionTracking, new Subtraction());
    }

    public void unMarkLastTracking(final Entity productionTracking) {
        productionTracking.setField(ProductionTrackingFields.LAST_TRACKING, Boolean.FALSE);
    }

    private interface Operation {

        BigDecimal perform(final BigDecimal argument1, final BigDecimal argument2);
    }

    private class Addition implements Operation {

        @Override
        public BigDecimal perform(final BigDecimal originalValue, final BigDecimal addition) {
            BigDecimal value;
            BigDecimal add;

            if (Objects.isNull(originalValue)) {
                value = BigDecimal.ZERO;
            } else {
                value = originalValue;
            }
            if (Objects.isNull(addition)) {
                add = BigDecimal.ZERO;
            } else {
                add = addition;
            }

            return value.add(add, numberService.getMathContext());
        }

    }

    private class Subtraction implements Operation {

        @Override
        public BigDecimal perform(final BigDecimal originalValue, final BigDecimal substrahend) {
            BigDecimal value;
            BigDecimal sub;

            if (Objects.isNull(originalValue)) {
                value = BigDecimal.ZERO;
            } else {
                value = originalValue;
            }

            if (Objects.isNull(substrahend)) {
                sub = BigDecimal.ZERO;
            } else {
                sub = substrahend;
            }

            return value.subtract(sub, numberService.getMathContext());
        }

    }

}
