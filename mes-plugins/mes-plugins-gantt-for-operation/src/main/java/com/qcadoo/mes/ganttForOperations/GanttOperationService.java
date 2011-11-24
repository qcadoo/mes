/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.0
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

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.basic.ShiftsServiceImpl;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ComponentState.MessageType;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;

@Service
public class GanttOperationService {

    @Autowired
    private ShiftsServiceImpl shiftsService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private TranslationService translationService;

    private Long orderId;

    public void refereshGanttChart(final ViewDefinitionState viewDefinitionState, final ComponentState triggerState,
            final String[] args) {
        viewDefinitionState.getComponentByReference("gantt").performEvent(viewDefinitionState, "refresh");
    }

    public void disableFormWhenNoOrderSelected(final ViewDefinitionState viewDefinitionState) {
        if (viewDefinitionState.getComponentByReference("gantt").getFieldValue() == null) {
            viewDefinitionState.getComponentByReference("dateFrom").setEnabled(false);
            viewDefinitionState.getComponentByReference("dateTo").setEnabled(false);
        } else {
            viewDefinitionState.getComponentByReference("dateFrom").setEnabled(true);
            viewDefinitionState.getComponentByReference("dateTo").setEnabled(true);
        }
    }

    public void showOperationsGantt(final ViewDefinitionState viewDefinitionState, final ComponentState triggerState,
            final String[] args) {
        orderId = (Long) triggerState.getFieldValue();

        scheduleOrder(orderId);

        if (orderId != null) {
            String url = "../page/ganttForOperations/ganttForOperations.html?context={\"gantt.orderId\":\"" + orderId + "\"}";

            viewDefinitionState.redirectTo(url, false, true);
        }
    }

    private void scheduleOrder(final Long orderId) {
        Entity order = dataDefinitionService.get("orders", "order").get(orderId);

        if (order == null) {
            return;
        }

        DataDefinition dataDefinition = dataDefinitionService.get("productionScheduling", "orderOperationComponent");

        List<Entity> operations = dataDefinition.find().add(SearchRestrictions.belongsTo("order", order)).list().getEntities();

        Date orderStartDate = null;

        if (order.getField("effectiveDateFrom") != null) {
            orderStartDate = (Date) order.getField("effectiveDateFrom");
        } else if (order.getField("dateFrom") != null) {
            orderStartDate = (Date) order.getField("dateFrom");
        } else {
            return;
        }

        for (Entity operation : operations) {
            Integer offset = (Integer) operation.getField("operationOffSet");
            Integer duration = (Integer) operation.getField("effectiveOperationRealizationTime");

            operation.setField("effectiveDateFrom", null);
            operation.setField("effectiveDateTo", null);

            if (offset == null || duration == null || duration.equals(0)) {
                continue;
            }

            if (offset == 0) {
                offset = 1;
            }

            Date dateFrom = shiftsService.findDateToForOrder(orderStartDate, offset);

            if (dateFrom == null) {
                continue;
            }

            Date dateTo = shiftsService.findDateToForOrder(orderStartDate, offset + duration);

            if (dateTo == null) {
                continue;
            }

            operation.setField("effectiveDateFrom", dateFrom);
            operation.setField("effectiveDateTo", dateTo);
        }

        for (Entity operation : operations) {
            dataDefinition.save(operation);
        }

    }

    public void checkDoneCalculate(final ViewDefinitionState viewDefinitionState) {
        ComponentState form = (ComponentState) viewDefinitionState.getComponentByReference("form");
        Entity order = dataDefinitionService.get("orders", "order").get(orderId);

        Object realizationTime = order.getField("realizationTime");
        if (realizationTime == null || "0".equals(realizationTime.toString()) || "".equals(realizationTime.toString())) {
            form.addMessage(translationService.translate("orders.order.report.realizationTime", viewDefinitionState.getLocale()),
                    MessageType.INFO, false);
        }
    }

    public void fillTitleLabel(final ViewDefinitionState viewDefinitionState) {

        FieldComponent title = (FieldComponent) viewDefinitionState.getComponentByReference("title");
        Entity order = dataDefinitionService.get("orders", "order").get(orderId);
        String number = order.getField("number").toString();
        String name = order.getField("name").toString();

        title.setFieldValue(name + " - " + number);
        title.requestComponentUpdateState();
    }
}
