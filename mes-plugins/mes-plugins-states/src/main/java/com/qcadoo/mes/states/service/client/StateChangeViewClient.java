package com.qcadoo.mes.states.service.client;

import com.qcadoo.mes.states.StateChangeContext;
import com.qcadoo.mes.states.service.client.util.ViewContextHolder;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;

public interface StateChangeViewClient {

    void changeState(final ViewDefinitionState view, final ComponentState component, final String[] args);

    void changeState(final ViewContextHolder viewContextHolder, final String targetState);

    void changeState(final ViewContextHolder viewContextHolder, final String targetState, final Entity entity);

    void showMessages(final ViewContextHolder viewContext, final StateChangeContext stateChangeContext);
}
