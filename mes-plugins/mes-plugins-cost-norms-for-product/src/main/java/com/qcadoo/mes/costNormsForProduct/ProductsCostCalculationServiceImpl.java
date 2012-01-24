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
package com.qcadoo.mes.costNormsForProduct;

import static com.google.common.base.Preconditions.checkArgument;
import static com.qcadoo.mes.costNormsForProduct.constants.ProductsCostCalculationConstants.AVERAGE;
import static com.qcadoo.mes.costNormsForProduct.constants.ProductsCostCalculationConstants.LASTPURCHASE;
import static com.qcadoo.mes.costNormsForProduct.constants.ProductsCostCalculationConstants.NOMINAL;
import static java.math.BigDecimal.ROUND_UP;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.costNormsForProduct.constants.ProductsCostCalculationConstants;
import com.qcadoo.mes.technologies.ProductQuantitiesService;
import com.qcadoo.mes.technologies.TechnologyService;
import com.qcadoo.model.api.Entity;

@Service
public class ProductsCostCalculationServiceImpl implements ProductsCostCalculationService {

    @Autowired
    TechnologyService technologyService;

    @Autowired
    private ProductQuantitiesService productQuantitiesService;

    public void calculateProductsCost(final Entity costCalculation) {
        checkArgument(costCalculation != null);
        BigDecimal quantity = getBigDecimal(costCalculation.getField("quantity"));
        BigDecimal result = BigDecimal.ZERO;
        ProductsCostCalculationConstants mode = getProductModeFromField(costCalculation.getField("calculateMaterialCostsMode"));

        checkArgument(quantity != null && quantity != BigDecimal.ZERO, "quantity is  null");
        checkArgument(mode != null, "mode is null!");

        Entity technology = costCalculation.getBelongsToField("technology");

        Map<Entity, BigDecimal> neededProductQuantities = productQuantitiesService.getNeededProductQuantities(technology,
                BigDecimal.ONE, true);

        for (Entry<Entity, BigDecimal> productQuantity : neededProductQuantities.entrySet()) {
            Entity product = productQuantity.getKey();

            BigDecimal cost = getBigDecimal(product.getField(mode.getStrValue()));
            BigDecimal costForNumber = getBigDecimal(product.getField("costForNumber"));
            BigDecimal costPerUnit = cost.divide(costForNumber, 4);

            result = result.add(costPerUnit.multiply(productQuantity.getValue()));
        }

        result = result.multiply(quantity);
        costCalculation.setField("totalMaterialCosts", result.setScale(3, ROUND_UP));
    }

    private ProductsCostCalculationConstants getProductModeFromField(final Object value) {
        String strValue = value.toString();
        if ("01nominal".equals(strValue)) {
            return NOMINAL;
        }
        if ("02average".equals(strValue)) {
            return AVERAGE;
        }
        if ("03lastPurchase".equals(strValue)) {
            return LASTPURCHASE;
        }
        return ProductsCostCalculationConstants.valueOf(strValue);
    }

    private BigDecimal getBigDecimal(Object value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(value.toString());
    }
}
