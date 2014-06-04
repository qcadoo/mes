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
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.basic.util.CurrencyService;
import com.qcadoo.mes.costCalculation.constants.CalculateMaterialCostsMode;
import com.qcadoo.mes.costCalculation.constants.SourceOfMaterialCosts;
import com.qcadoo.mes.orders.OrderService;
import com.qcadoo.mes.productionCounting.ProductionCountingService;
import com.qcadoo.mes.productionCounting.constants.OrderFieldsPC;
import com.qcadoo.mes.productionCounting.constants.ProductionBalanceFields;
import com.qcadoo.mes.productionCountingWithCosts.constants.ParameterFieldsPCWC;
import com.qcadoo.mes.productionCountingWithCosts.constants.ProductionBalanceFieldsPCWC;
import com.qcadoo.model.api.BigDecimalUtils;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ComponentState.MessageType;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.CheckBoxComponent;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;

@Service
public class ProductionBalanceDetailsHooksPCWC {

    private static final String L_FORM = "form";

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

    @Autowired
    private ParameterService parameterService;

    @Autowired
    private NumberService numberService;

    public void changeFieldsAndGridsVisibility(final ViewDefinitionState view) {
        FormComponent productionBalanceForm = (FormComponent) view.getComponentByReference(L_FORM);

        FieldComponent orderLookup = (FieldComponent) view.getComponentByReference(ProductionBalanceFields.ORDER);
        FieldComponent calculateOperationCostModeField = (FieldComponent) view
                .getComponentByReference(ProductionBalanceFields.CALCULATE_OPERATION_COST_MODE);
        CheckBoxComponent generatedCheckBox = (CheckBoxComponent) view.getComponentByReference(ProductionBalanceFields.GENERATED);

        if ((productionBalanceForm.getEntityId() == null) || !generatedCheckBox.isChecked()) {
            productionCountingService.setComponentsVisibility(view, L_COST_GRIDS_AND_LAYOUTS, false, false);

            return;
        }

        String calculateOperationCostMode = (String) calculateOperationCostModeField.getFieldValue();

        Long orderId = (Long) orderLookup.getFieldValue();

        if (orderId == null) {
            productionCountingService.setComponentsVisibility(view, L_COST_GRIDS_AND_LAYOUTS, false, false);

            return;
        }

        Entity order = orderService.getOrder(orderId);

        if (generatedCheckBox.isChecked() && (calculateOperationCostModeField != null) && (order != null)) {
            ComponentState materialCostsGridLayoutComponent = view.getComponentByReference(L_MATERIAL_COSTS_GRID_LAYOUT);

            ComponentState componentsCostSummaryBorderLayoutComponent = view
                    .getComponentByReference(L_COMPONENTS_COST_SUMMARY_BORDER_LAYOUT);
            ComponentState technologyOperationProductInComponentsGridComponent = view
                    .getComponentByReference(L_TECHNOLOGY_OPERATION_PRODUCT_IN_COMPONENTS_GRID);

            ComponentState workCostsGridLayoutComponent = view.getComponentByReference(L_WORK_COSTS_GRID_LAYOUT);

            ComponentState machineCostsBorderLayoutComponent = view.getComponentByReference(L_MACHINE_COSTS_BORDER_LAYOUT);
            ComponentState laborCostsBorderLayoutComponent = view.getComponentByReference(L_LABOR_COSTS_BORDER_LAYOUT);
            ComponentState operationsCostsGridComponent = view.getComponentByReference(L_OPERATIONS_COST_GRID);

            ComponentState pieceworkCostsGridLayoutComponent = view.getComponentByReference(L_PIECEWORK_COSTS_GRID_LAYOUT);
            ComponentState pieceworkCostsBorderLayoutComponent = view.getComponentByReference(L_PIECEWORK_COSTS_BORDER_LAYOUT);
            ComponentState operationsPieceworkCostGridComponent = view.getComponentByReference(L_OPERATIONS_PIECEWORK_COST_GRID);

            materialCostsGridLayoutComponent.setVisible(true);

            componentsCostSummaryBorderLayoutComponent.setVisible(true);
            technologyOperationProductInComponentsGridComponent.setVisible(true);

            if (productionCountingService.isCalculateOperationCostModeHourly(calculateOperationCostMode)
                    && order.getBooleanField(OrderFieldsPC.REGISTER_PRODUCTION_TIME)) {
                workCostsGridLayoutComponent.setVisible(true);

                if (productionCountingService.isTypeOfProductionRecordingForEach(order
                        .getStringField(OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING))) {
                    machineCostsBorderLayoutComponent.setVisible(true);
                    laborCostsBorderLayoutComponent.setVisible(true);
                    operationsCostsGridComponent.setVisible(true);
                } else if (productionCountingService.isTypeOfProductionRecordingCumulated(order
                        .getStringField(OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING))) {
                    machineCostsBorderLayoutComponent.setVisible(true);
                    laborCostsBorderLayoutComponent.setVisible(true);
                    operationsCostsGridComponent.setVisible(false);
                }

                pieceworkCostsGridLayoutComponent.setVisible(false);

                pieceworkCostsBorderLayoutComponent.setVisible(false);
                operationsPieceworkCostGridComponent.setVisible(false);
            } else if (productionCountingService.isCalculateOperationCostModePiecework(calculateOperationCostMode)
                    && order.getBooleanField(OrderFieldsPC.REGISTER_PIECEWORK)) {
                workCostsGridLayoutComponent.setVisible(false);

                machineCostsBorderLayoutComponent.setVisible(false);
                laborCostsBorderLayoutComponent.setVisible(false);
                operationsCostsGridComponent.setVisible(false);

                pieceworkCostsGridLayoutComponent.setVisible(true);

                pieceworkCostsBorderLayoutComponent.setVisible(true);
                operationsPieceworkCostGridComponent.setVisible(true);
            }
        }
    }

