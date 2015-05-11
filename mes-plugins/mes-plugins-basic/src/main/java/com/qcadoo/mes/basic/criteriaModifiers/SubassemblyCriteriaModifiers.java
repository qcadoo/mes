package com.qcadoo.mes.basic.criteriaModifiers;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.constants.SubassemblyFields;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;

@Service
public class SubassemblyCriteriaModifiers {

    public void showSubassembliesWithoutWorkstation(final SearchCriteriaBuilder scb) {
        scb.add(SearchRestrictions.isNull(SubassemblyFields.WORKSTATION));
    }

}
