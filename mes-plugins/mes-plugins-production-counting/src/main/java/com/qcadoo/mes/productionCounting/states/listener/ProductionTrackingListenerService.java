/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.4
 * <p/>
 * This file is part of Qcadoo.
 * <p/>
 * Qcadoo is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Affero General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option) any
 * later version.
 * <p/>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 * <p/>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA
 * ***************************************************************************
 */
package com.qcadoo.mes.productionCounting.states.listener;

import com.google.common.collect.Lists;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.basicProductionCounting.BasicProductionCountingService;
import com.qcadoo.mes.basicProductionCounting.constants.BasicProductionCountingFields;
import com.qcadoo.mes.basicProductionCounting.constants.OrderFieldsBPC;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.states.aop.OrderStateChangeAspect;
import com.qcadoo.mes.orders.states.constants.OrderState;
import com.qcadoo.mes.productionCounting.ProductionCountingService;
import com.qcadoo.mes.productionCounting.constants.*;
import com.qcadoo.mes.productionCounting.states.constants.ProductionTrackingStateStringValues;
import com.qcadoo.mes.productionCounting.utils.OrderClosingHelper;
import com.qcadoo.mes.states.StateChangeContext;
import com.qcadoo.mes.states.constants.StateChangeStatus;
import com.qcadoo.mes.states.service.StateChangeContextBuilder;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.api.validators.ErrorMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

import static com.qcadoo.mes.basicProductionCounting.constants.BasicProductionCountingFields.ORDER;
import static com.qcadoo.mes.orders.constants.OrderFields.STATE;
import static com.qcadoo.mes.orders.states.constants.OrderState.COMPLETED;

@Service
public final class ProductionTrackingListenerService {

