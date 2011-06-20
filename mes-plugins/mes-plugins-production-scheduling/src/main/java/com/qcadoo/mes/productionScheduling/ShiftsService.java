package com.qcadoo.mes.productionScheduling;

import java.util.Date;

import org.springframework.stereotype.Service;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;

@Service
public class ShiftsService {

    // @Autowired
    // TODO mina || masz - why autowired is not wotking
    private ShiftsGanttChartItemResolver shiftsGanttChartItemResolver = new ShiftsGanttChartItemResolver();

    private static final String[] WEEK_DAYS = { "monday", "tuesday", "wensday", "thursday", "friday", "saturday", "sunday" };

    public boolean validateShiftHoursField(final DataDefinition dataDefinition, final Entity entity) {
        boolean valid = true;
        for (String day : WEEK_DAYS) {
            if (!validateHourField(day, dataDefinition, entity)) {
                valid = false;
            }
        }
        return valid;
    }

    private boolean validateHourField(final String day, final DataDefinition dataDefinition, final Entity entity) {
        boolean isDayActive = (Boolean) entity.getField(day + "Working");
        String fieldValue = entity.getStringField(day + "Hours");
        if (!isDayActive) {
            return true;
        }
        if (fieldValue == null || "".equals(fieldValue.trim())) {
            entity.addError(dataDefinition.getField(day + "Hours"), "qcadooView.validate.field.error.missing");
            return false;
        }

        try {
            shiftsGanttChartItemResolver.convertDayHoursToInt(fieldValue);
        } catch (IllegalStateException e) {
            entity.addError(dataDefinition.getField(day + "Hours"),
                    "productionScheduling.validate.global.error.shift.hoursFieldWrongFormat");
            return false;
        }

        return true;
    }

    public boolean validateShiftTimetableException(final DataDefinition dataDefinition, final Entity entity) {
        Date dateFrom = (Date) entity.getField("fromDate");
        Date dateTo = (Date) entity.getField("toDate");
        if (dateFrom.compareTo(dateTo) > 0) {
            entity.addError(dataDefinition.getField("fromDate"),
                    "productionScheduling.validate.global.error.shiftTimetable.datesError");
            entity.addError(dataDefinition.getField("toDate"),
                    "productionScheduling.validate.global.error.shiftTimetable.datesError");
            return false;
        }
        return true;
    }

    public void showGanttShiftCalendar(final ViewDefinitionState viewDefinitionState, final ComponentState triggerState,
            final String[] args) {
        viewDefinitionState.openModal("../page/productionScheduling/ganttShiftCalendar.html");
    }

    public void showGanttOrdersCalendar(final ViewDefinitionState viewDefinitionState, final ComponentState triggerState,
            final String[] args) {
        viewDefinitionState.openModal("../page/productionScheduling/ganttOrdersCalendar.html");
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
