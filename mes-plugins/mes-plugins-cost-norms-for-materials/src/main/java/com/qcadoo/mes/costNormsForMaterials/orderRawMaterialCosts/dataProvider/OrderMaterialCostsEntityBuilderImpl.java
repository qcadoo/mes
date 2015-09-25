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
package com.qcadoo.mes.costNormsForMaterials.orderRawMaterialCosts.dataProvider;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.costNormsForMaterials.constants.CostNormsForMaterialsConstants;
import com.qcadoo.mes.costNormsForMaterials.constants.TechnologyInstOperProductInCompFields;
import com.qcadoo.mes.costNormsForMaterials.orderRawMaterialCosts.domain.ProductWithCosts;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;

@Service
final class OrderMaterialCostsEntityBuilderImpl implements OrderMaterialCostsEntityBuilder {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Override
    public Entity create(final Entity order, final ProductWithCosts productWithCosts) {
        Entity orderMaterialCosts = dataDefinitionService.get(CostNormsForMaterialsConstants.PLUGIN_IDENTIFIER,
                CostNormsForMaterialsConstants.MODEL_TECHNOLOGY_INST_OPER_PRODUCT_IN_COMP).create();
        orderMaterialCosts.setField(TechnologyInstOperProductInCompFields.ORDER, order);
        orderMaterialCosts.setField(TechnologyInstOperProductInCompFields.PRODUCT, productWithCosts.getProductId());
        orderMaterialCosts.setField(TechnologyInstOperProductInCompFields.COST_FOR_NUMBER, productWithCosts.getCostForNumber());
        orderMaterialCosts.setField(TechnologyInstOperProductInCompFields.NOMINAL_COST, productWithCosts.getNominalCost());
        orderMaterialCosts.setField(TechnologyInstOperProductInCompFields.LAST_PURCHASE_COST,
                productWithCosts.getLastPurchaseCost());
        orderMaterialCosts.setField(TechnologyInstOperProductInCompFields.AVERAGE_COST, productWithCosts.getAverageCost());
        return orderMaterialCosts;
    }

}
