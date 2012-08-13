/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.7
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

import static com.qcadoo.mes.assignmentToShift.constants.AssignmentToShiftFields.STAFF_ASSIGNMENT_TO_SHIFTS;
import static com.qcadoo.mes.assignmentToShift.constants.StaffAssignmentToShiftFields.STATE;
import static com.qcadoo.mes.assignmentToShift.constants.StaffAssignmentToShiftState.ACCEPTED;
import static com.qcadoo.mes.assignmentToShift.constants.StaffAssignmentToShiftState.CORRECTED;
import static com.qcadoo.mes.assignmentToShift.constants.StaffAssignmentToShiftState.SIMPLE;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;

@Service
public class AssignmentToShiftListenerService {

    public List<Entity> addAcceptedStaffsListToAssignment(final Entity assignment) {
        ArrayList<Entity> staffs = Lists.newArrayList(assignment.getHasManyField(STAFF_ASSIGNMENT_TO_SHIFTS));
        List<Entity> acceptedStaffs = setFlagPlannedToStaffAssignmentToShift(staffs, ACCEPTED.getStringValue());
        staffs.addAll(acceptedStaffs);
        return staffs;
    }

    public List<Entity> addCorrectedStaffsListToAssignment(final Entity assignment) {
        ArrayList<Entity> simpleAndAcceptedStaffs = Lists.newArrayList(assignment.getHasManyField(STAFF_ASSIGNMENT_TO_SHIFTS)
                .find().add(SearchRestrictions.ne(STATE, CORRECTED.getStringValue())).list().getEntities());
        ArrayList<Entity> staffsForCorrected = Lists.newArrayList(assignment.getHasManyField(STAFF_ASSIGNMENT_TO_SHIFTS).find()
                .add(SearchRestrictions.eq(STATE, SIMPLE.getStringValue())).list().getEntities());
        List<Entity> correctedStaffs = setFlagPlannedToStaffAssignmentToShift(staffsForCorrected, CORRECTED.getStringValue());
        simpleAndAcceptedStaffs.addAll(correctedStaffs);
        return simpleAndAcceptedStaffs;
    }

    private List<Entity> setFlagPlannedToStaffAssignmentToShift(final List<Entity> staffs, final String state) {
        List<Entity> staffWithSetFlags = new ArrayList<Entity>();
        for (Entity staff : staffs) {
            Entity acceptedStaff = staff.getDataDefinition().copy(staff.getId()).get(0);
            acceptedStaff.setField(STATE, state);
            staffWithSetFlags.add(acceptedStaff);
        }
        return staffWithSetFlags;
    }
}
