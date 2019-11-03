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

import java.util.Map;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.google.common.collect.Maps;

public class CellBinderRegistry {

    private Map<Integer, CellBinder> cellBinders = Maps.newHashMap();

    private int index = 0;

    public CellBinder getCellBinder(int columnNumber) {
        return cellBinders.get(columnNumber);
    }

    public void setCellBinder(final CellBinder cellBinder) {
        cellBinders.computeIfAbsent(index++, c -> cellBinder);
    }

    public Integer getSize() {
        return cellBinders.size();
    }

    public Integer getIndexUsingFieldName(final String fieldName) {
        return cellBinders.entrySet().stream().filter(entry -> entry.getValue().getFieldName().equals(fieldName))
                .map(Map.Entry::getKey).findFirst().orElse(null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        CellBinderRegistry that = (CellBinderRegistry) o;

        return new EqualsBuilder().append(cellBinders, that.cellBinders).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(cellBinders).toHashCode();
    }

}
