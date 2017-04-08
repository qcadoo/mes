package com.qcadoo.mes.materialFlowResources.hooks;

import org.springframework.stereotype.Service;

import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.api.ribbon.RibbonGroup;

@Service
public class PalletStorageStateListHooks {

    private static final String L_WINDOW = "window";

    private static final String L_GRID = "grid";

    private static final String L_DETAILS = "details";

    private static final String L_SHOW_DETAILS = "showDetails";

    private static final String L_MOVE_PALLETS = "movePallets";

    public void toggleButtons(final ViewDefinitionState view) {
        WindowComponent window = (WindowComponent) view.getComponentByReference(L_WINDOW);
        RibbonGroup analysis = window.getRibbon().getGroupByName(L_DETAILS);
        RibbonActionItem showDetails = analysis.getItemByName(L_SHOW_DETAILS);
        GridComponent palletStorageStateGrid = (GridComponent) view.getComponentByReference(L_GRID);
        showDetails.setEnabled(1 == palletStorageStateGrid.getSelectedEntities().size());
        showDetails.setMessage("materialFlowResources.palletStorageStateList.ribbon.message.selectOneRecord");
        showDetails.requestUpdate(true);

        RibbonGroup movePallets = window.getRibbon().getGroupByName(L_MOVE_PALLETS);
        movePallets.getItems().forEach(item -> {
            item.setEnabled(!palletStorageStateGrid.getSelectedEntitiesIds().isEmpty());
            item.requestUpdate(true);
        });
    }

}
