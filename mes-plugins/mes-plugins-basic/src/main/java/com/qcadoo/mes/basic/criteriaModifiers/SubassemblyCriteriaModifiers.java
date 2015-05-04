package com.qcadoo.mes.basic.criteriaModifiers;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.constants.WorkstationTypeFields;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;

@Service
public class SubassemblyCriteriaModifiers {

    public void showWorkstationTypesWithSubassembly(final SearchCriteriaBuilder scb) {
        scb.add(SearchRestrictions.eq(WorkstationTypeFields.SUBASSEMBLY, true));
    }
}
