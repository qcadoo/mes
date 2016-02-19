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
import com.qcadoo.mes.cmmsMachineParts.constants.MaintenanceEventFields;
import com.qcadoo.mes.states.StateEnum;
import com.qcadoo.model.api.Entity;

public enum MaintenanceEventState implements StateEnum {

    NEW(MaintenanceEventStateStringValues.NEW) {

        @Override
        public boolean canChangeTo(final StateEnum targetState) {
            return IN_PROGRESS.equals(targetState) || PLANNED.equals(targetState) || REVOKED.equals(targetState);
        }
    },
    IN_PROGRESS(MaintenanceEventStateStringValues.IN_PROGRESS) {

        @Override
        public boolean canChangeTo(final StateEnum targetState) {
            return EDITED.equals(targetState) || PLANNED.equals(targetState) || REVOKED.equals(targetState);
        }
    },
    EDITED(MaintenanceEventStateStringValues.EDITED) {

        @Override
        public boolean canChangeTo(final StateEnum targetState) {
            return ACCEPTED.equals(targetState) || IN_PROGRESS.equals(targetState) || CLOSED.equals(targetState);
        }

    },

    CLOSED(MaintenanceEventStateStringValues.CLOSED) {

        @Override
        public boolean canChangeTo(final StateEnum targetState) {
            return false;
        }

    },

    REVOKED(MaintenanceEventStateStringValues.REVOKED) {

        @Override
        public boolean canChangeTo(final StateEnum targetState) {
            return false;
        }

    },

    PLANNED(MaintenanceEventStateStringValues.PLANNED) {

        @Override
        public boolean canChangeTo(final StateEnum targetState) {
            return false;
        }

    },
    ACCEPTED(MaintenanceEventStateStringValues.ACCEPTED) {

        @Override
        public boolean canChangeTo(final StateEnum targetState) {
            return CLOSED.equals(targetState);
        }

    };

    private String stringValue;

    private MaintenanceEventState(final String stringValue) {
        this.stringValue = stringValue;
    }

    public static MaintenanceEventState of(final Entity ebrEntity) {
        return parseString(ebrEntity.getStringField(MaintenanceEventFields.STATE));
    }

    public static MaintenanceEventState parseString(final String string) {
        MaintenanceEventState parsedStatus = null;
        for (MaintenanceEventState status : MaintenanceEventState.values()) {
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
