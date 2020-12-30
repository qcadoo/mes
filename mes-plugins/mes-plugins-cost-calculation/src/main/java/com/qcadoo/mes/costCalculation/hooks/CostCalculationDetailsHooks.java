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
package com.qcadoo.mes.costCalculation.hooks;

import com.google.common.base.Optional;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.qcadoo.commons.functional.Either;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.basic.util.CurrencyService;
import com.qcadoo.mes.costCalculation.constants.CostCalculationConstants;
import com.qcadoo.mes.costCalculation.constants.CostCalculationFields;
import com.qcadoo.mes.costCalculation.constants.SourceOfOperationCosts;
import com.qcadoo.model.api.BigDecimalUtils;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.*;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.api.utils.NumberGeneratorService;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

@Service
public class CostCalculationDetailsHooks {

    private static final String L_PRODUCTION_COST_MARGIN_PROC = "productionCostMarginProc";

    private static final String L_MATERIAL_COST_MARGIN_PROC = "materialCostMarginProc";

    private static final String L_REGISTRATION_PRICE_OVERHEAD_PROC = "registrationPriceOverheadProc";

    private static final String L_PROFIT_PROC = "profitProc";

    private static final String L_ADDITIONAL_OVERHEAD_CURRENCY = "additionalOverheadCurrency";

    private static final String L_TOTAL_COST_PER_UNIT_UNIT = "totalCostPerUnitUnit";

    private static final String L_TOTAL_MACHINE_HOURLY_COSTS_CURRENCY = "totalMachineHourlyCostsCurrency";

    private static final String L_TOTAL_LABOR_HOURLY_COSTS_CURRENCY = "totalLaborHourlyCostsCurrency";

    private static final String L_TOTAL_PIECEWORK_COSTS_CURRENCY = "totalPieceworkCostsCurrency";

    @Autowired
    private NumberGeneratorService numberGeneratorService;

    @Autowired
    private CurrencyService currencyService;

    @Autowired
    private ParameterService parameterService;

    @Autowired
    private NumberService numbersService;

    public void onBeforeRender(final ViewDefinitionState view) throws JSONException {
        fillOverheadsFromParameters(view);
        setFieldsEnabled(view);
        generateNumber(view);
        fillCurrencyFields(view);
        roundResults(view);
        setButtonEnabled(view);
    }

    private void roundResults(final ViewDefinitionState view) {
        Set<String> referenceNames = Sets.newHashSet(CostCalculationFields.TOTAL_MATERIAL_COSTS,
                CostCalculationFields.TOTAL_MACHINE_HOURLY_COSTS, CostCalculationFields.TOTAL_LABOR_HOURLY_COSTS,
                CostCalculationFields.TOTAL_PIECEWORK_COSTS, CostCalculationFields.TOTAL_TECHNICAL_PRODUCTION_COSTS,
                CostCalculationFields.PRODUCTION_COST_MARGIN_VALUE, CostCalculationFields.MATERIAL_COST_MARGIN_VALUE,
                CostCalculationFields.ADDITIONAL_OVERHEAD_VALUE, CostCalculationFields.TOTAL_OVERHEAD,
                CostCalculationFields.TOTAL_COSTS, CostCalculationFields.TOTAL_COST_PER_UNIT,
                CostCalculationFields.REGISTRATION_PRICE_OVERHEAD_VALUE, CostCalculationFields.TECHNICAL_PRODUCTION_COSTS,
                CostCalculationFields.PROFIT_VALUE, CostCalculationFields.SELL_PRICE_VALUE);

        for (String name : referenceNames) {
            FieldComponent component = (FieldComponent) view.getComponentByReference(name);
            String value = (String) component.getFieldValue();
            Either<Exception, Optional<BigDecimal>> eitherValue = BigDecimalUtils.tryParse(value, view.getLocale());
            if (eitherValue.isRight()) {
                Optional<BigDecimal> maybeValue = eitherValue.getRight();
                if (maybeValue.isPresent()) {
                    component.setFieldValue(
                            numbersService.format(numbersService.setScaleWithDefaultMathContext(maybeValue.get(), 2)));
                    component.requestComponentUpdateState();
                }
            }
        }
    }

