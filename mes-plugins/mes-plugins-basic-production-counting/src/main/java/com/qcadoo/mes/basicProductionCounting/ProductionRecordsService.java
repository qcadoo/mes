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

    private static final String MODEL_FIELD_ORDER = "order";

    private static final String MODEL_FIELD_PRODUCT = "product";

    private static final String MODEL_FIELD_PLANNED_QUANTITY = "plannedQuantity";

    private static final String MODEL_FIELD_TYPE_OF_PROD_REC = "typeOfProductionRecording";

    private static final String MODEL_FIELD_NUMBER = "number";

    private void getProductionRecordsFromOrder(final Entity order) {
        final SearchCriteriaBuilder criteriaBuilder = dataDefinitionService.get("productionCounting", "productionRecord").find();
        final List<Entity> productionRecords = criteriaBuilder.add(SearchRestrictions.belongsTo(MODEL_FIELD_ORDER, order)).list()
                .getEntities();

        if (productionRecords.isEmpty()) {
            return;
        }
        removeProductionRecordForOrder(order);
        if ("02cumulated".equals(order.getField(MODEL_FIELD_TYPE_OF_PROD_REC))) {
            getUsedProductFromRecord(productionRecords.get(0), false);
            getProducedProductFromRecord(productionRecords.get(0), false);
        }
        if ("03forEach".equals(order.getField(MODEL_FIELD_TYPE_OF_PROD_REC))) {
            for (Entity productionRecord : productionRecords) {
                getUsedProductFromRecord(productionRecord, true);
                getProducedProductFromRecord(productionRecord, true);
            }
        }
    }

    private void removeProductionRecordForOrder(final Entity order) {
        final List<Entity> countings = dataDefinitionService
                .get(BasicProductionCountingConstants.PLUGIN_IDENTIFIER,
                        BasicProductionCountingConstants.MODEL_BASIC_PRODUCTION_COUNTING).find()
                .add(SearchRestrictions.belongsTo(MODEL_FIELD_ORDER, order)).list().getEntities();
        for (Entity counting : countings) {
            counting.getDataDefinition().delete(counting.getId());
        }

    }

    private void getProducedProductFromRecord(final Entity productionRecord, final boolean forEach) {
        final SearchCriteriaBuilder searchBuilder = dataDefinitionService.get("productionCounting",
                "recordOperationProductOutComponent").find();
        final List<Entity> productsOut = searchBuilder.add(SearchRestrictions.belongsTo("productionRecord", productionRecord))
                .list().getEntities();

        if (productsOut.isEmpty()) {
            return;
        }
        for (Entity productOut : productsOut) {
            addProductQuantityToCounting(productOut, forEach, "producedQuantity");
        }
    }

    private void addProductQuantityToCounting(final Entity record, final boolean forEach, final String quantityType) {
        final SearchCriteriaBuilder searchBuilder = dataDefinitionService.get(BasicProductionCountingConstants.PLUGIN_IDENTIFIER,
                BasicProductionCountingConstants.MODEL_BASIC_PRODUCTION_COUNTING).find();
        final List<Entity> productionCountings = searchBuilder.add(SearchRestrictions.belongsTo(MODEL_FIELD_ORDER, order)).list()
                .getEntities();

        final Map<Entity, BigDecimal> products = materialRequrements.getQuantitiesForOrdersTechnologyProducts(
                Arrays.asList(order), false);

        final Entity productOut = order.getBelongsToField(MODEL_FIELD_PRODUCT);
        final BigDecimal neededQuantity = (BigDecimal) order.getField(MODEL_FIELD_PLANNED_QUANTITY);
        products.put(productOut, neededQuantity);

        Entity foundCounting = null;
        for (Entity productionCounting : productionCountings) {
            if (productionCounting.getBelongsToField(MODEL_FIELD_PRODUCT).equals(record.getBelongsToField(MODEL_FIELD_PRODUCT))) {
                foundCounting = productionCounting;
                break;
            }
        }
        if (foundCounting == null) {
            foundCounting = dataDefinitionService.get(BasicProductionCountingConstants.PLUGIN_IDENTIFIER,
                    BasicProductionCountingConstants.MODEL_BASIC_PRODUCTION_COUNTING).create();
            foundCounting.setField(MODEL_FIELD_ORDER, order);
            foundCounting.setField(MODEL_FIELD_PRODUCT, record.getBelongsToField(MODEL_FIELD_PRODUCT));
            foundCounting.setField(quantityType, record.getField("usedQuantity"));
        } else {
            BigDecimal quantity = (BigDecimal) record.getField("usedQuantity");
            if (forEach) {
                final BigDecimal addition = (BigDecimal) foundCounting.getField(quantityType);
                if (quantity == null) {
                    quantity = addition;
                } else if (addition != null) {
                    quantity = quantity.add(addition);
                }
            }
            foundCounting.setField(quantityType, quantity);
        }
        for (Entry<Entity, BigDecimal> product : products.entrySet()) {
            if (product.getKey().getId().equals(foundCounting.getBelongsToField(MODEL_FIELD_PRODUCT).getId())) {
                foundCounting.setField(MODEL_FIELD_PLANNED_QUANTITY, product.getValue());
            }
        }
        foundCounting = foundCounting.getDataDefinition().save(foundCounting);
        if (!foundCounting.isValid()) {
            throw new IllegalStateException("basicProductionCounting entity is invalid");
        }
    }

    private void getUsedProductFromRecord(final Entity productionRecord, final boolean forEach) {
        final SearchCriteriaBuilder searchBuilder = dataDefinitionService.get("productionCounting",
                "recordOperationProductInComponent").find();
        final List<Entity> productsIn = searchBuilder.add(SearchRestrictions.belongsTo("productionRecord", productionRecord))
                .list().getEntities();

        if (productsIn.isEmpty()) {
            return;
        }

        for (Entity productIn : productsIn) {
            addProductQuantityToCounting(productIn, forEach, "usedQuantity");
        }
    }

    public void generateUsedProducts(final ViewDefinitionState state) {
        final String orderNumber = (String) state.getComponentByReference(MODEL_FIELD_NUMBER).getFieldValue();

        if (orderNumber != null) {
            final Entity order = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).find()
                    .add(SearchRestrictions.eq(MODEL_FIELD_NUMBER, orderNumber)).setMaxResults(1).uniqueResult();
            if (order != null) {
                this.order = order;
                getProductionRecordsFromOrder(order);
            }
        }
    }

    public void setProducedQuantity(final ViewDefinitionState view) {
        final FieldComponent typeOfProductionRecording = (FieldComponent) view
                .getComponentByReference(MODEL_FIELD_TYPE_OF_PROD_REC);
        final FieldComponent doneQuantity = (FieldComponent) view.getComponentByReference("doneQuantity");
        final String orderNumber = (String) view.getComponentByReference(MODEL_FIELD_NUMBER).getFieldValue();
        Entity order;
        List<Entity> productionCountings;

        if ("".equals(typeOfProductionRecording.getFieldValue())) {
            return;
        }

        if (orderNumber == null) {
            return;
        }
        order = dataDefinitionService.get("orders", "order").find().add(SearchRestrictions.eq(MODEL_FIELD_NUMBER, orderNumber))
                .uniqueResult();
        if (order == null) {
            return;
        }
        productionCountings = dataDefinitionService
                .get(BasicProductionCountingConstants.PLUGIN_IDENTIFIER,
                        BasicProductionCountingConstants.MODEL_BASIC_PRODUCTION_COUNTING).find()
                .add(SearchRestrictions.belongsTo(MODEL_FIELD_ORDER, order)).list().getEntities();

        if (productionCountings.isEmpty()) {
            return;
        }

        final Entity technology = order.getBelongsToField("technology");

        for (Entity counting : productionCountings) {
            final Entity aProduct = (Entity) counting.getField(MODEL_FIELD_PRODUCT);
            final String type = technologyService.getProductType(aProduct, technology);
            if (type.equals(TechnologyService.PRODUCT)) {
                doneQuantity.setFieldValue(counting.getField("producedQuantity"));
                break;
            }
        }
    }

    public void getProductsFromOrder(final ViewDefinitionState view) {
        final String orderNumber = (String) view.getComponentByReference(MODEL_FIELD_NUMBER).getFieldValue();

        if (orderNumber == null) {
            return;
        }
        final Entity order = dataDefinitionService.get("orders", "order").find()
                .add(SearchRestrictions.eq(MODEL_FIELD_NUMBER, orderNumber)).uniqueResult();
        if (order == null) {
            return;
        }

        if (!"01basic".equals(order.getField(MODEL_FIELD_TYPE_OF_PROD_REC))) {
            return;
        }

        final Map<Entity, BigDecimal> requirement = materialRequrements.getQuantitiesForOrdersTechnologyProducts(
                Arrays.asList(order), false);

        final Entity productOut = order.getBelongsToField(MODEL_FIELD_PRODUCT);
        final BigDecimal neededQuantity = (BigDecimal) order.getField(MODEL_FIELD_PLANNED_QUANTITY);
        requirement.put(productOut, neededQuantity);

        List<Entity> producedProducts = dataDefinitionService
                .get(BasicProductionCountingConstants.PLUGIN_IDENTIFIER,
                        BasicProductionCountingConstants.MODEL_BASIC_PRODUCTION_COUNTING).find()
                .add(SearchRestrictions.belongsTo(MODEL_FIELD_ORDER, order)).list().getEntities();

        for (Entry<Entity, BigDecimal> product : requirement.entrySet()) {
            Entity foundProduct = null;

            for (Entity producedProduct : producedProducts) {
                if (producedProduct.getBelongsToField(MODEL_FIELD_PRODUCT).getId().equals(product.getKey().getId())) {
                    foundProduct = producedProduct;
                    break;
                }
            }

            if (foundProduct == null) {
                final Entity newProduct = dataDefinitionService.get(BasicProductionCountingConstants.PLUGIN_IDENTIFIER,
                        BasicProductionCountingConstants.MODEL_BASIC_PRODUCTION_COUNTING).create();
                newProduct.setField(MODEL_FIELD_ORDER, order);
                newProduct.setField(MODEL_FIELD_PRODUCT, product.getKey());
                newProduct.setField(MODEL_FIELD_PLANNED_QUANTITY, product.getValue());
                newProduct.getDataDefinition().save(newProduct);
                if (!newProduct.isValid()) {
                    throw new IllegalStateException("new product entity is invalid  " + product.getValue() + "\n\n\n");
                }
            } else {
                final BigDecimal plannedQuantity = (BigDecimal) foundProduct.getField(MODEL_FIELD_PLANNED_QUANTITY);
                if (plannedQuantity != product.getValue()) {
                    foundProduct.setField(MODEL_FIELD_PLANNED_QUANTITY, product.getValue());
                    foundProduct.getDataDefinition().save(foundProduct);
                }
            }
        }
        producedProducts = dataDefinitionService
                .get(BasicProductionCountingConstants.PLUGIN_IDENTIFIER,
                        BasicProductionCountingConstants.MODEL_BASIC_PRODUCTION_COUNTING).find()
                .add(SearchRestrictions.belongsTo(MODEL_FIELD_ORDER, order)).list().getEntities();

        for (Entity producedProduct : producedProducts) {
            boolean found = false;
            for (Entry<Entity, BigDecimal> product : requirement.entrySet()) {
                if (producedProduct.getBelongsToField(MODEL_FIELD_PRODUCT).equals(product.getKey())) {
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
