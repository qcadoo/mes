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
package com.qcadoo.mes.orders.listeners;

import static com.qcadoo.mes.orders.constants.OrderFields.DATE_FROM;
import static com.qcadoo.mes.orders.constants.OrderFields.DATE_TO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;

@Service
public class OrderDetailsListeners {

    private static final String PLANNED_DATE_FROM = "plannedDateFrom";

    private static final String PLANNED_DATE_TO = "plannedDateTo";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void showOrderParameters(final ViewDefinitionState view, final ComponentState componentState, final String[] args) {
        Long orderId = (Long) componentState.getFieldValue();

        if (orderId != null) {
            String url = "../page/orders/technologyOperationComponentInOrderList.html?context={\"form.id\":\"" + orderId + "\"}";
            view.redirectTo(url, false, true);
        }
    }

    private void copyDate(final ViewDefinitionState viewDefinitionState, final String fromNameField, final String toNameField) {
        FormComponent form = (FormComponent) viewDefinitionState.getComponentByReference("form");
        if (form.getEntityId() == null) {
            return;
        }
        Entity order = getOrderFromForm(form.getEntityId());
        FieldComponent fromField = (FieldComponent) viewDefinitionState.getComponentByReference(fromNameField);
        FieldComponent toField = (FieldComponent) viewDefinitionState.getComponentByReference(toNameField);

        if (!fromField.getFieldValue().equals(order.getField(fromNameField))) {
            toField.setFieldValue(fromField.getFieldValue());
        }
        toField.requestComponentUpdateState();
    }

    public void copyStartDate(final ViewDefinitionState view, final ComponentState triggerState, final String[] args) {
        if (triggerState.getName().equals(DATE_FROM)) {
            copyDate(view, DATE_FROM, PLANNED_DATE_FROM);
        } else {
            copyDate(view, PLANNED_DATE_FROM, DATE_FROM);
        }
    }

    public void copyEndDate(final ViewDefinitionState view, final ComponentState triggerState, final String[] args) {
        if (triggerState.getName().equals(DATE_TO)) {
            copyDate(view, DATE_TO, PLANNED_DATE_TO);
        } else {
            copyDate(view, PLANNED_DATE_TO, DATE_TO);
        }

    }

    private Entity getOrderFromForm(final Long id) {
        return dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).get(id);
    }
}
