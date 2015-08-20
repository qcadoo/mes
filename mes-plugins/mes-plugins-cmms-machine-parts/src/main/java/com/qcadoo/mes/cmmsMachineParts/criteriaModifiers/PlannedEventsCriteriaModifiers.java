package com.qcadoo.mes.cmmsMachineParts.criteriaModifiers;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.cmmsMachineParts.constants.PlannedEventFields;
import com.qcadoo.mes.cmmsMachineParts.states.constants.PlannedEventStateStringValues;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;

@Service
public class PlannedEventsCriteriaModifiers {

    public void filterRealizedAndCancelledEvents(SearchCriteriaBuilder scb, FilterValueHolder filter) {
        if (filter.has(PlannedEventFields.NUMBER)) {
            scb.add(SearchRestrictions.ne(PlannedEventFields.NUMBER, filter.getString(PlannedEventFields.NUMBER)));
        }
        scb.add(SearchRestrictions.ne(PlannedEventFields.STATE, PlannedEventStateStringValues.REALIZED)).add(
                SearchRestrictions.ne(PlannedEventFields.STATE, PlannedEventStateStringValues.CANCELLED));
    }

}
