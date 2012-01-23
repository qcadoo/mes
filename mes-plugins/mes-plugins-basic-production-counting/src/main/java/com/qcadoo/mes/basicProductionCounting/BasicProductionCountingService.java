/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.2
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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.basicProductionCounting.constants.BasicProductionCountingConstants;
import com.qcadoo.mes.materialRequirements.api.MaterialRequirementReportDataService;
import com.qcadoo.mes.orders.constants.OrderStates;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.technologies.TechnologyService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ComponentState.MessageType;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.ribbon.RibbonActionItem;

@Service
public class BasicProductionCountingService {

    private static final String COMPONENT = "01component";

    private static final String INTERMEDIATE = "02intermediate";

    private static final String FINAL_PRODUCT = "03finalProduct";

    private static final String MODEL_FIELD_PRODUCT = "product";

    private static final String MODEL_FIELD_ORDER = "order";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private MaterialRequirementReportDataService materialRequirementReportDataService;

    @Autowired
    private TechnologyService technologyService;

    @Autowired
    private TranslationService translationService;

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
                .add(SearchRestrictions.belongsTo(MODEL_FIELD_ORDER, order)).list().getEntities();

        for (Entry<Entity, BigDecimal> product : products.entrySet()) {
            Entity foundProduct = null;

            for (Entity producedProduct : producedProducts) {
                if (producedProduct.getBelongsToField(MODEL_FIELD_PRODUCT).getId().equals(product.getKey().getId())) {
                    foundProduct = producedProduct;
                    break;
                }
            }

            if (foundProduct == null) {
                final Entity newProduct = dataDefinitionService.get(pluginName, modelName).create();
                newProduct.setField(MODEL_FIELD_ORDER, order);
                newProduct.setField(MODEL_FIELD_PRODUCT, product.getKey());
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
                .add(SearchRestrictions.belongsTo(MODEL_FIELD_ORDER, order)).list().getEntities();

        for (Entity producedProduct : producedProducts) {
            boolean found = false;
            for (Entry<Entity, BigDecimal> product : products.entrySet()) {
                if (producedProduct.getBelongsToField(MODEL_FIELD_PRODUCT).equals(product.getKey())) {
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
        FieldComponent productField = (FieldComponent) view.getComponentByReference(MODEL_FIELD_PRODUCT);
        FormComponent formComponent = (FormComponent) view.getComponentByReference("form");
        if (formComponent.getEntityId() == null) {
            return;
        }
        Entity basicProductionCountingEntity = dataDefinitionService.get(BasicProductionCountingConstants.PLUGIN_IDENTIFIER,
                BasicProductionCountingConstants.MODEL_BASIC_PRODUCTION_COUNTING).get(formComponent.getEntityId());
        if (basicProductionCountingEntity == null) {
            return;
        }
        Entity product = basicProductionCountingEntity.getBelongsToField(MODEL_FIELD_PRODUCT);
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
        FormComponent order = (FormComponent) view.getComponentByReference(MODEL_FIELD_ORDER);
        if (order.getEntityId() == null) {
            return;
        }
        Entity orderFromDB = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).get(
                order.getEntityId());
        if (orderFromDB == null) {
            return;
        }
        String orderState = orderFromDB.getStringField("state");
        String productionRecordType = orderFromDB.getStringField("typeOfProductionRecording");
        GridComponent grid = (GridComponent) view.getComponentByReference("grid");

        if (("01basic".equals(productionRecordType)) && ("03inProgress".equals(orderState))) {
            grid.setEditable(true);
        } else {
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
        Entity product = basicProductionCountingEntity.getBelongsToField(MODEL_FIELD_PRODUCT);
        if (product == null) {
            return;
        }
        for (String reference : Arrays.asList("usedQuantityCurrency", "producedQuantityCurrency", "plannedQuantityCurrency")) {
            FieldComponent field = (FieldComponent) view.getComponentByReference(reference);
            field.setFieldValue(product.getField("unit"));
            field.requestComponentUpdateState();
        }
    }

    public void setGridContent(final ViewDefinitionState view) {
        GridComponent grid = (GridComponent) view.getComponentByReference("grid");
        FormComponent orderForm = (FormComponent) view.getComponentByReference("order");

        Long orderId = orderForm.getEntityId();
        if (orderId != null) {
            Entity savedOrder = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).get(
                    orderId);

            SearchCriteriaBuilder criteriaBuilder = dataDefinitionService.get(BasicProductionCountingConstants.PLUGIN_IDENTIFIER,
                    BasicProductionCountingConstants.MODEL_BASIC_PRODUCTION_COUNTING).find();
            List<Entity> countings = criteriaBuilder.add(SearchRestrictions.belongsTo("order", savedOrder)).list().getEntities();

            Collections.sort(countings, countingsComparator);
            Collections.reverse(countings);
            grid.setEntities(countings);
        }
    }

    public void shouldDisableUsedProducedField(final ViewDefinitionState view) {
        FormComponent form = (FormComponent) view.getComponentByReference("form");
        FieldComponent producedField = (FieldComponent) view.getComponentByReference("producedQuantity");
        FieldComponent usedField = (FieldComponent) view.getComponentByReference("usedQuantity");

        if (form.getEntityId() != null) {
            Entity counting = dataDefinitionService.get(BasicProductionCountingConstants.PLUGIN_IDENTIFIER,
                    BasicProductionCountingConstants.MODEL_BASIC_PRODUCTION_COUNTING).get(form.getEntityId());
            Entity order = counting.getBelongsToField(MODEL_FIELD_ORDER);
            Entity technology = order.getBelongsToField("technology");
            Entity product = counting.getBelongsToField(MODEL_FIELD_PRODUCT);

            if (FINAL_PRODUCT.equals(technologyService.getProductType(product, technology))) {
                usedField.setEnabled(false);
            } else {
                usedField.setEnabled(true);

            }
            if (COMPONENT.equals(technologyService.getProductType(product, technology))) {
                producedField.setEnabled(false);
            } else {
                producedField.setEnabled(true);

            }
        }

    }

    private Comparator<Entity> countingsComparator = new Comparator<Entity>() {

        @Override
        public int compare(final Entity counting1, final Entity counting2) {
            final Entity product1 = counting1.getBelongsToField(MODEL_FIELD_PRODUCT);
            final Entity product2 = counting2.getBelongsToField(MODEL_FIELD_PRODUCT);
            final Entity technology = counting1.getBelongsToField(MODEL_FIELD_ORDER).getBelongsToField("technology");
            final String product1Type = technologyService.getProductType(product1, technology);
            final String product2Type = technologyService.getProductType(product2, technology);

            if (product1Type.equals(product2Type)) {
                return 0;
            }
            if (FINAL_PRODUCT.equals(product1Type)) {
                return 1;
            }
            if (FINAL_PRODUCT.equals(product2Type)) {
                return -1;
            }
            if (INTERMEDIATE.equals(product1Type)) {
                return 1;
            }
            if (INTERMEDIATE.equals(product2Type)) {
                return -1;
            }
            if (COMPONENT.equals(product1Type)) {
                return 1;
            }
            if (COMPONENT.equals(product2Type)) {
                return -1;
            }

            throw new IllegalStateException("Cant compare two product types");
        }
    };

    public void fillDoneQuantityField(final ViewDefinitionState viewState, final ComponentState triggerState, final String[] args) {
        final FormComponent form = (FormComponent) viewState.getComponentByReference("form");
        final FieldComponent producedQuantity = (FieldComponent) viewState.getComponentByReference("producedQuantity");
        final Long countingId = form.getEntityId();
        if (countingId != null) {
            final Entity counting = dataDefinitionService.get(BasicProductionCountingConstants.PLUGIN_IDENTIFIER,
                    BasicProductionCountingConstants.MODEL_BASIC_PRODUCTION_COUNTING).get(countingId);
            Entity order = counting.getBelongsToField(MODEL_FIELD_ORDER);
            final Entity product = counting.getBelongsToField(MODEL_FIELD_PRODUCT);

            if (order.getBelongsToField(MODEL_FIELD_PRODUCT).getId().equals(product.getId())) {
                final String fieldValue = (String) producedQuantity.getFieldValue();
                if (fieldValue == null || fieldValue.isEmpty()) {
                    return;
                }
                try {
                    final BigDecimal doneQuantity = new BigDecimal(fieldValue.replace(",", ".").replace(" ", "")
                            .replace("\u00A0", ""));
                    order.setField("doneQuantity", doneQuantity);
                    order = order.getDataDefinition().save(order);
                } catch (NumberFormatException ex) {
                    return;
                }
                if (!order.isValid()) {
                    producedQuantity.addMessage(
                            translationService.translate(order.getError("doneQuantity").getMessage(), viewState.getLocale()),
                            MessageType.FAILURE);
                }
            }
        }
    }

}
