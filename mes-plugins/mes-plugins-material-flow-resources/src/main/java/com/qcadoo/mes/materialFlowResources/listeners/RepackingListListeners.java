package com.qcadoo.mes.materialFlowResources.listeners;

import com.qcadoo.mes.materialFlowResources.states.RepackingServiceMarker;
import com.qcadoo.mes.newstates.StateExecutorService;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RepackingListListeners {

    @Autowired
    private StateExecutorService stateExecutorService;

    public void changeState(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        stateExecutorService.changeState(RepackingServiceMarker.class, view, args);
    }
}
