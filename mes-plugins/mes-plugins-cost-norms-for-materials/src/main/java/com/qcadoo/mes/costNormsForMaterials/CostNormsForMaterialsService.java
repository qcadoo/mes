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
package com.qcadoo.mes.costNormsForMaterials;

import static com.google.common.base.Preconditions.checkArgument;
import static com.qcadoo.model.api.search.SearchRestrictions.in;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.qcadoo.mes.costNormsForMaterials.constants.OrderFieldsCNFM;
import com.qcadoo.mes.costNormsForMaterials.constants.TechnologyInstOperProductInCompFields;
import com.qcadoo.mes.costNormsForMaterials.orderRawMaterialCosts.OrderMaterialsCostDataGenerator;
import com.qcadoo.mes.costNormsForMaterials.orderRawMaterialCosts.dataProvider.OrderMaterialCostsCriteria;
import com.qcadoo.mes.costNormsForMaterials.orderRawMaterialCosts.dataProvider.OrderMaterialCostsDataProvider;
import com.qcadoo.mes.costNormsForMaterials.orderRawMaterialCosts.domain.ProductWithQuantityAndCost;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.technologies.ProductQuantitiesService;
import com.qcadoo.mes.technologies.TechnologyService;
import com.qcadoo.mes.technologies.constants.MrpAlgorithm;
import com.qcadoo.mes.technologies.constants.OperationProductInComponentFields;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.constants.QcadooViewConstants;

@Service
public class CostNormsForMaterialsService {

    private static final Logger LOG = LoggerFactory.getLogger(CostNormsForMaterialsService.class);

    private static final String L_ORDER = "order";

    private static final String L_VIEW_DEFINITION_STATE_IS_NULL = "viewDefinitionState is null";

    @Autowired
    private NumberService numberService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private OrderMaterialCostsDataProvider orderMaterialCostsDataProvider;

    @Autowired
    private TechnologyService technologyService;

    @Autowired
    private ProductQuantitiesService productQuantitiesService;

    @Autowired
    private OrderMaterialsCostDataGenerator orderMaterialsCostDataGenerator;

    public void fillInProductsGridInTechnology(final ViewDefinitionState viewDefinitionState) {
        checkArgument(viewDefinitionState != null, L_VIEW_DEFINITION_STATE_IS_NULL);

        GridComponent grid = (GridComponent) viewDefinitionState.getComponentByReference(QcadooViewConstants.L_GRID);
        FormComponent technology = (FormComponent) viewDefinitionState.getComponentByReference(QcadooViewConstants.L_FORM);

        Long technologyId = technology.getEntityId();

        if (technologyId == null) {
            return;
        }

        List<Entity> inputProducts = Lists.newArrayList();

        Map<Long, BigDecimal> productQuantities = getProductQuantitiesFromTechnology(technologyId);

        for (Map.Entry<Long, BigDecimal> productQuantity : productQuantities.entrySet()) {
            Entity product = productQuantitiesService.getProduct(productQuantity.getKey());
            BigDecimal quantity = productQuantity.getValue();

            Entity operationProductInComponent = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                    TechnologiesConstants.MODEL_OPERATION_PRODUCT_IN_COMPONENT).create();

            operationProductInComponent.setField(OperationProductInComponentFields.PRODUCT, product);
            operationProductInComponent.setField(OperationProductInComponentFields.QUANTITY, quantity);

            inputProducts.add(operationProductInComponent);
        }

