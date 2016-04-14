package com.qcadoo.mes.assignmentToShift.criteriaModifiers;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.constants.StaffFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;

@Service
public class StaffCriteriaModifier {

    private static final String CREW_FILTER_VALUE = "crewId";

    public void filterByCrew(final SearchCriteriaBuilder scb, final FilterValueHolder filterValueHolder) {
        if (filterValueHolder.has(CREW_FILTER_VALUE)) {
            Long crewId = filterValueHolder.getLong(CREW_FILTER_VALUE);
            scb.add(SearchRestrictions.eq(StaffFields.CREW + ".id", crewId));
        }
    }

    public void putCrewNumber(final LookupComponent lookupComponent, final Entity crew) {
        FilterValueHolder valueHolder = lookupComponent.getFilterValue();
        if (crew != null) {
            valueHolder.put(CREW_FILTER_VALUE, crew.getId());
        } else {
            valueHolder.remove(CREW_FILTER_VALUE);
        }
        lookupComponent.setFilterValue(valueHolder);
    }
}
