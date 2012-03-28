/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.4
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
import static com.qcadoo.mes.productionCounting.internal.constants.OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING;
import static com.qcadoo.mes.productionCounting.internal.constants.TypeOfProductionRecording.BASIC;
import static com.qcadoo.mes.productionCounting.internal.constants.TypeOfProductionRecording.CUMULATED;
import static com.qcadoo.mes.productionCounting.internal.constants.TypeOfProductionRecording.FOR_EACH;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.productionCounting.internal.constants.TypeOfProductionRecording;
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

    private static final String L_REGISTER_PIECEWORK = "registerPiecework";

    private static final String LAST_RECORD = "lastRecord";

    private static final String COMPONENT_ALLOW_TO_CLOSE = "allowToClose";

    private static final String L_JUST_ONE = "justOne";

    private static final String FIELD_NUMBER = "number";

    private static final String FIELD_AUTO_CLOSE_ORDER = "autoCloseOrder";

    private static final String COMPONENT_ORDER = "order";

    private static final String FIELD_REGISTER_QUANTITY_IN_PRODUCT = "registerQuantityInProduct";

    private static final String FIELD_REGISTER_QUANTITY_OUT_PRODUCT = "registerQuantityOutProduct";

    private static final String COMPONENT_ORDER_OPERATION_COMPONENT = "orderOperationComponent";

    private static final String L_REGISTER_PRODUCTION_TIME = "registerProductionTime";

    private static final String L_COMPONENT_STATE = "state";

    private static final String L_COMPONENT_FORM = "form";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private TranslationService translationService;

    @Autowired
    private TechnologyService technologyService;

    private static final String CLOSED_ORDER = "04completed";

    private static final Logger LOG = LoggerFactory.getLogger(ProductionRecordViewService.class);

    public void initializeRecordDetailsView(final ViewDefinitionState view) {
        FormComponent recordForm = (FormComponent) view.getComponentByReference(L_COMPONENT_FORM);

        FieldComponent status = (FieldComponent) view.getComponentByReference(L_COMPONENT_STATE);
        if (recordForm.getEntityId() == null) {
            status.setFieldValue("01draft");
            status.requestComponentUpdateState();
            return;
        }
        Entity record = recordForm.getEntity().getDataDefinition().get(recordForm.getEntityId());
        status.setFieldValue(record.getField(L_COMPONENT_STATE));
        status.requestComponentUpdateState();

        Entity order = getOrderFromLookup(view);
        String typeOfProductionRecording = order.getStringField(TYPE_OF_PRODUCTION_RECORDING);
        setTimeAndPiecworkComponentsVisible(typeOfProductionRecording, order, view);

        view.getComponentByReference(COMPONENT_ORDER_OPERATION_COMPONENT).setVisible(
                FOR_EACH.getStringValue().equals(typeOfProductionRecording));
        view.getComponentByReference("recordOperationProductOutComponent").setVisible(
                order.getBooleanField(FIELD_REGISTER_QUANTITY_OUT_PRODUCT));
        view.getComponentByReference("recordOperationProductInComponent").setVisible(
                order.getBooleanField(FIELD_REGISTER_QUANTITY_IN_PRODUCT));

        view.getComponentByReference("isDisabled").setFieldValue(false);
    }

    public void setParametersDefaultValue(final ViewDefinitionState viewDefinitionState) {
        FormComponent form = (FormComponent) viewDefinitionState.getComponentByReference(L_COMPONENT_FORM);
        Entity parameter = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, MODEL_PARAMETER).get(form.getEntityId());

        for (String componentReference : Arrays.asList(FIELD_REGISTER_QUANTITY_IN_PRODUCT, FIELD_REGISTER_QUANTITY_OUT_PRODUCT,
                L_REGISTER_PRODUCTION_TIME)) {
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
        FormComponent form = (FormComponent) view.getComponentByReference(L_COMPONENT_FORM);
        if (form.getEntityId() == null) {
            return;
        }
        GridComponent productsIn = (GridComponent) view.getComponentByReference("recordOperationProductInComponent");
        GridComponent productOut = (GridComponent) view.getComponentByReference("recordOperationProductOutComponent");

        productOut.setEntities(new ArrayList<Entity>());
        productsIn.setEntities(new ArrayList<Entity>());
    }

    public void enabledOrDisableFields(final ViewDefinitionState view, final ComponentState componentState, final String[] args) {
        Entity order = getOrderFromLookup(view);
        if (order == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("order is null");
            }
            return;
        }

        String recordingType = (String) order.getField(TYPE_OF_PRODUCTION_RECORDING);
        setTimeAndPiecworkComponentsVisible(recordingType, order, view);
    }

    private void setTimeAndPiecworkComponentsVisible(final String recordingType, final Entity order,
            final ViewDefinitionState view) {
        view.getComponentByReference(COMPONENT_ORDER_OPERATION_COMPONENT).setVisible(
                TypeOfProductionRecording.FOR_EACH.getStringValue().equals(recordingType));
        ((FieldComponent) view.getComponentByReference(COMPONENT_ORDER_OPERATION_COMPONENT)).requestComponentUpdateState();

        boolean registerProductionTime = order.getBooleanField(L_REGISTER_PRODUCTION_TIME);
        view.getComponentByReference("borderLayoutTime").setVisible(
                registerProductionTime && !BASIC.getStringValue().equals(recordingType));

        boolean registerPiecework = order.getBooleanField("registerPiecework");
        view.getComponentByReference("borderLayoutPiecework").setVisible(
                registerPiecework && TypeOfProductionRecording.FOR_EACH.getStringValue().equals(recordingType));
    }

    public void closeOrder(final ViewDefinitionState view, final ComponentState componentState, final String[] args) {
        FormComponent form = (FormComponent) view.getComponentByReference(L_COMPONENT_FORM);
        Entity order = getOrderFromLookup(view);

        if (order == null) {
            return;
        }

        Boolean autoCloseOrder = order.getBooleanField(FIELD_AUTO_CLOSE_ORDER);
        String orderState = order.getStringField(L_COMPONENT_STATE);
        if (autoCloseOrder
                && "1".equals(view.getComponentByReference(LAST_RECORD).getFieldValue())
                && view.getComponentByReference(L_COMPONENT_STATE).getFieldValue()
                        .equals(ProductionCountingStates.ACCEPTED.getStringValue()) && "03inProgress".equals(orderState)) {
            order.setField(L_COMPONENT_STATE, CLOSED_ORDER);
            dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).save(order);
            Entity orderFromDB = order.getDataDefinition().get(order.getId());
            if (orderFromDB.getStringField(L_COMPONENT_STATE).equals(CLOSED_ORDER)) {
                form.addMessage("productionCounting.order.orderClosed", MessageType.INFO, false);
            } else {
                form.addMessage("productionCounting.order.orderCannotBeClosed", MessageType.INFO, false);

                List<ErrorMessage> errors = Lists.newArrayList();
                if (!order.getErrors().isEmpty()) {
                    errors.addAll(order.getErrors().values());
                }
                if (!order.getGlobalErrors().isEmpty()) {
                    errors.addAll(order.getGlobalErrors());
                }

                StringBuilder errorMessages = new StringBuilder();
                for (ErrorMessage message : errors) {
                    String translatedErrorMessage = translationService.translate(message.getMessage(), view.getLocale(),
                            message.getVars());
                    errorMessages.append(translatedErrorMessage);
                    errorMessages.append(", ");
                }
                form.addMessage("orders.order.orderStates.error", MessageType.FAILURE, false, errorMessages.toString());

            }
        }
    }

    public void fillFieldFromProduct(final ViewDefinitionState view) {
        Entity recordProduct = ((FormComponent) view.getComponentByReference(L_COMPONENT_FORM)).getEntity();
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
        if (order.getBooleanField(L_JUST_ONE)) {
            lastRecord.setFieldValue(true);
            lastRecord.setEnabled(false);
        } else {
            lastRecord.setFieldValue(false);
            lastRecord.setEnabled(true);
        }
        lastRecord.requestComponentUpdateState();
    }

    public void setOrderDefaultValue(final ViewDefinitionState view) {
        FieldComponent typeOfProductionRecording = (FieldComponent) view.getComponentByReference(TYPE_OF_PRODUCTION_RECORDING);

        FormComponent form = (FormComponent) view.getComponentByReference(L_COMPONENT_FORM);
        if (form.getEntityId() == null) {
            for (String componentReference : Arrays.asList(FIELD_REGISTER_QUANTITY_IN_PRODUCT,
                    FIELD_REGISTER_QUANTITY_OUT_PRODUCT, L_REGISTER_PRODUCTION_TIME, L_REGISTER_PIECEWORK)) {
                FieldComponent component = (FieldComponent) view.getComponentByReference(componentReference);
                if (component.getFieldValue() == null) {
                    component.setFieldValue(true);
                    component.requestComponentUpdateState();
                }
            }
            for (String componentReference : Arrays.asList(FIELD_REGISTER_QUANTITY_IN_PRODUCT,
                    FIELD_REGISTER_QUANTITY_OUT_PRODUCT, L_REGISTER_PRODUCTION_TIME, L_JUST_ONE, COMPONENT_ALLOW_TO_CLOSE,
                    FIELD_AUTO_CLOSE_ORDER)) {
                FieldComponent component = (FieldComponent) view.getComponentByReference(componentReference);
                component.setEnabled(false);
            }
            typeOfProductionRecording.setFieldValue(BASIC.getStringValue());
            typeOfProductionRecording.setEnabled(false);
        } else {
            Entity order = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).get(
                    form.getEntityId());
            if (order == null || "".equals(order.getField(TYPE_OF_PRODUCTION_RECORDING))) {
                typeOfProductionRecording.setFieldValue(BASIC.getStringValue());
            }
            for (String componentReference : Arrays.asList(FIELD_REGISTER_QUANTITY_IN_PRODUCT,
                    FIELD_REGISTER_QUANTITY_OUT_PRODUCT, L_REGISTER_PRODUCTION_TIME, L_REGISTER_PIECEWORK)) {
                FieldComponent component = (FieldComponent) view.getComponentByReference(componentReference);
                if (order == null || order.getField(componentReference) == null) {
                    component.setFieldValue(true);
                    component.requestComponentUpdateState();
                }
            }
        }
    }

    public void checkOrderState(final ViewDefinitionState viewDefinitionState) {
        FieldComponent orderState = (FieldComponent) viewDefinitionState.getComponentByReference(L_COMPONENT_STATE);
        FieldComponent typeOfProductionRecording = (FieldComponent) viewDefinitionState
                .getComponentByReference(TYPE_OF_PRODUCTION_RECORDING);
        if ("02accepted".equals(orderState.getFieldValue()) || "03inProgress".equals(orderState.getFieldValue())
                || "04completed".equals(orderState.getFieldValue()) || "06interrupted".equals(orderState.getFieldValue())) {
            for (String componentName : Arrays.asList(TYPE_OF_PRODUCTION_RECORDING, FIELD_REGISTER_QUANTITY_IN_PRODUCT,
                    FIELD_REGISTER_QUANTITY_OUT_PRODUCT, L_REGISTER_PRODUCTION_TIME, L_REGISTER_PIECEWORK, L_JUST_ONE,
                    COMPONENT_ALLOW_TO_CLOSE, FIELD_AUTO_CLOSE_ORDER)) {
                FieldComponent component = (FieldComponent) viewDefinitionState.getComponentByReference(componentName);
                component.setEnabled(false);
            }
        } else if ("".equals(typeOfProductionRecording.getFieldValue())
                || BASIC.getStringValue().equals(typeOfProductionRecording.getFieldValue())) {
            for (String componentName : Arrays.asList(FIELD_REGISTER_QUANTITY_IN_PRODUCT, FIELD_REGISTER_QUANTITY_OUT_PRODUCT,
                    L_REGISTER_PRODUCTION_TIME, L_REGISTER_PIECEWORK, L_JUST_ONE, COMPONENT_ALLOW_TO_CLOSE,
                    FIELD_AUTO_CLOSE_ORDER)) {
                FieldComponent component = (FieldComponent) viewDefinitionState.getComponentByReference(componentName);
                component.setEnabled(false);
            }
        }
    }

    public void disableFields(final ViewDefinitionState viewDefinitionState, final ComponentState componentState,
            final String[] args) {
        changeProducedQuantityFieldState(viewDefinitionState);
        FieldComponent typeOfProductionRecording = (FieldComponent) viewDefinitionState
                .getComponentByReference(TYPE_OF_PRODUCTION_RECORDING);
        if (CUMULATED.getStringValue().equals(typeOfProductionRecording.getFieldValue())
                || FOR_EACH.getStringValue().equals(typeOfProductionRecording.getFieldValue())) {
            for (String componentName : Arrays.asList(FIELD_REGISTER_QUANTITY_IN_PRODUCT, FIELD_REGISTER_QUANTITY_OUT_PRODUCT,
                    L_REGISTER_PRODUCTION_TIME, L_JUST_ONE, COMPONENT_ALLOW_TO_CLOSE, FIELD_AUTO_CLOSE_ORDER,
                    L_REGISTER_PIECEWORK)) {
                FieldComponent component = (FieldComponent) viewDefinitionState.getComponentByReference(componentName);
                component.setEnabled(true);
            }
        }
    }

    public void changeProducedQuantityFieldState(final ViewDefinitionState viewDefinitionState) {
        FieldComponent typeOfProductionRecording = (FieldComponent) viewDefinitionState
                .getComponentByReference(TYPE_OF_PRODUCTION_RECORDING);
        FieldComponent doneQuantity = (FieldComponent) viewDefinitionState.getComponentByReference("doneQuantity");
        if ("".equals(typeOfProductionRecording.getFieldValue())) {
            doneQuantity.setEnabled(true);
        } else {
            doneQuantity.setEnabled(false);
        }
    }

    public void setProducedQuantity(final ViewDefinitionState view) {
        FieldComponent typeOfProductionRecording = (FieldComponent) view.getComponentByReference(TYPE_OF_PRODUCTION_RECORDING);
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
            if (technologyService.getProductType(aProduct, technology).equals(TechnologyService.FINAL_PRODUCT)) {
                doneQuantity.setFieldValue(counting.getField("producedQuantity"));
                break;
            }
        }
    }
}
