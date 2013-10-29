/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.2.0
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
package com.qcadoo.mes.productionCounting.states.listener;

import static com.qcadoo.mes.basicProductionCounting.constants.BasicProductionCountingFields.ORDER;
import static com.qcadoo.mes.basicProductionCounting.constants.BasicProductionCountingFields.PRODUCT;
import static com.qcadoo.mes.orders.constants.OrderFields.STATE;
import static com.qcadoo.mes.orders.states.constants.OrderState.ACCEPTED;
import static com.qcadoo.mes.orders.states.constants.OrderState.COMPLETED;
import static com.qcadoo.mes.productionCounting.internal.constants.OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING;
import static com.qcadoo.mes.productionCounting.internal.constants.ProductionRecordFields.*;
import static com.qcadoo.mes.productionCounting.internal.constants.TypeOfProductionRecording.FOR_EACH;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.basicProductionCounting.constants.BasicProductionCountingConstants;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.states.aop.OrderStateChangeAspect;
import com.qcadoo.mes.orders.states.constants.OrderState;
import com.qcadoo.mes.productionCounting.internal.constants.ProductionRecordFields;
import com.qcadoo.mes.productionCounting.utils.OrderClosingHelper;
import com.qcadoo.mes.states.StateChangeContext;
import com.qcadoo.mes.states.constants.StateChangeStatus;
import com.qcadoo.mes.states.messages.constants.StateMessageType;
import com.qcadoo.mes.states.service.StateChangeContextBuilder;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.api.validators.ErrorMessage;

@Service
public final class ProductionRecordBasicListenerService {

    private static final String L_PRODUCED_QUANTITY = "producedQuantity";

    private static final String L_USED_QUANTITY = "usedQuantity";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NumberService numberService;

    @Autowired
    private TranslationService translationService;

    @Autowired
    private OrderStateChangeAspect orderStateChangeAspect;

    @Autowired
    private StateChangeContextBuilder stateChangeContextBuilder;

    @Autowired
    private OrderClosingHelper orderClosingHelper;

    public void onLeavingDraft(final StateChangeContext stateChangeContext) {
        Entity productionRecord = stateChangeContext.getOwner();
        productionRecord.setField(ProductionRecordFields.LAST_STATE_CHANGE_FAILS, false);
        productionRecord.setField(ProductionRecordFields.LAST_STATE_CHANGE_FAIL_CAUSE, null);
        stateChangeContext.setOwner(productionRecord);
    }

    public void validationOnAccept(final StateChangeContext stateChangeContext) {
        checkIfRecordOperationProductComponentsWereFilled(stateChangeContext);
        checkIfTimesWereFilled(stateChangeContext);
        checkIfExistsFinalRecord(stateChangeContext);
    }

    public void onAccept(final StateChangeContext stateChangeContext) {
        final Entity productionRecord = stateChangeContext.getOwner();
        updateBasicProductionCounting(productionRecord, new Addition());
        setOrderDoneQuantity(productionRecord);
        closeOrder(stateChangeContext);
    }

    public void onChangeFromAcceptedToDeclined(final StateChangeContext stateChangeContext) {
        final Entity productionRecord = stateChangeContext.getOwner();
        updateBasicProductionCounting(productionRecord, new Substraction());
        setOrderDoneQuantity(productionRecord);
    }

    private void checkIfRecordOperationProductComponentsWereFilled(final StateChangeContext stateChangeContext) {
        final Entity productionRecord = stateChangeContext.getOwner();

        if (!checkIfUsedQuantitiesWereFilled(productionRecord, ProductionRecordFields.RECORD_OPERATION_PRODUCT_IN_COMPONENTS)
                && !checkIfUsedQuantitiesWereFilled(productionRecord,
                        ProductionRecordFields.RECORD_OPERATION_PRODUCT_OUT_COMPONENTS)) {
            stateChangeContext
                    .addValidationError("productionCounting.productionRecord.messages.error.recordOperationProductComponentsNotFilled");
        }
    }

    private boolean checkIfUsedQuantitiesWereFilled(final Entity productionRecord, final String modelName) {
        final SearchCriteriaBuilder searchBuilder = productionRecord.getHasManyField(modelName).find()
                .add(SearchRestrictions.isNotNull(L_USED_QUANTITY));

        return (searchBuilder.list().getTotalNumberOfEntities() != 0);
    }

    private void checkIfTimesWereFilled(final StateChangeContext stateChangeContext) {
        final Entity productionRecord = stateChangeContext.getOwner();
        Integer machineTime = productionRecord.getIntegerField(ProductionRecordFields.MACHINE_TIME);
        Integer laborTime = productionRecord.getIntegerField(ProductionRecordFields.LABOR_TIME);

        if ((machineTime == null) || (laborTime == null)) {
            stateChangeContext.addValidationError("productionCounting.productionRecord.messages.error.timesNotFilled");
        }
    }

    private void checkIfExistsFinalRecord(final StateChangeContext stateChangeContext) {
        final Entity productionRecord = stateChangeContext.getOwner();
        final Entity order = productionRecord.getBelongsToField(ORDER);
        final String typeOfProductionRecording = order.getStringField(TYPE_OF_PRODUCTION_RECORDING);

        final SearchCriteriaBuilder searchBuilder = productionRecord.getDataDefinition().find();
        searchBuilder.add(SearchRestrictions.eq(STATE, ACCEPTED.getStringValue()));
        searchBuilder.add(SearchRestrictions.belongsTo(ORDER, order));
        searchBuilder.add(SearchRestrictions.eq(LAST_RECORD, true));

        if (FOR_EACH.getStringValue().equals(typeOfProductionRecording)) {
            searchBuilder.add(SearchRestrictions.belongsTo(TECHNOLOGY_INSTANCE_OPERATION_COMPONENT,
                    productionRecord.getBelongsToField(TECHNOLOGY_INSTANCE_OPERATION_COMPONENT)));
        }
        if (searchBuilder.list().getTotalNumberOfEntities() != 0) {
            stateChangeContext.addValidationError("productionCounting.productionRecord.messages.error.finalExists");
        }
    }

