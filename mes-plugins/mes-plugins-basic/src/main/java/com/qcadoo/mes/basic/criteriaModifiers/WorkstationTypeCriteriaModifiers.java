package com.qcadoo.mes.basic.criteriaModifiers;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.constants.WorkstationTypeFields;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;

@Service
public class WorkstationTypeCriteriaModifiers {

    public void showWorkstationTypesWithSubassembly(final SearchCriteriaBuilder scb) {
        scb.add(SearchRestrictions.eq(WorkstationTypeFields.SUBASSEMBLY, true));
    }

    public void showWorkstationTypesWithoutSubassembly(final SearchCriteriaBuilder scb) {
        scb.add(SearchRestrictions.or(SearchRestrictions.eq(WorkstationTypeFields.SUBASSEMBLY, false),
                SearchRestrictions.isNull(WorkstationTypeFields.SUBASSEMBLY)));
    }
}
