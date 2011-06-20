package com.qcadoo.mes.productionScheduling;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityList;
import com.qcadoo.model.api.EntityTree;
import com.qcadoo.model.api.FieldDefinition;
import com.qcadoo.model.api.validators.ErrorMessage;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;

@Service
public class OperationService {

	public void changeCountRealizedOperation(
			final ViewDefinitionState viewDefinitionState,
			final ComponentState state, final String[] args) {
		FieldComponent countRealizedOperation = (FieldComponent) viewDefinitionState
				.getComponentByReference("countRealizedOperation");
		FieldComponent countMachineOperation = (FieldComponent) viewDefinitionState
				.getComponentByReference("countMachineOperation");

		if (countRealizedOperation.getFieldValue().equals("02specified")) {
			countMachineOperation.setEnabled(true);
		} else {
			countMachineOperation.setEnabled(false);
		}
	}
	
		
	public void changeDfltValue(final ViewDefinitionState viewDefinitionState,
			final ComponentState state, final String[] args) {

		FieldComponent dfltValue = (FieldComponent) viewDefinitionState
				.getComponentByReference("dfltValue");
		FieldComponent tpz = (FieldComponent) viewDefinitionState
				.getComponentByReference("tpz");
		FieldComponent tj = (FieldComponent) viewDefinitionState
				.getComponentByReference("tj");
		FieldComponent parallel = (FieldComponent) viewDefinitionState
				.getComponentByReference("parallel");
		FieldComponent activeMachine = (FieldComponent) viewDefinitionState
				.getComponentByReference("activeMachine");
		

		if (dfltValue.getFieldValue().equals("1")) {
			tpz.setEnabled(false);
			tj.setEnabled(false);
			parallel.setEnabled(false);
			activeMachine.setEnabled(false);
			
		} else {
			tpz.setEnabled(true);
			tj.setEnabled(true);
			parallel.setEnabled(true);
			activeMachine.setEnabled(true);
		}
	}
	
	
}
