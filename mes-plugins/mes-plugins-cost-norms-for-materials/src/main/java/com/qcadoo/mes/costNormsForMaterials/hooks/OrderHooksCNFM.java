package com.qcadoo.mes.costNormsForMaterials.hooks;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.costNormsForMaterials.CostNormsForMaterialsService;
import com.qcadoo.mes.costNormsForMaterials.constants.OrderOperationProductInComponentFields;
import com.qcadoo.mes.costNormsForProduct.constants.CostNormsForProductConstants;
import com.qcadoo.mes.costNormsForProduct.constants.ProductCostNormsFields;
import com.qcadoo.mes.orders.constants.OrdersConstants;
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
            List<Entity> orderOperationProductInComponents = Lists.newArrayList();

            Long technologyId = technology.getId();

            Map<Entity, BigDecimal> productQuantities = costNormsForMaterialsService
                    .getProductQuantitiesFromTechnology(technologyId);

            if (!productQuantities.isEmpty()) {
                for (Map.Entry<Entity, BigDecimal> productQuantity : productQuantities.entrySet()) {
                    Entity product = productQuantity.getKey();

                    Entity orderOperationProductInComponent = dataDefinitionService.get(
                            CostNormsForProductConstants.PLUGIN_IDENTIFIER,
                            CostNormsForProductConstants.MODEL_ORDER_OPERATION_PRODUCT_IN_COMPONENT).create();

                    orderOperationProductInComponent.setField(OrdersConstants.MODEL_ORDER, order);
                    orderOperationProductInComponent.setField(BasicConstants.MODEL_PRODUCT, product);
                    orderOperationProductInComponent.setField(OrderOperationProductInComponentFields.COST_FOR_NUMBER,
                            product.getField(ProductCostNormsFields.COST_FOR_NUMBER));
                    orderOperationProductInComponent.setField(OrderOperationProductInComponentFields.NOMINAL_COST,
                            product.getField(ProductCostNormsFields.NOMINAL_COST));
                    orderOperationProductInComponent.setField(OrderOperationProductInComponentFields.LAST_PURCHASE_COST,
                            product.getField(ProductCostNormsFields.LAST_PURCHASE_COST));
                    orderOperationProductInComponent.setField(OrderOperationProductInComponentFields.AVERAGE_COST,
                            product.getField(ProductCostNormsFields.AVERAGE_COST));

                    orderOperationProductInComponent = orderOperationProductInComponent.getDataDefinition().save(
                            orderOperationProductInComponent);

                    orderOperationProductInComponents.add(orderOperationProductInComponent);

                    order.setField(CostNormsForProductConstants.ORDER_OPERATION_PRODUCT_IN_COMPONENTS,
                            orderOperationProductInComponents);
                }
            }
        } else {
            if (technology == null && hasOrderOperationProductInComponents(order)) {
                order.setField(CostNormsForProductConstants.ORDER_OPERATION_PRODUCT_IN_COMPONENTS, Lists.newArrayList());
            }
        }

    }

    @SuppressWarnings("unchecked")
    private boolean hasOrderOperationProductInComponents(final Entity order) {
        return ((order.getField(CostNormsForProductConstants.ORDER_OPERATION_PRODUCT_IN_COMPONENTS) != null) && !((List<Entity>) order
                .getField(CostNormsForProductConstants.ORDER_OPERATION_PRODUCT_IN_COMPONENTS)).isEmpty());
    }

    private boolean shouldFill(final Entity order, final Entity technology) {
        return (technology != null) && (technology.getId() != null)
                && (hasTechnologyChanged(order, technology) || !hasOrderOperationProductInComponents(order));
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
