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
package com.qcadoo.mes.orders.hooks;

import static com.qcadoo.mes.orders.constants.OrderFields.AMOUNT_OF_PRODUCT_PRODUCED;
import static com.qcadoo.mes.orders.constants.OrderFields.COMMENT_REASON_TYPE_CORRECTION_DATE_FROM;
import static com.qcadoo.mes.orders.constants.OrderFields.COMMENT_REASON_TYPE_CORRECTION_DATE_TO;
import static com.qcadoo.mes.orders.constants.OrderFields.COMMENT_REASON_TYPE_DEVIATIONS_QUANTITY;
import static com.qcadoo.mes.orders.constants.OrderFields.COMMISSIONED_CORRECTED_QUANTITY;
import static com.qcadoo.mes.orders.constants.OrderFields.COMMISSIONED_PLANNED_QUANTITY;
import static com.qcadoo.mes.orders.constants.OrderFields.CORRECTED_DATE_FROM;
import static com.qcadoo.mes.orders.constants.OrderFields.CORRECTED_DATE_TO;
import static com.qcadoo.mes.orders.constants.OrderFields.DATE_FROM;
import static com.qcadoo.mes.orders.constants.OrderFields.DATE_TO;
import static com.qcadoo.mes.orders.constants.OrderFields.DESCRIPTION;
import static com.qcadoo.mes.orders.constants.OrderFields.DONE_QUANTITY;
import static com.qcadoo.mes.orders.constants.OrderFields.EFFECTIVE_DATE_FROM;
import static com.qcadoo.mes.orders.constants.OrderFields.EFFECTIVE_DATE_TO;
import static com.qcadoo.mes.orders.constants.OrderFields.EXTERNAL_NUMBER;
import static com.qcadoo.mes.orders.constants.OrderFields.EXTERNAL_SYNCHRONIZED;
import static com.qcadoo.mes.orders.constants.OrderFields.FINISH_DATE;
import static com.qcadoo.mes.orders.constants.OrderFields.PLANNED_QUANTITY;
import static com.qcadoo.mes.orders.constants.OrderFields.REMAINING_AMOUNT_OF_PRODUCT_TO_PRODUCE;
import static com.qcadoo.mes.orders.constants.OrderFields.START_DATE;
import static com.qcadoo.mes.orders.constants.OrderFields.STATE;
import static com.qcadoo.mes.orders.constants.OrderFields.TYPE_OF_CORRECTION_CAUSES;
import static com.qcadoo.mes.orders.constants.ParameterFieldsO.REASON_NEEDED_WHEN_CORRECTING_THE_REQUESTED_VOLUME;
import static com.qcadoo.mes.orders.states.constants.OrderState.ABANDONED;
import static com.qcadoo.mes.orders.states.constants.OrderState.ACCEPTED;
import static com.qcadoo.mes.orders.states.constants.OrderState.COMPLETED;
import static com.qcadoo.mes.orders.states.constants.OrderState.INTERRUPTED;
import static com.qcadoo.mes.orders.states.constants.OrderState.IN_PROGRESS;
import static com.qcadoo.mes.orders.states.constants.OrderState.PENDING;

import java.math.BigDecimal;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.commons.dateTime.DateRange;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.basic.ProductService;
import com.qcadoo.mes.orders.OrderService;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.orders.constants.ParameterFieldsO;
import com.qcadoo.mes.orders.states.constants.OrderState;
import com.qcadoo.mes.orders.states.constants.OrderStateChangeDescriber;
import com.qcadoo.mes.orders.util.OrderDatesService;
import com.qcadoo.mes.states.service.StateChangeEntityBuilder;
import com.qcadoo.model.api.BigDecimalUtils;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;

@Service
public class OrderHooks {

    public static final long SECOND_MILLIS = 1000;

    public static final String L_TYPE_OF_PRODUCTION_RECORDING = "typeOfProductionRecording";

    @Autowired
    private ParameterService parameterService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private StateChangeEntityBuilder stateChangeEntityBuilder;

    @Autowired
    private OrderStateChangeDescriber describer;

    @Autowired
    private OrderDatesService orderDatesService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private ProductService productService;

    public boolean onValidate(final DataDefinition orderDD, final Entity order) {
        Entity parameter = parameterService.getParameter();
        return orderService.checkOrderDates(orderDD, order) && orderService.checkOrderPlannedQuantity(orderDD, order)
                && productService.checkIfProductIsNotRemoved(orderDD, order)
                && orderService.checkChosenTechnologyState(orderDD, order) && checkReasonOfStartDateCorrection(parameter, order)
                && checkReasonOfEndDateCorrection(parameter, order)
                && checkReasonOfEffectiveStartDateCorrection(parameter, order)
                && checkReasonOfEffectiveEndDateCorrection(parameter, order);
    }