    public void closeOrder(final StateChangeContext stateChangeContext) {
        final Entity productionRecord = stateChangeContext.getOwner();
        final Entity order = productionRecord.getBelongsToField(ORDER);

        if (!orderClosingHelper.orderShouldBeClosed(productionRecord)) {
            return;
        }
        if (order.getStringField(STATE).equals(COMPLETED.getStringValue())) {
            stateChangeContext.addMessage("productionCounting.order.orderIsAlreadyClosed", StateMessageType.INFO, false);
            return;
        }
        final StateChangeContext orderStateChangeContext = stateChangeContextBuilder.build(
                orderStateChangeAspect.getChangeEntityDescriber(), order, OrderState.COMPLETED.getStringValue());
        orderStateChangeAspect.changeState(orderStateChangeContext);
        Entity orderFromDB = order.getDataDefinition().get(orderStateChangeContext.getOwner().getId());
        if (orderFromDB.getStringField(STATE).equals(COMPLETED.getStringValue())) {
            stateChangeContext.addMessage("productionCounting.order.orderClosed", StateMessageType.INFO, false);
        } else if (StateChangeStatus.PAUSED.equals(orderStateChangeContext.getStatus())) {
            stateChangeContext.addMessage("productionCounting.order.orderWillBeClosedAfterExtSync", StateMessageType.INFO, false);
        } else {
            stateChangeContext.addMessage("productionCounting.order.orderCannotBeClosed", StateMessageType.FAILURE, false);

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

                stateChangeContext.addValidationError("orders.order.orderStates.error", errorMessages.toString());
            }
        }
    }

    private void setOrderDoneQuantity(final Entity productionRecord) {
        final Entity order = productionRecord.getBelongsToField(ORDER);

        Entity product = order.getBelongsToField(OrderFields.PRODUCT);
        product = product.getDataDefinition().get(product.getId());

        final List<Entity> basicProductionCountings = dataDefinitionService
                .get(BasicProductionCountingConstants.PLUGIN_IDENTIFIER,
                        BasicProductionCountingConstants.MODEL_BASIC_PRODUCTION_COUNTING).find()
                .add(SearchRestrictions.belongsTo(ORDER, order)).add(SearchRestrictions.belongsTo(PRODUCT, product)).list()
                .getEntities();

        BigDecimal producedQuantity = BigDecimal.ZERO;

        for (Entity basicProductionCounting : basicProductionCountings) {
            BigDecimal qty = (BigDecimal) basicProductionCounting.getField(L_PRODUCED_QUANTITY);
            if (qty == null) {
                qty = BigDecimal.ZERO;
            }
            producedQuantity = producedQuantity.add(qty, numberService.getMathContext());
        }

        order.setField("doneQuantity", producedQuantity);

        order.getDataDefinition().save(order);

    }

    private Entity getBasicProductionCounting(final Entity productIn, final List<Entity> productionCountings) {
        Entity product = productIn.getBelongsToField(PRODUCT);

        for (Entity productionCounting : productionCountings) {
            if (productionCounting.getBelongsToField(PRODUCT).getId().equals(product.getId())) {
                return productionCounting;
            }
        }

        throw new IllegalStateException("No basic production counting found for product");
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

    private void updateBasicProductionCounting(final Entity productionRecord, final Operation operation) {
        final Entity order = productionRecord.getBelongsToField(ORDER);

        final List<Entity> basicProductionCountings = dataDefinitionService
                .get(BasicProductionCountingConstants.PLUGIN_IDENTIFIER,
                        BasicProductionCountingConstants.MODEL_BASIC_PRODUCTION_COUNTING).find()
                .add(SearchRestrictions.belongsTo(ORDER, order)).list().getEntities();

        final List<Entity> productsIn = productionRecord.getHasManyField(RECORD_OPERATION_PRODUCT_IN_COMPONENTS);
        final List<Entity> productsOut = productionRecord.getHasManyField(RECORD_OPERATION_PRODUCT_OUT_COMPONENTS);

        for (Entity productIn : productsIn) {
            Entity basicProductionCounting;

            try {
                basicProductionCounting = getBasicProductionCounting(productIn, basicProductionCountings);
            } catch (IllegalStateException e) {
                continue;
            }

            final BigDecimal usedQuantity = (BigDecimal) basicProductionCounting.getField(L_USED_QUANTITY);
            final BigDecimal productQuantity = (BigDecimal) productIn.getField(L_USED_QUANTITY);
            final BigDecimal result = operation.perform(usedQuantity, productQuantity);
            basicProductionCounting.setField(L_USED_QUANTITY, result);
            basicProductionCounting = basicProductionCounting.getDataDefinition().save(basicProductionCounting);
        }

        for (Entity productOut : productsOut) {
            Entity productionCounting;

            try {
                productionCounting = getBasicProductionCounting(productOut, basicProductionCountings);
            } catch (IllegalStateException e) {
                continue;
            }

            final BigDecimal usedQuantity = (BigDecimal) productionCounting.getField(L_PRODUCED_QUANTITY);
            final BigDecimal productQuantity = (BigDecimal) productOut.getField(L_USED_QUANTITY);
            final BigDecimal result = operation.perform(usedQuantity, productQuantity);
            productionCounting.setField(L_PRODUCED_QUANTITY, result);
            productionCounting = productionCounting.getDataDefinition().save(productionCounting);
        }
    }

}
