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
package com.qcadoo.mes.productionCountingWithCosts.hooks;

import static com.qcadoo.mes.productionCountingWithCosts.constants.ProductionBalanceFieldsPCWC.ADDITIONAL_OVERHEAD;
import static com.qcadoo.mes.productionCountingWithCosts.constants.ProductionBalanceFieldsPCWC.AVERAGE_LABOR_HOURLY_COST;
import static com.qcadoo.mes.productionCountingWithCosts.constants.ProductionBalanceFieldsPCWC.AVERAGE_MACHINE_HOURLY_COST;
import static com.qcadoo.mes.productionCountingWithCosts.constants.ProductionBalanceFieldsPCWC.CALCULATE_MATERIAL_COSTS_MODE;
import static com.qcadoo.mes.productionCountingWithCosts.constants.ProductionBalanceFieldsPCWC.MATERIAL_COST_MARGIN;
import static com.qcadoo.mes.productionCountingWithCosts.constants.ProductionBalanceFieldsPCWC.PRINT_COST_NORMS_OF_MATERIALS;
import static com.qcadoo.mes.productionCountingWithCosts.constants.ProductionBalanceFieldsPCWC.PRODUCTION_COST_MARGIN;
import static com.qcadoo.mes.productionCountingWithCosts.constants.ProductionBalanceFieldsPCWC.SOURCE_OF_MATERIAL_COSTS;

import java.util.Arrays;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Sets;
import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.basic.util.CurrencyService;
import com.qcadoo.mes.costCalculation.constants.CalculateMaterialCostsMode;
import com.qcadoo.mes.costCalculation.constants.SourceOfMaterialCosts;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.productionCounting.internal.constants.CalculateOperationCostsMode;
import com.qcadoo.mes.productionCounting.internal.constants.ProductionBalanceFields;
import com.qcadoo.mes.productionCounting.internal.constants.TypeOfProductionRecording;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ComponentState.MessageType;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;

@Service
public class ProductionBalanceDetailsViewHooks {

    private static final String L_TIME_COSTS_GRID_LAYOUT = "timeCostsGridLayout";

    private static final String L_MATERIAL_COSTS_GRID_LAYOUT = "materialCostsGridLayout";

    private static final String L_COMPONENTS_COST_SUMMARY_BORDER_LAYOUT = "componentsCostSummaryBorderLayout";

    private static final String L_ORDER_OPERATION_PRODUCT_IN_COMPONENTS = "orderOperationProductInComponents";

    private static final String L_MACHINE_COSTS_BORDER_LAYOUT = "machineCostsBorderLayout";

    private static final String L_LABOR_COSTS_BORDER_LAYOUT = "laborCostsBorderLayout";

    private static final String L_OPERATIONS_COST_GRID = "operationsCostGrid";

    private static final String L_TYPE_OF_PRODUCTION_RECORDING = "typeOfProductionRecording";

    private static final String L_ASSUMPTIONS_BORDER_LAYOUT = "assumptionsBorderLayout";

