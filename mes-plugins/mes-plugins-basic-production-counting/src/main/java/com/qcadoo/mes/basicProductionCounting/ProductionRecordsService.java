package com.qcadoo.mes.basicProductionCounting;

import static com.qcadoo.mes.orders.constants.OrdersConstants.MODEL_ORDER;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.basicProductionCounting.constants.BasicProductionCountingConstants;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;

@Service
public class ProductionRecordsService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private TranslationService translationService;

    private Entity order;

    private void getProductionRecordsFromOrder(final Entity order) {
        SearchCriteriaBuilder criteriaBuilder = dataDefinitionService.get("productionCounting", "productionRecord").find();
        List<Entity> productionRecords = criteriaBuilder.add(SearchRestrictions.belongsTo("order", order)).list().getEntities();

        if (productionRecords.isEmpty()) {
            return;
        }

        if ("02cumulated".equals(order.getField("typeOfProductionRecording"))) {
            getUsedProductFromRecord(productionRecords.get(0));
            getProducedProductFromRecord(productionRecords.get(0));
        }
        if ("03forEach".equals(order.getField("typeOfProductionRecording"))) {
            for (Entity productionRecord : productionRecords) {
                getUsedProductFromRecord(productionRecord);
                getProducedProductFromRecord(productionRecord);
            }
        }
    }

    private void getProducedProductFromRecord(final Entity productionRecord) {
        SearchCriteriaBuilder searchBuilder = dataDefinitionService.get("productionCounting",
                "recordOperationProductOutComponent").find();
        List<Entity> productsOut = searchBuilder.add(SearchRestrictions.belongsTo("productionRecord", productionRecord)).list()
                .getEntities();

        if (productsOut.isEmpty()) {
            return;
        }
        for (Entity productOut : productsOut) {
            addProducedProductQuantityToCounting(productOut);
        }
    }

    private void addProducedProductQuantityToCounting(Entity record) {
        SearchCriteriaBuilder searchBuilder = dataDefinitionService.get("basicProductionCounting", "basicProductionCounting")
                .find();
        List<Entity> productionCountings = searchBuilder.add(SearchRestrictions.belongsTo("order", order)).list().getEntities();
        Entity foundCounting = null;
        for (Entity productionCounting : productionCountings) {
            if (productionCounting.getBelongsToField("product").equals(record.getBelongsToField("product"))) {
                foundCounting = productionCounting;
                break;
            }
        }
        if (foundCounting == null) {
            foundCounting = dataDefinitionService.get("basicProductionCounting", "basicProductionCounting").create();
            foundCounting.setField("order", order);
            foundCounting.setField("product", record.getBelongsToField("product"));
            foundCounting.setField("producedQuantity", record.getField("usedQuantity"));
        } else {
            foundCounting.setField("producedQuantity", record.getField("usedQuantity"));
            // foundCounting.setField("plannedQuantity", record.getField("plannedQuantity"));
        }
        foundCounting = foundCounting.getDataDefinition().save(foundCounting);
        if (!foundCounting.isValid()) {
            throw new IllegalStateException("basicProductionCounting entity is invalid");
        }
    }

    private void getUsedProductFromRecord(final Entity productionRecord) {
        SearchCriteriaBuilder searchBuilder = dataDefinitionService
                .get("productionCounting", "recordOperationProductInComponent").find();
        List<Entity> productsIn = searchBuilder.add(SearchRestrictions.belongsTo("productionRecord", productionRecord)).list()
                .getEntities();

        if (productsIn.isEmpty()) {
            return;
        }

        for (Entity productIn : productsIn) {
            addUsedproductQuantityToCounting(productIn);
        }
    }

    private void addUsedproductQuantityToCounting(final Entity record) {
        SearchCriteriaBuilder searchBuilder = dataDefinitionService.get("basicProductionCounting", "basicProductionCounting")
                .find();
        List<Entity> productionCountings = searchBuilder.add(SearchRestrictions.belongsTo("order", order)).list().getEntities();
        Entity foundCounting = null;
        for (Entity productionCounting : productionCountings) {
            if (productionCounting.getBelongsToField("product").equals(record.getBelongsToField("product"))) {
                foundCounting = productionCounting;
                break;
            }
        }
        if (foundCounting == null) {
            foundCounting = dataDefinitionService.get("basicProductionCounting", "basicProductionCounting").create();
            foundCounting.setField("order", order);
            foundCounting.setField("product", record.getBelongsToField("product"));
            foundCounting.setField("usedQuantity", record.getField("usedQuantity"));
        } else {
            foundCounting.setField("usedQuantity", record.getField("usedQuantity"));
        }
        foundCounting = foundCounting.getDataDefinition().save(foundCounting);
        if (!foundCounting.isValid()) {
            throw new IllegalStateException("basicProductionCounting entity is invalid");
        }
    }

    public void generateUsedProducts(final ViewDefinitionState state) {
        String orderNumber = (String) state.getComponentByReference("number").getFieldValue();

        if (orderNumber != null) {
            Entity order = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).find()
                    .add(SearchRestrictions.eq("number", orderNumber)).setMaxResults(1).uniqueResult();
            if (order != null) {
                this.order = order;
                getProductionRecordsFromOrder(order);
            }
        }
    }

    private Entity getOrderFromLookup(final ViewDefinitionState view) {
        ComponentState lookup = view.getComponentByReference("order");
        if (!(lookup.getFieldValue() instanceof Long)) {
            return null;
        }
        return dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, MODEL_ORDER).get((Long) lookup.getFieldValue());
    }

    public void getProductsFromOrder(ViewDefinitionState view) {
        String orderNumber = (String) view.getComponentByReference("number").getFieldValue();

        if (orderNumber == null) {
            return;
        }
        Entity order = dataDefinitionService.get("orders", "order").find().add(SearchRestrictions.eq("number", orderNumber))
                .uniqueResult();
        if (order == null) {
            return;
        }
        List<Entity> operationComponents = order.getTreeField("orderOperationComponents");

        if (operationComponents == null)
            return;

        for (Entity operationComponent : operationComponents) {
            List<Entity> productsIn = operationComponent.getBelongsToField("technologyOperationComponent").getHasManyField(
                    "operationProductInComponents");
            List<Entity> productsOut = operationComponent.getBelongsToField("technologyOperationComponent").getHasManyField(
                    "operationProductOutComponents");

            if (productsIn != null && !productsIn.isEmpty()) {

                for (Entity productIn : productsIn) {
                    Entity product = productIn.getBelongsToField("product");
                    List<Entity> allBPCs = dataDefinitionService
                            .get(BasicProductionCountingConstants.MODEL_BASIC_PRODUCTION_COUNTING,
                                    BasicProductionCountingConstants.PLUGIN_IDENTIFIER).find().list().getEntities();
                    boolean found = false;
                    for (Entity BPC : allBPCs) {
                        found = isEntitiAlreadyInDatabase(product, order, BPC, (BigDecimal) productIn.getField("quantity"));
                        if (found) {
                            break;
                        }
                        // if (isEntitiAlreadyInDatabase(product, order, BPC, (BigDecimal) productIn.getField("quantity")) ==
                        // true) {
                        // found = true;
                        // break;
                        // }
                    }
                    if (found) {
                        continue;
                    }
                    Entity producedProduct = dataDefinitionService.get("basicProductionCounting", "basicProductionCounting")
                            .create();
                    producedProduct.setField("product", product);
                    producedProduct.setField("plannedQuantity", productIn.getField("quantity"));
                    producedProduct.setField("order", order);
                    producedProduct = producedProduct.getDataDefinition().save(producedProduct);
                    if (!producedProduct.isValid()) {
                        throw new IllegalStateException("basicProductionCounting entity is invalid.");
                    }
                }
            }
            if (productsOut != null && !productsOut.isEmpty()) {
                for (Entity productOut : productsOut) {
                    Entity product = productOut.getBelongsToField("product");
                    List<Entity> allBPCs = dataDefinitionService
                            .get(BasicProductionCountingConstants.MODEL_BASIC_PRODUCTION_COUNTING,
                                    BasicProductionCountingConstants.PLUGIN_IDENTIFIER).find().list().getEntities();
                    boolean found = false;
                    for (Entity BPC : allBPCs) {
                        found = isEntitiAlreadyInDatabase(product, order, BPC, (BigDecimal) productOut.getField("quantity"));
                        if (found) {
                            break;
                        }
                        // if (isEntitiAlreadyInDatabase(product, order, BPC, (BigDecimal) productOut.getField("quantity"))) {
                        // found = true;
                        // break;
                        // }
                    }
                    if (found) {
                        continue;
                    }
                    Entity producedProduct = dataDefinitionService.get("basicProductionCounting", "basicProductionCounting")
                            .create();
                    producedProduct.setField("product", product);
                    producedProduct.setField("plannedQuantity", productOut.getField("quantity"));
                    producedProduct.setField("order", order);
                    producedProduct = producedProduct.getDataDefinition().save(producedProduct);
                    if (!producedProduct.isValid()) {
                        throw new IllegalStateException("basicProductionCounting entity is invalid.");
                    }
                }
            }
        }

    }

    private boolean isEntitiAlreadyInDatabase(Entity product, Entity order, Entity productionCounting, BigDecimal quantity) {
        Entity productionOrder = (Entity) productionCounting.getField("order");
        Entity productionProduct = (Entity) productionCounting.getField("product");
        BigDecimal productionQuantity = (BigDecimal) productionCounting.getField("plannedQuantity");

        if ((productionOrder.getId() == order.getId()) && (productionProduct.getId() == product.getId())
                && (productionQuantity.equals(quantity))) {
            return true;
        }

        return false;
    }
}
