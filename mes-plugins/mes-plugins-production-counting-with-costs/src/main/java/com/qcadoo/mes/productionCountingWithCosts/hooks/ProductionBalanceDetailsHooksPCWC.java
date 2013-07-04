/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.2.0
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

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.basic.util.CurrencyService;
import com.qcadoo.mes.costCalculation.constants.CalculateMaterialCostsMode;
import com.qcadoo.mes.costCalculation.constants.SourceOfMaterialCosts;
import com.qcadoo.mes.orders.OrderService;
import com.qcadoo.mes.productionCounting.ProductionCountingService;
import com.qcadoo.mes.productionCounting.constants.OrderFieldsPC;
import com.qcadoo.mes.productionCounting.constants.ProductionBalanceFields;
import com.qcadoo.mes.productionCountingWithCosts.constants.ProductionBalanceFieldsPCWC;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ComponentState.MessageType;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;

@Service
public class ProductionBalanceDetailsHooksPCWC {

    private static final String L_ASSUMPTIONS_BORDER_LAYOUT = "assumptionsBorderLayout";

    private static final String L_MATERIAL_COSTS_GRID_LAYOUT = "materialCostsGridLayout";

    private static final String L_COMPONENTS_COST_SUMMARY_BORDER_LAYOUT = "componentsCostSummaryBorderLayout";

    private static final String L_TECHNOLOGY_OPERATION_PRODUCT_IN_COMPONENTS_GRID = "technologyOperationProductInComponentsGrid";

    private static final String L_WORK_COSTS_GRID_LAYOUT = "workCostsGridLayout";

    private static final String L_MACHINE_COSTS_BORDER_LAYOUT = "machineCostsBorderLayout";

    private static final String L_LABOR_COSTS_BORDER_LAYOUT = "laborCostsBorderLayout";

    private static final String L_OPERATIONS_COST_GRID = "operationsCostGrid";

    private static final String L_PIECEWORK_COSTS_GRID_LAYOUT = "pieceworkCostsGridLayout";

    private static final String L_PIECEWORK_COSTS_BORDER_LAYOUT = "pieceworkCostsBorderLayout";

    private static final String L_OPERATIONS_PIECEWORK_COST_GRID = "operationsPieceworkCostGrid";

    private static final List<String> L_COST_FIELDS = Arrays.asList(ProductionBalanceFieldsPCWC.PRINT_COST_NORMS_OF_MATERIALS,
            ProductionBalanceFieldsPCWC.SOURCE_OF_MATERIAL_COSTS, ProductionBalanceFieldsPCWC.CALCULATE_MATERIAL_COSTS_MODE,
            ProductionBalanceFieldsPCWC.AVERAGE_MACHINE_HOURLY_COST, ProductionBalanceFieldsPCWC.AVERAGE_LABOR_HOURLY_COST,
            ProductionBalanceFieldsPCWC.PRODUCTION_COST_MARGIN, ProductionBalanceFieldsPCWC.MATERIAL_COST_MARGIN,
            ProductionBalanceFieldsPCWC.ADDITIONAL_OVERHEAD);

    private static final List<String> L_COST_GRIDS = Arrays.asList(L_TECHNOLOGY_OPERATION_PRODUCT_IN_COMPONENTS_GRID,
            L_OPERATIONS_COST_GRID);

    private static final List<String> L_COST_GRIDS_AND_LAYOUTS = Arrays.asList(L_MATERIAL_COSTS_GRID_LAYOUT,
            L_COMPONENTS_COST_SUMMARY_BORDER_LAYOUT, L_TECHNOLOGY_OPERATION_PRODUCT_IN_COMPONENTS_GRID, L_WORK_COSTS_GRID_LAYOUT,
            L_MACHINE_COSTS_BORDER_LAYOUT, L_LABOR_COSTS_BORDER_LAYOUT, L_OPERATIONS_COST_GRID, L_PIECEWORK_COSTS_GRID_LAYOUT,
            L_PIECEWORK_COSTS_BORDER_LAYOUT, L_OPERATIONS_PIECEWORK_COST_GRID);

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private CurrencyService currencyService;

    @Autowired
    private ProductionCountingService productionCountingService;

    @Autowired
    private OrderService orderService;

