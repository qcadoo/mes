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

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.cmmsMachineParts.ActionsService;
import com.qcadoo.mes.cmmsMachineParts.constants.ActionForPlannedEventFields;
import com.qcadoo.mes.cmmsMachineParts.constants.ActionForPlannedEventState;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class ActionForPlannedEventValidators {

    @Autowired
    private ActionsService actionsService;

    public boolean validateRequiredFields(final DataDefinition actionDD, final Entity actionForPlannedEvent) {

        Entity defaultAction = actionsService.getDefaultAction();
        Entity action = actionForPlannedEvent.getBelongsToField(ActionForPlannedEventFields.ACTION);
        boolean correct = true;
        if (defaultAction != null && action != null && defaultAction.getId().equals(action.getId())) {
            if (StringUtils.isEmpty(actionForPlannedEvent.getStringField(ActionForPlannedEventFields.DESCRIPTION))) {
                actionForPlannedEvent.addError(actionDD.getField(ActionForPlannedEventFields.DESCRIPTION),
                        "cmmsMachineParts.actionForPlannedEvent.error.descriptionRequired");
                correct = false;
            }
        }
        ActionForPlannedEventState state = ActionForPlannedEventState.from(actionForPlannedEvent);
        if (state.equals(ActionForPlannedEventState.INCORRECT)) {
            if (StringUtils.isEmpty(actionForPlannedEvent.getStringField(ActionForPlannedEventFields.REASON))) {
                actionForPlannedEvent.addError(actionDD.getField(ActionForPlannedEventFields.REASON),
                        "cmmsMachineParts.actionForPlannedEvent.error.reasonRequired");
                correct = false;
            }
        }
        return correct;
    }
}
