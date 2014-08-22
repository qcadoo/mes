package com.qcadoo.mes.assignmentToShift.dataProviders;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.base.Optional;
import com.qcadoo.mes.assignmentToShift.constants.AssignmentToShiftConstants;
import com.qcadoo.mes.assignmentToShift.constants.AssignmentToShiftFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.JoinType;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchCriterion;
import com.qcadoo.model.api.search.SearchOrder;
import com.qcadoo.model.api.search.SearchProjection;

@Service
public class AssignmentToShiftDataProvider {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public Optional<Entity> find(final AssignmentToShiftCriteria criteria, final Optional<SearchProjection> maybeProjection) {
        SearchCriteriaBuilder scb = createCriteriaBuilder(criteria);
        setProjectionIfPresent(scb, maybeProjection);
        return Optional.fromNullable(scb.setMaxResults(1).uniqueResult());
    }

    public List<Entity> findAll(final AssignmentToShiftCriteria criteria, final Optional<SearchProjection> maybeProjection,
            final Optional<SearchOrder> maybeSearchOrder) {
        SearchCriteriaBuilder scb = createCriteriaBuilder(criteria);
        setProjectionIfPresent(scb, maybeProjection);
        for (SearchOrder searchOrder : maybeSearchOrder.asSet()) {
            scb.addOrder(searchOrder);
        }
        return scb.list().getEntities();
    }

    private void setProjectionIfPresent(final SearchCriteriaBuilder scb, final Optional<SearchProjection> maybeProjection) {
        for (SearchProjection searchProjection : maybeProjection.asSet()) {
            scb.setProjection(searchProjection);
        }
    }

    public SearchCriteriaBuilder createCriteriaBuilder(final AssignmentToShiftCriteria criteria) {
        SearchCriteriaBuilder scb = getAssignmentDD().find();
        for (SearchCriterion searchCriterion : criteria.getCriteria().asSet()) {
            scb.add(searchCriterion);
        }
        SearchCriteriaBuilder shiftScb = scb.createCriteria(AssignmentToShiftFields.SHIFT, "shift_alias", JoinType.INNER);
        for (SearchCriterion searchCriterion : criteria.getShiftCriteria().asSet()) {
            shiftScb.add(searchCriterion);
        }
        return scb;
    }

    private DataDefinition getAssignmentDD() {
        return dataDefinitionService.get(AssignmentToShiftConstants.PLUGIN_IDENTIFIER,
                AssignmentToShiftConstants.MODEL_ASSIGNMENT_TO_SHIFT);
    }

}