    public void changeFieldsAndGridsVisibility(final ViewDefinitionState viewDefinitionState) {
        FormComponent form = (FormComponent) viewDefinitionState.getComponentByReference("form");

        FieldComponent generated = (FieldComponent) viewDefinitionState
                .getComponentByReference(ProductionBalanceFields.GENERATED);

        if ((form == null) || (form.getEntityId() == null) || (generated == null) || "0".equals(generated.getFieldValue())) {
            productionCountingService.setComponentsVisibility(viewDefinitionState, L_COST_GRIDS_AND_LAYOUTS, false, false);

            return;
        }

        FieldComponent orderLookup = (FieldComponent) viewDefinitionState.getComponentByReference(ProductionBalanceFields.ORDER);
        FieldComponent calculateOperationCostModeField = (FieldComponent) viewDefinitionState
                .getComponentByReference(ProductionBalanceFields.CALCULATE_OPERATION_COST_MODE);

        String calculateOperationCostMode = (String) calculateOperationCostModeField.getFieldValue();

        Long orderId = (Long) orderLookup.getFieldValue();

        if (orderId == null) {
            productionCountingService.setComponentsVisibility(viewDefinitionState, L_COST_GRIDS_AND_LAYOUTS, false, false);

            return;
        }

        Entity order = orderService.getOrder(orderId);

        if ("1".equals(generated.getFieldValue()) && (calculateOperationCostModeField != null) && (order != null)) {
            viewDefinitionState.getComponentByReference(L_MATERIAL_COSTS_GRID_LAYOUT).setVisible(true);

            viewDefinitionState.getComponentByReference(L_COMPONENTS_COST_SUMMARY_BORDER_LAYOUT).setVisible(true);
            viewDefinitionState.getComponentByReference(L_TECHNOLOGY_OPERATION_PRODUCT_IN_COMPONENTS_GRID).setVisible(true);

            if (productionCountingService.isCalculateOperationCostModeHourly(calculateOperationCostMode)
                    && order.getBooleanField(OrderFieldsPC.REGISTER_PRODUCTION_TIME)) {
                viewDefinitionState.getComponentByReference(L_WORK_COSTS_GRID_LAYOUT).setVisible(true);

                if (productionCountingService.isTypeOfProductionRecordingForEach(order
                        .getStringField(OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING))) {
                    viewDefinitionState.getComponentByReference(L_MACHINE_COSTS_BORDER_LAYOUT).setVisible(true);
                    viewDefinitionState.getComponentByReference(L_LABOR_COSTS_BORDER_LAYOUT).setVisible(true);
                    viewDefinitionState.getComponentByReference(L_OPERATIONS_COST_GRID).setVisible(true);
                } else if (productionCountingService.isTypeOfProductionRecordingCumulated(order
                        .getStringField(OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING))) {
                    viewDefinitionState.getComponentByReference(L_MACHINE_COSTS_BORDER_LAYOUT).setVisible(true);
                    viewDefinitionState.getComponentByReference(L_LABOR_COSTS_BORDER_LAYOUT).setVisible(true);
                    viewDefinitionState.getComponentByReference(L_OPERATIONS_COST_GRID).setVisible(false);
                }

                viewDefinitionState.getComponentByReference(L_PIECEWORK_COSTS_GRID_LAYOUT).setVisible(false);

                viewDefinitionState.getComponentByReference(L_PIECEWORK_COSTS_BORDER_LAYOUT).setVisible(false);
                viewDefinitionState.getComponentByReference(L_OPERATIONS_PIECEWORK_COST_GRID).setVisible(false);
            } else if (productionCountingService.isCalculateOperationCostModePiecework(calculateOperationCostMode)
                    && order.getBooleanField(OrderFieldsPC.REGISTER_PIECEWORK)) {
                viewDefinitionState.getComponentByReference(L_WORK_COSTS_GRID_LAYOUT).setVisible(false);

                viewDefinitionState.getComponentByReference(L_MACHINE_COSTS_BORDER_LAYOUT).setVisible(false);
                viewDefinitionState.getComponentByReference(L_LABOR_COSTS_BORDER_LAYOUT).setVisible(false);
                viewDefinitionState.getComponentByReference(L_OPERATIONS_COST_GRID).setVisible(false);

                viewDefinitionState.getComponentByReference(L_PIECEWORK_COSTS_GRID_LAYOUT).setVisible(true);

                viewDefinitionState.getComponentByReference(L_PIECEWORK_COSTS_BORDER_LAYOUT).setVisible(true);
                viewDefinitionState.getComponentByReference(L_OPERATIONS_PIECEWORK_COST_GRID).setVisible(true);
            }
        }
    }

    public void changeAssumptionsVisibility(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        changeAssumptionsVisibility(viewDefinitionState);
    }

