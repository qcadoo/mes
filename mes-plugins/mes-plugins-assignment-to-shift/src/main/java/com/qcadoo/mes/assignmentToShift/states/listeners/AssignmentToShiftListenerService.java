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
