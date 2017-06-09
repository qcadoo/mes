/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.4
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
package com.qcadoo.mes.assignmentToShift.states.listeners;

import java.util.List;

import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.qcadoo.mes.assignmentToShift.constants.AssignmentToShiftFields;
import com.qcadoo.mes.assignmentToShift.constants.StaffAssignmentToShiftFields;
import com.qcadoo.mes.assignmentToShift.constants.StaffAssignmentToShiftState;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;

@Service
public class AssignmentToShiftListenerService {

    public List<Entity> addAcceptedStaffsListToAssignment(final Entity assignmentToShift) {
        List<Entity> staffAssignmentToShifts = Lists.newArrayList(getSimpleStaffAssignmentToShifts(assignmentToShift));

        List<Entity> acceptedStaffAssignmentToShifts = setStaffAssignmentToShiftState(staffAssignmentToShifts,
                StaffAssignmentToShiftState.ACCEPTED.getStringValue());

        staffAssignmentToShifts.addAll(acceptedStaffAssignmentToShifts);

        return staffAssignmentToShifts;
    }

    public List<Entity> addCorrectedStaffsListToAssignment(final Entity assignmentToShift) {
        List<Entity> staffAssignmentToShifts = Lists.newArrayList(getNotCorrectedStaffAssignmentToShifts(assignmentToShift));
        List<Entity> simpleStaffAssignmentToShifts = Lists.newArrayList(getSimpleStaffAssignmentToShifts(assignmentToShift));

        List<Entity> correctedStaffAssignmentToShifts = setStaffAssignmentToShiftState(simpleStaffAssignmentToShifts,
                StaffAssignmentToShiftState.CORRECTED.getStringValue());

        staffAssignmentToShifts.addAll(correctedStaffAssignmentToShifts);

        return staffAssignmentToShifts;
    }

    private List<Entity> getSimpleStaffAssignmentToShifts(final Entity assignmentToShift) {
        return assignmentToShift
                .getHasManyField(AssignmentToShiftFields.STAFF_ASSIGNMENT_TO_SHIFTS)
                .find()
                .add(SearchRestrictions.eq(StaffAssignmentToShiftFields.STATE,
                        StaffAssignmentToShiftState.SIMPLE.getStringValue())).list().getEntities();
    }

    private List<Entity> getNotCorrectedStaffAssignmentToShifts(final Entity assignmentToShift) {
        return assignmentToShift
                .getHasManyField(AssignmentToShiftFields.STAFF_ASSIGNMENT_TO_SHIFTS)
                .find()
                .add(SearchRestrictions.ne(StaffAssignmentToShiftFields.STATE,
                        StaffAssignmentToShiftState.CORRECTED.getStringValue())).list().getEntities();
    }

    private List<Entity> setStaffAssignmentToShiftState(final List<Entity> staffAssignmentToShifts, final String state) {
        List<Entity> acceptedStaffAssignmentToShifts = Lists.newArrayList();

        for (Entity staffAssignmentToShift : staffAssignmentToShifts) {
            Entity acceptedStaffAssignmentToShift = staffAssignmentToShift.getDataDefinition().copy(staffAssignmentToShift.getId()).get(0);

            acceptedStaffAssignmentToShift.setField(StaffAssignmentToShiftFields.STATE, state);

            acceptedStaffAssignmentToShifts.add(acceptedStaffAssignmentToShift);
        }

        return acceptedStaffAssignmentToShifts;
    }

}
