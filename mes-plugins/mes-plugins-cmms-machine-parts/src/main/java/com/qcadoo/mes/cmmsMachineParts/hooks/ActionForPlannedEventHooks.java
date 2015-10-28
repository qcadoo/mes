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
package com.qcadoo.mes.cmmsMachineParts.hooks;

import com.qcadoo.mes.cmmsMachineParts.constants.ActionFields;
import org.springframework.stereotype.Service;

import com.google.common.base.Strings;
import com.qcadoo.mes.basic.constants.StaffFields;
import com.qcadoo.mes.cmmsMachineParts.constants.ActionForPlannedEventFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class ActionForPlannedEventHooks {

    public void onSave(final DataDefinition actionDD, final Entity actionForPlannedEvent) {
        if (actionForPlannedEvent.getBelongsToField(ActionForPlannedEventFields.RESPONSIBLE_WORKER) != null) {
            String person = Strings.nullToEmpty(actionForPlannedEvent.getBelongsToField(
                    ActionForPlannedEventFields.RESPONSIBLE_WORKER).getStringField(StaffFields.NAME))
                    + " "
                    + Strings.nullToEmpty(actionForPlannedEvent.getBelongsToField(ActionForPlannedEventFields.RESPONSIBLE_WORKER)
                            .getStringField(StaffFields.SURNAME));
            actionForPlannedEvent.setField(ActionForPlannedEventFields.RESPONSIBLE_WORKER_NAME, person);
        }

        Entity action = actionForPlannedEvent.getBelongsToField(ActionForPlannedEventFields.ACTION);
        if(action!=null){
            actionForPlannedEvent.setField(ActionForPlannedEventFields.ACTION_NAME, action.getStringField(ActionFields.NAME));
        }
    }

}