    @Autowired
    private CurrencyService currencyService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void changeOtherFieldsAndGridsVisibility(final ViewDefinitionState viewDefinitionState) {
        FormComponent form = (FormComponent) viewDefinitionState.getComponentByReference("form");

        if (form == null || form.getEntityId() == null) {
            viewDefinitionState.getComponentByReference(L_MATERIAL_COSTS_GRID_LAYOUT).setVisible(false);

            viewDefinitionState.getComponentByReference(L_COMPONENTS_COST_SUMMARY_BORDER_LAYOUT).setVisible(false);
            viewDefinitionState.getComponentByReference(L_ORDER_OPERATION_PRODUCT_IN_COMPONENTS).setVisible(false);

            viewDefinitionState.getComponentByReference(L_TIME_COSTS_GRID_LAYOUT).setVisible(false);

            viewDefinitionState.getComponentByReference(L_MACHINE_COSTS_BORDER_LAYOUT).setVisible(false);
            viewDefinitionState.getComponentByReference(L_LABOR_COSTS_BORDER_LAYOUT).setVisible(false);
            viewDefinitionState.getComponentByReference(L_OPERATIONS_COST_GRID).setVisible(false);
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
            viewDefinitionState.getComponentByReference(L_MATERIAL_COSTS_GRID_LAYOUT).setVisible(true);

            viewDefinitionState.getComponentByReference(L_COMPONENTS_COST_SUMMARY_BORDER_LAYOUT).setVisible(true);
            viewDefinitionState.getComponentByReference(L_ORDER_OPERATION_PRODUCT_IN_COMPONENTS).setVisible(true);

            if (CalculateOperationCostsMode.HOURLY.getStringValue().equals(calculateOperationCostMode.getFieldValue())) {
                viewDefinitionState.getComponentByReference(L_TIME_COSTS_GRID_LAYOUT).setVisible(true);

                if (TypeOfProductionRecording.FOR_EACH.getStringValue().equals(
                        order.getStringField(L_TYPE_OF_PRODUCTION_RECORDING))) {
                    viewDefinitionState.getComponentByReference(L_MACHINE_COSTS_BORDER_LAYOUT).setVisible(true);
                    viewDefinitionState.getComponentByReference(L_LABOR_COSTS_BORDER_LAYOUT).setVisible(true);
                    viewDefinitionState.getComponentByReference(L_OPERATIONS_COST_GRID).setVisible(true);
                } else if (TypeOfProductionRecording.CUMULATED.getStringValue().equals(
                        order.getStringField(L_TYPE_OF_PRODUCTION_RECORDING))) {
                    viewDefinitionState.getComponentByReference(L_MACHINE_COSTS_BORDER_LAYOUT).setVisible(true);
                    viewDefinitionState.getComponentByReference(L_LABOR_COSTS_BORDER_LAYOUT).setVisible(true);
                    viewDefinitionState.getComponentByReference(L_OPERATIONS_COST_GRID).setVisible(false);
                }
            }
        }
    }

    public void changeAssumptionsVisibility(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        changeAssumptionsVisibility(viewDefinitionState);
    }

    public void changeAssumptionsVisibility(final ViewDefinitionState viewDefinitionState) {
        ComponentState assumptionsBorderLayout = viewDefinitionState.getComponentByReference(L_ASSUMPTIONS_BORDER_LAYOUT);

        FieldComponent calculateOperationCostMode = (FieldComponent) viewDefinitionState
                .getComponentByReference(ProductionBalanceFields.CALCULATE_OPERATION_COST_MODE);

        FieldComponent orderLookup = (FieldComponent) viewDefinitionState.getComponentByReference(ProductionBalanceFields.ORDER);

        Long orderId = (Long) orderLookup.getFieldValue();

        if (orderId == null) {
            assumptionsBorderLayout.setVisible(false);

            return;
        }

        Entity order = getOrderFromDB(orderId);

        if ((order != null)
                && CalculateOperationCostsMode.HOURLY.getStringValue().equals(calculateOperationCostMode.getFieldValue())
                && TypeOfProductionRecording.CUMULATED.getStringValue().equals(
                        order.getStringField(L_TYPE_OF_PRODUCTION_RECORDING))) {
            assumptionsBorderLayout.setVisible(true);
        } else {
            assumptionsBorderLayout.setVisible(false);
        }
    }

