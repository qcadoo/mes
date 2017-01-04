/*
 * **************************************************************************
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
 * **************************************************************************
 */
package com.qcadoo.mes.basic.product.importing;

import java.util.Objects;

public class ImportError {
    private final String fieldName;
    private final int rowIndex;
    private final String code;
    private final String[] args;

    ImportError(int rowIndex, String fieldName, String code, String... args) {
        this.rowIndex = rowIndex;
        this.fieldName = fieldName;
        this.code = code;
        this.args = null == args ? new String[0] : args;
    }

    public String[] getArgs() {
        return args;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ImportError that = (ImportError) o;
        return rowIndex == that.rowIndex &&
                Objects.equals(fieldName, that.fieldName) &&
                Objects.equals(code, that.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fieldName, rowIndex, code);
    }

    public int getRowIndex() {
        return rowIndex;
    }

    public String getCode() {
        return code;
    }

    public String getFieldName() {
        return fieldName;
    }
}
