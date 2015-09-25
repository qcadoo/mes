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

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.costCalculation.constants.CostCalculationFields;
import com.qcadoo.mes.costCalculation.print.utils.CostCalculationMaterial;
import com.qcadoo.mes.costNormsForMaterials.ProductsCostCalculationService;
import com.qcadoo.mes.technologies.ProductQuantitiesService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;

@Service
public class CostCalculationMaterialsService {

    @Autowired
    private ProductQuantitiesService productQuantitiesService;

    @Autowired
    private ProductsCostCalculationService productsCostCalculationService;

    @Autowired
    private NumberService numberService;

    public List<CostCalculationMaterial> getSortedMaterialsFromProductQuantities(final Entity costCalculation,
            final Map<Long, BigDecimal> neededProductQuantities, final Entity order) {
        MathContext mathContext = numberService.getMathContext();
        List<CostCalculationMaterial> list = Lists.newArrayList();
        for (Map.Entry<Long, BigDecimal> neededProductQuantity : neededProductQuantities.entrySet()) {
            Entity product = productQuantitiesService.getProduct(neededProductQuantity.getKey());

            Entity productEntity = productsCostCalculationService.getAppropriateCostNormForProduct(product, order,
                    costCalculation.getStringField(CostCalculationFields.SOURCE_OF_MATERIAL_COSTS));

            BigDecimal productQuantity = neededProductQuantity.getValue();

            BigDecimal costForGivenQuantity = productsCostCalculationService.calculateProductCostForGivenQuantity(productEntity,
                    productQuantity, costCalculation.getStringField(CostCalculationFields.CALCULATE_MATERIAL_COSTS_MODE));

            BigDecimal materialCostMargin = costCalculation.getDecimalField(CostCalculationFields.MATERIAL_COST_MARGIN);

            if (materialCostMargin == null) {

                list.add(new CostCalculationMaterial(product.getStringField(ProductFields.NUMBER), product
                        .getStringField(ProductFields.UNIT), productQuantity, costForGivenQuantity));
            } else {
                BigDecimal toAdd = costForGivenQuantity.multiply(materialCostMargin.divide(new BigDecimal(100), mathContext),
                        mathContext);
                BigDecimal totalCosts = costForGivenQuantity.add(toAdd, mathContext);

                list.add(new CostCalculationMaterial(product.getStringField(ProductFields.NUMBER), product
                        .getStringField(ProductFields.UNIT), productQuantity, costForGivenQuantity, totalCosts, toAdd));
            }

        }
        list.sort(new Comparator<CostCalculationMaterial>() {

            @Override
            public int compare(CostCalculationMaterial o1, CostCalculationMaterial o2) {
                return o1.getCostForGivenQuantity().compareTo(o2.getCostForGivenQuantity());
            }
        });
        return list;
    }
}