    public void setFieldsEnabled(final ViewDefinitionState view) {
        Set<String> referenceNames = Sets.newHashSet(CostCalculationFields.QUANTITY, CostCalculationFields.NUMBER,
                CostCalculationFields.DESCRIPTION, CostCalculationFields.MATERIAL_COSTS_USED,
                CostCalculationFields.AVERAGE_LABOR_HOURLY_COST, CostCalculationFields.STANDARD_LABOR_COST,
                CostCalculationFields.PRODUCTION_COST_MARGIN, CostCalculationFields.AVERAGE_MACHINE_HOURLY_COST,
                CostCalculationFields.MATERIAL_COST_MARGIN, CostCalculationFields.ADDITIONAL_OVERHEAD,
                CostCalculationFields.PRINT_COST_NORMS_OF_MATERIALS, CostCalculationFields.PRINT_OPERATION_NORMS,
                CostCalculationFields.INCLUDE_TPZ, CostCalculationFields.USE_NOMINAL_COST_PRICE_NOT_SPECIFIED,
                CostCalculationFields.INCLUDE_ADDITIONAL_TIME, CostCalculationFields.SOURCE_OF_OPERATION_COSTS,
                CostCalculationFields.REGISTRATION_PRICE_OVERHEAD, CostCalculationFields.PROFIT, L_PRODUCTION_COST_MARGIN_PROC,
                L_MATERIAL_COST_MARGIN_PROC, L_ADDITIONAL_OVERHEAD_CURRENCY);

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
        if (isGenerated) {
            view.getComponentByReference(CostCalculationFields.TECHNOLOGIES).setEnabled(false);
        }
        if (!isGenerated) {
            FieldComponent sourceOfOperationCosts = (FieldComponent) view
                    .getComponentByReference(CostCalculationFields.SOURCE_OF_OPERATION_COSTS);
            FieldComponent standardLaborCost = (FieldComponent) view
                    .getComponentByReference(CostCalculationFields.STANDARD_LABOR_COST);
            CheckBoxComponent includeTPZ = (CheckBoxComponent) view.getComponentByReference(CostCalculationFields.INCLUDE_TPZ);
            CheckBoxComponent includeAdditionalTime = (CheckBoxComponent) view
                    .getComponentByReference(CostCalculationFields.INCLUDE_ADDITIONAL_TIME);
            standardLaborCost.setEnabled(
                    SourceOfOperationCosts.STANDARD_LABOR_COSTS.getStringValue().equals(sourceOfOperationCosts.getFieldValue()));
            standardLaborCost.requestComponentUpdateState();
            includeTPZ.setEnabled(
                    !SourceOfOperationCosts.STANDARD_LABOR_COSTS.getStringValue().equals(sourceOfOperationCosts.getFieldValue()));
            includeTPZ.requestComponentUpdateState();
            includeAdditionalTime.setEnabled(
                    !SourceOfOperationCosts.STANDARD_LABOR_COSTS.getStringValue().equals(sourceOfOperationCosts.getFieldValue()));
            includeAdditionalTime.requestComponentUpdateState();
        }
    }

    public void generateNumber(final ViewDefinitionState view) {
        numberGeneratorService.generateAndInsertNumber(view, CostCalculationConstants.PLUGIN_IDENTIFIER,
                CostCalculationConstants.MODEL_COST_CALCULATION, QcadooViewConstants.L_FORM, CostCalculationFields.NUMBER);
    }

    public void fillCurrencyFields(final ViewDefinitionState viewDefinitionState) {
        final String currencyAlphabeticCode = currencyService.getCurrencyAlphabeticCode();

        Set<String> referenceNames = Sets.newHashSet("totalCostsCurrency", "totalOverheadCurrency",
                "additionalOverheadValueCurrency", "materialCostMarginValueCurrency", "productionCostMarginValueCurrency",
                "totalTechnicalProductionCostsCurrency", L_TOTAL_PIECEWORK_COSTS_CURRENCY, L_TOTAL_LABOR_HOURLY_COSTS_CURRENCY,
                L_TOTAL_MACHINE_HOURLY_COSTS_CURRENCY, "totalMaterialCostsCurrency", L_ADDITIONAL_OVERHEAD_CURRENCY,
                "profitValueCurrency", "registrationPriceOverheadValueCurrency", L_TOTAL_COST_PER_UNIT_UNIT,
                "sellPriceValueCurrency", "technicalProductionCostsCurrency", "averageMachineHourlyCostCurrency",
                "averageLaborHourlyCostCurrency");

        for (String referenceName : referenceNames) {
            FieldComponent fieldComponent = (FieldComponent) viewDefinitionState.getComponentByReference(referenceName);

            fieldComponent.setFieldValue(currencyAlphabeticCode);
            fieldComponent.requestComponentUpdateState();
        }

        fillComponentWithPercent(L_PRODUCTION_COST_MARGIN_PROC, viewDefinitionState);
        fillComponentWithPercent(L_MATERIAL_COST_MARGIN_PROC, viewDefinitionState);
        fillComponentWithPercent(L_REGISTRATION_PRICE_OVERHEAD_PROC, viewDefinitionState);
        fillComponentWithPercent(L_PROFIT_PROC, viewDefinitionState);
    }

    private void fillComponentWithPercent(String componentName, ViewDefinitionState viewDefinitionState) {
        FieldComponent materialCostMarginProc = (FieldComponent) viewDefinitionState.getComponentByReference(componentName);
        materialCostMarginProc.setFieldValue("%");
        materialCostMarginProc.requestComponentUpdateState();
    }

