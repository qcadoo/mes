package com.qcadoo.mes.ganttForOrders;

import org.springframework.stereotype.Service;

import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;

@Service
public class GanttOrderService {

    public void showGanttOrdersCalendar(final ViewDefinitionState viewDefinitionState, final ComponentState triggerState,
            final String[] args) {
        viewDefinitionState.redirectTo("../page/ganttForOrders/ganttForOrders.html", false, true);
    }

}
