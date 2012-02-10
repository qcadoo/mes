/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.2
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
package com.qcadoo.mes.costCalculation;

import static com.google.common.base.Preconditions.checkArgument;
import static com.qcadoo.mes.costNormsForOperation.constants.OperationsCostCalculationConstants.HOURLY;

import java.math.BigDecimal;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Sets;
import com.qcadoo.mes.costNormsForOperation.OperationsCostCalculationService;
import com.qcadoo.mes.costNormsForOperation.constants.OperationsCostCalculationConstants;
import com.qcadoo.mes.costNormsForProduct.ProductsCostCalculationService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;

@Service
public class CostCalculationServiceImpl implements CostCalculationService {

    @Autowired
    private OperationsCostCalculationService operationsCostCalculationService;

    @Autowired
    private ProductsCostCalculationService productsCostCalculationService;

    @Autowired
    private NumberService numberService;

    public boolean clearGeneratedOnCopy(final DataDefinition dataDefinition, final Entity entity) {
        entity.setField("fileName", null);
        entity.setField("generated", false);
        entity.setField("dateOfCalculation", null);
        return true;
    }

    @Override
    public Entity calculateTotalCost(final Entity costCalculation) {

        BigDecimal productionCosts;
        BigDecimal materialCostMargin = getBigDecimal(costCalculation.getField("materialCostMargin"));
        BigDecimal productionCostMargin = getBigDecimal(costCalculation.getField("productionCostMargin"));
        BigDecimal additionalOverhead = getBigDecimal(costCalculation.getField("additionalOverhead"));
        BigDecimal quantity = getBigDecimal(costCalculation.getField("quantity"));
        OperationsCostCalculationConstants operationMode = getOperationModeFromField(costCalculation
                .getField("calculateOperationCostsMode"));

        // clear the previous results
        for (String fieldName : Sets.newHashSet("totalMachineHourlyCosts", "totalLaborHourlyCosts", "totalPieceworkCosts")) {
            costCalculation.setField(fieldName, numberService.setScale(BigDecimal.ZERO));
        }

        costCalculation.setField("dateOfCalculation", new Date());
        operationsCostCalculationService.calculateOperationsCost(costCalculation);
        productsCostCalculationService.calculateProductsCost(costCalculation);

        if (operationMode == HOURLY) {
            BigDecimal totalMachine = getBigDecimal(costCalculation.getField("totalMachineHourlyCosts"));
            BigDecimal totalLabor = getBigDecimal(costCalculation.getField("totalLaborHourlyCosts"));
            productionCosts = totalMachine.add(totalLabor, numberService.getMathContext());
        } else {
            productionCosts = getBigDecimal(costCalculation.getField("totalPieceworkCosts"));
        }

        BigDecimal materialCosts = getBigDecimal(costCalculation.getField("totalMaterialCosts"));
        BigDecimal productionCostMarginValue = productionCosts.multiply(productionCostMargin, numberService.getMathContext())
                .divide(BigDecimal.valueOf(100), numberService.getMathContext());
        BigDecimal materialCostMarginValue = materialCosts.multiply(materialCostMargin, numberService.getMathContext()).divide(
                BigDecimal.valueOf(100), numberService.getMathContext());
        BigDecimal totalTechnicalProductionCosts = productionCosts.add(materialCosts, numberService.getMathContext());
        BigDecimal totalOverhead = productionCostMarginValue.add(materialCostMarginValue, numberService.getMathContext()).add(
                additionalOverhead, numberService.getMathContext());
        BigDecimal totalCosts = totalOverhead.add(totalTechnicalProductionCosts, numberService.getMathContext());

        costCalculation.setField("productionCostMarginValue", productionCostMarginValue);
        costCalculation.setField("productionCostMarginValue", productionCostMarginValue);
        costCalculation.setField("materialCostMarginValue", materialCostMarginValue);
        costCalculation.setField("additionalOverheadValue", additionalOverhead);
        costCalculation.setField("totalOverhead", totalOverhead);
        costCalculation.setField("totalTechnicalProductionCosts", totalTechnicalProductionCosts);
        costCalculation.setField("totalCosts", totalCosts);
        costCalculation.setField("totalCostsPerUnit", totalCosts.divide(quantity, numberService.getMathContext()));

        return costCalculation.getDataDefinition().save(costCalculation);
    }

    private OperationsCostCalculationConstants getOperationModeFromField(final Object value) {
        checkArgument(value != null, "field value is null");
        return OperationsCostCalculationConstants.valueOf(value.toString().toUpperCase());
    }

    private BigDecimal getBigDecimal(final Object value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        }

        // MAKU - using BigDecimal.valueOf(Double) instead of new BigDecimal(String) to prevent issue described at
        // https://forums.oracle.com/forums/thread.jspa?threadID=2251030
        return BigDecimal.valueOf(Double.valueOf(value.toString()));
    }
}
