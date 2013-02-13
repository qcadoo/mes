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
package com.qcadoo.mes.basic.constants;

public enum CharType {
    SMALL_CHARS("01smallChars"), LARGE_CHARS("02largeChars");

    private final String charType;

    private CharType(final String type) {
        this.charType = type;
    }

    public String getStringValue() {
        return charType;
    }

    public static CharType parseString(final String charType) {
        if ("01smallChars".equals(charType)) {
            return SMALL_CHARS;
        } else if ("02largeChars".equals(charType)) {
            return LARGE_CHARS;
        }

        throw new IllegalStateException("Unsupported CharType: " + charType);
    }

}
