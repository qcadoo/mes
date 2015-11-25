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
package com.qcadoo.mes.cmmsMachineParts.validators;

import org.springframework.stereotype.Service;

import com.google.common.base.Strings;
import com.qcadoo.mes.cmmsMachineParts.states.constants.MaintenanceEventStateChangeFields;
import com.qcadoo.mes.cmmsMachineParts.states.constants.MaintenanceEventStateStringValues;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class MaintenanceEventStateChangeValidators {

    public boolean validate(final DataDefinition evenStateChangetDD, final Entity eventStateChange) {
        String targetState = eventStateChange.getStringField(MaintenanceEventStateChangeFields.TARGET_STATE);

        switch (targetState) {
            case MaintenanceEventStateStringValues.REVOKED:
                return validateForRevokedStatus(evenStateChangetDD, eventStateChange);
            case MaintenanceEventStateStringValues.PLANNED:
                return validateForPlannedStatus(evenStateChangetDD, eventStateChange);
            case MaintenanceEventStateStringValues.IN_PROGRESS:
                return validateForRevokedStatus(evenStateChangetDD, eventStateChange);
        }

        return true;
    }

    private boolean validateForRevokedStatus(final DataDefinition evenStateChangetDD, final Entity eventStateChange) {
        String comment = eventStateChange.getStringField(MaintenanceEventStateChangeFields.COMMENT);

        if (eventStateChange.getBooleanField(MaintenanceEventStateChangeFields.COMMENT_REQUIRED)
                && Strings.isNullOrEmpty(comment)) {
            eventStateChange.addError(evenStateChangetDD.getField(MaintenanceEventStateChangeFields.COMMENT),
                    "cmmsMachineParts.maintenanceEvent.state.commentRequired");
            return false;
        }

        return true;
    }

    private boolean validateForPlannedStatus(final DataDefinition evenStateChangetDD, final Entity eventStateChange) {
        String type = eventStateChange.getStringField(MaintenanceEventStateChangeFields.PLANNED_EVENT_TYPE);

        if (eventStateChange.getBooleanField(MaintenanceEventStateChangeFields.PLANNED_EVENT_TYPE_REQUIRED)
                && Strings.isNullOrEmpty(type)) {
            eventStateChange.addError(evenStateChangetDD.getField(MaintenanceEventStateChangeFields.PLANNED_EVENT_TYPE),
                    "cmmsMachineParts.maintenanceEventStateChange.plannedEventType.required.error");
            return false;
        }

        return true;
    }
}
