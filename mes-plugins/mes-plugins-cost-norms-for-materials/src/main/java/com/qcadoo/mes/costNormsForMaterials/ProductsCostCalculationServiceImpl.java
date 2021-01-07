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
import com.qcadoo.mes.technologies.constants.MrpAlgorithm;
import com.qcadoo.model.api.BigDecimalUtils;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
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
    private NumberService numberService;

    @Override
    public void calculateTotalProductsCost(final Entity entity, final Entity technology) {
        Map<Entity, BigDecimal> listProductWithCost = calculateListProductsCostForPlannedQuantity(entity, technology);
        BigDecimal result = BigDecimal.ZERO;
        for (Entry<Entity, BigDecimal> productWithCost : listProductWithCost.entrySet()) {
            result = result.add(productWithCost.getValue(), numberService.getMathContext());
        }
        entity.setField("totalMaterialCosts", numberService.setScaleWithDefaultMathContext(result));
    }

    private Map<Entity, BigDecimal> calculateListProductsCostForPlannedQuantity(final Entity entity, final Entity technology) {
        checkArgument(entity != null);
        BigDecimal quantity = BigDecimalUtils.convertNullToZero(entity.getDecimalField("quantity"));

        String materialCostsUsed = entity.getStringField("materialCostsUsed");

        checkArgument(materialCostsUsed != null, "materialCostsUsed is null!");

        return getProductWithCostForPlannedQuantities(entity, technology, quantity, materialCostsUsed);
    }

    @Override
    public BigDecimal calculateProductCostForGivenQuantity(final Entity product, final BigDecimal quantity,
            final String materialCostsUsed) {
        BigDecimal cost = BigDecimalUtils.convertNullToZero(product.getField(ProductsCostFields.forMode(
                materialCostsUsed).getStrValue()));
        BigDecimal costForNumber = BigDecimalUtils.convertNullToOne(product.getDecimalField("costForNumber"));
        if (BigDecimalUtils.valueEquals(costForNumber, BigDecimal.ZERO)) {
            costForNumber = BigDecimal.ONE;
        }
        BigDecimal costPerUnit = cost.divide(costForNumber, numberService.getMathContext());

        return costPerUnit.multiply(quantity, numberService.getMathContext());
    }

    private Map<Entity, BigDecimal> getProductWithCostForPlannedQuantities(final Entity entity, final Entity technology,
                                                                           final BigDecimal quantity, final String materialCostsUsed) {
        Map<Long, BigDecimal> neededProductQuantities = getNeededProductQuantities(entity, technology, quantity,
                MrpAlgorithm.ONLY_COMPONENTS);
        Map<Entity, BigDecimal> results = new HashMap<>();
        for (Entry<Long, BigDecimal> productQuantity : neededProductQuantities.entrySet()) {
            Entity product = productQuantitiesService.getProduct(productQuantity.getKey());
            BigDecimal thisProductsCost = calculateProductCostForGivenQuantity(product, productQuantity.getValue(),
                    materialCostsUsed);
            results.put(product, thisProductsCost);
        }
        return results;
    }

    private Map<Long, BigDecimal> getNeededProductQuantities(final Entity entity, final Entity technology,
            final BigDecimal quantity, final MrpAlgorithm algorithm) {
        return productQuantitiesService.getNeededProductQuantities(technology, quantity, algorithm);
    }
}
