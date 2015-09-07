package com.qcadoo.mes.basic.criteriaModifiers;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.constants.StaffFields;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;

@Service
public class StaffCriteriaModifiers {

    public void showStaffWithoutCrew(final SearchCriteriaBuilder scb) {
        scb.add(SearchRestrictions.isNull(StaffFields.CREW));
    }
}
