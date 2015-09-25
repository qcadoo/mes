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

import com.qcadoo.model.api.Entity;

public enum MaintenanceEventType {
    FAILURE("01failure"), ISSUE("02issue"), PROPOSAL("03proposal");

    private final String type;

    public static MaintenanceEventType from(final Entity entity) {
        return parseString(entity.getStringField(MaintenanceEventFields.TYPE));
    }

    private MaintenanceEventType(final String type) {
        this.type = type;
    }

    public String getStringValue() {
        return type;
    }

    public static MaintenanceEventType parseString(final String type) {
        if ("03proposal".equals(type)) {
            return PROPOSAL;
        } else if ("01failure".equals(type)) {
            return FAILURE;
        } else if ("02issue".equals(type)) {
            return ISSUE;
        }

        throw new IllegalStateException("Unsupported event type: " + type);
    }

}
