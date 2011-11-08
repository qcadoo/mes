/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.4.8
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

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basicProductionCounting.constants.BasicProductionCountingConstants;
import com.qcadoo.mes.materialRequirements.api.MaterialRequirementReportDataService;
import com.qcadoo.mes.orders.constants.OrderStates;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.ribbon.RibbonActionItem;

@Service
public class BasicProductionCountingService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private MaterialRequirementReportDataService materialRequirementReportDataService;

    public void generateProducedProducts(final ViewDefinitionState state) {
        String orderNumber = (String) state.getComponentByReference("number").getFieldValue();

        if (orderNumber != null) {
            Entity order = dataDefinitionService.get("orders", "order").find().add(SearchRestrictions.eq("number", orderNumber))
                    .uniqueResult();
            if (order != null)
                updateProductsForOrder(order, "basicProductionCounting", "basicProductionCounting", "plannedQuantity");
        }
    }

    private void updateProductsForOrder(Entity order, String pluginName, String modelName, String fieldName) {
        List<Entity> orderList = Arrays.asList(order);

        Map<Entity, BigDecimal> products = materialRequirementReportDataService.getQuantitiesForOrdersTechnologyProducts(
                orderList, true);

        List<Entity> producedProducts = dataDefinitionService.get(pluginName, modelName).find()
                .add(SearchRestrictions.belongsTo("order", order)).list().getEntities();

        for (Entry<Entity, BigDecimal> product : products.entrySet()) {
            Entity foundProduct = null;

            for (Entity producedProduct : producedProducts) {
                if (producedProduct.getBelongsToField("product").getId().equals(product.getKey().getId())) {
                    foundProduct = producedProduct;
                    break;
                }
            }

            if (foundProduct == null) {
                Entity newProduct = dataDefinitionService.get(pluginName, modelName).create();
                newProduct.setField("order", order);
                newProduct.setField("product", product.getKey());
                newProduct.setField(fieldName, product.getValue());
                newProduct.getDataDefinition().save(newProduct);
                if (!newProduct.isValid())
                    throw new IllegalStateException("new product entity is invalid  " + product.getValue() + "\n\n\n");
            } else {
                BigDecimal plannedQuantity = (BigDecimal) foundProduct.getField(fieldName);
                // if (plannedQuantity.compareTo(product.getValue()) != 0) {
                if (plannedQuantity != product.getValue()) {
                    foundProduct.setField(fieldName, product.getValue());
                    foundProduct.getDataDefinition().save(foundProduct);
                }
            }
        }
        producedProducts = dataDefinitionService.get(pluginName, modelName).find()
                .add(SearchRestrictions.belongsTo("order", order)).list().getEntities();

        for (Entity producedProduct : producedProducts) {
            boolean found = false;
            for (Entry<Entity, BigDecimal> product : products.entrySet()) {
                if (producedProduct.getBelongsToField("product").equals(product.getKey())) {
                    found = true;
                    break;
                }
            }
            if (!found)
                dataDefinitionService.get(pluginName, modelName).delete(producedProduct.getId());
        }
    }

    public void showProductionCounting(final ViewDefinitionState viewDefinitionState, final ComponentState triggerState,
            final String[] args) {
        Long orderId = (Long) triggerState.getFieldValue();

        if (orderId == null) {
            return;
        }

        JSONObject json = new JSONObject();
        try {
            json.put("order.id", orderId);
        } catch (JSONException e) {
            e.printStackTrace(); // TODO : BAKU fix it to new IllegalStateException();
        }

        String url = "../page/basicProductionCounting/basicProductionCountingList.html?context=" + json.toString();
        viewDefinitionState.redirectTo(url, false, true);
    }

    public void getProductNameFromCounting(final ViewDefinitionState view) {
        FieldComponent productField = (FieldComponent) view.getComponentByReference("product");
        FormComponent formComponent = (FormComponent) view.getComponentByReference("form");
        Entity basicProductionCountingEntity = formComponent.getEntity();
        basicProductionCountingEntity = basicProductionCountingEntity.getDataDefinition().get(
                basicProductionCountingEntity.getId());
        Entity product = basicProductionCountingEntity.getBelongsToField("product");
        productField.setFieldValue(product.getField("name"));
        productField.requestComponentUpdateState();
    }

    public void disabledButtonForAppropriateState(final ViewDefinitionState view) {
        FormComponent form = (FormComponent) view.getComponentByReference("form");
        if (form.getEntity() == null) {
            return;
        }
        WindowComponent window = (WindowComponent) view.getComponentByReference("window");
        RibbonActionItem productionCounting = window.getRibbon().getGroupByName("basicProductionCounting")
                .getItemByName("productionCounting");
        Entity order = form.getEntity();
        String state = order.getStringField("state");
        if (OrderStates.DECLINED.getStringValue().equals(state) || OrderStates.PENDING.getStringValue().equals(state)) {
            productionCounting.setEnabled(false);
            productionCounting.requestUpdate(true);
        }
    }

    public void sortProductsHook(ViewDefinitionState viewState) {
        GridComponent grid = (GridComponent) viewState.getComponentByReference("grid");
        SearchCriteriaBuilder searchBuilder = dataDefinitionService.get(BasicProductionCountingConstants.PLUGIN_IDENTIFIER,
                BasicProductionCountingConstants.MODEL_BASIC_PRODUCTION_COUNTING).find();

        List<Entity> products = searchBuilder.add(SearchRestrictions.eq("typeOfMaterial", "03product")).list().getEntities();
        List<Entity> intermediates = searchBuilder.add(SearchRestrictions.eq("typeOfMaterial", "02intermediate")).list()
                .getEntities();
        List<Entity> components = searchBuilder.add(SearchRestrictions.eq("typeOfMaterial", "01component")).list().getEntities();
        List<Entity> gridContent = grid.getEntities();

        if (!gridContent.isEmpty()) {
            gridContent.clear();
        }
        for (Entity product : products) {
            gridContent.add(product);
        }
        for (Entity intermediate : intermediates) {
            gridContent.add(intermediate);
        }
        for (Entity component : components) {
            gridContent.add(component);
        }
        grid.setEntities(gridContent);

    }

    public void fillFieldsCurrency(final ViewDefinitionState view) {
        FormComponent form = (FormComponent) view.getComponentByReference("form");
        if (form.getEntity() == null) {
            return;
        }
        Entity basicProductionCountingEntity = dataDefinitionService.get(BasicProductionCountingConstants.PLUGIN_IDENTIFIER,
                BasicProductionCountingConstants.MODEL_BASIC_PRODUCTION_COUNTING).get(form.getEntityId());
        Entity product = basicProductionCountingEntity.getBelongsToField("product");
        if (product == null) {
            return;
        }
        for (String reference : Arrays.asList("usedQuantityCurrency", "producedQuantityCurrency", "plannedQuantityCurrency")) {
            FieldComponent field = (FieldComponent) view.getComponentByReference(reference);
            field.setFieldValue(product.getField("unit"));
            field.requestComponentUpdateState();
        }
    }
}
