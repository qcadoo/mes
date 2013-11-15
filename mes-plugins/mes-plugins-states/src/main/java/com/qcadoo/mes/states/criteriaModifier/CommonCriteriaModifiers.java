package com.qcadoo.mes.states.criteriaModifier;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.states.constants.StateChangeStatus;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;

@Service
public class CommonCriteriaModifiers {

    public void filterHistory(final SearchCriteriaBuilder scb) {
        scb.add(SearchRestrictions.eq("status", StateChangeStatus.SUCCESSFUL.getStringValue()));
    }
}
