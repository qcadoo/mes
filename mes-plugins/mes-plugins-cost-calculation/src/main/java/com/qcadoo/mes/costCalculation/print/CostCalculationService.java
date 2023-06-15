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
package com.qcadoo.mes.costCalculation.print;

import com.qcadoo.mes.costCalculation.constants.CalculationResultFields;
import com.qcadoo.mes.costCalculation.constants.CostCalculationConstants;
import com.qcadoo.mes.costCalculation.constants.CostCalculationFields;
import com.qcadoo.mes.costCalculation.print.dto.CostCalculationMaterialBySize;
import com.qcadoo.mes.technologies.constants.OperationProductOutComponentFields;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.mes.timeNormsForOperations.constants.TechnologyOperationComponentFieldsTNFO;
import com.qcadoo.model.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CostCalculationService {

    @Autowired
    private NumberService numberService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    private final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);

    public Entity createCalculationResults(Entity costCalculation, Entity technology, final BigDecimal materialCosts,
            final BigDecimal labourCost, final BigDecimal additionalProductsQuantity, boolean noMaterialPrice) {
        DataDefinition calculationResultDD = dataDefinitionService.get(CostCalculationConstants.PLUGIN_IDENTIFIER,
                CostCalculationConstants.MODEL_CALCULATION_RESULT);
        Entity calculationResult = calculationResultDD.create();

        final BigDecimal quantity = getEffectiveQuantity(costCalculation, technology);
        calculateResults(costCalculation, calculationResult, quantity, materialCosts, labourCost);

        calculationResult.setField(CalculationResultFields.COST_CALCULATION, costCalculation);
        calculationResult.setField(CalculationResultFields.TECHNOLOGY, technology);
        calculationResult.setField(CalculationResultFields.PRODUCT, technology.getBelongsToField(TechnologyFields.PRODUCT));
        calculationResult.setField(CalculationResultFields.NO_MATERIAL_PRICE, noMaterialPrice);
        calculationResult.setField(CalculationResultFields.MATERIAL_COSTS,
                numberService.setScaleWithDefaultMathContext(materialCosts, 2));
        calculationResult.setField(CalculationResultFields.LABOUR_COST,
                numberService.setScaleWithDefaultMathContext(labourCost, 2));
        Entity calculationResultSaved = calculationResultDD.save(calculationResult);
        calculationResultSaved.setField(CalculationResultFields.ADDITIONAL_PRODUCTS_QUANTITY, additionalProductsQuantity);
        return calculationResultSaved;
    }

    private void calculateResults(final Entity costCalculation, final Entity calculationResult, final BigDecimal quantity,
            final BigDecimal materialCosts, final BigDecimal labourCost) {
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

        final BigDecimal technicalProductionCostOverheadValue = technicalProductionCost.multiply(
                BigDecimalUtils.convertNullToZero(
                        costCalculation.getDecimalField(CostCalculationFields.TECHNICAL_PRODUCTION_COST_OVERHEAD)),
                numberService.getMathContext()).divide(ONE_HUNDRED, numberService.getMathContext());

        final BigDecimal totalManufacturingCost = BigDecimalUtils.convertNullToZero(technicalProductionCost)
                .add(technicalProductionCostOverheadValue, numberService.getMathContext());

        final BigDecimal profitValue = totalManufacturingCost
                .multiply(BigDecimalUtils.convertNullToZero(costCalculation.getDecimalField(CostCalculationFields.PROFIT)),
                        numberService.getMathContext())
                .divide(ONE_HUNDRED, numberService.getMathContext());

        calculationResult.setField(CalculationResultFields.REGISTRATION_PRICE_OVERHEAD_VALUE,
                numberService.setScaleWithDefaultMathContext(registrationPriceOverheadValue));
        calculationResult.setField(CalculationResultFields.TECHNICAL_PRODUCTION_COST,
                numberService.setScaleWithDefaultMathContext(technicalProductionCost, 2));
        calculationResult.setField(CalculationResultFields.TECHNICAL_PRODUCTION_COST_OVERHEAD_VALUE,
                numberService.setScaleWithDefaultMathContext(technicalProductionCostOverheadValue));
        calculationResult.setField(CalculationResultFields.TOTAL_MANUFACTURING_COST,
                numberService.setScaleWithDefaultMathContext(totalManufacturingCost, 2));
        calculationResult.setField(CalculationResultFields.PROFIT_VALUE,
                numberService.setScaleWithDefaultMathContext(profitValue));

        final BigDecimal sellingPrice = BigDecimalUtils.convertNullToZero(totalManufacturingCost)
                .add(BigDecimalUtils.convertNullToZero(profitValue), numberService.getMathContext());

        calculationResult.setField(CalculationResultFields.SELLING_PRICE,
                numberService.setScaleWithDefaultMathContext(sellingPrice, 2));

    }

    private BigDecimal getEffectiveQuantity(final Entity costCalculation, final Entity technology) {
        BigDecimal effectiveQuantity = costCalculation.getDecimalField(CostCalculationFields.QUANTITY);
        Entity rootOperation = technology.getTreeField(TechnologyFields.OPERATION_COMPONENTS).getRoot();

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

    public List<CostCalculationMaterialBySize> getMaterialsBySize(Entity costCalculation) {
        List<Long> technologiesIds = costCalculation.getHasManyField(CostCalculationFields.TECHNOLOGIES).stream()
                .map(Entity::getId).collect(Collectors.toList());

        String query = "SELECT t.number AS technologyNumber, p.number AS productNumber, "
                + "tipt.name AS technologyInputProductType, pbsgp.id AS materialId, "
                + "sg.number AS sizeGroupNumber, pbsg.quantity as quantity, pbsgp.unit as unit "
                + "FROM technologies_technology t JOIN basic_product p ON t.product_id = p.id "
                + "JOIN technologies_technologyoperationcomponent toc ON toc.technology_id = t.id "
                + "JOIN technologies_operationproductincomponent opic ON opic.operationcomponent_id = toc.id "
                + "JOIN technologies_technologyinputproducttype tipt ON opic.technologyinputproducttype_id = tipt.id "
                + "JOIN technologies_productbysizegroup pbsg ON pbsg.operationproductincomponent_id = opic.id "
                + "JOIN basic_product pbsgp ON pbsg.product_id = pbsgp.id "
                + "JOIN basic_sizegroup sg ON pbsg.sizegroup_id = sg.id WHERE t.id IN (:technologiesIds) "
                + "ORDER BY technologyNumber, pbsgp.number ";
        return jdbcTemplate.query(query, new MapSqlParameterSource("technologiesIds", technologiesIds),
                BeanPropertyRowMapper.newInstance(CostCalculationMaterialBySize.class));
    }
}
