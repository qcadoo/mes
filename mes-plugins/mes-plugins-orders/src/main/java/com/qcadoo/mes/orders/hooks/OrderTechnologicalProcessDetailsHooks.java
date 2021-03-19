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

import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.orders.OrderTechnologicalProcessService;
import com.qcadoo.mes.orders.constants.OrderTechnologicalProcessFields;
import com.qcadoo.mes.technologies.constants.TechnologicalProcessFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.constants.QcadooViewConstants;

@Service
public class OrderTechnologicalProcessDetailsHooks {

    private static final String L_QUANTITY_UNIT = "quantityUnit";

    private static final String L_TECHNOLOGICAL_PROCESS_NAME = "technologicalProcessName";

    @Autowired
    private OrderTechnologicalProcessService orderTechnologicalProcessService;

    public final void onBeforeRender(final ViewDefinitionState view) {
        FormComponent orderTechnologicalProcessForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        Entity orderTechnologicalProcess = orderTechnologicalProcessForm.getEntity();
        Long orderTechnologicalProcessId = orderTechnologicalProcess.getId();

        if (Objects.nonNull(orderTechnologicalProcessId)) {
            orderTechnologicalProcess = orderTechnologicalProcess.getDataDefinition().get(orderTechnologicalProcessId);
        }

        setFormEnabled(orderTechnologicalProcessForm, orderTechnologicalProcess);

        fillTechnologicalProcessName(view, orderTechnologicalProcess);
        fillUnit(view, orderTechnologicalProcess);
    }

    public void setFormEnabled(final FormComponent orderTechnologicalProcessForm, final Entity orderTechnologicalProcess) {
        Entity order = orderTechnologicalProcess.getBelongsToField(OrderTechnologicalProcessFields.ORDER);

        boolean isOrderStateValid = !orderTechnologicalProcessService.checkOrderState(order);

        orderTechnologicalProcessForm.setFormEnabled(isOrderStateValid);
    }

    public void fillUnit(final ViewDefinitionState view, final Entity orderTechnologicalProcess) {
        FieldComponent quantityUnit = (FieldComponent) view.getComponentByReference(L_QUANTITY_UNIT);

        Entity product = orderTechnologicalProcess.getBelongsToField(OrderTechnologicalProcessFields.PRODUCT);

        String unit = null;

        if (Objects.nonNull(product)) {
            unit = product.getStringField(ProductFields.UNIT);
        }

        quantityUnit.setFieldValue(unit);
        quantityUnit.requestComponentUpdateState();
    }

    public void fillTechnologicalProcessName(final ViewDefinitionState view, final Entity orderTechnologicalProcess) {
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

}
