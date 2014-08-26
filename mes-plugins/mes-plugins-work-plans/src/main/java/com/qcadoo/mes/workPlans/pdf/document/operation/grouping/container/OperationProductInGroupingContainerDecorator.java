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
package com.qcadoo.mes.workPlans.pdf.document.operation.grouping.container;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.columnExtension.constants.ColumnAlignment;
import com.qcadoo.mes.technologies.ProductQuantitiesService;
import com.qcadoo.mes.technologies.constants.OperationFields;
import com.qcadoo.mes.technologies.constants.OperationProductInComponentFields;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.mes.technologies.dto.OperationProductComponentWithQuantityContainer;
import com.qcadoo.mes.technologies.grouping.OperationMergeService;
import com.qcadoo.mes.workPlans.pdf.document.operation.grouping.holder.OrderOperationComponent;
import com.qcadoo.mes.workPlans.pdf.document.operation.product.column.OperationProductColumn;
import com.qcadoo.mes.workPlans.pdf.document.order.column.OrderColumn;
import com.qcadoo.model.api.Entity;

public class OperationProductInGroupingContainerDecorator implements GroupingContainer {

    private OperationMergeService operationMergeService;

    private ProductQuantitiesService productQuantitiesService;

    private GroupingContainer groupingContainer;

    private Map<Long, Entity> operationComponentIdToOrder;

    private Map<Long, Entity> operationComponentIdToOperationComponent;

    private Map<String, Long> operationNumberToOperationComponentId;

    public OperationProductInGroupingContainerDecorator(OperationMergeService operationMergeService,
            GroupingContainer groupingContainer, ProductQuantitiesService productQuantitiesService) {
        this.operationMergeService = operationMergeService;
        this.groupingContainer = groupingContainer;
        this.productQuantitiesService = productQuantitiesService;
        initMaps();
    }

    private void initMaps() {
        this.operationComponentIdToOrder = new HashMap<Long, Entity>();
        this.operationComponentIdToOperationComponent = new HashMap<Long, Entity>();
        this.operationNumberToOperationComponentId = new HashMap<String, Long>();
    }

    @Override
    public void add(Entity order, Entity operationComponent) {
        operationComponentIdToOrder.put(operationComponent.getId(), order);
        operationComponentIdToOperationComponent.put(operationComponent.getId(), operationComponent);
        List<Entity> orders = Lists.newArrayList(order);
        OperationProductComponentWithQuantityContainer productQuantities = productQuantitiesService
                .getProductComponentQuantities(orders);

        String operationNumber = operationNumber(operationComponent);
        boolean quantityChanged = false;
        if (operationAlreadyExists(operationNumber)) {
            Long operationComponentId = operationNumberToOperationComponentId.get(operationNumber);
            Entity existingOperationComponent = operationComponentIdToOperationComponent.get(operationComponentId);

            List<Entity> existingOperationProductInComponents = operationProductInComponents(existingOperationComponent);
            List<Entity> operationProductInComponents = operationProductInComponents(operationComponent);
            Map<String, Entity> existingProductNumberToOperationProductInComponent = productNumberToOperationProductComponent(
                    existingOperationProductInComponents);
            Map<String, Entity> productNumberToOperationProductInComponent = productNumberToOperationProductComponent(
                    operationProductInComponents);
            if (containsAll(existingProductNumberToOperationProductInComponent, productNumberToOperationProductInComponent)) {
                for (Map.Entry<String, Entity> entry : existingProductNumberToOperationProductInComponent.entrySet()) {
                    Entity existingOperationProductInComponent = entry.getValue();
                    Entity operationProductInComponent = productNumberToOperationProductInComponent.get(entry.getKey());
                    BigDecimal quantity = productQuantities.get(operationProductInComponent);
                    BigDecimal increasedQuantity = increaseQuantityBy(productQuantities, existingOperationProductInComponent,
                            quantity);
                    quantity(operationProductInComponent, BigDecimal.ZERO);
                    quantityChanged = true;
                    operationMergeService
                            .mergeProductIn(existingOperationComponent, operationProductInComponent, increasedQuantity);
                    operationMergeService
                            .storeProductIn(existingOperationComponent, operationComponent, operationProductInComponent,
                                    quantity.negate());
                }
            }

            List<Entity> existingOperationProductOutComponents = operationProductOutComponents(existingOperationComponent);
            List<Entity> operationProductOutComponents = operationProductOutComponents(operationComponent);
            Map<String, Entity> existingProductNumberToOperationProductOutComponent = productNumberToOperationProductComponent(
                    existingOperationProductOutComponents);
            Map<String, Entity> productNumberToOperationProductOutComponent = productNumberToOperationProductComponent(
                    operationProductOutComponents);
            for (Map.Entry<String, Entity> entry : productNumberToOperationProductOutComponent.entrySet()) {
                Entity operationProductOutComponent = entry.getValue();
                Entity existingOperationProductOutComponent = existingProductNumberToOperationProductOutComponent
                        .get(entry.getKey());
                if (existingOperationProductOutComponent == null) {
                    existingOperationProductOutComponents.add(operationProductOutComponent);
                    operationMergeService.mergeProductOut(existingOperationComponent, operationProductOutComponent,
                            quantity(productQuantities, operationProductOutComponent));
                    operationMergeService
                            .storeProductOut(existingOperationComponent, operationComponent, operationProductOutComponent,
                                    quantity(productQuantities, operationProductOutComponent));
                } else {
                    BigDecimal quantity = productQuantities.get(operationProductOutComponent);
                    BigDecimal increasedQuantity = increaseQuantityBy(productQuantities, existingOperationProductOutComponent,
                            quantity);
                    quantity(operationProductOutComponent, BigDecimal.ZERO);
                    operationMergeService
                            .mergeProductOut(existingOperationComponent, existingOperationProductOutComponent, increasedQuantity);
                    operationMergeService
                            .storeProductOut(existingOperationComponent, operationComponent, operationProductOutComponent,
                                    quantity.negate());
                }
            }

        } else {
            operationNumberToOperationComponentId.put(operationNumber, operationComponent.getId());
        }

        if (quantityChanged) {
            return;
        }

        groupingContainer.add(order, operationComponent);

    }

