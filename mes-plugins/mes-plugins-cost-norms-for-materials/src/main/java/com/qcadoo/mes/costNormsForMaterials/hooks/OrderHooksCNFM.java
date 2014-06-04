/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.3
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
package com.qcadoo.mes.costNormsForMaterials.hooks;

import static com.qcadoo.mes.costNormsForMaterials.constants.CostNormsForMaterialsConstants.TECHNOLOGY_INST_OPER_PRODUCT_IN_COMPS;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.qcadoo.mes.costNormsForMaterials.CostNormsForMaterialsService;
import com.qcadoo.mes.costNormsForMaterials.constants.CostNormsForMaterialsConstants;
import com.qcadoo.mes.costNormsForMaterials.constants.TechnologyInstOperProductInCompFields;
import com.qcadoo.mes.costNormsForProduct.constants.ProductFieldsCNFP;
import com.qcadoo.mes.technologies.ProductQuantitiesService;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;

@Service
public class OrderHooksCNFM {

    @Autowired
    private CostNormsForMaterialsService costNormsForMaterialsService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private ProductQuantitiesService productQuantitiesService;

    public void fillOrderOperationProductsInComponents(final DataDefinition orderDD, final Entity order) {
        Entity technology = order.getBelongsToField(TechnologiesConstants.MODEL_TECHNOLOGY);

        if (shouldFill(order, technology)) {
            List<Entity> technologyInstOperProductInComps = Lists.newArrayList();

            Long technologyId = technology.getId();

            Map<Long, BigDecimal> productQuantities = costNormsForMaterialsService
                    .getProductQuantitiesFromTechnology(technologyId);

            if (!productQuantities.isEmpty()) {
                for (Entry<Long, BigDecimal> productQuantity : productQuantities.entrySet()) {
                    Entity product = productQuantitiesService.getProduct(productQuantity.getKey());

                    Entity technologyInstOperProductInComp = dataDefinitionService.get(
                            CostNormsForMaterialsConstants.PLUGIN_IDENTIFIER,
                            CostNormsForMaterialsConstants.MODEL_TECHNOLOGY_INST_OPER_PRODUCT_IN_COMP).create();

                    technologyInstOperProductInComp.setField(TechnologyInstOperProductInCompFields.ORDER, order);
                    technologyInstOperProductInComp.setField(TechnologyInstOperProductInCompFields.PRODUCT, product);
                    technologyInstOperProductInComp.setField(TechnologyInstOperProductInCompFields.COST_FOR_NUMBER,
                            product.getDecimalField(ProductFieldsCNFP.COST_FOR_NUMBER));
                    technologyInstOperProductInComp.setField(TechnologyInstOperProductInCompFields.NOMINAL_COST,
                            product.getDecimalField(ProductFieldsCNFP.NOMINAL_COST));
                    technologyInstOperProductInComp.setField(TechnologyInstOperProductInCompFields.LAST_PURCHASE_COST,
                            product.getDecimalField(ProductFieldsCNFP.LAST_PURCHASE_COST));
                    technologyInstOperProductInComp.setField(TechnologyInstOperProductInCompFields.AVERAGE_COST,
                            product.getDecimalField(ProductFieldsCNFP.AVERAGE_COST));

                    technologyInstOperProductInComp = technologyInstOperProductInComp.getDataDefinition().save(
                            technologyInstOperProductInComp);

                    technologyInstOperProductInComps.add(technologyInstOperProductInComp);

                    order.setField(TECHNOLOGY_INST_OPER_PRODUCT_IN_COMPS, technologyInstOperProductInComps);
                }
            }
        } else {
            if (technology == null && hasTechnologyInstOperProductInComps(order)) {
                order.setField(TECHNOLOGY_INST_OPER_PRODUCT_IN_COMPS, Lists.newArrayList());
            }
        }

    }

    @SuppressWarnings("unchecked")
    private boolean hasTechnologyInstOperProductInComps(final Entity order) {
        return ((order.getField(TECHNOLOGY_INST_OPER_PRODUCT_IN_COMPS) != null) && !((List<Entity>) order
                .getField(TECHNOLOGY_INST_OPER_PRODUCT_IN_COMPS)).isEmpty());
    }

    private boolean shouldFill(final Entity order, final Entity technology) {
        return (technology != null) && (technology.getId() != null)
                && (hasTechnologyChanged(order, technology) || !hasTechnologyInstOperProductInComps(order));
    }

    private boolean hasTechnologyChanged(final Entity order, final Entity technology) {
        Entity existingOrder = getExistingOrder(order);
        if (existingOrder == null) {
            return false;
        }
        Entity existingOrderTechnology = existingOrder.getBelongsToField(TechnologiesConstants.MODEL_TECHNOLOGY);
        if (existingOrderTechnology == null) {
            return true;
        }
        return !existingOrderTechnology.equals(technology);
    }

    private Entity getExistingOrder(final Entity order) {
        if (order.getId() == null) {
            return null;
        }
        return order.getDataDefinition().get(order.getId());
    }
}
