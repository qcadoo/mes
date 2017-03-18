package com.qcadoo.mes.productionCounting.hooks;

import org.springframework.stereotype.Service;

import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.api.ribbon.RibbonGroup;

@Service
public class PerformanceAnalysisHooks {

    private static final String L_WINDOW = "window";

    private static final String L_GRID = "grid";

    private static final String L_ANALYSIS = "analysis";

    private static final String L_SHOW_DETAILS = "showDetails";

    public void toggleShowDetailsButton(final ViewDefinitionState view) {
        WindowComponent window = (WindowComponent) view.getComponentByReference(L_WINDOW);
        RibbonGroup analysis = (RibbonGroup) window.getRibbon().getGroupByName(L_ANALYSIS);
        RibbonActionItem showDetails = (RibbonActionItem) analysis.getItemByName(L_SHOW_DETAILS);

        GridComponent performanceAnalysisGrid = (GridComponent) view.getComponentByReference(L_GRID);

        if (performanceAnalysisGrid.getSelectedEntities().isEmpty()) {
            showDetails.setEnabled(false);
        } else if (performanceAnalysisGrid.getSelectedEntities().size() == 1) {
            showDetails.setEnabled(true);
        } else {
            showDetails.setEnabled(false);
        }
        showDetails.setMessage("productionCounting.analysis.ribbon.message.selectOneRecord");
        showDetails.requestUpdate(true);

    }
}
