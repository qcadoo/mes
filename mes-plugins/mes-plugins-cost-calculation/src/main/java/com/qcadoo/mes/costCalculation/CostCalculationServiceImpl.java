/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
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
package com.qcadoo.mes.costCalculation;

import static com.qcadoo.mes.costNormsForOperation.constants.CalculateOperationCostMode.HOURLY;
import static com.qcadoo.mes.costNormsForOperation.constants.CalculateOperationCostMode.PIECEWORK;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.costCalculation.constants.CostCalculationFields;
import com.qcadoo.mes.costCalculation.constants.SourceOfOperationCosts;
import com.qcadoo.mes.costNormsForMaterials.ProductsCostCalculationService;
import com.qcadoo.mes.costNormsForOperation.constants.CalculateOperationCostMode;
import com.qcadoo.mes.operationCostCalculations.OperationsCostCalculationService;
import com.qcadoo.mes.technologies.constants.OperationProductOutComponentFields;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.mes.timeNormsForOperations.constants.TechnologyOperationComponentFieldsTNFO;
import com.qcadoo.model.api.BigDecimalUtils;
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

    private final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);

    @Override
    public Entity calculateTotalCost(final Entity entity) {
        entity.setField(CostCalculationFields.DATE, new Date());
        // FIXME MAKU beware of side effects - order of computations matter!
        calculateOperationsAndProductsCosts(entity);
        final BigDecimal productionCosts = calculateProductionCost(entity);
        calculateMarginsAndOverheads(entity, productionCosts);
        final BigDecimal effectiveQuantity = getEffectiveQuantity(entity);

        calculateTotalCosts(entity, productionCosts, effectiveQuantity);

        return entity.getDataDefinition().save(entity);
    }

    @Override
    public void calculateOperationsAndProductsCosts(final Entity entity) {
        boolean hourlyCostFromOperation = true;
        String sourceOfOperationCosts = entity.getStringField("sourceOfOperationCosts");
        if (sourceOfOperationCosts != null
                && SourceOfOperationCosts.PARAMETERS.equals(SourceOfOperationCosts.parseString(sourceOfOperationCosts))) {
            hourlyCostFromOperation = false;
        }
        operationsCostCalculationService.calculateOperationsCost(entity, hourlyCostFromOperation);

        final String sourceOfMaterialCosts = entity.getStringField(CostCalculationFields.SOURCE_OF_MATERIAL_COSTS);

        productsCostCalculationService.calculateTotalProductsCost(entity, sourceOfMaterialCosts);
    }

    @Override
    public void calculateTotalCosts(final Entity entity, final BigDecimal productionCosts, final BigDecimal quantity) {
        final BigDecimal materialCosts = BigDecimalUtils.convertNullToZero(entity
                .getDecimalField(CostCalculationFields.TOTAL_MATERIAL_COSTS));
        final BigDecimal totalOverhead = BigDecimalUtils.convertNullToZero(entity
                .getDecimalField(CostCalculationFields.TOTAL_OVERHEAD));
        final BigDecimal totalTechnicalProductionCosts = productionCosts.add(materialCosts, numberService.getMathContext());
        final BigDecimal totalCosts = totalOverhead.add(totalTechnicalProductionCosts, numberService.getMathContext());

        entity.setField(CostCalculationFields.TOTAL_TECHNICAL_PRODUCTION_COSTS,
                numberService.setScale(totalTechnicalProductionCosts));
        entity.setField(CostCalculationFields.TOTAL_COSTS, numberService.setScale(totalCosts, 2));

        if (BigDecimal.ZERO.compareTo(BigDecimalUtils.convertNullToZero(quantity)) != 0) {
            final BigDecimal totalCostsPerUnit = totalCosts.divide(quantity, numberService.getMathContext());

            entity.setField(CostCalculationFields.TOTAL_COST_PER_UNIT, numberService.setScale(totalCostsPerUnit, 2));

        }
    }

    private BigDecimal getEffectiveQuantity(final Entity entity) {
        Entity technology = entity.getBelongsToField(CostCalculationFields.TECHNOLOGY);
        Entity rootOperation = technology.getTreeField(TechnologyFields.OPERATION_COMPONENTS).getRoot();
        BigDecimal effectiveQuantity = entity.getDecimalField(CostCalculationFields.QUANTITY);

        boolean areProductQuantitiesDivisible = rootOperation
                .getBooleanField(TechnologyOperationComponentFieldsTNFO.ARE_PRODUCT_QUANTITIES_DIVISIBLE);
        if (!areProductQuantitiesDivisible) {
            List<Entity> opocs = rootOperation
                    .getHasManyField(TechnologyOperationComponentFields.OPERATION_PRODUCT_OUT_COMPONENTS);
            if (opocs.size() == 1) {
                Entity opoc = opocs.get(0);
                BigDecimal quantityInSingleCycle = opoc.getDecimalField(OperationProductOutComponentFields.QUANTITY);
                BigDecimal quantity = effectiveQuantity.divide(quantityInSingleCycle, new MathContext(2, RoundingMode.UP));
                effectiveQuantity = quantityInSingleCycle.multiply(quantity, numberService.getMathContext());
            }
        }
        return effectiveQuantity;
    }

    private void calculateMarginsAndOverheads(final Entity entity, final BigDecimal productionCosts) {
        final BigDecimal materialCostMargin = BigDecimalUtils.convertNullToZero(entity
                .getDecimalField(CostCalculationFields.MATERIAL_COST_MARGIN));
        final BigDecimal productionCostMargin = BigDecimalUtils.convertNullToZero(entity
                .getDecimalField(CostCalculationFields.PRODUCTION_COST_MARGIN));
        final BigDecimal materialCosts = BigDecimalUtils.convertNullToZero(entity
                .getDecimalField(CostCalculationFields.TOTAL_MATERIAL_COSTS));

        final BigDecimal productionCostMarginValue = productionCosts.multiply(productionCostMargin,
                numberService.getMathContext()).divide(ONE_HUNDRED, numberService.getMathContext());
        final BigDecimal materialCostMarginValue = materialCosts.multiply(materialCostMargin, numberService.getMathContext())
                .divide(ONE_HUNDRED, numberService.getMathContext());

        entity.setField(CostCalculationFields.PRODUCTION_COST_MARGIN_VALUE, numberService.setScale(productionCostMarginValue));
        entity.setField(CostCalculationFields.MATERIAL_COST_MARGIN_VALUE, numberService.setScale(materialCostMarginValue));

        calculateTotalOverhead(entity);
    }

    @Override
    public void calculateTotalOverhead(final Entity entity) {
        final BigDecimal productionCostMarginValue = BigDecimalUtils.convertNullToZero(entity
                .getDecimalField(CostCalculationFields.PRODUCTION_COST_MARGIN_VALUE));
        final BigDecimal materialCostMarginValue = BigDecimalUtils.convertNullToZero(entity
                .getDecimalField(CostCalculationFields.MATERIAL_COST_MARGIN_VALUE));
        final BigDecimal additionalOverhead = BigDecimalUtils.convertNullToZero(entity
                .getDecimalField(CostCalculationFields.ADDITIONAL_OVERHEAD));

        final BigDecimal totalOverhead = productionCostMarginValue.add(materialCostMarginValue, numberService.getMathContext())
                .add(additionalOverhead, numberService.getMathContext());

        entity.setField(CostCalculationFields.ADDITIONAL_OVERHEAD_VALUE, numberService.setScale(additionalOverhead));
        entity.setField(CostCalculationFields.TOTAL_OVERHEAD, numberService.setScale(totalOverhead));
    }

    @Override
    public BigDecimal calculateProductionCost(final Entity entity) {
        BigDecimal productionCosts = null;

        final CalculateOperationCostMode operationMode = CalculateOperationCostMode.parseString(entity
                .getStringField(CostCalculationFields.CALCULATE_OPERATION_COSTS_MODE));

        if (HOURLY.equals(operationMode)) {
            BigDecimal totalMachine = BigDecimalUtils.convertNullToZero(entity
                    .getDecimalField(CostCalculationFields.TOTAL_MACHINE_HOURLY_COSTS));
            BigDecimal totalLabor = BigDecimalUtils.convertNullToZero(entity
                    .getDecimalField(CostCalculationFields.TOTAL_LABOR_HOURLY_COSTS));
            productionCosts = totalMachine.add(totalLabor, numberService.getMathContext());
        } else if (PIECEWORK.equals(operationMode)) {
            productionCosts = BigDecimalUtils.convertNullToZero(entity
                    .getDecimalField(CostCalculationFields.TOTAL_PIECEWORK_COSTS));
        } else {
            throw new IllegalStateException("Unsupported calculateOperationCostsMode");
        }

        return productionCosts;
    }

    @Override
    public void calculateSellPriceOverhead(final Entity entity) {
        final BigDecimal totalCostPerUnit = BigDecimalUtils.convertNullToZero(entity
                .getDecimalField(CostCalculationFields.TOTAL_COST_PER_UNIT));
        final BigDecimal registrationPriceOverhead = BigDecimalUtils.convertNullToZero(entity
                .getDecimalField(CostCalculationFields.REGISTRATION_PRICE_OVERHEAD));
        final BigDecimal profit = BigDecimalUtils.convertNullToZero(entity.getDecimalField(CostCalculationFields.PROFIT));

        final BigDecimal registrationPriceOverheadValue = totalCostPerUnit.multiply(registrationPriceOverhead,
                numberService.getMathContext()).divide(ONE_HUNDRED, numberService.getMathContext());

        final BigDecimal profitValue = totalCostPerUnit.add(registrationPriceOverheadValue, numberService.getMathContext())
                .multiply(profit, numberService.getMathContext()).divide(ONE_HUNDRED, numberService.getMathContext());

        entity.setField(CostCalculationFields.REGISTRATION_PRICE_OVERHEAD_VALUE,
                numberService.setScale(registrationPriceOverheadValue));
        entity.setField(CostCalculationFields.PROFIT_VALUE, numberService.setScale(profitValue));

        final BigDecimal totalCostsPerUnit = BigDecimalUtils.convertNullToZero(entity
                .getDecimalField(CostCalculationFields.TOTAL_COST_PER_UNIT));
        final BigDecimal technicalProductionCosts = totalCostsPerUnit.add(registrationPriceOverheadValue,
                numberService.getMathContext());
        entity.setField(CostCalculationFields.TECHNICAL_PRODUCTION_COSTS, numberService.setScale(technicalProductionCosts, 2));
    }

    @Override
    public void calculateSellPrice(final Entity entity) {
        final BigDecimal totalCostPerUnit = BigDecimalUtils.convertNullToZero(entity
                .getDecimalField(CostCalculationFields.TOTAL_COST_PER_UNIT));
        final BigDecimal registrationPriceOverheadValue = BigDecimalUtils.convertNullToZero(entity
                .getDecimalField(CostCalculationFields.REGISTRATION_PRICE_OVERHEAD_VALUE));
        final BigDecimal profitValue = BigDecimalUtils.convertNullToZero(entity
                .getDecimalField(CostCalculationFields.PROFIT_VALUE));

        final BigDecimal sellPriceValue = totalCostPerUnit.add(registrationPriceOverheadValue, numberService.getMathContext())
                .add(profitValue, numberService.getMathContext());

        entity.setField(CostCalculationFields.SELL_PRICE_VALUE, numberService.setScale(sellPriceValue, 2));
    }
}
