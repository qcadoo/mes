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

import com.google.common.collect.Lists;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.costCalculation.constants.CostCalculationFields;
import com.qcadoo.mes.costCalculation.print.utils.CostCalculationMaterial;
import com.qcadoo.mes.costNormsForMaterials.ProductsCostCalculationService;
import com.qcadoo.mes.technologies.ProductQuantitiesService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Service
public class CostCalculationMaterialsService {

    @Autowired
    private ProductQuantitiesService productQuantitiesService;

    @Autowired
    private ProductsCostCalculationService productsCostCalculationService;

    @Autowired
    private NumberService numberService;

    public List<CostCalculationMaterial> getSortedMaterialsFromProductQuantities(final Entity costCalculation,
            final Entity technology) {
        MathContext mathContext = numberService.getMathContext();
        List<CostCalculationMaterial> list = Lists.newArrayList();
        BigDecimal quantity = costCalculation.getDecimalField(CostCalculationFields.QUANTITY);
        Map<Long, BigDecimal> neededProductQuantities = productsCostCalculationService.getNeededProductQuantities(costCalculation,
                technology, quantity);
        for (Map.Entry<Long, BigDecimal> neededProductQuantity : neededProductQuantities.entrySet()) {
            Entity product = productQuantitiesService.getProduct(neededProductQuantity.getKey());

            BigDecimal productQuantity = neededProductQuantity.getValue();

            BigDecimal costPerUnit = productsCostCalculationService.calculateProductCostPerUnit(product,
                    costCalculation.getStringField(CostCalculationFields.MATERIAL_COSTS_USED),
                    costCalculation.getBooleanField(CostCalculationFields.USE_NOMINAL_COST_PRICE_NOT_SPECIFIED));

            BigDecimal costForGivenQuantity = costPerUnit.multiply(productQuantity, numberService.getMathContext());

            BigDecimal materialCostMargin = costCalculation.getDecimalField(CostCalculationFields.MATERIAL_COST_MARGIN);

            if (materialCostMargin == null) {
                list.add(new CostCalculationMaterial(product.getStringField(ProductFields.NUMBER),
                        product.getStringField(ProductFields.NAME), product.getStringField(ProductFields.UNIT), productQuantity,
                        costPerUnit, costForGivenQuantity));
            } else {
                BigDecimal toAdd = costForGivenQuantity.multiply(materialCostMargin.divide(new BigDecimal(100), mathContext),
                        mathContext);
                list.add(new CostCalculationMaterial(product.getStringField(ProductFields.NUMBER),
                        product.getStringField(ProductFields.NAME), product.getStringField(ProductFields.UNIT), productQuantity,
                        costPerUnit, costForGivenQuantity, costForGivenQuantity.add(toAdd, mathContext), toAdd));
            }

        }
        list.sort(Comparator.comparing(CostCalculationMaterial::getCostForGivenQuantity));
        return list;
    }
}
