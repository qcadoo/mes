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

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
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
import com.qcadoo.model.api.BigDecimalUtils;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ComponentState.MessageType;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;

@Service
public class OrderDetailsListeners {

    private static final String L_FORM = "form";

    private static final String L_PLANNED_DATE_FROM = "plannedDateFrom";

    private static final String L_PLANNED_DATE_TO = "plannedDateTo";

    private static final String L_EFFECTIVE_DATE_FROM = "effectiveDateFrom";

    private static final String L_EFFECTIVE_DATE_TO = "effectiveDateTo";

    public static final String L_TYPE_OF_PRODUCTION_RECORDING = "typeOfProductionRecording";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private TechnologyServiceO technologyServiceO;

    @Autowired
    private OrderDetailsHooks orderDetailsHooks;

    @Autowired
    private NumberService numberService;

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
        FormComponent form = (FormComponent) viewDefinitionState.getComponentByReference(L_FORM);
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
        if (triggerState.getName().equals(L_PLANNED_DATE_FROM)) {
            copyDate(view, L_PLANNED_DATE_FROM, OrderFields.DATE_FROM);
        } else if (triggerState.getName().equals(L_EFFECTIVE_DATE_FROM)) {
            copyDate(view, OrderFields.EFFECTIVE_DATE_FROM, OrderFields.DATE_FROM);
        } else {
            copyDate(view, OrderFields.CORRECTED_DATE_FROM, OrderFields.DATE_FROM);
        }
    }

    public void copyEndDate(final ViewDefinitionState view, final ComponentState triggerState, final String[] args) {
        if (triggerState.getName().equals(L_PLANNED_DATE_TO)) {
            copyDate(view, L_PLANNED_DATE_TO, OrderFields.DATE_TO);
        } else if (triggerState.getName().equals(L_EFFECTIVE_DATE_TO)) {
            copyDate(view, OrderFields.EFFECTIVE_DATE_TO, OrderFields.DATE_TO);
        } else {
            copyDate(view, OrderFields.CORRECTED_DATE_TO, OrderFields.DATE_TO);
        }

    }

    public void copyStartDateToDetails(final ViewDefinitionState view, final ComponentState triggerState, final String[] args) {
        FormComponent form = (FormComponent) view.getComponentByReference(L_FORM);

        if (form.getEntityId() == null) {
            copyDate(view, OrderFields.DATE_FROM, L_PLANNED_DATE_FROM);
            return;
        }

        Entity order = getOrderFromForm(form.getEntityId());

        String state = order.getStringField(OrderFields.STATE);
        if (OrderState.PENDING.getStringValue().equals(state)) {
            copyDate(view, OrderFields.DATE_FROM, L_PLANNED_DATE_FROM);
        }
        if (OrderState.IN_PROGRESS.getStringValue().equals(state) || OrderState.ABANDONED.getStringValue().equals(state)
                || OrderState.COMPLETED.getStringValue().equals(state)) {
            copyDate(view, OrderFields.DATE_FROM, L_EFFECTIVE_DATE_FROM);
        }
        if ((OrderState.ACCEPTED.getStringValue().equals(state))) {
            copyDate(view, OrderFields.DATE_FROM, OrderFields.CORRECTED_DATE_FROM);
        }

    }

    public void copyFinishDateToDetails(final ViewDefinitionState view, final ComponentState triggerState, final String[] args) {
        FormComponent form = (FormComponent) view.getComponentByReference(L_FORM);
        if (form.getEntityId() == null) {
            copyDate(view, OrderFields.DATE_TO, L_PLANNED_DATE_TO);
            return;
        }

        Entity order = getOrderFromForm(form.getEntityId());

        String state = order.getStringField(OrderFields.STATE);

        if (OrderState.PENDING.getStringValue().equals(state)) {
            copyDate(view, OrderFields.DATE_TO, L_PLANNED_DATE_TO);
        }
        if (OrderState.COMPLETED.getStringValue().equals(state) || OrderState.ABANDONED.getStringValue().equals(state)) {
            copyDate(view, OrderFields.DATE_TO, L_EFFECTIVE_DATE_TO);
        }
        if (OrderState.ACCEPTED.getStringValue().equals(state) || OrderState.IN_PROGRESS.getStringValue().equals(state)) {
            copyDate(view, OrderFields.DATE_TO, OrderFields.CORRECTED_DATE_TO);
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

    public void onOrderTypeChange(final ViewDefinitionState view, final ComponentState componentState, final String[] args) {

        orderDetailsHooks.setFieldsVisibilityAndFill(view);

        final FormComponent orderForm = (FormComponent) view.getComponentByReference(L_FORM);
        Long orderId = orderForm.getEntityId();
        if (orderId != null) {
            FieldComponent orderType = (FieldComponent) view.getComponentByReference(OrderFields.ORDER_TYPE);

            boolean selectForPatternTechnology = OrderType.WITH_PATTERN_TECHNOLOGY.getStringValue().equals(
                    orderType.getFieldValue());
            if (selectForPatternTechnology) {
                orderForm.addMessage("order.orderType.changeOrderType", MessageType.INFO, false);

            }
        }
    }

    public void setProductQuantities(final ViewDefinitionState view, final ComponentState triggerState, final String[] args) {
        if (!isValidDecimalField(view, Arrays.asList(OrderFields.DONE_QUANTITY))) {
            return;
        }
        final FormComponent form = (FormComponent) view.getComponentByReference(L_FORM);
        if (form.getEntityId() == null) {
            return;
        }

        Entity order = form.getEntity();

        FieldComponent amountOfPPComponent = (FieldComponent) view
                .getComponentByReference(OrderFields.AMOUNT_OF_PRODUCT_PRODUCED);
        FieldComponent remainingAmountOfPTPComponent = (FieldComponent) view
                .getComponentByReference(OrderFields.REMAINING_AMOUNT_OF_PRODUCT_TO_PRODUCE);

        amountOfPPComponent.setFieldValue(numberService.format(order.getField(OrderFields.DONE_QUANTITY)));
        amountOfPPComponent.requestComponentUpdateState();

        BigDecimal remainingAmountOfPTP = BigDecimalUtils.convertNullToZero(order.getDecimalField(OrderFields.PLANNED_QUANTITY))
                .subtract(BigDecimalUtils.convertNullToZero(order.getDecimalField(OrderFields.DONE_QUANTITY)),
                        numberService.getMathContext());
        if (remainingAmountOfPTP.compareTo(BigDecimal.ZERO) == -1) {
            remainingAmountOfPTPComponent.setFieldValue(numberService.format(BigDecimal.ZERO));

        } else {
            remainingAmountOfPTPComponent.setFieldValue(numberService.format(remainingAmountOfPTP));

        }
        remainingAmountOfPTPComponent.requestComponentUpdateState();
    }

    public void setDoneQuantity(final ViewDefinitionState view, final ComponentState triggerState, final String[] args) {
        if (!isValidDecimalField(view, Arrays.asList(OrderFields.AMOUNT_OF_PRODUCT_PRODUCED))) {
            return;
        }
        final FormComponent form = (FormComponent) view.getComponentByReference(L_FORM);
        if (form.getEntityId() == null) {
            return;
        }

        Entity order = form.getEntity();

        FieldComponent doneQuantityComponent = (FieldComponent) view.getComponentByReference(OrderFields.DONE_QUANTITY);
        FieldComponent remainingAmountOfPTPComponent = (FieldComponent) view
                .getComponentByReference(OrderFields.REMAINING_AMOUNT_OF_PRODUCT_TO_PRODUCE);

        doneQuantityComponent.setFieldValue(numberService.format(order.getField(OrderFields.AMOUNT_OF_PRODUCT_PRODUCED)));
        doneQuantityComponent.requestComponentUpdateState();

        BigDecimal remainingAmountOfPTP = BigDecimalUtils.convertNullToZero(order.getDecimalField(OrderFields.PLANNED_QUANTITY))
                .subtract(BigDecimalUtils.convertNullToZero(order.getDecimalField(OrderFields.AMOUNT_OF_PRODUCT_PRODUCED)),
                        numberService.getMathContext());
        if (remainingAmountOfPTP.compareTo(BigDecimal.ZERO) == -1) {
            remainingAmountOfPTPComponent.setFieldValue(numberService.format(BigDecimal.ZERO));

        } else {
            remainingAmountOfPTPComponent.setFieldValue(numberService.format(remainingAmountOfPTP));

        }
        remainingAmountOfPTPComponent.requestComponentUpdateState();
    }

    private boolean isValidDecimalField(final ViewDefinitionState view, final List<String> fileds) {
        boolean isValid = true;
        FormComponent formComponent = (FormComponent) view.getComponentByReference(L_FORM);
        Entity entity = formComponent.getEntity();
        for (String field : fileds) {
            try {
                BigDecimal decimalField = entity.getDecimalField(field);
            } catch (IllegalArgumentException e) {
                FieldComponent component = (FieldComponent) view.getComponentByReference(field);
                component.addMessage("qcadooView.validate.field.error.invalidNumericFormat", MessageType.FAILURE);
                isValid = false;
            }
        }

        return isValid;
    }

}