    public void fillCurrencyAndUnitFields(final ViewDefinitionState viewDefinitionState) {
        String currencyAlphabeticCode = currencyService.getCurrencyAlphabeticCode();

        Set<String> currencyFieldNames = Sets.newHashSet("averageMachineHourlyCostCurrency", "averageLaborHourlyCostCurrency",
                "additionalOverheadCurrency", "registeredTotalTechnicalProductionCostsCurrency",
                "totalTechnicalProductionCostsCurrency", "balanceTechnicalProductionCostsCurrency",
                "productionCostMarginValueCurrency", "materialCostMarginValueCurrency", "additionalOverheadValueCurrency",
                "totalOverheadCurrency", "totalCostsCurrency");

        for (String currencyFieldName : currencyFieldNames) {
            FieldComponent fieldComponent = (FieldComponent) viewDefinitionState.getComponentByReference(currencyFieldName);
            fieldComponent.setFieldValue(currencyAlphabeticCode);
            fieldComponent.requestComponentUpdateState();
        }

        FieldComponent productionCostMarginProc = (FieldComponent) viewDefinitionState
                .getComponentByReference("productionCostMarginProc");
        productionCostMarginProc.setFieldValue("%");
        productionCostMarginProc.requestComponentUpdateState();

        FieldComponent materialCostMarginProc = (FieldComponent) viewDefinitionState
                .getComponentByReference("materialCostMarginProc");
        materialCostMarginProc.setFieldValue("%");
        materialCostMarginProc.requestComponentUpdateState();

        Set<String> unitFieldNames = Sets.newHashSet("registeredTotalTechnicalProductionCostPerUnitUnit",
                "totalTechnicalProductionCostPerUnitUnit", "balanceTechnicalProductionCostPerUnitUnit", "totalCostPerUnitUnit");

        Long productId = (Long) viewDefinitionState.getComponentByReference("product").getFieldValue();

        if (productId == null) {
            return;
        }

        Entity product = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_PRODUCT).get(productId);

        String unit = product.getStringField("unit");

        for (String unitFieldName : unitFieldNames) {
            FieldComponent fieldComponent = (FieldComponent) viewDefinitionState.getComponentByReference(unitFieldName);
            fieldComponent.setFieldValue(currencyAlphabeticCode + "/" + unit);
            fieldComponent.requestComponentUpdateState();
        }

    }

    public void disableOtherFieldsAndGridsWhenGenerated(final ViewDefinitionState viewDefinitionState) {
        FieldComponent generated = (FieldComponent) viewDefinitionState
                .getComponentByReference(ProductionBalanceFields.GENERATED);

        if ((generated != null) && (generated.getFieldValue() != null) && "1".equals(generated.getFieldValue())) {
            for (String fieldName : Arrays.asList(PRINT_COST_NORMS_OF_MATERIALS, SOURCE_OF_MATERIAL_COSTS,
                    CALCULATE_MATERIAL_COSTS_MODE, AVERAGE_MACHINE_HOURLY_COST, AVERAGE_LABOR_HOURLY_COST,
                    PRODUCTION_COST_MARGIN, MATERIAL_COST_MARGIN, ADDITIONAL_OVERHEAD)) {
                FieldComponent fieldComponent = (FieldComponent) viewDefinitionState.getComponentByReference(fieldName);
                fieldComponent.setEnabled(false);
                fieldComponent.requestComponentUpdateState();
            }

            viewDefinitionState.getComponentByReference(L_ORDER_OPERATION_PRODUCT_IN_COMPONENTS).setEnabled(false);
            viewDefinitionState.getComponentByReference(L_OPERATIONS_COST_GRID).setEnabled(false);
        }
    }

    public void checkIfOptionsAreAvailable(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        FieldComponent sourceOfMaterialCosts = (FieldComponent) viewDefinitionState
                .getComponentByReference(SOURCE_OF_MATERIAL_COSTS);
        FieldComponent calculateMaterialCostsMode = (FieldComponent) viewDefinitionState
                .getComponentByReference(CALCULATE_MATERIAL_COSTS_MODE);

        if (SourceOfMaterialCosts.CURRENT_GLOBAL_DEFINITIONS_IN_PRODUCT.getStringValue().equals(
                sourceOfMaterialCosts.getFieldValue())
                && CalculateMaterialCostsMode.COST_FOR_ORDER.getStringValue().equals(calculateMaterialCostsMode.getFieldValue())) {
            sourceOfMaterialCosts.addMessage("productionCountingWithCosts.messages.optionUnavailable", MessageType.FAILURE);
        }
    }

    private Entity getOrderFromDB(Long orderId) {
        return dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).get(orderId);
    }
}
