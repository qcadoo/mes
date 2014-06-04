/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.3
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
package com.qcadoo.mes.costCalculation.hooks;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.basic.util.CurrencyService;
import com.qcadoo.mes.costCalculation.constants.CostCalculationConstants;
import com.qcadoo.mes.costCalculation.constants.CostCalculationFields;
import com.qcadoo.mes.costNormsForOperation.constants.CalculateOperationCostMode;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.constants.OrderType;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.CheckBoxComponent;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;
import com.qcadoo.view.api.utils.NumberGeneratorService;

@Service
public class CostCalculationDetailsHooks {

    private static final String L_FORM = "form";

    private static final String L_PRODUCTION_COST_MARGIN_PROC = "productionCostMarginProc";

    private static final String L_MATERIAL_COST_MARGIN_PROC = "materialCostMarginProc";

    private static final String L_ADDITIONAL_OVERHEAD_CURRENCY = "additionalOverheadCurrency";

    private static final String L_TOTAL_COST_PER_UNIT_UNIT = "totalCostPerUnitUnit";

    private static final String L_TOTAL_MACHINE_HOURLY_COSTS_CURRENCY = "totalMachineHourlyCostsCurrency";

    private static final String L_TOTAL_LABOR_HOURLY_COSTS_CURRENCY = "totalLaborHourlyCostsCurrency";

    private static final String L_TOTAL_PIECEWORK_COSTS_CURRENCY = "totalPieceworkCostsCurrency";

    @Autowired
    private NumberGeneratorService numberGeneratorService;

    @Autowired
    private CurrencyService currencyService;

    public void setCriteriaModifierParameters(final ViewDefinitionState view) {
        LookupComponent orderLookup = (LookupComponent) view.getComponentByReference(CostCalculationFields.ORDER);
        LookupComponent technologyLookup = (LookupComponent) view.getComponentByReference(CostCalculationFields.TECHNOLOGY);
        LookupComponent productLookup = (LookupComponent) view.getComponentByReference(CostCalculationFields.PRODUCT);

        Entity order = orderLookup.getEntity();
        Entity product = productLookup.getEntity();

        FilterValueHolder filterValueHolder = technologyLookup.getFilterValue();

        if (order == null) {
            filterValueHolder.remove(CostCalculationFields.TECHNOLOGY);
        } else {
            String orderType = order.getStringField(OrderFields.ORDER_TYPE);

            Entity technology = null;

            if (OrderType.WITH_PATTERN_TECHNOLOGY.getStringValue().equals(orderType)) {
                technology = order.getBelongsToField(OrderFields.TECHNOLOGY_PROTOTYPE);
            } else {
                technology = order.getBelongsToField(OrderFields.TECHNOLOGY);
            }

            if (technology == null) {
                filterValueHolder.remove(CostCalculationFields.TECHNOLOGY);
            } else {
                filterValueHolder.put(CostCalculationFields.TECHNOLOGY, technology.getId());
            }
        }

        if (product == null) {
            filterValueHolder.remove(CostCalculationFields.PRODUCT);
        } else {
            filterValueHolder.put(CostCalculationFields.PRODUCT, product.getId());
        }

        technologyLookup.setFilterValue(filterValueHolder);
    }

    public void setFieldsEnabled(final ViewDefinitionState view) {
        Set<String> referenceNames = Sets.newHashSet(CostCalculationFields.PRODUCT, CostCalculationFields.ORDER,
                CostCalculationFields.QUANTITY, CostCalculationFields.TECHNOLOGY, CostCalculationFields.NUMBER,
                CostCalculationFields.PRODUCTION_LINE, CostCalculationFields.DESCRIPTION,
                CostCalculationFields.CALCULATE_MATERIAL_COSTS_MODE, CostCalculationFields.CALCULATE_OPERATION_COSTS_MODE,
                CostCalculationFields.PRODUCTION_COST_MARGIN, CostCalculationFields.MATERIAL_COST_MARGIN,
                CostCalculationFields.ADDITIONAL_OVERHEAD, CostCalculationFields.PRINT_COST_NORMS_OF_MATERIALS,
                CostCalculationFields.PRINT_OPERATION_NORMS, CostCalculationFields.INCLUDE_TPZ,
                CostCalculationFields.INCLUDE_ADDITIONAL_TIME, CostCalculationFields.SOURCE_OF_MATERIAL_COSTS,
                L_PRODUCTION_COST_MARGIN_PROC, L_MATERIAL_COST_MARGIN_PROC, L_ADDITIONAL_OVERHEAD_CURRENCY);

        Map<String, FieldComponent> componentsMap = Maps.newHashMap();

        for (String referenceName : referenceNames) {
            FieldComponent fieldComponent = (FieldComponent) view.getComponentByReference(referenceName);
            componentsMap.put(referenceName, fieldComponent);
        }

        CheckBoxComponent generatedField = (CheckBoxComponent) view.getComponentByReference(CostCalculationFields.GENERATED);

        boolean isGenerated = generatedField.isChecked();

        for (Entry<String, FieldComponent> entry : componentsMap.entrySet()) {
            entry.getValue().setEnabled(!isGenerated);
        }

        if (componentsMap.get(CostCalculationFields.TECHNOLOGY).getFieldValue() == null) {
            componentsMap.get(CostCalculationFields.PRODUCT).setEnabled(true);
            componentsMap.get(CostCalculationFields.QUANTITY).setEnabled(true);
            componentsMap.get(CostCalculationFields.ORDER).setEnabled(true);
            componentsMap.get(CostCalculationFields.TECHNOLOGY).setEnabled(true);
        }
    }

