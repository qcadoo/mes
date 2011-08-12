package com.qcadoo.mes.costNormsForProduct;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;

@Service
public class CostNormsForProductService {

    @Autowired
    private DataDefinitionService dataDefinitionService; 
    
	/* ****** VIEW HOOKS ******* */
	
	public void fillCostTabUnit(final ViewDefinitionState viewDefinitionState) {
		FormComponent form = (FormComponent) viewDefinitionState.getComponentByReference("form");
		FieldComponent costUnit = (FieldComponent) viewDefinitionState.getComponentByReference("costTabUnit"); 
		Entity product;
		
		//TODO: check formName and get properly product id!
		if("product".equals(form.getName())) {
		    product = dataDefinitionService.get("basic", "product").get(form.getEntityId());
		} else if("form".equals(form.getName())) {
		    product = dataDefinitionService.get("technologies", "operationProductInComponent").get(form.getEntityId()).getBelongsToField("product");
		} else {
		    return;
		}
		
//		Entity product = dataDefinitionService.get("basic", "product").get(form.getEntityId());
		if(product == null) {
			return;
		}

		costUnit.setFieldValue(product.getStringField("unit"));
		costUnit.requestComponentUpdateState();
		costUnit.setEnabled(false);
	}

	public void fillCostTabCurrency(final ViewDefinitionState viewDefinitionState) {
		for(String componentReference : Arrays.asList("nominalCostCurrency", "lastPurchaseCostCurrency", "averageCostCurrency")) {
		    FieldComponent field = (FieldComponent) viewDefinitionState.getComponentByReference(componentReference);
		    field.setEnabled(true);
		    //temporary
		    field.setFieldValue("PLN");
		    field.setEnabled(false);
		    field.requestComponentUpdateState();
		}
	}
	
	/* ****** CUSTOM EVENT LISTENER ****** */
	

	
	/* ****** VALIDATORS ****** */

	
}
