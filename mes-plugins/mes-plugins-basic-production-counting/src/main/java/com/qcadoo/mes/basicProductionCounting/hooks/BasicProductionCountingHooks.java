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

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basicProductionCounting.constants.BasicProductionCountingFields;
import com.qcadoo.mes.basicProductionCounting.constants.OrderFieldsBPC;
import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.model.api.search.SearchRestrictions;

@Service
public class BasicProductionCountingHooks {

    @Autowired
    private NumberService numberService;

    public boolean validatesWith(final DataDefinition basicProductionCountingDD, final Entity basicProductionCounting) {
        return checkValueOfQuantity(basicProductionCountingDD, basicProductionCounting);
    }

    public void onView(final DataDefinition basicProductionCountingDD, final Entity basicProductionCounting) {
        fillPlannedQuantity(basicProductionCountingDD, basicProductionCounting);
    }

    private void fillPlannedQuantity(final DataDefinition basicProductionCountingDD, final Entity basicProductionCounting) {
        basicProductionCounting.setField(BasicProductionCountingFields.PLANNED_QUANTITY,
                numberService.setScale(getPlannedQuantity(basicProductionCounting)));
    }

    private BigDecimal getPlannedQuantity(final Entity basicProductionCounting) {
        BigDecimal plannedQuantity = BigDecimal.ZERO;

        Entity order = basicProductionCounting.getBelongsToField(BasicProductionCountingFields.ORDER);
        Entity product = basicProductionCounting.getBelongsToField(BasicProductionCountingFields.PRODUCT);

        List<Entity> productionCountingQuantities = order.getHasManyField(OrderFieldsBPC.PRODUCTION_COUNTING_QUANTITIES).find()
                .add(SearchRestrictions.isNull(ProductionCountingQuantityFields.OPERATION_PRODUCT_OUT_COMPONENT))
                .add(SearchRestrictions.belongsTo(ProductionCountingQuantityFields.PRODUCT, product)).list().getEntities();

        for (Entity productionCountingQuantity : productionCountingQuantities) {
            BigDecimal productionCountingQuantityPlannedQuantity = productionCountingQuantity
                    .getDecimalField(ProductionCountingQuantityFields.PLANNED_QUANTITY);

            if (productionCountingQuantityPlannedQuantity != null) {
                plannedQuantity = plannedQuantity.add(productionCountingQuantityPlannedQuantity, numberService.getMathContext());
            }
        }

        return plannedQuantity;
    }

    public boolean checkValueOfQuantity(final DataDefinition basicProductionCountingDD, final Entity basicProductionCounting) {
        BigDecimal usedQuantity = basicProductionCounting.getDecimalField(BasicProductionCountingFields.USED_QUANTITY);
        BigDecimal producedQuantity = basicProductionCounting.getDecimalField(BasicProductionCountingFields.PRODUCED_QUANTITY);

        if ((usedQuantity == null) && (producedQuantity == null)) {
            return true;
        }

        if ((usedQuantity != null) && (usedQuantity.compareTo(BigDecimal.ZERO) == -1)) {
            basicProductionCounting.addError(basicProductionCountingDD.getField(BasicProductionCountingFields.USED_QUANTITY),
                    "basic.production.counting.value.lower.zero");
        }

        if ((producedQuantity != null) && (producedQuantity.compareTo(BigDecimal.ZERO) == -1)) {
            basicProductionCounting.addError(basicProductionCountingDD.getField(BasicProductionCountingFields.PRODUCED_QUANTITY),
                    "basic.production.counting.value.lower.zero");
        }

        if (!basicProductionCounting.getGlobalErrors().isEmpty() || !basicProductionCounting.getErrors().isEmpty()) {
            return false;
        }

        return true;
    }

}
