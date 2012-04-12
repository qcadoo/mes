package com.qcadoo.mes.costNormsForMaterials.hooks;

import static com.qcadoo.mes.costNormsForMaterials.constants.CostNormsForMaterialsConstants.TECHNOLOGY_INSTANCE_OPERATION_PRODUCT_IN_COMPONENTS;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.qcadoo.mes.costNormsForMaterials.CostNormsForMaterialsService;
import com.qcadoo.mes.costNormsForMaterials.constants.CostNormsForMaterialsConstants;
import com.qcadoo.mes.costNormsForMaterials.constants.TechnologyInstanceOperationProductInComponentFields;
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
            List<Entity> technologyInstanceOperationProductInComponents = Lists.newArrayList();

            Long technologyId = technology.getId();

            Map<Entity, BigDecimal> productQuantities = costNormsForMaterialsService
                    .getProductQuantitiesFromTechnology(technologyId);

            if (!productQuantities.isEmpty()) {
                for (Map.Entry<Entity, BigDecimal> productQuantity : productQuantities.entrySet()) {
                    Entity product = productQuantity.getKey();

                    Entity technologyInstanceOperationProductInComponent = dataDefinitionService.get(
                            CostNormsForMaterialsConstants.PLUGIN_IDENTIFIER,
                            CostNormsForMaterialsConstants.MODEL_TECHNOLOGY_INSTANCE_OPERATION_PRODUCT_IN_COMPONENT).create();

                    technologyInstanceOperationProductInComponent.setField(
                            TechnologyInstanceOperationProductInComponentFields.ORDER, order);
                    technologyInstanceOperationProductInComponent.setField(
                            TechnologyInstanceOperationProductInComponentFields.PRODUCT, product);
                    technologyInstanceOperationProductInComponent.setField(
                            TechnologyInstanceOperationProductInComponentFields.COST_FOR_NUMBER,
                            product.getDecimalField(ProductCostNormsFields.COST_FOR_NUMBER));
                    technologyInstanceOperationProductInComponent.setField(
                            TechnologyInstanceOperationProductInComponentFields.NOMINAL_COST,
                            product.getDecimalField(ProductCostNormsFields.NOMINAL_COST));
                    technologyInstanceOperationProductInComponent.setField(
                            TechnologyInstanceOperationProductInComponentFields.LAST_PURCHASE_COST,
                            product.getDecimalField(ProductCostNormsFields.LAST_PURCHASE_COST));
                    technologyInstanceOperationProductInComponent.setField(
                            TechnologyInstanceOperationProductInComponentFields.AVERAGE_COST,
                            product.getDecimalField(ProductCostNormsFields.AVERAGE_COST));

                    technologyInstanceOperationProductInComponent = technologyInstanceOperationProductInComponent
                            .getDataDefinition().save(technologyInstanceOperationProductInComponent);

                    technologyInstanceOperationProductInComponents.add(technologyInstanceOperationProductInComponent);

                    order.setField(TECHNOLOGY_INSTANCE_OPERATION_PRODUCT_IN_COMPONENTS,
                            technologyInstanceOperationProductInComponents);
                }
            }
        } else {
            if (technology == null && hasTechnologyInstanceOperationProductInComponents(order)) {
                order.setField(TECHNOLOGY_INSTANCE_OPERATION_PRODUCT_IN_COMPONENTS, Lists.newArrayList());
            }
        }

    }

    @SuppressWarnings("unchecked")
    private boolean hasTechnologyInstanceOperationProductInComponents(final Entity order) {
        return ((order.getField(TECHNOLOGY_INSTANCE_OPERATION_PRODUCT_IN_COMPONENTS) != null) && !((List<Entity>) order
                .getField(TECHNOLOGY_INSTANCE_OPERATION_PRODUCT_IN_COMPONENTS)).isEmpty());
    }

    private boolean shouldFill(final Entity order, final Entity technology) {
        return (technology != null) && (technology.getId() != null)
                && (hasTechnologyChanged(order, technology) || !hasTechnologyInstanceOperationProductInComponents(order));
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
