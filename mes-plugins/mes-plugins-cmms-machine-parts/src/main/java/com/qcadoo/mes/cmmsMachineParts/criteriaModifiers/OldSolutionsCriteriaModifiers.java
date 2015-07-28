package com.qcadoo.mes.cmmsMachineParts.criteriaModifiers;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.cmmsMachineParts.constants.MaintenanceEventFields;
import com.qcadoo.mes.cmmsMachineParts.states.constants.MaintenanceEventState;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;

@Service
public class OldSolutionsCriteriaModifiers {

    public void showSolutions(final SearchCriteriaBuilder scb) {
        scb.add(SearchRestrictions.in(MaintenanceEventFields.STATE, MaintenanceEventState.EDITED.getStringValue(),
                MaintenanceEventState.CLOSED.getStringValue()));
        scb.add(SearchRestrictions.isNotNull(MaintenanceEventFields.SOLUTION_DESCRIPTION));
    }
}