        grid.setEntities(inputProducts);
    }

    public Map<Long, BigDecimal> getProductQuantitiesFromTechnology(final Long technologyId) {
        Entity technology = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_TECHNOLOGY).get(technologyId);

        Entity operationComponentRoot = technology.getTreeField(TechnologyFields.OPERATION_COMPONENTS).getRoot();

        if (operationComponentRoot != null) {
            try {
                BigDecimal givenQty = technologyService.getProductCountForOperationComponent(operationComponentRoot);

                return productQuantitiesService.getNeededProductQuantities(technology,
                        givenQty, MrpAlgorithm.COMPONENTS_AND_SUBCONTRACTORS_PRODUCTS);
            } catch (IllegalStateException e) {
                if (LOG.isWarnEnabled()) {
                    LOG.warn("Invalid technology tree!", e);
                }
            }
        }

        return Maps.newHashMap();
    }

    public List<Entity> updateCostsForProductInOrder(Entity order, Collection<ProductWithQuantityAndCost> productsInfo) {
        List<Entity> result = Lists.newArrayList();

        if (!productsInfo.isEmpty()) {

            Map<Long, ProductWithQuantityAndCost> productsInfoGroupedByProductId = productsInfo.stream()
                    .collect(Collectors.toMap(ProductWithQuantityAndCost::getProductId, Function.identity()));

            List<Entity> orderMaterialCostsList = orderMaterialCostsDataProvider.findAll(OrderMaterialCostsCriteria
                    .forOrder(order.getId()).setProductCriteria(in("id", productsInfoGroupedByProductId.keySet())));

            Map<Long, List<Entity>> orderMaterialCostsGroupedByProductId = orderMaterialCostsList.stream().collect(
                    Collectors.groupingBy(e -> e.getBelongsToField(TechnologyInstOperProductInCompFields.PRODUCT).getId()));

            for (Map.Entry<Long, ProductWithQuantityAndCost> entry : productsInfoGroupedByProductId.entrySet()) {

                Long productId = entry.getKey();

                if (orderMaterialCostsGroupedByProductId.containsKey(productId)) {

                    List<Entity> orderMaterialCostsForProduct = orderMaterialCostsGroupedByProductId.get(productId);

                    Entity orderMaterialCosts = orderMaterialCostsForProduct.get(0);

                    Optional<BigDecimal> costForOrder = entry.getValue().getCostOpt();
                    orderMaterialCosts.setField(TechnologyInstOperProductInCompFields.COST_FOR_ORDER,
                            numberService.setScaleWithDefaultMathContext(costForOrder.orElse(BigDecimal.ZERO)));

                    result.add(orderMaterialCosts.getDataDefinition().save(orderMaterialCosts));
                } else {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug(String.format(
                                "TechnologyInstanceOperationProductInComponent (order material costs entity) not found for product: %d order: %d",
                                productId, order.getId()));
                    }
                }
            }
        }
        return result;
    }

    public void updateCosts(final ViewDefinitionState viewDefinitionState, final ComponentState component, final String[] args) {
        FormComponent form = (FormComponent) viewDefinitionState.getComponentByReference(L_ORDER);
        if ((form == null) || (form.getEntityId() == null)) {
            return;
        }
        Long orderId = form.getEntityId();
        Entity order = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).get(orderId);
        form.setEntity(updateCostsInOrder(order));
    }

    private Entity updateCostsInOrder(Entity order) {
        List<Entity> orderMaterialsCosts = orderMaterialsCostDataGenerator.generateUpdatedMaterialsListFor(order);
        order.setField(OrderFieldsCNFM.TECHNOLOGY_INST_OPER_PRODUCT_IN_COMPS, orderMaterialsCosts);
        return order.getDataDefinition().save(order);
    }

    public void saveCosts(final ViewDefinitionState viewDefinitionState, final ComponentState component, final String[] args) {
        FormComponent orderForm = (FormComponent) viewDefinitionState.getComponentByReference("order");
        Entity order = orderForm.getEntity();
        Entity orderEntity = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).get(
                order.getId());
        orderEntity.setField(OrderFieldsCNFM.DIRECT_ADDITIONAL_COST,
                order.getDecimalField(OrderFieldsCNFM.DIRECT_ADDITIONAL_COST));
        orderEntity.setField(OrderFieldsCNFM.DIRECT_ADDITIONAL_COST_DESCRIPTION,
                order.getStringField(OrderFieldsCNFM.DIRECT_ADDITIONAL_COST_DESCRIPTION));

        orderEntity.getDataDefinition().save(orderEntity);
    }

}
