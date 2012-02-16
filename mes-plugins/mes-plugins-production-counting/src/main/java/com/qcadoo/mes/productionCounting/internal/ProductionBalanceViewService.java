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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.productionCounting.internal.constants.ProductionCountingConstants;
import com.qcadoo.mes.productionCounting.internal.print.utils.EntityProductInOutComparator;
import com.qcadoo.mes.productionCounting.internal.print.utils.EntityProductionRecordOperationComparator;
import com.qcadoo.mes.productionCounting.internal.states.ProductionCountingStates;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.api.utils.TimeConverterService;

@Service
public class ProductionBalanceViewService {

    private static final String COMPONENT_INPUT_PRODUCTS_GRID = "inputProductsGrid";

    private static final String COMPONENT_REGISTER_PRODUCTION_TIME = "registerProductionTime";

    private static final String COMPONENT_STATE = "state";

    private static final String COMPONENT_PRODUCTION_TIME_GRID_LAYOUT = "productionTimeGridLayout";

    private static final String COMPONENT_OPERATIONS_TIME_GRID = "operationsTimeGrid";

    private static final String FIELD_TYPE_OF_PRODUCTION_RECORDING = "typeOfProductionRecording";

    private static final String FIELD_ORDER = "order";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private ProductionBalanceReportDataService productionBalanceReportDataService;

    @Autowired
    private TimeConverterService timeConverterService;

    @Autowired
    private NumberService numberService;

    public void fillFieldsWhenOrderChanged(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        if (!(state instanceof FieldComponent)) {
            return;
        }
        FieldComponent orderLookup = (FieldComponent) viewDefinitionState.getComponentByReference(FIELD_ORDER);
        if (orderLookup.getFieldValue() == null) {
            setGridsVisibility(viewDefinitionState, false);
            clearFieldValues(viewDefinitionState);
            return;
        }
        Entity order = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).get(
                (Long) orderLookup.getFieldValue());
        if (order == null) {
            setGridsVisibility(viewDefinitionState, false);
            clearFieldValues(viewDefinitionState);
            return;
        }
        if (order.getStringField(FIELD_TYPE_OF_PRODUCTION_RECORDING) == null
                || order.getStringField(FIELD_TYPE_OF_PRODUCTION_RECORDING).equals("01none")) {
            setGridsVisibility(viewDefinitionState, false);
            clearFieldValues(viewDefinitionState);
            ((FieldComponent) viewDefinitionState.getComponentByReference(FIELD_ORDER)).addMessage(
                    "productionCounting.productionBalance.report.error.orderWithoutRecordingType",
                    ComponentState.MessageType.FAILURE);
            return;
        }

