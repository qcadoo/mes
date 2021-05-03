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
package com.qcadoo.mes.orders.hooks;

import java.util.Date;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.orders.OrderTechnologicalProcessService;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.constants.OrderTechnologicalProcessFields;
import com.qcadoo.mes.orders.constants.ParameterFieldsO;
import com.qcadoo.mes.orders.states.constants.OrderStateStringValues;
import com.qcadoo.mes.technologies.constants.TechnologicalProcessFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.api.ribbon.RibbonGroup;
import com.qcadoo.view.constants.QcadooViewConstants;

@Service
public class OrderTechnologicalProcessDetailsHooks {

    private static final String L_ACTIONS = "actions";

    private static final String L_DELETE = "delete";

    private static final String L_TECHNOLOGICAL_PROCESS_NAME = "technologicalProcessName";

    private static final String L_QUANTITY_UNIT = "quantityUnit";

    @Autowired
    private ParameterService parameterService;

    @Autowired
    private OrderTechnologicalProcessService orderTechnologicalProcessService;

    public final void onBeforeRender(final ViewDefinitionState view) {
        FormComponent orderTechnologicalProcessForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        Entity orderTechnologicalProcess = orderTechnologicalProcessForm.getEntity();
        Long orderTechnologicalProcessId = orderTechnologicalProcess.getId();

        if (Objects.nonNull(orderTechnologicalProcessId)) {
            orderTechnologicalProcess = orderTechnologicalProcess.getDataDefinition().get(orderTechnologicalProcessId);
        }

        updateRibbonState(view, orderTechnologicalProcess);
        setFormEnabled(orderTechnologicalProcessForm, orderTechnologicalProcess);
        setQuantityFieldEnabled(view, orderTechnologicalProcess);

        fillTechnologicalProcessName(view, orderTechnologicalProcess);
        fillUnit(view, orderTechnologicalProcess);
    }

    private void updateRibbonState(final ViewDefinitionState view, final Entity orderTechnologicalProcess) {
        WindowComponent window = (WindowComponent) view.getComponentByReference(QcadooViewConstants.L_WINDOW);
        RibbonGroup actionsGroup = window.getRibbon().getGroupByName(L_ACTIONS);
        RibbonActionItem deleteActionItem = actionsGroup.getItemByName(L_DELETE);

        Entity order = orderTechnologicalProcess.getBelongsToField(OrderTechnologicalProcessFields.ORDER);

        boolean isOrderStateValid = !checkOrderState(order);

        deleteActionItem.setEnabled(isOrderStateValid);
        deleteActionItem.requestUpdate(true);
    }

    private boolean checkOrderState(final Entity order) {
        if (Objects.nonNull(order)) {
            String state = order.getStringField(OrderFields.STATE);

            return OrderStateStringValues.COMPLETED.equals(state) || OrderStateStringValues.ABANDONED.equals(state);
        }

        return false;
    }

    private void setFormEnabled(final FormComponent orderTechnologicalProcessForm, final Entity orderTechnologicalProcess) {
        Entity order = orderTechnologicalProcess.getBelongsToField(OrderTechnologicalProcessFields.ORDER);

        boolean isOrderStateValid = !orderTechnologicalProcessService.checkOrderState(order);

        orderTechnologicalProcessForm.setFormEnabled(isOrderStateValid);
    }

    private void setQuantityFieldEnabled(final ViewDefinitionState view, final Entity orderTechnologicalProcess) {
        FieldComponent quantityField = (FieldComponent) view.getComponentByReference(OrderTechnologicalProcessFields.QUANTITY);

        Entity order = orderTechnologicalProcess.getBelongsToField(OrderTechnologicalProcessFields.ORDER);
        Date date = orderTechnologicalProcess.getDateField(OrderTechnologicalProcessFields.DATE);
        Entity worker = orderTechnologicalProcess.getBelongsToField(OrderTechnologicalProcessFields.WORKER);

        boolean isOrderStateValid = !orderTechnologicalProcessService.checkOrderState(order);
        boolean allowChangeOrDeleteOrderTechnologicalProcess = parameterService.getParameter()
                .getBooleanField(ParameterFieldsO.ALLOW_CHANGE_OR_DELETE_ORDER_TECHNOLOGICAL_PROCESS);
        boolean isCompleted = Objects.nonNull(date) && Objects.nonNull(worker);

        quantityField.setEnabled(isOrderStateValid && (allowChangeOrDeleteOrderTechnologicalProcess || !isCompleted));
        quantityField.requestComponentUpdateState();
    }

    private void fillTechnologicalProcessName(final ViewDefinitionState view, final Entity orderTechnologicalProcess) {
        FieldComponent technologicalProcessNameField = (FieldComponent) view
                .getComponentByReference(L_TECHNOLOGICAL_PROCESS_NAME);

        Entity technologicalProcess = orderTechnologicalProcess
                .getBelongsToField(OrderTechnologicalProcessFields.TECHNOLOGICAL_PROCESS);

        String technologicalProcessName = null;

        if (Objects.nonNull(technologicalProcess)) {
            technologicalProcessName = technologicalProcess.getStringField(TechnologicalProcessFields.NAME);
        }

        technologicalProcessNameField.setFieldValue(technologicalProcessName);
        technologicalProcessNameField.requestComponentUpdateState();
    }

    private void fillUnit(final ViewDefinitionState view, final Entity orderTechnologicalProcess) {
        FieldComponent quantityUnit = (FieldComponent) view.getComponentByReference(L_QUANTITY_UNIT);

        Entity product = orderTechnologicalProcess.getBelongsToField(OrderTechnologicalProcessFields.PRODUCT);

        String unit = null;

        if (Objects.nonNull(product)) {
            unit = product.getStringField(ProductFields.UNIT);
        }

        quantityUnit.setFieldValue(unit);
        quantityUnit.requestComponentUpdateState();
    }

}
