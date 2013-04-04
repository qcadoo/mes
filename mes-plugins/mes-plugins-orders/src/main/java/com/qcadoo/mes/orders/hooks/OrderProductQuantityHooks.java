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
import static com.qcadoo.mes.orders.constants.OrderFields.COMMENT_REASON_TYPE_DEVIATIONS_QUANTITY;
import static com.qcadoo.mes.orders.constants.OrderFields.COMMISSIONED_CORRECTED_QUANTITY;
import static com.qcadoo.mes.orders.constants.OrderFields.DONE_QUANTITY;
import static com.qcadoo.mes.orders.constants.OrderFields.PLANNED_QUANTITY;
import static com.qcadoo.mes.orders.constants.OrderFields.PRODUCT;
import static com.qcadoo.mes.orders.constants.OrderFields.REMAINING_AMOUNT_OF_PRODUCT_TO_PRODUCE;
import static com.qcadoo.mes.orders.constants.OrderFields.STATE;
import static com.qcadoo.mes.orders.constants.OrderFields.TYPE_OF_CORRECTION_CAUSES;
import static com.qcadoo.mes.orders.constants.ParameterFieldsO.BLOCK_ABILILITY_TO_CHANGE_APPROVAL_ORDER;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.orders.states.constants.OrderState;
import com.qcadoo.model.api.BigDecimalUtils;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;

@Service
public class OrderProductQuantityHooks {

    public static final String L_TYPE_OF_PRODUCTION_RECORDING = "typeOfProductionRecording";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NumberService numberService;

    @Autowired
    private ParameterService parameterService;

    public void changedEnabledFieldForSpecificOrderState(final ViewDefinitionState view) {
        final FormComponent form = (FormComponent) view.getComponentByReference("form");
        if (form.getEntityId() == null) {
            return;
        }
        final Entity order = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).get(
                form.getEntityId());

        if (!blockAbilityToChangeApprovalOrder()) {
            if (order.getStringField(STATE).equals(OrderState.ACCEPTED.getStringValue())
                    || order.getStringField(STATE).equals(OrderState.IN_PROGRESS.getStringValue())
                    || order.getStringField(STATE).equals(OrderState.INTERRUPTED.getStringValue())
                    || order.getStringField(STATE).equals(OrderState.PENDING.getStringValue())) {
                List<String> references = Arrays.asList(PLANNED_QUANTITY);
                changedEnabledFields(view, references, true);
            }
        }
        changedEnabledFields(view, Arrays.asList(TYPE_OF_CORRECTION_CAUSES), false);
        if (order.getStringField(STATE).equals(OrderState.PENDING.getStringValue())) {
            List<String> references = Arrays.asList(COMMISSIONED_CORRECTED_QUANTITY, TYPE_OF_CORRECTION_CAUSES,
                    COMMENT_REASON_TYPE_DEVIATIONS_QUANTITY);
            changedEnabledFields(view, references, false);
        }
        if (!blockAbilityToChangeApprovalOrder()) {
            if (order.getStringField(STATE).equals(OrderState.ACCEPTED.getStringValue())
                    || order.getStringField(STATE).equals(OrderState.IN_PROGRESS.getStringValue())
                    || order.getStringField(STATE).equals(OrderState.INTERRUPTED.getStringValue())) {

                List<String> references = Arrays.asList(COMMISSIONED_CORRECTED_QUANTITY, TYPE_OF_CORRECTION_CAUSES,
                        COMMENT_REASON_TYPE_DEVIATIONS_QUANTITY);
                changedEnabledFields(view, references, true);
            }
        }

    }

    private void changedEnabledFields(final ViewDefinitionState view, final List<String> references, final boolean enabled) {
        for (String reference : references) {
            FieldComponent field = (FieldComponent) view.getComponentByReference(reference);
            if (field == null) {
                continue;
            }
            field.setEnabled(enabled);
            field.requestComponentUpdateState();
        }
    }

    public boolean blockAbilityToChangeApprovalOrder() {
        return parameterService.getParameter().getBooleanField(BLOCK_ABILILITY_TO_CHANGE_APPROVAL_ORDER);
    }

    public void fillProductUnit(final ViewDefinitionState state) {
        List<String> references = Arrays.asList("unitCCQ", "unitCPQ", "unitAOPP", "unitRAOPTP");
        fillProductUnit(state, references);
    }

    public void fillProductUnit(final ViewDefinitionState state, final List<String> references) {
        FieldComponent productState = (FieldComponent) state.getComponentByReference(PRODUCT);
        for (String reference : references) {
            FieldComponent unitState = (FieldComponent) state.getComponentByReference(reference);
            unitState.requestComponentUpdateState();
            if (productState.getFieldValue() == null) {
                unitState.setFieldValue("");
            } else {
                Entity product = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_PRODUCT).get(
                        (Long) productState.getFieldValue());
                unitState.setFieldValue(product.getStringField("unit"));
            }
        }
    }

    public void setProductQuantity(final ViewDefinitionState view) {
        final FormComponent form = (FormComponent) view.getComponentByReference("form");
        if (form.getEntityId() == null) {
            return;
        }

        final Entity order = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).get(
                form.getEntityId());

        FieldComponent amountOfPPComponent = (FieldComponent) view.getComponentByReference(AMOUNT_OF_PRODUCT_PRODUCED);
        FieldComponent remainingAmountOfPTPComponent = (FieldComponent) view
                .getComponentByReference(REMAINING_AMOUNT_OF_PRODUCT_TO_PRODUCE);

        String typeOfProductionRecording = order.getStringField(L_TYPE_OF_PRODUCTION_RECORDING);
        if (!StringUtils.isEmpty(typeOfProductionRecording)) {
            amountOfPPComponent.setFieldValue(numberService.format(order.getField(DONE_QUANTITY)));
            order.setField(AMOUNT_OF_PRODUCT_PRODUCED, order.getField(DONE_QUANTITY));
        }

        BigDecimal remainingAmountOfPTP = BigDecimalUtils.convertNullToZero(order.getDecimalField(PLANNED_QUANTITY)).subtract(
                BigDecimalUtils.convertNullToZero(order.getDecimalField(DONE_QUANTITY)), numberService.getMathContext());
        if (remainingAmountOfPTP.compareTo(BigDecimal.ZERO) == -1) {
            remainingAmountOfPTPComponent.setFieldValue(numberService.format(BigDecimal.ZERO));
            order.setField(REMAINING_AMOUNT_OF_PRODUCT_TO_PRODUCE, numberService.setScale(BigDecimal.ZERO));
        } else {
            remainingAmountOfPTPComponent.setFieldValue(numberService.format(remainingAmountOfPTP));
            order.setField(REMAINING_AMOUNT_OF_PRODUCT_TO_PRODUCE, numberService.setScale(remainingAmountOfPTP));
        }
        order.getDataDefinition().save(order);
    }
}
