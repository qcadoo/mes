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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.base.Strings;
import com.qcadoo.mes.basic.constants.StaffFields;
import com.qcadoo.mes.cmmsMachineParts.constants.MaintenanceEventFields;
import com.qcadoo.mes.cmmsMachineParts.states.constants.MaintenanceEventState;
import com.qcadoo.mes.cmmsMachineParts.states.constants.MaintenanceEventStateChangeDescriber;
import com.qcadoo.mes.states.service.StateChangeEntityBuilder;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class MaintenanceEventHooks {

    @Autowired
    private MaintenanceEventStateChangeDescriber describer;

    @Autowired
    private StateChangeEntityBuilder stateChangeEntityBuilder;

    public void onCreate(final DataDefinition eventDD, final Entity event) {
        setInitialState(event);
    }

    public void onSave(final DataDefinition eventDD, final Entity event) {
        if (event.getBelongsToField(MaintenanceEventFields.PERSON_RECEIVING) != null) {
            String person = Strings.nullToEmpty(event.getBelongsToField(MaintenanceEventFields.PERSON_RECEIVING).getStringField(
                    StaffFields.NAME))
                    + " "
                    + Strings.nullToEmpty(event.getBelongsToField(MaintenanceEventFields.PERSON_RECEIVING).getStringField(
                            StaffFields.SURNAME));
            event.setField(MaintenanceEventFields.PERSON_RECEIVING_NAME, person);
        }
        if (!MaintenanceEventState.NEW.getStringValue().equals(event.getStringField(MaintenanceEventFields.STATE))) {
            event.setField(MaintenanceEventFields.SOUND_NOTIFICATIONS, false);
        }
    }

    private void setInitialState(final Entity event) {
        stateChangeEntityBuilder.buildInitial(describer, event, MaintenanceEventState.NEW);
    }

}
