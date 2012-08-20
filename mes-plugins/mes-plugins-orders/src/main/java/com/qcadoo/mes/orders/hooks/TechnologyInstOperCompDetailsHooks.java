/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.7
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

import static com.qcadoo.plugin.api.PluginUtils.isEnabled;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.states.constants.OrderState;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.api.ribbon.RibbonGroup;

@Service
public class TechnologyInstOperCompDetailsHooks {

    public void disabledFormWhenOrderStateIsAccepted(final ViewDefinitionState viewDefinitionState) {
        FormComponent form = (FormComponent) viewDefinitionState.getComponentByReference("form");
        Entity techInstOperComp = form.getEntity().getDataDefinition().get(form.getEntityId());
        Entity order = techInstOperComp.getBelongsToField("order");
        WindowComponent windowComponent = (WindowComponent) viewDefinitionState.getComponentByReference("window");
        RibbonGroup standardFormTemplate = windowComponent.getRibbon().getGroupByName("actions");
        if (!order.getStringField(OrderFields.STATE).equals(OrderState.PENDING.getStringValue())) {
            form.setFormEnabled(false);
            for (RibbonActionItem item : standardFormTemplate.getItems()) {
                if (!item.getName().equals("refresh")) {
                    item.setEnabled(false);
                    item.requestUpdate(true);
                }
            }
            if (isEnabled("costNormsForOperation")) {
                disabledButton(windowComponent, "costs", "copyCostsFromTechnology");
            }
            if (isEnabled("timeNormsForOperations")) {
                disabledButton(windowComponent, "norm", "copyTimeNormsFromTechnology");
            }
        }
    }

    private void disabledButton(final WindowComponent windowComponent, final String ribbonGroup, final String ribbonActionItem) {
        RibbonGroup group = windowComponent.getRibbon().getGroupByName(ribbonGroup);
        RibbonActionItem actionItem = group.getItemByName(ribbonActionItem);
        actionItem.setEnabled(false);
        actionItem.requestUpdate(true);
    }
}
