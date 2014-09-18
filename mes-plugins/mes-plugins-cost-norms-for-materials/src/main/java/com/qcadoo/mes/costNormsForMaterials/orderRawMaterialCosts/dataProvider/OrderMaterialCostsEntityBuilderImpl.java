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
