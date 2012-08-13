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
package com.qcadoo.mes.assignmentToShift.states.constants;

import com.google.common.base.Preconditions;
import com.qcadoo.mes.states.StateEnum;

public enum AssignmentToShiftState implements StateEnum {

    DRAFT(AssignmentToShiftStateStringValues.DRAFT) {

        @Override
        public boolean canChangeTo(final StateEnum targetState) {
            return ACCEPTED.equals(targetState);
        }
    },
    ACCEPTED(AssignmentToShiftStateStringValues.ACCEPTED) {

        @Override
        public boolean canChangeTo(final StateEnum targetState) {
            return DURING_CORRECTION.equals(targetState);
        }
    },
    DURING_CORRECTION(AssignmentToShiftStateStringValues.DURING_CORRECTION) {

        @Override
        public boolean canChangeTo(final StateEnum targetState) {
            return CORRECTED.equals(targetState);
        }
    },
    CORRECTED(AssignmentToShiftStateStringValues.CORRECTED) {

        @Override
        public boolean canChangeTo(final StateEnum targetState) {
            return DURING_CORRECTION.equals(targetState);
        }
    };

    private final String stringValue;

    private AssignmentToShiftState(final String state) {
        this.stringValue = state;
    }

    @Override
    public String getStringValue() {
        return stringValue;
    }

    public static AssignmentToShiftState parseString(final String string) {
        AssignmentToShiftState parsedStatus = null;
        for (AssignmentToShiftState status : AssignmentToShiftState.values()) {
            if (status.getStringValue().equals(string)) {
                parsedStatus = status;
                break;
            }
        }
        Preconditions.checkArgument(parsedStatus != null, "Couldn't parse AssignmentToShiftState from string '" + string + "'");
        return parsedStatus;
    }

    public abstract boolean canChangeTo(final StateEnum targetState);
}
