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
package com.qcadoo.mes.orders.constants.mbr;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Preconditions;
import com.qcadoo.model.api.Entity;

public enum FormulaSuffixEnum implements FormulaSuffix {

    RIGHT_BRACKET("01rightBracket") {

        @Override
        public String getSuffix() {
            return ")";
        }
    };

    private final String value;

    private FormulaSuffixEnum(final String value) {
        this.value = value;
    }

    public static FormulaSuffixEnum of(final Entity formula) {
        Preconditions.checkArgument(formula != null, "Passed entity have not to be null.");
        return parseString(formula.getStringField(FormulaFields.SUFFIX));
    }

    public static FormulaSuffixEnum parseString(final String suffix) {
        for (FormulaSuffixEnum formulaSuffix : values()) {
            if (StringUtils.equalsIgnoreCase(suffix, formulaSuffix.getStringValue())) {
                return formulaSuffix;
            }
        }
        throw new IllegalArgumentException("Couldn't parse FormulaSuffixValue from string '" + suffix + "'");
    }

    public String getStringValue() {
        return this.value;
    }
}
