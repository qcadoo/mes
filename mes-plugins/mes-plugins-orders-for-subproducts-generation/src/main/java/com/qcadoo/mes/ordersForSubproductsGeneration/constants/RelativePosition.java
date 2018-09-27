/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo Framework
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
package com.qcadoo.mes.ordersForSubproductsGeneration.constants;

public enum RelativePosition {
    BEFORE("01before"), EQUAL("02equal"), AFTER("03after");

    private final String relativePosition;

    private RelativePosition(final String relativePosition) {
        this.relativePosition = relativePosition;
    }

    public String getStringValue() {
        return relativePosition;
    }

    public static RelativePosition parseString(final String string) {
        if ("01before".equals(string)) {
            return BEFORE;
        } else if ("02equal".equals(string)) {
            return EQUAL;
        } else if ("03after".equals(string)) {
            return AFTER;
        }

        throw new IllegalStateException("Unsupported relativePosition: " + string);
    }

    public static RelativePosition getComparedPosition(final int compareResult) {
        if (compareResult < 0) {
            return BEFORE;
        } else if (compareResult == 0) {
            return EQUAL;
        } else {
            return AFTER;
        }
    }

}