    public void setInitialState(final DataDefinition dataDefinition, final Entity order) {
        stateChangeEntityBuilder.buildInitial(describer, order, OrderState.PENDING);
    }

    public void copyStartDate(final DataDefinition dataDefinition, final Entity entity) {
        setStartDate(entity);
        fillStartDate(entity);
    }

    public void copyEndDate(final DataDefinition dataDefinition, final Entity entity) {
        setEndDate(entity);
        fillEndDate(entity);
    }

    protected boolean checkReasonOfStartDateCorrection(final Entity parameter, final Entity order) {
        return !parameter.getBooleanField(ParameterFieldsO.REASON_NEEDED_WHEN_CORRECTING_DATE_FROM)
                || checkReasonNeeded(order, OrderFields.CORRECTED_DATE_FROM, OrderFields.REASON_TYPES_CORRECTION_DATE_FROM,
                        "orders.order.commentReasonTypeCorrectionDateFrom.isRequired");
    }

    protected boolean checkReasonOfEndDateCorrection(final Entity parameter, final Entity order) {
        return !parameter.getBooleanField(ParameterFieldsO.REASON_NEEDED_WHEN_CORRECTING_DATE_TO)
                || checkReasonNeeded(order, OrderFields.CORRECTED_DATE_TO, OrderFields.REASON_TYPES_CORRECTION_DATE_TO,
                        "orders.order.commentReasonTypeCorrectionDateTo.isRequired");
    }

    protected boolean checkReasonOfEffectiveStartDateCorrection(final Entity parameter, final Entity order) {
        return !parameter.getBooleanField(ParameterFieldsO.REASON_NEEDED_WHEN_CORRECTING_DATE_FROM)
                || checkReasonNeeded(order, OrderFields.EFFECTIVE_DATE_FROM,
                        OrderFields.REASON_TYPES_DEVIATIONS_OF_EFFECTIVE_START,
                        "orders.order.reasonTypesDeviationsOfEffectiveStart.isRequired");
    }

    protected boolean checkReasonOfEffectiveEndDateCorrection(final Entity parameter, final Entity order) {
        return !parameter.getBooleanField(ParameterFieldsO.REASON_NEEDED_WHEN_CORRECTING_DATE_TO)
                || checkReasonNeeded(order, OrderFields.EFFECTIVE_DATE_TO, OrderFields.REASON_TYPES_DEVIATIONS_OF_EFFECTIVE_END,
                        "orders.order.reasonTypesDeviationsOfEffectiveEnd.isRequired");
    }

    private boolean checkReasonNeeded(final Entity order, final String dateFieldName, final String reasonTypeFieldName,
            final String messageTranslationKey) {
        if (order.getField(dateFieldName) != null && order.getHasManyField(reasonTypeFieldName).isEmpty()) {
            order.addError(order.getDataDefinition().getField(reasonTypeFieldName), messageTranslationKey);
            return false;
        }
        return true;
    }

    // private boolean checkEffectiveDeviation(final Entity parameter, final Entity order) {
    // if (parameter.getBooleanField(ParameterFieldsO.REASON_NEEDED_WHEN_DELAYED_EFFECTIVE_DATE_FROM)) {
    // ParameterFieldsO.DELAYED_EFFECTIVE_DATE_FROM_TIME
    // }
    // if (parameter.getBooleanField(ParameterFieldsO.REASON_NEEDED_WHEN_EARLIER_EFFECTIVE_DATE_FROM)) {
    //
    // }
    // }

