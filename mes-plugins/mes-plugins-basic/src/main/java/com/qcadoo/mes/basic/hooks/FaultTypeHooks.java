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
package com.qcadoo.mes.basic.hooks;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.constants.FaultTypeAppliesTo;
import com.qcadoo.mes.basic.constants.FaultTypeFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class FaultTypeHooks {

    public void onSave(final DataDefinition faultTypeDD, final Entity faultType) {
        String appliesTo = faultType.getStringField(FaultTypeFields.APPLIES_TO);

        if (FaultTypeAppliesTo.WORKSTATION_OR_SUBASSEMBLY.getStringValue().equals(appliesTo)) {
            clearFields(faultType, false, true);
        } else if (FaultTypeAppliesTo.WORKSTATION_TYPE.getStringValue().equals(appliesTo)) {
            clearFields(faultType, true, false);
        } else {
            clearFields(faultType, true, true);
        }
    }

    private void clearFields(final Entity faultType, boolean clearWorkstations, boolean clearWorkstationTypes) {
        if (clearWorkstations) {
            faultType.setField(FaultTypeFields.WORKSTATIONS, null);
            faultType.setField(FaultTypeFields.SUBASSEMBLIES, null);
        }

        if (clearWorkstationTypes) {
            faultType.setField(FaultTypeFields.WORKSTATION_TYPES, null);
        }
    }

}
