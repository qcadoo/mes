/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.3
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
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ComponentState.MessageType;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.api.utils.TimeConverterService;

@Service
public class OrderTimePredictionService {

    private static final String TECHNOLOGY_COMPONENT = "technology";

    private static final String QUANTITY_COMPONENT = "quantity";

    private static final String DATE_FROM_COMPONENT = "dateFrom";

    private static final String DATE_TO_COMPONENT = "dateTo";

    private static final String REALIZATION_TIME_COMPONENT = "realizationTime";

    @Autowired
    private OrderRealizationTimeService orderRealizationTimeService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private ShiftsServiceImpl shiftsService;

    @Autowired
    private TimeConverterService timeConverterService;

    public void setFieldDisable(final ViewDefinitionState viewDefinitionState) {
        FieldComponent technology = (FieldComponent) viewDefinitionState.getComponentByReference(TECHNOLOGY_COMPONENT);

        FieldComponent quantity = (FieldComponent) viewDefinitionState.getComponentByReference(QUANTITY_COMPONENT);
        FieldComponent dateFrom = (FieldComponent) viewDefinitionState.getComponentByReference(DATE_FROM_COMPONENT);
        FieldComponent dateTo = (FieldComponent) viewDefinitionState.getComponentByReference(DATE_TO_COMPONENT);
        FieldComponent realizationTime = (FieldComponent) viewDefinitionState.getComponentByReference(REALIZATION_TIME_COMPONENT);

        WindowComponent window = (WindowComponent) viewDefinitionState.getComponentByReference("window");
        RibbonActionItem countTimeOfTechnology = window.getRibbon().getGroupByName("timeOfTechnology")
                .getItemByName("countTimeOfTechnology");

        quantity.setRequired(true);
        dateFrom.setRequired(true);
        technology.setRequired(true);

        quantity.setEnabled(true);
        dateFrom.setEnabled(true);
        dateTo.setEnabled(false);
        realizationTime.setEnabled(false);
        countTimeOfTechnology.setEnabled(false);

        quantity.requestComponentUpdateState();
        dateFrom.requestComponentUpdateState();
        dateTo.requestComponentUpdateState();
        technology.requestComponentUpdateState();
        realizationTime.requestComponentUpdateState();
    }

    public void clearAllField(final ViewDefinitionState viewDefinitionState, final ComponentState state, final String[] args) {
        FieldComponent technology = (FieldComponent) viewDefinitionState.getComponentByReference(TECHNOLOGY_COMPONENT);

        FieldComponent quantity = (FieldComponent) viewDefinitionState.getComponentByReference(QUANTITY_COMPONENT);
        FieldComponent dateFrom = (FieldComponent) viewDefinitionState.getComponentByReference(DATE_FROM_COMPONENT);
        FieldComponent dateTo = (FieldComponent) viewDefinitionState.getComponentByReference(DATE_TO_COMPONENT);
        FieldComponent realizationTime = (FieldComponent) viewDefinitionState.getComponentByReference(REALIZATION_TIME_COMPONENT);

        quantity.setFieldValue("");
        dateFrom.setFieldValue("");
        dateTo.setFieldValue("");
        realizationTime.setFieldValue("");
        technology.setFieldValue("");
    }

    @Transactional
    public void changeRealizationTime(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {

        FieldComponent technologyLookup = (FieldComponent) viewDefinitionState.getComponentByReference(TECHNOLOGY_COMPONENT);

        FieldComponent plannedQuantity = (FieldComponent) viewDefinitionState.getComponentByReference(QUANTITY_COMPONENT);
        FieldComponent dateFrom = (FieldComponent) viewDefinitionState.getComponentByReference(DATE_FROM_COMPONENT);
        FieldComponent dateTo = (FieldComponent) viewDefinitionState.getComponentByReference(DATE_TO_COMPONENT);
        FieldComponent realizationTime = (FieldComponent) viewDefinitionState.getComponentByReference(REALIZATION_TIME_COMPONENT);

        if (!StringUtils.hasText((String) plannedQuantity.getFieldValue())
                || !StringUtils.hasText((String) dateFrom.getFieldValue())) {
            realizationTime.setFieldValue(null);
            dateTo.setFieldValue(null);
        }

        BigDecimal quantity = orderRealizationTimeService.getBigDecimalFromField(plannedQuantity.getFieldValue(),
                viewDefinitionState.getLocale());

        if (quantity.intValue() < 0) {
            realizationTime.setFieldValue(null);
            dateTo.setFieldValue(null);
            return;
        }

        int maxPathTime = 0;

        if (technologyLookup.getFieldValue() == null) {
            realizationTime.setFieldValue(null);
            dateTo.setFieldValue(null);
            return;
        }

        Entity technology = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_TECHNOLOGY).get((Long) technologyLookup.getFieldValue());

        maxPathTime = orderRealizationTimeService.estimateRealizationTimeForOperation(
                technology.getTreeField("operationComponents").getRoot(), quantity);

        if (maxPathTime > OrderRealizationTimeService.MAX_REALIZATION_TIME) {
            state.addMessage("orders.validate.global.error.RealizationTimeIsToLong", MessageType.FAILURE);
            realizationTime.setFieldValue(null);
            dateTo.setFieldValue(null);
        } else {
            realizationTime.setFieldValue(maxPathTime);
            Date startTime = timeConverterService.getDateFromField(dateFrom.getFieldValue());
            Date stopTime = shiftsService.findDateToForOrder(startTime, maxPathTime);

            if (stopTime != null) {
                startTime = shiftsService.findDateFromForOrder(stopTime, maxPathTime);
            }

            if (startTime == null) {
                dateFrom.setFieldValue(null);
            } else {
                dateFrom.setFieldValue(orderRealizationTimeService.setDateToField(startTime));

            }
            if (stopTime == null) {
                dateTo.setFieldValue(null);
            } else {
                dateTo.setFieldValue(orderRealizationTimeService.setDateToField(stopTime));
            }
        }
    }

    public void disableRealizationTime(final ViewDefinitionState viewDefinitionState) {
        viewDefinitionState.getComponentByReference(REALIZATION_TIME_COMPONENT).setEnabled(false);
    }

    public void clearFieldValue(final ViewDefinitionState viewDefinitionState, final ComponentState state, final String[] args) {
        state.setFieldValue(null);
    }
}
