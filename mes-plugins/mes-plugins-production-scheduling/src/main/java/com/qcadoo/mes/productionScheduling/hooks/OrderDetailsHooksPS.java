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
package com.qcadoo.mes.productionScheduling.hooks;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.api.ribbon.RibbonGroup;

@Service
public class OrderDetailsHooksPS {

    private static final String L_FORM = "form";

    private static final String L_WINDOW = "window";

    private static final String L_OPERATION_DURATION = "operationDuration";

    public void disabledButtonOperationDuration(final ViewDefinitionState view) {
        FormComponent orderForm = (FormComponent) view.getComponentByReference(L_FORM);

        Long orderId = orderForm.getEntityId();

        if (orderId != null) {
            Entity order = orderForm.getEntity().getDataDefinition().get(orderId);

            if ((order != null) && (order.getBelongsToField(OrderFields.TECHNOLOGY) != null)) {
                disabledButton(view, true);

                return;
            }
        }

        disabledButton(view, false);
    }

    private void disabledButton(final ViewDefinitionState view, final boolean enable) {
        WindowComponent window = (WindowComponent) view.getComponentByReference(L_WINDOW);
        RibbonGroup group = (RibbonGroup) window.getRibbon().getGroupByName(L_OPERATION_DURATION);

        RibbonActionItem operationDuration = (RibbonActionItem) group.getItemByName(L_OPERATION_DURATION);
        operationDuration.setEnabled(enable);
        operationDuration.requestUpdate(true);
    }

}
