package com.qcadoo.mes.materialFlowResources.listeners;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.materialFlowResources.hooks.LocationDetailsHooksMFR;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;

@Service
public class LocationDetailsListenersMFR {

    @Autowired
    LocationDetailsHooksMFR locationDetailsHooksMFR;

    public void onAlgorithmChange(final ViewDefinitionState view, final ComponentState componentState, final String[] args) {
        locationDetailsHooksMFR.setEnabledForBatchCheckbox(view);
    }

}
