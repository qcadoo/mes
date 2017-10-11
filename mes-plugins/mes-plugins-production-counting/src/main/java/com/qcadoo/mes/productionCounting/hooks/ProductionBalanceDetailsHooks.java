/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.4
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
package com.qcadoo.mes.productionCounting.hooks;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.basic.util.CurrencyService;
import com.qcadoo.mes.costCalculation.constants.CalculateMaterialCostsMode;
import com.qcadoo.mes.costCalculation.constants.SourceOfMaterialCosts;
import com.qcadoo.mes.orders.OrderService;
import com.qcadoo.mes.productionCounting.ProductionBalanceService;
import com.qcadoo.mes.productionCounting.ProductionCountingService;
import com.qcadoo.mes.productionCounting.constants.OrderFieldsPC;
import com.qcadoo.mes.productionCounting.constants.ParameterFieldsPC;
import com.qcadoo.mes.productionCounting.constants.ProductionBalanceFields;
import com.qcadoo.mes.productionCounting.constants.ProductionCountingConstants;
import com.qcadoo.model.api.BigDecimalUtils;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.CheckBoxComponent;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.api.utils.NumberGeneratorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

@Service
public class ProductionBalanceDetailsHooks {

    private static final String L_INPUT_PRODUCTS_GRID = "inputProductsGrid";

    private static final String L_OUTPUT_PRODUCTS_GRID = "outputProductsGrid";

    private static final String L_TIME_GRID_LAYOUT = "workGridLayout";

    private static final String L_LABOR_TIME_BORDER_LAYOUT = "laborTimeBorderLayout";

    private static final String L_MACHINE_TIME_BORDER_LAYOUT = "machineTimeBorderLayout";

    private static final String L_OPERATIONS_TIME_GRID = "operationsTimeGrid";

    private static final String L_OPERATIONS_PIECEWORK_GRID = "operationsPieceworkGrid";

    private static final String L_ASSUMPTIONS_BORDER_LAYOUT = "assumptionsBorderLayout";

    private static final List<String> L_FIELDS_AND_CHECKBOXES = Arrays.asList(ProductionBalanceFields.ORDER,
            ProductionBalanceFields.NAME, ProductionBalanceFields.DESCRIPTION, ProductionBalanceFields.PRINT_OPERATION_NORMS,
            ProductionBalanceFields.CALCULATE_OPERATION_COST_MODE, ProductionBalanceFields.TYPE,
            ProductionBalanceFields.INCLUDE_TPZ, ProductionBalanceFields.INCLUDE_ADDITIONAL_TIME);

    private static final List<String> L_FIELDS = L_FIELDS_AND_CHECKBOXES.subList(0, L_FIELDS_AND_CHECKBOXES.size() - 2);

    private static final List<String> L_GRIDS = Arrays.asList(L_INPUT_PRODUCTS_GRID, L_OUTPUT_PRODUCTS_GRID);

    private static final List<String> L_GRIDS_AND_LAYOUTS = Arrays.asList(L_INPUT_PRODUCTS_GRID, L_OUTPUT_PRODUCTS_GRID,
            L_TIME_GRID_LAYOUT, L_MACHINE_TIME_BORDER_LAYOUT, L_LABOR_TIME_BORDER_LAYOUT, L_OPERATIONS_TIME_GRID,
            L_OPERATIONS_PIECEWORK_GRID);

    private static final String L_FORM = "form";

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

    private static final List<String> L_COST_FIELDS = Arrays.asList(ProductionBalanceFields.PRINT_COST_NORMS_OF_MATERIALS,
            ProductionBalanceFields.SOURCE_OF_MATERIAL_COSTS, ProductionBalanceFields.CALCULATE_MATERIAL_COSTS_MODE,
            ProductionBalanceFields.AVERAGE_MACHINE_HOURLY_COST, ProductionBalanceFields.AVERAGE_LABOR_HOURLY_COST,
            ProductionBalanceFields.PRODUCTION_COST_MARGIN, ProductionBalanceFields.MATERIAL_COST_MARGIN,
            ProductionBalanceFields.ADDITIONAL_OVERHEAD, "sourceOfOperationCostsPB", "registrationPriceOverhead", "profit");

    public static final String L_ORDERS_GRID = "orders";

