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
package com.qcadoo.mes.costNormsForMaterials;

import com.qcadoo.mes.costNormsForMaterials.constants.ProductsCostFields;
import com.qcadoo.mes.technologies.ProductQuantitiesService;
import com.qcadoo.mes.technologies.ProductQuantitiesWithComponentsService;
import com.qcadoo.mes.technologies.constants.MrpAlgorithm;
import com.qcadoo.model.api.BigDecimalUtils;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.plugin.api.PluginManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import static com.google.common.base.Preconditions.checkArgument;

@Service
public class ProductsCostCalculationServiceImpl implements ProductsCostCalculationService {

    @Autowired
    private ProductQuantitiesService productQuantitiesService;

    @Autowired
    private ProductQuantitiesWithComponentsService productQuantitiesWithComponentsService;

    @Autowired
    private PluginManager pluginManager;

    @Autowired
    private NumberService numberService;

    @Override
    public BigDecimal calculateTotalProductsCost(final Entity costCalculation, final Entity technology,
            final Entity calculationResult) {
        checkArgument(costCalculation != null);
        Map<Entity, BigDecimal> listProductWithCost = getProductsWithCosts(costCalculation, technology);
        BigDecimal result = BigDecimal.ZERO;
        boolean noMaterialPrice = false;
        for (Entry<Entity, BigDecimal> productWithCost : listProductWithCost.entrySet()) {
            BigDecimal value = productWithCost.getValue();
            if (BigDecimalUtils.valueEquals(value, BigDecimal.ZERO)) {
                noMaterialPrice = true;
            }
            result = result.add(productWithCost.getValue(), numberService.getMathContext());
        }
        calculationResult.setField("noMaterialPrice", noMaterialPrice);
        return numberService.setScaleWithDefaultMathContext(result);
    }

    private Map<Entity, BigDecimal> getProductsWithCosts(final Entity costCalculation, final Entity technology) {
        BigDecimal quantity = BigDecimalUtils.convertNullToZero(costCalculation.getDecimalField("quantity"));
        String materialCostsUsed = costCalculation.getStringField("materialCostsUsed");

        checkArgument(materialCostsUsed != null, "materialCostsUsed is null!");
        boolean useNominalCostPriceNotSpecified = costCalculation.getBooleanField("useNominalCostPriceNotSpecified");
        Map<Long, BigDecimal> neededProductQuantities = getNeededProductQuantities(costCalculation, technology, quantity);
        Map<Entity, BigDecimal> results = new HashMap<>();
        for (Entry<Long, BigDecimal> productQuantity : neededProductQuantities.entrySet()) {
            Entity product = productQuantitiesService.getProduct(productQuantity.getKey());
            BigDecimal thisProductsCost = calculateProductCostForGivenQuantity(product, productQuantity.getValue(),
                    materialCostsUsed, useNominalCostPriceNotSpecified);
            results.put(product, thisProductsCost);
        }
        return results;
    }

    @Override
    public BigDecimal calculateProductCostForGivenQuantity(final Entity product, final BigDecimal quantity,
            final String materialCostsUsed, final boolean useNominalCostPriceNotSpecified) {
        BigDecimal cost = BigDecimalUtils
                .convertNullToZero(product.getField(ProductsCostFields.forMode(materialCostsUsed).getStrValue()));
        if (useNominalCostPriceNotSpecified && BigDecimalUtils.valueEquals(cost, BigDecimal.ZERO)) {
            cost = BigDecimalUtils.convertNullToZero(product.getField(ProductsCostFields.NOMINAL.getStrValue()));
        }
        BigDecimal costForNumber = BigDecimalUtils.convertNullToOne(product.getDecimalField("costForNumber"));
        if (BigDecimalUtils.valueEquals(costForNumber, BigDecimal.ZERO)) {
            costForNumber = BigDecimal.ONE;
        }
        BigDecimal costPerUnit = cost.divide(costForNumber, numberService.getMathContext());

        return costPerUnit.multiply(quantity, numberService.getMathContext());
    }

    public Map<Long, BigDecimal> getNeededProductQuantities(final Entity costCalculation, final Entity technology,
            final BigDecimal quantity) {
        boolean includeComponents = costCalculation.getBooleanField("includeComponents");
        if (pluginManager.isPluginEnabled("ordersForSubproductsGeneration") && includeComponents) {
            return productQuantitiesWithComponentsService.getNeededProductQuantities(technology, quantity,
                    MrpAlgorithm.ONLY_MATERIALS);
        }
        return productQuantitiesService.getNeededProductQuantities(technology, quantity, MrpAlgorithm.ONLY_COMPONENTS);
    }
}
