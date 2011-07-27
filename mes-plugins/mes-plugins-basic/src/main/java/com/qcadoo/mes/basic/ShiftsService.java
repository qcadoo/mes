/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.4.5
 *
 * This file is part of Qcadoo.
 *
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */
package com.qcadoo.mes.basic;

import java.util.Date;

import org.springframework.stereotype.Service;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;

@Service
public class ShiftsService {

    private static final String[] WEEK_DAYS = { "monday", "tuesday", "wensday", "thursday", "friday", "saturday", "sunday" };

    public boolean validateShiftTimetableException(final DataDefinition dataDefinition, final Entity entity) {
        Date dateFrom = (Date) entity.getField("fromDate");
        Date dateTo = (Date) entity.getField("toDate");
        if (dateFrom.compareTo(dateTo) > 0) {
            entity.addError(dataDefinition.getField("fromDate"), "basic.validate.global.error.shiftTimetable.datesError");
            entity.addError(dataDefinition.getField("toDate"), "basic.validate.global.error.shiftTimetable.datesError");
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
        viewDefinitionState.redirectTo("../page/productionScheduling/ganttOrdersCalendar.html", false, true);
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