    public void changeAssumptionsVisibility(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        changeAssumptionsVisibility(view);
    }

    public void changeAssumptionsVisibility(final ViewDefinitionState view) {
        ComponentState assumptionsBorderLayout = view.getComponentByReference(L_ASSUMPTIONS_BORDER_LAYOUT);

        FieldComponent averageMachineHourlyCostsField = (FieldComponent) view
                .getComponentByReference(ProductionBalanceFieldsPCWC.AVERAGE_MACHINE_HOURLY_COST);
        FieldComponent averageLaborHourlyCostField = (FieldComponent) view
                .getComponentByReference(ProductionBalanceFieldsPCWC.AVERAGE_LABOR_HOURLY_COST);

        FieldComponent orderLookup = (FieldComponent) view.getComponentByReference(ProductionBalanceFields.ORDER);
        FieldComponent calculateOperationCostModeField = (FieldComponent) view
                .getComponentByReference(ProductionBalanceFields.CALCULATE_OPERATION_COST_MODE);

        String calculateOperationCostMode = (String) calculateOperationCostModeField.getFieldValue();

        Long orderId = (Long) orderLookup.getFieldValue();

        if (orderId == null) {
            assumptionsBorderLayout.setVisible(false);

            averageMachineHourlyCostsField.setRequired(false);
            averageLaborHourlyCostField.setRequired(false);

            return;
        }

        Entity order = orderService.getOrder(orderId);

        if ((order != null)
                && productionCountingService.isCalculateOperationCostModeHourly(calculateOperationCostMode)
                && productionCountingService.isTypeOfProductionRecordingCumulated(order
                        .getStringField(OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING))) {
            assumptionsBorderLayout.setVisible(true);

            averageMachineHourlyCostsField.setRequired(true);
            averageLaborHourlyCostField.setRequired(true);
        } else {
            assumptionsBorderLayout.setVisible(false);

            averageMachineHourlyCostsField.setRequired(false);
            averageLaborHourlyCostField.setRequired(false);
        }
    }

    public void fillCurrencyAndUnitFields(final ViewDefinitionState view) {
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
            FieldComponent fieldComponent = (FieldComponent) view.getComponentByReference(currencyFieldName);
            fieldComponent.setFieldValue(currencyAlphabeticCode);
            fieldComponent.requestComponentUpdateState();
        }

        FieldComponent productionCostMarginProc = (FieldComponent) view.getComponentByReference("productionCostMarginProc");
        productionCostMarginProc.setFieldValue("%");
        productionCostMarginProc.requestComponentUpdateState();

