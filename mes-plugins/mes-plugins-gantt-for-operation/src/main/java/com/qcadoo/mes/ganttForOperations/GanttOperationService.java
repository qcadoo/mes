/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.6
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
package com.qcadoo.mes.ganttForOperations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.productionScheduling.constants.ProductionSchedulingConstants;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ComponentState.MessageType;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.ribbon.RibbonActionItem;

@Service
public class GanttOperationService {

    private static final String GANTT_FIELD = "gantt";

    private static final String DATE_TO_FIELD = "dateTo";

    private static final String DATE_FROM_FIELD = "dateFrom";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    private Long orderId;

    public void refereshGanttChart(final ViewDefinitionState viewDefinitionState, final ComponentState triggerState,
            final String[] args) {
        viewDefinitionState.getComponentByReference(GANTT_FIELD).performEvent(viewDefinitionState, "refresh");
    }

    public void disableFormWhenNoOrderSelected(final ViewDefinitionState viewDefinitionState) {
        if (viewDefinitionState.getComponentByReference(GANTT_FIELD).getFieldValue() == null) {
            viewDefinitionState.getComponentByReference(DATE_FROM_FIELD).setEnabled(false);
            viewDefinitionState.getComponentByReference(DATE_TO_FIELD).setEnabled(false);
        } else {
            viewDefinitionState.getComponentByReference(DATE_FROM_FIELD).setEnabled(true);
            viewDefinitionState.getComponentByReference(DATE_TO_FIELD).setEnabled(true);
        }
    }

    public void showOperationsGantt(final ViewDefinitionState viewDefinitionState, final ComponentState triggerState,
            final String[] args) {
        orderId = (Long) triggerState.getFieldValue();

        if (orderId != null) {
            String url = "../page/ganttForOperations/ganttForOperations.html?context={\"gantt.orderId\":\"" + orderId + "\"}";

            viewDefinitionState.redirectTo(url, false, true);
        }
    }

    public void checkDoneCalculate(final ViewDefinitionState viewDefinitionState) {
        FormComponent form = (FormComponent) viewDefinitionState.getComponentByReference("form");
        Entity order = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).get(orderId);

        Integer realizationTime = (Integer) order.getField("realizationTime");
        if (realizationTime == null) {
            form.addMessage("orders.order.report.realizationTime", MessageType.INFO, false);
        }
    }

    public void fillTitleLabel(final ViewDefinitionState viewDefinitionState) {
        FieldComponent title = (FieldComponent) viewDefinitionState.getComponentByReference("title");
        Entity order = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).get(orderId);
        String number = order.getField("number").toString();
        String name = order.getField("name").toString();

        title.setFieldValue(name + " - " + number);
        title.requestComponentUpdateState();
    }

    public boolean isRealizationTimeGenerated(final ViewDefinitionState viewDefinitionState) {
        FormComponent form = (FormComponent) viewDefinitionState.getComponentByReference("form");
        if (form.getEntityId() == null) {
            return false;
        }
        Entity order = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).get(
                form.getEntityId());
        Integer realizationTime = (Integer) order.getField("realizationTime");
        return !(realizationTime == null || realizationTime == 0);
    }

    public void disableCalendarButtonWhenRealizationTimeNotGenerated(final ViewDefinitionState view) {
        FormComponent form = (FormComponent) view.getComponentByReference("form");
        if (form.getEntity() == null) {
            return;
        }

        WindowComponent window = (WindowComponent) view.getComponentByReference("window");
        RibbonActionItem showOnCalendarButton = window.getRibbon()
                .getGroupByName(ProductionSchedulingConstants.VIEW_RIBBON_ACTION_ITEM_GROUP)
                .getItemByName(ProductionSchedulingConstants.VIEW_RIBBON_ACTION_ITEM_NAME);
        if (isRealizationTimeGenerated(view)) {
            showOnCalendarButton.setEnabled(true);
        } else {
            showOnCalendarButton.setEnabled(false);
        }
        showOnCalendarButton.requestUpdate(true);
    }

}
