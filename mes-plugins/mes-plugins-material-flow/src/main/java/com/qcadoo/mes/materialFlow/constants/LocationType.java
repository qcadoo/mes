/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.6
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
package com.qcadoo.mes.materialFlow.constants;

public enum LocationType {

    SIMPLE_CONTROL_POINT("01simpleControlPoint");

    private final String type;

    private LocationType(final String type) {
        this.type = type;
    }

    public String getStringValue() {
        return type;
    }

    public static LocationType parseString(final String type) {
        if ("01simpleControlPoint".equalsIgnoreCase(type)) {
            return SIMPLE_CONTROL_POINT;
        } else {
            throw new IllegalArgumentException("Couldn't parse LocationType from string '" + type + "'");
        }
    }

}
