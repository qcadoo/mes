package com.qcadoo.mes.productionScheduling;

import org.springframework.stereotype.Service;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;

@Service
public class NormService {

	public void changeEnableNorm(final ViewDefinitionState viewDefinitionState,
			final ComponentState state, final String[] args) {

		FieldComponent enableNorm = (FieldComponent) viewDefinitionState
				.getComponentByReference("enableNorm");
		FieldComponent tpzNorm = (FieldComponent) viewDefinitionState
				.getComponentByReference("tpzNorm");
		FieldComponent tjNorm = (FieldComponent) viewDefinitionState
				.getComponentByReference("tjNorm");
		FieldComponent countRealizedNorm = (FieldComponent) viewDefinitionState
				.getComponentByReference("countRealizedNorm");
		FieldComponent changeNorm = (FieldComponent) viewDefinitionState
				.getComponentByReference("changeNorm");
		FieldComponent timeNextOperationNorm = (FieldComponent) viewDefinitionState
				.getComponentByReference("timeNextOperationNorm");

		if (enableNorm.getFieldValue().equals("1")) {
			tpzNorm.setEnabled(false);
			tjNorm.setEnabled(false);
			countRealizedNorm.setEnabled(false);
			changeNorm.setEnabled(false);
			timeNextOperationNorm.setEnabled(false);
		} else {
			tpzNorm.setEnabled(true);
			tjNorm.setEnabled(true);
			countRealizedNorm.setEnabled(true);
			changeNorm.setEnabled(true);
			timeNextOperationNorm.setEnabled(true);
		}
	}

	public void changeCountRealizedNorm(final ViewDefinitionState viewDefinitionState,
			final ComponentState state, final String[] args) {
		FieldComponent countRealizedNorm = (FieldComponent) viewDefinitionState
				.getComponentByReference("countRealizedNorm");
		FieldComponent countMachineNorm = (FieldComponent) viewDefinitionState
				.getComponentByReference("countMachineNorm");

		if (countRealizedNorm.getFieldValue().equals("02specified")) {
			countMachineNorm.setEnabled(true);
		} else {
			countMachineNorm.setEnabled(false);
		}
	}

	public void copyDefaultDataToOperationComponent(final DataDefinition dataDefinition, final Entity entity) {

		Entity operation = entity.getBelongsToField("operation");
		if (entity.getStringField("tpzOperation") == null
				|| "".equals(entity.getStringField("tpzOperation"))) {

			entity.setField("tpzNorm", operation.getStringField("tpzOperation"));
			entity.setField("tjNorm", operation.getStringField("tjOperation"));
			entity.setField("countRealizedNorm",
					operation.getStringField("countRealizedOperation"));
			entity.setField("timeNextOperationNorm",
					operation.getStringField("timeNextOperation"));
			entity.setField("countMachineNorm",
					operation.getStringField("countMachineOperation"));
		} else {
			return;
		}
	}
}
