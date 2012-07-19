package com.qcadoo.mes.avgLaborCostCalcForOrder.hooks;

import static com.qcadoo.mes.avgLaborCostCalcForOrder.constants.AvgLaborCostCalcForOrderFields.AVERAGE_LABOR_HOURLY_COST;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.ribbon.RibbonActionItem;

@Service
public class AvgLaborCostCalcForOrderDetailsHooks {

    public void enabledButtonForCopyNorms(final ViewDefinitionState view) {
        WindowComponent window = (WindowComponent) view.getComponentByReference("window");
        RibbonActionItem copyToOperationsNorms = window.getRibbon().getGroupByName("hourlyCostNorms")
                .getItemByName("copyToOperationsNorms");
        FieldComponent averageLaborHourlyCost = (FieldComponent) view.getComponentByReference(AVERAGE_LABOR_HOURLY_COST);
        if (StringUtils.isEmpty((String) averageLaborHourlyCost.getFieldValue())) {
            copyToOperationsNorms.setEnabled(false);
        } else {
            copyToOperationsNorms.setEnabled(true);
        }
        copyToOperationsNorms.requestUpdate(true);
    }
}