        FieldComponent materialCostMarginProc = (FieldComponent) view.getComponentByReference("materialCostMarginProc");
        materialCostMarginProc.setFieldValue("%");
        materialCostMarginProc.requestComponentUpdateState();

        Set<String> unitFieldNames = Sets.newHashSet("registeredTotalTechnicalProductionCostPerUnitUnit",
                "totalTechnicalProductionCostPerUnitUnit", "balanceTechnicalProductionCostPerUnitUnit", "totalCostPerUnitUnit");

        Long productId = (Long) view.getComponentByReference(ProductionBalanceFields.PRODUCT).getFieldValue();

        if (productId == null) {
            return;
        }

        Entity product = getProductFromDB(productId);

        String unit = product.getStringField(ProductFields.UNIT);

        for (String unitFieldName : unitFieldNames) {
            FieldComponent fieldComponent = (FieldComponent) view.getComponentByReference(unitFieldName);
            fieldComponent.setFieldValue(currencyAlphabeticCode + "/" + unit);
            fieldComponent.requestComponentUpdateState();
        }
    }

    public void disableFieldsAndGridsWhenGenerated(final ViewDefinitionState view) {
        CheckBoxComponent generatedCheckBox = (CheckBoxComponent) view.getComponentByReference(ProductionBalanceFields.GENERATED);

        if (generatedCheckBox.isChecked()) {
            productionCountingService.setComponentsState(view, L_COST_FIELDS, false, true);
            productionCountingService.setComponentsState(view, L_COST_GRIDS, false, false);
        } else {
            productionCountingService.setComponentsState(view, L_COST_FIELDS, true, true);
            productionCountingService.setComponentsState(view, L_COST_GRIDS, true, false);
        }
    }

    public void checkIfOptionsAreAvailable(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FieldComponent sourceOfMaterialCosts = (FieldComponent) view
                .getComponentByReference(ProductionBalanceFieldsPCWC.SOURCE_OF_MATERIAL_COSTS);
        FieldComponent calculateMaterialCostsMode = (FieldComponent) view
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

    public void setTheFieldBasedOnParameters(final ViewDefinitionState view) {
        FormComponent form = (FormComponent) view.getComponentByReference("form");
        if (form.getEntityId() == null) {
            CheckBoxComponent isSetFieldsFromParameter = (CheckBoxComponent) view
                    .getComponentByReference("isSetFieldsFromParameter");
            if (isSetFieldsFromParameter.isChecked()) {
                return;
            }
            Entity parameter = parameterService.getParameter();

            FieldComponent printOperationNorms = (FieldComponent) view
                    .getComponentByReference(ProductionBalanceFields.PRINT_OPERATION_NORMS);
            printOperationNorms.setFieldValue(parameter.getBooleanField(ParameterFieldsPCWC.PRINT_OPERATION_NORMS_PB));
            printOperationNorms.requestComponentUpdateState();

            FieldComponent calculateOperationsCostsMode = (FieldComponent) view
                    .getComponentByReference(ProductionBalanceFields.CALCULATE_OPERATION_COST_MODE);
            if (parameter.getField(ParameterFieldsPCWC.CALCULATE_OPERATION_COST_MODE_PB) != null) {
                calculateOperationsCostsMode.setFieldValue(parameter
                        .getField(ParameterFieldsPCWC.CALCULATE_OPERATION_COST_MODE_PB));
                calculateOperationsCostsMode.requestComponentUpdateState();
            }
            FieldComponent calculateMaterialCostsMode = (FieldComponent) view
                    .getComponentByReference("calculateMaterialCostsMode");
            if (parameter.getField(ParameterFieldsPCWC.CALCULATE_MATERIAL_COSTS_MODE_PB) != null) {

                calculateMaterialCostsMode
                        .setFieldValue(parameter.getField(ParameterFieldsPCWC.CALCULATE_MATERIAL_COSTS_MODE_PB));
                calculateMaterialCostsMode.requestComponentUpdateState();
            }
            FieldComponent includeTPZ = (FieldComponent) view.getComponentByReference(ProductionBalanceFields.INCLUDE_TPZ);
            includeTPZ.setFieldValue(parameter.getBooleanField(ParameterFieldsPCWC.INCLUDE_TPZ_PB));
            includeTPZ.requestComponentUpdateState();

            FieldComponent includeAdditionalTime = (FieldComponent) view
                    .getComponentByReference(ProductionBalanceFields.INCLUDE_ADDITIONAL_TIME);
            includeAdditionalTime.setFieldValue(parameter.getBooleanField(ParameterFieldsPCWC.INCLUDE_ADDITIONAL_TIME_PB));
            includeAdditionalTime.requestComponentUpdateState();

            FieldComponent printCostNormsOfMaterials = (FieldComponent) view
                    .getComponentByReference(ProductionBalanceFieldsPCWC.PRINT_COST_NORMS_OF_MATERIALS);
            printCostNormsOfMaterials.setFieldValue(parameter
                    .getBooleanField(ParameterFieldsPCWC.PRINT_COST_NORMS_OF_MATERIALS_PB));
            printCostNormsOfMaterials.requestComponentUpdateState();

            FieldComponent sourceOfMaterialCosts = (FieldComponent) view
                    .getComponentByReference(ProductionBalanceFieldsPCWC.SOURCE_OF_MATERIAL_COSTS);
            if (parameter.getField(ParameterFieldsPCWC.SOURCE_OF_MATERIAL_COSTS_PB) != null) {

                sourceOfMaterialCosts.setFieldValue(parameter.getField(ParameterFieldsPCWC.SOURCE_OF_MATERIAL_COSTS_PB));
                sourceOfMaterialCosts.requestComponentUpdateState();
            }

            FieldComponent averageMachineHourlyCost = (FieldComponent) view
                    .getComponentByReference(ProductionBalanceFieldsPCWC.AVERAGE_MACHINE_HOURLY_COST);
            averageMachineHourlyCost.setFieldValue(numberService.format(BigDecimalUtils.convertNullToZero(parameter
                    .getDecimalField(ParameterFieldsPCWC.AVERAGE_MACHINE_HOURLY_COST_PB))));
            averageMachineHourlyCost.requestComponentUpdateState();

            FieldComponent averageLaborHourlyCost = (FieldComponent) view
                    .getComponentByReference(ProductionBalanceFieldsPCWC.AVERAGE_LABOR_HOURLY_COST);
            averageLaborHourlyCost.setFieldValue(numberService.format(BigDecimalUtils.convertNullToZero(parameter
                    .getDecimalField(ParameterFieldsPCWC.AVERAGE_LABOR_HOURLY_COST_PB))));
            averageLaborHourlyCost.requestComponentUpdateState();

            FieldComponent productionCostMargin = (FieldComponent) view
                    .getComponentByReference(ProductionBalanceFieldsPCWC.PRODUCTION_COST_MARGIN);
            productionCostMargin.setFieldValue(numberService.format(BigDecimalUtils.convertNullToZero(parameter
                    .getDecimalField(ParameterFieldsPCWC.PRODUCTION_COST_MARGIN_PB))));
            productionCostMargin.requestComponentUpdateState();

            FieldComponent materialCostMargin = (FieldComponent) view
                    .getComponentByReference(ProductionBalanceFieldsPCWC.MATERIAL_COST_MARGIN);
            materialCostMargin.setFieldValue(numberService.format(BigDecimalUtils.convertNullToZero(parameter
                    .getDecimalField(ParameterFieldsPCWC.MATERIAL_COST_MARGIN_PB))));
            materialCostMargin.requestComponentUpdateState();

            FieldComponent additionalOverhead = (FieldComponent) view
                    .getComponentByReference(ProductionBalanceFieldsPCWC.ADDITIONAL_OVERHEAD);
            additionalOverhead.setFieldValue(numberService.format(BigDecimalUtils.convertNullToZero(parameter
                    .getDecimalField(ParameterFieldsPCWC.ADDITIONAL_OVERHEAD_PB))));
            additionalOverhead.requestComponentUpdateState();
            isSetFieldsFromParameter.setFieldValue(true);
            isSetFieldsFromParameter.requestComponentUpdateState();
        }
    }
}
