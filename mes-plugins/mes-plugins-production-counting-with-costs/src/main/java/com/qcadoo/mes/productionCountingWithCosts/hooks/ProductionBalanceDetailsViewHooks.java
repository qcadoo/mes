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

import static com.qcadoo.mes.costCalculation.constants.CalculateMaterialCostsMode.COST_FOR_ORDER;
import static com.qcadoo.mes.costCalculation.constants.SourceOfMaterialCosts.CURRENT_GLOBAL_DEFINITIONS_IN_PRODUCT;
import static com.qcadoo.mes.productionCounting.internal.constants.CalculateOperationCostsMode.HOURLY;
import static com.qcadoo.mes.productionCounting.internal.constants.CalculateOperationCostsMode.PIECEWORK;
import static com.qcadoo.mes.productionCounting.internal.constants.OrderFieldsPC.REGISTER_PIECEWORK;
import static com.qcadoo.mes.productionCounting.internal.constants.OrderFieldsPC.REGISTER_PRODUCTION_TIME;
import static com.qcadoo.mes.productionCounting.internal.constants.OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING;
import static com.qcadoo.mes.productionCounting.internal.constants.ProductionBalanceFields.CALCULATE_OPERATION_COST_MODE;
import static com.qcadoo.mes.productionCounting.internal.constants.ProductionBalanceFields.GENERATED;
import static com.qcadoo.mes.productionCounting.internal.constants.ProductionBalanceFields.ORDER;
import static com.qcadoo.mes.productionCounting.internal.constants.TypeOfProductionRecording.CUMULATED;
import static com.qcadoo.mes.productionCounting.internal.constants.TypeOfProductionRecording.FOR_EACH;
import static com.qcadoo.mes.productionCountingWithCosts.constants.ProductionBalanceFieldsPCWC.ADDITIONAL_OVERHEAD;
import static com.qcadoo.mes.productionCountingWithCosts.constants.ProductionBalanceFieldsPCWC.AVERAGE_LABOR_HOURLY_COST;
import static com.qcadoo.mes.productionCountingWithCosts.constants.ProductionBalanceFieldsPCWC.AVERAGE_MACHINE_HOURLY_COST;
import static com.qcadoo.mes.productionCountingWithCosts.constants.ProductionBalanceFieldsPCWC.CALCULATE_MATERIAL_COSTS_MODE;
import static com.qcadoo.mes.productionCountingWithCosts.constants.ProductionBalanceFieldsPCWC.MATERIAL_COST_MARGIN;
import static com.qcadoo.mes.productionCountingWithCosts.constants.ProductionBalanceFieldsPCWC.PRINT_COST_NORMS_OF_MATERIALS;
import static com.qcadoo.mes.productionCountingWithCosts.constants.ProductionBalanceFieldsPCWC.PRODUCTION_COST_MARGIN;
import static com.qcadoo.mes.productionCountingWithCosts.constants.ProductionBalanceFieldsPCWC.SOURCE_OF_MATERIAL_COSTS;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Sets;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.basic.util.CurrencyService;
import com.qcadoo.mes.productionCounting.internal.ProductionBalanceService;
import com.qcadoo.mes.productionCounting.internal.ProductionBalanceViewService;
import com.qcadoo.mes.productionCounting.internal.constants.ProductionBalanceFields;
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
public class ProductionBalanceDetailsViewHooks {

    private static final String L_ASSUMPTIONS_BORDER_LAYOUT = "assumptionsBorderLayout";

    private static final String L_MATERIAL_COSTS_GRID_LAYOUT = "materialCostsGridLayout";

    private static final String L_COMPONENTS_COST_SUMMARY_BORDER_LAYOUT = "componentsCostSummaryBorderLayout";

    private static final String L_TECHNOLOGY_INSTANCE_OPER_PRODUCT_IN_GRID = "technologyInstOperProductInGrid";

    private static final String L_WORK_COSTS_GRID_LAYOUT = "workCostsGridLayout";

    private static final String L_MACHINE_COSTS_BORDER_LAYOUT = "machineCostsBorderLayout";

    private static final String L_LABOR_COSTS_BORDER_LAYOUT = "laborCostsBorderLayout";

    private static final String L_OPERATIONS_COST_GRID = "operationsCostGrid";

    private static final String L_PIECEWORK_COSTS_GRID_LAYOUT = "pieceworkCostsGridLayout";

    private static final String L_PIECEWORK_COSTS_BORDER_LAYOUT = "pieceworkCostsBorderLayout";

