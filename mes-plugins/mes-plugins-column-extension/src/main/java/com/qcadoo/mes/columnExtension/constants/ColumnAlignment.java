/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.2.0-SNAPSHOT
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
package com.qcadoo.mes.columnExtension.constants;

public enum ColumnAlignment {
    LEFT("01left"), RIGHT("02right");

    private String alignment;

    private ColumnAlignment(final String alignment) {
        this.alignment = alignment;
    }

    public String getStringValue() {
        return alignment;
    }

    public static ColumnAlignment parseString(final String alignment) {
        if ("01left".equals(alignment)) {
            return LEFT;
        } else if ("02right".equals(alignment)) {
            return RIGHT;
        }

        throw new IllegalStateException("Unsupported column alignment '" + alignment + "'");
    }

}
