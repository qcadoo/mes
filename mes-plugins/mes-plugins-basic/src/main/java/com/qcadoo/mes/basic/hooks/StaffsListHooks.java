package com.qcadoo.mes.basic.hooks;

import org.springframework.stereotype.Service;

import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.api.ribbon.RibbonGroup;

@Service
public class StaffsListHooks {

    private static final String L_GRID = "grid";

    private static final String L_WINDOW = "window";

    private static final String L_LABELS = "labels";

    private static final String L_PRINT_STAFF_LABELS = "printStaffLabels";

    public void updateRibbonState(final ViewDefinitionState view) {
        GridComponent staffsGrid = (GridComponent) view.getComponentByReference(L_GRID);

        WindowComponent window = (WindowComponent) view.getComponentByReference(L_WINDOW);
        RibbonGroup labels = window.getRibbon().getGroupByName(L_LABELS);
        RibbonActionItem printStaffLabels = labels.getItemByName(L_PRINT_STAFF_LABELS);

        boolean isEnabled = !staffsGrid.getSelectedEntities().isEmpty();

        printStaffLabels.setEnabled(isEnabled);
        printStaffLabels.requestUpdate(true);
    }

}
