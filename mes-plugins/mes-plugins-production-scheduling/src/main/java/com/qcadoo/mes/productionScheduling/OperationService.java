package com.qcadoo.mes.productionScheduling;

import org.springframework.stereotype.Service;

import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;

@Service
public class OperationService {

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

    public void updateCountMachineOperationFieldStateonWindowLoad(final ViewDefinitionState viewDefinitionState) {

        FieldComponent countRealizedOperation = (FieldComponent) viewDefinitionState
                .getComponentByReference("countRealizedOperation");
        FieldComponent countMachineOperation = (FieldComponent) viewDefinitionState
                .getComponentByReference("countMachineOperation");

        countRealizedOperation.setRequired(true);

        if (countRealizedOperation.getFieldValue().equals("02specified")) {
            countMachineOperation.setEnabled(true);
        } else {
            countMachineOperation.setEnabled(false);
        }

    }

    public void refereshGanttChart(final ViewDefinitionState viewDefinitionState, final ComponentState triggerState,
            final String[] args) {
        viewDefinitionState.getComponentByReference("gantt").performEvent(viewDefinitionState, "refresh");
    }

    public void disableFormWhenNoOrderSelected(final ViewDefinitionState viewDefinitionState) {
        if (viewDefinitionState.getComponentByReference("gantt").getFieldValue() == null) {
            viewDefinitionState.getComponentByReference("dateFrom").setEnabled(false);
            viewDefinitionState.getComponentByReference("dateTo").setEnabled(false);
        } else {
            viewDefinitionState.getComponentByReference("dateFrom").setEnabled(true);
            viewDefinitionState.getComponentByReference("dateTo").setEnabled(true);
        }
    }

    public void setCountRealizedOperationValue(final ViewDefinitionState viewDefinitionState) {
        FieldComponent countRealizedOperation = (FieldComponent) viewDefinitionState
                .getComponentByReference("countRealizedOperation");
        if (!"02specified".equals(countRealizedOperation.getFieldValue())) {
            countRealizedOperation.setFieldValue("01all");

        }
    }
}
