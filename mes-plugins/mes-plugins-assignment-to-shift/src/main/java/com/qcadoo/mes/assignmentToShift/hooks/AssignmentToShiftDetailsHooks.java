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
package com.qcadoo.mes.assignmentToShift.hooks;

import static com.qcadoo.mes.assignmentToShift.constants.AssignmentToShiftFields.STAFF_ASSIGNMENT_TO_SHIFTS;
import static com.qcadoo.mes.assignmentToShift.constants.AssignmentToShiftFields.STATE;
import static com.qcadoo.mes.assignmentToShift.constants.StaffAssignmentToShiftState.ACCEPTED;
import static com.qcadoo.mes.assignmentToShift.constants.StaffAssignmentToShiftState.CORRECTED;
import static com.qcadoo.mes.assignmentToShift.constants.StaffAssignmentToShiftState.SIMPLE;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.assignmentToShift.states.constants.AssignmentToShiftState;
import com.qcadoo.model.api.search.CustomRestriction;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.GridComponent;

@Service
public class AssignmentToShiftDetailsHooks {

    private static CustomRestriction customRestrictionAccepted = new CustomRestriction() {

        @Override
        public void addRestriction(final SearchCriteriaBuilder searchBuilder) {
            searchBuilder.add(SearchRestrictions.eq(STATE, ACCEPTED.getStringValue()));
        }

    };

    private static CustomRestriction customRestrictionSimple = new CustomRestriction() {

        @Override
        public void addRestriction(final SearchCriteriaBuilder searchBuilder) {
            searchBuilder.add(SearchRestrictions.eq(STATE, SIMPLE.getStringValue()));
        }
    };

    private static CustomRestriction customRestrictionCorrected = new CustomRestriction() {

        @Override
        public void addRestriction(final SearchCriteriaBuilder searchBuilder) {
            searchBuilder.add(SearchRestrictions.eq(STATE, CORRECTED.getStringValue()));
        }

    };

    public final void addDiscriminatorRestrictionToStaffAssignmentGrid(final ViewDefinitionState view) {
        GridComponent grid = (GridComponent) view.getComponentByReference(STAFF_ASSIGNMENT_TO_SHIFTS);
        grid.setCustomRestriction(customRestrictionSimple);
    }

    public final void addDiscriminatorRestrictionToCorrectedStaffAssignmentGrid(final ViewDefinitionState view) {
        GridComponent grid = (GridComponent) view.getComponentByReference("correctedStaffAssignment");
        grid.setCustomRestriction(customRestrictionCorrected);
    }

    public final void addDiscriminatorRestrictionToAcceptedStaffAssignmentGrid(final ViewDefinitionState view) {
        GridComponent grid = (GridComponent) view.getComponentByReference("plannedStaffAssignment");
        grid.setCustomRestriction(customRestrictionAccepted);
    }

    public void disabledGridWhenStateIsAcceptedOrCorrected(final ViewDefinitionState view) {
        FieldComponent stateField = (FieldComponent) view.getComponentByReference(STATE);
        String state = stateField.getFieldValue().toString();
        GridComponent grid = (GridComponent) view.getComponentByReference(STAFF_ASSIGNMENT_TO_SHIFTS);
        if (AssignmentToShiftState.ACCEPTED.getStringValue().equals(state)
                || AssignmentToShiftState.CORRECTED.getStringValue().equals(state)) {
            grid.setEditable(false);
        } else {
            grid.setEditable(true);
        }
    }

}