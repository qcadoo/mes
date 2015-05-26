package com.qcadoo.mes.basic.criteriaModifiers;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.constants.DivisionFields;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;

@Service
public class BasicDivisionCriteriaModifiers {

    public void showDivisionWithoutFactory(final SearchCriteriaBuilder scb) {
        scb.add(SearchRestrictions.isNull(DivisionFields.FACTORY));
    }

}
