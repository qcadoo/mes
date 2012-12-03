/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.2.0-SNAPSHOT
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

import static com.qcadoo.mes.orders.constants.OrderFields.STATE;
import static com.qcadoo.mes.orders.constants.OrderFields.TECHNOLOGY;
import static com.qcadoo.mes.orders.states.constants.OrderStateChangeFields.STATUS;
import static com.qcadoo.mes.productionCounting.internal.constants.OrderFieldsPC.ALLOW_TO_CLOSE;
import static com.qcadoo.mes.productionCounting.internal.constants.OrderFieldsPC.AUTO_CLOSE_ORDER;
import static com.qcadoo.mes.productionCounting.internal.constants.OrderFieldsPC.JUST_ONE;
import static com.qcadoo.mes.productionCounting.internal.constants.OrderFieldsPC.REGISTER_PIECEWORK;
import static com.qcadoo.mes.productionCounting.internal.constants.OrderFieldsPC.REGISTER_PRODUCTION_TIME;
import static com.qcadoo.mes.productionCounting.internal.constants.OrderFieldsPC.REGISTER_QUANTITY_IN_PRODUCT;
import static com.qcadoo.mes.productionCounting.internal.constants.OrderFieldsPC.REGISTER_QUANTITY_OUT_PRODUCT;
import static com.qcadoo.mes.productionCounting.internal.constants.OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING;
import static com.qcadoo.mes.productionCounting.internal.constants.ProductionRecordFields.LAST_RECORD;
import static com.qcadoo.mes.productionCounting.internal.constants.ProductionRecordFields.NUMBER;
import static com.qcadoo.mes.productionCounting.internal.constants.ProductionRecordFields.ORDER;
import static com.qcadoo.mes.productionCounting.internal.constants.ProductionRecordFields.TECHNOLOGY_INSTANCE_OPERATION_COMPONENT;
import static com.qcadoo.mes.productionCounting.internal.constants.TypeOfProductionRecording.BASIC;
import static com.qcadoo.mes.productionCounting.internal.constants.TypeOfProductionRecording.CUMULATED;
import static com.qcadoo.mes.productionCounting.internal.constants.TypeOfProductionRecording.FOR_EACH;
import static com.qcadoo.mes.productionCounting.states.constants.ProductionRecordState.DRAFT;
import static com.qcadoo.mes.states.constants.StateChangeStatus.SUCCESSFUL;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.basicProductionCounting.constants.BasicProductionCountingConstants;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.orders.states.constants.OrderState;
import com.qcadoo.mes.states.service.client.util.StateChangeHistoryService;
import com.qcadoo.mes.technologies.TechnologyService;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.CustomRestriction;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.ribbon.RibbonActionItem;

@Service
public class ProductionRecordViewService {

    private static final String L_RECORD_OPERATION_PRODUCT_IN_COMPONENT = "recordOperationProductInComponent";

    private static final String L_RECORD_OPERATION_PRODUCT_OUT_COMPONENT = "recordOperationProductOutComponent";

    private static final String L_FORM = "form";

    private static final String L_PLANNED_QUANTITY_UNIT = "plannedQuantityUNIT";

    private static final String L_USED_QUANTITY_UNIT = "usedQuantityUNIT";

    private static final String L_PRODUCT = "product";

    private static final String L_DONE_QUANTITY = "doneQuantity";

    private static final String L_UNIT = "unit";

    private static final String L_NAME = "name";

    private static final String L_STATE = "state";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private TechnologyService technologyService;

    @Autowired
    private ParameterService parameterService;

    @Autowired
    private StateChangeHistoryService stateChangeHistoryService;

    private static final Logger LOG = LoggerFactory.getLogger(ProductionRecordViewService.class);

    public void enabledOrDisabledCopyRibbon(final ViewDefinitionState view) {
        FormComponent form = (FormComponent) view.getComponentByReference(L_FORM);
        if (form.getEntity() == null) {
            return;
        }
        WindowComponent window = (WindowComponent) view.getComponentByReference("window");
        RibbonActionItem copyButton = window.getRibbon().getGroupByName("actions").getItemByName("copy");

        Entity productionRecord = form.getEntity();
        Entity order = productionRecord.getBelongsToField(ORDER);
        if (order == null) {
            return;
        }
        String orderState = order.getStringField(L_STATE);
        if (OrderState.IN_PROGRESS.getStringValue().equals(orderState)) {
            copyButton.setEnabled(true);
        } else {
            copyButton.setEnabled(false);
        }
        copyButton.requestUpdate(true);
    }

