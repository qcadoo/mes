package com.qcadoo.mes.productionScheduling;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.api.search.SearchResult;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;

@Service
public class OperationService {

	@Autowired
	private DataDefinitionService dataDefinitionService;

	/* listener */
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

	/* hook */
	public void updateCountMachineOperationFieldStateonWindowLoad(
			final ViewDefinitionState viewDefinitionState) {

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

	/* listener */
	public void updateFieldsStateWhenDefaultValueCheckboxChanged(
			final ViewDefinitionState viewDefinitionState,
			final ComponentState state, final String[] args) {
		FieldComponent dfltValue = (FieldComponent) viewDefinitionState
				.getComponentByReference("useDefaultValue");
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

	/* hook */
	public void updateFieldsStateOnWindowLoad(
			final ViewDefinitionState viewDefinitionState) {
		FieldComponent dfltValue = (FieldComponent) viewDefinitionState
				.getComponentByReference("useDefaultValue");
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

	public void selectMachineInOperationComponent(
			final ViewDefinitionState state,
			final ComponentState componentState, final String[] args) {
		if (!(componentState instanceof FieldComponent)) {
			throw new IllegalStateException(
					"component is not FieldComponentState");
		}

		if (componentState.getFieldValue() == null) {
			return;
		}

		Long machineId = (Long) componentState.getFieldValue();

		Entity machine = dataDefinitionService.get("basic", "machine").get(
				machineId);

		if (machine == null) {
			return;
		}

		FieldComponent tpz = (FieldComponent) state
				.getComponentByReference("tpz");
		FieldComponent tj = (FieldComponent) state
				.getComponentByReference("tj");
		FieldComponent parallel = (FieldComponent) state
				.getComponentByReference("parallel");
		FieldComponent active = (FieldComponent) state
				.getComponentByReference("activeMachine");

		if (machine.getField("tpz") != null) {
			tpz.setFieldValue(machine.getField("tpz"));
		} else {
			tpz.setFieldValue("");
		}

		if (machine.getField("tj") != null) {
			tj.setFieldValue(machine.getField("tj"));
		} else {
			tj.setFieldValue("");
		}

		if (machine.getField("parallel") != null) {
			parallel.setFieldValue(machine.getField("parallel"));
		} else {
			parallel.setFieldValue("");
		}

		if (machine.getField("ofMachine") != null) {
			active.setFieldValue(!(Boolean) machine.getField("ofMachine"));
		} else {
			active.setFieldValue(false);
		}
	}

	public boolean checkMachineInOperationComponentUniqueness(
			final DataDefinition dataDefinition, final Entity entity) {
		Entity machine = entity.getBelongsToField("machine");
		Entity operationComponent = entity
				.getBelongsToField("operationComponent");

		if (operationComponent == null || machine == null) {
			return true;
		}

		SearchResult searchResult = dataDefinition
				.find()
				.add(SearchRestrictions.belongsTo("machine", machine))
				.add(SearchRestrictions.belongsTo("operationComponent",
						operationComponent)).list();

		if (searchResult.getTotalNumberOfEntities() == 1
				&& !searchResult.getEntities().get(0).getId()
						.equals(entity.getId())) {
			return true;
		} else if (searchResult.getTotalNumberOfEntities() > 0) {
			entity.addError(dataDefinition.getField("machine"),
					"productionScheduling.validate.global.error.machineInOperationDuplicated");
			return false;
		} else {
			return true;
		}
	}

	public void refereshGanttChart(
			final ViewDefinitionState viewDefinitionState,
			final ComponentState triggerState, final String[] args) {
		viewDefinitionState.getComponentByReference("gantt").performEvent(
				viewDefinitionState, "refresh");
	}

	public void disableFormWhenNoOrderSelected(
			final ViewDefinitionState viewDefinitionState) {
		if (viewDefinitionState.getComponentByReference("gantt")
				.getFieldValue() == null) {
			viewDefinitionState.getComponentByReference("dateFrom").setEnabled(
					false);
			viewDefinitionState.getComponentByReference("dateTo").setEnabled(
					false);
		} else {
			viewDefinitionState.getComponentByReference("dateFrom").setEnabled(
					true);
			viewDefinitionState.getComponentByReference("dateTo").setEnabled(
					true);
		}
	}

}
