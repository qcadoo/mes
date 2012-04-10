package com.qcadoo.mes.costNormsForMaterials;

import static com.google.common.base.Preconditions.checkArgument;
import static com.qcadoo.mes.costNormsForMaterials.constants.OrderOperationProductInComponentFields.AVERAGE_COST;
import static com.qcadoo.mes.costNormsForMaterials.constants.OrderOperationProductInComponentFields.COST_FOR_NUMBER;
import static com.qcadoo.mes.costNormsForMaterials.constants.OrderOperationProductInComponentFields.LAST_PURCHASE_COST;
import static com.qcadoo.mes.costNormsForMaterials.constants.OrderOperationProductInComponentFields.NOMINAL_COST;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.costNormsForMaterials.constants.CostNormsForMaterialsConstants;
import com.qcadoo.mes.costNormsForProduct.CostNormsForProductService;
import com.qcadoo.mes.costNormsForProduct.constants.ProductCostNormsFields;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.technologies.ProductQuantitiesService;
import com.qcadoo.mes.technologies.TechnologyService;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;

@Service
public class CostNormsForMaterialsService {

    private static final Logger LOG = LoggerFactory.getLogger(CostNormsForProductService.class);

    private static final String L_VIEW_DEFINITION_STATE_IS_NULL = "viewDefinitionState is null";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private TechnologyService technologyService;

    @Autowired
    private ProductQuantitiesService productQuantitiesService;

    public void fillInProductsGridInTechnology(final ViewDefinitionState viewDefinitionState) {
        checkArgument(viewDefinitionState != null, L_VIEW_DEFINITION_STATE_IS_NULL);

        GridComponent grid = (GridComponent) viewDefinitionState.getComponentByReference("grid");

        FormComponent technology = (FormComponent) viewDefinitionState.getComponentByReference("technology");

        if ((technology == null) || (technology.getEntityId() == null)) {
            return;
        }

        Long technologyId = technology.getEntityId();

        List<Entity> inputProducts = Lists.newArrayList();

        Map<Entity, BigDecimal> productQuantities = getProductQuantitiesFromTechnology(technologyId);

        if (!productQuantities.isEmpty()) {
            for (Map.Entry<Entity, BigDecimal> productQuantity : productQuantities.entrySet()) {
                Entity product = productQuantity.getKey();
                BigDecimal quantity = productQuantity.getValue();

                Entity operationProductInComponent = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                        TechnologiesConstants.MODEL_OPERATION_PRODUCT_IN_COMPONENT).create();

                operationProductInComponent.setField(BasicConstants.MODEL_PRODUCT, product);
                operationProductInComponent.setField("quantity", quantity);

                inputProducts.add(operationProductInComponent);
            }
        }

        grid.setEntities(inputProducts);
    }

    public Map<Entity, BigDecimal> getProductQuantitiesFromTechnology(final Long technologyId) {
        Entity technology = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_TECHNOLOGY).get(technologyId);

        Entity operationComponentRoot = technology.getTreeField(TechnologiesConstants.OPERATION_COMPONENTS).getRoot();

        if (operationComponentRoot != null) {
            try {
                BigDecimal giventQty = technologyService.getProductCountForOperationComponent(operationComponentRoot);

                Map<Entity, BigDecimal> productQuantities = productQuantitiesService.getNeededProductQuantities(technology,
                        giventQty, true);

                return productQuantities;
            } catch (IllegalStateException e) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Invalid technology tree!");
                }
            }
        }

        return Maps.newHashMap();
    }

    public void copyCostsFromProducts(final ViewDefinitionState viewDefinitionState, final ComponentState component,
            final String[] args) {
        checkArgument(viewDefinitionState != null, L_VIEW_DEFINITION_STATE_IS_NULL);

        GridComponent grid = (GridComponent) viewDefinitionState.getComponentByReference("grid");

        FormComponent order = (FormComponent) viewDefinitionState.getComponentByReference("order");

        if ((order == null) || (order.getEntityId() == null)) {
            return;
        }

        Long orderId = order.getEntityId();

        List<Entity> inputProducts = Lists.newArrayList();

        Entity existingOrder = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).get(
                orderId);

        List<Entity> orderOperationProductIncomponents = existingOrder
                .getHasManyField(CostNormsForMaterialsConstants.ORDER_OPERATION_PRODUCT_IN_COMPONENTS);

        if (orderOperationProductIncomponents != null) {
            for (Entity orderOperationProductIncomponent : orderOperationProductIncomponents) {
                Entity product = orderOperationProductIncomponent.getBelongsToField(BasicConstants.MODEL_PRODUCT);

                orderOperationProductIncomponent.setField(COST_FOR_NUMBER,
                        product.getField(ProductCostNormsFields.COST_FOR_NUMBER));
                orderOperationProductIncomponent.setField(NOMINAL_COST, product.getField(ProductCostNormsFields.NOMINAL_COST));
                orderOperationProductIncomponent.setField(LAST_PURCHASE_COST,
                        product.getField(ProductCostNormsFields.LAST_PURCHASE_COST));
                orderOperationProductIncomponent.setField(AVERAGE_COST, product.getField(ProductCostNormsFields.AVERAGE_COST));

                orderOperationProductIncomponent = orderOperationProductIncomponent.getDataDefinition().save(
                        orderOperationProductIncomponent);

                inputProducts.add(orderOperationProductIncomponent);
            }
        }

        grid.setEntities(inputProducts);
    }

}
