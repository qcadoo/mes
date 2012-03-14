/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.3
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

import static com.qcadoo.mes.productionCounting.internal.constants.ProductionBalanceFields.CALCULATE_OPERATION_COST_MODE;
import static com.qcadoo.mes.productionCounting.internal.constants.ProductionBalanceFields.DESCRIPTION;
import static com.qcadoo.mes.productionCounting.internal.constants.ProductionBalanceFields.INCLUDE_ADDITIONAL_TIME;
import static com.qcadoo.mes.productionCounting.internal.constants.ProductionBalanceFields.INCLUDE_TPZ;
import static com.qcadoo.mes.productionCounting.internal.constants.ProductionBalanceFields.NAME;
import static com.qcadoo.mes.productionCounting.internal.constants.ProductionBalanceFields.ORDER;
import static com.qcadoo.mes.productionCounting.internal.constants.ProductionBalanceFields.PRINT_OPERATION_NORMS;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.productionCounting.internal.constants.CalculateOperationCostsMode;
import com.qcadoo.mes.productionCounting.internal.constants.ProductionBalanceFields;
import com.qcadoo.mes.productionCounting.internal.constants.ProductionCountingConstants;
import com.qcadoo.mes.productionCounting.internal.constants.ProductionRecordFields;
import com.qcadoo.mes.productionCounting.internal.constants.TypeOfProductionRecording;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;

@Service
public class ProductionBalanceViewService {

    private static final String L_TIME_GRID_LAYOUT = "timeGridLayout";

    private static final String L_INPUT_PRODUCTS_GRID = "inputProductsGrid";

    private static final String L_OUTPUT_PRODUCTS_GRID = "outputProductsGrid";

    private static final String L_LABOR_TIME_BORDER_LAYOUT = "laborTimeBorderLayout";

    private static final String L_MACHINE_TIME_BORDER_LAYOUT = "machineTimeBorderLayout";

    private static final String L_OPERATIONS_TIME_GRID = "operationsTimeGrid";

    private static final String L_TYPE_OF_PRODUCTION_RECORDING = "typeOfProductionRecording";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private ProductionBalanceService productionBalanceService;

    public void changeFieldsAndGridsVisibility(final ViewDefinitionState viewDefinitionState) {
        FormComponent form = (FormComponent) viewDefinitionState.getComponentByReference("form");

        if (form == null || form.getEntityId() == null) {
            viewDefinitionState.getComponentByReference(L_INPUT_PRODUCTS_GRID).setVisible(false);
            viewDefinitionState.getComponentByReference(L_OUTPUT_PRODUCTS_GRID).setVisible(false);

            viewDefinitionState.getComponentByReference(L_TIME_GRID_LAYOUT).setVisible(false);

            viewDefinitionState.getComponentByReference(L_MACHINE_TIME_BORDER_LAYOUT).setVisible(false);
            viewDefinitionState.getComponentByReference(L_LABOR_TIME_BORDER_LAYOUT).setVisible(false);
            viewDefinitionState.getComponentByReference(L_OPERATIONS_TIME_GRID).setVisible(false);
        }

        FieldComponent generated = (FieldComponent) viewDefinitionState
                .getComponentByReference(ProductionBalanceFields.GENERATED);

        FieldComponent calculateOperationCostMode = (FieldComponent) viewDefinitionState
                .getComponentByReference(ProductionBalanceFields.CALCULATE_OPERATION_COST_MODE);

        FieldComponent orderLookup = (FieldComponent) viewDefinitionState.getComponentByReference(ProductionBalanceFields.ORDER);

        Long orderId = (Long) orderLookup.getFieldValue();

        if (orderId == null) {
            return;
        }

        Entity order = getOrderFromDB(orderId);

        if ((generated != null) && "1".equals(generated.getFieldValue()) && (calculateOperationCostMode != null)
                && (order != null)) {
            if (order.getBooleanField(ProductionCountingConstants.PARAM_REGISTER_IN_PRODUCTS)) {
                viewDefinitionState.getComponentByReference(L_INPUT_PRODUCTS_GRID).setVisible(true);
            }

            if (order.getBooleanField(ProductionCountingConstants.PARAM_REGISTER_OUT_PRODUCTS)) {
                viewDefinitionState.getComponentByReference(L_OUTPUT_PRODUCTS_GRID).setVisible(true);
            }

            if (CalculateOperationCostsMode.HOURLY.getStringValue().equals(calculateOperationCostMode.getFieldValue())
                    && order.getBooleanField(ProductionCountingConstants.PARAM_REGISTER_TIME)) {
                viewDefinitionState.getComponentByReference(L_TIME_GRID_LAYOUT).setVisible(true);

                if (TypeOfProductionRecording.FOR_EACH.getStringValue().equals(
                        order.getStringField(L_TYPE_OF_PRODUCTION_RECORDING))) {
                    viewDefinitionState.getComponentByReference(L_MACHINE_TIME_BORDER_LAYOUT).setVisible(true);
                    viewDefinitionState.getComponentByReference(L_LABOR_TIME_BORDER_LAYOUT).setVisible(true);
                    viewDefinitionState.getComponentByReference(L_OPERATIONS_TIME_GRID).setVisible(true);
                } else if (TypeOfProductionRecording.CUMULATED.getStringValue().equals(
                        order.getStringField(L_TYPE_OF_PRODUCTION_RECORDING))) {
                    viewDefinitionState.getComponentByReference(L_MACHINE_TIME_BORDER_LAYOUT).setVisible(true);
                    viewDefinitionState.getComponentByReference(L_LABOR_TIME_BORDER_LAYOUT).setVisible(true);
                    viewDefinitionState.getComponentByReference(L_OPERATIONS_TIME_GRID).setVisible(false);
                }
            }

        }
    }

