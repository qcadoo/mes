/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.2.0-SNAPSHOT
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

import static com.google.common.base.Preconditions.checkArgument;
import static com.qcadoo.mes.technologies.constants.MrpAlgorithm.COMPONENTS_AND_SUBCONTRACTORS_PRODUCTS;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.costNormsForMaterials.constants.CostNormsForMaterialsConstants;
import com.qcadoo.mes.costNormsForMaterials.constants.ProductsCostFields;
import com.qcadoo.mes.technologies.ProductQuantitiesService;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.model.api.search.SearchRestrictions;

@Service
public class ProductsCostCalculationServiceImpl implements ProductsCostCalculationService {

    @Autowired
    private ProductQuantitiesService productQuantitiesService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NumberService numberService;

    @Override
    public void calculateTotalProductsCost(final Entity entity, final String sourceOfMaterialCosts) {
        Map<Entity, BigDecimal> listProductWithCost = calculateListProductsCostForPlannedQuantity(entity, sourceOfMaterialCosts);
        BigDecimal result = BigDecimal.ZERO;
        for (Entry<Entity, BigDecimal> productWithCost : listProductWithCost.entrySet()) {
            result = result.add(productWithCost.getValue(), numberService.getMathContext());
        }
        entity.setField("totalMaterialCosts", numberService.setScale(result));
    }

    public Map<Entity, BigDecimal> calculateListProductsCostForPlannedQuantity(final Entity entity,
            final String sourceOfMaterialCosts) {
        checkArgument(entity != null);
        BigDecimal quantity = getBigDecimal(entity.getField("quantity"));

        String calculateMaterialCostsMode = entity.getStringField("calculateMaterialCostsMode");

        checkArgument(calculateMaterialCostsMode != null, "calculateMaterialCostsMode is null!");

        Entity technology = entity.getBelongsToField("technology");

        Entity order = entity.getBelongsToField("order");

        if ("02fromOrdersMaterialCosts".equals(sourceOfMaterialCosts)) {
            return getProductWithCostForPlannedQuantities(technology, quantity, calculateMaterialCostsMode, order);
        } else if ("01currentGlobalDefinitionsInProduct".equals(sourceOfMaterialCosts)) {
            return getProductWithCostForPlannedQuantities(technology, quantity, calculateMaterialCostsMode);
        }

        throw new IllegalStateException("sourceOfProductCosts is neither FROM_ORDER nor GLOBAL");
    }

    public BigDecimal calculateProductCostForGivenQuantity(final Entity product, final BigDecimal quantity,
            final String calculateMaterialCostsMode) {
        BigDecimal cost = getBigDecimal(product
                .getField(ProductsCostFields.parseString(calculateMaterialCostsMode).getStrValue()));
        BigDecimal costForNumber = getBigDecimal(product.getField("costForNumber"));
        BigDecimal costPerUnit = cost.divide(costForNumber, numberService.getMathContext());

        return costPerUnit.multiply(quantity, numberService.getMathContext());
    }

    public Map<Entity, BigDecimal> getProductWithCostForPlannedQuantities(final Entity technology, final BigDecimal quantity,
            final String calculateMaterialCostsMode) {
        Map<Entity, BigDecimal> neededProductQuantities = productQuantitiesService.getNeededProductQuantities(technology,
                quantity, COMPONENTS_AND_SUBCONTRACTORS_PRODUCTS);
        Map<Entity, BigDecimal> results = new HashMap<Entity, BigDecimal>();
        for (Entry<Entity, BigDecimal> productQuantity : neededProductQuantities.entrySet()) {
            Entity product = productQuantity.getKey();
            BigDecimal thisProductsCost = calculateProductCostForGivenQuantity(product, productQuantity.getValue(),
                    calculateMaterialCostsMode);
            results.put(product, thisProductsCost);
        }
        return results;
    }

    public Map<Entity, BigDecimal> getProductWithCostForPlannedQuantities(final Entity technology, final BigDecimal quantity,
            final String calculateMaterialCostsMode, final Entity order) {
        Map<Entity, BigDecimal> neededProductQuantities = productQuantitiesService.getNeededProductQuantities(technology,
                quantity, COMPONENTS_AND_SUBCONTRACTORS_PRODUCTS);
        Map<Entity, BigDecimal> results = new HashMap<Entity, BigDecimal>();

        for (Entry<Entity, BigDecimal> productQuantity : neededProductQuantities.entrySet()) {
            Entity product = productQuantity.getKey();
            product = product.getDataDefinition().get(product.getId());

            Entity technologyInstOperProductInComp = dataDefinitionService
                    .get(CostNormsForMaterialsConstants.PLUGIN_IDENTIFIER,
                            CostNormsForMaterialsConstants.MODEL_TECHNOLOGY_INST_OPER_PRODUCT_IN_COMP).find()
                    .add(SearchRestrictions.belongsTo("order", order)).add(SearchRestrictions.belongsTo("product", product))
                    .uniqueResult();

            BigDecimal thisProductsCost = calculateProductCostForGivenQuantity(technologyInstOperProductInComp,
                    productQuantity.getValue(), calculateMaterialCostsMode);
            results.put(product, thisProductsCost);
        }
        return results;
    }

    public Entity getAppropriateCostNormForProduct(final Entity product, final Entity order, final String sourceOfMaterialCosts) {
        if ("01currentGlobalDefinitionsInProduct".equals(sourceOfMaterialCosts)) {
            return product;
        } else {
            Entity productWithCostNormsFromOrder = dataDefinitionService
                    .get(CostNormsForMaterialsConstants.PLUGIN_IDENTIFIER,
                            CostNormsForMaterialsConstants.MODEL_TECHNOLOGY_INST_OPER_PRODUCT_IN_COMP).find()
                    .add(SearchRestrictions.belongsTo("order", order)).add(SearchRestrictions.belongsTo("product", product))
                    .uniqueResult();
            if (productWithCostNormsFromOrder == null) {
                throw new IllegalStateException("Product with number " + product.getStringField(ProductFields.NUMBER)
                        + " doesn't exists for order with id" + order.getId());
            }
            return productWithCostNormsFromOrder;
        }
    }

    private BigDecimal getBigDecimal(final Object value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(value.toString());
    }

}
