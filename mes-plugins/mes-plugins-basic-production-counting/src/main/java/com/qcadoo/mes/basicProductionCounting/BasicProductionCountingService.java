/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.6
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

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basicProductionCounting.constants.BasicProductionCountingConstants;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.orders.states.constants.OrderState;
import com.qcadoo.mes.technologies.TechnologyService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.ribbon.RibbonActionItem;

@Service
public class BasicProductionCountingService {

    private static final String PRODUCED_QUANTITY_L = "producedQuantity";

    private static final String FORM_L = "form";

    private static final String COMPONENT = "01component";

    private static final String FINAL_PRODUCT = "03finalProduct";

    private static final String MODEL_FIELD_PRODUCT = "product";

    private static final String MODEL_FIELD_ORDER = "order";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private TechnologyService technologyService;

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
        FormComponent formComponent = (FormComponent) view.getComponentByReference(FORM_L);
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
        FormComponent form = (FormComponent) view.getComponentByReference(FORM_L);
        if (form.getEntity() == null) {
            return;
        }
        WindowComponent window = (WindowComponent) view.getComponentByReference("window");
        RibbonActionItem productionCounting = window.getRibbon()
                .getGroupByName(BasicProductionCountingConstants.VIEW_RIBBON_ACTION_ITEM_GROUP)
                .getItemByName(BasicProductionCountingConstants.VIEW_RIBBON_ACTION_ITEM_NAME);
        Entity order = form.getEntity();
        String state = order.getStringField("state");
        if (OrderState.DECLINED.getStringValue().equals(state) || OrderState.PENDING.getStringValue().equals(state)) {
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
        FormComponent form = (FormComponent) view.getComponentByReference(FORM_L);
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

    public void shouldDisableUsedProducedField(final ViewDefinitionState view) {
        FormComponent form = (FormComponent) view.getComponentByReference(FORM_L);
        FieldComponent producedField = (FieldComponent) view.getComponentByReference(PRODUCED_QUANTITY_L);
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

    public void fillDoneQuantityField(final ViewDefinitionState viewState, final ComponentState triggerState, final String[] args) {
        final FormComponent form = (FormComponent) viewState.getComponentByReference(FORM_L);
        final FieldComponent producedQuantity = (FieldComponent) viewState.getComponentByReference(PRODUCED_QUANTITY_L);
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
                    producedQuantity.addMessage(order.getError("doneQuantity"));
                }
            }
        }
    }

    public boolean checkValueOfQuantity(final DataDefinition dataDefinition, final Entity entity) {
        BigDecimal usedQuantity = (BigDecimal) entity.getField("usedQuantity");
        BigDecimal producedQuantity = (BigDecimal) entity.getField(PRODUCED_QUANTITY_L);
        if (usedQuantity == null && producedQuantity == null) {
            return true;
        }
        if (usedQuantity != null && usedQuantity.compareTo(BigDecimal.ZERO) == -1) {
            entity.addError(dataDefinition.getField("usedQuantity"), "basic.production.counting.value.lower.zero");
        }
        if (producedQuantity != null && producedQuantity.compareTo(BigDecimal.ZERO) == -1) {
            entity.addError(dataDefinition.getField(PRODUCED_QUANTITY_L), "basic.production.counting.value.lower.zero");
        }
        if (!entity.getGlobalErrors().isEmpty() || !entity.getErrors().isEmpty()) {
            return false;
        }
        return true;
    }
}