        setFieldValues(viewDefinitionState, order);
        setGridsContent(viewDefinitionState, order);
    }

    public void fillGrids(final ViewDefinitionState viewDefinitionState) {
        FieldComponent orderLookup = (FieldComponent) viewDefinitionState.getComponentByReference(FIELD_ORDER);
        if (orderLookup.getFieldValue() == null) {
            setGridsVisibility(viewDefinitionState, false);
            return;
        }
        Entity order = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).get(
                (Long) orderLookup.getFieldValue());
        if (order == null) {
            setGridsVisibility(viewDefinitionState, false);
            return;
        }
        if (order.getStringField(FIELD_TYPE_OF_PRODUCTION_RECORDING) == null
                || order.getStringField(FIELD_TYPE_OF_PRODUCTION_RECORDING).equals("01none")) {
            setGridsVisibility(viewDefinitionState, false);
            clearFieldValues(viewDefinitionState);
            return;
        }

        setGridsContent(viewDefinitionState, order);
    }

    private void setFieldValues(final ViewDefinitionState viewDefinitionState, final Entity order) {
        FieldComponent productField = (FieldComponent) viewDefinitionState.getComponentByReference("product");
        productField.setFieldValue(order.getBelongsToField("product").getId());
        FieldComponent recordsNumberField = (FieldComponent) viewDefinitionState.getComponentByReference("recordsNumber");
        Integer recordsNumberValue = dataDefinitionService
                .get(ProductionCountingConstants.PLUGIN_IDENTIFIER, ProductionCountingConstants.MODEL_PRODUCTION_COUNTING)
                .find("where order.id=" + order.getId().toString()).list().getEntities().size();
        recordsNumberField.setFieldValue(recordsNumberValue);
    }

    private void setGridsContent(final ViewDefinitionState viewDefinitionState, final Entity order) {
        if ((Boolean) order.getField("registerQuantityInProduct")) {
            setInputProductsGridContent(viewDefinitionState, order);
        } else {
            viewDefinitionState.getComponentByReference(COMPONENT_INPUT_PRODUCTS_GRID).setVisible(false);
        }
        if ((Boolean) order.getField("registerQuantityOutProduct")) {
            setOutputProductsGridContent(viewDefinitionState, order);
        } else {
            viewDefinitionState.getComponentByReference("outputProductsGrid").setVisible(false);
        }
        if ((Boolean) order.getField(COMPONENT_REGISTER_PRODUCTION_TIME)) {
            setProductionTimeTabContent(viewDefinitionState, order);
        } else {
            viewDefinitionState.getComponentByReference(COMPONENT_OPERATIONS_TIME_GRID).setVisible(false);
            viewDefinitionState.getComponentByReference(COMPONENT_PRODUCTION_TIME_GRID_LAYOUT).setVisible(false);
        }
    }

    private void clearFieldValues(final ViewDefinitionState viewDefinitionState) {
        FieldComponent product = (FieldComponent) viewDefinitionState.getComponentByReference("product");
        product.setFieldValue(null);
        FieldComponent recordsNumber = (FieldComponent) viewDefinitionState.getComponentByReference("recordsNumber");
        recordsNumber.setFieldValue(null);
    }

    private void setGridsVisibility(final ViewDefinitionState viewDefinitionState, final Boolean isVisible) {
        viewDefinitionState.getComponentByReference(COMPONENT_INPUT_PRODUCTS_GRID).setVisible(isVisible);
        viewDefinitionState.getComponentByReference("outputProductsGrid").setVisible(isVisible);
        viewDefinitionState.getComponentByReference(COMPONENT_OPERATIONS_TIME_GRID).setVisible(isVisible);
        viewDefinitionState.getComponentByReference(COMPONENT_PRODUCTION_TIME_GRID_LAYOUT).setVisible(isVisible);
    }

    private void setInputProductsGridContent(final ViewDefinitionState viewDefinitionState, final Entity order) {
        GridComponent inputProducts = (GridComponent) viewDefinitionState.getComponentByReference(COMPONENT_INPUT_PRODUCTS_GRID);
        List<Entity> inputProductsList = new ArrayList<Entity>();
        List<Entity> productionRecordList = dataDefinitionService
                .get(ProductionCountingConstants.PLUGIN_IDENTIFIER, ProductionCountingConstants.MODEL_PRODUCTION_RECORD).find()
                .add(SearchRestrictions.eq(COMPONENT_STATE, ProductionCountingStates.ACCEPTED.getStringValue()))
                .add(SearchRestrictions.belongsTo(FIELD_ORDER, order)).list().getEntities();
        for (Entity productionRecord : productionRecordList) {
            inputProductsList.addAll(productionRecord.getHasManyField("recordOperationProductInComponents"));
        }
        if (!inputProductsList.isEmpty()) {
            Collections.sort(inputProductsList, new EntityProductInOutComparator());
            inputProducts.setEntities(productionBalanceReportDataService.groupProductInOutComponentsByProduct(inputProductsList));
            inputProducts.setEntities(inputProductsList);
            inputProducts.setVisible(true);
        }
    }

    private void setOutputProductsGridContent(final ViewDefinitionState viewDefinitionState, final Entity order) {
        GridComponent outputProducts = (GridComponent) viewDefinitionState.getComponentByReference("outputProductsGrid");
        List<Entity> outputProductsList = new ArrayList<Entity>();
        List<Entity> productionRecordList = dataDefinitionService
                .get(ProductionCountingConstants.PLUGIN_IDENTIFIER, ProductionCountingConstants.MODEL_PRODUCTION_RECORD).find()
                .add(SearchRestrictions.eq(COMPONENT_STATE, ProductionCountingStates.ACCEPTED.getStringValue()))
                .add(SearchRestrictions.belongsTo(FIELD_ORDER, order)).list().getEntities();
        for (Entity productionRecord : productionRecordList) {
            outputProductsList.addAll(productionRecord.getHasManyField("recordOperationProductOutComponents"));
        }
        if (!outputProductsList.isEmpty()) {
            Collections.sort(outputProductsList, new EntityProductInOutComparator());
            outputProducts.setEntities(productionBalanceReportDataService
                    .groupProductInOutComponentsByProduct(outputProductsList));
            outputProducts.setVisible(true);
        }
    }

    private void setProductionTimeTabContent(final ViewDefinitionState viewDefinitionState, final Entity order) {
        if (order.getStringField(FIELD_TYPE_OF_PRODUCTION_RECORDING).equals("03forEach")) {
            viewDefinitionState.getComponentByReference(COMPONENT_OPERATIONS_TIME_GRID).setVisible(true);
            viewDefinitionState.getComponentByReference(COMPONENT_PRODUCTION_TIME_GRID_LAYOUT).setVisible(false);
            setProductionTimeGridContent(viewDefinitionState, order);
        } else if (order.getStringField(FIELD_TYPE_OF_PRODUCTION_RECORDING).equals("02cumulated")) {
            viewDefinitionState.getComponentByReference(COMPONENT_OPERATIONS_TIME_GRID).setVisible(false);
            viewDefinitionState.getComponentByReference(COMPONENT_PRODUCTION_TIME_GRID_LAYOUT).setVisible(true);
            setTimeValues(viewDefinitionState, order);
        }
    }

    private void setTimeValues(final ViewDefinitionState viewDefinitionState, final Entity order) {
        BigDecimal plannedTime;
        BigDecimal machinePlannedTime = BigDecimal.ZERO;
        BigDecimal laborPlannedTime = BigDecimal.ZERO;

        List<Entity> orderOperationComponents = order.getTreeField("orderOperationComponents");
        for (Entity orderOperationComponent : orderOperationComponents) {
            plannedTime = ((BigDecimal) orderOperationComponent.getField("productionInOneCycle")).multiply(
                    new BigDecimal((Integer) orderOperationComponent.getField("tj")), numberService.getMathContext()).add(
                    new BigDecimal((Integer) orderOperationComponent.getField("tpz")), numberService.getMathContext());
            machinePlannedTime = machinePlannedTime.add(
                    plannedTime.multiply((BigDecimal) orderOperationComponent.getField("machineUtilization"),
                            numberService.getMathContext()), numberService.getMathContext());
            laborPlannedTime = laborPlannedTime.add(
                    plannedTime.multiply((BigDecimal) orderOperationComponent.getField("laborUtilization"),
                            numberService.getMathContext()), numberService.getMathContext());
        }

        BigDecimal machineRegisteredTime = BigDecimal.ZERO;
        BigDecimal laborRegisteredTime = BigDecimal.ZERO;
        List<Entity> productionRecordsList = dataDefinitionService
                .get(ProductionCountingConstants.PLUGIN_IDENTIFIER, ProductionCountingConstants.MODEL_PRODUCTION_RECORD).find()
                .add(SearchRestrictions.eq(COMPONENT_STATE, ProductionCountingStates.ACCEPTED.getStringValue()))
                .add(SearchRestrictions.belongsTo(FIELD_ORDER, order)).list().getEntities();
        for (Entity productionRecord : productionRecordsList) {
            machineRegisteredTime = machineRegisteredTime.add(new BigDecimal((Integer) productionRecord.getField("machineTime")),
                    numberService.getMathContext());
            laborRegisteredTime = laborRegisteredTime.add(new BigDecimal((Integer) productionRecord.getField("laborTime")),
                    numberService.getMathContext());
        }

        BigDecimal machineTimeBalance = machineRegisteredTime.subtract(machinePlannedTime, numberService.getMathContext());
        BigDecimal laborTimeBalance = laborRegisteredTime.subtract(laborPlannedTime, numberService.getMathContext());

        FieldComponent machinePlannedTimeField = (FieldComponent) viewDefinitionState
                .getComponentByReference("machinePlannedTime");
        machinePlannedTimeField.setFieldValue(timeConverterService.convertTimeToString(machinePlannedTime.intValue()));
        machinePlannedTimeField.requestComponentUpdateState();

        FieldComponent machineRegisteredTimeField = (FieldComponent) viewDefinitionState
                .getComponentByReference("machineRegisteredTime");
        machineRegisteredTimeField.setFieldValue(timeConverterService.convertTimeToString(machineRegisteredTime.intValue()));
        machineRegisteredTimeField.requestComponentUpdateState();

        FieldComponent machineTimeBalanceField = (FieldComponent) viewDefinitionState
                .getComponentByReference("machineTimeBalance");
        machineTimeBalanceField.setFieldValue(timeConverterService.convertTimeToString(machineTimeBalance.intValue()));
        machineTimeBalanceField.requestComponentUpdateState();

        FieldComponent laborPlannedTimeField = (FieldComponent) viewDefinitionState.getComponentByReference("laborPlannedTime");
        laborPlannedTimeField.setFieldValue(timeConverterService.convertTimeToString(laborPlannedTime.intValue()));
        laborPlannedTimeField.requestComponentUpdateState();

        FieldComponent laborRegisteredTimeField = (FieldComponent) viewDefinitionState
                .getComponentByReference("laborRegisteredTime");
        laborRegisteredTimeField.setFieldValue(timeConverterService.convertTimeToString(laborRegisteredTime.intValue()));
        laborRegisteredTimeField.requestComponentUpdateState();

        FieldComponent laborTimeBalanceField = (FieldComponent) viewDefinitionState.getComponentByReference("laborTimeBalance");
        laborTimeBalanceField.setFieldValue(timeConverterService.convertTimeToString(laborTimeBalance.intValue()));
        laborTimeBalanceField.requestComponentUpdateState();
    }

    private void setProductionTimeGridContent(final ViewDefinitionState viewDefinitionState, final Entity order) {
        GridComponent productionsTime = (GridComponent) viewDefinitionState
                .getComponentByReference(COMPONENT_OPERATIONS_TIME_GRID);
        List<Entity> productionRecordsList = dataDefinitionService
                .get(ProductionCountingConstants.PLUGIN_IDENTIFIER, ProductionCountingConstants.MODEL_PRODUCTION_RECORD).find()
                .add(SearchRestrictions.eq(COMPONENT_STATE, ProductionCountingStates.ACCEPTED.getStringValue()))
                .add(SearchRestrictions.belongsTo(FIELD_ORDER, order)).list().getEntities();
        if (!productionRecordsList.isEmpty()) {
            Collections.sort(productionRecordsList, new EntityProductionRecordOperationComparator());
            productionsTime.setEntities(productionBalanceReportDataService
                    .groupProductionRecordsByOperation(productionRecordsList));
            productionsTime.setVisible(true);
        }
    }

    public void disableFieldsWhenGenerated(final ViewDefinitionState view) {
        Boolean enabled = false;
        ComponentState generated = view.getComponentByReference("generated");
        if (generated == null || generated.getFieldValue() == null || "0".equals(generated.getFieldValue())) {
            enabled = true;
        }
        for (String reference : Arrays.asList(FIELD_ORDER, "name", "description")) {
            FieldComponent component = (FieldComponent) view.getComponentByReference(reference);
            component.setEnabled(enabled);
        }
    }
}
