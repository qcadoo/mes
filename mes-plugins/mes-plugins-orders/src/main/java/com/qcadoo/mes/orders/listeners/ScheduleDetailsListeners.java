package com.qcadoo.mes.orders.listeners;

import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import org.springframework.stereotype.Service;

@Service
public class ScheduleDetailsListeners {

    public void informAboutGetOperations(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        view.addMessage("orders.schedule.orders.listChanged", ComponentState.MessageType.INFO);
    }
}
