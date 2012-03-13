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

import java.util.Arrays;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Sets;
import com.qcadoo.mes.basic.util.CurrencyService;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.productionCounting.internal.constants.CalculateOperationCostsMode;
import com.qcadoo.mes.productionCounting.internal.constants.ProductionCountingConstants;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;

@Service
public class ProductionBalanceViewService {

    private static final String L_OUTPUT_PRODUCTS_GRID = "outputProductsGrid";

    private static final String L_INPUT_PRODUCTS_GRID = "inputProductsGrid";

    private static final String L_PRODUCTION_TIME_GRID_LAYOUT = "productionTimeGridLayout";

    private static final String L_OPERATIONS_TIME_GRID = "operationsTimeGrid";

    private static final String L_ORDER = "order";

    private static final String L_TYPE_OF_PRODUCTION_RECORDING = "typeOfProductionRecording";

    @Autowired
    private CurrencyService currencyService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void fillCurrencyFields(final ViewDefinitionState viewDefinitionState) {
        final String currencyAlphabeticCode = currencyService.getCurrencyAlphabeticCode();
        generateNumber(viewDefinitionState);
        Set<String> fields = Sets.newHashSet("totalCostsCurrency", "totalOverheadCurrency", "additionalOverheadValueCurrency",
                "materialCostMarginValueCurrency", "productionCostMarginValueCurrency", "totalTechnicalProductionCostsCurrency",
                "totalPieceworkCostsCurrency", "totalLaborHourlyCostsCurrency", "totalMachineHourlyCostsCurrency",
                "totalMaterialCostsCurrency", "additionalOverheadCurrency");

        for (String componentReference : fields) {
            FieldComponent field = (FieldComponent) viewDefinitionState.getComponentByReference(componentReference);
            field.setFieldValue(currencyAlphabeticCode);
            field.requestComponentUpdateState();
        }

        FieldComponent productionCostMarginProc = (FieldComponent) viewDefinitionState
                .getComponentByReference("productionCostMarginProc");
        productionCostMarginProc.setFieldValue("%");
        productionCostMarginProc.requestComponentUpdateState();
        FieldComponent materialCostMarginProc = (FieldComponent) viewDefinitionState
                .getComponentByReference("materialCostMarginProc");
        materialCostMarginProc.setFieldValue("%");
        materialCostMarginProc.requestComponentUpdateState();
        fillCostPerUnitUnitField(viewDefinitionState, null, null);
    }

    public void fillFieldsWhenOrderChanged(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        if (!(state instanceof FieldComponent)) {
            return;
        }
        FieldComponent orderLookup = (FieldComponent) viewDefinitionState.getComponentByReference(L_ORDER);
        if (orderLookup.getFieldValue() == null) {
            clearFieldValues(viewDefinitionState);
            return;
        }
        Entity order = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).get(
                (Long) orderLookup.getFieldValue());
        if (order == null) {
            clearFieldValues(viewDefinitionState);
            return;
        }
        if (order.getStringField(L_TYPE_OF_PRODUCTION_RECORDING) == null
                || order.getStringField(L_TYPE_OF_PRODUCTION_RECORDING).equals("01none")) {
            clearFieldValues(viewDefinitionState);
            ((FieldComponent) viewDefinitionState.getComponentByReference(L_ORDER)).addMessage(
                    "productionCounting.productionBalance.report.error.orderWithoutRecordingType",
                    ComponentState.MessageType.FAILURE);
            return;
        }

        setFieldValues(viewDefinitionState, order);
    }

    public void showGrids(final ViewDefinitionState viewDefinitionState) {
        FormComponent form = (FormComponent) viewDefinitionState.getComponentByReference("form");

        if (form == null || form.getEntityId() == null) {
            setGridsVisibility(viewDefinitionState, true);
        }
    }

    private void setFieldValues(final ViewDefinitionState viewDefinitionState, final Entity order) {
        FieldComponent productField = (FieldComponent) viewDefinitionState.getComponentByReference("product");
        FieldComponent recordsNumberField = (FieldComponent) viewDefinitionState.getComponentByReference("recordsNumber");

        productField.setFieldValue(order.getBelongsToField("product").getId());

        Integer recordsNumberValue = dataDefinitionService
                .get(ProductionCountingConstants.PLUGIN_IDENTIFIER, ProductionCountingConstants.MODEL_PRODUCTION_RECORD).find()
                .add(SearchRestrictions.belongsTo("order", order)).list().getEntities().size();

        recordsNumberField.setFieldValue(recordsNumberValue);
    }

    private void clearFieldValues(final ViewDefinitionState viewDefinitionState) {
        FieldComponent product = (FieldComponent) viewDefinitionState.getComponentByReference("product");
        product.setFieldValue(null);
        FieldComponent recordsNumber = (FieldComponent) viewDefinitionState.getComponentByReference("recordsNumber");
        recordsNumber.setFieldValue(null);
    }

    private void setGridsVisibility(final ViewDefinitionState viewDefinitionState, final Boolean isVisible) {
        viewDefinitionState.getComponentByReference(L_INPUT_PRODUCTS_GRID).setVisible(isVisible);
        viewDefinitionState.getComponentByReference(L_OUTPUT_PRODUCTS_GRID).setVisible(isVisible);
        viewDefinitionState.getComponentByReference(L_OPERATIONS_TIME_GRID).setVisible(isVisible);
        viewDefinitionState.getComponentByReference(L_PRODUCTION_TIME_GRID_LAYOUT).setVisible(isVisible);
    }

    public void disableFieldsWhenGenerated(final ViewDefinitionState view) {
        Boolean enabled = false;
        ComponentState generated = view.getComponentByReference("generated");
        if (generated == null || generated.getFieldValue() == null || "0".equals(generated.getFieldValue())) {
            enabled = true;
        }
        for (String reference : Arrays.asList("order", "name", "description")) {
            FieldComponent component = (FieldComponent) view.getComponentByReference(reference);
            component.setEnabled(enabled);
        }
    }

    public void disableCheckboxes(final ViewDefinitionState viewDefinitionState, final ComponentState state, final String[] args) {
        FieldComponent calculateOperationCostsMode = (FieldComponent) viewDefinitionState
                .getComponentByReference("calculateOperationCostsMode");
        FieldComponent includeTPZ = (FieldComponent) viewDefinitionState.getComponentByReference("includeTPZ");
        FieldComponent includeAdditionalTime = (FieldComponent) viewDefinitionState
                .getComponentByReference("includeAdditionalTime");

        if (CalculateOperationCostsMode.PIECEWORK.getStringValue().equals(calculateOperationCostsMode.getFieldValue())) {
            includeTPZ.setFieldValue(false);
            includeTPZ.setEnabled(false);
            includeTPZ.requestComponentUpdateState();

            includeAdditionalTime.setFieldValue(false);
            includeAdditionalTime.setEnabled(false);
            includeAdditionalTime.requestComponentUpdateState();
        } else {
            includeTPZ.setEnabled(true);
            includeTPZ.setFieldValue(true);
            includeTPZ.requestComponentUpdateState();

            includeAdditionalTime.setEnabled(true);
            includeTPZ.setFieldValue(true);
            includeAdditionalTime.requestComponentUpdateState();
        }
    }
}
