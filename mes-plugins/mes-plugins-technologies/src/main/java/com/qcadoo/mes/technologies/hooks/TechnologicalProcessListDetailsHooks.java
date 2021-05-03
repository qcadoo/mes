package com.qcadoo.mes.technologies.hooks;

import java.util.Objects;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.technologies.criteriaModifiers.TechnologicalProcessListDetailsCriteriaModifiers;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.constants.QcadooViewConstants;

@Service
public class TechnologicalProcessListDetailsHooks {

    private static final String L_TECHNOLOGIES = "technologies";

    private static final String L_TECHNOLOGICAL_PROCESSES = "technologicalProcesses";

    private static final String L_ADD_PROCESSES = "addProcesses";

    public void onBeforeRender(final ViewDefinitionState view) {
        GridComponent technologies = (GridComponent) view.getComponentByReference(L_TECHNOLOGIES);

        WindowComponent window = (WindowComponent) view.getComponentByReference(QcadooViewConstants.L_WINDOW);
        RibbonActionItem addProcesses = window.getRibbon().getGroupByName(L_TECHNOLOGICAL_PROCESSES)
                .getItemByName(L_ADD_PROCESSES);

        Long technologicalProcessListId = ((FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM))
                .getEntityId();

        if (Objects.nonNull(technologicalProcessListId)) {
            addProcesses.setEnabled(true);
            addProcesses.requestUpdate(true);
        }

        FilterValueHolder gridFilterValueHolder = technologies.getFilterValue();

        gridFilterValueHolder.put(TechnologicalProcessListDetailsCriteriaModifiers.L_TECHNOLOGICAL_PROCESS_LIST_ID,
                technologicalProcessListId);

        technologies.setFilterValue(gridFilterValueHolder);
    }

}
