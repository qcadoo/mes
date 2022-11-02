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

import java.math.BigDecimal;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.basic.util.CurrencyService;
import com.qcadoo.mes.costCalculation.constants.CostCalculationConstants;
import com.qcadoo.mes.costCalculation.constants.CostCalculationFields;
import com.qcadoo.mes.costCalculation.constants.MaterialCostsUsed;
import com.qcadoo.mes.costCalculation.constants.SourceOfOperationCosts;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.CheckBoxComponent;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.api.utils.NumberGeneratorService;
import com.qcadoo.view.constants.QcadooViewConstants;

@Service
public class CostCalculationDetailsHooks {

    private static final String L_PRODUCTION_COST_MARGIN_PROC = "productionCostMarginProc";

    private static final String L_MATERIAL_COST_MARGIN_PROC = "materialCostMarginProc";

    private static final String L_REGISTRATION_PRICE_OVERHEAD_PROC = "registrationPriceOverheadProc";

    private static final String TECHNICAL_PRODUCTION_COST_OVERHEAD_PROC = "technicalProductionCostOverheadProc";

    private static final String L_PROFIT_PROC = "profitProc";

    private static final String L_ADDITIONAL_OVERHEAD_CURRENCY = "additionalOverheadCurrency";

    public static final String COST_CALCULATION_RIBBON_MESSAGE_RECORD_ALREADY_GENERATED = "costCalculation.ribbon.message.recordAlreadyGenerated";

    public static final String ACTIONS = "actions";

    public static final String RECORD_NOT_CREATED = "recordNotCreated";

    @Autowired
    private NumberGeneratorService numberGeneratorService;

    @Autowired
    private CurrencyService currencyService;

    @Autowired
    private ParameterService parameterService;

    @Autowired
    private NumberService numbersService;

    public void onBeforeRender(final ViewDefinitionState view) {
        fillOverheadsFromParameters(view);
        setFieldsEnabled(view);
        generateNumber(view);
        fillCurrencyFields(view);
        setButtonsEnabled(view);
    }

