package com.qcadoo.mes.technologies.hooks;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.technologies.criteriaModifiers.TechnologicalProcessListDetailsCriteriaModifiers;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;
import com.qcadoo.view.constants.QcadooViewConstants;

@Service
public class TechnologicalProcessListDetailsHooks {

    private static final String L_TECHNOLOGICAL_PROCESS_LOOKUP = "technologicalProcessLookup";

    private static final String L_TECHNOLOGIES = "technologies";

    public void onBeforeRender(final ViewDefinitionState view) {
        Long technologicalProcessListId = ((FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM))
                .getEntityId();
        LookupComponent technologicalProcessLookup = (LookupComponent) view
                .getComponentByReference(L_TECHNOLOGICAL_PROCESS_LOOKUP);
        FilterValueHolder filterValueHolder = technologicalProcessLookup.getFilterValue();

        filterValueHolder.put(TechnologicalProcessListDetailsCriteriaModifiers.L_TECHNOLOGICAL_PROCESS_LIST_ID,
                technologicalProcessListId);

        technologicalProcessLookup.setFilterValue(filterValueHolder);

        GridComponent technologies = (GridComponent) view.getComponentByReference(L_TECHNOLOGIES);
        FilterValueHolder gridFilterValueHolder = technologies.getFilterValue();

        gridFilterValueHolder.put(TechnologicalProcessListDetailsCriteriaModifiers.L_TECHNOLOGICAL_PROCESS_LIST_ID,
                technologicalProcessListId);

        technologies.setFilterValue(gridFilterValueHolder);
    }
}