    public void initializeRecordDetailsView(final ViewDefinitionState view) {
        FormComponent recordForm = (FormComponent) view.getComponentByReference(L_FORM);

        FieldComponent status = (FieldComponent) view.getComponentByReference(L_STATE);
        if (recordForm.getEntityId() == null) {
            status.setFieldValue("01draft");
            status.requestComponentUpdateState();
            return;
        }
        Entity record = recordForm.getEntity().getDataDefinition().get(recordForm.getEntityId());
        status.setFieldValue(record.getField(L_STATE));
        status.requestComponentUpdateState();

        Entity order = ((LookupComponent) view.getComponentByReference(ORDER)).getEntity();
        String typeOfProductionRecording = order.getStringField(TYPE_OF_PRODUCTION_RECORDING);
        setTimeAndPiecworkComponentsVisible(typeOfProductionRecording, order, view);

        view.getComponentByReference(TECHNOLOGY_INSTANCE_OPERATION_COMPONENT).setVisible(
                FOR_EACH.getStringValue().equals(typeOfProductionRecording));
        view.getComponentByReference(L_RECORD_OPERATION_PRODUCT_OUT_COMPONENT).setVisible(
                order.getBooleanField(REGISTER_QUANTITY_OUT_PRODUCT));
        view.getComponentByReference(L_RECORD_OPERATION_PRODUCT_IN_COMPONENT).setVisible(
                order.getBooleanField(REGISTER_QUANTITY_IN_PRODUCT));

        view.getComponentByReference("isDisabled").setFieldValue(false);
    }

    public void clearFields(final ViewDefinitionState view, final ComponentState componentState, final String[] args) {
        FieldComponent operation = (FieldComponent) view.getComponentByReference(TECHNOLOGY_INSTANCE_OPERATION_COMPONENT);
        operation.setFieldValue("");
        FormComponent form = (FormComponent) view.getComponentByReference(L_FORM);
        if (form.getEntityId() == null) {
            return;
        }
        GridComponent productsIn = (GridComponent) view.getComponentByReference(L_RECORD_OPERATION_PRODUCT_IN_COMPONENT);
        GridComponent productOut = (GridComponent) view.getComponentByReference(L_RECORD_OPERATION_PRODUCT_OUT_COMPONENT);

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
        view.getComponentByReference(TECHNOLOGY_INSTANCE_OPERATION_COMPONENT).setVisible(
                FOR_EACH.getStringValue().equals(recordingType));
        ((FieldComponent) view.getComponentByReference(TECHNOLOGY_INSTANCE_OPERATION_COMPONENT)).requestComponentUpdateState();

        boolean registerProductionTime = order.getBooleanField(REGISTER_PRODUCTION_TIME);
        view.getComponentByReference("borderLayoutTime").setVisible(
                registerProductionTime && !BASIC.getStringValue().equals(recordingType));

        boolean registerPiecework = order.getBooleanField(REGISTER_PIECEWORK);
        view.getComponentByReference("borderLayoutPiecework").setVisible(
                registerPiecework && FOR_EACH.getStringValue().equals(recordingType));
    }

    public void fillFieldFromProduct(final ViewDefinitionState view) {
        Entity recordProduct = ((FormComponent) view.getComponentByReference(L_FORM)).getEntity();
        recordProduct = recordProduct.getDataDefinition().get(recordProduct.getId());
        Entity product = recordProduct.getBelongsToField(L_PRODUCT);

        view.getComponentByReference(NUMBER).setFieldValue(product.getField(NUMBER));
        view.getComponentByReference(L_NAME).setFieldValue(product.getField(L_NAME));

        view.getComponentByReference(L_USED_QUANTITY_UNIT).setFieldValue(product.getStringField(L_UNIT));
        view.getComponentByReference(L_PLANNED_QUANTITY_UNIT).setFieldValue(product.getStringField(L_UNIT));
        for (String reference : Arrays.asList(NUMBER, L_NAME, L_USED_QUANTITY_UNIT, L_PLANNED_QUANTITY_UNIT)) {
            ((FieldComponent) view.getComponentByReference(reference)).requestComponentUpdateState();
        }

    }

