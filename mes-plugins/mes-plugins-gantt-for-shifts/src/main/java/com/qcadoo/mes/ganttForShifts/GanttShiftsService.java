package com.qcadoo.mes.ganttForShifts;

import org.springframework.stereotype.Service;

import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;

@Service
public class GanttShiftsService {

    public void showGanttShiftCalendar(final ViewDefinitionState viewDefinitionState, final ComponentState triggerState,
            final String[] args) {
        viewDefinitionState.openModal("../page/ganttForShifts/ganttForShifts.html");
    }
}