    private static final String L_OPERATIONS_PIECEWORK_COST_GRID = "operationsPieceworkCostGrid";

    private static final List<String> COST_FIELDS = Arrays.asList(PRINT_COST_NORMS_OF_MATERIALS, SOURCE_OF_MATERIAL_COSTS,
            CALCULATE_MATERIAL_COSTS_MODE, AVERAGE_MACHINE_HOURLY_COST, AVERAGE_LABOR_HOURLY_COST, PRODUCTION_COST_MARGIN,
            MATERIAL_COST_MARGIN, ADDITIONAL_OVERHEAD);

    private static final List<String> COST_GRIDS = Arrays.asList(L_TECHNOLOGY_INSTANCE_OPER_PRODUCT_IN_GRID,
            L_OPERATIONS_COST_GRID);

    private static final List<String> COST_GRIDS_AND_LAYOUTS = Arrays.asList(L_MATERIAL_COSTS_GRID_LAYOUT,
            L_COMPONENTS_COST_SUMMARY_BORDER_LAYOUT, L_TECHNOLOGY_INSTANCE_OPER_PRODUCT_IN_GRID, L_WORK_COSTS_GRID_LAYOUT,
            L_MACHINE_COSTS_BORDER_LAYOUT, L_LABOR_COSTS_BORDER_LAYOUT, L_OPERATIONS_COST_GRID, L_PIECEWORK_COSTS_GRID_LAYOUT,
            L_PIECEWORK_COSTS_BORDER_LAYOUT, L_OPERATIONS_PIECEWORK_COST_GRID);

    @Autowired
    private ProductionBalanceService productionBalanceService;

    @Autowired
    private ProductionBalanceViewService productionBalanceViewService;

    @Autowired
    private CurrencyService currencyService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private ParameterService parameterService;

    @Autowired
    private NumberService numberService;