    public void setFieldsEnabled(final ViewDefinitionState view) {
        Set<String> referenceNames = Sets.newHashSet(CostCalculationFields.QUANTITY, CostCalculationFields.NUMBER,
                CostCalculationFields.DESCRIPTION, CostCalculationFields.MATERIAL_COSTS_USED, CostCalculationFields.OFFER,
                CostCalculationFields.AVERAGE_LABOR_HOURLY_COST, CostCalculationFields.STANDARD_LABOR_COST,
                CostCalculationFields.PRODUCTION_COST_MARGIN, CostCalculationFields.AVERAGE_MACHINE_HOURLY_COST,
                CostCalculationFields.MATERIAL_COST_MARGIN, CostCalculationFields.ADDITIONAL_OVERHEAD,
                CostCalculationFields.INCLUDE_TPZ, CostCalculationFields.USE_NOMINAL_COST_PRICE_NOT_SPECIFIED,
                CostCalculationFields.INCLUDE_ADDITIONAL_TIME, CostCalculationFields.SOURCE_OF_OPERATION_COSTS,
                CostCalculationFields.REGISTRATION_PRICE_OVERHEAD, CostCalculationFields.TECHNICAL_PRODUCTION_COST_OVERHEAD,
                CostCalculationFields.PROFIT);

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
            FieldComponent materialCostsUsed = (FieldComponent) view
                    .getComponentByReference(CostCalculationFields.MATERIAL_COSTS_USED);
            FieldComponent offer = (FieldComponent) view
                    .getComponentByReference(CostCalculationFields.OFFER);
            FieldComponent sourceOfOperationCosts = (FieldComponent) view
                    .getComponentByReference(CostCalculationFields.SOURCE_OF_OPERATION_COSTS);
            FieldComponent standardLaborCost = (FieldComponent) view
                    .getComponentByReference(CostCalculationFields.STANDARD_LABOR_COST);
            CheckBoxComponent includeTPZ = (CheckBoxComponent) view.getComponentByReference(CostCalculationFields.INCLUDE_TPZ);
            CheckBoxComponent includeAdditionalTime = (CheckBoxComponent) view
                    .getComponentByReference(CostCalculationFields.INCLUDE_ADDITIONAL_TIME);
            FieldComponent averageMachineHourlyCost = (FieldComponent) view
                    .getComponentByReference(CostCalculationFields.AVERAGE_MACHINE_HOURLY_COST);
            FieldComponent averageLaborHourlyCost = (FieldComponent) view
                    .getComponentByReference(CostCalculationFields.AVERAGE_LABOR_HOURLY_COST);
            offer.setEnabled(
                    MaterialCostsUsed.OFFER_COST_OR_LAST_PURCHASE.getStringValue().equals(materialCostsUsed.getFieldValue()));
            offer.setRequired(
                    MaterialCostsUsed.OFFER_COST_OR_LAST_PURCHASE.getStringValue().equals(materialCostsUsed.getFieldValue()));
            if (!MaterialCostsUsed.OFFER_COST_OR_LAST_PURCHASE.getStringValue().equals(materialCostsUsed.getFieldValue())) {
                offer.setFieldValue(null);
            }
            offer.requestComponentUpdateState();
            standardLaborCost.setEnabled(
                    SourceOfOperationCosts.STANDARD_LABOR_COSTS.getStringValue().equals(sourceOfOperationCosts.getFieldValue()));
            standardLaborCost.setRequired(
                    SourceOfOperationCosts.STANDARD_LABOR_COSTS.getStringValue().equals(sourceOfOperationCosts.getFieldValue()));
            if (!SourceOfOperationCosts.STANDARD_LABOR_COSTS.getStringValue().equals(sourceOfOperationCosts.getFieldValue())) {
                standardLaborCost.setFieldValue(null);
            }
            standardLaborCost.requestComponentUpdateState();
            includeTPZ.setEnabled(
                    !SourceOfOperationCosts.STANDARD_LABOR_COSTS.getStringValue().equals(sourceOfOperationCosts.getFieldValue()));
            includeAdditionalTime.setEnabled(
                    !SourceOfOperationCosts.STANDARD_LABOR_COSTS.getStringValue().equals(sourceOfOperationCosts.getFieldValue()));
            averageMachineHourlyCost.setEnabled(
                    SourceOfOperationCosts.PARAMETERS.getStringValue().equals(sourceOfOperationCosts.getFieldValue()));
            averageLaborHourlyCost.setEnabled(
                    SourceOfOperationCosts.PARAMETERS.getStringValue().equals(sourceOfOperationCosts.getFieldValue()));
        }
    }

    public void generateNumber(final ViewDefinitionState view) {
        numberGeneratorService.generateAndInsertNumber(view, CostCalculationConstants.PLUGIN_IDENTIFIER,
                CostCalculationConstants.MODEL_COST_CALCULATION, QcadooViewConstants.L_FORM, CostCalculationFields.NUMBER);
    }

    public void fillCurrencyFields(final ViewDefinitionState viewDefinitionState) {
        final String currencyAlphabeticCode = currencyService.getCurrencyAlphabeticCode();

        Set<String> referenceNames = Sets.newHashSet(L_ADDITIONAL_OVERHEAD_CURRENCY, "averageMachineHourlyCostCurrency",
                "averageLaborHourlyCostCurrency");

        for (String referenceName : referenceNames) {
            FieldComponent fieldComponent = (FieldComponent) viewDefinitionState.getComponentByReference(referenceName);

            fieldComponent.setFieldValue(currencyAlphabeticCode);
            fieldComponent.requestComponentUpdateState();
        }

        fillComponentWithPercent(L_PRODUCTION_COST_MARGIN_PROC, viewDefinitionState);
        fillComponentWithPercent(L_MATERIAL_COST_MARGIN_PROC, viewDefinitionState);
        fillComponentWithPercent(L_REGISTRATION_PRICE_OVERHEAD_PROC, viewDefinitionState);
        fillComponentWithPercent(TECHNICAL_PRODUCTION_COST_OVERHEAD_PROC, viewDefinitionState);
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
            CheckBoxComponent isSetFieldsFromParameter = (CheckBoxComponent) view
                    .getComponentByReference(CostCalculationFields.IS_SET_FIELDS_FROM_PARAMETER);
            if (isSetFieldsFromParameter.isChecked()) {
                return;
            }
            Entity parameter = parameterService.getParameter();
            fillWithProperty(CostCalculationFields.MATERIAL_COSTS_USED,
                    parameter.getStringField(CostCalculationFields.MATERIAL_COSTS_USED), view);
            fillCheckboxWithProperty(CostCalculationFields.USE_NOMINAL_COST_PRICE_NOT_SPECIFIED,
                    parameter.getBooleanField(CostCalculationFields.USE_NOMINAL_COST_PRICE_NOT_SPECIFIED), view);
            fillWithProperty(CostCalculationFields.SOURCE_OF_OPERATION_COSTS,
                    parameter.getStringField(CostCalculationFields.SOURCE_OF_OPERATION_COSTS), view);
            fillLookupWithProperty(CostCalculationFields.STANDARD_LABOR_COST,
                    parameter.getBelongsToField(CostCalculationFields.STANDARD_LABOR_COST), view);
            fillWithPropertyOrZero(CostCalculationFields.AVERAGE_MACHINE_HOURLY_COST,
                    parameter.getDecimalField(CostCalculationFields.AVERAGE_MACHINE_HOURLY_COST), view, false);
            fillWithPropertyOrZero(CostCalculationFields.AVERAGE_LABOR_HOURLY_COST,
                    parameter.getDecimalField(CostCalculationFields.AVERAGE_LABOR_HOURLY_COST), view, false);
            fillCheckboxWithProperty(CostCalculationFields.INCLUDE_TPZ,
                    parameter.getBooleanField(CostCalculationFields.INCLUDE_TPZ), view);
            fillCheckboxWithProperty(CostCalculationFields.INCLUDE_ADDITIONAL_TIME,
                    parameter.getBooleanField(CostCalculationFields.INCLUDE_ADDITIONAL_TIME), view);

            fillWithPropertyOrZero(CostCalculationFields.MATERIAL_COST_MARGIN,
                    parameter.getDecimalField(CostCalculationFields.MATERIAL_COST_MARGIN), view, true);
            fillWithPropertyOrZero(CostCalculationFields.PRODUCTION_COST_MARGIN,
                    parameter.getDecimalField(CostCalculationFields.PRODUCTION_COST_MARGIN), view, true);
            fillWithPropertyOrZero(CostCalculationFields.ADDITIONAL_OVERHEAD,
                    parameter.getDecimalField(CostCalculationFields.ADDITIONAL_OVERHEAD), view, true);
            fillWithPropertyOrZero(CostCalculationFields.REGISTRATION_PRICE_OVERHEAD,
                    parameter.getDecimalField(CostCalculationFields.REGISTRATION_PRICE_OVERHEAD), view, true);
            fillWithPropertyOrZero(CostCalculationFields.TECHNICAL_PRODUCTION_COST_OVERHEAD,
                    parameter.getDecimalField(CostCalculationFields.TECHNICAL_PRODUCTION_COST_OVERHEAD), view, true);
            fillWithPropertyOrZero(CostCalculationFields.PROFIT, parameter.getDecimalField(CostCalculationFields.PROFIT), view,
                    true);
            isSetFieldsFromParameter.setFieldValue(true);
            isSetFieldsFromParameter.requestComponentUpdateState();
        }
    }

    private void fillCheckboxWithProperty(String componentName, boolean propertyValue, ViewDefinitionState view) {
        CheckBoxComponent component = (CheckBoxComponent) view.getComponentByReference(componentName);
        component.setFieldValue(propertyValue);
    }

    private void fillLookupWithProperty(String componentName, Entity propertyValue, ViewDefinitionState view) {
        LookupComponent component = (LookupComponent) view.getComponentByReference(componentName);
        if (propertyValue != null) {
            component.setFieldValue(propertyValue.getId());
        }
    }

    private void fillWithProperty(String componentName, String propertyValue, ViewDefinitionState view) {
        FieldComponent component = (FieldComponent) view.getComponentByReference(componentName);
        if (propertyValue != null) {
            component.setFieldValue(propertyValue);
        }
    }

    private void fillWithPropertyOrZero(String componentName, BigDecimal propertyValue, ViewDefinitionState view,
                                        boolean defaultValue) {
        FieldComponent component = (FieldComponent) view.getComponentByReference(componentName);
        if (component.getFieldValue() == null) {
            if (propertyValue != null) {
                String formattedProductionCostMargin = numbersService.formatWithMinimumFractionDigits(propertyValue.setScale(2),
                        0);
                component.setFieldValue(formattedProductionCostMargin);
            } else if (defaultValue) {
                component.setFieldValue(0);
            }
        }
    }

    private void setButtonsEnabled(ViewDefinitionState view) {
        WindowComponent window = (WindowComponent) view.getComponentByReference(QcadooViewConstants.L_WINDOW);
        RibbonActionItem saveNominalCosts = window.getRibbon().getGroupByName(CostCalculationFields.SAVE_COSTS)
                .getItemByName(CostCalculationFields.NOMINAL_COSTS);
        RibbonActionItem generate = window.getRibbon().getGroupByName("generate").getItemByName("generate");
        RibbonActionItem pdfButton = window.getRibbon().getGroupByName("export").getItemByName("pdf");
        RibbonActionItem xlsButton = window.getRibbon().getGroupByName("export").getItemByName("xls");
        RibbonActionItem save = window.getRibbon().getGroupByName(ACTIONS).getItemByName("save");
        RibbonActionItem saveBack = window.getRibbon().getGroupByName(ACTIONS).getItemByName("saveBack");
        RibbonActionItem saveNew = window.getRibbon().getGroupByName(ACTIONS).getItemByName("saveNew");
        RibbonActionItem copy = window.getRibbon().getGroupByName(ACTIONS).getItemByName("copy");
        RibbonActionItem cancel = window.getRibbon().getGroupByName(ACTIONS).getItemByName("cancel");
        FormComponent form = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        boolean pdf = form.getPersistedEntityWithIncludedFormValues().getBooleanField(CostCalculationFields.PDF);

        boolean entityExists = form.getEntityId() != null;

        if (entityExists) {
            CheckBoxComponent generatedField = (CheckBoxComponent) view.getComponentByReference(CostCalculationFields.GENERATED);
            int technologiesCount = ((GridComponent) view.getComponentByReference(CostCalculationFields.TECHNOLOGIES))
                    .getEntities().size();
            generate.setEnabled(technologiesCount > 0);
            generate.requestUpdate(true);
            saveNominalCosts.setEnabled(generatedField.isChecked());
            saveNominalCosts.requestUpdate(true);
            if (generatedField.isChecked()) {
                if (pdf) {
                    pdfButton.setEnabled(true);
                    pdfButton.requestUpdate(true);
                } else {
                    xlsButton.setEnabled(true);
                    xlsButton.requestUpdate(true);
                }
                generate.setEnabled(false);
                generate.requestUpdate(true);
                save.setEnabled(false);
                save.setMessage(COST_CALCULATION_RIBBON_MESSAGE_RECORD_ALREADY_GENERATED);
                save.requestUpdate(true);
                saveNew.setEnabled(false);
                saveNew.setMessage(COST_CALCULATION_RIBBON_MESSAGE_RECORD_ALREADY_GENERATED);
                saveNew.requestUpdate(true);
                saveBack.setEnabled(false);
                saveBack.setMessage(COST_CALCULATION_RIBBON_MESSAGE_RECORD_ALREADY_GENERATED);
                saveBack.requestUpdate(true);
                cancel.setEnabled(false);
                cancel.setMessage(COST_CALCULATION_RIBBON_MESSAGE_RECORD_ALREADY_GENERATED);
            } else {
                pdfButton.setEnabled(false);
                pdfButton.setMessage("costCalculation.ribbon.message.recordNotGenerated");
                pdfButton.requestUpdate(true);
                xlsButton.setEnabled(false);
                xlsButton.setMessage("costCalculation.ribbon.message.recordNotGenerated");
                xlsButton.requestUpdate(true);
                save.setEnabled(true);
                save.requestUpdate(true);
                cancel.setEnabled(true);
            }
        } else {
            copy.setEnabled(false);
            copy.setMessage(RECORD_NOT_CREATED);
            copy.requestUpdate(true);
            pdfButton.setEnabled(false);
            pdfButton.setMessage(RECORD_NOT_CREATED);
            pdfButton.requestUpdate(true);
            xlsButton.setEnabled(false);
            xlsButton.setMessage(RECORD_NOT_CREATED);
            xlsButton.requestUpdate(true);
            save.setEnabled(true);
            save.requestUpdate(true);
            cancel.setEnabled(true);
        }
        cancel.requestUpdate(true);
    }
}