    private Entity getOrderFromLookup(final ViewDefinitionState view) {
        LookupComponent lookup = (LookupComponent) view.getComponentByReference(ORDER);
        return lookup.getEntity();
    }

    public void checkJustOne(final ViewDefinitionState view, final ComponentState componentState, final String[] args) {
        Entity order = getOrderFromLookup(view);
        if (order == null) {
            return;
        }
        FieldComponent lastRecord = (FieldComponent) view.getComponentByReference(LAST_RECORD);
        if (order.getBooleanField(JUST_ONE)) {
            lastRecord.setFieldValue(true);
            lastRecord.setEnabled(false);
        } else {
            lastRecord.setFieldValue(false);
            lastRecord.setEnabled(true);
        }
        lastRecord.requestComponentUpdateState();
    }

    public void setOrderDefaultValue(final ViewDefinitionState view) {

        FormComponent form = (FormComponent) view.getComponentByReference(L_FORM);
        if (form.getEntityId() != null) {
            return;
        }

        for (String componentReference : Arrays.asList(REGISTER_QUANTITY_IN_PRODUCT, REGISTER_QUANTITY_OUT_PRODUCT,
                REGISTER_PRODUCTION_TIME, JUST_ONE, ALLOW_TO_CLOSE, AUTO_CLOSE_ORDER, REGISTER_PIECEWORK)) {
            FieldComponent component = (FieldComponent) view.getComponentByReference(componentReference);
            if (component.getFieldValue() == null) {
                component.setFieldValue(getDefaultValueForProductionRecordFromParameter(componentReference));
                component.requestComponentUpdateState();
            }
            component.setEnabled(false);
        }
        FieldComponent typeOfProductionRecording = (FieldComponent) view.getComponentByReference(TYPE_OF_PRODUCTION_RECORDING);
        if (typeOfProductionRecording.getFieldValue() == null) {
            typeOfProductionRecording
                    .setFieldValue(getDefaultValueForTypeOfProductionRecordingParameter(TYPE_OF_PRODUCTION_RECORDING));

        }

    }

    private boolean getDefaultValueForProductionRecordFromParameter(final String reference) {
        Entity parameter = parameterService.getParameter();
        return parameter.getBooleanField(reference);
    }

    private String getDefaultValueForTypeOfProductionRecordingParameter(final String reference) {
        Entity parameter = parameterService.getParameter();
        return parameter.getStringField(reference);
    }

    public void checkTypeOfProductionRecording(final ViewDefinitionState viewDefinitionState) {
        FieldComponent typeOfProductionRecording = (FieldComponent) viewDefinitionState
                .getComponentByReference(TYPE_OF_PRODUCTION_RECORDING);
        if ("".equals(typeOfProductionRecording.getFieldValue())
                || BASIC.getStringValue().equals(typeOfProductionRecording.getFieldValue())) {
            for (String componentName : Arrays.asList(REGISTER_QUANTITY_IN_PRODUCT, REGISTER_QUANTITY_OUT_PRODUCT,
                    REGISTER_PRODUCTION_TIME, REGISTER_PIECEWORK, JUST_ONE, ALLOW_TO_CLOSE, AUTO_CLOSE_ORDER)) {
                FieldComponent component = (FieldComponent) viewDefinitionState.getComponentByReference(componentName);
                component.setEnabled(false);
            }
        } else if (CUMULATED.getStringValue().equals(typeOfProductionRecording.getFieldValue())) {
            FieldComponent component = (FieldComponent) viewDefinitionState.getComponentByReference(REGISTER_PIECEWORK);
            component.setFieldValue(false);
            component.setEnabled(false);
        }
    }

    public void disableFields(final ViewDefinitionState viewDefinitionState, final ComponentState componentState,
            final String[] args) {
        changeProducedQuantityFieldState(viewDefinitionState);
        FieldComponent typeOfProductionRecording = (FieldComponent) viewDefinitionState
                .getComponentByReference(TYPE_OF_PRODUCTION_RECORDING);
        if (CUMULATED.getStringValue().equals(typeOfProductionRecording.getFieldValue())
                || FOR_EACH.getStringValue().equals(typeOfProductionRecording.getFieldValue())) {
            for (String componentName : Arrays.asList(REGISTER_QUANTITY_IN_PRODUCT, REGISTER_QUANTITY_OUT_PRODUCT,
                    REGISTER_PRODUCTION_TIME, JUST_ONE, ALLOW_TO_CLOSE, AUTO_CLOSE_ORDER, REGISTER_PIECEWORK)) {
                FieldComponent component = (FieldComponent) viewDefinitionState.getComponentByReference(componentName);
                component.setEnabled(true);
            }
        }
    }

