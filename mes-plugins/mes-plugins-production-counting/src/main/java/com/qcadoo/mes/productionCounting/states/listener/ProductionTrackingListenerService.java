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
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.basicProductionCounting.BasicProductionCountingService;
import com.qcadoo.mes.basicProductionCounting.constants.BasicProductionCountingFields;
import com.qcadoo.mes.basicProductionCounting.constants.OrderFieldsBPC;
import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityFields;
import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityRole;
import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityTypeOfMaterial;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.states.aop.OrderStateChangeAspect;
import com.qcadoo.mes.orders.states.constants.OrderState;
import com.qcadoo.mes.productionCounting.ProductionCountingService;
import com.qcadoo.mes.productionCounting.constants.OrderFieldsPC;
import com.qcadoo.mes.productionCounting.constants.ParameterFieldsPC;
import com.qcadoo.mes.productionCounting.constants.ProductionTrackingFields;
import com.qcadoo.mes.productionCounting.constants.StaffWorkTimeFields;
import com.qcadoo.mes.productionCounting.constants.TrackingOperationProductInComponentFields;
import com.qcadoo.mes.productionCounting.constants.TrackingOperationProductOutComponentFields;
import com.qcadoo.mes.productionCounting.constants.TypeOfProductionRecording;
import com.qcadoo.mes.productionCounting.states.constants.ProductionTrackingStateStringValues;
import com.qcadoo.mes.productionCounting.utils.OrderClosingHelper;
import com.qcadoo.mes.states.StateChangeContext;
import com.qcadoo.mes.states.constants.StateChangeStatus;
import com.qcadoo.mes.states.service.StateChangeContextBuilder;
import com.qcadoo.model.api.BigDecimalUtils;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchProjections;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.api.validators.ErrorMessage;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
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

    @Autowired
    private NumberService numberService;

    @Autowired
    private TranslationService translationService;

    @Autowired
    private ProductionCountingService productionCountingService;

    @Autowired
    private BasicProductionCountingService basicProductionCountingService;

    @Autowired
    private OrderStateChangeAspect orderStateChangeAspect;

    @Autowired
    private StateChangeContextBuilder stateChangeContextBuilder;

    @Autowired
    private OrderClosingHelper orderClosingHelper;

    @Autowired
    private ParameterService parameterService;

    public void onChangeFromDraftToAny(final Entity productionTracking) {
        productionTracking.setField(ProductionTrackingFields.LAST_STATE_CHANGE_FAILS, false);
        productionTracking.setField(ProductionTrackingFields.LAST_STATE_CHANGE_FAIL_CAUSE, null);
    }

    public void validationOnAccept(final Entity productionTracking) {
        checkIfExistsFinalRecord(productionTracking);
        checkIfTimesIsSet(productionTracking);
        checkIfBatchEvidenceSet(productionTracking);
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
        List<Entity> productInComponents = productionTracking
                .getHasManyField(ProductionTrackingFields.TRACKING_OPERATION_PRODUCT_IN_COMPONENTS);
        productInComponents.forEach(pic -> {
            Entity picProduct = pic.getBelongsToField(TrackingOperationProductInComponentFields.PRODUCT);
            if (BigDecimalUtils.convertNullToZero(pic.getDecimalField(TrackingOperationProductInComponentFields.USED_QUANTITY))
                    .compareTo(BigDecimal.ZERO) > 0
                    && picProduct.getBooleanField(ProductFields.BATCH_EVIDENCE)
                    && pic.getHasManyField(TrackingOperationProductInComponentFields.USED_BATCHES).isEmpty()) {
                productionTracking.addGlobalError("productionCounting.productionTracking.error.batchEvidenceRequiredForProduct",
                        picProduct.getStringField(ProductFields.NUMBER));
            }
        });
    }

    public void onLeavingDraft(final Entity productionTracking) {
        productionTracking.setField(ProductionTrackingFields.LAST_STATE_CHANGE_FAILS, false);
        productionTracking.setField(ProductionTrackingFields.LAST_STATE_CHANGE_FAIL_CAUSE, null);
        if (parameterService.getParameter().getBooleanField(
                ParameterFieldsPC.CALCULATE_AMOUNT_TIME_EMPLOYEES_ON_ACCEPTANCE_RECORD)) {
            Integer laborTime = productionTracking.getHasManyField(ProductionTrackingFields.STAFF_WORK_TIMES).stream()
                    .mapToInt(en -> en.getIntegerField(StaffWorkTimeFields.LABOR_TIME)).sum();
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
        updateBasicProductionCounting(productionTracking, new Substraction());
        setOrderDoneAndWastesQuantity(productionTracking, new Substraction());
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

        final SearchCriteriaBuilder searchBuilder = productionTracking.getDataDefinition().find();
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
            productionTracking.addGlobalError("productionCounting.order.orderCannotBeClosed", false);

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

                productionTracking.addGlobalError("orders.order.orderStates.error", errorMessages.toString());
            }
        }
    }

    private void fillOrderReportedQuantity(final Entity productionTracking, final Operation operation) {
        Entity order = productionTracking.getBelongsToField(ProductionTrackingFields.ORDER);
        order = order.getDataDefinition().get(order.getId());
        Entity mainProduct = order.getBelongsToField(OrderFields.PRODUCT);
        Entity mainTrackingOperationProductOutComponent = productionTracking
                .getHasManyField(ProductionTrackingFields.TRACKING_OPERATION_PRODUCT_OUT_COMPONENTS).find()
                .add(SearchRestrictions.belongsTo(TrackingOperationProductOutComponentFields.PRODUCT, mainProduct))
                .setMaxResults(1).uniqueResult();
        if (mainTrackingOperationProductOutComponent != null) {
            BigDecimal trackedQuantity = mainTrackingOperationProductOutComponent
                    .getDecimalField(TrackingOperationProductOutComponentFields.USED_QUANTITY);
            BigDecimal reported = BigDecimalUtils.convertNullToZero(order.getDecimalField(OrderFields.REPORTED_PRODUCTION_QUANTITY));
            BigDecimal newQuantity =  operation.perform(reported, trackedQuantity);
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

        if (mainTrackingOperationProductOutComponent != null) {
            order.setField(OrderFields.DONE_QUANTITY,
                    basicProductionCountingService.getProducedQuantityFromBasicProductionCountings(order));

            order.setField(OrderFields.WASTES_QUANTITY,
                    getWastesQuantity(mainTrackingOperationProductOutComponent, order, operation));
            order.setField("finalProductionTracking", productionTracking.getBooleanField(ProductionTrackingFields.LAST_TRACKING));
            order.getDataDefinition().save(order);
        }
    }

    private BigDecimal getWastesQuantity(final Entity mainTrackingOperationProductOutComponent, final Entity order,
            final Operation operation) {
        BigDecimal mainWastesQuantity = mainTrackingOperationProductOutComponent
                .getDecimalField(TrackingOperationProductOutComponentFields.WASTES_QUANTITY);
        BigDecimal orderWastesQuantity = order.getDecimalField(OrderFields.WASTES_QUANTITY);

        if (orderWastesQuantity == null) {
            orderWastesQuantity = BigDecimal.ZERO;
        }
        if (mainWastesQuantity != null) {

            return operation.perform(orderWastesQuantity, mainWastesQuantity);
        }

        return orderWastesQuantity;
    }

    private void updateProductionCountingQuantitySubtraction(Entity productionTracking, Operation operation) {
        final Entity order = productionTracking.getBelongsToField(ProductionTrackingFields.ORDER);
        String typeOfProductionRecording = order.getStringField(OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING);

        if (TypeOfProductionRecording.FOR_EACH.getStringValue().equals(typeOfProductionRecording)) {
            Entity technologyOperationComponent = productionTracking
                    .getBelongsToField(ProductionTrackingFields.TECHNOLOGY_OPERATION_COMPONENT);
            Entity toc = technologyOperationComponent.getDataDefinition().get(technologyOperationComponent.getId());
            final List<Entity> trackingOperationProductInComponents = productionTracking
                    .getHasManyField(ProductionTrackingFields.TRACKING_OPERATION_PRODUCT_IN_COMPONENTS);
            final List<Entity> trackingOperationProductOutComponents = productionTracking
                    .getHasManyField(ProductionTrackingFields.TRACKING_OPERATION_PRODUCT_OUT_COMPONENTS);

            trackingOperationProductInComponents.forEach(trackingOperationProductInComponent -> {
                List<Entity> productionCountingQuantities = getInProductionCountingQuantities(
                        trackingOperationProductInComponent, order, toc);

                if (productionCountingQuantities.isEmpty()) {
                    return;
                }

                if (productionCountingQuantities.size() == 1) {
                    Entity pcq = productionCountingQuantities.get(0);
                    final BigDecimal usedQuantity = BigDecimalUtils.convertNullToZero(pcq
                            .getDecimalField(ProductionCountingQuantityFields.USED_QUANTITY));
                    final BigDecimal productQuantity = trackingOperationProductInComponent
                            .getDecimalField(TrackingOperationProductInComponentFields.USED_QUANTITY);

                    final BigDecimal result = operation.perform(usedQuantity, productQuantity);

                    pcq.setField(ProductionCountingQuantityFields.USED_QUANTITY, result);
                    pcq.getDataDefinition().save(pcq);
                } else {

                    BigDecimal productQuantity = trackingOperationProductInComponent
                            .getDecimalField(TrackingOperationProductInComponentFields.USED_QUANTITY);

                    for (int i = productionCountingQuantities.size() - 1; i >= 0; i--) {
                        Entity pcq = productionCountingQuantities.get(i);
                        final BigDecimal usedQuantity = BigDecimalUtils.convertNullToZero(pcq
                                .getDecimalField(ProductionCountingQuantityFields.USED_QUANTITY));

                        if (usedQuantity.compareTo(BigDecimal.ZERO) > 0 && productQuantity.compareTo(BigDecimal.ZERO) > 0) {
                            if (usedQuantity.compareTo(productQuantity) <= 0) {

                                pcq.setField(ProductionCountingQuantityFields.USED_QUANTITY, BigDecimal.ZERO);
                                pcq.getDataDefinition().save(pcq);
                                productQuantity = productQuantity.subtract(usedQuantity, numberService.getMathContext());
                            } else {
                                final BigDecimal result = operation.perform(usedQuantity, productQuantity);
                                pcq.setField(ProductionCountingQuantityFields.USED_QUANTITY, result);
                                pcq.getDataDefinition().save(pcq);
                                productQuantity = productQuantity.subtract(productQuantity, numberService.getMathContext());
                            }
                        }
                    }
                }

            });

            trackingOperationProductOutComponents.forEach(trackingOperationProductOutComponent -> {

                Entity productionCountingQuantity = getOutProductionCountingQuantity(trackingOperationProductOutComponent, order,
                        toc);
                if (productionCountingQuantity == null) {
                    return;
                }
                final BigDecimal usedQuantity = productionCountingQuantity
                        .getDecimalField(ProductionCountingQuantityFields.PRODUCED_QUANTITY);
                final BigDecimal productQuantity = trackingOperationProductOutComponent
                        .getDecimalField(TrackingOperationProductOutComponentFields.USED_QUANTITY);
                final BigDecimal result = operation.perform(usedQuantity, productQuantity);
                productionCountingQuantity.setField(ProductionCountingQuantityFields.PRODUCED_QUANTITY, result);
                productionCountingQuantity.getDataDefinition().save(productionCountingQuantity);
            });
        }
    }

    private void updateProductionCountingQuantity(final Entity productionTracking, final Operation operation) {
        final Entity order = productionTracking.getBelongsToField(ProductionTrackingFields.ORDER);
        String typeOfProductionRecording = order.getStringField(OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING);

        if (TypeOfProductionRecording.FOR_EACH.getStringValue().equals(typeOfProductionRecording)) {
            Entity technologyOperationComponent = productionTracking
                    .getBelongsToField(ProductionTrackingFields.TECHNOLOGY_OPERATION_COMPONENT);
            Entity toc = technologyOperationComponent.getDataDefinition().get(technologyOperationComponent.getId());
            final List<Entity> trackingOperationProductInComponents = productionTracking
                    .getHasManyField(ProductionTrackingFields.TRACKING_OPERATION_PRODUCT_IN_COMPONENTS);
            final List<Entity> trackingOperationProductOutComponents = productionTracking
                    .getHasManyField(ProductionTrackingFields.TRACKING_OPERATION_PRODUCT_OUT_COMPONENTS);

            trackingOperationProductInComponents
                    .forEach(trackingOperationProductInComponent -> {
                        List<Entity> productionCountingQuantities = getInProductionCountingQuantities(
                                trackingOperationProductInComponent, order, toc);

                        if (productionCountingQuantities.isEmpty()) {
                            return;
                        }

                        if (productionCountingQuantities.size() == 1) {
                            Entity pcq = productionCountingQuantities.get(0);
                            final BigDecimal usedQuantity = BigDecimalUtils.convertNullToZero(pcq
                                    .getDecimalField(ProductionCountingQuantityFields.USED_QUANTITY));
                            final BigDecimal productQuantity = trackingOperationProductInComponent
                                    .getDecimalField(TrackingOperationProductInComponentFields.USED_QUANTITY);

                            final BigDecimal result = operation.perform(usedQuantity, productQuantity);

                            pcq.setField(ProductionCountingQuantityFields.USED_QUANTITY, result);
                            pcq.getDataDefinition().save(pcq);
                        } else {
                            int lastIndex = productionCountingQuantities.size() - 1;
                            BigDecimal productQuantity = trackingOperationProductInComponent
                                    .getDecimalField(TrackingOperationProductInComponentFields.USED_QUANTITY);

                            for (int i = 0; i < productionCountingQuantities.size(); i++) {
                                Entity pcq = productionCountingQuantities.get(i);
                                final BigDecimal usedQuantity = BigDecimalUtils.convertNullToZero(pcq
                                        .getDecimalField(ProductionCountingQuantityFields.USED_QUANTITY));
                                final BigDecimal plannedQuantity = pcq
                                        .getDecimalField(ProductionCountingQuantityFields.PLANNED_QUANTITY);

                                if (i == lastIndex) {

                                    final BigDecimal result = operation.perform(usedQuantity, productQuantity);
                                    pcq.setField(ProductionCountingQuantityFields.USED_QUANTITY, result);
                                    pcq.getDataDefinition().save(pcq);
                                    productQuantity = productQuantity.subtract(productQuantity, numberService.getMathContext());

                                } else {

                                    if (usedQuantity.compareTo(plannedQuantity) != 0) {

                                        BigDecimal diff = plannedQuantity.subtract(usedQuantity, numberService.getMathContext());
                                        if (diff.compareTo(BigDecimal.ZERO) > 0) {

                                            if (diff.compareTo(productQuantity) >= 0) {
                                                final BigDecimal result = operation.perform(usedQuantity, productQuantity);
                                                pcq.setField(ProductionCountingQuantityFields.USED_QUANTITY, result);
                                                pcq.getDataDefinition().save(pcq);
                                                productQuantity = productQuantity.subtract(productQuantity,
                                                        numberService.getMathContext());

                                            } else {
                                                final BigDecimal result = operation.perform(usedQuantity, diff);
                                                pcq.setField(ProductionCountingQuantityFields.USED_QUANTITY, result);
                                                pcq.getDataDefinition().save(pcq);
                                                productQuantity = productQuantity.subtract(diff, numberService.getMathContext());
                                            }
                                        }
                                    }
                                }

                            }
                        }

                    });

            trackingOperationProductOutComponents.forEach(trackingOperationProductOutComponent -> {

                Entity productionCountingQuantity = getOutProductionCountingQuantity(trackingOperationProductOutComponent, order,
                        toc);
                if (productionCountingQuantity == null) {
                    return;
                }
                final BigDecimal usedQuantity = productionCountingQuantity
                        .getDecimalField(ProductionCountingQuantityFields.PRODUCED_QUANTITY);
                final BigDecimal productQuantity = trackingOperationProductOutComponent
                        .getDecimalField(TrackingOperationProductOutComponentFields.USED_QUANTITY);
                final BigDecimal result = operation.perform(usedQuantity, productQuantity);
                productionCountingQuantity.setField(ProductionCountingQuantityFields.PRODUCED_QUANTITY, result);
                productionCountingQuantity.getDataDefinition().save(productionCountingQuantity);
            });
        }
    }

    private void updateBasicProductionCounting(final Entity productionTracking, final Operation operation) {
        final Entity order = productionTracking.getBelongsToField(ProductionTrackingFields.ORDER);

        final List<Entity> trackingOperationProductInComponents = productionTracking
                .getHasManyField(ProductionTrackingFields.TRACKING_OPERATION_PRODUCT_IN_COMPONENTS);
        final List<Entity> trackingOperationProductOutComponents = productionTracking
                .getHasManyField(ProductionTrackingFields.TRACKING_OPERATION_PRODUCT_OUT_COMPONENTS);

        trackingOperationProductInComponents.forEach(trackingOperationProductInComponent -> {
            Entity basicProductionCounting = getBasicProductionCounting(trackingOperationProductInComponent, order);

            if (basicProductionCounting == null) {
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

            if (basicProductionCounting == null) {
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
            Integer machineTimie = productionTracking.getIntegerField(ProductionTrackingFields.MACHINE_TIME);

            if (machineTimie == null || machineTimie == 0) {
                productionTracking.addError(productionTracking.getDataDefinition()
                        .getField(ProductionTrackingFields.MACHINE_TIME), "qcadooView.validate.field.error.missing");
            }

            Integer laborTime = productionTracking.getIntegerField(ProductionTrackingFields.LABOR_TIME);

            if (laborTime == null || laborTime == 0) {
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

    private List<Entity> getInProductionCountingQuantities(Entity trackingOperationProductComponent, Entity order, Entity toc) {
        Entity product = trackingOperationProductComponent.getBelongsToField(L_PRODUCT);

        return order
                .getHasManyField(OrderFieldsBPC.PRODUCTION_COUNTING_QUANTITIES)
                .find()
                .add(SearchRestrictions.eq(ProductionCountingQuantityFields.ROLE,
                        ProductionCountingQuantityRole.USED.getStringValue()))
                .add(SearchRestrictions.eq(ProductionCountingQuantityFields.TYPE_OF_MATERIAL,
                        ProductionCountingQuantityTypeOfMaterial.COMPONENT.getStringValue()))
                .add(SearchRestrictions.belongsTo(ProductionCountingQuantityFields.PRODUCT, product))
                .add(SearchRestrictions.belongsTo(ProductionCountingQuantityFields.TECHNOLOGY_OPERATION_COMPONENT, toc)).list()
                .getEntities();

    }

    private Entity getOutProductionCountingQuantity(Entity trackingOperationProductOutComponent, Entity order, Entity toc) {
        Entity product = trackingOperationProductOutComponent.getBelongsToField(L_PRODUCT);

        return order
                .getHasManyField(OrderFieldsBPC.PRODUCTION_COUNTING_QUANTITIES)
                .find()
                .add(SearchRestrictions.eq(ProductionCountingQuantityFields.ROLE,
                        ProductionCountingQuantityRole.PRODUCED.getStringValue()))
                .add(SearchRestrictions.belongsTo(ProductionCountingQuantityFields.PRODUCT, product))
                .add(SearchRestrictions.belongsTo(ProductionCountingQuantityFields.TECHNOLOGY_OPERATION_COMPONENT, toc))
                .setMaxResults(1).uniqueResult();

    }

    public void onCorrected(final Entity productionTracking) {
        updateBasicProductionCounting(productionTracking, new Substraction());
        updateProductionCountingQuantitySubtraction(productionTracking, new Substraction());
        setOrderDoneAndWastesQuantity(productionTracking, new Substraction());
        fillOrderReportedQuantity(productionTracking, new Substraction());
    }

    private interface Operation {

        BigDecimal perform(BigDecimal argument1, BigDecimal argument2);
    }

    private class Addition implements Operation {

        @Override
        public BigDecimal perform(final BigDecimal orginalValue, final BigDecimal addition) {
            BigDecimal value;
            BigDecimal add;

            if (orginalValue == null) {
                value = BigDecimal.ZERO;
            } else {
                value = orginalValue;
            }
            if (addition == null) {
                add = BigDecimal.ZERO;
            } else {
                add = addition;
            }

            return value.add(add, numberService.getMathContext());
        }

    }

    private class Substraction implements Operation {

        @Override
        public BigDecimal perform(final BigDecimal orginalValue, final BigDecimal substrahend) {
            BigDecimal value;
            BigDecimal sub;

            if (orginalValue == null) {
                value = BigDecimal.ZERO;
            } else {
                value = orginalValue;
            }

            if (substrahend == null) {
                sub = BigDecimal.ZERO;
            } else {
                sub = substrahend;
            }

            return value.subtract(sub, numberService.getMathContext());
        }

    }

}
