package com.qcadoo.mes.technologies.states;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.states.service.StateChangeService;
import com.qcadoo.mes.states.service.client.AbstractStateChangeViewClient;
import com.qcadoo.mes.technologies.states.aop.TechnologyStateChangeAspect;

@Service
public class TechnologyStateChangeViewClient extends AbstractStateChangeViewClient {

    @Autowired
    private TechnologyStateChangeAspect technologyStateChangeService;

    @Override
    protected StateChangeService getStateChangeService() {
        return technologyStateChangeService;
    }

}
