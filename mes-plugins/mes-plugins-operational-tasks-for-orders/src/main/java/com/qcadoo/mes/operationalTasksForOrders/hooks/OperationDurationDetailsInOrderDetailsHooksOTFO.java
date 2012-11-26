/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.2.0-SNAPSHOT
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

import static com.qcadoo.mes.orders.constants.OrderFields.STATE;
import static com.qcadoo.mes.orders.states.constants.OrderState.ACCEPTED;
import static com.qcadoo.mes.orders.states.constants.OrderState.INTERRUPTED;
import static com.qcadoo.mes.orders.states.constants.OrderState.IN_PROGRESS;
import static com.qcadoo.mes.orders.states.constants.OrderState.PENDING;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.orders.states.constants.OrderState;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.ribbon.RibbonActionItem;

@Service
public class OperationDurationDetailsInOrderDetailsHooksOTFO {

    public void disabledCreateButton(final ViewDefinitionState viewDefinitionState) {
        WindowComponent window = (WindowComponent) viewDefinitionState.getComponentByReference("window");
        RibbonActionItem createOperationalTasks = window.getRibbon().getGroupByName("operationalTasks")
                .getItemByName("createOperationalTasks");
        if (isGenerated(viewDefinitionState) && orderHasCorrectState(viewDefinitionState)) {
            createOperationalTasks.setEnabled(true);
        } else {
            createOperationalTasks.setEnabled(false);
        }
        createOperationalTasks.requestUpdate(true);
    }

    private boolean isGenerated(final ViewDefinitionState viewDefinitionState) {
        FieldComponent generatedEndDate = (FieldComponent) viewDefinitionState.getComponentByReference("generatedEndDate");
        return !StringUtils.isEmpty(generatedEndDate.getFieldValue().toString());
    }

    private boolean orderHasCorrectState(final ViewDefinitionState viewDefinitionState) {
        FormComponent form = (FormComponent) viewDefinitionState.getComponentByReference("form");
        Entity order = form.getEntity().getDataDefinition().get(form.getEntityId());
        OrderState orderState = OrderState.parseString(order.getStringField(STATE));
        return (orderState.equals(PENDING) || orderState.equals(ACCEPTED) || orderState.equals(IN_PROGRESS) || orderState
                .equals(INTERRUPTED));
    }
}
