/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.3
 *
 * This file is part of Qcadoo.
 *
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */
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
