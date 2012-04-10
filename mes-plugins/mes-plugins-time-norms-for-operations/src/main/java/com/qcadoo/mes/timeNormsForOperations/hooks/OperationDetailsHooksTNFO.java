package com.qcadoo.mes.timeNormsForOperations.hooks;

import org.springframework.stereotype.Service;

import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;

@Service
public class OperationDetailsHooksTNFO {

    public void setCountRealizedOperationValue(final ViewDefinitionState viewDefinitionState) {
        FieldComponent countRealizedOperation = (FieldComponent) viewDefinitionState
                .getComponentByReference("countRealizedOperation");
        if (!"02specified".equals(countRealizedOperation.getFieldValue())) {
            countRealizedOperation.setFieldValue("01all");

        }
    }

    public void updateCountMachineOperationFieldStateonWindowLoad(final ViewDefinitionState viewDefinitionState) {

        FieldComponent countRealizedOperation = (FieldComponent) viewDefinitionState
                .getComponentByReference("countRealizedOperation");
        FieldComponent countMachineOperation = (FieldComponent) viewDefinitionState
                .getComponentByReference("countMachineOperation");
        FieldComponent countMachineUNIT = (FieldComponent) viewDefinitionState.getComponentByReference("countMachineUNIT");

        countRealizedOperation.setRequired(true);

        if (countRealizedOperation.getFieldValue().equals("02specified")) {
            countMachineOperation.setVisible(true);
            countMachineOperation.setEnabled(true);
            countMachineUNIT.setVisible(true);
            countMachineUNIT.setEnabled(true);
        } else {
            countMachineOperation.setVisible(false);
            countMachineUNIT.setVisible(false);
        }
        countMachineOperation.requestComponentUpdateState();
    }
}
