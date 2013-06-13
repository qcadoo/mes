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

import static com.qcadoo.mes.basicProductionCounting.constants.BasicProductionCountingFields.ORDER;
import static com.qcadoo.mes.basicProductionCounting.constants.BasicProductionCountingFields.PLANNED_QUANTITY;
import static com.qcadoo.mes.basicProductionCounting.constants.BasicProductionCountingFields.PRODUCED_QUANTITY;
import static com.qcadoo.mes.basicProductionCounting.constants.BasicProductionCountingFields.PRODUCT;
import static com.qcadoo.mes.basicProductionCounting.constants.BasicProductionCountingFields.USED_QUANTITY;
import static com.qcadoo.mes.basicProductionCounting.constants.OrderFieldsBPC.PRODUCTION_COUNTING_QUANTITIES;
import static com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityFields.OPERATION_PRODUCT_OUT_COMPONENT;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
        basicProductionCounting.setField(PLANNED_QUANTITY, numberService.setScale(getPlannedQuantity(basicProductionCounting)));
    }

    private BigDecimal getPlannedQuantity(final Entity basicProductionCounting) {
        BigDecimal plannedQuantity = BigDecimal.ZERO;

        Entity order = basicProductionCounting.getBelongsToField(ORDER);
        Entity product = basicProductionCounting.getBelongsToField(PRODUCT);

        List<Entity> productionCountingQuantities = order.getHasManyField(PRODUCTION_COUNTING_QUANTITIES).find()
                .add(SearchRestrictions.isNull(OPERATION_PRODUCT_OUT_COMPONENT))
                .add(SearchRestrictions.belongsTo(PRODUCT, product)).list().getEntities();

        for (Entity productionCountingQuantity : productionCountingQuantities) {
            BigDecimal productionCountingQuantityPlannedQuantity = productionCountingQuantity.getDecimalField(PLANNED_QUANTITY);

            if (productionCountingQuantityPlannedQuantity != null) {
                plannedQuantity = plannedQuantity.add(productionCountingQuantityPlannedQuantity, numberService.getMathContext());
            }
        }

        return plannedQuantity;
    }

    public boolean checkValueOfQuantity(final DataDefinition basicProductionCountingDD, final Entity basicProductionCounting) {
        BigDecimal usedQuantity = (BigDecimal) basicProductionCounting.getField(USED_QUANTITY);
        BigDecimal producedQuantity = (BigDecimal) basicProductionCounting.getField(PRODUCED_QUANTITY);

        if (usedQuantity == null && producedQuantity == null) {
            return true;
        }

        if (usedQuantity != null && usedQuantity.compareTo(BigDecimal.ZERO) == -1) {
            basicProductionCounting.addError(basicProductionCountingDD.getField(USED_QUANTITY),
                    "basic.production.counting.value.lower.zero");
        }

        if (producedQuantity != null && producedQuantity.compareTo(BigDecimal.ZERO) == -1) {
            basicProductionCounting.addError(basicProductionCountingDD.getField(PRODUCED_QUANTITY),
                    "basic.production.counting.value.lower.zero");
        }

        if (!basicProductionCounting.getGlobalErrors().isEmpty() || !basicProductionCounting.getErrors().isEmpty()) {
            return false;
        }

        return true;
    }

}
