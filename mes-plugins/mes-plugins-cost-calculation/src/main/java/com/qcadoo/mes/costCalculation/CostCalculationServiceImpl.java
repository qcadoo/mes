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

import com.qcadoo.mes.costCalculation.constants.*;
import com.qcadoo.mes.costNormsForMaterials.ProductsCostCalculationService;
import com.qcadoo.mes.operationCostCalculations.OperationsCostCalculationService;
import com.qcadoo.mes.technologies.constants.OperationProductOutComponentFields;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.mes.technologies.tree.ProductStructureTreeService;
import com.qcadoo.mes.timeNormsForOperations.constants.TechnologyOperationComponentFieldsTNFO;
import com.qcadoo.model.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.List;

@Service
public class CostCalculationServiceImpl implements CostCalculationService {

    @Autowired
    private OperationsCostCalculationService operationsCostCalculationService;

    @Autowired
    private ProductsCostCalculationService productsCostCalculationService;

    @Autowired
    private NumberService numberService;

    @Autowired
    private ProductStructureTreeService productStructureTreeService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    private final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);

    @Override
    public void createCalculationResults(Entity costCalculation, final Entity technology) {
        DataDefinition calculationResultDD = dataDefinitionService.get(CostCalculationConstants.PLUGIN_IDENTIFIER,
                CostCalculationConstants.MODEL_CALCULATION_RESULT);
        Entity calculationResult = calculationResultDD.create();

        calculateResults(costCalculation, technology, calculationResult);

        costCalculation = costCalculation.getDataDefinition().save(costCalculation);

        calculationResult.setField(CalculationResultFields.COST_CALCULATION, costCalculation);
        calculationResult.setField(CalculationResultFields.TECHNOLOGY, technology);
        calculationResult.setField(CalculationResultFields.PRODUCT, technology.getBelongsToField(TechnologyFields.PRODUCT));
        calculationResultDD.save(calculationResult);
    }

    private void calculateResults(final Entity costCalculation, final Entity technology, final Entity calculationResult) {
        productStructureTreeService.generateProductStructureTree(null, technology);
        String sourceOfOperationCosts = costCalculation.getStringField(CostCalculationFields.SOURCE_OF_OPERATION_COSTS);
        BigDecimal labourCost;
        if (SourceOfOperationCosts.STANDARD_LABOR_COSTS.equals(SourceOfOperationCosts.parseString(sourceOfOperationCosts))) {
            labourCost = costCalculation.getBelongsToField(CostCalculationFields.STANDARD_LABOR_COST)
                    .getDecimalField(StandardLaborCostFields.LABOR_COST);
            costCalculation.setField(CostCalculationFields.TOTAL_MACHINE_HOURLY_COSTS, BigDecimal.ZERO);
            costCalculation.setField(CostCalculationFields.TOTAL_LABOR_HOURLY_COSTS, BigDecimal.ZERO);
        } else {
            boolean hourlyCostFromOperation = true;
            if (SourceOfOperationCosts.PARAMETERS.equals(SourceOfOperationCosts.parseString(sourceOfOperationCosts))) {
                hourlyCostFromOperation = false;
            }
            operationsCostCalculationService.calculateOperationsCost(costCalculation, hourlyCostFromOperation, technology);
            labourCost = BigDecimalUtils
                    .convertNullToZero(costCalculation.getDecimalField(CostCalculationFields.TOTAL_MACHINE_HOURLY_COSTS))
                    .add(BigDecimalUtils
                            .convertNullToZero(costCalculation.getDecimalField(CostCalculationFields.TOTAL_LABOR_HOURLY_COSTS)),
                            numberService.getMathContext());
        }
        final BigDecimal materialCosts = BigDecimalUtils
                .convertNullToZero(productsCostCalculationService.calculateTotalProductsCost(costCalculation, technology, calculationResult));
        calculationResult.setField(CalculationResultFields.MATERIAL_COSTS,
                numberService.setScaleWithDefaultMathContext(materialCosts, 2));
        calculationResult.setField(CalculationResultFields.LABOUR_COST,
                numberService.setScaleWithDefaultMathContext(labourCost, 2));

        final BigDecimal labourCostMarginValue = labourCost.multiply(
                BigDecimalUtils.convertNullToZero(costCalculation.getDecimalField(CostCalculationFields.PRODUCTION_COST_MARGIN)),
                numberService.getMathContext()).divide(ONE_HUNDRED, numberService.getMathContext());
        final BigDecimal materialCostMarginValue = materialCosts.multiply(
                BigDecimalUtils.convertNullToZero(costCalculation.getDecimalField(CostCalculationFields.MATERIAL_COST_MARGIN)),
                numberService.getMathContext()).divide(ONE_HUNDRED, numberService.getMathContext());

        calculationResult.setField(CalculationResultFields.LABOUR_COST_MARGIN_VALUE,
                numberService.setScaleWithDefaultMathContext(labourCostMarginValue));
        calculationResult.setField(CalculationResultFields.MATERIAL_COST_MARGIN_VALUE,
                numberService.setScaleWithDefaultMathContext(materialCostMarginValue));

        final BigDecimal productionCosts = labourCost.add(materialCosts, numberService.getMathContext());
        final BigDecimal totalCost = BigDecimalUtils.convertNullToZero(labourCostMarginValue)
                .add(BigDecimalUtils.convertNullToZero(materialCostMarginValue), numberService.getMathContext())
                .add(BigDecimalUtils
                        .convertNullToZero(costCalculation.getDecimalField(CostCalculationFields.ADDITIONAL_OVERHEAD)),
                        numberService.getMathContext())
                .add(productionCosts, numberService.getMathContext());

        calculationResult.setField(CalculationResultFields.PRODUCTION_COSTS,
                numberService.setScaleWithDefaultMathContext(productionCosts, 2));
        calculationResult.setField(CalculationResultFields.TOTAL_COST,
                numberService.setScaleWithDefaultMathContext(totalCost, 2));

        final BigDecimal quantity = getEffectiveQuantity(costCalculation, technology);
        final BigDecimal registrationPrice = totalCost.divide(quantity, numberService.getMathContext());

        calculationResult.setField(CalculationResultFields.REGISTRATION_PRICE,
                numberService.setScaleWithDefaultMathContext(registrationPrice, 2));

        final BigDecimal registrationPriceOverheadValue = BigDecimalUtils.convertNullToZero(registrationPrice)
                .multiply(
                        BigDecimalUtils.convertNullToZero(
                                costCalculation.getDecimalField(CostCalculationFields.REGISTRATION_PRICE_OVERHEAD)),
                        numberService.getMathContext())
                .divide(ONE_HUNDRED, numberService.getMathContext());

        final BigDecimal technicalProductionCost = BigDecimalUtils.convertNullToZero(registrationPrice)
                .add(registrationPriceOverheadValue, numberService.getMathContext());

        final BigDecimal profitValue = technicalProductionCost
                .multiply(BigDecimalUtils.convertNullToZero(costCalculation.getDecimalField(CostCalculationFields.PROFIT)),
                        numberService.getMathContext())
                .divide(ONE_HUNDRED, numberService.getMathContext());

        calculationResult.setField(CalculationResultFields.REGISTRATION_PRICE_OVERHEAD_VALUE,
                numberService.setScaleWithDefaultMathContext(registrationPriceOverheadValue));
        calculationResult.setField(CalculationResultFields.TECHNICAL_PRODUCTION_COST,
                numberService.setScaleWithDefaultMathContext(technicalProductionCost, 2));
        calculationResult.setField(CalculationResultFields.PROFIT_VALUE,
                numberService.setScaleWithDefaultMathContext(profitValue));

        final BigDecimal sellingPrice = BigDecimalUtils.convertNullToZero(technicalProductionCost)
                .add(BigDecimalUtils.convertNullToZero(profitValue), numberService.getMathContext());

        calculationResult.setField(CalculationResultFields.SELLING_PRICE,
                numberService.setScaleWithDefaultMathContext(sellingPrice, 2));

    }

    private BigDecimal getEffectiveQuantity(final Entity entity, final Entity technology) {
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
}