    private static final List<String> L_COST_GRIDS = Arrays.asList(L_TECHNOLOGY_OPERATION_PRODUCT_IN_COMPONENTS_GRID,
            L_OPERATIONS_COST_GRID, L_ORDERS_GRID);

    private static final List<String> L_COST_GRIDS_AND_LAYOUTS = Arrays.asList(L_MATERIAL_COSTS_GRID_LAYOUT,
            L_COMPONENTS_COST_SUMMARY_BORDER_LAYOUT, L_TECHNOLOGY_OPERATION_PRODUCT_IN_COMPONENTS_GRID, L_WORK_COSTS_GRID_LAYOUT,
            L_MACHINE_COSTS_BORDER_LAYOUT, L_LABOR_COSTS_BORDER_LAYOUT, L_OPERATIONS_COST_GRID, L_PIECEWORK_COSTS_GRID_LAYOUT,
            L_PIECEWORK_COSTS_BORDER_LAYOUT, L_OPERATIONS_PIECEWORK_COST_GRID);

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private CurrencyService currencyService;

    @Autowired
    private ParameterService parameterService;

    @Autowired
    private NumberService numberService;

    @Autowired
    private ProductionCountingService productionCountingService;

    @Autowired
    private ProductionBalanceService productionBalanceService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private NumberGeneratorService numberGeneratorService;

    public void onBeforeRender(final ViewDefinitionState view) {
        generateOrderNumber(view);
    }

    public void generateOrderNumber(final ViewDefinitionState view) {
        numberGeneratorService.generateAndInsertNumber(view, ProductionCountingConstants.PLUGIN_IDENTIFIER,
                ProductionCountingConstants.MODEL_PRODUCTION_BALANCE, L_FORM, ProductionBalanceFields.NUMBER);
    }

    public void changeAssumptionsVisibility(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        changeAssumptionsVisibility(view);
    }

