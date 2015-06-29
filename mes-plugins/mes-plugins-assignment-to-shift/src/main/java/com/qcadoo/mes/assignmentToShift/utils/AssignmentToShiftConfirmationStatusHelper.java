/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo Framework
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
package com.qcadoo.mes.assignmentToShift.utils;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.assignmentToShift.constants.AssignmentToShiftFields;
import com.qcadoo.model.api.Entity;

@Service
public class AssignmentToShiftConfirmationStatusHelper {

    public void clearFailureInfo(final Entity assignmentToShift) {
        setLastStateChangeFail(assignmentToShift, false, null);
        setShowLastResultsFlag(assignmentToShift, false);
    }

    public void markAsFailure(final Entity assignmentToShift, final String message) {
        setLastStateChangeFail(assignmentToShift, true, message);
        setShowLastResultsFlag(assignmentToShift, true);
    }

    private void setLastStateChangeFail(final Entity assignmentToShift, final boolean lastStateChangeFails,
            final String lastStateChangeFailCause) {
        assignmentToShift.setField(AssignmentToShiftFields.LAST_STATE_CHANGE_FAILS, lastStateChangeFails);
        assignmentToShift.setField(AssignmentToShiftFields.LAST_STATE_CHANGE_FAIL_CAUSE, lastStateChangeFailCause);
    }

    public void setShowLastResultsFlag(final Entity assignmentToShift, final boolean showLastResults) {
        assignmentToShift.setField(AssignmentToShiftFields.SHOW_LAST_STATE_CHANGE_RESULT, showLastResults);
    }

}
