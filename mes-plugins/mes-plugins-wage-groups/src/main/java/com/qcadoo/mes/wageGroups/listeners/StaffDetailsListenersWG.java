package com.qcadoo.mes.wageGroups.listeners;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.wageGroups.hooks.StaffDetailsHooks;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;

@Service
public class StaffDetailsListenersWG {

    @Autowired
    private StaffDetailsHooks detailsHooks;

    public void enabledIndividualCost(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        detailsHooks.enabledIndividualCost(view);
    }

    public void fillFieldAboutWageGroup(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        detailsHooks.fillFieldAboutWageGroup(view);
    }
}
