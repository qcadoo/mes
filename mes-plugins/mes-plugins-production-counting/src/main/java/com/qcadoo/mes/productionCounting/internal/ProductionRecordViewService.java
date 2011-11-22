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
package com.qcadoo.mes.productionCounting.internal;

import static com.qcadoo.mes.basic.constants.BasicConstants.MODEL_PARAMETER;
import static com.qcadoo.mes.orders.constants.OrdersConstants.MODEL_ORDER;
import static com.qcadoo.mes.productionCounting.internal.ProductionRecordService.getBooleanValue;
import static com.qcadoo.mes.productionCounting.internal.constants.ProductionCountingConstants.PARAM_RECORDING_TYPE_BASIC;
import static com.qcadoo.mes.productionCounting.internal.constants.ProductionCountingConstants.PARAM_RECORDING_TYPE_CUMULATED;
import static com.qcadoo.mes.productionCounting.internal.constants.ProductionCountingConstants.PARAM_RECORDING_TYPE_FOREACH;
import static com.qcadoo.mes.productionCounting.internal.constants.ProductionCountingConstants.PARAM_RECORDING_TYPE_NONE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.productionCounting.internal.states.ProductionCountingStates;
import com.qcadoo.mes.technologies.TechnologyService;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.api.validators.ErrorMessage;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ComponentState.MessageType;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.ribbon.RibbonActionItem;

