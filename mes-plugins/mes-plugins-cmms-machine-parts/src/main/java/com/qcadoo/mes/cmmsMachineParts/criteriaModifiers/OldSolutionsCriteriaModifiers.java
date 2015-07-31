package com.qcadoo.mes.cmmsMachineParts.criteriaModifiers;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.cmmsMachineParts.constants.MaintenanceEventFields;
import com.qcadoo.mes.cmmsMachineParts.states.constants.MaintenanceEventState;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;

@Service
public class OldSolutionsCriteriaModifiers {

    public void showSolutionsDivision(final SearchCriteriaBuilder scb) {
        showSolutionsLine(scb);
        scb.add(SearchRestrictions.isNull(MaintenanceEventFields.PRODUCTION_LINE));
    }

    public void showSolutionsLine(final SearchCriteriaBuilder scb) {
        showSolutionsWorkstation(scb);
        scb.add(SearchRestrictions.isNull(MaintenanceEventFields.WORKSTATION));
    }

    public void showSolutionsWorkstation(final SearchCriteriaBuilder scb) {
        showSolutionsSubassembly(scb);
        scb.add(SearchRestrictions.isNull(MaintenanceEventFields.SUBASSEMBLY));
    }

    public void showSolutionsSubassembly(final SearchCriteriaBuilder scb) {
        scb.add(SearchRestrictions.in(MaintenanceEventFields.STATE, MaintenanceEventState.EDITED.getStringValue(),
                MaintenanceEventState.CLOSED.getStringValue()));
        scb.add(SearchRestrictions.isNotNull(MaintenanceEventFields.SOLUTION_DESCRIPTION));
    }
}
