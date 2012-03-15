/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.3
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

import java.math.BigDecimal;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.costNormsForOperation.OperationsCostCalculationService;
import com.qcadoo.mes.costNormsForOperation.constants.CalculateOperationCostMode;
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
        entity.setField("date", null);
        return true;
    }

    @Override
    public Entity calculateTotalCost(final Entity entity) {
        BigDecimal productionCosts;
        BigDecimal materialCostMargin = getBigDecimal(entity.getField("materialCostMargin"));
        BigDecimal productionCostMargin = getBigDecimal(entity.getField("productionCostMargin"));
        BigDecimal additionalOverhead = getBigDecimal(entity.getField("additionalOverhead"));
        BigDecimal quantity = getBigDecimal(entity.getField("quantity"));

        CalculateOperationCostMode operationMode = CalculateOperationCostMode.parseString(entity
                .getStringField("calculateOperationCostsMode"));

        entity.setField("date", new Date());

        operationsCostCalculationService.calculateOperationsCost(entity);
        productsCostCalculationService.calculateTotalProductsCost(entity);

        if (CalculateOperationCostMode.HOURLY.equals(operationMode)) {
            BigDecimal totalMachine = getBigDecimal(entity.getField("totalMachineHourlyCosts"));
            BigDecimal totalLabor = getBigDecimal(entity.getField("totalLaborHourlyCosts"));
            productionCosts = totalMachine.add(totalLabor, numberService.getMathContext());
        } else if (CalculateOperationCostMode.PIECEWORK.equals(operationMode)) {
            productionCosts = getBigDecimal(entity.getField("totalPieceworkCosts"));
        } else {
            throw new IllegalStateException("Unsupported calculateOperationCostsMode");
        }

        BigDecimal materialCosts = getBigDecimal(entity.getField("totalMaterialCosts"));

        BigDecimal productionCostMarginValue = productionCosts.multiply(productionCostMargin, numberService.getMathContext())
                .divide(BigDecimal.valueOf(100), numberService.getMathContext());
        BigDecimal materialCostMarginValue = materialCosts.multiply(materialCostMargin, numberService.getMathContext()).divide(
                BigDecimal.valueOf(100), numberService.getMathContext());

        // TODO mici, I think we should clamp it to get the consisted result, because DB clamps it to the setScale(3) anyway.
        productionCostMarginValue = numberService.setScale(productionCostMarginValue);
        materialCostMarginValue = numberService.setScale(materialCostMarginValue);

        BigDecimal totalTechnicalProductionCosts = productionCosts.add(materialCosts, numberService.getMathContext());
        BigDecimal totalOverhead = productionCostMarginValue.add(materialCostMarginValue, numberService.getMathContext()).add(
                additionalOverhead, numberService.getMathContext());
        BigDecimal totalCosts = totalOverhead.add(totalTechnicalProductionCosts, numberService.getMathContext());

        entity.setField("productionCostMarginValue", numberService.setScale(productionCostMarginValue));
        entity.setField("materialCostMarginValue", numberService.setScale(materialCostMarginValue));
        entity.setField("additionalOverheadValue", numberService.setScale(additionalOverhead));

        entity.setField("totalOverhead", numberService.setScale(totalOverhead));
        entity.setField("totalTechnicalProductionCosts", numberService.setScale(totalTechnicalProductionCosts));
        entity.setField("totalCosts", numberService.setScale(totalCosts));
        entity.setField("totalCostPerUnit", numberService.setScale(totalCosts.divide(quantity, numberService.getMathContext())));

        return entity.getDataDefinition().save(entity);
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
