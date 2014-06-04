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
package com.qcadoo.mes.productionCountingWithCosts.hooks;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.productionCountingWithCosts.constants.ProductionBalanceFieldsPCWC;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class ProductionBalanceHooksPCWC {

    private static final List<String> L_PRODUCTION_BALANCE_COST_FIELDS = Arrays.asList(
            ProductionBalanceFieldsPCWC.PLANNED_COMPONENTS_COSTS, ProductionBalanceFieldsPCWC.COMPONENTS_COSTS,
            ProductionBalanceFieldsPCWC.COMPONENTS_COSTS_BALANCE, ProductionBalanceFieldsPCWC.PLANNED_MACHINE_COSTS,
            ProductionBalanceFieldsPCWC.MACHINE_COSTS, ProductionBalanceFieldsPCWC.MACHINE_COSTS_BALANCE,
            ProductionBalanceFieldsPCWC.PLANNED_LABOR_COSTS, ProductionBalanceFieldsPCWC.LABOR_COSTS,
            ProductionBalanceFieldsPCWC.LABOR_COSTS_BALANCE, ProductionBalanceFieldsPCWC.PLANNED_CYCLES_COSTS,
            ProductionBalanceFieldsPCWC.CYCLES_COSTS, ProductionBalanceFieldsPCWC.CYCLES_COSTS_BALANCE,
            ProductionBalanceFieldsPCWC.REGISTERED_TOTAL_TECHNICAL_PRODUCTION_COSTS,
            ProductionBalanceFieldsPCWC.REGISTERED_TOTAL_TECHNICAL_PRODUCTION_COST_PER_UNIT,
            ProductionBalanceFieldsPCWC.TOTAL_TECHNICAL_PRODUCTION_COSTS,
            ProductionBalanceFieldsPCWC.TOTAL_TECHNICAL_PRODUCTION_COST_PER_UNIT,
            ProductionBalanceFieldsPCWC.BALANCE_TECHNICAL_PRODUCTION_COSTS,
            ProductionBalanceFieldsPCWC.BALANCE_TECHNICAL_PRODUCTION_COST_PER_UNIT,
            ProductionBalanceFieldsPCWC.PRODUCTION_COST_MARGIN_VALUE, ProductionBalanceFieldsPCWC.MATERIAL_COST_MARGIN_VALUE,
            ProductionBalanceFieldsPCWC.ADDITIONAL_OVERHEAD_VALUE, ProductionBalanceFieldsPCWC.TOTAL_OVERHEAD,
            ProductionBalanceFieldsPCWC.TOTAL_COSTS, ProductionBalanceFieldsPCWC.TOTAL_COST_PER_UNIT);

    public void onCopy(final DataDefinition productionBalanceDD, final Entity productionBalance) {
        clearGeneratedCosts(productionBalance);
    }

    private void clearGeneratedCosts(final Entity productionBalance) {
        for (String fieldName : L_PRODUCTION_BALANCE_COST_FIELDS) {
            if (ProductionBalanceFieldsPCWC.PRODUCTION_COST_MARGIN_VALUE.equals(fieldName)
                    || ProductionBalanceFieldsPCWC.MATERIAL_COST_MARGIN_VALUE.equals(fieldName)
                    || ProductionBalanceFieldsPCWC.ADDITIONAL_OVERHEAD_VALUE.equals(fieldName)) {
                productionBalance.setField(fieldName, BigDecimal.ZERO);
            } else {
                productionBalance.setField(fieldName, null);
            }
        }
    }

}
