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

import static com.qcadoo.mes.orders.constants.OrderFields.CORRECTED_DATE_FROM;
import static com.qcadoo.mes.orders.constants.OrderFields.CORRECTED_DATE_TO;
import static com.qcadoo.mes.orders.constants.OrderFields.DATE_FROM;
import static com.qcadoo.mes.orders.constants.OrderFields.DATE_TO;
import static com.qcadoo.mes.orders.constants.OrderFields.EFFECTIVE_DATE_FROM;
import static com.qcadoo.mes.orders.constants.OrderFields.EFFECTIVE_DATE_TO;
import static com.qcadoo.mes.orders.constants.OrderFields.FINISH_DATE;
import static com.qcadoo.mes.orders.constants.OrderFields.REASON_TYPE_CORRECTION_DATE_FROM;
import static com.qcadoo.mes.orders.constants.OrderFields.REASON_TYPE_CORRECTION_DATE_TO;
import static com.qcadoo.mes.orders.constants.OrderFields.START_DATE;
import static com.qcadoo.mes.orders.constants.OrderFields.STATE;
import static com.qcadoo.mes.orders.states.constants.OrderState.ABANDONED;
import static com.qcadoo.mes.orders.states.constants.OrderState.ACCEPTED;
import static com.qcadoo.mes.orders.states.constants.OrderState.COMPLETED;
import static com.qcadoo.mes.orders.states.constants.OrderState.IN_PROGRESS;
import static com.qcadoo.mes.orders.states.constants.OrderState.PENDING;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.orders.states.constants.OrderState;
import com.qcadoo.mes.orders.states.constants.OrderStateChangeDescriber;
import com.qcadoo.mes.states.service.StateChangeEntityBuilder;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;

@Service
public class OrderHooks {

    public static final long SECOND_MILLIS = 1000;

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
        if (parameter.getBooleanField("reasonNeededWhenCorrectingDateFrom")
                && entity.getField(OrderFields.CORRECTED_DATE_FROM) != null
                && entity.getStringField(REASON_TYPE_CORRECTION_DATE_FROM) == null) {
            entity.addError(dataDefinition.getField(REASON_TYPE_CORRECTION_DATE_FROM),
                    "orders.order.commentReasonTypeCorrectionDateFrom.isRequired");
            return false;
        }
        return true;
    }

    public boolean checkReasonNeededWhenDelayedEffectiveDateToIfIsRequired(final DataDefinition dataDefinition,
            final Entity entity) {
        Entity parameter = parameterService.getParameter();
        if (parameter.getBooleanField("reasonNeededWhenCorrectingDateTo")
                && entity.getField(OrderFields.CORRECTED_DATE_TO) != null
                && entity.getStringField(REASON_TYPE_CORRECTION_DATE_TO) == null) {
            entity.addError(dataDefinition.getField(REASON_TYPE_CORRECTION_DATE_TO),
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

}
