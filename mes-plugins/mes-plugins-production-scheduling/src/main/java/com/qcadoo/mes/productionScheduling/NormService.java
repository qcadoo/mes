package com.qcadoo.mes.productionScheduling;

import org.springframework.stereotype.Service;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.api.search.SearchResult;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;

@Service
public class NormService {

	public void changeDefaultValue(final ViewDefinitionState viewDefinitionState,
			final ComponentState state, final String[] args) {

		FieldComponent useDefaultValue = (FieldComponent) viewDefinitionState
				.getComponentByReference("useDefaultValue");
		FieldComponent tpzNorm = (FieldComponent) viewDefinitionState
				.getComponentByReference("tpz");
		FieldComponent tjNorm = (FieldComponent) viewDefinitionState
				.getComponentByReference("tj");
		FieldComponent countRealizedNorm = (FieldComponent) viewDefinitionState
				.getComponentByReference("countRealizedNorm");
		FieldComponent useMachineNorm = (FieldComponent) viewDefinitionState
				.getComponentByReference("useMachineNorm");
		FieldComponent timeNextOperationNorm = (FieldComponent) viewDefinitionState
				.getComponentByReference("timeNextOperationNorm");

		if (useDefaultValue.getFieldValue().equals("1")) {
			tpzNorm.setEnabled(false);
			tjNorm.setEnabled(false);
			countRealizedNorm.setEnabled(false);
			useMachineNorm.setEnabled(false);
			timeNextOperationNorm.setEnabled(false);
		} else {
			tpzNorm.setEnabled(true);
			tjNorm.setEnabled(true);
			countRealizedNorm.setEnabled(true);
			useMachineNorm.setEnabled(true);
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
		if (entity.getField("tpz") == null
				|| "".equals(entity.getField("tpz"))) {

			entity.setField("tpz", operation.getField("tpz"));
			entity.setField("tj", operation.getField("tj"));
			entity.setField("countRealizedNorm",
					operation.getStringField("countRealizedOperation"));
			entity.setField("timeNextOperationNorm",
					operation.getField("timeNextOperation"));
			entity.setField("countMachineNorm",
					operation.getField("countMachineOperation"));
		} else {
			return;
		}
	}
	
/*	public boolean checkOperationComponentUniqueness(final DataDefinition dataDefinition, final Entity entity) {
        Entity machine = entity.getBelongsToField("machine");
        Entity operationComponent = entity.getBelongsToField("operationComponent");

        if (operationComponent == null || machine == null) {
            return false;
        }

        SearchResult searchResult = dataDefinition.find().add(SearchRestrictions.belongsTo("machine", machine))
                .add(SearchRestrictions.belongsTo("operationComponent", operationComponent)).list();

        if (searchResult.getTotalNumberOfEntities() > 0 && !searchResult.getEntities().get(0).getId().equals(entity.getId())) {
            entity.addError(dataDefinition.getField("machine"),
                    "productionScheduling.validate.global.error.machineInOperationDuplicated");
            return false;
        } else {
            return true;
        }
    }*/
}
