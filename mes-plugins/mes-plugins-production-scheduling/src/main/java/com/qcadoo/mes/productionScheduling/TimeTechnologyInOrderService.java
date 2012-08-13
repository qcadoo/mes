/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.7
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
package com.qcadoo.mes.productionScheduling;

import java.math.BigDecimal;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.qcadoo.mes.basic.ShiftsServiceImpl;
import com.qcadoo.mes.operationTimeCalculations.OrderRealizationTimeService;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ComponentState.MessageType;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.utils.TimeConverterService;

@Service
public class TimeTechnologyInOrderService {

    private static final String FORM_COMPONENT = "form";

    private static final String QUANTITY_COMPONENT = "quantity";

    private static final String START_TIME_COMPONENT = "startTime";

    private static final String STOP_TIME_COMPONENT = "stopTime";

    private static final String REALIZATION_TIME_COMPONENT = "realizationTime";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private OrderRealizationTimeService orderRealizationTimeService;

    @Autowired
    private ShiftsServiceImpl shiftsService;

    @Autowired
    private TimeConverterService timeConverterService;

    @Transactional
    public void changeRealizationTime(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        FormComponent form = (FormComponent) viewDefinitionState.getComponentByReference(FORM_COMPONENT);

        FieldComponent plannedQuantity = (FieldComponent) viewDefinitionState.getComponentByReference(QUANTITY_COMPONENT);
        FieldComponent startTime = (FieldComponent) viewDefinitionState.getComponentByReference(START_TIME_COMPONENT);
        FieldComponent stopTime = (FieldComponent) viewDefinitionState.getComponentByReference(STOP_TIME_COMPONENT);
        FieldComponent realizationTime = (FieldComponent) viewDefinitionState.getComponentByReference(REALIZATION_TIME_COMPONENT);

        if (!StringUtils.hasText((String) plannedQuantity.getFieldValue())) {
            plannedQuantity.addMessage("productionScheduling.error.fieldRequired", MessageType.FAILURE);
            return;
        }

        if (!StringUtils.hasText((String) startTime.getFieldValue())) {
            startTime.addMessage("productionScheduling.error.fieldRequired", MessageType.FAILURE);
            return;
        }

        if (!StringUtils.hasText((String) plannedQuantity.getFieldValue())
                || !StringUtils.hasText((String) startTime.getFieldValue())) {
            realizationTime.setFieldValue(null);
            stopTime.setFieldValue(null);
            return;
        }

        BigDecimal quantity = orderRealizationTimeService.getBigDecimalFromField(plannedQuantity.getFieldValue(),
                viewDefinitionState.getLocale());

        if (quantity.intValue() < 0) {
            realizationTime.setFieldValue(null);
            stopTime.setFieldValue(null);
            return;
        }

        int maxPathTime = 0;

        Entity order = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).get(
                form.getEntityId());

        Entity productionLine = order.getBelongsToField("productionLine");

        if (productionLine == null) {
            state.addMessage("orders.validate.global.error.noProductionLine", MessageType.FAILURE);
            return;
        }

        maxPathTime = orderRealizationTimeService.estimateOperationTimeConsumption(
                order.getTreeField("technologyInstanceOperationComponents").getRoot(),
                orderRealizationTimeService.getBigDecimalFromField(plannedQuantity.getFieldValue(),
                        viewDefinitionState.getLocale()), productionLine);

        if (maxPathTime > OrderRealizationTimeService.MAX_REALIZATION_TIME) {
            state.addMessage("orders.validate.global.error.RealizationTimeIsToLong", MessageType.FAILURE);
            realizationTime.setFieldValue(null);
            stopTime.setFieldValue(null);
        } else {
            realizationTime.setFieldValue(maxPathTime);

            Date dateFrom = timeConverterService.getDateFromField(startTime.getFieldValue());
            Date dateTo = shiftsService.findDateToForOrder(dateFrom, maxPathTime);

            if (dateTo != null) {
                dateFrom = shiftsService.findDateFromForOrder(dateTo, maxPathTime);
            }

            if (dateFrom == null) {
                startTime.setFieldValue(null);
            } else {
                startTime.setFieldValue(orderRealizationTimeService.setDateToField(dateFrom));
            }

            if (dateTo == null) {
                stopTime.setFieldValue(null);
            } else {
                stopTime.setFieldValue(orderRealizationTimeService.setDateToField(dateTo));

            }
        }
        plannedQuantity.requestComponentUpdateState();
        startTime.requestComponentUpdateState();
        stopTime.requestComponentUpdateState();
        realizationTime.requestComponentUpdateState();
    }

    public void clearFieldValue(final ViewDefinitionState viewDefinitionState, final ComponentState state, final String[] args) {
        state.setFieldValue(null);
    }

}
