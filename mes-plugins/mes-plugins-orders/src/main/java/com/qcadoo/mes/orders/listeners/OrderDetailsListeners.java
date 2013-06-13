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
package com.qcadoo.mes.orders.listeners;

import static com.qcadoo.mes.orders.constants.OrderFields.CORRECTED_DATE_FROM;
import static com.qcadoo.mes.orders.constants.OrderFields.CORRECTED_DATE_TO;
import static com.qcadoo.mes.orders.constants.OrderFields.DATE_FROM;
import static com.qcadoo.mes.orders.constants.OrderFields.DATE_TO;
import static com.qcadoo.mes.orders.states.constants.OrderState.ABANDONED;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Maps;
import com.qcadoo.mes.orders.TechnologyServiceO;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.constants.OrderType;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.orders.hooks.OrderDetailsHooks;
import com.qcadoo.mes.orders.states.constants.OrderState;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ComponentState.MessageType;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;

@Service
public class OrderDetailsListeners {

    private static final String PLANNED_DATE_FROM = "plannedDateFrom";

    private static final String PLANNED_DATE_TO = "plannedDateTo";

    private static final String EFFECTIVE_DATE_FROM = "effectiveDateFrom";

    private static final String EFFECTIVE_DATE_TO = "effectiveDateTo";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private TechnologyServiceO technologyServiceO;

    @Autowired
    private OrderDetailsHooks orderDetailsHooks;

    public void showCopyOfTechnology(final ViewDefinitionState view, final ComponentState componentState, final String[] args) {
        Long orderId = (Long) componentState.getFieldValue();

        if (orderId != null) {

            Entity order = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).get(orderId);
            if (order.getField(OrderFields.ORDER_TYPE).equals(OrderType.WITH_PATTERN_TECHNOLOGY.getStringValue())) {
                LookupComponent patternTechnologyLookup = (LookupComponent) view
                        .getComponentByReference(OrderFields.TECHNOLOGY_PROTOTYPE);
                if (patternTechnologyLookup.getEntity() == null) {

                    componentState.addMessage("order.technology.patternTechnology.not.set", MessageType.INFO);
                    return;
                }

            }
            Long technologyId = order.getBelongsToField(OrderFields.TECHNOLOGY).getId();
            Map<String, Object> parameters = Maps.newHashMap();
            parameters.put("form.id", technologyId);

            String url = "../page/orders/copyOfTechnologyDetails.html";
            view.redirectTo(url, false, true, parameters);

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
        } else if (triggerState.getName().equals(EFFECTIVE_DATE_FROM)) {
            copyDate(view, OrderFields.EFFECTIVE_DATE_FROM, DATE_FROM);
        } else {
            copyDate(view, CORRECTED_DATE_FROM, DATE_FROM);
        }
    }

    public void copyEndDate(final ViewDefinitionState view, final ComponentState triggerState, final String[] args) {
        if (triggerState.getName().equals(PLANNED_DATE_TO)) {
            copyDate(view, PLANNED_DATE_TO, DATE_TO);
        } else if (triggerState.getName().equals(EFFECTIVE_DATE_TO)) {
            copyDate(view, OrderFields.EFFECTIVE_DATE_TO, DATE_TO);
        } else {
            copyDate(view, CORRECTED_DATE_TO, DATE_TO);
        }

    }

    public void copyStartDateToDetails(final ViewDefinitionState view, final ComponentState triggerState, final String[] args) {
        FormComponent form = (FormComponent) view.getComponentByReference("form");

        if (form.getEntityId() == null) {
            copyDate(view, DATE_FROM, PLANNED_DATE_FROM);
            return;
        }

        Entity order = getOrderFromForm(form.getEntityId());

        String state = order.getStringField(OrderFields.STATE);
        if (OrderState.PENDING.getStringValue().equals(state)) {
            copyDate(view, DATE_FROM, PLANNED_DATE_FROM);
        }
        if (OrderState.IN_PROGRESS.getStringValue().equals(state) || ABANDONED.getStringValue().equals(state)
                || OrderState.COMPLETED.getStringValue().equals(state)) {
            copyDate(view, DATE_FROM, EFFECTIVE_DATE_FROM);
        }
        if ((OrderState.ACCEPTED.getStringValue().equals(state))) {
            copyDate(view, DATE_FROM, CORRECTED_DATE_FROM);
        }

    }

    public void copyFinishDateToDetails(final ViewDefinitionState view, final ComponentState triggerState, final String[] args) {
        FormComponent form = (FormComponent) view.getComponentByReference("form");
        if (form.getEntityId() == null) {
            copyDate(view, DATE_TO, PLANNED_DATE_TO);
            return;
        }

        Entity order = getOrderFromForm(form.getEntityId());

        String state = order.getStringField(OrderFields.STATE);

        if (OrderState.PENDING.getStringValue().equals(state)) {
            copyDate(view, DATE_TO, PLANNED_DATE_TO);
        }
        if (OrderState.COMPLETED.getStringValue().equals(state) || OrderState.ABANDONED.getStringValue().equals(state)) {
            copyDate(view, DATE_TO, EFFECTIVE_DATE_TO);
        }
        if (OrderState.ACCEPTED.getStringValue().equals(state) || OrderState.IN_PROGRESS.getStringValue().equals(state)) {
            copyDate(view, DATE_TO, CORRECTED_DATE_TO);
        }
    }

    public void changeOrderProduct(final ViewDefinitionState viewDefinitionState, final ComponentState state, final String[] args) {
        FieldComponent orderType = (FieldComponent) viewDefinitionState.getComponentByReference("orderType");
        if (OrderType.WITH_PATTERN_TECHNOLOGY.getStringValue().equals(orderType.getFieldValue())) {
            LookupComponent productLookup = (LookupComponent) viewDefinitionState.getComponentByReference(OrderFields.PRODUCT);
            FieldComponent technology = (FieldComponent) viewDefinitionState
                    .getComponentByReference(OrderFields.TECHNOLOGY_PROTOTYPE);
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
    }

    private Entity getOrderFromForm(final Long id) {
        return dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).get(id);
    }

    public void setFieldsVisibility(final ViewDefinitionState view, final ComponentState componentState, final String[] args) {
        orderDetailsHooks.setFieldsVisibilityAndFill(view);
    }

}
