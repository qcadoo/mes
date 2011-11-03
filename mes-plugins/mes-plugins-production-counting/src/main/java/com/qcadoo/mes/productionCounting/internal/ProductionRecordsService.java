package com.qcadoo.mes.productionCounting.internal;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.productionCounting.internal.constants.ProductionCountingConstants;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.ViewDefinitionState;

@Service
public class ProductionRecordsService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

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
        SearchCriteriaBuilder searchBuilder = dataDefinitionService.get(ProductionCountingConstants.PLUGIN_IDENTIFIER,
                ProductionCountingConstants.MODEL_RECORD_OPERATION_PRODUCT_OUT_COMPONENT).find();
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
}
