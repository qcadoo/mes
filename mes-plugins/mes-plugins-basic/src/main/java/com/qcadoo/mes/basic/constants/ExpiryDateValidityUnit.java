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
package com.qcadoo.mes.basic.constants;

public enum ExpiryDateValidityUnit {
    MONTHS("01months"), DAYS("02days");

    private final String expiryDateValidityUnit;

    private ExpiryDateValidityUnit(final String expiryDateValidityUnit) {
        this.expiryDateValidityUnit = expiryDateValidityUnit;
    }

    public String getStringValue() {
        return expiryDateValidityUnit;
    }

    public static ExpiryDateValidityUnit parseString(final String expiryDateValidityUnit) {
        if ("01months".equals(expiryDateValidityUnit)) {
            return MONTHS;
        } else if ("02days".equals(expiryDateValidityUnit)) {
            return DAYS;
        }

        throw new IllegalStateException("Unsupported ExpiryDateValidityUnit: " + expiryDateValidityUnit);
    }

}
