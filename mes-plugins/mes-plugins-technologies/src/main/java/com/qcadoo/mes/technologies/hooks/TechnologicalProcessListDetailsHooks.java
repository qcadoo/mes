package com.qcadoo.mes.technologies.hooks;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.technologies.criteriaModifiers.TechnologicalProcessListDetailsCriteriaModifiers;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;
import com.qcadoo.view.constants.QcadooViewConstants;

@Service
public class TechnologicalProcessListDetailsHooks {

    private static final String L_TECHNOLOGICAL_PROCESS_LOOKUP = "technologicalProcessLookup";

    public void onBeforeRender(final ViewDefinitionState view) {
        LookupComponent technologicalProcessLookup = (LookupComponent) view
                .getComponentByReference(L_TECHNOLOGICAL_PROCESS_LOOKUP);
        setCriteriaModifierParameters(technologicalProcessLookup,
                ((FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM)).getEntityId());
    }

    private void setCriteriaModifierParameters(LookupComponent technologicalProcessLookup, Long technologicalProcessListId) {
        FilterValueHolder filterValueHolder = technologicalProcessLookup.getFilterValue();

        filterValueHolder.put(TechnologicalProcessListDetailsCriteriaModifiers.L_TECHNOLOGICAL_PROCESS_LIST_ID,
                technologicalProcessListId);

        technologicalProcessLookup.setFilterValue(filterValueHolder);
        technologicalProcessLookup.requestComponentUpdateState();
    }
}
