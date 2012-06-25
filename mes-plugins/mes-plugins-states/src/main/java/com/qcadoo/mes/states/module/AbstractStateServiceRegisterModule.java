package com.qcadoo.mes.states.module;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.states.service.StateChangeService;
import com.qcadoo.mes.states.service.StateChangeServiceResolver;
import com.qcadoo.plugin.api.Module;

@Service
public abstract class AbstractStateServiceRegisterModule extends Module {

    @Autowired
    private StateChangeServiceResolver stateChangeServiceResolver;

    protected abstract StateChangeService getStateChangeService();

    @Override
    public void enable() {
        register();
    }

    @Override
    public void multiTenantEnable() {
        register();
    }

    private void register() {
        stateChangeServiceResolver.register(getStateChangeService().getChangeEntityDescriber().getOwnerDataDefinition(),
                getStateChangeService());
    }

    @Override
    public void disable() {
        stateChangeServiceResolver.unregister(getStateChangeService());
    }

}
