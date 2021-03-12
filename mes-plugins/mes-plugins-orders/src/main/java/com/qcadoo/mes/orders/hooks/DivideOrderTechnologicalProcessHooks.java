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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.beust.jcommander.internal.Lists;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.orders.constants.OrderTechnologicalProcessFields;
import com.qcadoo.mes.orders.constants.OrderTechnologicalProcessPartFields;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.AwesomeDynamicListComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.constants.QcadooViewConstants;

@Service
public class DivideOrderTechnologicalProcessHooks {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NumberService numberService;

    public final void onBeforeRender(final ViewDefinitionState view) {
        updateAwesomeDynamicList(view);
    }

    private void updateAwesomeDynamicList(final ViewDefinitionState view) {
        FormComponent orderTechnologicalProcessForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        AwesomeDynamicListComponent orderTechnologicalProcessPartsADL = (AwesomeDynamicListComponent) view
                .getComponentByReference(OrderTechnologicalProcessFields.ORDER_TECHNOLOGICAL_PROCESS_PARTS);

        if (view.isViewAfterRedirect()) {
            Entity orderTechnologicalProcess = orderTechnologicalProcessForm.getEntity();
            Long orderTechnologicalProcessId = orderTechnologicalProcess.getId();

            orderTechnologicalProcess = orderTechnologicalProcess.getDataDefinition().get(orderTechnologicalProcessId);

            Entity product = orderTechnologicalProcess.getBelongsToField(OrderTechnologicalProcessFields.PRODUCT);
            BigDecimal quantity = orderTechnologicalProcess.getDecimalField(OrderTechnologicalProcessFields.QUANTITY);

            String productUnit = product.getStringField(ProductFields.UNIT);

            List<Entity> orderTechnologicalProcessParts = Lists.newArrayList();

            BigDecimal half = new BigDecimal(2);

            BigDecimal quotient = quantity.divide(half, numberService.getMathContext()).setScale(0, RoundingMode.FLOOR);;

            if (BigDecimal.ZERO.compareTo(quantity.remainder(half, numberService.getMathContext())) == 0) {
                orderTechnologicalProcessParts.add(createOrderTechnologicalProcessPart("1", quotient, productUnit));
                orderTechnologicalProcessParts.add(createOrderTechnologicalProcessPart("2", quotient, productUnit));
            } else {
                BigDecimal difference = quantity.subtract(quotient, numberService.getMathContext());

                orderTechnologicalProcessParts.add(createOrderTechnologicalProcessPart("1", quotient, productUnit));
                orderTechnologicalProcessParts.add(createOrderTechnologicalProcessPart("2", difference, productUnit));
            }

            orderTechnologicalProcessPartsADL.setFieldValue(orderTechnologicalProcessParts);
        }
    }

    private Entity createOrderTechnologicalProcessPart(final String number, final BigDecimal quantity, final String unit) {
        Entity orderTechnologicalProcessPart = getOrderTechnologicalProcessPartDD().create();

        orderTechnologicalProcessPart.setField(OrderTechnologicalProcessPartFields.NUMBER, number);
        orderTechnologicalProcessPart.setField(OrderTechnologicalProcessPartFields.QUANTITY, quantity);
        orderTechnologicalProcessPart.setField(OrderTechnologicalProcessPartFields.UNIT, unit);

        return orderTechnologicalProcessPart;
    }

    private DataDefinition getOrderTechnologicalProcessPartDD() {
        return dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER,
                OrdersConstants.MODEL_ORDER_TECHNOLOGICAL_PROCESS_PART);
    }

}
