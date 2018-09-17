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
package com.qcadoo.mes.orderSupplies.constants;

public enum CoverageProductState {
    COVERED("01covered"), DELAY("02delay"), LACK("03lack");

    private final String coverageProductState;

    private CoverageProductState(final String coverageProductState) {
        this.coverageProductState = coverageProductState;
    }

    public String getStringValue() {
        return coverageProductState;
    }

    public static CoverageProductState parseString(final String string) {
        if ("01covered".equals(string)) {
            return COVERED;
        } else if ("02delay".equals(string)) {
            return DELAY;
        } else if ("03lack".equals(string)) {
            return LACK;
        }

        throw new IllegalStateException("Unsupported coverageProductState: " + string);
    }

}
