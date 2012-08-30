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
package com.qcadoo.mes.wageGroups.hooks;

import static com.qcadoo.mes.wageGroups.constants.WageGroupFields.LABOR_HOURLY_COST;
import static com.qcadoo.mes.wageGroups.constants.WageGroupFields.SUPERIOR_WAGE_GROUP;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.util.CurrencyService;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.LookupComponent;

@Service
public class StaffDetailsHooks {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private CurrencyService currencyService;

    public void enabledIndividualCost(final ViewDefinitionState view) {
        FieldComponent individual = (FieldComponent) view.getComponentByReference("determinedIndividual");
        FieldComponent individualLaborCost = (FieldComponent) view.getComponentByReference("individualLaborCost");
        if (individual.getFieldValue() != null && individual.getFieldValue().equals("1")) {
            individualLaborCost.setEnabled(true);
        } else {
            individualLaborCost.setEnabled(false);
        }
        individualLaborCost.requestComponentUpdateState();
    }

    public void setCurrency(final ViewDefinitionState view) {
        FieldComponent laborHourlyCostUNIT = (FieldComponent) view.getComponentByReference("individualLaborCostCURRENCY");
        FieldComponent laborCostFromWageGroupsUNIT = (FieldComponent) view
                .getComponentByReference("laborCostFromWageGroupsCURRENCY");
        laborHourlyCostUNIT.setFieldValue(currencyService.getCurrencyAlphabeticCode());
        laborCostFromWageGroupsUNIT.setFieldValue(currencyService.getCurrencyAlphabeticCode());
        laborHourlyCostUNIT.requestComponentUpdateState();
        laborCostFromWageGroupsUNIT.requestComponentUpdateState();
    }

    public void fillFieldAboutWageGroup(final ViewDefinitionState view) {
        LookupComponent lookup = (LookupComponent) view.getComponentByReference("wageGroup");
        Entity wageGroup = lookup.getEntity();
        if (wageGroup == null) {
            return;
        }
        FieldComponent laborCostFromWageGroups = (FieldComponent) view.getComponentByReference("laborCostFromWageGroups");
        FieldComponent superiorWageGroups = (FieldComponent) view.getComponentByReference("superiorWageGroups");
        laborCostFromWageGroups.setFieldValue(wageGroup.getField(LABOR_HOURLY_COST));
        superiorWageGroups.setFieldValue(wageGroup.getStringField(SUPERIOR_WAGE_GROUP));
    }

}
