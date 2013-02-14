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
package com.qcadoo.mes.productionCountingWithCosts.materials;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.productionCountingWithCosts.constants.ProductionBalanceFieldsPCWC;
import com.qcadoo.mes.productionCountingWithCosts.util.DecimalUtils;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;

@Service
public final class RegisteredMaterialCostHelperImpl implements RegisteredMaterialCostHelper {

    @Autowired
    private NumberService numberService;

    @Override
    public void countRegisteredMaterialMarginValue(final Entity productionBalance) {
        final BigDecimal materialCostMargin = DecimalUtils.nullToZero(productionBalance
                .getDecimalField(ProductionBalanceFieldsPCWC.MATERIAL_COST_MARGIN));
        final BigDecimal registeredMaterialCost = getRegisteredMaterialCost(productionBalance);
        final BigDecimal materialCostMarginFactor = DecimalUtils.toPercent(materialCostMargin, numberService.getMathContext());
        final BigDecimal registeredMaterialMarginValue = registeredMaterialCost.multiply(materialCostMarginFactor,
                numberService.getMathContext());

        productionBalance.setField(ProductionBalanceFieldsPCWC.MATERIAL_COST_MARGIN_VALUE,
                numberService.setScale(registeredMaterialMarginValue));
    }

    private BigDecimal getRegisteredMaterialCost(final Entity productionBalance) {
        return DecimalUtils.nullToZero(productionBalance.getDecimalField(ProductionBalanceFieldsPCWC.COMPONENTS_COSTS));
    }

}
