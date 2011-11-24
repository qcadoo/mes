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

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basicProductionCounting.constants.BasicProductionCountingConstants;
import com.qcadoo.mes.materialRequirements.api.MaterialRequirementReportDataService;
import com.qcadoo.mes.orders.constants.OrderStates;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
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

    private final String modelFieldProduct = "product";

    private final String modelFieldOrder = "order";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private MaterialRequirementReportDataService materialRequirementReportDataService;

    public void generateProducedProducts(final ViewDefinitionState state) {
        final String orderNumber = (String) state.getComponentByReference("number").getFieldValue();

        if (orderNumber != null) {
            Entity order = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).find()
                    .add(SearchRestrictions.eq("number", orderNumber)).uniqueResult();
            if (order != null) {
                updateProductsForOrder(order, BasicProductionCountingConstants.PLUGIN_IDENTIFIER,
                        BasicProductionCountingConstants.MODEL_BASIC_PRODUCTION_COUNTING, "plannedQuantity");
            }
        }
    }

    private void updateProductsForOrder(Entity order, String pluginName, String modelName, String fieldName) {
        final List<Entity> orderList = Arrays.asList(order);

        final Map<Entity, BigDecimal> products = materialRequirementReportDataService.getQuantitiesForOrdersTechnologyProducts(
                orderList, true);

        List<Entity> producedProducts = dataDefinitionService.get(pluginName, modelName).find()
                .add(SearchRestrictions.belongsTo(modelFieldOrder, order)).list().getEntities();

        for (Entry<Entity, BigDecimal> product : products.entrySet()) {
            Entity foundProduct = null;

            for (Entity producedProduct : producedProducts) {
                if (producedProduct.getBelongsToField(modelFieldProduct).getId().equals(product.getKey().getId())) {
                    foundProduct = producedProduct;
                    break;
                }
            }

            if (foundProduct == null) {
                final Entity newProduct = dataDefinitionService.get(pluginName, modelName).create();
                newProduct.setField(modelFieldOrder, order);
                newProduct.setField(modelFieldProduct, product.getKey());
                newProduct.setField(fieldName, product.getValue());
                newProduct.getDataDefinition().save(newProduct);
                if (!newProduct.isValid()) {
                    throw new IllegalStateException("new product entity is invalid  " + product.getValue() + "\n\n\n");
                }
            } else {
                BigDecimal plannedQuantity = (BigDecimal) foundProduct.getField(fieldName);
                if (plannedQuantity != product.getValue()) {
                    foundProduct.setField(fieldName, product.getValue());
                    foundProduct.getDataDefinition().save(foundProduct);
                }
            }
        }
        producedProducts = dataDefinitionService.get(pluginName, modelName).find()
                .add(SearchRestrictions.belongsTo(modelFieldOrder, order)).list().getEntities();

        for (Entity producedProduct : producedProducts) {
            boolean found = false;
            for (Entry<Entity, BigDecimal> product : products.entrySet()) {
                if (producedProduct.getBelongsToField(modelFieldProduct).equals(product.getKey())) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                dataDefinitionService.get(pluginName, modelName).delete(producedProduct.getId());
            }
        }
    }

    public void showProductionCounting(final ViewDefinitionState viewState, final ComponentState triggerState, final String[] args) {
        Long orderId = (Long) triggerState.getFieldValue();

        if (orderId == null) {
            return;
        }

        JSONObject json = new JSONObject();
        try {
            json.put("order.id", orderId);
        } catch (JSONException e) {
            throw new IllegalStateException(e);
        }

        String url = "../page/basicProductionCounting/basicProductionCountingList.html?context=" + json.toString();
        viewState.redirectTo(url, false, true);
    }

    public void getProductNameFromCounting(final ViewDefinitionState view) {
        FieldComponent productField = (FieldComponent) view.getComponentByReference(modelFieldProduct);
        FormComponent formComponent = (FormComponent) view.getComponentByReference("form");
        if (formComponent.getEntityId() == null) {
            return;
        }
        Entity basicProductionCountingEntity = dataDefinitionService.get(BasicProductionCountingConstants.PLUGIN_IDENTIFIER,
                BasicProductionCountingConstants.MODEL_BASIC_PRODUCTION_COUNTING).get(formComponent.getEntityId());
        if (basicProductionCountingEntity == null) {
            return;
        }
        Entity product = basicProductionCountingEntity.getBelongsToField(modelFieldProduct);
        productField.setFieldValue(product.getField("name"));
        productField.requestComponentUpdateState();
    }

    public void disabledButtonForAppropriateState(final ViewDefinitionState view) {
        FormComponent form = (FormComponent) view.getComponentByReference("form");
        if (form.getEntity() == null) {
            return;
        }
        WindowComponent window = (WindowComponent) view.getComponentByReference("window");
        RibbonActionItem productionCounting = window.getRibbon()
                .getGroupByName(BasicProductionCountingConstants.VIEW_RIBBON_ACTION_ITEM_GROUP)
                .getItemByName(BasicProductionCountingConstants.VIEW_RIBBON_ACTION_ITEM_NAME);
        Entity order = form.getEntity();
        String state = order.getStringField("state");
        if (OrderStates.DECLINED.getStringValue().equals(state) || OrderStates.PENDING.getStringValue().equals(state)) {
            productionCounting.setEnabled(false);
            productionCounting.requestUpdate(true);
        }
    }

    public void setUneditableGridWhenOrderTypeRecordingIsBasic(final ViewDefinitionState view) {
        FormComponent order = (FormComponent) view.getComponentByReference(modelFieldOrder);
        if (order.getEntityId() == null) {
            return;
        }
        Entity orderFromDB = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).get(
                order.getEntityId());
        if (orderFromDB == null) {
            return;
        }
        String orderState = orderFromDB.getStringField("typeOfProductionRecording");
        if (!("01basic".equals(orderState))) {
            GridComponent grid = (GridComponent) view.getComponentByReference("grid");
            grid.setEditable(false);
        }
    }

    public void fillFieldsCurrency(final ViewDefinitionState view) {
        FormComponent form = (FormComponent) view.getComponentByReference("form");
        if (form.getEntity() == null) {
            return;
        }
        Entity basicProductionCountingEntity = dataDefinitionService.get(BasicProductionCountingConstants.PLUGIN_IDENTIFIER,
                BasicProductionCountingConstants.MODEL_BASIC_PRODUCTION_COUNTING).get(form.getEntityId());
        Entity product = basicProductionCountingEntity.getBelongsToField(modelFieldProduct);
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
