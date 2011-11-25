/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.0
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
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basicProductionCounting.constants.BasicProductionCountingConstants;
import com.qcadoo.mes.materialRequirements.api.MaterialRequirementReportDataService;
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

    @Autowired
    private MaterialRequirementReportDataService materialRequrements;

    private Entity order;

    private final String modelFieldOrder = "order";

    private final String modelFieldProduct = "product";

    private final String modelFieldRequiredQuantity = "plannedQuantity";

    private void getProductionRecordsFromOrder(final Entity order) {
        SearchCriteriaBuilder criteriaBuilder = dataDefinitionService.get("productionCounting", "productionRecord").find();
        List<Entity> productionRecords = criteriaBuilder.add(SearchRestrictions.belongsTo("order", order)).list().getEntities();

        if (productionRecords.isEmpty()) {
            return;
        }
        // clean table for specified order
        removeProductionRecordForOrder(order);
        if ("02cumulated".equals(order.getField("typeOfProductionRecording"))) {
            getUsedProductFromRecord(productionRecords.get(0), false);
            getProducedProductFromRecord(productionRecords.get(0), false);
        }
        if ("03forEach".equals(order.getField("typeOfProductionRecording"))) {
            for (Entity productionRecord : productionRecords) {
                getUsedProductFromRecord(productionRecord, true);
                getProducedProductFromRecord(productionRecord, true);
            }
        }
    }

    private void removeProductionRecordForOrder(Entity order) {
        List<Entity> countings = dataDefinitionService
                .get(BasicProductionCountingConstants.PLUGIN_IDENTIFIER,
                        BasicProductionCountingConstants.MODEL_BASIC_PRODUCTION_COUNTING).find()
                .add(SearchRestrictions.belongsTo("order", order)).list().getEntities();
        for (Entity counting : countings) {
            counting.getDataDefinition().delete(counting.getId());
        }

    }

    /*
     * TODO: BAKU facade pattern
     */

    private void getProducedProductFromRecord(final Entity productionRecord, final boolean forEach) {
        SearchCriteriaBuilder searchBuilder = dataDefinitionService.get("productionCounting",
                "recordOperationProductOutComponent").find();
        List<Entity> productsOut = searchBuilder.add(SearchRestrictions.belongsTo("productionRecord", productionRecord)).list()
                .getEntities();

        if (productsOut.isEmpty()) {
            return;
        }
        for (Entity productOut : productsOut) {
            addProducedProductQuantityToCounting(productOut, forEach);
        }
    }

    private void addProducedProductQuantityToCounting(final Entity record, final boolean forEach) {
        SearchCriteriaBuilder searchBuilder = dataDefinitionService.get(BasicProductionCountingConstants.PLUGIN_IDENTIFIER,
                BasicProductionCountingConstants.MODEL_BASIC_PRODUCTION_COUNTING).find();
        List<Entity> productionCountings = searchBuilder.add(SearchRestrictions.belongsTo("order", order)).list().getEntities();

        final Map<Entity, BigDecimal> products = materialRequrements.getQuantitiesForOrdersTechnologyProducts(
                Arrays.asList(order), false);

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
            BigDecimal producedQuantity = (BigDecimal) foundCounting.getField("producedQuantity");
            if (forEach) {
                BigDecimal addition = (BigDecimal) record.getField("usedQuantity");
                if (producedQuantity == null) {
                    producedQuantity = addition;
                } else {
                    producedQuantity = producedQuantity.add(addition);
                }
            }
            foundCounting.setField("producedQuantity", producedQuantity);
        }
        for (Entry<Entity, BigDecimal> product : products.entrySet()) {
            if (product.getKey().getId().equals(foundCounting.getBelongsToField("product").getId())) {
                foundCounting.setField("plannedQuantity", product.getValue());
            }
        }
        foundCounting = foundCounting.getDataDefinition().save(foundCounting);
        if (!foundCounting.isValid()) {
            throw new IllegalStateException("basicProductionCounting entity is invalid");
        }
    }

    private void getUsedProductFromRecord(final Entity productionRecord, final boolean forEach) {
        SearchCriteriaBuilder searchBuilder = dataDefinitionService
                .get("productionCounting", "recordOperationProductInComponent").find();
        List<Entity> productsIn = searchBuilder.add(SearchRestrictions.belongsTo("productionRecord", productionRecord)).list()
                .getEntities();

        if (productsIn.isEmpty()) {
            return;
        }

        for (Entity productIn : productsIn) {
            addUsedproductQuantityToCounting(productIn, forEach);
        }
    }

    private void addUsedproductQuantityToCounting(final Entity record, final boolean forEach) {
        SearchCriteriaBuilder searchBuilder = dataDefinitionService.get(BasicProductionCountingConstants.PLUGIN_IDENTIFIER,
                BasicProductionCountingConstants.MODEL_BASIC_PRODUCTION_COUNTING).find();
        List<Entity> productionCountings = searchBuilder.add(SearchRestrictions.belongsTo("order", order)).list().getEntities();

        final Map<Entity, BigDecimal> products = materialRequrements.getQuantitiesForOrdersTechnologyProducts(
                Arrays.asList(order), false);

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
            BigDecimal usedQuantity = (BigDecimal) foundCounting.getField("usedQuantity");
            if (forEach) {
                BigDecimal addition = (BigDecimal) record.getField("usedQuantity");
                if (usedQuantity == null) {
                    usedQuantity = addition;
                } else {
                    usedQuantity = usedQuantity.add(addition);
                }
            }
            foundCounting.setField("usedQuantity", usedQuantity);
        }
        for (Entry<Entity, BigDecimal> product : products.entrySet()) {
            if (product.getKey().getId().equals(foundCounting.getBelongsToField("product").getId())) {
                foundCounting.setField("plannedQuantity", product.getValue());
            }
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

        Map<Entity, BigDecimal> requirement = materialRequrements.getQuantitiesForOrdersTechnologyProducts(Arrays.asList(order),
                false);

        List<Entity> producedProducts = dataDefinitionService
                .get(BasicProductionCountingConstants.PLUGIN_IDENTIFIER,
                        BasicProductionCountingConstants.MODEL_BASIC_PRODUCTION_COUNTING).find()
                .add(SearchRestrictions.belongsTo(modelFieldOrder, order)).list().getEntities();

        for (Entry<Entity, BigDecimal> product : requirement.entrySet()) {
            Entity foundProduct = null;

            for (Entity producedProduct : producedProducts) {
                if (producedProduct.getBelongsToField(modelFieldProduct).getId().equals(product.getKey().getId())) {
                    foundProduct = producedProduct;
                    break;
                }
            }

            if (foundProduct == null) {
                final Entity newProduct = dataDefinitionService.get(BasicProductionCountingConstants.PLUGIN_IDENTIFIER,
                        BasicProductionCountingConstants.MODEL_BASIC_PRODUCTION_COUNTING).create();
                newProduct.setField(modelFieldOrder, order);
                newProduct.setField(modelFieldProduct, product.getKey());
                newProduct.setField(modelFieldRequiredQuantity, product.getValue());
                newProduct.getDataDefinition().save(newProduct);
                if (!newProduct.isValid()) {
                    throw new IllegalStateException("new product entity is invalid  " + product.getValue() + "\n\n\n");
                }
            } else {
                BigDecimal plannedQuantity = (BigDecimal) foundProduct.getField(modelFieldRequiredQuantity);
                if (plannedQuantity != product.getValue()) {
                    foundProduct.setField(modelFieldRequiredQuantity, product.getValue());
                    foundProduct.getDataDefinition().save(foundProduct);
                }
            }
        }
        producedProducts = dataDefinitionService
                .get(BasicProductionCountingConstants.PLUGIN_IDENTIFIER,
                        BasicProductionCountingConstants.MODEL_BASIC_PRODUCTION_COUNTING).find()
                .add(SearchRestrictions.belongsTo(modelFieldOrder, order)).list().getEntities();

        for (Entity producedProduct : producedProducts) {
            boolean found = false;
            for (Entry<Entity, BigDecimal> product : requirement.entrySet()) {
                if (producedProduct.getBelongsToField(modelFieldProduct).equals(product.getKey())) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                dataDefinitionService.get(BasicProductionCountingConstants.PLUGIN_IDENTIFIER,
                        BasicProductionCountingConstants.MODEL_BASIC_PRODUCTION_COUNTING).delete(producedProduct.getId());
            }
        }

    }
}
