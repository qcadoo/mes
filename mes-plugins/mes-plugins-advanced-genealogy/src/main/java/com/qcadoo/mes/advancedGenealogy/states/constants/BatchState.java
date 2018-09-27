/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo Framework
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
package com.qcadoo.mes.advancedGenealogy.states.constants;

import com.google.common.base.Preconditions;
import com.qcadoo.mes.states.StateEnum;

public enum BatchState implements StateEnum {

    TRACKED(BatchStateStringValues.TRACKED) {

        @Override
        public boolean canChangeTo(final StateEnum targetState) {
            return BLOCKED.equals(targetState);
        }
    },
    BLOCKED(BatchStateStringValues.BLOCKED) {

        @Override
        public boolean canChangeTo(final StateEnum targetState) {
            return TRACKED.equals(targetState);
        }
    };

    private final String stringValue;

    private BatchState(final String stringValue) {
        this.stringValue = stringValue;
    }

    public static BatchState parseString(final String stringValue) {
        BatchState parsedStatus = null;
        for (BatchState status : BatchState.values()) {
            if (status.getStringValue().equals(stringValue)) {
                parsedStatus = status;
                break;
            }
        }
        Preconditions.checkArgument(parsedStatus != null, "Couldn't parse BatchState from string '" + stringValue + "'");
        return parsedStatus;
    }

    @Override
    public abstract boolean canChangeTo(final StateEnum targetState);

    @Override
    public String getStringValue() {
        return stringValue;
    }

}
