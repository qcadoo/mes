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
package com.qcadoo.mes.avgLaborCostCalcForOrder.hooks;

import static com.qcadoo.mes.avgLaborCostCalcForOrder.constants.AvgLaborCostCalcForOrderFields.AVERAGE_LABOR_HOURLY_COST;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.ribbon.RibbonActionItem;

@Service
public class AvgLaborCostCalcForOrderDetailsHooks {

    public void enabledButtonForCopyNorms(final ViewDefinitionState view) {
        WindowComponent window = (WindowComponent) view.getComponentByReference("window");
        RibbonActionItem copyToOperationsNorms = window.getRibbon().getGroupByName("hourlyCostNorms")
                .getItemByName("copyToOperationsNorms");
        FieldComponent averageLaborHourlyCost = (FieldComponent) view.getComponentByReference(AVERAGE_LABOR_HOURLY_COST);
        if (StringUtils.isEmpty((String) averageLaborHourlyCost.getFieldValue())) {
            copyToOperationsNorms.setEnabled(false);
        } else {
            copyToOperationsNorms.setEnabled(true);
        }
        copyToOperationsNorms.requestUpdate(true);
    }
}
