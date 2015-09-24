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

import org.springframework.stereotype.Service;

import com.qcadoo.mes.cmmsMachineParts.constants.ActionAppliesTo;
import com.qcadoo.mes.cmmsMachineParts.constants.ActionFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class ActionHooks {

    public void onSave(final DataDefinition actionDD, final Entity action) {
        ActionAppliesTo appliesTo = ActionAppliesTo.from(action);
        if (appliesTo.compareTo(ActionAppliesTo.WORKSTATION_OR_SUBASSEMBLY) == 0) {
            clearFields(action, false, true);
        } else if (appliesTo.compareTo(ActionAppliesTo.WORKSTATION_TYPE) == 0) {
            clearFields(action, true, false);
        } else {
            clearFields(action, true, true);
        }
    }

    private void clearFields(final Entity action, boolean clearWorkstations, boolean clearWorkstationTypes) {
        if (clearWorkstations) {
            action.setField(ActionFields.WORKSTATIONS, null);
            action.setField(ActionFields.SUBASSEMBLIES, null);
        }
        if (clearWorkstationTypes) {
            action.setField(ActionFields.WORKSTATION_TYPES, null);
        }
    }
}
