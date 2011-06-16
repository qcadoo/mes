package com.qcadoo.mes.productionScheduling;

import org.springframework.stereotype.Service;

import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;

@Service
public class ShiftsService {

    private static final String[] WEEK_DAYS = { "monday", "tuesday", "wensday", "thursday", "friday", "saturday", "sunday" };

    public void showGanttShiftCalendar(final ViewDefinitionState viewDefinitionState, final ComponentState triggerState,
            final String[] args) {
        viewDefinitionState.openModal("../page/productionScheduling/ganttShiftCalendar.html");
    }

    public void onDayCheckboxChange(final ViewDefinitionState viewDefinitionState, final ComponentState state, final String[] args) {
        updateDayFieldsState(viewDefinitionState);
    }

    public void setHourFieldsState(final ViewDefinitionState viewDefinitionState) {
        updateDayFieldsState(viewDefinitionState);
    }

    private void updateDayFieldsState(final ViewDefinitionState viewDefinitionState) {
        for (String day : WEEK_DAYS) {
            updateDayFieldState(day, viewDefinitionState);
        }
    }

    private void updateDayFieldState(final String day, final ViewDefinitionState viewDefinitionState) {
        FieldComponent mondayWorking = (FieldComponent) viewDefinitionState.getComponentByReference(day + "Working");
        FieldComponent mondayHours = (FieldComponent) viewDefinitionState.getComponentByReference(day + "Hours");
        if (mondayWorking.getFieldValue().equals("0")) {
            mondayHours.setEnabled(false);
            mondayHours.setRequired(false);
        } else {
            mondayHours.setEnabled(true);
            mondayHours.setRequired(true);
        }
    }

}
