/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.3
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

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.costNormsForProduct.constants.CostNormsForProductConstants;
import com.qcadoo.mes.costNormsForProduct.constants.ProductsCostCalculationConstants;
import com.qcadoo.mes.costNormsForProduct.constants.SourceOfProductCosts;
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
    public void calculateTotalProductsCost(final Entity entity, final SourceOfProductCosts sourceOfProductCosts) {
        Map<Entity, BigDecimal> listProductWithCost = calculateListProductsCostForPlannedQuantity(entity, sourceOfProductCosts);
        BigDecimal result = BigDecimal.ZERO;
        for (Entry<Entity, BigDecimal> productWithCost : listProductWithCost.entrySet()) {
            result = result.add(productWithCost.getValue(), numberService.getMathContext());
        }
        entity.setField("totalMaterialCosts", numberService.setScale(result));
    }

    public Map<Entity, BigDecimal> calculateListProductsCostForPlannedQuantity(final Entity entity,
            final SourceOfProductCosts sourceOfProductCosts) {
        checkArgument(entity != null);
        BigDecimal quantity = getBigDecimal(entity.getField("quantity"));

        String mode = entity.getStringField("calculateMaterialCostsMode");

        checkArgument(quantity != null && quantity != BigDecimal.ZERO, "quantity is  null");
        checkArgument(mode != null, "mode is null!");

        Entity technology = entity.getBelongsToField("technology");

        Entity order = entity.getBelongsToField("order");

        if (sourceOfProductCosts.equals(SourceOfProductCosts.FROM_ORDER)) {
            return getProductWithCostForPlannedQuantities(technology, quantity, mode, order);
        } else if (sourceOfProductCosts.equals(SourceOfProductCosts.GLOBAL)) {
            return getProductWithCostForPlannedQuantities(technology, quantity, mode);
        }

        throw new IllegalStateException("sourceOfProductCosts is neither FROM_ORDER nor GLOBAL");
    }

    private Map<Entity, BigDecimal> getProductWithCostForPlannedQuantities(final Entity technology, final BigDecimal quantity,
            final String mode, final Entity order) {
        Map<Entity, BigDecimal> neededProductQuantities = productQuantitiesService.getNeededProductQuantities(technology,
                quantity, true);
        Map<Entity, BigDecimal> results = new HashMap<Entity, BigDecimal>();

        for (Entry<Entity, BigDecimal> productQuantity : neededProductQuantities.entrySet()) {
            Entity product = productQuantity.getKey();
            product = product.getDataDefinition().get(product.getId());

            Entity orderOperationProductInComponent = dataDefinitionService
                    .get(CostNormsForProductConstants.PLUGIN_IDENTIFIER,
                            CostNormsForProductConstants.MODEL_ORDER_OPERATION_PRODUCT_IN_COMPONENT).find()
                    .add(SearchRestrictions.belongsTo("order", order)).add(SearchRestrictions.belongsTo("product", product))
                    .uniqueResult();

            BigDecimal thisProductsCost = calculateProductCostForGivenQuantity(orderOperationProductInComponent,
                    productQuantity.getValue(), mode);
            results.put(product, thisProductsCost);
        }
        return results;
    }

    @Override
    public Map<Entity, BigDecimal> getProductWithCostForPlannedQuantities(final Entity technology, final BigDecimal quantity,
            final String mode) {
        Map<Entity, BigDecimal> neededProductQuantities = productQuantitiesService.getNeededProductQuantities(technology,
                quantity, true);
        Map<Entity, BigDecimal> results = new HashMap<Entity, BigDecimal>();
        for (Entry<Entity, BigDecimal> productQuantity : neededProductQuantities.entrySet()) {
            Entity product = productQuantity.getKey();
            BigDecimal thisProductsCost = calculateProductCostForGivenQuantity(product, productQuantity.getValue(), mode);
            results.put(product, thisProductsCost);
        }
        return results;
    }

    @Override
    public BigDecimal calculateProductCostForGivenQuantity(Entity product, BigDecimal quantity, final String mode) {
        BigDecimal cost = getBigDecimal(product.getField(ProductsCostCalculationConstants.parseString(mode).getStrValue()));
        BigDecimal costForNumber = getBigDecimal(product.getField("costForNumber"));
        BigDecimal costPerUnit = cost.divide(costForNumber, numberService.getMathContext());

        return costPerUnit.multiply(quantity, numberService.getMathContext());
    }

    private BigDecimal getBigDecimal(final Object value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(value.toString());
    }

}
