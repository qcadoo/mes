/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.4
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
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import com.google.common.base.Optional;
import com.qcadoo.commons.functional.Either;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.orders.constants.OrderTechnologicalProcessFields;
import com.qcadoo.mes.orders.constants.OrderTechnologicalProcessPartFields;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.model.api.BigDecimalUtils;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.AwesomeDynamicListComponent;
import com.qcadoo.view.api.components.CheckBoxComponent;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.constants.QcadooViewConstants;

@Service
public class DivideOrderTechnologicalProcessListeners {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NumberService numberService;

    public void saveDivision(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent orderTechnologicalProcessForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        AwesomeDynamicListComponent orderTechnologicalProcessPartsADL = (AwesomeDynamicListComponent) view
                .getComponentByReference(OrderTechnologicalProcessFields.ORDER_TECHNOLOGICAL_PROCESS_PARTS);
        CheckBoxComponent isValidCheckBox = (CheckBoxComponent) view
                .getComponentByReference(OrderTechnologicalProcessFields.IS_VALID);

        Entity orderTechnologicalProcess = orderTechnologicalProcessForm.getEntity();
        Long orderTechnologicalProcessId = orderTechnologicalProcess.getId();

        orderTechnologicalProcess = orderTechnologicalProcess.getDataDefinition().get(orderTechnologicalProcessId);

        List<FormComponent> orderTechnologicalProcessPartForms = orderTechnologicalProcessPartsADL.getFormComponents();

        boolean isValid = validateDivision(orderTechnologicalProcessForm, orderTechnologicalProcessPartForms,
                orderTechnologicalProcess);

        if (isValid) {
            for (FormComponent orderTechnologicalProcessPartForm : orderTechnologicalProcessPartForms) {
                FieldComponent numberField = orderTechnologicalProcessPartForm
                        .findFieldComponentByName(OrderTechnologicalProcessPartFields.NUMBER);
                FieldComponent quantityField = orderTechnologicalProcessPartForm
                        .findFieldComponentByName(OrderTechnologicalProcessPartFields.QUANTITY);

                String number = (String) numberField.getFieldValue();
                Either<Exception, Optional<BigDecimal>> eitherQuantity = BigDecimalUtils
                        .tryParse(quantityField.getFieldValue().toString(), LocaleContextHolder.getLocale());
                Optional<BigDecimal> mayBeQuantity = eitherQuantity.getRight();

                if ("1".equals(number)) {
                    if (mayBeQuantity.isPresent()) {
                        orderTechnologicalProcess.setField(OrderTechnologicalProcessFields.QUANTITY, mayBeQuantity.get());
                    }

                    orderTechnologicalProcess.getDataDefinition().save(orderTechnologicalProcess);
                } else {
                    Entity orderPack = orderTechnologicalProcess.getBelongsToField(OrderTechnologicalProcessFields.ORDER_PACK);
                    Entity order = orderTechnologicalProcess.getBelongsToField(OrderTechnologicalProcessFields.ORDER);
                    Entity product = orderTechnologicalProcess.getBelongsToField(OrderTechnologicalProcessFields.PRODUCT);
                    Entity operation = orderTechnologicalProcess.getBelongsToField(OrderTechnologicalProcessFields.OPERATION);
                    Entity technologyOperationComponent = orderTechnologicalProcess
                            .getBelongsToField(OrderTechnologicalProcessFields.TECHNOLOGY_OPERATION_COMPONENT);
                    Entity technologicalProcess = orderTechnologicalProcess
                            .getBelongsToField(OrderTechnologicalProcessFields.TECHNOLOGICAL_PROCESS);

                    if (mayBeQuantity.isPresent()) {
                        createOrderTechnologicalProcess(orderPack, order, product, technologyOperationComponent, operation,
                                technologicalProcess, mayBeQuantity.get());
                    }
                }
            }

            view.addMessage("orders.divideOrderTechnologicalProcess.divide.success", ComponentState.MessageType.SUCCESS);
        } else {
            view.addMessage("orders.divideOrderTechnologicalProcess.divide.failure", ComponentState.MessageType.FAILURE);
        }

        isValidCheckBox.setChecked(isValid);
        isValidCheckBox.requestComponentUpdateState();
    }

