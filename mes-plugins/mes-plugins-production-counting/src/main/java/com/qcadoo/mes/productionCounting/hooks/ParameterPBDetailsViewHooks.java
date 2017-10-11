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

import com.qcadoo.mes.basic.util.CurrencyService;
import com.qcadoo.mes.costCalculation.constants.CalculateMaterialCostsMode;
import com.qcadoo.mes.costCalculation.constants.SourceOfMaterialCosts;
import com.qcadoo.mes.productionCounting.constants.CalculateOperationCostsMode;
import com.qcadoo.mes.productionCounting.constants.ParameterFieldsPC;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class ParameterPBDetailsViewHooks {

    @Autowired
    private CurrencyService currencyService;

    public void fillCurrencyAndUnitFields(final ViewDefinitionState viewDefinitionState) {
        String currencyAlphabeticCode = currencyService.getCurrencyAlphabeticCode();

        List<String> currencyFieldNames = Arrays.asList("averageMachineHourlyCostPBCurrency", "averageLaborHourlyCostPBCurrency",
                "additionalOverheadPBCurrency");

        for (String currencyFieldName : currencyFieldNames) {
            FieldComponent fieldComponent = (FieldComponent) viewDefinitionState.getComponentByReference(currencyFieldName);
            fieldComponent.setFieldValue(currencyAlphabeticCode);
            fieldComponent.requestComponentUpdateState();
        }

        fillComponentWithPercent("productionCostMarginPBProc", viewDefinitionState);
        fillComponentWithPercent("materialCostMarginPBProc", viewDefinitionState);
        fillComponentWithPercent("registrationPriceOverheadPBProc", viewDefinitionState);
        fillComponentWithPercent("profitPBProc", viewDefinitionState);

    }

    private void fillComponentWithPercent(String componentName, ViewDefinitionState viewDefinitionState) {
        FieldComponent materialCostMarginProc = (FieldComponent) viewDefinitionState
            .getComponentByReference(componentName);
        materialCostMarginProc.setFieldValue("%");
        materialCostMarginProc.requestComponentUpdateState();
    }

    public void onSourceOfMaterialCostsChange(final ViewDefinitionState viewDefinitionState, final ComponentState state, final String[] args) {
        FieldComponent costsMode = (FieldComponent) viewDefinitionState
                .getComponentByReference(ParameterFieldsPC.SOURCE_OF_MATERIAL_COSTS_PB);
        if(SourceOfMaterialCosts.FROM_ORDERS_MATERIAL_COSTS.getStringValue().equals((String) costsMode.getFieldValue())){
            FieldComponent calculateMaterialCostsMode = (FieldComponent) viewDefinitionState
                    .getComponentByReference(ParameterFieldsPC.CALCULATE_MATERIAL_COSTS_MODE_PB);
            calculateMaterialCostsMode.setFieldValue(CalculateMaterialCostsMode.COST_FOR_ORDER.getStringValue());
        }

    }
    public void disableCheckboxes(final ViewDefinitionState viewDefinitionState, final ComponentState state, final String[] args) {
        disableCheckboxes(viewDefinitionState);
    }

    public void disableCheckboxes(final ViewDefinitionState viewDefinitionState) {
        FieldComponent calculateOperationCostsMode = (FieldComponent) viewDefinitionState
                .getComponentByReference(ParameterFieldsPC.CALCULATE_OPERATION_COST_MODE_PB);

        FieldComponent includeTPZ = (FieldComponent) viewDefinitionState
                .getComponentByReference(ParameterFieldsPC.INCLUDE_TPZ_PB);
        FieldComponent includeAdditionalTime = (FieldComponent) viewDefinitionState
                .getComponentByReference(ParameterFieldsPC.INCLUDE_ADDITIONAL_TIME_PB);

        if (CalculateOperationCostsMode.PIECEWORK.getStringValue().equals(calculateOperationCostsMode.getFieldValue())) {
            includeTPZ.setFieldValue(false);
            includeTPZ.setEnabled(false);
            includeTPZ.requestComponentUpdateState();

            includeAdditionalTime.setFieldValue(false);
            includeAdditionalTime.setEnabled(false);
            includeAdditionalTime.requestComponentUpdateState();
        } else {
            includeTPZ.setEnabled(true);
            includeTPZ.requestComponentUpdateState();

            includeAdditionalTime.setEnabled(true);
            includeAdditionalTime.requestComponentUpdateState();
        }
    }
}
