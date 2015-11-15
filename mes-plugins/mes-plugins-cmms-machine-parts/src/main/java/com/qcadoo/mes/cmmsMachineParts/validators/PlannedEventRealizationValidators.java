/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.4
 * <p>
 * This file is part of Qcadoo.
 * <p>
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */
package com.qcadoo.mes.cmmsMachineParts.validators;

import java.util.Date;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.cmmsMachineParts.constants.PlannedEventFields;
import com.qcadoo.mes.cmmsMachineParts.constants.PlannedEventRealizationFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service public class PlannedEventRealizationValidators {

    public boolean validatesWith(final DataDefinition dataDefinition, final Entity plannedEventRealization) {

        if (!checkOperatorWorkTime(dataDefinition, plannedEventRealization)) {
            return false;
        }

        if (!checkAction(dataDefinition, plannedEventRealization)) {
            return false;
        }

        return true;
    }

    private boolean checkAction(DataDefinition dataDefinition, Entity plannedEventRealization) {
        Entity plannedEvent = plannedEventRealization.getBelongsToField(PlannedEventRealizationFields.PLANNED_EVENT);
        if (plannedEvent.getBooleanField(PlannedEventFields.PLANNED_SEPARATELY)) {
            Entity actionForPlannedEvent = plannedEventRealization.getBelongsToField(PlannedEventRealizationFields.ACTION);

            if(actionForPlannedEvent == null){
                plannedEventRealization.addError(dataDefinition.getField(PlannedEventRealizationFields.ACTION),
                        "cmmsMachineParts.plannedEventRealizationDetails.error.requiredAction");
                return false;
            }
        }

        return true;
    }

    private boolean checkOperatorWorkTime(final DataDefinition dataDefinition, final Entity plannedEventRealization) {
        Date startDate = plannedEventRealization.getDateField(PlannedEventRealizationFields.START_DATE);
        Date finishDate = plannedEventRealization.getDateField(PlannedEventRealizationFields.FINISH_DATE);

        if (startDate == null || finishDate == null || finishDate.after(startDate)) {
            return true;
        }
        plannedEventRealization.addError(dataDefinition.getField(PlannedEventRealizationFields.FINISH_DATE),
                "cmmsMachineParts.plannedEventRealizationDetails.error.wrongDateOrder");
        return false;
    }
}
