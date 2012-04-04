package com.qcadoo.mes.productionLines.hooks;

import org.springframework.stereotype.Service;

import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;

@Service
public class ProductionLinesViewHooks {

    public void disableSupportedTechnologiesGrids(final ViewDefinitionState view) {
        ComponentState supportsAllTechnologies = view.getComponentByReference("supportsAllTechnologies");

        if ("1".equals(supportsAllTechnologies.getFieldValue())) {
            view.getComponentByReference("technologies").setEnabled(false);
            view.getComponentByReference("groups").setEnabled(false);
        } else {
            view.getComponentByReference("technologies").setEnabled(true);
            view.getComponentByReference("groups").setEnabled(true);
        }
    }

    public void disableSupportedTechnologiesGrids(final ViewDefinitionState view, final ComponentState component,
            final String[] args) {
        disableSupportedTechnologiesGrids(view);
    }
}
