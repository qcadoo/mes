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
import static com.qcadoo.mes.orders.constants.OrderFields.REASON_TYPES_CORRECTION_DATE_FROM;
import static com.qcadoo.mes.orders.constants.OrderFields.REASON_TYPES_CORRECTION_DATE_TO;
import static com.qcadoo.mes.orders.constants.OrderFields.REMAINING_AMOUNT_OF_PRODUCT_TO_PRODUCE;
import static com.qcadoo.mes.orders.constants.OrderFields.START_DATE;
import static com.qcadoo.mes.orders.constants.OrderFields.STATE;
import static com.qcadoo.mes.orders.constants.OrderFields.TYPE_OF_CORRECTION_CAUSES;
import static com.qcadoo.mes.orders.constants.ParameterFieldsO.REASON_NEEDED_WHEN_CORRECTING_DATE_FROM;
import static com.qcadoo.mes.orders.constants.ParameterFieldsO.REASON_NEEDED_WHEN_CORRECTING_DATE_TO;
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

import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.orders.states.constants.OrderState;
import com.qcadoo.mes.orders.states.constants.OrderStateChangeDescriber;
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

    public boolean checkReasonNeededWhenDelayedEffectiveDateFromIfIsRequired(final DataDefinition dataDefinition,
            final Entity entity) {
        Entity parameter = parameterService.getParameter();
        if (parameter.getBooleanField(REASON_NEEDED_WHEN_CORRECTING_DATE_FROM) && entity.getField(CORRECTED_DATE_FROM) != null
                && entity.getHasManyField(REASON_TYPES_CORRECTION_DATE_FROM).isEmpty()) {
            entity.addError(dataDefinition.getField(REASON_TYPES_CORRECTION_DATE_FROM),
                    "orders.order.commentReasonTypeCorrectionDateFrom.isRequired");
            return false;
        }
        return true;
    }

    public boolean checkReasonNeededWhenDelayedEffectiveDateToIfIsRequired(final DataDefinition dataDefinition,
            final Entity entity) {
        Entity parameter = parameterService.getParameter();
        if (parameter.getBooleanField(REASON_NEEDED_WHEN_CORRECTING_DATE_TO) && entity.getField(CORRECTED_DATE_TO) != null
                && entity.getHasManyField(REASON_TYPES_CORRECTION_DATE_TO).isEmpty()) {
            entity.addError(dataDefinition.getField(REASON_TYPES_CORRECTION_DATE_TO),
                    "orders.order.commentReasonTypeCorrectionDateTo.isRequired");
            return false;
        }
        return true;
    }

    private void setStartDate(final Entity entity) {
        if (entity.getId() == null) {
            return;
        }
        if (entity.getField(START_DATE) == null) {
            return;
        }
        Entity order = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).get(
                entity.getId());
        String state = entity.getStringField(STATE);
        Date startDate = choppingOffMiliseconds(entity.getField(START_DATE));
        Date startDateDB = new Date();
        if (order.getField(START_DATE) != null) {
            startDateDB = choppingOffMiliseconds(order.getField(START_DATE));
        }
        if (PENDING.getStringValue().equals(state) && !startDate.equals(startDateDB)) {
            entity.setField(DATE_FROM, entity.getField(START_DATE));
        }
        if (IN_PROGRESS.getStringValue().equals(state) && !startDate.equals(startDateDB)) {
            entity.setField(EFFECTIVE_DATE_FROM, entity.getField(START_DATE));
        }
        if ((ACCEPTED.getStringValue().equals(state) || ABANDONED.getStringValue().equals(state) || IN_PROGRESS.getStringValue()
                .equals(state)) && !startDateDB.equals(startDate)) {
            entity.setField(CORRECTED_DATE_FROM, entity.getField(START_DATE));
        }
    }

    private void setEndDate(final Entity entity) {
        if (entity.getId() == null) {
            return;
        }
        if (entity.getField(FINISH_DATE) == null) {
            return;
        }
        Entity order = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).get(
                entity.getId());
        String state = entity.getStringField(STATE);
        Date finishDate = choppingOffMiliseconds(entity.getField(FINISH_DATE));
        Date finishDateDB = new Date();
        if (order.getField(FINISH_DATE) != null) {
            finishDateDB = choppingOffMiliseconds(order.getField(FINISH_DATE));
        }
        if (PENDING.getStringValue().equals(state) && !finishDateDB.equals(finishDate)) {
            entity.setField(DATE_TO, entity.getField(FINISH_DATE));
        }
        if (COMPLETED.getStringValue().equals(state) && !finishDateDB.equals(finishDate)) {
            entity.setField(CORRECTED_DATE_TO, entity.getField(START_DATE));
        }
        if ((ACCEPTED.getStringValue().equals(state) || ABANDONED.getStringValue().equals(state) || IN_PROGRESS.getStringValue()
                .equals(state)) && !finishDateDB.equals(finishDate)) {
            entity.setField(CORRECTED_DATE_TO, entity.getField(FINISH_DATE));
        }
    }

    private void fillStartDate(final Entity order) {
        if (order.getField(EFFECTIVE_DATE_FROM) != null) {
            order.setField(START_DATE, order.getField(EFFECTIVE_DATE_FROM));
        } else if (order.getField(CORRECTED_DATE_FROM) != null) {
            order.setField(START_DATE, order.getField(CORRECTED_DATE_FROM));
        } else if (order.getField(DATE_FROM) != null) {
            order.setField(START_DATE, order.getField(DATE_FROM));
        } else {
            order.setField(DATE_FROM, order.getField(START_DATE));
        }
    }

    private void fillEndDate(final Entity order) {
        if (order.getField(EFFECTIVE_DATE_TO) != null) {
            order.setField(FINISH_DATE, order.getField(EFFECTIVE_DATE_TO));
        } else if (order.getField(CORRECTED_DATE_TO) != null) {
            order.setField(FINISH_DATE, order.getField(CORRECTED_DATE_TO));
        } else if (order.getField(DATE_TO) != null) {
            order.setField(FINISH_DATE, order.getField(DATE_TO));
        } else {
            order.setField(DATE_TO, order.getField(FINISH_DATE));
        }
    }

    private Date choppingOffMiliseconds(final Object date) {
        return new Date(((Date) date).getTime() / SECOND_MILLIS);
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

    public void setSommissionedPlannedQuantity(final DataDefinition orderDD, final Entity entity) {
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

        return true;
    }

}
