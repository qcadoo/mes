package com.qcadoo.mes.costNormsForMaterials.hooks;

import static com.qcadoo.mes.costNormsForMaterials.constants.CostNormsForMaterialsConstants.TECHNOLOGY_INST_OPER_PRODUCT_IN_COMPS;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.qcadoo.mes.costNormsForMaterials.CostNormsForMaterialsService;
import com.qcadoo.mes.costNormsForMaterials.constants.CostNormsForMaterialsConstants;
import com.qcadoo.mes.costNormsForMaterials.constants.TechnologyInstOperProductInCompFields;
import com.qcadoo.mes.costNormsForProduct.constants.ProductCostNormsFields;
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

    public void fillOrderOperationProductsInComponents(final DataDefinition orderDD, final Entity order) {
        Entity technology = order.getBelongsToField(TechnologiesConstants.MODEL_TECHNOLOGY);

        if (shouldFill(order, technology)) {
            List<Entity> technologyInstOperProductInComps = Lists.newArrayList();

            Long technologyId = technology.getId();

            Map<Entity, BigDecimal> productQuantities = costNormsForMaterialsService
                    .getProductQuantitiesFromTechnology(technologyId);

            if (!productQuantities.isEmpty()) {
                for (Map.Entry<Entity, BigDecimal> productQuantity : productQuantities.entrySet()) {
                    Entity product = productQuantity.getKey();

                    Entity technologyInstOperProductInComp = dataDefinitionService.get(
                            CostNormsForMaterialsConstants.PLUGIN_IDENTIFIER,
                            CostNormsForMaterialsConstants.MODEL_TECHNOLOGY_INST_OPER_PRODUCT_IN_COMP).create();

                    technologyInstOperProductInComp.setField(TechnologyInstOperProductInCompFields.ORDER, order);
                    technologyInstOperProductInComp.setField(TechnologyInstOperProductInCompFields.PRODUCT, product);
                    technologyInstOperProductInComp.setField(TechnologyInstOperProductInCompFields.COST_FOR_NUMBER,
                            product.getDecimalField(ProductCostNormsFields.COST_FOR_NUMBER));
                    technologyInstOperProductInComp.setField(TechnologyInstOperProductInCompFields.NOMINAL_COST,
                            product.getDecimalField(ProductCostNormsFields.NOMINAL_COST));
                    technologyInstOperProductInComp.setField(TechnologyInstOperProductInCompFields.LAST_PURCHASE_COST,
                            product.getDecimalField(ProductCostNormsFields.LAST_PURCHASE_COST));
                    technologyInstOperProductInComp.setField(TechnologyInstOperProductInCompFields.AVERAGE_COST,
                            product.getDecimalField(ProductCostNormsFields.AVERAGE_COST));

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