    private boolean containsAll(Map<String, Entity> map1, Map<String, Entity> map2) {
        return map1.keySet().containsAll(map2.keySet());
    }

    private Map<String, Entity> productNumberToOperationProductComponent(List<Entity> list) {
        Map<String, Entity> productNumberToOperationProductIn = new HashMap<String, Entity>();
        for (Entity entity : list) {
            productNumberToOperationProductIn.put(productNumber(entity), entity);
        }
        return productNumberToOperationProductIn;
    }

    @Override
    public ListMultimap<String, OrderOperationComponent> getTitleToOperationComponent() {
        return groupingContainer.getTitleToOperationComponent();
    }

    @Override
    public List<Entity> getOrders() {
        return groupingContainer.getOrders();
    }

    @Override
    public boolean hasManyOrders() {
        return groupingContainer.hasManyOrders();
    }

    @Override
    public Map<OrderColumn, ColumnAlignment> getOrderColumnToAlignment() {
        return groupingContainer.getOrderColumnToAlignment();
    }

    @Override
    public Map<Long, Map<OperationProductColumn, ColumnAlignment>> getOperationComponentIdProductInColumnToAlignment() {
        return groupingContainer.getOperationComponentIdProductInColumnToAlignment();
    }

    @Override
    public Map<Long, Map<OperationProductColumn, ColumnAlignment>> getOperationComponentIdProductOutColumnToAlignment() {
        return groupingContainer.getOperationComponentIdProductOutColumnToAlignment();
    }

    private BigDecimal increaseQuantityBy(OperationProductComponentWithQuantityContainer quantityContainer,
            Entity operationProductInComponent, BigDecimal quantity) {
        BigDecimal increasedQuantity = quantity(quantityContainer, operationProductInComponent).add(quantity);
        quantity(operationProductInComponent, increasedQuantity);
        return increasedQuantity;
    }

    private BigDecimal quantity(OperationProductComponentWithQuantityContainer quantityContainer,
            Entity operationProductInComponent) {
        return quantityContainer.get(operationProductInComponent);
    }

    private void quantity(Entity operationProductInComponent, BigDecimal quantity) {
        operationProductInComponent.setField(OperationProductInComponentFields.QUANTITY, quantity);
    }

    private boolean operationAlreadyExists(String operationNumber) {
        return operationNumberToOperationComponentId.containsKey(operationNumber);
    }

    private String productNumber(Entity operationProductInComponent) {
        return product(operationProductInComponent).getStringField(ProductFields.NUMBER);
    }

    private Entity product(Entity operationProductInComponent) {
        return operationProductInComponent.getBelongsToField(OperationProductInComponentFields.PRODUCT);
    }

    private String operationNumber(Entity operationComponent) {
        return operation(operationComponent).getStringField(OperationFields.NUMBER);
    }

    private Entity operation(Entity operationComponent) {
        return operationComponent.getBelongsToField(TechnologyOperationComponentFields.OPERATION);
    }

    private List<Entity> operationProductInComponents(Entity operationComponent) {
        return operationComponent.getHasManyField(TechnologyOperationComponentFields.OPERATION_PRODUCT_IN_COMPONENTS);
    }

    private List<Entity> operationProductOutComponents(Entity operationComponent) {
        return operationComponent.getHasManyField(TechnologyOperationComponentFields.OPERATION_PRODUCT_OUT_COMPONENTS);
    }

}