    public void generateNumber(final ViewDefinitionState view) {
        numberGeneratorService.generateAndInsertNumber(view, CostCalculationConstants.PLUGIN_IDENTIFIER,
                CostCalculationConstants.MODEL_COST_CALCULATION, L_FORM, CostCalculationFields.NUMBER);
    }

    public void fillCurrencyFields(final ViewDefinitionState viewDefinitionState) {
        final String currencyAlphabeticCode = currencyService.getCurrencyAlphabeticCode();

        Set<String> referenceNames = Sets.newHashSet("totalCostsCurrency", "totalOverheadCurrency",
                "additionalOverheadValueCurrency", "materialCostMarginValueCurrency", "productionCostMarginValueCurrency",
                "totalTechnicalProductionCostsCurrency", L_TOTAL_PIECEWORK_COSTS_CURRENCY, L_TOTAL_LABOR_HOURLY_COSTS_CURRENCY,
                L_TOTAL_MACHINE_HOURLY_COSTS_CURRENCY, "totalMaterialCostsCurrency", L_ADDITIONAL_OVERHEAD_CURRENCY);

        for (String referenceName : referenceNames) {
            FieldComponent fieldComponent = (FieldComponent) viewDefinitionState.getComponentByReference(referenceName);

            fieldComponent.setFieldValue(currencyAlphabeticCode);
            fieldComponent.requestComponentUpdateState();
        }

        FieldComponent productionCostMarginProc = (FieldComponent) viewDefinitionState
                .getComponentByReference(L_PRODUCTION_COST_MARGIN_PROC);
        productionCostMarginProc.setFieldValue("%");
        productionCostMarginProc.requestComponentUpdateState();

        FieldComponent materialCostMarginProc = (FieldComponent) viewDefinitionState
                .getComponentByReference(L_MATERIAL_COST_MARGIN_PROC);
        materialCostMarginProc.setFieldValue("%");
        materialCostMarginProc.requestComponentUpdateState();

        fillCostPerUnitUnitField(viewDefinitionState);
    }

    public void fillCostPerUnitUnitField(final ViewDefinitionState view) {
        final String currencyAlphabeticCode = currencyService.getCurrencyAlphabeticCode();

        FieldComponent totalCostPerUnitUnit = (FieldComponent) view.getComponentByReference(L_TOTAL_COST_PER_UNIT_UNIT);
        LookupComponent productField = (LookupComponent) view.getComponentByReference(CostCalculationFields.PRODUCT);

        Entity product = productField.getEntity();

        if (product == null) {
            return;
        }

        totalCostPerUnitUnit.setFieldValue(currencyAlphabeticCode + " / " + product.getStringField(ProductFields.UNIT));
        totalCostPerUnitUnit.requestComponentUpdateState();
    }

    public void disableCheckboxIfPieceworkIsSelected(final ViewDefinitionState viewDefinitionState) {
        FieldComponent calculateOperationCostsModeField = (FieldComponent) viewDefinitionState
                .getComponentByReference(CostCalculationFields.CALCULATE_OPERATION_COSTS_MODE);

        FieldComponent includeTPZField = (FieldComponent) viewDefinitionState
                .getComponentByReference(CostCalculationFields.INCLUDE_TPZ);
        FieldComponent includeAdditionalTimeField = (FieldComponent) viewDefinitionState
                .getComponentByReference(CostCalculationFields.INCLUDE_ADDITIONAL_TIME);

        FieldComponent machineHourlyCostsField = (FieldComponent) viewDefinitionState
                .getComponentByReference(CostCalculationFields.TOTAL_MACHINE_HOURLY_COSTS);
        FieldComponent machineHourlyCostsCurrencyField = (FieldComponent) viewDefinitionState
                .getComponentByReference(L_TOTAL_MACHINE_HOURLY_COSTS_CURRENCY);

        FieldComponent totalLaborHourlyCostsField = (FieldComponent) viewDefinitionState
                .getComponentByReference(CostCalculationFields.TOTAL_LABOR_HOURLY_COSTS);
        FieldComponent totalLaborHourlyCostsCurrencyField = (FieldComponent) viewDefinitionState
                .getComponentByReference(L_TOTAL_LABOR_HOURLY_COSTS_CURRENCY);

        FieldComponent totalPieceworkCostsField = (FieldComponent) viewDefinitionState
                .getComponentByReference(CostCalculationFields.TOTAL_PIECEWORK_COSTS);
        FieldComponent totalPieceworkCostsCurrencyField = (FieldComponent) viewDefinitionState
                .getComponentByReference(L_TOTAL_PIECEWORK_COSTS_CURRENCY);

        if (CalculateOperationCostMode.PIECEWORK.getStringValue().equals(calculateOperationCostsModeField.getFieldValue())) {
            includeTPZField.setFieldValue(false);
            includeTPZField.setEnabled(false);
            includeTPZField.requestComponentUpdateState();

            includeAdditionalTimeField.setFieldValue(false);
            includeAdditionalTimeField.setEnabled(false);
            includeAdditionalTimeField.requestComponentUpdateState();

            machineHourlyCostsField.setVisible(false);
            machineHourlyCostsCurrencyField.setVisible(false);

            totalLaborHourlyCostsField.setVisible(false);
            totalLaborHourlyCostsCurrencyField.setVisible(false);

            totalPieceworkCostsField.setVisible(true);
            totalPieceworkCostsCurrencyField.setVisible(true);
        } else {
            machineHourlyCostsField.setVisible(true);
            machineHourlyCostsCurrencyField.setVisible(true);

            totalLaborHourlyCostsField.setVisible(true);
            totalLaborHourlyCostsCurrencyField.setVisible(true);

            totalPieceworkCostsField.setVisible(false);
            totalPieceworkCostsCurrencyField.setVisible(false);
        }
    }

}
