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

public enum FormulaTypeEnum implements FormulaType {

    ADDITION("01addition") {

        @Override
        public String getOperator() {
            return "+";
        }
    },
    SUBTRACTION("02subtraction") {

        @Override
        public String getOperator() {
            return "-";
        }
    },
    DIVISION("03division") {

        @Override
        public String getOperator() {
            return "/";
        }
    },
    MULTIPLICATION("04multiplication") {

        @Override
        public String getOperator() {
            return "*";
        }
    };

    private final String value;

    private FormulaTypeEnum(final String value) {
        this.value = value;
    }

    public String getStringValue() {
        return this.value;
    }

    public static FormulaTypeEnum parseString(final String type) {
        for (FormulaTypeEnum documentType : values()) {
            if (StringUtils.equalsIgnoreCase(type, documentType.getStringValue())) {
                return documentType;
            }
        }
        throw new IllegalArgumentException("Couldn't parse FormulaType from string '" + type + "'");
    }

}
