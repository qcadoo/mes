package com.qcadoo.mes.productionCounting.hooks;

import java.util.List;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.productionCounting.constants.ProductionTrackingFields;
import com.qcadoo.mes.productionCounting.states.constants.ProductionTrackingStateStringValues;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.ribbon.RibbonActionItem;

@Service
public class ProductionTrackingsListHooks {

    private static final String L_CORRECT = "correct";

    private static final String L_CORRECTION = "correction";

    private static final String L_WINDOW = "window";

    private static final String L_GRID = "grid";

    public void onBeforeRender(final ViewDefinitionState view) {
        GridComponent grid = (GridComponent) view.getComponentByReference(L_GRID);
        WindowComponent window = (WindowComponent) view.getComponentByReference(L_WINDOW);
        RibbonActionItem correctButton = window.getRibbon().getGroupByName(L_CORRECTION).getItemByName(L_CORRECT);
        correctButton.setEnabled(false);

        List<Entity> selected = grid.getSelectedEntities();
        if (selected.size() == 1) {
            Entity selectedEntity = selected.get(0);
            String state = selectedEntity.getStringField(ProductionTrackingFields.STATE);
            if (ProductionTrackingStateStringValues.ACCEPTED.equals(state)) {
                correctButton.setEnabled(true);
            }
        }
        correctButton.requestUpdate(true);
    }

}
