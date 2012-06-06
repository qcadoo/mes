package com.qcadoo.mes.states.service.client;

import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;

public interface ViewStateChangeClient {

    void changeState(final ViewDefinitionState view, final ComponentState component, final Entity entity, final String targetState);
}
