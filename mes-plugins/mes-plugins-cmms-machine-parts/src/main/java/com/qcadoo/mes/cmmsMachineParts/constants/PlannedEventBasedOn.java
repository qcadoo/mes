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

public enum PlannedEventBasedOn {
    DATE("01date"), COUNTER("02counter");

    private final String basedOn;

    public static PlannedEventBasedOn from(final Entity entity) {
        return parseString(entity.getStringField(PlannedEventFields.BASED_ON));
    }

    private PlannedEventBasedOn(final String appliesTo) {
        this.basedOn = appliesTo;
    }

    public String getStringValue() {
        return basedOn;
    }

    public static PlannedEventBasedOn parseString(final String basedOn) {
        if ("01date".equals(basedOn)) {
            return DATE;
        } else if ("02counter".equals(basedOn)) {
            return COUNTER;
        }

        throw new IllegalStateException("Unsupported basedOn: " + basedOn);
    }

}
