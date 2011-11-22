/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.4.10
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
package com.qcadoo.mes.basicProductionCounting;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basicProductionCounting.constants.BasicProductionCountingConstants;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.technologies.TechnologyService;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;

@Service
public class ProductionRecordsService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private TechnologyService technologyService;

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

    private void addProducedProductQuantityToCounting(final Entity record) {
        SearchCriteriaBuilder searchBuilder = dataDefinitionService.get(BasicProductionCountingConstants.PLUGIN_IDENTIFIER,
                BasicProductionCountingConstants.MODEL_BASIC_PRODUCTION_COUNTING).find();
        List<Entity> productionCountings = searchBuilder.add(SearchRestrictions.belongsTo("order", order)).list().getEntities();
        Entity foundCounting = null;
        for (Entity productionCounting : productionCountings) {
            if (productionCounting.getBelongsToField("product").equals(record.getBelongsToField("product"))) {
                foundCounting = productionCounting;
                break;
            }
        }
        if (foundCounting == null) {
            foundCounting = dataDefinitionService.get(BasicProductionCountingConstants.PLUGIN_IDENTIFIER,
                    BasicProductionCountingConstants.MODEL_BASIC_PRODUCTION_COUNTING).create();
            foundCounting.setField("order", order);
            foundCounting.setField("product", record.getBelongsToField("product"));
            foundCounting.setField("producedQuantity", record.getField("usedQuantity"));
        } else {
            foundCounting.setField("producedQuantity", record.getField("usedQuantity"));
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
        SearchCriteriaBuilder searchBuilder = dataDefinitionService.get(BasicProductionCountingConstants.PLUGIN_IDENTIFIER,
                BasicProductionCountingConstants.MODEL_BASIC_PRODUCTION_COUNTING).find();
        List<Entity> productionCountings = searchBuilder.add(SearchRestrictions.belongsTo("order", order)).list().getEntities();
        Entity foundCounting = null;
        for (Entity productionCounting : productionCountings) {
            if (productionCounting.getBelongsToField("product").equals(record.getBelongsToField("product"))) {
                foundCounting = productionCounting;
                break;
            }
        }
        if (foundCounting == null) {
            foundCounting = dataDefinitionService.get(BasicProductionCountingConstants.PLUGIN_IDENTIFIER,
                    BasicProductionCountingConstants.MODEL_BASIC_PRODUCTION_COUNTING).create();
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

    public void setProducedQuantity(final ViewDefinitionState view) {
        FieldComponent typeOfProductionRecording = (FieldComponent) view.getComponentByReference("typeOfProductionRecording");
        FieldComponent doneQuantity = (FieldComponent) view.getComponentByReference("doneQuantity");
        String orderNumber = (String) view.getComponentByReference("number").getFieldValue();
        Entity order;
        List<Entity> productionCountings;

        if ("".equals(typeOfProductionRecording.getFieldValue())) {
            return;
        }

        if (orderNumber == null) {
            return;
        }
        order = dataDefinitionService.get("orders", "order").find().add(SearchRestrictions.eq("number", orderNumber))
                .uniqueResult();
        if (order == null) {
            return;
        }
        productionCountings = dataDefinitionService
                .get(BasicProductionCountingConstants.PLUGIN_IDENTIFIER,
                        BasicProductionCountingConstants.MODEL_BASIC_PRODUCTION_COUNTING).find()
                .add(SearchRestrictions.belongsTo("order", order)).list().getEntities();

        if (productionCountings.isEmpty()) {
            return;
        }

        Entity technology = order.getBelongsToField("technology");

        for (Entity counting : productionCountings) {
            Entity aProduct = (Entity) counting.getField("product");
            String type = technologyService.getProductType(aProduct, technology);
            if (type.equals(TechnologyService.PRODUCT)) {
                doneQuantity.setFieldValue(counting.getField("producedQuantity"));
                break;
            }
        }
    }

    public void getProductsFromOrder(final ViewDefinitionState view) {
        String orderNumber = (String) view.getComponentByReference("number").getFieldValue();

        if (orderNumber == null) {
            return;
        }
        Entity order = dataDefinitionService.get("orders", "order").find().add(SearchRestrictions.eq("number", orderNumber))
                .uniqueResult();
        if (order == null) {
            return;
        }

        if (!"01basic".equals(order.getField("typeOfProductionRecording"))) {
            return;
        }

        List<Entity> operationComponents = order.getTreeField("orderOperationComponents");

        if (operationComponents == null) {
            return;
        }

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
                    for (Entity bpc : allBPCs) {
                        found = isEntitiAlreadyInDatabase(product, order, bpc, (BigDecimal) productIn.getField("quantity"));
                        if (found) {
                            break;
                        }
                    }
                    if (found) {
                        continue;
                    }
                    Entity producedProduct = dataDefinitionService.get(BasicProductionCountingConstants.PLUGIN_IDENTIFIER,
                            BasicProductionCountingConstants.MODEL_BASIC_PRODUCTION_COUNTING).create();
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
                    for (Entity bpc : allBPCs) {
                        found = isEntitiAlreadyInDatabase(product, order, bpc, (BigDecimal) productOut.getField("quantity"));
                        if (found) {
                            break;
                        }
                    }
                    if (found) {
                        continue;
                    }
                    Entity producedProduct = dataDefinitionService.get(BasicProductionCountingConstants.PLUGIN_IDENTIFIER,
                            BasicProductionCountingConstants.MODEL_BASIC_PRODUCTION_COUNTING).create();
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

    private boolean isEntitiAlreadyInDatabase(final Entity product, final Entity order, final Entity productionCounting,
            final BigDecimal quantity) {
        Entity productionOrder = (Entity) productionCounting.getField("order");
        Entity productionProduct = (Entity) productionCounting.getField("product");
        BigDecimal productionQuantity = (BigDecimal) productionCounting.getField("plannedQuantity");

        if ((productionOrder.getId().equals(order.getId())) && (productionProduct.getId().equals(product.getId()))
                && (productionQuantity.equals(quantity))) {
            return true;
        }

        return false;
    }
}
