package com.qcadoo.mes.productionScheduling;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;

@Service
public class NormService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void updateFieldsStateOnWindowLoad(final ViewDefinitionState viewDefinitionState) {
        FieldComponent tpzNorm = (FieldComponent) viewDefinitionState.getComponentByReference("tpz");
        FieldComponent tjNorm = (FieldComponent) viewDefinitionState.getComponentByReference("tj");
        FieldComponent countRealized = (FieldComponent) viewDefinitionState.getComponentByReference("countRealized");
        FieldComponent timeNextOperation = (FieldComponent) viewDefinitionState.getComponentByReference("timeNextOperation");
        Object value = countRealized.getFieldValue();

        tpzNorm.setEnabled(true);
        tjNorm.setEnabled(true);
        countRealized.setEnabled(true);
        if (!"02specified".equals(value)) {
            countRealized.setFieldValue("01all");
        }
        timeNextOperation.setEnabled(true);
    }

    public void updateFieldsStateWhenDefaultValueCheckboxChanged(final ViewDefinitionState viewDefinitionState,
            final ComponentState componentState, final String[] args) {
        /* uruchamia sie hook before render */
    }

    public void changeCountRealizedNorm(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        FieldComponent countRealized = (FieldComponent) viewDefinitionState.getComponentByReference("countRealized");
        FieldComponent countMachine = (FieldComponent) viewDefinitionState.getComponentByReference("countMachine");

        if (countRealized.getFieldValue().equals("02specified")) {
            countMachine.setEnabled(true);
        } else {
            countMachine.setEnabled(false);
        }
    }

    public void copyNormFromOperation(final ViewDefinitionState viewDefinitionState, final ComponentState componentState,
            final String[] args) {
        FieldComponent tpzNorm = (FieldComponent) viewDefinitionState.getComponentByReference("tpz");
        FieldComponent tjNorm = (FieldComponent) viewDefinitionState.getComponentByReference("tj");
        FieldComponent countRealized = (FieldComponent) viewDefinitionState.getComponentByReference("countRealized");
        FieldComponent countMachine = (FieldComponent) viewDefinitionState.getComponentByReference("countMachine");
        FieldComponent timeNextOperation = (FieldComponent) viewDefinitionState.getComponentByReference("timeNextOperation");

        Long operationId = (Long) componentState.getFieldValue();

        Entity operation = operationId != null ? dataDefinitionService.get("technologies", "operation").get(operationId) : null;

        if (operation != null) {
            tpzNorm.setFieldValue(operation.getField("tpz"));
            tjNorm.setFieldValue(operation.getField("tj"));
            countRealized.setFieldValue(operation.getField("countRealizedOperation") != null ? operation
                    .getField("countRealizedOperation") : "01all");
            countMachine.setFieldValue(operation.getField("countMachineOperation"));
            timeNextOperation.setFieldValue(operation.getField("timeNextOperation"));
        } else {
            tpzNorm.setFieldValue(null);
            tjNorm.setFieldValue(null);
            countRealized.setFieldValue("01all");
            countMachine.setFieldValue(null);
            timeNextOperation.setFieldValue(null);
        }
    }

    public void updateCountMachineOperationFieldStateonWindowLoad(final ViewDefinitionState viewDefinitionState) {
        FieldComponent countRealizedOperation = (FieldComponent) viewDefinitionState.getComponentByReference("countRealized");
        FieldComponent countMachineOperation = (FieldComponent) viewDefinitionState.getComponentByReference("countMachine");

        if ("02specified".equals(countRealizedOperation.getFieldValue())) {
            countMachineOperation.setEnabled(true);
        } else {
            countMachineOperation.setEnabled(false);
        }
    }

}
