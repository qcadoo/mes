package com.qcadoo.mes.productionScheduling;

import org.springframework.stereotype.Service;

import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;

@Service
public class ShiftsService {

    public void onDayCheckboxChange(final ViewDefinitionState viewDefinitionState, final ComponentState state, final String[] args) {
        FieldComponent mondayWorking = (FieldComponent) viewDefinitionState.getComponentByReference("mondayWorking");
        FieldComponent mondayHours = (FieldComponent) viewDefinitionState.getComponentByReference("mondayHours");

        if (mondayWorking.getFieldValue().equals("0")) {
            mondayHours.setEnabled(false);
        } else {
            mondayHours.setEnabled(true);
        }

    }

}
