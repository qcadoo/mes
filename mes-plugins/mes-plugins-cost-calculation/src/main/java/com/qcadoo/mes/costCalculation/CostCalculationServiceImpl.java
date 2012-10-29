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
package com.qcadoo.mes.costCalculation;

import static com.qcadoo.mes.costNormsForOperation.constants.CalculateOperationCostMode.HOURLY;
import static com.qcadoo.mes.costNormsForOperation.constants.CalculateOperationCostMode.PIECEWORK;

import java.math.BigDecimal;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.costCalculation.constants.CostCalculationFields;
import com.qcadoo.mes.costNormsForMaterials.ProductsCostCalculationService;
import com.qcadoo.mes.costNormsForOperation.constants.CalculateOperationCostMode;
import com.qcadoo.mes.operationCostCalculations.OperationsCostCalculationService;
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

    @Override
    public Entity calculateTotalCost(final Entity entity) {
        entity.setField(CostCalculationFields.DATE, new Date());
        // FIXME MAKU beware of side effects - order of computations matter!
        calculateOperationsAndProductsCosts(entity);
        final BigDecimal productionCosts = calculateProductionCost(entity);
        calculateMarginsAndOverheads(entity, productionCosts);
        calculateTotalCosts(entity, productionCosts, entity.getDecimalField(CostCalculationFields.QUANTITY));

        return entity.getDataDefinition().save(entity);
    }

    @Override
    public void calculateOperationsAndProductsCosts(final Entity entity) {
        operationsCostCalculationService.calculateOperationsCost(entity);
        final String sourceOfMaterialCosts = entity.getStringField(CostCalculationFields.SOURCE_OF_MATERIAL_COSTS);
        productsCostCalculationService.calculateTotalProductsCost(entity, sourceOfMaterialCosts);
    }

    @Override
    public void calculateTotalCosts(final Entity entity, final BigDecimal productionCosts, final BigDecimal quantity) {
        final BigDecimal materialCosts = convertNullToZero(entity.getDecimalField(CostCalculationFields.TOTAL_MATERIAL_COSTS));
        final BigDecimal totalOverhead = convertNullToZero(entity.getDecimalField(CostCalculationFields.TOTAL_OVERHEAD));
        final BigDecimal totalTechnicalProductionCosts = productionCosts.add(materialCosts, numberService.getMathContext());
        final BigDecimal totalCosts = totalOverhead.add(totalTechnicalProductionCosts, numberService.getMathContext());

        entity.setField(CostCalculationFields.TOTAL_TECHNICAL_PRODUCTION_COSTS,
                numberService.setScale(totalTechnicalProductionCosts));
        entity.setField(CostCalculationFields.TOTAL_COSTS, numberService.setScale(totalCosts));

        if (BigDecimal.ZERO.compareTo(convertNullToZero(quantity)) != 0) {
            final BigDecimal totalCostsPerUnit = totalCosts.divide(quantity, numberService.getMathContext());
            entity.setField(CostCalculationFields.TOTAL_COST_PER_UNIT, numberService.setScale(totalCostsPerUnit));
        }
    }

    private void calculateMarginsAndOverheads(final Entity entity, final BigDecimal productionCosts) {
        final BigDecimal materialCostMargin = convertNullToZero(entity
                .getDecimalField(CostCalculationFields.MATERIAL_COST_MARGIN));
        final BigDecimal productionCostMargin = convertNullToZero(entity
                .getDecimalField(CostCalculationFields.PRODUCTION_COST_MARGIN));
        final BigDecimal materialCosts = convertNullToZero(entity.getDecimalField(CostCalculationFields.TOTAL_MATERIAL_COSTS));

        final BigDecimal productionCostMarginValue = productionCosts.multiply(productionCostMargin,
                numberService.getMathContext()).divide(BigDecimal.valueOf(100), numberService.getMathContext());
        final BigDecimal materialCostMarginValue = materialCosts.multiply(materialCostMargin, numberService.getMathContext())
                .divide(BigDecimal.valueOf(100), numberService.getMathContext());

        entity.setField(CostCalculationFields.PRODUCTION_COST_MARGIN_VALUE, numberService.setScale(productionCostMarginValue));
        entity.setField(CostCalculationFields.MATERIAL_COST_MARGIN_VALUE, numberService.setScale(materialCostMarginValue));
    }

    @Override
    public void calculateTotalOverhead(final Entity entity) {
        final BigDecimal productionCostMarginValue = convertNullToZero(entity
                .getDecimalField(CostCalculationFields.PRODUCTION_COST_MARGIN_VALUE));
        final BigDecimal materialCostMarginValue = convertNullToZero(entity
                .getDecimalField(CostCalculationFields.MATERIAL_COST_MARGIN_VALUE));
        final BigDecimal additionalOverhead = convertNullToZero(entity.getDecimalField(CostCalculationFields.ADDITIONAL_OVERHEAD));

        final BigDecimal totalOverhead = productionCostMarginValue.add(materialCostMarginValue, numberService.getMathContext())
                .add(additionalOverhead, numberService.getMathContext());

        entity.setField(CostCalculationFields.ADDITIONAL_OVERHEAD_VALUE, numberService.setScale(additionalOverhead));
        entity.setField(CostCalculationFields.TOTAL_OVERHEAD, numberService.setScale(totalOverhead));
    }

    @Override
    public BigDecimal calculateProductionCost(final Entity entity) {
        final CalculateOperationCostMode operationMode = CalculateOperationCostMode.parseString(entity
                .getStringField(CostCalculationFields.CALCULATE_OPERATION_COSTS_MODE));
        BigDecimal productionCosts = BigDecimal.ZERO;
        if (HOURLY.equals(operationMode)) {
            BigDecimal totalMachine = convertNullToZero(entity.getDecimalField(CostCalculationFields.TOTAL_MACHINE_HOURLY_COSTS));
            BigDecimal totalLabor = convertNullToZero(entity.getDecimalField(CostCalculationFields.TOTAL_LABOR_HOURLY_COSTS));
            productionCosts = totalMachine.add(totalLabor, numberService.getMathContext());
        } else if (PIECEWORK.equals(operationMode)) {
            productionCosts = convertNullToZero(entity.getDecimalField(CostCalculationFields.TOTAL_PIECEWORK_COSTS));
        } else {
            throw new IllegalStateException("Unsupported calculateOperationCostsMode");
        }
        return productionCosts;
    }

    private BigDecimal convertNullToZero(final BigDecimal value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        return value;
    }
}
