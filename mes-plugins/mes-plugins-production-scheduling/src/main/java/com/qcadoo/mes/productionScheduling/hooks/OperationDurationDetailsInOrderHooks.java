/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.3
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
package com.qcadoo.mes.productionScheduling.hooks;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.productionScheduling.constants.OrderFieldsPS;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.api.ribbon.RibbonGroup;

@Service
public class OperationDurationDetailsInOrderHooks {

    private static final String L_FORM = "form";

    private static final String L_WINDOW = "window";

    private static final String L_GENERATED_END_DATE = "generatedEndDate";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void fillUnitField(final ViewDefinitionState view) {
        FormComponent orderForm = (FormComponent) view.getComponentByReference(L_FORM);
        FieldComponent unitField = (FieldComponent) view.getComponentByReference(OrderFieldsPS.OPERATION_DURATION_QUANTITY_UNIT);

        Long orderId = orderForm.getEntityId();

        if (orderId != null) {
            Entity order = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).get(orderId);

            if (order != null) {
                Entity product = order.getBelongsToField(OrderFields.PRODUCT);

                if (product != null) {
                    unitField.setFieldValue(product.getField(ProductFields.UNIT));
                }
            }
        }
    }

    public void disableCopyRealizationTimeButton(final ViewDefinitionState view) {
        WindowComponent window = (WindowComponent) view.getComponentByReference(L_WINDOW);

        RibbonGroup realizationTimeGroup = window.getRibbon().getGroupByName("operationDuration");
        RibbonActionItem realizationTime = realizationTimeGroup.getItemByName("copy");

        if (isGenerated(view)) {
            realizationTime.setEnabled(true);
        } else {
            realizationTime.setEnabled(false);
        }

        realizationTime.requestUpdate(true);
    }

    private boolean isGenerated(final ViewDefinitionState view) {
        FieldComponent generatedEndDateField = (FieldComponent) view.getComponentByReference(L_GENERATED_END_DATE);

        return !StringUtils.isEmpty((String) generatedEndDateField.getFieldValue());
    }
}
