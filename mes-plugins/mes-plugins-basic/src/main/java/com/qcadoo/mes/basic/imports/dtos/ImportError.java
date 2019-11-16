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
package com.qcadoo.mes.basic.imports.dtos;

import java.util.Objects;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class ImportError {

    private String fieldName;

    private int rowIndex;

    private String code;

    private String[] args;

    public ImportError(final int rowIndex, final String fieldName, final String code, final String... args) {
        this.rowIndex = rowIndex;
        this.fieldName = fieldName;
        this.code = code;
        this.args = Objects.isNull(args) ? new String[0] : args;
    }

    public String getFieldName() {
        return fieldName;
    }

    public int getRowIndex() {
        return rowIndex;
    }

    public String getCode() {
        return code;
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

        return new EqualsBuilder().append(rowIndex, that.rowIndex).append(fieldName, that.fieldName).append(code, that.code)
                .append(args, that.args).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(fieldName).append(rowIndex).append(code).append(args).toHashCode();
    }

}
