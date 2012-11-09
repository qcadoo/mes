package com.qcadoo.mes.productionCounting.listeners;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.productionCounting.hooks.ParametersHooksPC;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;

@Service
public class ParametersListenersPC {

    @Autowired
    private ParametersHooksPC parametersHooksPC;

    public void setParametersDefaultValue(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        parametersHooksPC.setParametersDefaultValue(viewDefinitionState);
    }
}
