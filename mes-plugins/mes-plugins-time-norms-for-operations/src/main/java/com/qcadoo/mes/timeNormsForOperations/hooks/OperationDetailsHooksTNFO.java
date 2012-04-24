package com.qcadoo.mes.timeNormsForOperations.hooks;

import static com.qcadoo.mes.timeNormsForOperations.constants.TechnologyOperCompTNFOFields.COUNT_MACHINE;
import static com.qcadoo.mes.timeNormsForOperations.constants.TechnologyOperCompTNFOFields.COUNT_MACHINE_UNIT;
import static com.qcadoo.mes.timeNormsForOperations.constants.TechnologyOperCompTNFOFields.COUNT_REALIZED;

import org.springframework.stereotype.Service;

import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;

@Service
public class OperationDetailsHooksTNFO {

    public void setCountRealizedOperationValue(final ViewDefinitionState viewDefinitionState) {
        FieldComponent countRealized = (FieldComponent) viewDefinitionState.getComponentByReference(COUNT_REALIZED);
        if (!"02specified".equals(countRealized.getFieldValue())) {
            countRealized.setFieldValue("01all");

        }
    }

    public void updateFieldsStateOnWindowLoad(final ViewDefinitionState viewDefinitionState) {

        FieldComponent countRealized = (FieldComponent) viewDefinitionState.getComponentByReference(COUNT_REALIZED);
        FieldComponent countMachine = (FieldComponent) viewDefinitionState.getComponentByReference(COUNT_MACHINE);
        FieldComponent countMachineUNIT = (FieldComponent) viewDefinitionState.getComponentByReference(COUNT_MACHINE_UNIT);
        FieldComponent areProductQuantitiesDivisible = (FieldComponent) viewDefinitionState
                .getComponentByReference("areProductQuantitiesDivisible");
        FieldComponent isTjDivisible = (FieldComponent) viewDefinitionState.getComponentByReference("isTjDivisible");

        countRealized.setRequired(true);

        if (countRealized.getFieldValue().equals("02specified")) {
            countMachine.setVisible(true);
            countMachine.setEnabled(true);
            countMachineUNIT.setVisible(true);
            countMachineUNIT.setEnabled(true);
        } else {
            countMachine.setVisible(false);
            countMachineUNIT.setVisible(false);
        }
        countMachine.requestComponentUpdateState();
        if ("1".equals(areProductQuantitiesDivisible.getFieldValue())) {
            isTjDivisible.setEnabled(true);
        }
    }
}
