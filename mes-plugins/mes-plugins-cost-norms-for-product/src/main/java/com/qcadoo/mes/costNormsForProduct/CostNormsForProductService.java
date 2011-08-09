package com.qcadoo.mes.costNormsForProduct;

import java.util.Arrays;

import org.springframework.stereotype.Service;

import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;

@Service
public class CostNormsForProductService {

	/* ****** VIEW HOOKS ******* */
	
	public void fillCostTabUnit(final ViewDefinitionState viewDefinitionState) {
		FormComponent form = (FormComponent) viewDefinitionState.getComponentByReference("form");
		ComponentState costUnit = (ComponentState) viewDefinitionState.getComponentByReference("costTabUnit"); 
		Entity product = form.getEntity();
		
		if(product == null || product.getId() == null) {
			return;
		}
		
		costUnit.setFieldValue(product.getField("unit").toString());
		costUnit.setEnabled(false);
	}

	public void fillCostTabCurrency(final ViewDefinitionState viewDefinitionState) {
		for(String componentReference : Arrays.asList("nominalCostCurrency", "lastPurchaseCostCurrency", "averageCostCurrency")) {
			//temporary
			viewDefinitionState.getComponentByReference(componentReference).setFieldValue("PLN");
			viewDefinitionState.getComponentByReference(componentReference).setEnabled(false);
		}
	}
	
	/* ****** CUSTOM EVENT LISTENER ****** */
	

	
	/* ****** VALIDATORS ****** */

	
}
