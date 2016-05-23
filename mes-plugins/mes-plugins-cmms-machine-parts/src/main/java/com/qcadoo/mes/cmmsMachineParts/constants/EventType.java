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
package com.qcadoo.mes.cmmsMachineParts.constants;

import com.google.common.base.Preconditions;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

public enum EventType {
    MAINTENANCE_EVENT {

        @Override
        public String getModelName() {
            return CmmsMachinePartsConstants.MODEL_MAINTENANCE_EVENT;
        }

        @Override
        public String getMachinePartsName() {
            return MaintenanceEventFields.MACHINE_PARTS_FOR_EVENT;
        }
    },
    PLANNED_EVENT {

        @Override
        public String getModelName() {
            return CmmsMachinePartsConstants.MODEL_PLANNED_EVENT;
        }

        @Override
        public String getMachinePartsName() {
            return PlannedEventFields.MACHINE_PARTS_FOR_EVENT;
        }
    };

    public static EventType of(final Entity event) {
        DataDefinition dd = event.getDataDefinition();
        Preconditions.checkArgument(CmmsMachinePartsConstants.PLUGIN_IDENTIFIER.equals(dd.getPluginIdentifier()));
        if (CmmsMachinePartsConstants.MODEL_MAINTENANCE_EVENT.equals(dd.getName())) {
            return MAINTENANCE_EVENT;
        } else if (CmmsMachinePartsConstants.MODEL_PLANNED_EVENT.equals(dd.getName())) {
            return PLANNED_EVENT;
        }
        throw new IllegalArgumentException(String.format("Unsupported model type: '%s'", dd.getName()));
    }

    public abstract String getModelName();

    public abstract String getMachinePartsName();

}
