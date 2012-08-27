/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.7
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
package com.qcadoo.mes.assignmentToShift.constants;

public enum OccupationType {

    WORK_ON_LINE("01workOnLine"), OTHER_CASE("02otherCase");

    private final String type;

    private OccupationType(final String type) {
        this.type = type;
    }

    public String getStringValue() {
        return type;
    }

    public static OccupationType parseString(final String string) {
        if ("01workOnLine".equals(string)) {
            return WORK_ON_LINE;
        } else if ("02otherCase".equals(string)) {
            return OTHER_CASE;
        }

        throw new IllegalStateException("Unsupported OccupationTypeEnumStringValue: " + string);
    }

}
