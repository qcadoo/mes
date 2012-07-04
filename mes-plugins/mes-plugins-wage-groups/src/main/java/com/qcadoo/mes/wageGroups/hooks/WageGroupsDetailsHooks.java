package com.qcadoo.mes.wageGroups.hooks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.util.CurrencyService;
import com.qcadoo.mes.wageGroups.constants.WageGroupFields;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;

@Service
public class WageGroupsDetailsHooks {

    @Autowired
    private CurrencyService currencyService;

    public void setCurrency(final ViewDefinitionState view) {
        FieldComponent individualLaborCostUNIT = (FieldComponent) view
                .getComponentByReference(WageGroupFields.LABOR_HOURLY_COST_CURRENCY);
        individualLaborCostUNIT.setFieldValue(currencyService.getCurrencyAlphabeticCode());
        individualLaborCostUNIT.requestComponentUpdateState();
    }
}
