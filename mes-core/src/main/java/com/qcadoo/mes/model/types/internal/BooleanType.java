/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.2.0
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

package com.qcadoo.mes.model.types.internal;

import java.util.Locale;

import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.model.FieldDefinition;
import com.qcadoo.mes.model.types.FieldType;

public final class BooleanType implements FieldType {

    @Override
    public boolean isSearchable() {
        return true;
    }

    @Override
    public boolean isOrderable() {
        return true;
    }

    @Override
    public boolean isAggregable() {
        return false;
    }

    @Override
    public Class<?> getType() {
        return Boolean.class;
    }

    @Override
    public Object toObject(final FieldDefinition fieldDefinition, final Object value, final Entity validatedEntity) {
        if (value instanceof Boolean) {
            return value;
        }
        return parseStringToBoolean(String.valueOf(value));
    }

    private Boolean parseStringToBoolean(final String value) {
        return "1".equals(value) || "true".equals(value) || "yes".equals(value);
    }

    private String parseBooleanToString(final Boolean value) {
        if (value) {
            return "1";
        } else {
            return "0";
        }
    }

    @Override
    public String toString(final Object value, final Locale locale) {
        if (value instanceof Boolean) {
            return parseBooleanToString((Boolean) value);
        } else {
            return parseBooleanToString(parseStringToBoolean(String.valueOf(value)));
        }
    }

    @Override
    public Object fromString(final String value, final Locale locale) {
        return parseStringToBoolean(value);
    }

}
