package com.qcadoo.mes.productionLines.hooks;

import static com.qcadoo.mes.productionLines.constants.ProductionLineFields.GROUPS;
import static com.qcadoo.mes.productionLines.constants.ProductionLineFields.SUPPORTSALLTECHNOLOGIES;
import static com.qcadoo.mes.productionLines.constants.ProductionLineFields.TECHNOLOGIES;

import org.springframework.stereotype.Service;

import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;

@Service
public class ProductionLineDetailsViewHooks {

    public void disableSupportedTechnologiesGrids(final ViewDefinitionState view) {
        ComponentState supportsAllTechnologies = view.getComponentByReference(SUPPORTSALLTECHNOLOGIES);

        if ("1".equals(supportsAllTechnologies.getFieldValue())) {
            view.getComponentByReference(TECHNOLOGIES).setEnabled(false);
            view.getComponentByReference(GROUPS).setEnabled(false);
        } else {
            view.getComponentByReference(TECHNOLOGIES).setEnabled(true);
            view.getComponentByReference(GROUPS).setEnabled(true);
        }
    }

    public void disableSupportedTechnologiesGrids(final ViewDefinitionState view, final ComponentState component,
            final String[] args) {
        disableSupportedTechnologiesGrids(view);
    }
}
