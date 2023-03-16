package com.qcadoo.mes.orders.hooks;

import com.qcadoo.mes.orders.services.WorkstationChangeoverService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.ribbon.Ribbon;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.api.ribbon.RibbonGroup;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WorkstationChangeoverNormsListHooksO {

    private static final String L_ACTIONS = "actions";

    private static final String L_DELETE = "delete";

    @Autowired
    private WorkstationChangeoverService workstationChangeoverService;

    public void updateRibbonState(final ViewDefinitionState view) {
        WindowComponent window = (WindowComponent) view.getComponentByReference(QcadooViewConstants.L_WINDOW);

        Ribbon ribbon = window.getRibbon();
        RibbonGroup actionsRibbonGroup = ribbon.getGroupByName(L_ACTIONS);
        RibbonActionItem deleteRibbonActionItem = actionsRibbonGroup.getItemByName(L_DELETE);

        GridComponent workstationChangeoverNormsGrid = (GridComponent) view.getComponentByReference(QcadooViewConstants.L_GRID);

        List<Entity> workstationChangeoverNorms = workstationChangeoverNormsGrid.getSelectedEntities();

        boolean areSelected = !workstationChangeoverNorms.isEmpty();
        boolean isEnabled = areSelected && workstationChangeoverNorms.stream().noneMatch(workstationChangeoverNorm -> workstationChangeoverService.hasWorkstationChangeoverForOperationalTasks(workstationChangeoverNorm));

        deleteRibbonActionItem.setEnabled(isEnabled);
        deleteRibbonActionItem.requestUpdate(true);
    }

}
