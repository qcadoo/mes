package com.qcadoo.mes.assignmentToShift.criteriaModifiers;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.localization.api.utils.DateUtils;
import com.qcadoo.mes.assignmentToShift.constants.AssignmentToShiftConstants;
import com.qcadoo.mes.assignmentToShift.constants.AssignmentToShiftFields;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.basic.constants.StaffFields;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;

@Service
public class StaffCriteriaModifier {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private ParameterService parameterService;

    private static final String CREW_FILTER_VALUE = "crewId";

    private static final String WORKERS_IDS = "workersIds";

    public void filterByCrew(final SearchCriteriaBuilder scb, final FilterValueHolder filterValueHolder) {
        if (filterValueHolder.has(CREW_FILTER_VALUE)) {
            Long crewId = filterValueHolder.getLong(CREW_FILTER_VALUE);
            scb.add(SearchRestrictions.eq(StaffFields.CREW + ".id", crewId));
        }
    }

    public void filterByCrewAndUnassigned(final SearchCriteriaBuilder scb, final FilterValueHolder filterValueHolder) {
        filterByCrew(scb, filterValueHolder);
        hideAssignedWorkers(scb, filterValueHolder);
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

    public void hideAssignedWorkers(final SearchCriteriaBuilder scb, final FilterValueHolder filterValueHolder) {
        boolean shouldHideAssignedWorkers = parameterService.getParameter().getBooleanField("hideAssignedStaff");
        if (shouldHideAssignedWorkers) {
            if (filterValueHolder.has(WORKERS_IDS)) {
                List<Long> ids = filterValueHolder.getListOfLongs(WORKERS_IDS);
                scb.add(SearchRestrictions.not(SearchRestrictions.in("id", ids)));
            }
        }

    }

    public void setFilterParameters(final LookupComponent staffLookup, Entity assignmentToShift) {
        FilterValueHolder filter = staffLookup.getFilterValue();

        Date startDate = assignmentToShift.getDateField(AssignmentToShiftFields.START_DATE);
        String hql = "select staff.worker.id as workerId from #assignmentToShift_staffAssignmentToShift staff "
                + " where staff.assignmentToShift.startDate = '" + DateUtils.toDateString(startDate) + "'";
        List<Entity> workersIds = dataDefinitionService
                .get(AssignmentToShiftConstants.PLUGIN_IDENTIFIER, AssignmentToShiftConstants.MODEL_STAFF_ASSIGNMENT_TO_SHIFT)
                .find(hql).list().getEntities();
        if (!workersIds.isEmpty()) {
            filter.put("workersIds", workersIds.stream().map(id -> id.getLongField("workerId")).collect(Collectors.toList()));
            staffLookup.setFilterValue(filter);
        }
    }
}