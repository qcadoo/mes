package com.qcadoo.mes.technologies.hooks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.states.service.StateChangeEntityBuilder;
import com.qcadoo.mes.technologies.states.constants.TechnologyState;
import com.qcadoo.mes.technologies.states.constants.TechnologyStateChangeDescriber;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class TechnologyModelHooks {

    @Autowired
    private StateChangeEntityBuilder stateChangeEntityBuilder;

    @Autowired
    private TechnologyStateChangeDescriber describer;

    public void setInitialState(final DataDefinition dataDefinition, final Entity technology) {
        stateChangeEntityBuilder.buildInitial(describer, technology, TechnologyState.DRAFT);
    }

}
