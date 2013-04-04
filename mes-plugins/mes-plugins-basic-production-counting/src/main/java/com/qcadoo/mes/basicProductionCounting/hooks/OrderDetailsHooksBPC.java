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
package com.qcadoo.mes.basicProductionCounting.hooks;

import static com.qcadoo.mes.orders.constants.OrderFields.STATE;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.basicProductionCounting.constants.BasicProductionCountingConstants;
import com.qcadoo.mes.orders.states.constants.OrderState;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.ribbon.RibbonActionItem;

@Service
public class OrderDetailsHooksBPC {

    private static final String L_WINDOW = "window";

    private static final String L_FORM = "form";

    public void disabledButtonForAppropriateState(final ViewDefinitionState view) {
        FormComponent form = (FormComponent) view.getComponentByReference(L_FORM);

        if (form.getEntity() == null) {
            return;
        }

        WindowComponent window = (WindowComponent) view.getComponentByReference(L_WINDOW);
        RibbonActionItem productionCounting = window.getRibbon()
                .getGroupByName(BasicProductionCountingConstants.VIEW_RIBBON_ACTION_ITEM_GROUP)
                .getItemByName(BasicProductionCountingConstants.VIEW_RIBBON_ACTION_ITEM_NAME);

        Entity order = form.getEntity();
        String state = order.getStringField(STATE);

        if (OrderState.DECLINED.getStringValue().equals(state) || OrderState.PENDING.getStringValue().equals(state)) {
            productionCounting.setEnabled(false);
            productionCounting.requestUpdate(true);
        }
    }

}
