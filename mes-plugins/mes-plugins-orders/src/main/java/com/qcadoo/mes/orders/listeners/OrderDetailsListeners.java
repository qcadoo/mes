/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.2.0-SNAPSHOT
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

import static com.qcadoo.mes.orders.constants.OrderFields.CORRECTED_DATE_FROM;
import static com.qcadoo.mes.orders.constants.OrderFields.CORRECTED_DATE_TO;
import static com.qcadoo.mes.orders.constants.OrderFields.DATE_FROM;
import static com.qcadoo.mes.orders.constants.OrderFields.DATE_TO;
import static com.qcadoo.mes.orders.constants.OrderFields.TECHNOLOGY;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.orders.TechnologyServiceO;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;

@Service
public class OrderDetailsListeners {

    private static final String PLANNED_DATE_FROM = "plannedDateFrom";

    private static final String PLANNED_DATE_TO = "plannedDateTo";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private TechnologyServiceO technologyServiceO;

    public void showOrderParameters(final ViewDefinitionState view, final ComponentState componentState, final String[] args) {
        Long orderId = (Long) componentState.getFieldValue();

        if (orderId != null) {
            String url = "../page/orders/technologyInstanceOperationComponentList.html?context={\"form.id\":\"" + orderId + "\"}";
            view.redirectTo(url, false, true);
        }
    }

    private void copyDate(final ViewDefinitionState viewDefinitionState, final String fromNameField, final String toNameField) {
        FormComponent form = (FormComponent) viewDefinitionState.getComponentByReference("form");
        FieldComponent fromField = (FieldComponent) viewDefinitionState.getComponentByReference(fromNameField);
        FieldComponent toField = (FieldComponent) viewDefinitionState.getComponentByReference(toNameField);
        if (form.getEntityId() == null) {
            toField.setFieldValue(fromField.getFieldValue());
            return;
        }
        Entity order = getOrderFromForm(form.getEntityId());

        if (!fromField.getFieldValue().equals(order.getField(fromNameField))) {
            toField.setFieldValue(fromField.getFieldValue());
        }
        toField.requestComponentUpdateState();
    }

    public void copyStartDate(final ViewDefinitionState view, final ComponentState triggerState, final String[] args) {
        if (triggerState.getName().equals(PLANNED_DATE_FROM)) {
            copyDate(view, PLANNED_DATE_FROM, DATE_FROM);
        } else {
            copyDate(view, CORRECTED_DATE_FROM, DATE_FROM);
        }
    }

    public void copyEndDate(final ViewDefinitionState view, final ComponentState triggerState, final String[] args) {
        if (triggerState.getName().equals(PLANNED_DATE_TO)) {
            copyDate(view, PLANNED_DATE_TO, DATE_TO);
        } else {
            copyDate(view, CORRECTED_DATE_TO, DATE_TO);
        }

    }

    public void changeOrderProduct(final ViewDefinitionState viewDefinitionState, final ComponentState state, final String[] args) {
        LookupComponent productLookup = (LookupComponent) viewDefinitionState.getComponentByReference(OrderFields.PRODUCT);
        FieldComponent technology = (FieldComponent) viewDefinitionState.getComponentByReference(TECHNOLOGY);
        FieldComponent defaultTechnology = (FieldComponent) viewDefinitionState.getComponentByReference("defaultTechnology");

        Entity product = productLookup.getEntity();
        defaultTechnology.setFieldValue("");
        technology.setFieldValue(null);
        if (product != null) {
            Entity defaultTechnologyEntity = technologyServiceO.getDefaultTechnology(product);
            if (defaultTechnologyEntity != null) {
                technology.setFieldValue(defaultTechnologyEntity.getId());
            }
        }
    }

    private Entity getOrderFromForm(final Long id) {
        return dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).get(id);
    }
}
