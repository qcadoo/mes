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

@Service
public class ProductionRecordViewService {

    private static final String LAST_RECORD = "lastRecord";

    private static final String SHIFT = "shift";

    private static final String OPTION_01BASIC = "01basic";

    private static final String COMPONENT_ALLOW_TO_CLOSE = "allowToClose";

    private static final String FIELD_JUST_ONE = "justOne";

    private static final String FIELD_NUMBER = "number";

    private static final String FIELD_AUTO_CLOSE_ORDER = "autoCloseOrder";

    private static final String COMPONENT_ORDER = "order";

    private static final String FIELD_REGISTER_QUANTITY_IN_PRODUCT = "registerQuantityInProduct";

    private static final String FIELD_REGISTER_QUANTITY_OUT_PRODUCT = "registerQuantityOutProduct";

    private static final String COMPONENT_BORDER_LAYOUT_NONE = "borderLayoutNone";

    private static final String COMPONENT_BORDER_LAYOUT_FOR_EACH = "borderLayoutForEach";

    private static final String COMPONENT_BORDER_LAYOUT_CUMULATED = "borderLayoutCumulated";

    private static final String COMPONENT_ORDER_OPERATION_COMPONENT = "orderOperationComponent";

    private static final String COMPONENT_MACHINE_TIME = "machineTime";

    private static final String COMPONENT_LABOR_TIME = "laborTime";

    private static final String COMPONENT_REGISTER_PRODUCTION_TIME = "registerProductionTime";

    private static final String COMPONENT_TYPE_OF_PRODUCTION_RECORDING = "typeOfProductionRecording";

    private static final String COMPONENT_STATE = "state";

    private static final String COMPONENT_FORM = "form";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private TranslationService translationService;

    @Autowired
    private TechnologyService technologyService;

    private static final String CLOSED_ORDER = "04completed";

    private static final Logger LOG = LoggerFactory.getLogger(ProductionRecordViewService.class);

    public void initializeRecordDetailsView(final ViewDefinitionState view) {
        FormComponent recordForm = (FormComponent) view.getComponentByReference(COMPONENT_FORM);

        FieldComponent status = (FieldComponent) view.getComponentByReference(COMPONENT_STATE);
        if (recordForm.getEntityId() == null) {
            status.setFieldValue("01draft");
            status.requestComponentUpdateState();
            return;
        }
        Entity record = recordForm.getEntity().getDataDefinition().get(recordForm.getEntityId());
        status.setFieldValue(record.getField(COMPONENT_STATE));
        status.requestComponentUpdateState();

        Entity order = getOrderFromLookup(view);
        String typeOfProductionRecording = order.getStringField(COMPONENT_TYPE_OF_PRODUCTION_RECORDING);

        view.getComponentByReference(COMPONENT_LABOR_TIME).setVisible(order.getBooleanField(COMPONENT_REGISTER_PRODUCTION_TIME));
        view.getComponentByReference(COMPONENT_MACHINE_TIME)
                .setVisible(order.getBooleanField(COMPONENT_REGISTER_PRODUCTION_TIME));

        view.getComponentByReference(COMPONENT_ORDER_OPERATION_COMPONENT).setVisible(
                PARAM_RECORDING_TYPE_FOREACH.equals(typeOfProductionRecording));
        view.getComponentByReference(COMPONENT_BORDER_LAYOUT_CUMULATED).setVisible(
                PARAM_RECORDING_TYPE_CUMULATED.equals(typeOfProductionRecording)
                        && order.getBooleanField(COMPONENT_REGISTER_PRODUCTION_TIME));
        view.getComponentByReference(COMPONENT_BORDER_LAYOUT_FOR_EACH).setVisible(
                PARAM_RECORDING_TYPE_FOREACH.equals(typeOfProductionRecording)
                        && order.getBooleanField(COMPONENT_REGISTER_PRODUCTION_TIME));
        view.getComponentByReference(COMPONENT_BORDER_LAYOUT_NONE).setVisible(
                getBooleanValue(!PARAM_RECORDING_TYPE_CUMULATED.equals(typeOfProductionRecording)
                        && !PARAM_RECORDING_TYPE_FOREACH.equals(typeOfProductionRecording)));
        view.getComponentByReference("recordOperationProductOutComponent").setVisible(
                order.getBooleanField(FIELD_REGISTER_QUANTITY_OUT_PRODUCT));
        view.getComponentByReference("recordOperationProductInComponent").setVisible(
                order.getBooleanField(FIELD_REGISTER_QUANTITY_IN_PRODUCT));

        view.getComponentByReference("isDisabled").setFieldValue(false);
    }

