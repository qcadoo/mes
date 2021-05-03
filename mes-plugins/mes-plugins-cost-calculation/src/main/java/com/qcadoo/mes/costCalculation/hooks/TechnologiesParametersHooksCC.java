package com.qcadoo.mes.costCalculation.hooks;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.util.CurrencyService;
import com.qcadoo.mes.costCalculation.constants.CostCalculationConstants;
import com.qcadoo.mes.costCalculation.constants.CostCalculationFields;
import com.qcadoo.mes.costCalculation.constants.SourceOfOperationCosts;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;

@Service
public class TechnologiesParametersHooksCC {

    @Autowired
    private CurrencyService currencyService;

    public void fillCurrencyAndUnitFields(final ViewDefinitionState viewDefinitionState) {
        String currencyAlphabeticCode = currencyService.getCurrencyAlphabeticCode();

        List<String> currencyFieldNames = Arrays.asList("averageMachineHourlyCostCurrency", "averageLaborHourlyCostCurrency",
                "additionalOverheadCurrency");

        for (String currencyFieldName : currencyFieldNames) {
            FieldComponent fieldComponent = (FieldComponent) viewDefinitionState.getComponentByReference(currencyFieldName);
            fieldComponent.setFieldValue(currencyAlphabeticCode);
            fieldComponent.requestComponentUpdateState();
        }

        fillComponentWithPercent("productionCostMarginProc", viewDefinitionState);
        fillComponentWithPercent("materialCostMarginProc", viewDefinitionState);
        fillComponentWithPercent("registrationPriceOverheadProc", viewDefinitionState);
        fillComponentWithPercent("technicalProductionCostOverheadProc", viewDefinitionState);
        fillComponentWithPercent("profitProc", viewDefinitionState);

        FieldComponent sourceOfOperationCosts = (FieldComponent) viewDefinitionState
                .getComponentByReference(CostCalculationFields.SOURCE_OF_OPERATION_COSTS);
        FieldComponent standardLaborCost = (FieldComponent) viewDefinitionState
                .getComponentByReference(CostCalculationConstants.MODEL_STANDARD_LABOR_COST);
        standardLaborCost.setEnabled(
                SourceOfOperationCosts.STANDARD_LABOR_COSTS.getStringValue().equals(sourceOfOperationCosts.getFieldValue()));
        if (!SourceOfOperationCosts.STANDARD_LABOR_COSTS.getStringValue().equals(sourceOfOperationCosts.getFieldValue())) {
            standardLaborCost.setFieldValue(null);
        }
        standardLaborCost.requestComponentUpdateState();
    }

    private void fillComponentWithPercent(String componentName, ViewDefinitionState viewDefinitionState) {
        FieldComponent materialCostMarginProc = (FieldComponent) viewDefinitionState.getComponentByReference(componentName);
        materialCostMarginProc.setFieldValue("%");
        materialCostMarginProc.requestComponentUpdateState();
    }
}
