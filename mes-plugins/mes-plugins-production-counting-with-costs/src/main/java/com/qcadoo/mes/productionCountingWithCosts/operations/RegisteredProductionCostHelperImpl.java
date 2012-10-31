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
package com.qcadoo.mes.productionCountingWithCosts.operations;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.costNormsForOperation.constants.CalculateOperationCostMode;
import com.qcadoo.mes.productionCounting.internal.constants.ProductionBalanceFields;
import com.qcadoo.mes.productionCountingWithCosts.constants.ProductionBalanceFieldsPCWC;
import com.qcadoo.mes.productionCountingWithCosts.util.DecimalUtils;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;

@Service
public final class RegisteredProductionCostHelperImpl implements RegisteredProductionCostHelper {

    @Autowired
    private NumberService numberService;

    @Override
    public void countRegisteredProductionMarginValue(final Entity productionBalance) {
        final BigDecimal productionCostMargin = DecimalUtils.nullToZero(productionBalance
                .getDecimalField(ProductionBalanceFieldsPCWC.PRODUCTION_COST_MARGIN));
        final BigDecimal registeredProductionCost = getRegisteredProductionCost(productionBalance);
        final BigDecimal productionCostMarginFactor = DecimalUtils
                .toPercent(productionCostMargin, numberService.getMathContext());
        final BigDecimal registeredProductionMarginValue = registeredProductionCost.multiply(productionCostMarginFactor,
                numberService.getMathContext());

        productionBalance.setField(ProductionBalanceFieldsPCWC.PRODUCTION_COST_MARGIN_VALUE,
                numberService.setScale(registeredProductionMarginValue));
    }

    private BigDecimal getRegisteredProductionCost(final Entity productionBalance) {
        final CalculateOperationCostMode operationCostMode = CalculateOperationCostMode.parseString(productionBalance
                .getStringField(ProductionBalanceFields.CALCULATE_OPERATION_COST_MODE));

        if (CalculateOperationCostMode.HOURLY.equals(operationCostMode)) {
            return getRegisteredHourlyCosts(productionBalance);
        } else if (CalculateOperationCostMode.PIECEWORK.equals(operationCostMode)) {
            return getRegisteredPieceworkCosts(productionBalance);
        } else {
            throw new IllegalArgumentException("Unsupported operation costs counting mode: " + operationCostMode);
        }
    }

    private BigDecimal getRegisteredPieceworkCosts(final Entity productionBalance) {
        return DecimalUtils.nullToZero(productionBalance.getDecimalField(ProductionBalanceFieldsPCWC.CYCLES_COSTS));
    }

    private BigDecimal getRegisteredHourlyCosts(final Entity productionBalance) {
        final BigDecimal registeredMachineCosts = productionBalance.getDecimalField(ProductionBalanceFieldsPCWC.MACHINE_COSTS);
        final BigDecimal registeredLaborCosts = productionBalance.getDecimalField(ProductionBalanceFieldsPCWC.LABOR_COSTS);
        return DecimalUtils.nullToZero(registeredMachineCosts).add(DecimalUtils.nullToZero(registeredLaborCosts),
                numberService.getMathContext());
    }

}
