/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo Framework
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
package com.qcadoo.mes.orderSupplies.hooks;

import com.qcadoo.mes.basic.ParameterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.CheckBoxComponent;
import com.qcadoo.view.api.components.FieldComponent;

@Service
public class SupplyParametersHooksOS {

    public static final String L_REALIZATION_FROM_STOCK = "realizationFromStock";
    public static final String L_CONSIDER_MINIMUM_STOCK_LEVEL_WHEN_CREATING_PRODUCTION_ORDERS = "considerMinimumStockLevelWhenCreatingProductionOrders";

    @Autowired
    private ParameterService parameterService;

    public void onBeforeRender(final ViewDefinitionState view) {
        updateOrdersIncludePeriodState(view);
    }

    public void updateOrdersIncludePeriodState(final ViewDefinitionState view) {
        CheckBoxComponent includeRequirements = (CheckBoxComponent) view.getComponentByReference("includeRequirements");
        FieldComponent ordersIncludePeriod = (FieldComponent) view.getComponentByReference("ordersIncludePeriod");
        boolean shouldIncludeRequirements = includeRequirements.isChecked();
        ordersIncludePeriod.setEnabled(shouldIncludeRequirements);
        if (!shouldIncludeRequirements) {
            ordersIncludePeriod.setFieldValue(null);
        }
        ordersIncludePeriod.requestComponentUpdateState();

        CheckBoxComponent considerMinimumStockLevelWhenCreatingProductionOrders = (CheckBoxComponent) view
                .getComponentByReference(L_CONSIDER_MINIMUM_STOCK_LEVEL_WHEN_CREATING_PRODUCTION_ORDERS);
        if(parameterService.getParameter().getBooleanField(L_REALIZATION_FROM_STOCK)) {
            considerMinimumStockLevelWhenCreatingProductionOrders.setEnabled(true);
        } else {
            considerMinimumStockLevelWhenCreatingProductionOrders.setEnabled(false);
        }

    }
}
