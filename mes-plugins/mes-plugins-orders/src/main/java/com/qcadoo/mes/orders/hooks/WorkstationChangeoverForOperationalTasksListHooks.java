package com.qcadoo.mes.orders.hooks;

import com.qcadoo.mes.orders.constants.WorkstationChangeoverForOperationalTaskChangeoverType;
import com.qcadoo.mes.orders.constants.WorkstationChangeoverForOperationalTaskDtoFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.ribbon.Ribbon;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.api.ribbon.RibbonGroup;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WorkstationChangeoverForOperationalTasksListHooks {

    private static final String L_ACTIONS = "actions";

    private static final String L_COPY = "copy";

    public void onBeforeRender(final ViewDefinitionState view) {
        setRibbonState(view);
    }

    private void setRibbonState(final ViewDefinitionState view) {
        WindowComponent window = (WindowComponent) view.getComponentByReference(QcadooViewConstants.L_WINDOW);

        Ribbon ribbon = window.getRibbon();
        RibbonGroup actionsRibbonGroup = ribbon.getGroupByName(L_ACTIONS);
        RibbonActionItem copyRibbonActionItem = actionsRibbonGroup.getItemByName(L_COPY);

        GridComponent workstationChangeoverForOperationalTasksGrid = (GridComponent) view.getComponentByReference(QcadooViewConstants.L_GRID);

        List<Entity> workstationChangeoverForOperationalTasks = workstationChangeoverForOperationalTasksGrid.getSelectedEntities();

        boolean areSelected = !workstationChangeoverForOperationalTasks.isEmpty();
        boolean areOwn = workstationChangeoverForOperationalTasks.stream()
                .allMatch(workstationChangeoverForOperationalTaskDto ->
                        WorkstationChangeoverForOperationalTaskChangeoverType.OWN.getStringValue()
                                .equals(workstationChangeoverForOperationalTaskDto.getStringField(WorkstationChangeoverForOperationalTaskDtoFields.CHANGEOVER_TYPE)));

        copyRibbonActionItem.setEnabled(areSelected && areOwn);
        copyRibbonActionItem.requestUpdate(true);
    }

}
