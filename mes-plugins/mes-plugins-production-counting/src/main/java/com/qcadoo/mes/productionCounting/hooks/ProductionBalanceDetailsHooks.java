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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.basic.util.CurrencyService;
import com.qcadoo.mes.productionCounting.ProductionCountingService;
import com.qcadoo.mes.productionCounting.constants.ParameterFieldsPC;
import com.qcadoo.mes.productionCounting.constants.ProductionBalanceFields;
import com.qcadoo.mes.productionCounting.constants.ProductionCountingConstants;
import com.qcadoo.model.api.BigDecimalUtils;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.CheckBoxComponent;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.utils.NumberGeneratorService;
import com.qcadoo.view.constants.QcadooViewConstants;

@Service
public class ProductionBalanceDetailsHooks {

    private static final List<String> L_FIELDS_AND_CHECKBOXES = Arrays.asList(ProductionBalanceFields.NAME,
            ProductionBalanceFields.DESCRIPTION, ProductionBalanceFields.INCLUDE_WAGE_GROUPS, ProductionBalanceFields.INCLUDE_TPZ,
            ProductionBalanceFields.INCLUDE_ADDITIONAL_TIME);

    private static final List<String> L_FIELDS = L_FIELDS_AND_CHECKBOXES.subList(0, L_FIELDS_AND_CHECKBOXES.size() - 2);

    private static final List<String> L_COST_FIELDS = Arrays.asList(ProductionBalanceFields.MATERIAL_COSTS_USED,
            ProductionBalanceFields.PRODUCTION_COST_MARGIN, ProductionBalanceFields.MATERIAL_COST_MARGIN,
            ProductionBalanceFields.ADDITIONAL_OVERHEAD, ProductionBalanceFields.SOURCE_OF_OPERATION_COSTS,
            ProductionBalanceFields.REGISTRATION_PRICE_OVERHEAD, ProductionBalanceFields.TECHNICAL_PRODUCTION_COST_OVERHEAD,
            ProductionBalanceFields.PROFIT);

    private static final List<String> L_COST_GRIDS = Collections.singletonList(ProductionBalanceFields.ORDERS);

    @Autowired
    private CurrencyService currencyService;

    @Autowired
    private ParameterService parameterService;

    @Autowired
    private NumberService numberService;

    @Autowired
    private ProductionCountingService productionCountingService;

    @Autowired
    private NumberGeneratorService numberGeneratorService;

    public void generateOrderNumber(final ViewDefinitionState view) {
        numberGeneratorService.generateAndInsertNumber(view, ProductionCountingConstants.PLUGIN_IDENTIFIER,
                ProductionCountingConstants.MODEL_PRODUCTION_BALANCE, QcadooViewConstants.L_FORM, ProductionBalanceFields.NUMBER);
    }

    public void disableFieldsAndGridsWhenGenerated(final ViewDefinitionState view) {
        CheckBoxComponent generatedCheckBox = (CheckBoxComponent) view.getComponentByReference(ProductionBalanceFields.GENERATED);

        if (generatedCheckBox.isChecked()) {
            productionCountingService.setComponentsState(view, L_FIELDS_AND_CHECKBOXES, false, true);
        } else {
            productionCountingService.setComponentsState(view, L_FIELDS, true, true);
        }

        if (generatedCheckBox.isChecked()) {
            productionCountingService.setComponentsState(view, L_COST_FIELDS, false, true);
            productionCountingService.setComponentsState(view, L_COST_GRIDS, false, false);
        } else {
            productionCountingService.setComponentsState(view, L_COST_FIELDS, true, true);
            productionCountingService.setComponentsState(view, L_COST_GRIDS, true, false);
        }

        FormComponent form = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        if (form.getEntityId() == null) {
            productionCountingService.setComponentsState(view, L_COST_GRIDS, false, false);
        }
    }

    public void fillCurrencyAndUnitFields(final ViewDefinitionState view) {
        String currencyAlphabeticCode = currencyService.getCurrencyAlphabeticCode();

        List<String> currencyFieldNames = Lists.newArrayList("additionalOverheadCurrency");

        for (String currencyFieldName : currencyFieldNames) {
            FieldComponent fieldComponent = (FieldComponent) view.getComponentByReference(currencyFieldName);
            fieldComponent.setFieldValue(currencyAlphabeticCode);
            fieldComponent.requestComponentUpdateState();
        }

        List<String> procFields = Lists.newArrayList("productionCostMarginProc", "materialCostMarginProc",
                "registrationPriceOverheadProc", "technicalProductionCostOverheadProc", "profitProc");
        for (String field : procFields) {
            FieldComponent materialCostMarginProc = (FieldComponent) view.getComponentByReference(field);
            materialCostMarginProc.setFieldValue("%");
            materialCostMarginProc.requestComponentUpdateState();
        }
    }

