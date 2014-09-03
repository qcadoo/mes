

package com.qcadoo.mes.workPlans.pdf.document.operation.grouping.container;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.MultiHashMap;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.columnExtension.constants.ColumnAlignment;
import com.qcadoo.mes.technologies.ProductQuantitiesService;
import com.qcadoo.mes.technologies.constants.OperationFields;
import com.qcadoo.mes.technologies.constants.OperationProductInComponentFields;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.mes.technologies.dto.OperationProductComponentWithQuantityContainer;
import com.qcadoo.mes.technologies.grouping.OperationMergeService;
import com.qcadoo.mes.workPlans.pdf.document.operation.grouping.container.util.OrderIdOperationNumberOperationComponentIdMap;
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

    private OrderIdOperationNumberOperationComponentIdMap orderIdOperationNumberOperationComponentIdMap;

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
        this.orderIdOperationNumberOperationComponentIdMap = OrderIdOperationNumberOperationComponentIdMap.create();
    }

    @Override
    public void add(Entity order, Entity operationComponent, OperationProductComponentWithQuantityContainer productQuantities) {
        removeAlreadyExistsMergesForOrder(order);

        operationComponentIdToOrder.put(operationComponent.getId(), order);
        operationComponentIdToOperationComponent.put(operationComponent.getId(), operationComponent);

        String operationNumber = operationNumber(operationComponent);
        boolean quantityChanged = false;
        if (operationAlreadyExists(operationNumber)) {
            Long operationComponentId = operationNumberToOperationComponentId.get(operationNumber);
            Entity existingToc = operationComponentIdToOperationComponent.get(operationComponentId);

            List<Entity> existingOperationProductInComponents = operationProductInComponents(existingToc);
            List<Entity> operationProductInComponents = operationProductInComponents(operationComponent);
            Map<String, Entity> existingProductNumberToOperationProductInComponent = productNumberToOperationProductComponent(
                    existingOperationProductInComponents);
            Map<String, Entity> productNumberToOperationProductInComponent = productNumberToOperationProductComponent(
                    operationProductInComponents);
            boolean sameProductsIn = sameProductsIn(existingProductNumberToOperationProductInComponent,
                    productNumberToOperationProductInComponent);
            if (sameProductsIn) {
                for (Map.Entry<String, Entity> entry : existingProductNumberToOperationProductInComponent.entrySet()) {
                    Entity existingOperationProductInComponent = entry.getValue();
                    Entity operationProductInComponent = productNumberToOperationProductInComponent.get(entry.getKey());
                    BigDecimal quantity = productQuantities.get(operationProductInComponent);
                    BigDecimal increasedQuantity = increaseQuantityBy(productQuantities, existingOperationProductInComponent,
                            quantity);
                    quantity(operationProductInComponent, BigDecimal.ZERO);
                    quantityChanged = true;
                    operationMergeService
                            .mergeProductIn(order, existingToc, existingOperationProductInComponent, increasedQuantity);
                    operationMergeService
                            .storeProductIn(order, existingToc, operationComponent, operationProductInComponent,
                                    quantity.negate());
                }

                List<Entity> existingOperationProductOutComponents = Lists
                        .newArrayList(operationProductOutComponents(existingToc));
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
                        quantity(operationProductOutComponent, productQuantities.get(operationProductOutComponent));
                        existingOperationProductOutComponents.add(operationProductOutComponent);
                        operationMergeService.mergeProductOut(order, existingToc, operationProductOutComponent,
                                quantity(productQuantities, operationProductOutComponent));
                        operationMergeService
                                .storeProductOut(order, existingToc, operationComponent, operationProductOutComponent,
                                        null);
                    } else {
                        BigDecimal quantity = productQuantities.get(operationProductOutComponent);
                        BigDecimal increasedQuantity = increaseQuantityBy(productQuantities, existingOperationProductOutComponent,
                                quantity);
                        quantityChanged = true;
                        operationMergeService
                                .mergeProductIn(order, existingToc,  existingOperationProductInComponent, increasedQuantity);
                        operationMergeService
                                .storeProductIn(order, existingToc,  operationComponent, operationProductInComponent,
                                        quantity.negate());
                    }

                    List<Entity> existingOperationProductOutComponents = Lists.newArrayList(operationProductOutComponents(existingToc));
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
                            quantity(operationProductOutComponent, productQuantities.get(operationProductOutComponent));
                            existingOperationProductOutComponents.add(operationProductOutComponent);
                            operationMergeService.mergeProductOut(order, existingToc, operationProductOutComponent,
                                    quantity(productQuantities, operationProductOutComponent));
                            operationMergeService
                                    .storeProductOut(order, existingToc, operationComponent, operationProductOutComponent,
                                            null);
                        } else {
                            BigDecimal quantity = productQuantities.get(operationProductOutComponent);
                            BigDecimal increasedQuantity = increaseQuantityBy(productQuantities, existingOperationProductOutComponent,
                                    quantity);
                            quantity(operationProductOutComponent, BigDecimal.ZERO);
                            operationMergeService
                                    .mergeProductOut(order, existingToc, existingOperationProductOutComponent, increasedQuantity);
                            operationMergeService
                                    .storeProductOut(order, existingToc, operationComponent, operationProductOutComponent,
                                            quantity.negate());
                        }
                    }
                    existingToc.setField(TechnologyOperationComponentFields.OPERATION_PRODUCT_OUT_COMPONENTS, existingOperationProductOutComponents);
                }
                existingToc.setField(TechnologyOperationComponentFields.OPERATION_PRODUCT_OUT_COMPONENTS,
                        existingOperationProductOutComponents);
            }
        }

        if (quantityChanged) {
            return;
        }

        orderIdOperationNumberOperationComponentIdMap.put(order.getId(), operationNumber, operationComponent.getId());

        for (Entity operationProductInComponent : operationProductInComponents(operationComponent)) {
            quantity(operationProductInComponent, productQuantities.get(operationProductInComponent));
        }

        for (Entity operationProductOutComponent : operationProductOutComponents(operationComponent)) {
            quantity(operationProductOutComponent, productQuantities.get(operationProductOutComponent));
        }

        groupingContainer.add(order, operationComponent, productQuantities);


    }

    private boolean sameProductsIn(Map<String, Entity> existingProductNumberToOperationProductInComponent,
            Map<String, Entity> productNumberToOperationProductInComponent) {
        return containsAll(existingProductNumberToOperationProductInComponent, productNumberToOperationProductInComponent);
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
        BigDecimal increasedQuantity = quantity(operationProductInComponent).add(quantity);
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

    private BigDecimal quantity(Entity operationProductInComponent) {
        return operationProductInComponent.getDecimalField(OperationProductInComponentFields.QUANTITY);
    }

    private boolean operationAlreadyExists(Entity order, String operationNumber) {
        return orderIdOperationNumberOperationComponentIdMap.containsKey(order.getId(), operationNumber);
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
