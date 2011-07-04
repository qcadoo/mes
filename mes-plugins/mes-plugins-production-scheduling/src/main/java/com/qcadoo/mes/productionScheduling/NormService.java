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
        FieldComponent countRealizedNorm = (FieldComponent) viewDefinitionState.getComponentByReference("countRealizedNorm");
        FieldComponent timeNextOperationNorm = (FieldComponent) viewDefinitionState
                .getComponentByReference("timeNextOperationNorm");
        Object value = countRealizedNorm.getFieldValue();

        tpzNorm.setEnabled(true);
        tjNorm.setEnabled(true);
        countRealizedNorm.setEnabled(true);
        if (!"02specified".equals(value)) {
            countRealizedNorm.setFieldValue("01all");
        }
        timeNextOperationNorm.setEnabled(true);
    }

    public void updateFieldsStateWhenDefaultValueCheckboxChanged(final ViewDefinitionState viewDefinitionState,
            final ComponentState componentState, final String[] args) {
        /* uruchamia sie hook before render */
    }

    public void changeCountRealizedNorm(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        FieldComponent countRealizedNorm = (FieldComponent) viewDefinitionState.getComponentByReference("countRealizedNorm");
        FieldComponent countMachineNorm = (FieldComponent) viewDefinitionState.getComponentByReference("countMachineNorm");

        if (countRealizedNorm.getFieldValue().equals("02specified")) {
            countMachineNorm.setEnabled(true);
        } else {
            countMachineNorm.setEnabled(false);
        }
    }

    public void copyNormFromOperation(final ViewDefinitionState viewDefinitionState, final ComponentState componentState,
            final String[] args) {
        FieldComponent tpzNorm = (FieldComponent) viewDefinitionState.getComponentByReference("tpz");
        FieldComponent tjNorm = (FieldComponent) viewDefinitionState.getComponentByReference("tj");
        FieldComponent countRealizedNorm = (FieldComponent) viewDefinitionState.getComponentByReference("countRealizedNorm");
        FieldComponent countMachineNorm = (FieldComponent) viewDefinitionState.getComponentByReference("countMachineNorm");
        FieldComponent timeNextOperationNorm = (FieldComponent) viewDefinitionState
                .getComponentByReference("timeNextOperationNorm");

        Long operationId = (Long) componentState.getFieldValue();

        Entity operation = operationId != null ? dataDefinitionService.get("technologies", "operation").get(operationId) : null;

        if (operation != null) {
            tpzNorm.setFieldValue(operation.getField("tpz"));
            tjNorm.setFieldValue(operation.getField("tj"));
            countRealizedNorm.setFieldValue(operation.getField("countRealizedOperation"));
            countMachineNorm.setFieldValue(operation.getField("countMachineOperation"));
            timeNextOperationNorm.setFieldValue(operation.getField("timeNextOperation"));
        } else {
            tpzNorm.setFieldValue(null);
            tjNorm.setFieldValue(null);
            countRealizedNorm.setFieldValue("01all");
            countMachineNorm.setFieldValue(null);
            timeNextOperationNorm.setFieldValue(null);
        }
    }

    public void updateCountMachineOperationFieldStateonWindowLoad(final ViewDefinitionState viewDefinitionState) {
        FieldComponent countRealizedOperation = (FieldComponent) viewDefinitionState.getComponentByReference("countRealizedNorm");
        FieldComponent countMachineOperation = (FieldComponent) viewDefinitionState.getComponentByReference("countMachineNorm");

        if (countRealizedOperation.getFieldValue().equals("02specified")) {
            countMachineOperation.setEnabled(true);
        } else {
            countMachineOperation.setEnabled(false);
        }
    }

}