    public void setParametersDefaultValue(final ViewDefinitionState viewDefinitionState) {
        FormComponent form = (FormComponent) viewDefinitionState.getComponentByReference(COMPONENT_FORM);
        Entity parameter = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, MODEL_PARAMETER).get(form.getEntityId());

        for (String componentReference : Arrays.asList(FIELD_REGISTER_QUANTITY_IN_PRODUCT, FIELD_REGISTER_QUANTITY_OUT_PRODUCT,
                COMPONENT_REGISTER_PRODUCTION_TIME)) {
            FieldComponent component = (FieldComponent) viewDefinitionState.getComponentByReference(componentReference);
            if (parameter == null || parameter.getField(componentReference) == null) {
                component.setFieldValue(true);
                component.requestComponentUpdateState();
            }
        }
    }

    public void clearFields(final ViewDefinitionState view, final ComponentState componentState, final String[] args) {
        FieldComponent operation = (FieldComponent) view.getComponentByReference(COMPONENT_ORDER_OPERATION_COMPONENT);
        operation.setFieldValue("");
        FormComponent form = (FormComponent) view.getComponentByReference(COMPONENT_FORM);
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

        setComponentVisible((String) order.getField(COMPONENT_TYPE_OF_PRODUCTION_RECORDING), view);
    }

    private void setComponentVisible(final String recordingType, final ViewDefinitionState view) {
        view.getComponentByReference(COMPONENT_ORDER_OPERATION_COMPONENT).setVisible(
                PARAM_RECORDING_TYPE_FOREACH.equals(recordingType));
        ((FieldComponent) view.getComponentByReference(COMPONENT_ORDER_OPERATION_COMPONENT)).requestComponentUpdateState();

        if (PARAM_RECORDING_TYPE_CUMULATED.equals(recordingType)) {
            view.getComponentByReference(COMPONENT_BORDER_LAYOUT_CUMULATED).setVisible(true);
            view.getComponentByReference(COMPONENT_BORDER_LAYOUT_FOR_EACH).setVisible(false);
            view.getComponentByReference(COMPONENT_BORDER_LAYOUT_NONE).setVisible(false);
            view.getComponentByReference(COMPONENT_MACHINE_TIME).setVisible(true);
            view.getComponentByReference(COMPONENT_LABOR_TIME).setVisible(true);
        }
        if (PARAM_RECORDING_TYPE_FOREACH.equals(recordingType)) {
            view.getComponentByReference(COMPONENT_BORDER_LAYOUT_CUMULATED).setVisible(false);
            view.getComponentByReference(COMPONENT_BORDER_LAYOUT_FOR_EACH).setVisible(true);
            view.getComponentByReference(COMPONENT_BORDER_LAYOUT_NONE).setVisible(false);
            view.getComponentByReference(COMPONENT_MACHINE_TIME).setVisible(true);
            view.getComponentByReference(COMPONENT_LABOR_TIME).setVisible(true);
        }
        if (!PARAM_RECORDING_TYPE_CUMULATED.equals(recordingType) && !PARAM_RECORDING_TYPE_FOREACH.equals(recordingType)
                && !PARAM_RECORDING_TYPE_BASIC.equals(recordingType)) {
            ((FieldComponent) view.getComponentByReference(COMPONENT_ORDER)).addMessage(
                    translationService.translate("productionRecord.productionRecord.report.error.orderWithoutRecordingType",
                            view.getLocale()), ComponentState.MessageType.FAILURE);
            view.getComponentByReference(COMPONENT_BORDER_LAYOUT_CUMULATED).setVisible(false);
            view.getComponentByReference(COMPONENT_BORDER_LAYOUT_FOR_EACH).setVisible(false);
            view.getComponentByReference(COMPONENT_MACHINE_TIME).setVisible(false);
            view.getComponentByReference(COMPONENT_LABOR_TIME).setVisible(false);
            view.getComponentByReference(COMPONENT_BORDER_LAYOUT_NONE).setVisible(true);
        }

    }

    public void registeringProductionTime(final ViewDefinitionState view, final ComponentState componentState, final String[] args) {
        Entity order = getOrderFromLookup(view);
        if (order == null) {
            return;
        }
        Boolean registerProductionTime = order.getBooleanField(COMPONENT_REGISTER_PRODUCTION_TIME);
        if (registerProductionTime && order.getStringField(COMPONENT_TYPE_OF_PRODUCTION_RECORDING) != null
                && !("01none".equals(order.getStringField(COMPONENT_TYPE_OF_PRODUCTION_RECORDING)))) {
            view.getComponentByReference(COMPONENT_MACHINE_TIME).setVisible(true);
            view.getComponentByReference(COMPONENT_LABOR_TIME).setVisible(true);

        } else {
            view.getComponentByReference(COMPONENT_MACHINE_TIME).setVisible(false);
            view.getComponentByReference(COMPONENT_LABOR_TIME).setVisible(false);
            view.getComponentByReference(COMPONENT_BORDER_LAYOUT_CUMULATED).setVisible(false);
            view.getComponentByReference(COMPONENT_BORDER_LAYOUT_FOR_EACH).setVisible(false);
            view.getComponentByReference(COMPONENT_BORDER_LAYOUT_NONE).setVisible(false);
        }
    }

    public void closeOrder(final ViewDefinitionState view, final ComponentState componentState, final String[] args) {
        FormComponent form = (FormComponent) view.getComponentByReference(COMPONENT_FORM);
        Entity order = getOrderFromLookup(view);

        if (order == null) {
            return;
        }

        Boolean autoCloseOrder = order.getBooleanField(FIELD_AUTO_CLOSE_ORDER);
        String orderState = order.getStringField(COMPONENT_STATE);
        if (autoCloseOrder
                && "1".equals(view.getComponentByReference(LAST_RECORD).getFieldValue())
                && view.getComponentByReference(COMPONENT_STATE).getFieldValue()
                        .equals(ProductionCountingStates.ACCEPTED.getStringValue()) && "03inProgress".equals(orderState)) {
            order.setField(COMPONENT_STATE, CLOSED_ORDER);
            dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).save(order);
            Entity orderFromDB = order.getDataDefinition().get(order.getId());
            if (orderFromDB.getStringField(COMPONENT_STATE).equals(CLOSED_ORDER)) {
                form.addMessage(translationService.translate("productionCounting.order.orderClosed", view.getLocale()),
                        MessageType.INFO, false);
            } else {
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
            }
        }
    }

    public void fillFieldFromProduct(final ViewDefinitionState view) {
        Entity recordProduct = ((FormComponent) view.getComponentByReference(COMPONENT_FORM)).getEntity();
        recordProduct = recordProduct.getDataDefinition().get(recordProduct.getId());
        Entity product = recordProduct.getBelongsToField("product");

        view.getComponentByReference(FIELD_NUMBER).setFieldValue(product.getField(FIELD_NUMBER));
        view.getComponentByReference("name").setFieldValue(product.getField("name"));

        view.getComponentByReference("usedQuantityUNIT").setFieldValue(product.getStringField("unit"));
        view.getComponentByReference("plannedQuantityUNIT").setFieldValue(product.getStringField("unit"));
        for (String reference : Arrays.asList(FIELD_NUMBER, "name", "type", "usedQuantityUNIT", "plannedQuantityUNIT")) {
            ((FieldComponent) view.getComponentByReference(reference)).requestComponentUpdateState();
        }

    }

    private Entity getOrderFromLookup(final ViewDefinitionState view) {
        ComponentState lookup = view.getComponentByReference(COMPONENT_ORDER);
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
        FieldComponent lastRecord = (FieldComponent) view.getComponentByReference(LAST_RECORD);
        if (order.getField(FIELD_JUST_ONE) != null && (Boolean) order.getField(FIELD_JUST_ONE)) {
            lastRecord.setFieldValue(true);
            lastRecord.setEnabled(false);
        } else {
            lastRecord.setFieldValue(false);
        }
        lastRecord.requestComponentUpdateState();
    }

    // VIEW HOOK for OrderDetails
    public void setOrderDefaultValue(final ViewDefinitionState view) {
        FieldComponent typeOfProductionRecording = (FieldComponent) view
                .getComponentByReference(COMPONENT_TYPE_OF_PRODUCTION_RECORDING);

        FormComponent form = (FormComponent) view.getComponentByReference(COMPONENT_FORM);
        if (form.getEntityId() == null) {
            for (String componentReference : Arrays.asList(FIELD_REGISTER_QUANTITY_IN_PRODUCT,
                    FIELD_REGISTER_QUANTITY_OUT_PRODUCT, COMPONENT_REGISTER_PRODUCTION_TIME)) {
                FieldComponent component = (FieldComponent) view.getComponentByReference(componentReference);
                if (component.getFieldValue() == null) {
                    component.setFieldValue(true);
                    component.requestComponentUpdateState();
                }
            }
            for (String componentReference : Arrays.asList(FIELD_REGISTER_QUANTITY_IN_PRODUCT,
                    FIELD_REGISTER_QUANTITY_OUT_PRODUCT, COMPONENT_REGISTER_PRODUCTION_TIME, FIELD_JUST_ONE,
                    COMPONENT_ALLOW_TO_CLOSE, FIELD_AUTO_CLOSE_ORDER)) {
                FieldComponent component = (FieldComponent) view.getComponentByReference(componentReference);
                component.setEnabled(false);
            }
            typeOfProductionRecording.setFieldValue(PARAM_RECORDING_TYPE_NONE);
            typeOfProductionRecording.setEnabled(false);
            // typeOfProductionRecording.addMessage(translationService.translate(
            // "orders.orderDetails.window.productionCounting.typeOfProductionRecording.error.saveOrderFirst",
            // view.getLocale()), MessageType.SUCCESS);
        } else {
            Entity order = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).get(
                    form.getEntityId());
            if (order == null || "".equals(order.getField(COMPONENT_TYPE_OF_PRODUCTION_RECORDING))) {
                typeOfProductionRecording.setFieldValue(PARAM_RECORDING_TYPE_NONE);
            }
            for (String componentReference : Arrays.asList(FIELD_REGISTER_QUANTITY_IN_PRODUCT,
                    FIELD_REGISTER_QUANTITY_OUT_PRODUCT, COMPONENT_REGISTER_PRODUCTION_TIME)) {
                FieldComponent component = (FieldComponent) view.getComponentByReference(componentReference);
                if (order == null || order.getField(componentReference) == null) {
                    component.setFieldValue(true);
                    component.requestComponentUpdateState();
                }
            }
        }
    }

    public void checkOrderState(final ViewDefinitionState viewDefinitionState) {
        FieldComponent orderState = (FieldComponent) viewDefinitionState.getComponentByReference(COMPONENT_STATE);
        FieldComponent typeOfProductionRecording = (FieldComponent) viewDefinitionState
                .getComponentByReference(COMPONENT_TYPE_OF_PRODUCTION_RECORDING);
        if ("02accepted".equals(orderState.getFieldValue()) || "03inProgress".equals(orderState.getFieldValue())
                || "04completed".equals(orderState.getFieldValue()) || "06interrupted".equals(orderState.getFieldValue())) {
            for (String componentName : Arrays.asList(COMPONENT_TYPE_OF_PRODUCTION_RECORDING, FIELD_REGISTER_QUANTITY_IN_PRODUCT,
                    FIELD_REGISTER_QUANTITY_OUT_PRODUCT, COMPONENT_REGISTER_PRODUCTION_TIME, FIELD_JUST_ONE,
                    COMPONENT_ALLOW_TO_CLOSE, FIELD_AUTO_CLOSE_ORDER)) {
                FieldComponent component = (FieldComponent) viewDefinitionState.getComponentByReference(componentName);
                component.setEnabled(false);
            }
        } else if (typeOfProductionRecording.getFieldValue().equals("")
                || typeOfProductionRecording.getFieldValue().equals(OPTION_01BASIC)) {
            for (String componentName : Arrays.asList(FIELD_REGISTER_QUANTITY_IN_PRODUCT, FIELD_REGISTER_QUANTITY_OUT_PRODUCT,
                    COMPONENT_REGISTER_PRODUCTION_TIME, FIELD_JUST_ONE, COMPONENT_ALLOW_TO_CLOSE, FIELD_AUTO_CLOSE_ORDER)) {
                FieldComponent component = (FieldComponent) viewDefinitionState.getComponentByReference(componentName);
                component.setEnabled(false);
            }
        }
    }

    public void disableFields(final ViewDefinitionState viewDefinitionState, final ComponentState componentState,
            final String[] args) {
        FieldComponent typeOfProductionRecording = (FieldComponent) viewDefinitionState
                .getComponentByReference(COMPONENT_TYPE_OF_PRODUCTION_RECORDING);
        FieldComponent doneQuantity = (FieldComponent) viewDefinitionState.getComponentByReference("doneQuantity");
        if ("".equals(typeOfProductionRecording.getFieldValue())) {
            doneQuantity.setEnabled(true);
        } else {
            doneQuantity.setEnabled(false);
        }
        if (typeOfProductionRecording.getFieldValue().equals("02cumulated")
                || typeOfProductionRecording.getFieldValue().equals("03forEach")) {
            for (String componentName : Arrays.asList(FIELD_REGISTER_QUANTITY_IN_PRODUCT, FIELD_REGISTER_QUANTITY_OUT_PRODUCT,
                    COMPONENT_REGISTER_PRODUCTION_TIME, FIELD_JUST_ONE, COMPONENT_ALLOW_TO_CLOSE, FIELD_AUTO_CLOSE_ORDER)) {
                FieldComponent component = (FieldComponent) viewDefinitionState.getComponentByReference(componentName);
                component.setEnabled(true);
            }
        }
    }

    public void changeProducedQuantityFieldState(final ViewDefinitionState viewDefinitionState) {
        FieldComponent typeOfProductionRecording = (FieldComponent) viewDefinitionState
                .getComponentByReference(COMPONENT_TYPE_OF_PRODUCTION_RECORDING);
        FieldComponent doneQuantity = (FieldComponent) viewDefinitionState.getComponentByReference("doneQuantity");
        if ("".equals(typeOfProductionRecording.getFieldValue())) {
            doneQuantity.setEnabled(true);
        } else {
            doneQuantity.setEnabled(false);
        }
    }

    public void setProducedQuantity(final ViewDefinitionState view) {
        FieldComponent typeOfProductionRecording = (FieldComponent) view
                .getComponentByReference(COMPONENT_TYPE_OF_PRODUCTION_RECORDING);
        FieldComponent doneQuantity = (FieldComponent) view.getComponentByReference("doneQuantity");
        String orderNumber = (String) view.getComponentByReference(FIELD_NUMBER).getFieldValue();
        Entity order;
        List<Entity> productionCountings;

        if ("".equals(typeOfProductionRecording.getFieldValue())) {
            return;
        }

        if (orderNumber == null) {
            return;
        }
        order = dataDefinitionService.get("orders", COMPONENT_ORDER).find().add(SearchRestrictions.eq(FIELD_NUMBER, orderNumber))
                .uniqueResult();
        if (order == null) {
            return;
        }
        productionCountings = dataDefinitionService.get("basicProductionCounting", "basicProductionCounting").find()
                .add(SearchRestrictions.eq(COMPONENT_ORDER, order)).list().getEntities();

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
        FieldComponent orderComponent = (FieldComponent) view.getComponentByReference(COMPONENT_ORDER);
        Entity order = getOrderFromLookup(view);

        if (order == null) {
            return;
        }

        List<String> components = Arrays.asList(SHIFT, COMPONENT_MACHINE_TIME, COMPONENT_LABOR_TIME, LAST_RECORD);
        for (String componentName : components) {
            FieldComponent component = (FieldComponent) view.getComponentByReference(componentName);
            if (OPTION_01BASIC.equals(order.getField(COMPONENT_TYPE_OF_PRODUCTION_RECORDING))) {
                component.setEnabled(false);
            } else {
                component.setEnabled(true);
            }
            component.requestComponentUpdateState();
        }

        List<String> times = Arrays.asList(COMPONENT_LABOR_TIME, COMPONENT_MACHINE_TIME);
        for (String componentName : times) {
            FieldComponent component = (FieldComponent) view.getComponentByReference(componentName);
            if (OPTION_01BASIC.equals(order.getField(COMPONENT_TYPE_OF_PRODUCTION_RECORDING))) {
                component.setVisible(false);
            } else {
                component.setVisible(true);
            }
            component.requestComponentUpdateState();
        }

        FieldComponent isDisabled = (FieldComponent) view.getComponentByReference("isDisabled");

        if (OPTION_01BASIC.equals(order.getField(COMPONENT_TYPE_OF_PRODUCTION_RECORDING))) {
            orderComponent.addMessage(translationService.translate(
                    "productionRecord.productionRecord.report.error.orderWithBasicProductionCounting", view.getLocale()),
                    ComponentState.MessageType.INFO);
            isDisabled.setFieldValue(true);
        } else {
            isDisabled.setFieldValue(false);
        }
        isDisabled.requestComponentUpdateState();
    }
}