    public void disableCheckboxes(final ViewDefinitionState viewDefinitionState, final ComponentState state, final String[] args) {
        disableCheckboxes(viewDefinitionState);
    }

    public void disableCheckboxes(final ViewDefinitionState viewDefinitionState) {
        FieldComponent calculateOperationCostsMode = (FieldComponent) viewDefinitionState
                .getComponentByReference(CALCULATE_OPERATION_COST_MODE);

        FieldComponent includeTPZ = (FieldComponent) viewDefinitionState.getComponentByReference(INCLUDE_TPZ);
        FieldComponent includeAdditionalTime = (FieldComponent) viewDefinitionState
                .getComponentByReference(INCLUDE_ADDITIONAL_TIME);

        if (CalculateOperationCostsMode.PIECEWORK.getStringValue().equals(calculateOperationCostsMode.getFieldValue())) {
            includeTPZ.setFieldValue(false);
            includeTPZ.setEnabled(false);
            includeTPZ.requestComponentUpdateState();

            includeAdditionalTime.setFieldValue(false);
            includeAdditionalTime.setEnabled(false);
            includeAdditionalTime.requestComponentUpdateState();
        } else {
            includeTPZ.setEnabled(true);
            includeTPZ.requestComponentUpdateState();

            includeAdditionalTime.setEnabled(true);
            includeAdditionalTime.requestComponentUpdateState();
        }
    }

    public void disableFieldsAndGridsWhenGenerated(final ViewDefinitionState viewDefinitionState) {
        FieldComponent generated = (FieldComponent) viewDefinitionState
                .getComponentByReference(ProductionBalanceFields.GENERATED);

        if ((generated != null) && (generated.getFieldValue() != null) && "1".equals(generated.getFieldValue())) {
            for (String fieldName : Arrays.asList(ORDER, NAME, DESCRIPTION, PRINT_OPERATION_NORMS, CALCULATE_OPERATION_COST_MODE,
                    INCLUDE_TPZ, INCLUDE_ADDITIONAL_TIME)) {
                FieldComponent fieldComponent = (FieldComponent) viewDefinitionState.getComponentByReference(fieldName);
                fieldComponent.setEnabled(false);
                fieldComponent.requestComponentUpdateState();
            }

            viewDefinitionState.getComponentByReference(L_INPUT_PRODUCTS_GRID).setEnabled(false);
            viewDefinitionState.getComponentByReference(L_OUTPUT_PRODUCTS_GRID).setEnabled(false);
        }
    }

    public void fillProductAndRecordsNumber(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        FieldComponent orderLookup = (FieldComponent) viewDefinitionState.getComponentByReference(ProductionBalanceFields.ORDER);

        Long orderId = (Long) viewDefinitionState.getComponentByReference(ProductionBalanceFields.ORDER).getFieldValue();

        if (orderId == null) {
            clearProductAndRecordsNumber(viewDefinitionState);

            return;
        }

        Entity order = getOrderFromDB(orderId);

        if (order == null) {
            clearProductAndRecordsNumber(viewDefinitionState);
            return;
        }

        if (productionBalanceService.checkIfTypeOfProductionRecordingIsBasic(order)) {
            clearProductAndRecordsNumber(viewDefinitionState);

            orderLookup.addMessage("productionCounting.productionBalance.report.error.orderWithoutRecordingType",
                    ComponentState.MessageType.FAILURE);

            return;
        }

        fillProductAndRecordsNumber(viewDefinitionState, order);
    }

    private void fillProductAndRecordsNumber(final ViewDefinitionState viewDefinitionState, final Entity order) {
        FieldComponent productField = (FieldComponent) viewDefinitionState
                .getComponentByReference(ProductionBalanceFields.PRODUCT);
        FieldComponent recordsNumberField = (FieldComponent) viewDefinitionState
                .getComponentByReference(ProductionBalanceFields.RECORDS_NUMBER);

        Entity product = order.getBelongsToField("product");
        Integer recordsNumber = dataDefinitionService
                .get(ProductionCountingConstants.PLUGIN_IDENTIFIER, ProductionCountingConstants.MODEL_PRODUCTION_RECORD).find()
                .add(SearchRestrictions.belongsTo(ProductionRecordFields.ORDER, order)).list().getEntities().size();

        productField.setFieldValue(product.getId());
        recordsNumberField.setFieldValue(recordsNumber);
    }

    private void clearProductAndRecordsNumber(final ViewDefinitionState viewDefinitionState) {
        FieldComponent productField = (FieldComponent) viewDefinitionState
                .getComponentByReference(ProductionBalanceFields.PRODUCT);
        FieldComponent recordsNumberField = (FieldComponent) viewDefinitionState
                .getComponentByReference(ProductionBalanceFields.RECORDS_NUMBER);

        productField.setFieldValue(null);
        recordsNumberField.setFieldValue(null);
    }

    private Entity getOrderFromDB(Long orderId) {
        return dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).get(orderId);
    }
}