    public void changeAssumptionsVisibility(final ViewDefinitionState viewDefinitionState) {
        ComponentState assumptionsBorderLayout = viewDefinitionState.getComponentByReference(L_ASSUMPTIONS_BORDER_LAYOUT);

        FieldComponent orderLookup = (FieldComponent) viewDefinitionState.getComponentByReference(ProductionBalanceFields.ORDER);
        FieldComponent calculateOperationCostModeField = (FieldComponent) viewDefinitionState
                .getComponentByReference(ProductionBalanceFields.CALCULATE_OPERATION_COST_MODE);

        String calculateOperationCostMode = (String) calculateOperationCostModeField.getFieldValue();

        Long orderId = (Long) orderLookup.getFieldValue();

        if (orderId == null) {
            assumptionsBorderLayout.setVisible(false);

            return;
        }

        Entity order = orderService.getOrder(orderId);

        if ((order != null)
                && productionCountingService.isCalculateOperationCostModeHourly(calculateOperationCostMode)
                && productionCountingService.isTypeOfProductionRecordingCumulated(order
                        .getStringField(OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING))) {
            assumptionsBorderLayout.setVisible(true);
        } else {
            assumptionsBorderLayout.setVisible(false);
        }
    }

    public void fillCurrencyAndUnitFields(final ViewDefinitionState viewDefinitionState) {
        String currencyAlphabeticCode = currencyService.getCurrencyAlphabeticCode();

        List<String> currencyFieldNames = Lists.newArrayList("averageMachineHourlyCostCurrency",
                "averageLaborHourlyCostCurrency", "additionalOverheadCurrency", "plannedComponentsCostsCurrency",
                "componentsCostsCurrency", "componentsCostsBalanceCurrency", "plannedMachineCostsCurrency",
                "machineCostsCurrency", "machineCostsBalanceCurrency", "plannedLaborCostsCurrency", "laborCostsCurrency",
                "laborCostsBalanceCurrency", "plannedCyclesCostsCurrency", "cyclesCostsCurrency", "cyclesCostsBalanceCurrency",
                "registeredTotalTechnicalProductionCostsCurrency", "totalTechnicalProductionCostsCurrency",
                "balanceTechnicalProductionCostsCurrency", "productionCostMarginValueCurrency",
                "materialCostMarginValueCurrency", "additionalOverheadValueCurrency", "totalOverheadCurrency",
                "totalCostsCurrency");

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

        Long productId = (Long) viewDefinitionState.getComponentByReference(ProductionBalanceFields.PRODUCT).getFieldValue();

        if (productId == null) {
            return;
        }

        Entity product = getProductFromDB(productId);

        String unit = product.getStringField(ProductFields.UNIT);

        for (String unitFieldName : unitFieldNames) {
            FieldComponent fieldComponent = (FieldComponent) viewDefinitionState.getComponentByReference(unitFieldName);
            fieldComponent.setFieldValue(currencyAlphabeticCode + "/" + unit);
            fieldComponent.requestComponentUpdateState();
        }

    }

    public void disableFieldsAndGridsWhenGenerated(final ViewDefinitionState viewDefinitionState) {
        FieldComponent generated = (FieldComponent) viewDefinitionState
                .getComponentByReference(ProductionBalanceFields.GENERATED);

        if ((generated != null) && (generated.getFieldValue() != null) && "1".equals(generated.getFieldValue())) {
            productionCountingService.setComponentsState(viewDefinitionState, L_COST_FIELDS, false, true);
            productionCountingService.setComponentsState(viewDefinitionState, L_COST_GRIDS, false, false);
        } else {
            productionCountingService.setComponentsState(viewDefinitionState, L_COST_FIELDS, true, true);
            productionCountingService.setComponentsState(viewDefinitionState, L_COST_GRIDS, true, false);
        }
    }

    public void checkIfOptionsAreAvailable(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        FieldComponent sourceOfMaterialCosts = (FieldComponent) viewDefinitionState
                .getComponentByReference(ProductionBalanceFieldsPCWC.SOURCE_OF_MATERIAL_COSTS);
        FieldComponent calculateMaterialCostsMode = (FieldComponent) viewDefinitionState
                .getComponentByReference(ProductionBalanceFieldsPCWC.CALCULATE_MATERIAL_COSTS_MODE);

        if (SourceOfMaterialCosts.CURRENT_GLOBAL_DEFINITIONS_IN_PRODUCT.getStringValue().equals(
                sourceOfMaterialCosts.getFieldValue())
                && CalculateMaterialCostsMode.COST_FOR_ORDER.getStringValue().equals(calculateMaterialCostsMode.getFieldValue())) {
            sourceOfMaterialCosts.addMessage("productionCountingWithCosts.messages.optionUnavailable", MessageType.FAILURE);
        }
    }

    private Entity getProductFromDB(final Long productId) {
        return dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_PRODUCT).get(productId);
    }

}
