package com.qcadoo.mes.costNormsForOperation;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;

@Service
public class CostNormsForOperationService {

	@Autowired
	private DataDefinitionService dataDefinitionService;
	
	/* ****** VIEW HOOKS ******* */
	
	public void inheirtOperationCostValuesFromOperation(final ViewDefinitionState viewDefinitionState) {	
		FormComponent form = (FormComponent) viewDefinitionState.getComponentByReference("form");	
		// form.getEntity() may will also return that entity, but in detached state!
		Entity entity = dataDefinitionService
							.get("technologies", "technologyOperationComponent")
								.get(form.getEntityId());
		
		applyCostValuesFromoperation(viewDefinitionState, entity.getBelongsToField("operation"));
	}
	
	public void inheirtOperationCostValuesFromTechnology(final ViewDefinitionState viewDefinitionState) {
		FormComponent form = (FormComponent) viewDefinitionState.getComponentByReference("form");
		// form.getEntity() may will also return that entity, but in detached state!
		Entity entity = dataDefinitionService
							.get("productionScheduling", "orderOperationComponent")
								.get(form.getEntityId());
		
		applyCostValuesFromoperation(viewDefinitionState, entity.getBelongsToField("technologyOperationComponent"));
	}
	
	private void applyCostValuesFromoperation(final ViewDefinitionState viewDefinitionState, final Entity source) {
		if (source != null) {
			for(String componentReference : Arrays.asList("pieceworkCost", "numberOfOperations", "laborHourlyCost", "machineHourlyCost")) {
				ComponentState component = (ComponentState) viewDefinitionState.getComponentByReference(componentReference);
				if (component.getFieldValue().toString().isEmpty()) {
					component.setFieldValue(source.getField(componentReference));
				}
			}
		}
	}
}
