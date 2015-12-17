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
import com.qcadoo.mes.cmmsMachineParts.states.constants.PlannedEventStateChangeFields;
import com.qcadoo.mes.cmmsMachineParts.states.constants.PlannedEventStateStringValues;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class PlannedEventStateChangeValidators {

    public boolean validate(final DataDefinition evenStateChangetDD, final Entity eventStateChange) {
        String eventStatus = eventStateChange.getStringField(PlannedEventStateChangeFields.TARGET_STATE);
        String oldEventStatus = eventStateChange.getStringField(PlannedEventStateChangeFields.SOURCE_STATE);

        switch (eventStatus) {
            case PlannedEventStateStringValues.CANCELED:
                return validateRequiredComment(evenStateChangetDD, eventStateChange);
            case PlannedEventStateStringValues.IN_REALIZATION:
                if (PlannedEventStateStringValues.IN_EDITING.equals(oldEventStatus)) {
                    return validateRequiredComment(evenStateChangetDD, eventStateChange);
                }
        }

        return true;
    }

    private boolean validateRequiredComment(final DataDefinition evenStateChangetDD, final Entity eventStateChange) {
        String comment = eventStateChange.getStringField(PlannedEventStateChangeFields.COMMENT);

        if (eventStateChange.getBooleanField(PlannedEventStateChangeFields.COMMENT_REQUIRED) && Strings.isNullOrEmpty(comment)) {
            eventStateChange.addError(evenStateChangetDD.getField(PlannedEventStateChangeFields.COMMENT),
                    "cmmsMachineParts.plannedEvent.state.commentRequired");
            return false;
        }

        return true;
    }
}