@Service
public class ProductionRecordViewService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private TranslationService translationService;

    @Autowired
    private TechnologyService technologyService;

    private static final String CLOSED_ORDER = "04completed";

    private static final Logger LOG = LoggerFactory.getLogger(ProductionRecordViewService.class);

    public void initializeRecordDetailsView(final ViewDefinitionState view) {
        FormComponent recordForm = (FormComponent) view.getComponentByReference("form");
        if (recordForm.getEntityId() == null) {
            return;
        }

        Entity order = getOrderFromLookup(view);
        String typeOfProductionRecording = order.getStringField("typeOfProductionRecording");

        view.getComponentByReference("laborTime").setVisible(getBooleanValue(order.getField("registerProductionTime")));
        view.getComponentByReference("machineTime").setVisible(getBooleanValue(order.getField("registerProductionTime")));

        view.getComponentByReference("orderOperationComponent").setVisible(
                PARAM_RECORDING_TYPE_FOREACH.equals(typeOfProductionRecording));
        view.getComponentByReference("borderLayoutCumulated").setVisible(
                PARAM_RECORDING_TYPE_CUMULATED.equals(typeOfProductionRecording)
                        && getBooleanValue(order.getField("registerProductionTime")));
        view.getComponentByReference("borderLayoutForEach").setVisible(
                PARAM_RECORDING_TYPE_FOREACH.equals(typeOfProductionRecording)
                        && getBooleanValue(order.getField("registerProductionTime")));
        view.getComponentByReference("borderLayoutNone").setVisible(
                getBooleanValue(!PARAM_RECORDING_TYPE_CUMULATED.equals(typeOfProductionRecording)
                        && !PARAM_RECORDING_TYPE_FOREACH.equals(typeOfProductionRecording)));
        view.getComponentByReference("recordOperationProductOutComponent").setVisible(
                getBooleanValue(order.getField("registerQuantityOutProduct")));
        view.getComponentByReference("recordOperationProductInComponent").setVisible(
                getBooleanValue(order.getField("registerQuantityInProduct")));
    }

    public void setParametersDefaultValue(final ViewDefinitionState viewDefinitionState) {
        FormComponent form = (FormComponent) viewDefinitionState.getComponentByReference("form");
        Entity parameter = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, MODEL_PARAMETER).get(form.getEntityId());

        for (String componentReference : Arrays.asList("registerQuantityInProduct", "registerQuantityOutProduct",
                "registerProductionTime")) {
            FieldComponent component = (FieldComponent) viewDefinitionState.getComponentByReference(componentReference);
            if (parameter == null || parameter.getField(componentReference) == null) {
                component.setFieldValue(true);
                component.requestComponentUpdateState();
            }
        }
    }

    public void clearFields(final ViewDefinitionState view, final ComponentState componentState, final String[] args) {
        FieldComponent operation = (FieldComponent) view.getComponentByReference("orderOperationComponent");
        operation.setFieldValue("");
        FormComponent form = (FormComponent) view.getComponentByReference("form");
        if (form.getEntityId() == null) {
            return;
        }
        GridComponent productsIn = (GridComponent) view.getComponentByReference("recordOperationProductInComponent");
        GridComponent productOut = (GridComponent) view.getComponentByReference("recordOperationProductOutComponent");

        productOut.setEntities(new ArrayList<Entity>());
        productsIn.setEntities(new ArrayList<Entity>());
    }

    public void enabledOrDisabledOperationField(final ViewDefinitionState view, final ComponentState componentState,
            final String[] args) {
        Entity order = getOrderFromLookup(view);
        if (order == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("order is null");
            }
            return;
        }

        setComponentVisible((String) order.getField("typeOfProductionRecording"), view);
    }

    private void setComponentVisible(final String recordingType, final ViewDefinitionState view) {
        view.getComponentByReference("orderOperationComponent").setVisible(PARAM_RECORDING_TYPE_FOREACH.equals(recordingType));
        ((FieldComponent) view.getComponentByReference("orderOperationComponent")).requestComponentUpdateState();

        if (PARAM_RECORDING_TYPE_CUMULATED.equals(recordingType)) {
            view.getComponentByReference("borderLayoutCumulated").setVisible(true);
            view.getComponentByReference("borderLayoutForEach").setVisible(false);
            view.getComponentByReference("borderLayoutNone").setVisible(false);
            view.getComponentByReference("machineTime").setVisible(true);
            view.getComponentByReference("laborTime").setVisible(true);
        }
        if (PARAM_RECORDING_TYPE_FOREACH.equals(recordingType)) {
            view.getComponentByReference("borderLayoutCumulated").setVisible(false);
            view.getComponentByReference("borderLayoutForEach").setVisible(true);
            view.getComponentByReference("borderLayoutNone").setVisible(false);
            view.getComponentByReference("machineTime").setVisible(true);
            view.getComponentByReference("laborTime").setVisible(true);
        }
        if (!PARAM_RECORDING_TYPE_CUMULATED.equals(recordingType) && !PARAM_RECORDING_TYPE_FOREACH.equals(recordingType)
                && !PARAM_RECORDING_TYPE_BASIC.equals(recordingType)) {
            ((FieldComponent) view.getComponentByReference("order")).addMessage(
                    translationService.translate("productionRecord.productionRecord.report.error.orderWithoutRecordingType",
                            view.getLocale()), ComponentState.MessageType.FAILURE);
            view.getComponentByReference("borderLayoutCumulated").setVisible(false);
            view.getComponentByReference("borderLayoutForEach").setVisible(false);
            view.getComponentByReference("machineTime").setVisible(false);
            view.getComponentByReference("laborTime").setVisible(false);
            view.getComponentByReference("borderLayoutNone").setVisible(true);
        }

    }

    public void registeringProductionTime(final ViewDefinitionState view, final ComponentState componentState, final String[] args) {
        Entity order = getOrderFromLookup(view);
        if (order == null) {
            return;
        }
        Boolean registerProductionTime = getBooleanValue(order.getField("registerProductionTime"));
        if (registerProductionTime && order.getStringField("typeOfProductionRecording") != null
                && !("01none".equals(order.getStringField("typeOfProductionRecording")))) {
            view.getComponentByReference("machineTime").setVisible(true);
            view.getComponentByReference("laborTime").setVisible(true);

        } else {
            view.getComponentByReference("machineTime").setVisible(false);
            view.getComponentByReference("laborTime").setVisible(false);
            view.getComponentByReference("borderLayoutCumulated").setVisible(false);
            view.getComponentByReference("borderLayoutForEach").setVisible(false);
            view.getComponentByReference("borderLayoutNone").setVisible(false);
        }
    }

    public void closeOrder(final ViewDefinitionState view, final ComponentState componentState, final String[] args) {
        FormComponent form = (FormComponent) view.getComponentByReference("form");
        Entity order = getOrderFromLookup(view);
        Boolean autoCloseOrder = getBooleanValue(order.getField("autoCloseOrder"));
        String orderState = order.getStringField("state");
        if (autoCloseOrder
                && "1".equals(view.getComponentByReference("lastRecord").getFieldValue())
                && view.getComponentByReference("state").getFieldValue()
                        .equals(ProductionCountingStates.ACCEPTED.getStringValue()) && "03inProgress".equals(orderState)) {
            order.setField("state", CLOSED_ORDER);
            dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).save(order);
            Entity orderFromDB = order.getDataDefinition().get(order.getId());
            if (!orderFromDB.getStringField("state").equals(CLOSED_ORDER)) {
                form.addMessage(translationService.translate("productionCounting.order.orderCannotBeClosed", view.getLocale()),
                        MessageType.INFO, false);
                for (ErrorMessage message : order.getErrors().values()) {
                    StringBuilder error = new StringBuilder();
                    error = error.append(translationService.translate("orders.order.orderStates.error", form.getLocale()));
                    error = error.append(" ");
                    error = error.append(message.getMessage());
                    form.addMessage(error.toString(), MessageType.FAILURE, false);
                }
                for (ErrorMessage message : order.getGlobalErrors()) {
                    StringBuilder error = new StringBuilder();
                    error = error.append(translationService.translate("orders.order.orderStates.error", form.getLocale()));
                    error = error.append(" ");
                    error = error.append(message.getMessage());
                    form.addMessage(error.toString(), MessageType.FAILURE, false);
                }
            } else {
                form.addMessage(translationService.translate("productionCounting.order.orderClosed", view.getLocale()),
                        MessageType.INFO, false);
            }
        }
        if ("01basic".equals(order.getField("typeOfProductionRecording"))) {
            form.addMessage(translationService.translate(
                    "productionRecord.productionRecord.report.error.orderWithBasicProductionCounting", view.getLocale()),
                    ComponentState.MessageType.FAILURE);
        }

    }

    public void fillFieldFromProduct(final ViewDefinitionState view) {
        Entity recordProduct = ((FormComponent) view.getComponentByReference("form")).getEntity();
        recordProduct = recordProduct.getDataDefinition().get(recordProduct.getId());
        Entity product = recordProduct.getBelongsToField("product");

        view.getComponentByReference("number").setFieldValue(product.getField("number"));
        view.getComponentByReference("name").setFieldValue(product.getField("name"));

        view.getComponentByReference("usedQuantityUNIT").setFieldValue(product.getStringField("unit"));
        view.getComponentByReference("plannedQuantityUNIT").setFieldValue(product.getStringField("unit"));
        for (String reference : Arrays.asList("number", "name", "type", "usedQuantityUNIT", "plannedQuantityUNIT")) {
            ((FieldComponent) view.getComponentByReference(reference)).requestComponentUpdateState();
        }

    }

    private Entity getOrderFromLookup(final ViewDefinitionState view) {
        ComponentState lookup = view.getComponentByReference("order");
        if (!(lookup.getFieldValue() instanceof Long)) {
            return null;
        }
        return dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, MODEL_ORDER).get((Long) lookup.getFieldValue());
    }

    public void checkJustOne(final ViewDefinitionState view, final ComponentState componentState, final String[] args) {
        Entity order = getOrderFromLookup(view);
        if (order == null) {
            return;
        }
        FieldComponent lastRecord = (FieldComponent) view.getComponentByReference("lastRecord");
        if (order.getField("justOne") != null && (Boolean) order.getField("justOne")) {
            lastRecord.setFieldValue(true);
            lastRecord.setEnabled(false);
        } else {
            lastRecord.setFieldValue(false);
        }
        lastRecord.requestComponentUpdateState();
    }

    // VIEW HOOK for OrderDetails
    public void setOrderDefaultValue(final ViewDefinitionState view) {
        FieldComponent typeOfProductionRecording = (FieldComponent) view.getComponentByReference("typeOfProductionRecording");

        FormComponent form = (FormComponent) view.getComponentByReference("form");
        if (form.getEntityId() != null) {
            Entity order = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).get(
                    form.getEntityId());
            if (order == null || "".equals(order.getField("typeOfProductionRecording"))) {
                typeOfProductionRecording.setFieldValue(PARAM_RECORDING_TYPE_NONE);
            }
            for (String componentReference : Arrays.asList("registerQuantityInProduct", "registerQuantityOutProduct",
                    "registerProductionTime")) {
                FieldComponent component = (FieldComponent) view.getComponentByReference(componentReference);
                if (order == null || order.getField(componentReference) == null) {
                    component.setFieldValue(true);
                    component.requestComponentUpdateState();
                }
            }
        } else {
            for (String componentReference : Arrays.asList("registerQuantityInProduct", "registerQuantityOutProduct",
                    "registerProductionTime")) {
                FieldComponent component = (FieldComponent) view.getComponentByReference(componentReference);
                if (component.getFieldValue() == null) {
                    component.setFieldValue(true);
                    component.requestComponentUpdateState();
                }
            }
            for (String componentReference : Arrays.asList("registerQuantityInProduct", "registerQuantityOutProduct",
                    "registerProductionTime", "justOne", "allowToClose", "autoCloseOrder")) {
                FieldComponent component = (FieldComponent) view.getComponentByReference(componentReference);
                component.setEnabled(false);
            }
            typeOfProductionRecording.setFieldValue(PARAM_RECORDING_TYPE_NONE);
            typeOfProductionRecording.setEnabled(false);
            typeOfProductionRecording.addMessage(translationService.translate(
                    "orders.orderDetails.window.productionCounting.typeOfProductionRecording.error.saveOrderFirst",
                    view.getLocale()), MessageType.INFO);
        }
    }

    public void checkOrderState(final ViewDefinitionState viewDefinitionState) {
        FieldComponent orderState = (FieldComponent) viewDefinitionState.getComponentByReference("state");
        FieldComponent typeOfProductionRecording = (FieldComponent) viewDefinitionState
                .getComponentByReference("typeOfProductionRecording");
        if ("03inProgress".equals(orderState.getFieldValue()) || "04completed".equals(orderState.getFieldValue())
                || "06interrupted".equals(orderState.getFieldValue())) {
            for (String componentName : Arrays.asList("typeOfProductionRecording", "registerQuantityInProduct",
                    "registerQuantityOutProduct", "registerProductionTime", "justOne", "allowToClose", "autoCloseOrder")) {
                FieldComponent component = (FieldComponent) viewDefinitionState.getComponentByReference(componentName);
                component.setEnabled(false);
            }
        } else if (typeOfProductionRecording.getFieldValue().equals("")
                || typeOfProductionRecording.getFieldValue().equals("01basic")) {
            for (String componentName : Arrays.asList("registerQuantityInProduct", "registerQuantityOutProduct",
                    "registerProductionTime", "justOne", "allowToClose", "autoCloseOrder")) {
                FieldComponent component = (FieldComponent) viewDefinitionState.getComponentByReference(componentName);
                component.setEnabled(false);
            }
        }
    }

    public void disableFields(final ViewDefinitionState viewDefinitionState, final ComponentState componentState,
            final String[] args) {
        FieldComponent typeOfProductionRecording = (FieldComponent) viewDefinitionState
                .getComponentByReference("typeOfProductionRecording");
        FieldComponent doneQuantity = (FieldComponent) viewDefinitionState.getComponentByReference("doneQuantity");
        if ("".equals(typeOfProductionRecording.getFieldValue())) {
            doneQuantity.setEnabled(true);
        } else {
            doneQuantity.setEnabled(false);
        }
        if (typeOfProductionRecording.getFieldValue().equals("02cumulated")
                || typeOfProductionRecording.getFieldValue().equals("03forEach")) {
            for (String componentName : Arrays.asList("registerQuantityInProduct", "registerQuantityOutProduct",
                    "registerProductionTime", "justOne", "allowToClose", "autoCloseOrder")) {
                FieldComponent component = (FieldComponent) viewDefinitionState.getComponentByReference(componentName);
                component.setEnabled(true);
            }
        }
    }

    public void changeProducedQuantityFieldState(final ViewDefinitionState viewDefinitionState) {
        FieldComponent typeOfProductionRecording = (FieldComponent) viewDefinitionState
                .getComponentByReference("typeOfProductionRecording");
        FieldComponent doneQuantity = (FieldComponent) viewDefinitionState.getComponentByReference("doneQuantity");
        if ("".equals(typeOfProductionRecording.getFieldValue())) {
            doneQuantity.setEnabled(true);
        } else {
            doneQuantity.setEnabled(false);
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
        productionCountings = dataDefinitionService.get("basicProductionCounting", "basicProductionCounting").find()
                .add(SearchRestrictions.eq("order", order)).list().getEntities();

        Entity technology = order.getBelongsToField("technology");

        if (productionCountings.isEmpty()) {
            return;
        }
        for (Entity counting : productionCountings) {
            Entity aProduct = (Entity) counting.getField("product");
            if (technologyService.getProductType(aProduct, technology).equals(TechnologyService.PRODUCT)) {
                doneQuantity.setFieldValue(counting.getField("producedQuantity"));
                break;
            }
        }
    }

    public void disableFieldsIfBasicSelected(final ViewDefinitionState view, final ComponentState componentState,
            final String[] args) {
        FieldComponent shift = (FieldComponent) view.getComponentByReference("shift");
        FieldComponent laborTime = (FieldComponent) view.getComponentByReference("laborTime");
        FieldComponent machineTime = (FieldComponent) view.getComponentByReference("machineTime");
        FieldComponent orderComponent = (FieldComponent) view.getComponentByReference("order");
        WindowComponent window = (WindowComponent) view.getComponentByReference("window");

        RibbonActionItem save = window.getRibbon().getGroupByName("actions").getItemByName("save");
        RibbonActionItem saveBack = window.getRibbon().getGroupByName("actions").getItemByName("saveBack");
        RibbonActionItem saveNew = window.getRibbon().getGroupByName("actions").getItemByName("saveNew");

        Entity order = getOrderFromLookup(view);

        if (order == null) {
            return;
        }

        if ("01basic".equals(order.getField("typeOfProductionRecording"))) {
            shift.setEnabled(false);
            laborTime.setEnabled(false);
            machineTime.setEnabled(false);
            save.setEnabled(false);
            saveNew.setEnabled(false);
            saveBack.setEnabled(false);
            orderComponent.addMessage(translationService.translate(
                    "productionRecord.productionRecord.report.error.orderWithBasicProductionCounting", view.getLocale()),
                    ComponentState.MessageType.INFO);
        } else {
            shift.setEnabled(true);
            laborTime.setEnabled(true);
            machineTime.setEnabled(true);
            save.setEnabled(true);
            saveNew.setEnabled(true);
            saveBack.setEnabled(true);
        }
    }
}
