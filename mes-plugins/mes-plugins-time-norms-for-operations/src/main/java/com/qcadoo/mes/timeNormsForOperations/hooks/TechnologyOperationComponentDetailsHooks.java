package com.qcadoo.mes.timeNormsForOperations.hooks;

import static com.qcadoo.mes.technologies.constants.TechnologyInstanceOperCompFields.TECHNOLOGY_OPERATION_COMPONENT;
import static com.qcadoo.mes.timeNormsForOperations.constants.TechnologyOperationComponentTNFOFields.COUNT_MACHINE;
import static com.qcadoo.mes.timeNormsForOperations.constants.TechnologyOperationComponentTNFOFields.COUNT_MACHINE_UNIT;
import static com.qcadoo.mes.timeNormsForOperations.constants.TechnologyOperationComponentTNFOFields.COUNT_REALIZED;
import static com.qcadoo.mes.timeNormsForOperations.constants.TechnologyOperationComponentTNFOFields.PRODUCTION_IN_ONE_CYCLE;
import static com.qcadoo.mes.timeNormsForOperations.constants.TechnologyOperationComponentTNFOFields.TIME_NEXT_OPERATION;

import org.springframework.stereotype.Service;

import com.google.common.collect.Sets;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;

@Service
public class TechnologyOperationComponentDetailsHooks {

    public void updateCountMachineOperationFieldStateonWindowLoad(final ViewDefinitionState viewDefinitionState) {
        FieldComponent countRealizedOperation = (FieldComponent) viewDefinitionState.getComponentByReference(COUNT_REALIZED);
        FieldComponent countMachineOperation = (FieldComponent) viewDefinitionState.getComponentByReference(COUNT_MACHINE);
        FieldComponent countMachineUNIT = (FieldComponent) viewDefinitionState.getComponentByReference(COUNT_MACHINE_UNIT);

        if ("02specified".equals(countRealizedOperation.getFieldValue())) {
            countMachineOperation.setVisible(true);
            countMachineOperation.setEnabled(true);
            countMachineUNIT.setVisible(true);
            countMachineUNIT.setEnabled(true);
        } else {
            countMachineOperation.setVisible(false);
            countMachineUNIT.setVisible(false);

        }
    }

    public void updateFieldsStateOnWindowLoad(final ViewDefinitionState viewDefinitionState) {
        FieldComponent tpzNorm = (FieldComponent) viewDefinitionState.getComponentByReference("tpz");
        FieldComponent tjNorm = (FieldComponent) viewDefinitionState.getComponentByReference("tj");
        FieldComponent productionInOneCycle = (FieldComponent) viewDefinitionState
                .getComponentByReference(PRODUCTION_IN_ONE_CYCLE);
        FieldComponent countRealized = (FieldComponent) viewDefinitionState.getComponentByReference(COUNT_REALIZED);
        FieldComponent timeNextOperation = (FieldComponent) viewDefinitionState.getComponentByReference(TIME_NEXT_OPERATION);
        Object value = countRealized.getFieldValue();

        tpzNorm.setEnabled(true);
        tjNorm.setEnabled(true);
        productionInOneCycle.setEnabled(true);

        countRealized.setEnabled(true);
        if (!"02specified".equals(value)) {
            countRealized.setFieldValue("01all");
        }
        timeNextOperation.setEnabled(true);
    }

    public void fillUnitFields(final ViewDefinitionState view) {
        FieldComponent component = null;
        Entity formEntity = ((FormComponent) view.getComponentByReference("form")).getEntity();

        // we can pass units only to technology level operations
        if (formEntity.getId() == null || !TECHNOLOGY_OPERATION_COMPONENT.equals(formEntity.getDataDefinition().getName())) {
            return;
        }

        // be sure that entity isn't in detached state before you wander through the relationship
        formEntity = formEntity.getDataDefinition().get(formEntity.getId());
        // you can use someEntity.getSTH().getSTH() only when you are 100% sure that all the passers-relations
        // will not return null (i.e. all relations using below are mandatory on the model definition level)
        String unit = formEntity.getBelongsToField("technology").getBelongsToField("product").getField("unit").toString();
        for (String referenceName : Sets.newHashSet(COUNT_MACHINE_UNIT, "productionInOneCycleUNIT")) {
            component = (FieldComponent) view.getComponentByReference(referenceName);
            if (component == null) {
                continue;
            }
            component.setFieldValue(unit);
            component.requestComponentUpdateState();
        }
    }
}
