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
package com.qcadoo.mes.assignmentToShift.states.aop.listeners;

import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.qcadoo.mes.assignmentToShift.constants.AssignmentToShiftConstants;
import com.qcadoo.mes.assignmentToShift.constants.AssignmentToShiftFields;
import com.qcadoo.mes.assignmentToShift.states.aop.AssignmentToShiftStateChangeAspect;
import com.qcadoo.mes.assignmentToShift.states.constants.AssignmentToShiftStateChangePhase;
import com.qcadoo.mes.assignmentToShift.states.constants.AssignmentToShiftStateStringValues;
import com.qcadoo.mes.assignmentToShift.states.listeners.AssignmentToShiftListenerService;
import com.qcadoo.mes.assignmentToShift.utils.AssignmentToShiftConfirmationStatusHelper;
import com.qcadoo.mes.states.StateChangeContext;
import com.qcadoo.mes.states.annotation.RunForStateTransition;
import com.qcadoo.mes.states.annotation.RunInPhase;
import com.qcadoo.mes.states.aop.AbstractStateListenerAspect;
import com.qcadoo.model.api.Entity;
import com.qcadoo.plugin.api.RunIfEnabled;

@Aspect
@Configurable
@RunIfEnabled(AssignmentToShiftConstants.PLUGIN_IDENTIFIER)
public class AssignmentToShiftListenerAspect extends AbstractStateListenerAspect {

    @Autowired
    private AssignmentToShiftListenerService assignmentToShiftListenerService;

    @Autowired
    private AssignmentToShiftConfirmationStatusHelper assignmentToShiftConfirmationStatusHelper;

    @Pointcut(AssignmentToShiftStateChangeAspect.SELECTOR_POINTCUT)
    protected void targetServicePointcut() {

    }

    @RunInPhase(AssignmentToShiftStateChangePhase.DEFAULT)
    @RunForStateTransition(sourceState = AssignmentToShiftStateStringValues.DRAFT)
    @Before(PHASE_EXECUTION_POINTCUT)
    public void onLeaveDraftStateAfterValidation(final StateChangeContext stateChangeContext, final int phase) {
        Entity assignmentToShift = stateChangeContext.getOwner();

        assignmentToShiftConfirmationStatusHelper.clearFailureInfo(assignmentToShift);

        stateChangeContext.setOwner(assignmentToShift);
    }

    @RunInPhase(AssignmentToShiftStateChangePhase.LAST)
    @RunForStateTransition(sourceState = AssignmentToShiftStateStringValues.DRAFT, targetState = AssignmentToShiftStateStringValues.ACCEPTED)
    @After(PHASE_EXECUTION_POINTCUT)
    public void onAcceptedAfterAllOtherOpsSuccessfullyDone(final StateChangeContext stateChangeContext, final int phase) {
        Entity assignmentToShift = stateChangeContext.getOwner();
        assignmentToShift.setField(AssignmentToShiftFields.STAFF_ASSIGNMENT_TO_SHIFTS,
                assignmentToShiftListenerService.addAcceptedStaffsListToAssignment(assignmentToShift));
        assignmentToShift.setField(AssignmentToShiftFields.APPROVED_ATTENDANCE_LIST, true);

        assignmentToShiftConfirmationStatusHelper.setShowLastResultsFlag(assignmentToShift, true);

        stateChangeContext.setOwner(assignmentToShift);
    }

    @RunInPhase(AssignmentToShiftStateChangePhase.LAST)
    @RunForStateTransition(sourceState = AssignmentToShiftStateStringValues.DURING_CORRECTION, targetState = AssignmentToShiftStateStringValues.CORRECTED)
    @After(PHASE_EXECUTION_POINTCUT)
    public void onCorrectedAfterAllOtherOpsSuccessfullyDone(final StateChangeContext stateChangeContext, final int phase) {
        Entity assignmentToShift = stateChangeContext.getOwner();

        assignmentToShift.setField(AssignmentToShiftFields.STAFF_ASSIGNMENT_TO_SHIFTS,
                assignmentToShiftListenerService.addCorrectedStaffsListToAssignment(assignmentToShift));

        assignmentToShiftConfirmationStatusHelper.setShowLastResultsFlag(assignmentToShift, true);

        stateChangeContext.setOwner(assignmentToShift);
    }

}