    private void setStartDate(final Entity entity) {
        if (entity.getId() == null) {
            return;
        }
        Date startDate = entity.getDateField(START_DATE);
        if (startDate == null) {
            return;
        }
        Entity order = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).get(
                entity.getId());
        String state = entity.getStringField(STATE);
        Date startDateDB = new Date();
        if (order.getDateField(START_DATE) != null) {
            startDateDB = order.getDateField(START_DATE);
        }
        if (PENDING.getStringValue().equals(state) && !startDate.equals(startDateDB)) {
            entity.setField(DATE_FROM, startDate);
        }
        if (IN_PROGRESS.getStringValue().equals(state) && !startDate.equals(startDateDB)) {
            entity.setField(EFFECTIVE_DATE_FROM, startDate);
        }
        if ((ACCEPTED.getStringValue().equals(state) || ABANDONED.getStringValue().equals(state))
                && !startDateDB.equals(startDate)) {
            entity.setField(CORRECTED_DATE_FROM, startDate);
        }
    }

    private void setEndDate(final Entity entity) {
        if (entity.getId() == null) {
            return;
        }
        Date finishDate = entity.getDateField(FINISH_DATE);
        if (finishDate == null) {
            return;
        }
        Entity order = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).get(
                entity.getId());
        String state = entity.getStringField(STATE);
        Date finishDateDB = new Date();
        if (order.getDateField(FINISH_DATE) != null) {
            finishDateDB = order.getDateField(FINISH_DATE);
        }
        if (PENDING.getStringValue().equals(state) && !finishDateDB.equals(finishDate)) {
            entity.setField(DATE_TO, finishDate);
        }
        if (COMPLETED.getStringValue().equals(state) && !finishDateDB.equals(finishDate)) {
            entity.setField(EFFECTIVE_DATE_TO, finishDate);
        }
        if ((ACCEPTED.getStringValue().equals(state) || ABANDONED.getStringValue().equals(state) || IN_PROGRESS.getStringValue()
                .equals(state)) && !finishDateDB.equals(finishDate)) {
            entity.setField(CORRECTED_DATE_TO, finishDate);
        }
    }

    private void fillStartDate(final Entity order) {
        DateRange dateRange = orderDatesService.getDates(order);
        if (dateRange.getFrom() == null) {
            order.setField(OrderFields.DATE_FROM, order.getField(OrderFields.START_DATE));
        } else {
            order.setField(OrderFields.START_DATE, dateRange.getFrom());
        }
    }

    private void fillEndDate(final Entity order) {
        DateRange dateRange = orderDatesService.getDates(order);
        if (dateRange.getTo() == null) {
            order.setField(OrderFields.DATE_TO, order.getField(OrderFields.FINISH_DATE));
        } else {
            order.setField(OrderFields.FINISH_DATE, dateRange.getTo());
        }
    }

    public boolean validateDates(final DataDefinition orderDD, final Entity order) {
        Date effectiveDateFrom = (Date) order.getField(EFFECTIVE_DATE_FROM);
        Date effectiveDateTo = (Date) order.getField(EFFECTIVE_DATE_TO);

        if ((effectiveDateFrom != null) && (effectiveDateTo != null) && effectiveDateTo.before(effectiveDateFrom)) {
            order.addError(orderDD.getField(EFFECTIVE_DATE_TO), "orders.validate.global.error.effectiveDateTo");

            return false;
        }

        return true;
    }

    public void copyProductQuantity(final DataDefinition orderDD, final Entity order) {
        setProductQuantity(order);
    }

    private void setProductQuantity(Entity entity) {
        if (entity.getId() == null) {
            return;
        }

        Entity order = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).get(
                entity.getId());

        BigDecimal plannedQuantity = entity.getDecimalField(PLANNED_QUANTITY);
        BigDecimal commissionedPlannedQuantity = entity.getDecimalField(COMMISSIONED_PLANNED_QUANTITY);
        BigDecimal commissionedCorrectedQuantity = entity.getDecimalField(COMMISSIONED_CORRECTED_QUANTITY);
        BigDecimal plannedQuantityDB = order.getDecimalField(PLANNED_QUANTITY);

        if (plannedQuantityDB.compareTo(plannedQuantity) != 0) {
            String state = entity.getStringField(STATE);

            if (PENDING.getStringValue().equals(state)) {
                entity.setField(COMMISSIONED_PLANNED_QUANTITY, entity.getField(PLANNED_QUANTITY));
            }
            if (ACCEPTED.getStringValue().equals(state) || IN_PROGRESS.getStringValue().equals(state)
                    || INTERRUPTED.getStringValue().equals(state)) {
                entity.setField(COMMISSIONED_CORRECTED_QUANTITY, entity.getField(PLANNED_QUANTITY));
            }

        } else {
            if (BigDecimal.ZERO.compareTo(BigDecimalUtils.convertNullToZero(commissionedCorrectedQuantity)) != 0) {
                entity.setField(PLANNED_QUANTITY, entity.getField(COMMISSIONED_CORRECTED_QUANTITY));
            } else if (BigDecimal.ZERO.compareTo(BigDecimalUtils.convertNullToZero(commissionedPlannedQuantity)) != 0) {
                entity.setField(PLANNED_QUANTITY, entity.getField(COMMISSIONED_PLANNED_QUANTITY));
            }
        }

        BigDecimal doneQuantityDB = order.getDecimalField(DONE_QUANTITY);
        BigDecimal doneQuantity = entity.getDecimalField(DONE_QUANTITY);
        BigDecimal amountOfProductProducedDB = order.getDecimalField(AMOUNT_OF_PRODUCT_PRODUCED);
        BigDecimal amountOfProductProduced = entity.getDecimalField(AMOUNT_OF_PRODUCT_PRODUCED);

        String typeOfProductionRecording = entity.getStringField(L_TYPE_OF_PRODUCTION_RECORDING);

        if (StringUtils.isEmpty(typeOfProductionRecording)) {
            if (BigDecimalUtils.convertNullToZero(doneQuantity).compareTo(BigDecimalUtils.convertNullToZero(doneQuantityDB)) != 0) {
                entity.setField(AMOUNT_OF_PRODUCT_PRODUCED, entity.getField(DONE_QUANTITY));
            } else if (BigDecimalUtils.convertNullToZero(amountOfProductProduced).compareTo(
                    BigDecimalUtils.convertNullToZero(amountOfProductProducedDB)) != 0) {
                entity.setField(DONE_QUANTITY, entity.getField(AMOUNT_OF_PRODUCT_PRODUCED));
            }
        } else {
            entity.setField(AMOUNT_OF_PRODUCT_PRODUCED, entity.getField(DONE_QUANTITY));
        }
    }

    public void onCorrectingTheRequestedVolume(final DataDefinition orderDD, final Entity entity) {
        if (!neededWhenCorrectingTheRequestedVolume()) {
            return;
        }

        if (entity.getId() == null) {
            return;
        }

        String state = entity.getStringField(STATE);

        if (ACCEPTED.getStringValue().equals(state) || IN_PROGRESS.getStringValue().equals(state)
                || INTERRUPTED.getStringValue().equals(state)) {

            Entity order = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).get(
                    entity.getId());

            BigDecimal commissionedCorrectedQuantity = entity.getDecimalField(COMMISSIONED_CORRECTED_QUANTITY);
            BigDecimal commissionedCorrectedQuantityDB = order.getDecimalField(COMMISSIONED_CORRECTED_QUANTITY);

            if ((BigDecimalUtils.convertNullToZero(commissionedCorrectedQuantity).compareTo(
                    BigDecimalUtils.convertNullToZero(commissionedCorrectedQuantityDB)) != 0)
                    && entity.getHasManyField(TYPE_OF_CORRECTION_CAUSES).isEmpty()) {
                entity.addGlobalError("orders.order.correctingQuantity.missingTypeOfCorrectionCauses");
            }
        }
    }

    public boolean neededWhenCorrectingTheRequestedVolume() {
        return parameterService.getParameter().getBooleanField(REASON_NEEDED_WHEN_CORRECTING_THE_REQUESTED_VOLUME);
    }

    public void setCommissionedPlannedQuantity(final DataDefinition orderDD, final Entity entity) {
        entity.setField(COMMISSIONED_PLANNED_QUANTITY, entity.getField(PLANNED_QUANTITY));
    }

    public void changedEnabledDescriptionFieldForSpecificOrderState(final ViewDefinitionState view) {
        final FormComponent form = (FormComponent) view.getComponentByReference("form");
        if (form.getEntityId() == null) {
            return;
        }

        final Entity order = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).get(
                form.getEntityId());

        if (order.getStringField(STATE).equals(OrderState.ACCEPTED.getStringValue())
                || order.getStringField(STATE).equals(OrderState.IN_PROGRESS.getStringValue())
                || order.getStringField(STATE).equals(OrderState.INTERRUPTED.getStringValue())
                || order.getStringField(STATE).equals(OrderState.PENDING.getStringValue())) {
            FieldComponent field = (FieldComponent) view.getComponentByReference(DESCRIPTION);
            field.setEnabled(true);
            field.requestComponentUpdateState();
        }
    }

    public void setProductQuantity(final DataDefinition dataDefinition, final Entity order) {
        if (order == null) {
            return;
        }

        order.setField(COMMISSIONED_PLANNED_QUANTITY, order.getDecimalField(PLANNED_QUANTITY));
        order.setField(COMMISSIONED_CORRECTED_QUANTITY, null);
        order.setField(AMOUNT_OF_PRODUCT_PRODUCED, null);
        order.setField(REMAINING_AMOUNT_OF_PRODUCT_TO_PRODUCE, null);
    }

    public boolean clearOrSetSpecyfiedValueOrderFieldsOnCopy(final DataDefinition dataDefinition, final Entity entity) {
        entity.setField(STATE, OrderState.PENDING.getStringValue());
        entity.setField(EFFECTIVE_DATE_TO, null);
        entity.setField(EFFECTIVE_DATE_FROM, null);
        entity.setField(CORRECTED_DATE_FROM, null);
        entity.setField(CORRECTED_DATE_TO, null);
        entity.setField(DATE_FROM, entity.getDateField(START_DATE));
        entity.setField(DATE_TO, entity.getDateField(FINISH_DATE));
        entity.setField(DONE_QUANTITY, null);
        entity.setField(EXTERNAL_NUMBER, null);
        entity.setField(EXTERNAL_SYNCHRONIZED, true);
        entity.setField(COMMENT_REASON_TYPE_CORRECTION_DATE_FROM, null);
        entity.setField(COMMENT_REASON_TYPE_CORRECTION_DATE_TO, null);
        entity.setField(COMMENT_REASON_TYPE_DEVIATIONS_QUANTITY, null);
        return true;
    }
}
