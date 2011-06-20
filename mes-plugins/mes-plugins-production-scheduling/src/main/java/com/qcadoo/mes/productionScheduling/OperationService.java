package com.qcadoo.mes.productionScheduling;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;

@Service
public class OperationService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void changeCountRealizedOperation(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
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

    public void changeDfltValue(final ViewDefinitionState viewDefinitionState, final ComponentState state, final String[] args) {

        FieldComponent dfltValue = (FieldComponent) viewDefinitionState.getComponentByReference("dfltValue");
        FieldComponent tpz = (FieldComponent) viewDefinitionState.getComponentByReference("tpz");
        FieldComponent tj = (FieldComponent) viewDefinitionState.getComponentByReference("tj");
        FieldComponent parallel = (FieldComponent) viewDefinitionState.getComponentByReference("parallel");
        FieldComponent activeMachine = (FieldComponent) viewDefinitionState.getComponentByReference("activeMachine");

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

    public void selectMachineInOperationComponent(final ViewDefinitionState state, final ComponentState componentState,
            final String[] args) {
        if (!(componentState instanceof FieldComponent)) {
            throw new IllegalStateException("component is not FieldComponentState");
        }

        if (componentState.getFieldValue() == null) {
            return;
        }

        Long machineId = (Long) componentState.getFieldValue();

        Entity machine = dataDefinitionService.get("basic", "machine").get(machineId);

        if (machine == null) {
            return;
        }

        FieldComponent tpz = (FieldComponent) state.getComponentByReference("tpz");
        FieldComponent tj = (FieldComponent) state.getComponentByReference("tj");
        FieldComponent parallel = (FieldComponent) state.getComponentByReference("parallel");
        FieldComponent active = (FieldComponent) state.getComponentByReference("activeMachine");

        if (machine.getField("tpzMachine") != null) {
            tpz.setFieldValue(machine.getField("tpzMachine"));
        } else {
            tpz.setFieldValue("");
        }

        if (machine.getField("tjMachine") != null) {
            tj.setFieldValue(machine.getField("tjMachine"));
        } else {
            tj.setFieldValue("");
        }

        if (machine.getField("parallelMachine") != null) {
            parallel.setFieldValue(machine.getField("parallelMachine"));
        } else {
            parallel.setFieldValue("");
        }

        if (machine.getField("ofMachine") != null) {
            active.setFieldValue(!(Boolean) machine.getField("ofMachine"));
        } else {
            active.setFieldValue(false);
        }
    }

}
