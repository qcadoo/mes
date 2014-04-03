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
<<<<<<< HEAD:mes-plugins/mes-plugins-production-counting/src/main/java/com/qcadoo/mes/productionCounting/hooks/TrackingOperationProductInComponentHooks.java
package com.qcadoo.mes.productionCounting.hooks;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityRole;
import com.qcadoo.mes.productionCounting.constants.TrackingOperationProductInComponentFields;
import com.qcadoo.mes.productionCounting.hooks.helpers.AbstractPlannedQuantitiesCounter;
=======
package com.qcadoo.mes.costCalculation.hooks;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.costCalculation.constants.CostCalculationFields;
>>>>>>> master:mes-plugins/mes-plugins-cost-calculation/src/main/java/com/qcadoo/mes/costCalculation/hooks/CostCalculationModelHooks.java
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class TrackingOperationProductInComponentHooks extends AbstractPlannedQuantitiesCounter {

<<<<<<< HEAD:mes-plugins/mes-plugins-production-counting/src/main/java/com/qcadoo/mes/productionCounting/hooks/TrackingOperationProductInComponentHooks.java
    public TrackingOperationProductInComponentHooks() {
        super(ProductionCountingQuantityRole.USED);
    }

    public void onView(final DataDefinition trackingOperationProductInComponentDD,
            final Entity trackingOperationProductInComponent) {
        fillPlannedQuantity(trackingOperationProductInComponent);
    }

    private void fillPlannedQuantity(final Entity trackingOperationProductInComponent) {
        trackingOperationProductInComponent.setField(TrackingOperationProductInComponentFields.PLANNED_QUANTITY,
                getPlannedQuantity(trackingOperationProductInComponent));
    }

}
=======
    private static final List<String> L_COST_CALCULATION_COST_FIELDS = Arrays.asList(CostCalculationFields.TOTAL_MATERIAL_COSTS,
            CostCalculationFields.TOTAL_MACHINE_HOURLY_COSTS, CostCalculationFields.TOTAL_LABOR_HOURLY_COSTS,
            CostCalculationFields.TOTAL_PIECEWORK_COSTS, CostCalculationFields.TOTAL_TECHNICAL_PRODUCTION_COSTS,
            CostCalculationFields.PRODUCTION_COST_MARGIN_VALUE, CostCalculationFields.MATERIAL_COST_MARGIN_VALUE,
            CostCalculationFields.ADDITIONAL_OVERHEAD_VALUE, CostCalculationFields.TOTAL_OVERHEAD,
            CostCalculationFields.TOTAL_COSTS, CostCalculationFields.TOTAL_COST_PER_UNIT);

    public void clearGeneratedOnCopy(final DataDefinition costCalculationDD, final Entity costCalculation) {
        costCalculation.setField(CostCalculationFields.FILE_NAME, null);
        costCalculation.setField(CostCalculationFields.GENERATED, false);
        costCalculation.setField(CostCalculationFields.DATE, null);
    }

    public void clearGeneratedCosts(final DataDefinition costCalculationDD, final Entity costCalculation) {
        for (String fieldName : L_COST_CALCULATION_COST_FIELDS) {
            costCalculation.setField(fieldName, BigDecimal.ZERO);
        }
    }

}
>>>>>>> master:mes-plugins/mes-plugins-cost-calculation/src/main/java/com/qcadoo/mes/costCalculation/hooks/CostCalculationModelHooks.java
