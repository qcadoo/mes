/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.2.0
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
package com.qcadoo.mes.productionCounting.constants;

public enum TypeOfProductionRecording {
    BASIC("01basic"), CUMULATED("02cumulated"), FOR_EACH("03forEach");

    private final String typeOfProductionRecording;

    private TypeOfProductionRecording(final String typeOfProductionRecording) {
        this.typeOfProductionRecording = typeOfProductionRecording;
    }

    public String getStringValue() {
        return typeOfProductionRecording;
    }

    public static TypeOfProductionRecording parseString(final String string) {
        if ("01basic".equals(string)) {
            return BASIC;
        } else if ("02cumulated".equals(string)) {
            return CUMULATED;
        } else if ("03forEach".equals(string)) {
            return FOR_EACH;
        }

        throw new IllegalStateException("Unsupported typeOfProductionRecording: " + string);
    }

}
