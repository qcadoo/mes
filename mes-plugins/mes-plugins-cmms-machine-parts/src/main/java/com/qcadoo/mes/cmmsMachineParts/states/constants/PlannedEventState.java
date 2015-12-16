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
package com.qcadoo.mes.cmmsMachineParts.states.constants;

import com.google.common.base.Preconditions;
import com.qcadoo.mes.cmmsMachineParts.constants.PlannedEventFields;
import com.qcadoo.mes.states.StateEnum;
import com.qcadoo.model.api.Entity;

public enum PlannedEventState implements StateEnum {

    NEW(PlannedEventStateStringValues.NEW) {

        @Override
        public boolean canChangeTo(final StateEnum targetState) {
            return IN_PLAN.equals(targetState) || PLANNED.equals(targetState) || CANCELED.equals(targetState);
        }
    },
    IN_PLAN(PlannedEventStateStringValues.IN_PLAN) {

        @Override
        public boolean canChangeTo(final StateEnum targetState) {
            return PLANNED.equals(targetState) || CANCELED.equals(targetState);
        }
    },
    PLANNED(PlannedEventStateStringValues.PLANNED) {

        @Override
        public boolean canChangeTo(final StateEnum targetState) {
            return IN_PLAN.equals(targetState) || IN_REALIZATION.equals(targetState) || CANCELED.equals(targetState);
        }

    },

    IN_REALIZATION(PlannedEventStateStringValues.IN_REALIZATION) {

        @Override
        public boolean canChangeTo(final StateEnum targetState) {
            return IN_EDITING.equals(targetState) || CANCELED.equals(targetState);
        }

    },

    IN_EDITING(PlannedEventStateStringValues.IN_EDITING) {

        @Override
        public boolean canChangeTo(final StateEnum targetState) {
            return REALIZED.equals(targetState) || ACCEPTED.equals(targetState) || IN_REALIZATION.equals(targetState);
        }
    },

    REALIZED(PlannedEventStateStringValues.REALIZED) {

        @Override
        public boolean canChangeTo(final StateEnum targetState) {
            return false;
        }

    },
    ACCEPTED(PlannedEventStateStringValues.ACCEPTED) {

        @Override
        public boolean canChangeTo(final StateEnum targetState) {
            return REALIZED.equals(targetState);
        }
    },

    CANCELED(PlannedEventStateStringValues.CANCELED) {

        @Override
        public boolean canChangeTo(final StateEnum targetState) {
            return false;
        }

    };

    private String stringValue;

    private PlannedEventState(final String stringValue) {
        this.stringValue = stringValue;
    }

    public static PlannedEventState of(final Entity ebrEntity) {
        return parseString(ebrEntity.getStringField(PlannedEventFields.STATE));
    }

    public static PlannedEventState parseString(final String string) {
        PlannedEventState parsedStatus = null;
        for (PlannedEventState status : PlannedEventState.values()) {
            if (status.getStringValue().equals(string)) {
                parsedStatus = status;
                break;
            }
        }
        Preconditions.checkArgument(parsedStatus != null, "Couldn't parse '" + string + "'");
        return parsedStatus;
    }

    public abstract boolean canChangeTo(final StateEnum targetState);

    public String getStringValue() {
        return stringValue;
    }

}
