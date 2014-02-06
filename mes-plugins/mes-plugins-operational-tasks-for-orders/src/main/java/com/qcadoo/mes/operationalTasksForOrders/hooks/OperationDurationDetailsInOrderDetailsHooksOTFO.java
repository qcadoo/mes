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
package com.qcadoo.mes.operationalTasksForOrders.hooks;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.states.constants.OrderStateStringValues;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.api.ribbon.RibbonGroup;

@Service
public class OperationDurationDetailsInOrderDetailsHooksOTFO {

    private static final String L_FORM = "form";

    private static final String L_WINDOW = "window";

    private static final String L_OPERATIONAL_TASKS = "operationalTasks";

    private static final String L_CREATE_OPERATIONAL_TASKS = "createOperationalTasks";

    private static final String L_GENERATED_END_DATE = "generatedEndDate";

    public void disableCreateButton(final ViewDefinitionState view) {
        WindowComponent window = (WindowComponent) view.getComponentByReference(L_WINDOW);

        RibbonGroup operationalTasks = window.getRibbon().getGroupByName(L_OPERATIONAL_TASKS);
        RibbonActionItem createOperationalTasks = operationalTasks.getItemByName(L_CREATE_OPERATIONAL_TASKS);

        if (isGenerated(view) && orderHasCorrectState(view)) {
            createOperationalTasks.setEnabled(true);
        } else {
            createOperationalTasks.setEnabled(false);
        }

        createOperationalTasks.requestUpdate(true);
    }

    private boolean isGenerated(final ViewDefinitionState view) {
        FieldComponent generatedEndDateField = (FieldComponent) view.getComponentByReference(L_GENERATED_END_DATE);

        return !StringUtils.isEmpty((String) generatedEndDateField.getFieldValue());
    }

    private boolean orderHasCorrectState(final ViewDefinitionState viewDefinitionState) {
        FormComponent orderForm = (FormComponent) viewDefinitionState.getComponentByReference(L_FORM);

        Long orderId = orderForm.getEntityId();

        if (orderId == null) {
            return false;
        }

        Entity order = orderForm.getEntity().getDataDefinition().get(orderId);

        String state = order.getStringField(OrderFields.STATE);

        return (OrderStateStringValues.PENDING.equals(state) || OrderStateStringValues.ACCEPTED.equals(state)
                || OrderStateStringValues.IN_PROGRESS.equals(state) || OrderStateStringValues.INTERRUPTED.equals(state));
    }

}
