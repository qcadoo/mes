package com.qcadoo.mes.costCalculation.hooks;

import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.springframework.stereotype.Service;

@Service
public class TechnologiesListHooksCC {

    public void toggleGenerateCostCalculationButton(final ViewDefinitionState view) {
        WindowComponent window = (WindowComponent) view.getComponentByReference(QcadooViewConstants.L_WINDOW);
        RibbonActionItem generateCostCalculation = window.getRibbon().getGroupByName("costCalculation")
                .getItemByName("createCostCalculation");

        GridComponent grid = (GridComponent) view.getComponentByReference(QcadooViewConstants.L_GRID);

        generateCostCalculation.setEnabled(!grid.getSelectedEntities().isEmpty());

        generateCostCalculation.requestUpdate(true);
    }
}
