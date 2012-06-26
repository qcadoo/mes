package com.qcadoo.mes.technologies.states.module;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.states.module.AbstractStateServiceRegisterModule;
import com.qcadoo.mes.states.service.StateChangeService;
import com.qcadoo.mes.technologies.states.aop.TechnologyStateChangeAspect;

@Service
public final class TechnologyStateServiceRegisterModule extends AbstractStateServiceRegisterModule {

    @Autowired
    private TechnologyStateChangeAspect technologyStateChangeAspect;

    @Override
    protected StateChangeService getStateChangeService() {
        return technologyStateChangeAspect;
    }

}
