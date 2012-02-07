/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.2
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
package com.qcadoo.mes.productionCounting.internal.states;

public enum ProductionCountingStates {
    DRAFT("01draft"), ACCEPTED("02accepted"), DECLINED("03declined");

    private final String state;

    private ProductionCountingStates(final String state) {
        this.state = state;
    }

    public String getStringValue() {
        return state;
    }

    public static ProductionCountingStates parseString(final String stringValue) {
        if ("01draft".equals(stringValue)) {
            return DRAFT;
        } else if ("02accepted".equals(stringValue)) {
            return ACCEPTED;
        } else if ("03declined".equals(stringValue)) {
            return DECLINED;
        } else {
            throw new IllegalArgumentException("Unsupported or unspecified batch state " + stringValue);
        }
    }
}
