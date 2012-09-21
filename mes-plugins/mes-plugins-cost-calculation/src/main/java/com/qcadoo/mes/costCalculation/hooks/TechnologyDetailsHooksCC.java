package com.qcadoo.mes.costCalculation.hooks;

import org.springframework.stereotype.Service;

import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.api.ribbon.RibbonGroup;

@Service
public class TechnologyDetailsHooksCC {
	public void updateViewCostsCalculationButtonState(final ViewDefinitionState view){
		FormComponent orderForm = (FormComponent) view.getComponentByReference("form");

        WindowComponent window = (WindowComponent) view.getComponentByReference("window");
        RibbonGroup materials = (RibbonGroup) window.getRibbon().getGroupByName("costCalculate");
        RibbonActionItem costCalculate = (RibbonActionItem) materials.getItemByName("costCalculate");
        if(orderForm.getEntityId()==null){
        	costCalculate.setEnabled(false);
        }else{
        	costCalculate.setEnabled(true);
        }
        costCalculate.requestUpdate(true);
	}
}