    private void fillOverheadsFromParameters(ViewDefinitionState view) {
        FormComponent form = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        if (form.getEntityId() == null) {
            fillWithProperty(CostCalculationFields.MATERIAL_COSTS_USED, CostCalculationFields.MATERIAL_COSTS_USED, view);
            fillCheckboxWithProperty(CostCalculationFields.USE_NOMINAL_COST_PRICE_NOT_SPECIFIED,
                    CostCalculationFields.USE_NOMINAL_COST_PRICE_NOT_SPECIFIED, view);
            fillWithProperty(CostCalculationFields.SOURCE_OF_OPERATION_COSTS, CostCalculationFields.SOURCE_OF_OPERATION_COSTS,
                    view);
            fillLookupWithProperty(CostCalculationFields.STANDARD_LABOR_COST, CostCalculationFields.STANDARD_LABOR_COST, view);
            fillWithPropertyOrZero(CostCalculationFields.AVERAGE_MACHINE_HOURLY_COST,
                    CostCalculationFields.AVERAGE_MACHINE_HOURLY_COST, view, false);
            fillWithPropertyOrZero(CostCalculationFields.AVERAGE_LABOR_HOURLY_COST,
                    CostCalculationFields.AVERAGE_LABOR_HOURLY_COST, view, false);
            fillCheckboxWithProperty(CostCalculationFields.INCLUDE_TPZ, CostCalculationFields.INCLUDE_TPZ, view);
            fillCheckboxWithProperty(CostCalculationFields.INCLUDE_ADDITIONAL_TIME, CostCalculationFields.INCLUDE_ADDITIONAL_TIME,
                    view);

            fillWithPropertyOrZero(CostCalculationFields.MATERIAL_COST_MARGIN, CostCalculationFields.MATERIAL_COST_MARGIN, view,
                    true);
            fillWithPropertyOrZero(CostCalculationFields.PRODUCTION_COST_MARGIN, CostCalculationFields.PRODUCTION_COST_MARGIN,
                    view, true);
            fillWithPropertyOrZero(CostCalculationFields.ADDITIONAL_OVERHEAD, CostCalculationFields.ADDITIONAL_OVERHEAD, view,
                    true);
            fillWithPropertyOrZero(CostCalculationFields.REGISTRATION_PRICE_OVERHEAD,
                    CostCalculationFields.REGISTRATION_PRICE_OVERHEAD, view, true);
            fillWithPropertyOrZero(CostCalculationFields.PROFIT, CostCalculationFields.PROFIT, view, true);
        }
    }

    private void fillCheckboxWithProperty(String componentName, String propertyName, ViewDefinitionState view) {
        CheckBoxComponent component = (CheckBoxComponent) view.getComponentByReference(componentName);
        boolean propertyValue = parameterService.getParameter().getBooleanField(propertyName);
        component.setFieldValue(propertyValue);
    }

    private void fillLookupWithProperty(String componentName, String propertyName, ViewDefinitionState view) {
        LookupComponent component = (LookupComponent) view.getComponentByReference(componentName);
        Entity propertyValue = parameterService.getParameter().getBelongsToField(propertyName);
        if (propertyValue != null) {
            component.setFieldValue(propertyValue.getId());
        }
    }

    private void fillWithProperty(String componentName, String propertyName, ViewDefinitionState view) {
        FieldComponent component = (FieldComponent) view.getComponentByReference(componentName);
        String propertyValue = parameterService.getParameter().getStringField(propertyName);
        if (propertyValue != null) {
            component.setFieldValue(propertyValue);
        }
    }

    private void fillWithPropertyOrZero(String componentName, String propertyName, ViewDefinitionState view,
            boolean defaultValue) {
        FieldComponent component = (FieldComponent) view.getComponentByReference(componentName);
        if (component.getFieldValue() == null) {
            BigDecimal propertyValue = parameterService.getParameter().getDecimalField(propertyName);

            if (propertyValue != null) {
                String formattedProductionCostMargin = numbersService.formatWithMinimumFractionDigits(propertyValue.setScale(2),
                        0);
                component.setFieldValue(formattedProductionCostMargin);
            } else if (defaultValue) {
                component.setFieldValue(0);
            }
        }
    }

    private void setButtonEnabled(ViewDefinitionState view) {
        WindowComponent window = (WindowComponent) view.getComponentByReference(QcadooViewConstants.L_WINDOW);
        RibbonActionItem saveNominalCosts = window.getRibbon().getGroupByName(CostCalculationFields.SAVE_COSTS)
                .getItemByName(CostCalculationFields.NOMINAL_COSTS);
        CheckBoxComponent generatedField = (CheckBoxComponent) view.getComponentByReference(CostCalculationFields.GENERATED);
        if (generatedField.isChecked()) {
            saveNominalCosts.setEnabled(true);
            saveNominalCosts.requestUpdate(true);
        }
    }
}
