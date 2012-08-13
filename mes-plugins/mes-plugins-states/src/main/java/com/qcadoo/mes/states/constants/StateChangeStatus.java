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
package com.qcadoo.mes.states.constants;

import com.google.common.base.Preconditions;

public enum StateChangeStatus {

    IN_PROGRESS("01inProgress"), PAUSED("02paused"), SUCCESSFUL("03successful"), FAILURE("04failure"), CANCELED("05canceled");

    private final String stringValue;

    public boolean canContinue() {
        return this.equals(IN_PROGRESS);
    }

    private StateChangeStatus(final String stringValue) {
        this.stringValue = stringValue;
    }

    public static StateChangeStatus parseString(final String string) {
        StateChangeStatus parsedStatus = null;
        for (StateChangeStatus status : StateChangeStatus.values()) {
            if (status.getStringValue().equals(string)) {
                parsedStatus = status;
                break;
            }
        }
        Preconditions.checkArgument(parsedStatus != null, "Couldn't parse StateChangeStatus from string '" + string + "'");
        return parsedStatus;
    }

    public String getStringValue() {
        return stringValue;
    }

    @Override
    public String toString() {
        return stringValue;
    }
}
