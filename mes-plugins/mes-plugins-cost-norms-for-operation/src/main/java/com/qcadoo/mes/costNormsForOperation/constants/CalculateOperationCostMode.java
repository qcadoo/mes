/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.3
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
package com.qcadoo.mes.costNormsForOperation.constants;

public enum CalculateOperationCostMode {
    HOURLY("01hourly"), PIECEWORK("02piecework");

    private String calculateOperationCostMode;

    private CalculateOperationCostMode(final String calculateOperationCostMode) {
        this.calculateOperationCostMode = calculateOperationCostMode;
    }

    public String getStringValue() {
        return calculateOperationCostMode;
    }

    public static CalculateOperationCostMode parseString(final String string) {
        if ("01hourly".equals(string)) {
            return HOURLY;
        } else if ("02piecework".equals(string)) {
            return PIECEWORK;
        }

        throw new IllegalStateException("Unsupported calculateOperationCostMode: " + string);
    }

}
