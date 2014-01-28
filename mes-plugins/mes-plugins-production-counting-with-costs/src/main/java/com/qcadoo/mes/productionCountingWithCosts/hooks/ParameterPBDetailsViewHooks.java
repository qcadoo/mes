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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.util.CurrencyService;
import com.qcadoo.mes.productionCounting.internal.constants.CalculateOperationCostsMode;
import com.qcadoo.mes.productionCountingWithCosts.constants.ParameterFieldsPCWC;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;

@Service
public class ParameterPBDetailsViewHooks {

    @Autowired
    private CurrencyService currencyService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void fillCurrencyAndUnitFields(final ViewDefinitionState viewDefinitionState) {
        String currencyAlphabeticCode = currencyService.getCurrencyAlphabeticCode();

        List<String> currencyFieldNames = Arrays.asList("averageMachineHourlyCostPBCurrency", "averageLaborHourlyCostPBCurrency",
                "additionalOverheadPBCurrency");

        for (String currencyFieldName : currencyFieldNames) {
            FieldComponent fieldComponent = (FieldComponent) viewDefinitionState.getComponentByReference(currencyFieldName);
            fieldComponent.setFieldValue(currencyAlphabeticCode);
            fieldComponent.requestComponentUpdateState();
        }

        FieldComponent productionCostMarginProc = (FieldComponent) viewDefinitionState
                .getComponentByReference("productionCostMarginPBProc");
        productionCostMarginProc.setFieldValue("%");
        productionCostMarginProc.requestComponentUpdateState();

        FieldComponent materialCostMarginProc = (FieldComponent) viewDefinitionState
                .getComponentByReference("materialCostMarginPBProc");
        materialCostMarginProc.setFieldValue("%");
        materialCostMarginProc.requestComponentUpdateState();

    }

    public void disableCheckboxes(final ViewDefinitionState viewDefinitionState, final ComponentState state, final String[] args) {
        disableCheckboxes(viewDefinitionState);
    }

    public void disableCheckboxes(final ViewDefinitionState viewDefinitionState) {
        FieldComponent calculateOperationCostsMode = (FieldComponent) viewDefinitionState
                .getComponentByReference(ParameterFieldsPCWC.CALCULATE_OPERATION_COST_MODE_PB);

        FieldComponent includeTPZ = (FieldComponent) viewDefinitionState
                .getComponentByReference(ParameterFieldsPCWC.INCLUDE_TPZ_PB);
        FieldComponent includeAdditionalTime = (FieldComponent) viewDefinitionState
                .getComponentByReference(ParameterFieldsPCWC.INCLUDE_ADDITIONAL_TIME_PB);

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
