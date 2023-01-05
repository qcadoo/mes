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
package com.qcadoo.mes.technologies.constants;

public enum WorkstationChangeoverNormChangeoverType {

    ANY_CHANGE("01anyChange"), BETWEEN_VALUES("02betweenValues");

    private final String changeoverType;

    private WorkstationChangeoverNormChangeoverType(final String changeoverType) {
        this.changeoverType = changeoverType;
    }

    public String getStringValue() {
        return changeoverType;
    }

    public static WorkstationChangeoverNormChangeoverType parseString(final String changeoverType) {
        if ("01anyChange".equals(changeoverType)) {
            return ANY_CHANGE;
        } else if ("02betweenValues".equals(changeoverType)) {
            return BETWEEN_VALUES;
        }

        throw new IllegalStateException("Unsupported WorkstationChangeoverNormChangeoverType: " + changeoverType);
    }

}