    public void changeProducedQuantityFieldState(final ViewDefinitionState viewDefinitionState) {
        FieldComponent typeOfProductionRecording = (FieldComponent) viewDefinitionState
                .getComponentByReference(TYPE_OF_PRODUCTION_RECORDING);
        FieldComponent doneQuantity = (FieldComponent) viewDefinitionState.getComponentByReference(L_DONE_QUANTITY);
        if ("".equals(typeOfProductionRecording.getFieldValue())) {
            doneQuantity.setEnabled(true);
        } else {
            doneQuantity.setEnabled(false);
        }
    }

    public void setProducedQuantity(final ViewDefinitionState view) {
        FieldComponent typeOfProductionRecording = (FieldComponent) view.getComponentByReference(TYPE_OF_PRODUCTION_RECORDING);
        FieldComponent doneQuantity = (FieldComponent) view.getComponentByReference(L_DONE_QUANTITY);
        String orderNumber = (String) view.getComponentByReference(NUMBER).getFieldValue();
        Entity order;
        List<Entity> productionCountings;

        if ("".equals(typeOfProductionRecording.getFieldValue())) {
            return;
        }

        if (orderNumber == null) {
            return;
        }
        order = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).find()
                .add(SearchRestrictions.eq(NUMBER, orderNumber)).uniqueResult();
        if (order == null) {
            return;
        }
        productionCountings = dataDefinitionService
                .get(BasicProductionCountingConstants.PLUGIN_IDENTIFIER,
                        BasicProductionCountingConstants.MODEL_BASIC_PRODUCTION_COUNTING).find()
                .add(SearchRestrictions.eq(ORDER, order)).list().getEntities();

        Entity technology = order.getBelongsToField(TECHNOLOGY);

        if (productionCountings.isEmpty()) {
            return;
        }
        for (Entity counting : productionCountings) {
            Entity aProduct = (Entity) counting.getField(L_PRODUCT);
            if (technologyService.getProductType(aProduct, technology).equals(TechnologyService.L_03_FINAL_PRODUCT)) {
                doneQuantity.setFieldValue(counting.getField("producedQuantity"));
                break;
            }
        }
    }

    public void disabledFieldWhenStateNotDraft(final ViewDefinitionState view) {
        final FormComponent form = (FormComponent) view.getComponentByReference("form");
        if (form.getEntity() == null) {
            return;
        }
        final Entity productionRecord = form.getEntity();
        final String state = productionRecord.getStringField(STATE);
        enabledOrDisabledField(view, DRAFT.getStringValue().equals(state));
    }

    private void enabledOrDisabledField(final ViewDefinitionState view, final boolean isEnabled) {
        for (String reference : Arrays.asList("lastRecord", "number", "order", "technologyInstanceOperationComponent", "staff",
                "shift", "workstationType", "division", "laborTime", "machineTime", "executedOperationCycles")) {
            final FieldComponent field = (FieldComponent) view.getComponentByReference(reference);
            field.setEnabled(isEnabled);
            field.requestComponentUpdateState();
        }
        final GridComponent recordOperationProductInComponent = (GridComponent) view
                .getComponentByReference("recordOperationProductInComponent");
        recordOperationProductInComponent.setEditable(isEnabled);
        final GridComponent recordOperationProductOutComponent = (GridComponent) view
                .getComponentByReference("recordOperationProductOutComponent");
        recordOperationProductOutComponent.setEditable(isEnabled);
        final GridComponent loggingsGrid = (GridComponent) view.getComponentByReference("loggingsGrid");
        loggingsGrid.setEditable(isEnabled);
    }

    public void filterStateChangeHistory(final ViewDefinitionState view) {
        final GridComponent historyGrid = (GridComponent) view.getComponentByReference("loggingsGrid");
        final CustomRestriction onlySuccessfulRestriction = stateChangeHistoryService.buildStatusRestriction(STATUS,
                Lists.newArrayList(SUCCESSFUL.getStringValue()));
        historyGrid.setCustomRestriction(onlySuccessfulRestriction);
    }
}
