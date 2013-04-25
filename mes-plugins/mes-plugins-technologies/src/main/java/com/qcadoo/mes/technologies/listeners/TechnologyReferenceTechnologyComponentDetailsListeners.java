package com.qcadoo.mes.technologies.listeners;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.technologies.hooks.TechnologyReferenceTechnologyComponentDetailsHooks;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;

@Service
public class TechnologyReferenceTechnologyComponentDetailsListeners {

    @Autowired
    private TechnologyReferenceTechnologyComponentDetailsHooks technologyReferenceTechnologyComponentDetailsHooks;

    public void disabledSaveBackButton(final ViewDefinitionState view, final ComponentState componentState, final String[] args) {
        technologyReferenceTechnologyComponentDetailsHooks.disabledSaveBackButton(view);
    }

}
