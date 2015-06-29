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

    public List<Entity> addAcceptedStaffsListToAssignment(final Entity assignment) {
        List<Entity> staffs = Lists.newArrayList(assignment.getHasManyField(AssignmentToShiftFields.STAFF_ASSIGNMENT_TO_SHIFTS));

        List<Entity> acceptedStaffs = setFlagPlannedToStaffAssignmentToShift(staffs,
                StaffAssignmentToShiftState.ACCEPTED.getStringValue());

        staffs.addAll(acceptedStaffs);

        return staffs;
    }

    public List<Entity> addCorrectedStaffsListToAssignment(final Entity assignment) {
        List<Entity> simpleAndAcceptedStaffs = Lists.newArrayList(assignment
                .getHasManyField(AssignmentToShiftFields.STAFF_ASSIGNMENT_TO_SHIFTS)
                .find()
                .add(SearchRestrictions.ne(StaffAssignmentToShiftFields.STATE,
                        StaffAssignmentToShiftState.CORRECTED.getStringValue())).list().getEntities());

        List<Entity> staffsForCorrected = Lists.newArrayList(assignment
                .getHasManyField(AssignmentToShiftFields.STAFF_ASSIGNMENT_TO_SHIFTS)
                .find()
                .add(SearchRestrictions.eq(StaffAssignmentToShiftFields.STATE,
                        StaffAssignmentToShiftState.SIMPLE.getStringValue())).list().getEntities());

        List<Entity> correctedStaffs = setFlagPlannedToStaffAssignmentToShift(staffsForCorrected,
                StaffAssignmentToShiftState.CORRECTED.getStringValue());

        simpleAndAcceptedStaffs.addAll(correctedStaffs);

        return simpleAndAcceptedStaffs;
    }

    private List<Entity> setFlagPlannedToStaffAssignmentToShift(final List<Entity> staffs, final String state) {
        List<Entity> staffWithSetFlags = Lists.newArrayList();

        for (Entity staff : staffs) {
            Entity acceptedStaff = staff.getDataDefinition().copy(staff.getId()).get(0);

            acceptedStaff.setField(StaffAssignmentToShiftFields.STATE, state);

            staffWithSetFlags.add(acceptedStaff);
        }

        return staffWithSetFlags;
    }

}
