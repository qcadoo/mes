/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.2.0-SNAPSHOT
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

import static com.qcadoo.mes.assignmentToShift.constants.AssignmentToShiftFields.APPROVED_ATTENDANCE_LIST;
import static com.qcadoo.mes.assignmentToShift.constants.AssignmentToShiftFields.STAFF_ASSIGNMENT_TO_SHIFTS;

import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.qcadoo.mes.assignmentToShift.constants.AssignmentToShiftConstants;
import com.qcadoo.mes.assignmentToShift.states.aop.AssignmentToShiftStateChangeAspect;
import com.qcadoo.mes.assignmentToShift.states.constants.AssignmentToShiftStateChangePhase;
import com.qcadoo.mes.assignmentToShift.states.constants.AssignmentToShiftStateStringValues;
import com.qcadoo.mes.assignmentToShift.states.listeners.AssignmentToShiftListenerService;
import com.qcadoo.mes.states.StateChangeContext;
import com.qcadoo.mes.states.annotation.RunForStateTransition;
import com.qcadoo.mes.states.annotation.RunInPhase;
import com.qcadoo.mes.states.aop.AbstractStateListenerAspect;
import com.qcadoo.plugin.api.RunIfEnabled;

@Aspect
@Configurable
@RunIfEnabled(AssignmentToShiftConstants.PLUGIN_IDENTIFIER)
public class AssignmentToShiftListenerAspect extends AbstractStateListenerAspect {

    @Autowired
    private AssignmentToShiftListenerService assignmentToShiftListenerService;

    @RunInPhase(AssignmentToShiftStateChangePhase.LAST)
    @RunForStateTransition(sourceState = AssignmentToShiftStateStringValues.DRAFT, targetState = AssignmentToShiftStateStringValues.ACCEPTED)
    @After(PHASE_EXECUTION_POINTCUT)
    public void afterStartAccepted(final StateChangeContext stateChangeContext, final int phase) {
        stateChangeContext.getOwner().setField(STAFF_ASSIGNMENT_TO_SHIFTS,
                assignmentToShiftListenerService.addAcceptedStaffsListToAssignment(stateChangeContext.getOwner()));
        stateChangeContext.getOwner().setField(APPROVED_ATTENDANCE_LIST, true);
    }

    @RunInPhase(AssignmentToShiftStateChangePhase.LAST)
    @RunForStateTransition(sourceState = AssignmentToShiftStateStringValues.DURING_CORRECTION, targetState = AssignmentToShiftStateStringValues.CORRECTED)
    @After(PHASE_EXECUTION_POINTCUT)
    public void afterComplete(final StateChangeContext stateChangeContext, final int phase) {
        stateChangeContext.getOwner().setField(STAFF_ASSIGNMENT_TO_SHIFTS,
                assignmentToShiftListenerService.addCorrectedStaffsListToAssignment(stateChangeContext.getOwner()));
    }

    @Pointcut(AssignmentToShiftStateChangeAspect.SELECTOR_POINTCUT)
    protected void targetServicePointcut() {
    }

}