    public void changeFieldsAndGridsVisibility(final ViewDefinitionState viewDefinitionState) {
        FormComponent form = (FormComponent) viewDefinitionState.getComponentByReference("form");

        FieldComponent generated = (FieldComponent) viewDefinitionState.getComponentByReference(GENERATED);

        if ((form == null) || (form.getEntityId() == null) || (generated == null) || "0".equals(generated.getFieldValue())) {
            productionBalanceViewService.setComponentsVisibility(viewDefinitionState, COST_GRIDS_AND_LAYOUTS, false, false);

            return;
        }

        FieldComponent calculateOperationCostMode = (FieldComponent) viewDefinitionState
                .getComponentByReference(CALCULATE_OPERATION_COST_MODE);

        FieldComponent orderLookup = (FieldComponent) viewDefinitionState.getComponentByReference(ORDER);

        Long orderId = (Long) orderLookup.getFieldValue();

        if (orderId == null) {
            productionBalanceViewService.setComponentsVisibility(viewDefinitionState, COST_GRIDS_AND_LAYOUTS, false, false);

            return;
        }

        Entity order = productionBalanceService.getOrderFromDB(orderId);

        if ("1".equals(generated.getFieldValue()) && (calculateOperationCostMode != null) && (order != null)) {
            viewDefinitionState.getComponentByReference(L_MATERIAL_COSTS_GRID_LAYOUT).setVisible(true);

            viewDefinitionState.getComponentByReference(L_COMPONENTS_COST_SUMMARY_BORDER_LAYOUT).setVisible(true);
            viewDefinitionState.getComponentByReference(L_TECHNOLOGY_INSTANCE_OPER_PRODUCT_IN_GRID).setVisible(true);

            if (HOURLY.getStringValue().equals(calculateOperationCostMode.getFieldValue())
                    && order.getBooleanField(REGISTER_PRODUCTION_TIME)) {
                viewDefinitionState.getComponentByReference(L_WORK_COSTS_GRID_LAYOUT).setVisible(true);

                if (FOR_EACH.getStringValue().equals(order.getStringField(TYPE_OF_PRODUCTION_RECORDING))) {
                    viewDefinitionState.getComponentByReference(L_MACHINE_COSTS_BORDER_LAYOUT).setVisible(true);
                    viewDefinitionState.getComponentByReference(L_LABOR_COSTS_BORDER_LAYOUT).setVisible(true);
                    viewDefinitionState.getComponentByReference(L_OPERATIONS_COST_GRID).setVisible(true);
                } else if (CUMULATED.getStringValue().equals(order.getStringField(TYPE_OF_PRODUCTION_RECORDING))) {
                    viewDefinitionState.getComponentByReference(L_MACHINE_COSTS_BORDER_LAYOUT).setVisible(true);
                    viewDefinitionState.getComponentByReference(L_LABOR_COSTS_BORDER_LAYOUT).setVisible(true);
                    viewDefinitionState.getComponentByReference(L_OPERATIONS_COST_GRID).setVisible(false);
                }

                viewDefinitionState.getComponentByReference(L_PIECEWORK_COSTS_GRID_LAYOUT).setVisible(false);

                viewDefinitionState.getComponentByReference(L_PIECEWORK_COSTS_BORDER_LAYOUT).setVisible(false);
                viewDefinitionState.getComponentByReference(L_OPERATIONS_PIECEWORK_COST_GRID).setVisible(false);
            } else if (PIECEWORK.getStringValue().equals(calculateOperationCostMode.getFieldValue())
                    && order.getBooleanField(REGISTER_PIECEWORK)) {
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

        FieldComponent calculateOperationCostMode = (FieldComponent) viewDefinitionState
                .getComponentByReference(CALCULATE_OPERATION_COST_MODE);

        FieldComponent orderLookup = (FieldComponent) viewDefinitionState.getComponentByReference(ProductionBalanceFields.ORDER);

        Long orderId = (Long) orderLookup.getFieldValue();

        if (orderId == null) {
            assumptionsBorderLayout.setVisible(false);

            return;
        }

        Entity order = productionBalanceService.getOrderFromDB(orderId);

        if ((order != null) && HOURLY.getStringValue().equals(calculateOperationCostMode.getFieldValue())
                && CUMULATED.getStringValue().equals(order.getStringField(TYPE_OF_PRODUCTION_RECORDING))) {
            assumptionsBorderLayout.setVisible(true);
        } else {
            assumptionsBorderLayout.setVisible(false);
        }
    }

    public void fillCurrencyAndUnitFields(final ViewDefinitionState viewDefinitionState) {
        String currencyAlphabeticCode = currencyService.getCurrencyAlphabeticCode();

        List<String> currencyFieldNames = Arrays.asList("averageMachineHourlyCostCurrency", "averageLaborHourlyCostCurrency",
                "additionalOverheadCurrency", "plannedComponentsCostsCurrency", "componentsCostsCurrency",
                "componentsCostsBalanceCurrency", "plannedMachineCostsCurrency", "machineCostsCurrency",
                "machineCostsBalanceCurrency", "plannedLaborCostsCurrency", "laborCostsCurrency", "laborCostsBalanceCurrency",
                "plannedCyclesCostsCurrency", "cyclesCostsCurrency", "cyclesCostsBalanceCurrency",
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

        Long productId = (Long) viewDefinitionState.getComponentByReference("product").getFieldValue();

        if (productId == null) {
            return;
        }

        Entity product = getProductFromDB(productId);

        String unit = product.getStringField("unit");

        for (String unitFieldName : unitFieldNames) {
            FieldComponent fieldComponent = (FieldComponent) viewDefinitionState.getComponentByReference(unitFieldName);
            fieldComponent.setFieldValue(currencyAlphabeticCode + "/" + unit);
            fieldComponent.requestComponentUpdateState();
        }

    }

    public void disableFieldsAndGridsWhenGenerated(final ViewDefinitionState viewDefinitionState) {
        FieldComponent generated = (FieldComponent) viewDefinitionState.getComponentByReference(GENERATED);

        if ((generated != null) && (generated.getFieldValue() != null) && "1".equals(generated.getFieldValue())) {
            productionBalanceViewService.setComponentsState(viewDefinitionState, COST_FIELDS, false, true);
            productionBalanceViewService.setComponentsState(viewDefinitionState, COST_GRIDS, false, false);
        } else {
            productionBalanceViewService.setComponentsState(viewDefinitionState, COST_FIELDS, true, true);
            productionBalanceViewService.setComponentsState(viewDefinitionState, COST_GRIDS, true, false);
        }
    }

    public void checkIfOptionsAreAvailable(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        FieldComponent sourceOfMaterialCosts = (FieldComponent) viewDefinitionState
                .getComponentByReference(SOURCE_OF_MATERIAL_COSTS);
        FieldComponent calculateMaterialCostsMode = (FieldComponent) viewDefinitionState
                .getComponentByReference(CALCULATE_MATERIAL_COSTS_MODE);

        if (CURRENT_GLOBAL_DEFINITIONS_IN_PRODUCT.getStringValue().equals(sourceOfMaterialCosts.getFieldValue())
                && COST_FOR_ORDER.getStringValue().equals(calculateMaterialCostsMode.getFieldValue())) {
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
