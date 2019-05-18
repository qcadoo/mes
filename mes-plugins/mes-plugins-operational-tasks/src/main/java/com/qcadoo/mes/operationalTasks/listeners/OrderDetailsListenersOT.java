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
package com.qcadoo.mes.operationalTasks.listeners;

import com.google.common.collect.Maps;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrderDetailsListenersOT {

    private static final String L_FORM = "form";

    private static final String L_GRID_OPTIONS = "grid.options";

    private static final String L_FILTERS = "filters";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void showOperationalTasks(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent orderForm = (FormComponent) view.getComponentByReference(L_FORM);

        Long orderId = orderForm.getEntityId();

        if (orderId == null) {
            return;
        }

        Entity orderFromDB = orderForm.getEntity().getDataDefinition().get(orderId);

        Map<String, String> filters = Maps.newHashMap();
        filters.put("orderNumber", applyInOperator(orderFromDB.getStringField(OrderFields.NUMBER)));

        Map<String, Object> gridOptions = Maps.newHashMap();
        gridOptions.put(L_FILTERS, filters);

        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put(L_GRID_OPTIONS, gridOptions);

        String url = "/page/operationalTasks/operationalTasksList.html";
        view.redirectTo(url, false, true, parameters);
    }
    private String applyInOperator(final String value){
        StringBuilder builder = new StringBuilder();
        return builder.append("[").append(value).append("]").toString();
    }

}
