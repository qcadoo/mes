package com.qcadoo.mes.technologies.hooks;

import org.springframework.stereotype.Service;

import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ComponentState.MessageType;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.GridComponent;

@Service
public class TechnologiesWithUsingProductListHooks {

    private static final String L_GRID = "grid";

    private static final String L_FORM = "form";

    public void beforeRender(final ViewDefinitionState viewDefinitionState) {
        showInfoIfNotUsed(viewDefinitionState);
    }

    private void showInfoIfNotUsed(final ViewDefinitionState viewDefinitionState) {
        GridComponent grid = (GridComponent) viewDefinitionState.getComponentByReference(L_GRID);
        if (grid.getEntities().isEmpty()) {
            ComponentState form = viewDefinitionState.getComponentByReference(L_FORM);
            form.addMessage("technologies.product.info.notUsed", MessageType.INFO, true);
        }
    }

}