    private static final String L_PRODUCT = "product";

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
        checkIfRecordOperationProductComponentsWereFilled(productionTracking);
        checkIfExistsFinalRecord(productionTracking);
        checkIfTimesIsSet(productionTracking);
    }

    public void onLeavingDraft(final Entity productionTracking) {
        productionTracking.setField(ProductionTrackingFields.LAST_STATE_CHANGE_FAILS, false);
        productionTracking.setField(ProductionTrackingFields.LAST_STATE_CHANGE_FAIL_CAUSE, null);
    }

    public void onAccept(final Entity productionTracking) {
        updateBasicProductionCounting(productionTracking, new Addition());
        setOrderDoneAndWastesQuantity(productionTracking, new Addition());
        closeOrder(productionTracking);
    }

    public void onChangeFromAcceptedToDeclined(final Entity productionTracking) {
        updateBasicProductionCounting(productionTracking, new Substraction());
        setOrderDoneAndWastesQuantity(productionTracking, new Substraction());
    }

    private void checkIfRecordOperationProductComponentsWereFilled(final Entity productionTracking) {
        if (!checkIfUsedQuantitiesWereFilled(productionTracking)
                && !checkIfUsedOrWastesQuantitiesWereFilled(productionTracking)) {
            productionTracking.addGlobalError("productionCounting.productionTracking.messages.error.recordOperationProductComponentsNotFilled");
        }
    }

    public boolean checkIfUsedQuantitiesWereFilled(final Entity productionTracking) {
        final SearchCriteriaBuilder searchBuilder = productionTracking
                .getHasManyField(ProductionTrackingFields.TRACKING_OPERATION_PRODUCT_IN_COMPONENTS).find()
                .add(SearchRestrictions.isNotNull(TrackingOperationProductInComponentFields.USED_QUANTITY));

        return (searchBuilder.list().getTotalNumberOfEntities() != 0);
    }

    public boolean checkIfUsedOrWastesQuantitiesWereFilled(final Entity productionTracking) {
        final SearchCriteriaBuilder searchBuilder = productionTracking
                .getHasManyField(ProductionTrackingFields.TRACKING_OPERATION_PRODUCT_OUT_COMPONENTS).find()
                .add(SearchRestrictions.or(SearchRestrictions.isNotNull(TrackingOperationProductOutComponentFields.USED_QUANTITY),
                        SearchRestrictions.isNotNull(TrackingOperationProductOutComponentFields.WASTES_QUANTITY)));

        return (searchBuilder.list().getTotalNumberOfEntities() != 0);
    }

    public void checkIfExistsFinalRecord(final Entity productionTracking) {
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

    public void closeOrder(final Entity productionTracking) {
        final Entity order = productionTracking.getBelongsToField(ORDER);
        Entity orderFromDB = order.getDataDefinition().get(order.getId());
        if (!orderClosingHelper.orderShouldBeClosed(productionTracking)) {
            return;
        }
        if (order.getStringField(STATE).equals(COMPLETED.getStringValue())) {
            productionTracking.addGlobalMessage("productionCounting.order.orderIsAlreadyClosed", false, false);
            return;
        }
        final StateChangeContext orderStateChangeContext = stateChangeContextBuilder
                .build(orderStateChangeAspect.getChangeEntityDescriber(), orderFromDB, OrderState.COMPLETED.getStringValue());
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

            if (!errors.isEmpty()) {
                StringBuilder errorMessages = new StringBuilder();

                for (ErrorMessage errorMessage : errors) {
                    String translatedErrorMessage = translationService.translate(errorMessage.getMessage(), Locale.getDefault(),
                            errorMessage.getVars());
                    errorMessages.append(translatedErrorMessage);
                    errorMessages.append(", ");
                }

                productionTracking.addGlobalError("orders.order.orderStates.error", errorMessages.toString());
            }
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

            order.setField(OrderFields.WASTES_QUANTITY, getWastesQuantity(productionTracking, order, operation));
            order.setField("finalProductionTracking", productionTracking.getBooleanField(ProductionTrackingFields.LAST_TRACKING));
            order.getDataDefinition().save(order);
        }
    }

    private BigDecimal getWastesQuantity(final Entity productionTracking, final Entity order, final Operation operation) {
        Entity mainProduct = order.getBelongsToField(OrderFields.PRODUCT);
        Entity mainTrackingOperationProductOutComponent = productionTracking
                .getHasManyField(ProductionTrackingFields.TRACKING_OPERATION_PRODUCT_OUT_COMPONENTS).find()
                .add(SearchRestrictions.belongsTo(TrackingOperationProductOutComponentFields.PRODUCT, mainProduct))
                .setMaxResults(1).uniqueResult();
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

    private void updateBasicProductionCounting(final Entity productionTracking, final Operation operation) {
        final Entity order = productionTracking.getBelongsToField(ProductionTrackingFields.ORDER);

        final List<Entity> basicProductionCountings = order.getHasManyField(OrderFieldsBPC.BASIC_PRODUCTION_COUNTINGS);

        final List<Entity> trackingOperationProductInComponents = productionTracking
                .getHasManyField(ProductionTrackingFields.TRACKING_OPERATION_PRODUCT_IN_COMPONENTS);
        final List<Entity> trackingOperationProductOutComponents = productionTracking
                .getHasManyField(ProductionTrackingFields.TRACKING_OPERATION_PRODUCT_OUT_COMPONENTS);

        for (Entity trackingOperationProductInComponent : trackingOperationProductInComponents) {
            Entity basicProductionCounting;

            try {
                basicProductionCounting = getBasicProductionCounting(trackingOperationProductInComponent,
                        basicProductionCountings);
            } catch (IllegalStateException e) {
                continue;
            }

            final BigDecimal usedQuantity = basicProductionCounting.getDecimalField(BasicProductionCountingFields.USED_QUANTITY);
            final BigDecimal productQuantity = trackingOperationProductInComponent
                    .getDecimalField(TrackingOperationProductInComponentFields.USED_QUANTITY);
            final BigDecimal result = operation.perform(usedQuantity, productQuantity);

            basicProductionCounting.setField(BasicProductionCountingFields.USED_QUANTITY, result);
            basicProductionCounting = basicProductionCounting.getDataDefinition().save(basicProductionCounting);
        }

        for (Entity trackingOperationProductOutComponent : trackingOperationProductOutComponents) {
            Entity productionCounting;

            try {
                productionCounting = getBasicProductionCounting(trackingOperationProductOutComponent, basicProductionCountings);
            } catch (IllegalStateException e) {
                continue;
            }

            final BigDecimal usedQuantity = productionCounting.getDecimalField(BasicProductionCountingFields.PRODUCED_QUANTITY);
            final BigDecimal productQuantity = trackingOperationProductOutComponent
                    .getDecimalField(TrackingOperationProductOutComponentFields.USED_QUANTITY);
            final BigDecimal result = operation.perform(usedQuantity, productQuantity);

            productionCounting.setField(BasicProductionCountingFields.PRODUCED_QUANTITY, result);
            productionCounting = productionCounting.getDataDefinition().save(productionCounting);
        }
    }

    public void checkIfTimesIsSet(final Entity productionTracking) {
        Entity orderEntity = productionTracking.getBelongsToField(ProductionTrackingFields.ORDER);
        Entity parameter = parameterService.getParameter();
        if (parameter.getBooleanField(ParameterFieldsPC.VALIDATE_PRODUCTION_RECORD_TIMES)
                && orderEntity.getBooleanField(OrderFieldsPC.REGISTER_PRODUCTION_TIME)) {
            Integer machineTimie = productionTracking.getIntegerField(ProductionTrackingFields.MACHINE_TIME);
            if (machineTimie == null || machineTimie == 0) {
                productionTracking.addError(productionTracking.getDataDefinition().getField(ProductionTrackingFields.MACHINE_TIME),
                        "qcadooView.validate.field.error.missing");
            }
            Integer laborTime = productionTracking.getIntegerField(ProductionTrackingFields.LABOR_TIME);
            if (laborTime == null || laborTime == 0) {
                productionTracking.addError(productionTracking.getDataDefinition().getField(ProductionTrackingFields.LABOR_TIME),
                        "qcadooView.validate.field.error.missing");
            }
        }

    }

    private Entity getBasicProductionCounting(final Entity trackingOperationProductComponent,
            final List<Entity> basicProductionCountings) {
        Entity product = trackingOperationProductComponent.getBelongsToField(L_PRODUCT);

        for (Entity basicProductionCounting : basicProductionCountings) {
            if (basicProductionCounting.getBelongsToField(BasicProductionCountingFields.PRODUCT).getId()
                    .equals(product.getId())) {
                return basicProductionCounting;
            }
        }

        throw new IllegalStateException("No basic production counting found for product");
    }

    public void onCorrected(final Entity productionTracking) {
        updateBasicProductionCounting(productionTracking, new Substraction());
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
