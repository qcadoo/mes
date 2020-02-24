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
package com.qcadoo.mes.productionPerShift.factory;

import com.qcadoo.mes.productionPerShift.constants.BalanceFields;
import com.qcadoo.mes.productionPerShift.constants.ProductionPerShiftConstants;
import com.qcadoo.mes.productionPerShift.domain.ProductionProgressScope;
import com.qcadoo.mes.productionPerShift.domain.QuantitiesBalance;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;

@Service
public class DefaultBalanceEntityFactory implements BalanceEntityFactory {

    private static final int REGISTERED_QUANTITY_SCALE = 5;

    private static final int QUANTITY_DIFFERENCE_SCALE = 5;

    private static final int PERCENTAGE_DEVIATION_SCALE = 5;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NumberService numberService;

    @Override
    public Entity create(final ProductionProgressScope scope, final QuantitiesBalance balance) {
        Entity balanceEntity = getDataDefinition().create();
        MathContext mathContext = numberService.getMathContext();
        balanceEntity.setField(BalanceFields.SHIFT_NAME, scope.getShift().getName());
        balanceEntity.setField(BalanceFields.ORDER_NUMBER, scope.getOrder().getNumber());
        balanceEntity.setField(BalanceFields.PRODUCT_NUMBER, scope.getProduct().getNumber());
        balanceEntity.setField(BalanceFields.PRODUCT_UNIT, scope.getProduct().getUnit());
        balanceEntity.setField(BalanceFields.DAY, scope.getDay().toDate());

        // this field relays on the default scale/precision bounds
        BigDecimal plannedQuantity = numberService.setScaleWithDefaultMathContext(balance.getPlanned());
        balanceEntity.setField(BalanceFields.PLANNED_QUANTITY, plannedQuantity);

        BigDecimal registeredQuantity = setScale(balance.getRegistered(), REGISTERED_QUANTITY_SCALE, mathContext);
        balanceEntity.setField(BalanceFields.REGISTERED_QUANTITY, registeredQuantity);

        BigDecimal difference = setScale(balance.getDifference(), QUANTITY_DIFFERENCE_SCALE, mathContext);
        balanceEntity.setField(BalanceFields.DIFFERENCE, difference);

        BigDecimal percentageDeviation = setScale(balance.getPercentageDeviation(), PERCENTAGE_DEVIATION_SCALE, mathContext);
        balanceEntity.setField(BalanceFields.PERCENTAGE_DEVIATION, percentageDeviation);

        return balanceEntity;
    }

    // TODO consider NumberService.setScaleWithDefaultMathContext method overloading
    private BigDecimal setScale(final BigDecimal valueOrNull, final int scale, final MathContext mc) {
        if (valueOrNull == null) {
            return null;
        }
        return valueOrNull.setScale(scale, mc.getRoundingMode());
    }

    private DataDefinition getDataDefinition() {
        return dataDefinitionService.get(ProductionPerShiftConstants.PLUGIN_IDENTIFIER,
                ProductionPerShiftConstants.MODEL_BALANCE);
    }

}