    public void setTheFieldBasedOnParameters(final ViewDefinitionState view) {
        FormComponent form = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        if (form.getEntityId() == null) {
            Entity parameter = parameterService.getParameter();

            FieldComponent sourceOfOperationCosts = (FieldComponent) view
                    .getComponentByReference(ProductionBalanceFields.SOURCE_OF_OPERATION_COSTS);
            if (parameter.getField(ParameterFieldsPC.SOURCE_OF_OPERATION_COSTS_PB) != null) {
                sourceOfOperationCosts.setFieldValue(parameter.getField(ParameterFieldsPC.SOURCE_OF_OPERATION_COSTS_PB));
                sourceOfOperationCosts.requestComponentUpdateState();
            }
            FieldComponent materialCostsUsed = (FieldComponent) view
                    .getComponentByReference(ProductionBalanceFields.MATERIAL_COSTS_USED);
            if (parameter.getField(ParameterFieldsPC.MATERIAL_COSTS_USED_PB) != null) {

                materialCostsUsed.setFieldValue(parameter.getField(ParameterFieldsPC.MATERIAL_COSTS_USED_PB));
                materialCostsUsed.requestComponentUpdateState();
            }
            FieldComponent includeTPZ = (FieldComponent) view.getComponentByReference(ProductionBalanceFields.INCLUDE_TPZ);
            includeTPZ.setFieldValue(parameter.getBooleanField(ParameterFieldsPC.INCLUDE_TPZ_PB));
            includeTPZ.requestComponentUpdateState();

            FieldComponent includeAdditionalTime = (FieldComponent) view
                    .getComponentByReference(ProductionBalanceFields.INCLUDE_ADDITIONAL_TIME);
            includeAdditionalTime.setFieldValue(parameter.getBooleanField(ParameterFieldsPC.INCLUDE_ADDITIONAL_TIME_PB));
            includeAdditionalTime.requestComponentUpdateState();

            FieldComponent includeWageGroups = (FieldComponent) view
                    .getComponentByReference(ProductionBalanceFields.INCLUDE_WAGE_GROUPS);
            includeWageGroups.setFieldValue(parameter.getBooleanField(ParameterFieldsPC.INCLUDE_WAGE_GROUPS));
            includeWageGroups.requestComponentUpdateState();

            FieldComponent productionCostMargin = (FieldComponent) view
                    .getComponentByReference(ProductionBalanceFields.PRODUCTION_COST_MARGIN);
            productionCostMargin.setFieldValue(numberService.format(
                    BigDecimalUtils.convertNullToZero(parameter.getDecimalField(ParameterFieldsPC.PRODUCTION_COST_MARGIN_PB))));
            productionCostMargin.requestComponentUpdateState();

            FieldComponent materialCostMargin = (FieldComponent) view
                    .getComponentByReference(ProductionBalanceFields.MATERIAL_COST_MARGIN);
            materialCostMargin.setFieldValue(numberService.format(
                    BigDecimalUtils.convertNullToZero(parameter.getDecimalField(ParameterFieldsPC.MATERIAL_COST_MARGIN_PB))));
            materialCostMargin.requestComponentUpdateState();

            FieldComponent additionalOverhead = (FieldComponent) view
                    .getComponentByReference(ProductionBalanceFields.ADDITIONAL_OVERHEAD);
            additionalOverhead.setFieldValue(numberService.format(
                    BigDecimalUtils.convertNullToZero(parameter.getDecimalField(ParameterFieldsPC.ADDITIONAL_OVERHEAD_PB))));
            additionalOverhead.requestComponentUpdateState();

            FieldComponent registrationPriceOverhead = (FieldComponent) view
                    .getComponentByReference(ProductionBalanceFields.REGISTRATION_PRICE_OVERHEAD);
            registrationPriceOverhead.setFieldValue(numberService.format(BigDecimalUtils
                    .convertNullToZero(parameter.getDecimalField(ParameterFieldsPC.REGISTRATION_PRICE_OVERHEAD_PB))));
            registrationPriceOverhead.requestComponentUpdateState();

            FieldComponent technicalProductionCostOverhead = (FieldComponent) view
                    .getComponentByReference(ProductionBalanceFields.TECHNICAL_PRODUCTION_COST_OVERHEAD);
            technicalProductionCostOverhead.setFieldValue(numberService.format(BigDecimalUtils
                    .convertNullToZero(parameter.getDecimalField(ParameterFieldsPC.TECHNICAL_PRODUCTION_COST_OVERHEAD_PB))));
            technicalProductionCostOverhead.requestComponentUpdateState();

            FieldComponent profit = (FieldComponent) view.getComponentByReference(ProductionBalanceFields.PROFIT);
            profit.setFieldValue(numberService
                    .format(BigDecimalUtils.convertNullToZero(parameter.getDecimalField(ParameterFieldsPC.PROFIT_PB))));
            profit.requestComponentUpdateState();
        }
    }
}