    public void changeAssumptionsVisibility(final ViewDefinitionState view) {
        ComponentState assumptionsBorderLayout = view.getComponentByReference(L_ASSUMPTIONS_BORDER_LAYOUT);

        FieldComponent averageMachineHourlyCostsField = (FieldComponent) view
                .getComponentByReference(ProductionBalanceFields.AVERAGE_MACHINE_HOURLY_COST);
        FieldComponent averageLaborHourlyCostField = (FieldComponent) view
                .getComponentByReference(ProductionBalanceFields.AVERAGE_LABOR_HOURLY_COST);

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

    public void changeFieldsAndGridsVisibility(final ViewDefinitionState view) {
        FormComponent productionBalanceForm = (FormComponent) view.getComponentByReference(L_FORM);

        productionBalanceService.changeTabsVisible(view);
        changeFieldsAndGridsVisibilityForCosts(view);
        CheckBoxComponent generatedCheckBox = (CheckBoxComponent) view.getComponentByReference(ProductionBalanceFields.GENERATED);
        LookupComponent orderLookup = (LookupComponent) view.getComponentByReference(ProductionBalanceFields.ORDER);
        FieldComponent calculateOperationCostModeField = (FieldComponent) view
                .getComponentByReference(ProductionBalanceFields.CALCULATE_OPERATION_COST_MODE);

        if ((productionBalanceForm.getEntityId() == null) || !generatedCheckBox.isChecked()) {
            productionCountingService.setComponentsVisibility(view, L_GRIDS_AND_LAYOUTS, false, false);
        }

        String calculateOperationCostMode = (String) calculateOperationCostModeField.getFieldValue();

        Long orderId = (Long) orderLookup.getFieldValue();

        if (orderId == null) {
            productionCountingService.setComponentsVisibility(view, L_GRIDS_AND_LAYOUTS, false, false);

            return;
        }

        Entity order = orderService.getOrder(orderId);

        if (generatedCheckBox.isChecked() && (order != null)) {
            ComponentState inputProductsGridComponent = view.getComponentByReference(L_INPUT_PRODUCTS_GRID);
            ComponentState outputProductsGridComponent = view.getComponentByReference(L_OUTPUT_PRODUCTS_GRID);

            if (order.getBooleanField(OrderFieldsPC.REGISTER_QUANTITY_IN_PRODUCT)) {
                inputProductsGridComponent.setVisible(true);
            }

            if (order.getBooleanField(OrderFieldsPC.REGISTER_QUANTITY_OUT_PRODUCT)) {
                outputProductsGridComponent.setVisible(true);
            }

            ComponentState timeGridLayoutComponent = view.getComponentByReference(L_TIME_GRID_LAYOUT);

            ComponentState machineTimeBorderLayoutComponent = view.getComponentByReference(L_MACHINE_TIME_BORDER_LAYOUT);
            ComponentState laborTimeBorderLayoutComponent = view.getComponentByReference(L_LABOR_TIME_BORDER_LAYOUT);
            ComponentState operationsTimeGridComponent = view.getComponentByReference(L_OPERATIONS_TIME_GRID);

            ComponentState operationsPieceworkGridComponent = view.getComponentByReference(L_OPERATIONS_PIECEWORK_GRID);

            if (productionCountingService.isCalculateOperationCostModeHourly(calculateOperationCostMode)
                    && order.getBooleanField(OrderFieldsPC.REGISTER_PRODUCTION_TIME)) {
                timeGridLayoutComponent.setVisible(true);

                if (productionCountingService.isTypeOfProductionRecordingForEach(order
                        .getStringField(OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING))) {
                    machineTimeBorderLayoutComponent.setVisible(true);
                    laborTimeBorderLayoutComponent.setVisible(true);
                    operationsTimeGridComponent.setVisible(true);
                } else if (productionCountingService.isTypeOfProductionRecordingCumulated(order
                        .getStringField(OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING))) {
                    machineTimeBorderLayoutComponent.setVisible(true);
                    laborTimeBorderLayoutComponent.setVisible(true);
                    operationsTimeGridComponent.setVisible(false);
                }

                operationsPieceworkGridComponent.setVisible(false);
            } else if (productionCountingService.isCalculateOperationCostModePiecework(calculateOperationCostMode)
                    && order.getBooleanField(OrderFieldsPC.REGISTER_PIECEWORK)) {
                timeGridLayoutComponent.setVisible(false);

                machineTimeBorderLayoutComponent.setVisible(false);
                laborTimeBorderLayoutComponent.setVisible(false);
                operationsTimeGridComponent.setVisible(false);

                operationsPieceworkGridComponent.setVisible(true);
            }
        }

    }

    public void disableCheckboxes(final ViewDefinitionState view) {
        productionBalanceService.disableCheckboxes(view);
    }

    public void disableFieldsAndGridsWhenGenerated(final ViewDefinitionState view) {
        CheckBoxComponent generatedCheckBox = (CheckBoxComponent) view.getComponentByReference(ProductionBalanceFields.GENERATED);

        if (generatedCheckBox.isChecked()) {
            productionCountingService.setComponentsState(view, L_FIELDS_AND_CHECKBOXES, false, true);
            productionCountingService.setComponentsState(view, L_GRIDS, false, false);
        } else {
            productionCountingService.setComponentsState(view, L_FIELDS, true, true);
            productionCountingService.setComponentsState(view, L_GRIDS, true, false);
        }

        if (generatedCheckBox.isChecked()) {
            productionCountingService.setComponentsState(view, L_COST_FIELDS, false, true);
            productionCountingService.setComponentsState(view, L_COST_GRIDS, false, false);
        } else {
            productionCountingService.setComponentsState(view, L_COST_FIELDS, true, true);
            productionCountingService.setComponentsState(view, L_COST_GRIDS, true, false);
        }
    }

    public void changeFieldsAndGridsVisibilityForCosts(final ViewDefinitionState view) {
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

        if (generatedCheckBox.isChecked() && (order != null)) {
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

        List<String> procFields = Lists.newArrayList("productionCostMarginProc", "materialCostMarginProc",
                "registrationPriceOverheadProc", "profitProc");
        for (String field : procFields) {
            FieldComponent materialCostMarginProc = (FieldComponent) view.getComponentByReference(field);
            materialCostMarginProc.setFieldValue("%");
            materialCostMarginProc.requestComponentUpdateState();
        }

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

    public void onSourceOfMaterialCostsChange(final ViewDefinitionState viewDefinitionState, final ComponentState state, final String[] args) {
        checkIfOptionsAreAvailable(viewDefinitionState, state, args);
        FieldComponent sourceOfMaterialCosts = (FieldComponent) viewDefinitionState
                .getComponentByReference(ProductionBalanceFields.SOURCE_OF_MATERIAL_COSTS);
        FieldComponent calculateMaterialCostsMode = (FieldComponent) viewDefinitionState
                .getComponentByReference(ProductionBalanceFields.CALCULATE_MATERIAL_COSTS_MODE);
        if(SourceOfMaterialCosts.FROM_ORDERS_MATERIAL_COSTS.getStringValue().equals((String) sourceOfMaterialCosts.getFieldValue())){
            calculateMaterialCostsMode.setFieldValue(CalculateMaterialCostsMode.COST_FOR_ORDER.getStringValue());
        }

    }

    public void checkIfOptionsAreAvailable(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FieldComponent sourceOfMaterialCosts = (FieldComponent) view
                .getComponentByReference(ProductionBalanceFields.SOURCE_OF_MATERIAL_COSTS);
        FieldComponent calculateMaterialCostsMode = (FieldComponent) view
                .getComponentByReference(ProductionBalanceFields.CALCULATE_MATERIAL_COSTS_MODE);

        if (SourceOfMaterialCosts.CURRENT_GLOBAL_DEFINITIONS_IN_PRODUCT.getStringValue().equals(
                sourceOfMaterialCosts.getFieldValue())
                && CalculateMaterialCostsMode.COST_FOR_ORDER.getStringValue().equals(calculateMaterialCostsMode.getFieldValue())) {
            sourceOfMaterialCosts.addMessage("productionCounting.messages.optionUnavailable", ComponentState.MessageType.FAILURE);
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
            printOperationNorms.setFieldValue(parameter.getBooleanField(ParameterFieldsPC.PRINT_OPERATION_NORMS_PB));
            printOperationNorms.requestComponentUpdateState();

            FieldComponent calculateOperationsCostsMode = (FieldComponent) view
                    .getComponentByReference(ProductionBalanceFields.CALCULATE_OPERATION_COST_MODE);
            if (parameter.getField(ParameterFieldsPC.CALCULATE_OPERATION_COST_MODE_PB) != null) {
                calculateOperationsCostsMode
                        .setFieldValue(parameter.getField(ParameterFieldsPC.CALCULATE_OPERATION_COST_MODE_PB));
                calculateOperationsCostsMode.requestComponentUpdateState();
            }
            FieldComponent sourceOfOperationCostsPB = (FieldComponent) view
                    .getComponentByReference(ProductionBalanceFields.SOURCE_OF_OPERATION_COSTS_PB);
            if (parameter.getField(ParameterFieldsPC.SOURCE_OF_OPERATION_COSTS_PB) != null) {
                sourceOfOperationCostsPB.setFieldValue(parameter.getField(ParameterFieldsPC.SOURCE_OF_OPERATION_COSTS_PB));
                sourceOfOperationCostsPB.requestComponentUpdateState();
            }
            FieldComponent calculateMaterialCostsMode = (FieldComponent) view
                    .getComponentByReference("calculateMaterialCostsMode");
            if (parameter.getField(ParameterFieldsPC.CALCULATE_MATERIAL_COSTS_MODE_PB) != null) {

                calculateMaterialCostsMode.setFieldValue(parameter.getField(ParameterFieldsPC.CALCULATE_MATERIAL_COSTS_MODE_PB));
                calculateMaterialCostsMode.requestComponentUpdateState();
            }
            FieldComponent includeTPZ = (FieldComponent) view.getComponentByReference(ProductionBalanceFields.INCLUDE_TPZ);
            includeTPZ.setFieldValue(parameter.getBooleanField(ParameterFieldsPC.INCLUDE_TPZ_PB));
            includeTPZ.requestComponentUpdateState();

            FieldComponent includeAdditionalTime = (FieldComponent) view
                    .getComponentByReference(ProductionBalanceFields.INCLUDE_ADDITIONAL_TIME);
            includeAdditionalTime.setFieldValue(parameter.getBooleanField(ParameterFieldsPC.INCLUDE_ADDITIONAL_TIME_PB));
            includeAdditionalTime.requestComponentUpdateState();

            FieldComponent printCostNormsOfMaterials = (FieldComponent) view
                    .getComponentByReference(ProductionBalanceFields.PRINT_COST_NORMS_OF_MATERIALS);
            printCostNormsOfMaterials
                    .setFieldValue(parameter.getBooleanField(ParameterFieldsPC.PRINT_COST_NORMS_OF_MATERIALS_PB));
            printCostNormsOfMaterials.requestComponentUpdateState();

            FieldComponent sourceOfMaterialCosts = (FieldComponent) view
                    .getComponentByReference(ProductionBalanceFields.SOURCE_OF_MATERIAL_COSTS);
            if (parameter.getField(ParameterFieldsPC.SOURCE_OF_MATERIAL_COSTS_PB) != null) {

                sourceOfMaterialCosts.setFieldValue(parameter.getField(ParameterFieldsPC.SOURCE_OF_MATERIAL_COSTS_PB));
                sourceOfMaterialCosts.requestComponentUpdateState();
            }

            FieldComponent averageMachineHourlyCost = (FieldComponent) view
                    .getComponentByReference(ProductionBalanceFields.AVERAGE_MACHINE_HOURLY_COST);
            averageMachineHourlyCost.setFieldValue(numberService.format(BigDecimalUtils.convertNullToZero(parameter
                    .getDecimalField(ParameterFieldsPC.AVERAGE_MACHINE_HOURLY_COST_PB))));
            averageMachineHourlyCost.requestComponentUpdateState();

            FieldComponent averageLaborHourlyCost = (FieldComponent) view
                    .getComponentByReference(ProductionBalanceFields.AVERAGE_LABOR_HOURLY_COST);
            averageLaborHourlyCost.setFieldValue(numberService.format(BigDecimalUtils.convertNullToZero(parameter
                    .getDecimalField(ParameterFieldsPC.AVERAGE_LABOR_HOURLY_COST_PB))));
            averageLaborHourlyCost.requestComponentUpdateState();

            FieldComponent productionCostMargin = (FieldComponent) view
                    .getComponentByReference(ProductionBalanceFields.PRODUCTION_COST_MARGIN);
            productionCostMargin.setFieldValue(numberService.format(BigDecimalUtils.convertNullToZero(parameter
                    .getDecimalField(ParameterFieldsPC.PRODUCTION_COST_MARGIN_PB))));
            productionCostMargin.requestComponentUpdateState();

            FieldComponent materialCostMargin = (FieldComponent) view
                    .getComponentByReference(ProductionBalanceFields.MATERIAL_COST_MARGIN);
            materialCostMargin.setFieldValue(numberService.format(BigDecimalUtils.convertNullToZero(parameter
                    .getDecimalField(ParameterFieldsPC.MATERIAL_COST_MARGIN_PB))));
            materialCostMargin.requestComponentUpdateState();

            FieldComponent additionalOverhead = (FieldComponent) view
                    .getComponentByReference(ProductionBalanceFields.ADDITIONAL_OVERHEAD);
            additionalOverhead.setFieldValue(numberService.format(BigDecimalUtils.convertNullToZero(parameter
                    .getDecimalField(ParameterFieldsPC.ADDITIONAL_OVERHEAD_PB))));
            additionalOverhead.requestComponentUpdateState();

            FieldComponent registrationPriceOverhead = (FieldComponent) view
                    .getComponentByReference(ProductionBalanceFields.REGISTRATION_PRICE_OVERHEAD);
            registrationPriceOverhead.setFieldValue(numberService.format(BigDecimalUtils.convertNullToZero(parameter
                    .getDecimalField(ParameterFieldsPC.REGISTRATION_PRICE_OVERHEAD_PB))));
            registrationPriceOverhead.requestComponentUpdateState();

            FieldComponent profit = (FieldComponent) view.getComponentByReference(ProductionBalanceFields.PROFIT);
            profit.setFieldValue(numberService.format(BigDecimalUtils.convertNullToZero(parameter
                    .getDecimalField(ParameterFieldsPC.PROFIT_PB))));
            profit.requestComponentUpdateState();

            isSetFieldsFromParameter.setFieldValue(true);
            isSetFieldsFromParameter.requestComponentUpdateState();
        }
    }

}
