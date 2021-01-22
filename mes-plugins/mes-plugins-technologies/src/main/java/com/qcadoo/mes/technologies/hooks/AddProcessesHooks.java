package com.qcadoo.mes.technologies.hooks;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.technologies.criteriaModifiers.TechnologicalProcessListDetailsCriteriaModifiers;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;
import com.qcadoo.view.constants.QcadooViewConstants;

@Service
public class AddProcessesHooks {

    private static final String L_TECHNOLOGICAL_PROCESSES = "technologicalProcesses";

    public void onBeforeRender(final ViewDefinitionState view) {
        Long technologicalProcessListId = ((FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM))
                .getEntityId();
        GridComponent technologicalProcesses = (GridComponent) view.getComponentByReference(L_TECHNOLOGICAL_PROCESSES);
        FilterValueHolder gridFilterValueHolder = technologicalProcesses.getFilterValue();

        gridFilterValueHolder.put(TechnologicalProcessListDetailsCriteriaModifiers.L_TECHNOLOGICAL_PROCESS_LIST_ID,
                technologicalProcessListId);

        technologicalProcesses.setFilterValue(gridFilterValueHolder);
    }
}
