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

import org.apache.commons.lang3.StringUtils;

import com.qcadoo.model.api.Entity;

public enum ActionAppliesTo {
    WORKSTATION_OR_SUBASSEMBLY("01workstationOrSubassembly"), WORKSTATION_TYPE("02workstationType"), NONE("");

    private final String appliesTo;

    public static ActionAppliesTo from(final Entity entity) {
        return parseString(entity.getStringField(ActionFields.APPLIES_TO));
    }

    private ActionAppliesTo(final String appliesTo) {
        this.appliesTo = appliesTo;
    }

    public String getStringValue() {
        return appliesTo;
    }

    public static ActionAppliesTo parseString(final String appliesTo) {
        if (StringUtils.isEmpty(appliesTo)) {
            return NONE;
        } else if ("01workstationOrSubassembly".equals(appliesTo)) {
            return WORKSTATION_OR_SUBASSEMBLY;
        } else if ("02workstationType".equals(appliesTo)) {
            return WORKSTATION_TYPE;
        }

        throw new IllegalStateException("Unsupported AppliesTo: " + appliesTo);
    }

}