    private boolean validateDivision(final FormComponent orderTechnologicalProcessForm,
            final List<FormComponent> orderTechnologicalProcessPartForms, final Entity orderTechnologicalProcess) {
        boolean isValid = true;

        BigDecimal quantity = orderTechnologicalProcess.getDecimalField(OrderTechnologicalProcessFields.QUANTITY);

        if (orderTechnologicalProcessPartForms.size() < 2) {
            orderTechnologicalProcessForm.addMessage("orders.divideOrderTechnologicalProcess.divide.notEnough",
                    ComponentState.MessageType.FAILURE);

            isValid = false;
        } else {
            BigDecimal quantitySum = BigDecimal.ZERO;

            for (FormComponent orderTechnologicalProcessPartForm : orderTechnologicalProcessPartForms) {
                FieldComponent quantityField = orderTechnologicalProcessPartForm
                        .findFieldComponentByName(OrderTechnologicalProcessPartFields.QUANTITY);

                Either<Exception, Optional<BigDecimal>> eitherQuantity = BigDecimalUtils
                        .tryParse(quantityField.getFieldValue().toString(), LocaleContextHolder.getLocale());

                if (eitherQuantity.isLeft()) {
                    quantityField.addMessage("qcadooView.validate.field.error.custom", ComponentState.MessageType.FAILURE);

                    isValid = false;
                } else if (eitherQuantity.getRight().isPresent()) {
                    BigDecimal quantityPart = eitherQuantity.getRight().get();

                    if (BigDecimal.ZERO.compareTo(quantityPart) >= 0) {
                        quantityField.addMessage("qcadooView.validate.field.error.outOfRange.toSmall",
                                ComponentState.MessageType.FAILURE);

                        isValid = false;
                    } else {
                        int scale = quantityPart.scale();
                        int max = NumberService.DEFAULT_MAX_FRACTION_DIGITS_IN_DECIMAL;

                        if (scale > max) {
                            quantityField.addMessage("qcadooView.validate.field.error.invalidScale.max",
                                    ComponentState.MessageType.FAILURE, String.valueOf(max));

                            return false;
                        } else {
                            quantitySum = quantitySum.add(quantityPart, numberService.getMathContext());
                        }
                    }
                }
            }

            if (quantity.compareTo(quantitySum) != 0) {
                orderTechnologicalProcessForm.addMessage("orders.divideOrderTechnologicalProcess.divide.quantityIncorrect",
                        ComponentState.MessageType.FAILURE);

                isValid = false;
            }
        }

        return isValid;
    }

    private void createOrderTechnologicalProcess(final Entity orderPack, final Entity order, final Entity product,
            final Entity technologyOperationComponent, final Entity operation, final Entity technologicalProcess,
            final BigDecimal quantity) {
        Entity orderTechnologicalProcess = getOrderTechnologicalProcessDD().create();

        orderTechnologicalProcess.setField(OrderTechnologicalProcessFields.ORDER_PACK, orderPack);
        orderTechnologicalProcess.setField(OrderTechnologicalProcessFields.ORDER, order);
        orderTechnologicalProcess.setField(OrderTechnologicalProcessFields.PRODUCT, product);
        orderTechnologicalProcess.setField(OrderTechnologicalProcessFields.OPERATION, operation);
        orderTechnologicalProcess.setField(OrderTechnologicalProcessFields.TECHNOLOGY_OPERATION_COMPONENT,
                technologyOperationComponent);
        orderTechnologicalProcess.setField(OrderTechnologicalProcessFields.TECHNOLOGICAL_PROCESS, technologicalProcess);
        orderTechnologicalProcess.setField(OrderTechnologicalProcessFields.QUANTITY, quantity);

        orderTechnologicalProcess.getDataDefinition().save(orderTechnologicalProcess);
    }

    public void onAddRow(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent orderTechnologicalProcessForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        AwesomeDynamicListComponent orderTechnologicalProcessPartsADL = (AwesomeDynamicListComponent) view
                .getComponentByReference(OrderTechnologicalProcessFields.ORDER_TECHNOLOGICAL_PROCESS_PARTS);

        Entity orderTechnologicalProcess = orderTechnologicalProcessForm.getEntity();
        Long orderTechnologicalProcessId = orderTechnologicalProcess.getId();

        orderTechnologicalProcess = orderTechnologicalProcess.getDataDefinition().get(orderTechnologicalProcessId);

        Entity product = orderTechnologicalProcess.getBelongsToField(OrderTechnologicalProcessFields.PRODUCT);

        String productUnit = product.getStringField(ProductFields.UNIT);

        List<FormComponent> orderTechnologicalProcessPartForms = orderTechnologicalProcessPartsADL.getFormComponents();

        for (FormComponent orderTechnologicalProcessPartForm : orderTechnologicalProcessPartForms) {
            FieldComponent numberField = orderTechnologicalProcessPartForm
                    .findFieldComponentByName(OrderTechnologicalProcessPartFields.NUMBER);
            FieldComponent unitField = orderTechnologicalProcessPartForm
                    .findFieldComponentByName(OrderTechnologicalProcessPartFields.UNIT);

            String number = (String) numberField.getFieldValue();
            String unit = (String) unitField.getFieldValue();

            if (StringUtils.isEmpty(number)) {
                numberField.setFieldValue(String.valueOf(orderTechnologicalProcessPartForms.size()));
            }
            numberField.requestComponentUpdateState();

            if (StringUtils.isEmpty(unit)) {
                unitField.setFieldValue(productUnit);
            }
            unitField.requestComponentUpdateState();
        }
    }

    public void onDeleteRow(final ViewDefinitionState view, final ComponentState componentState, final String[] args) {
        AwesomeDynamicListComponent orderTechnologicalProcessPartsADL = (AwesomeDynamicListComponent) view
                .getComponentByReference(OrderTechnologicalProcessFields.ORDER_TECHNOLOGICAL_PROCESS_PARTS);

        List<FormComponent> orderTechnologicalProcessPartForms = orderTechnologicalProcessPartsADL.getFormComponents();

        if (orderTechnologicalProcessPartForms.size() < 2) {
            view.addMessage("orders.divideOrderTechnologicalProcess.divide.notEnough", ComponentState.MessageType.INFO);
        }
    }

    private DataDefinition getOrderTechnologicalProcessDD() {
        return dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER_TECHNOLOGICAL_PROCESS);
    }

}
