/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.3.0
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

import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.Locale;

import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.model.FieldDefinition;
import com.qcadoo.mes.model.types.FieldType;

public final class IntegerType implements FieldType {

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
        return true;
    }

    @Override
    public Class<?> getType() {
        return Integer.class;
    }

    @Override
    public Object toObject(final FieldDefinition fieldDefinition, final Object value, final Entity validatedEntity) {
        if (value instanceof Integer) {
            return value;
        }
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException e) {
            validatedEntity.addError(fieldDefinition, "core.validate.field.error.invalidNumericFormat");
            return null;
        }
    }

    @Override
    public String toString(final Object value, final Locale locale) {
        return NumberFormat.getIntegerInstance(locale).format(value);
    }

    @Override
    public Object fromString(final String value, final Locale locale) {
        ParsePosition parsePosition = new ParsePosition(0);
        String trimedValue = value.replace(" ", "");
        Object parsedValue = NumberFormat.getIntegerInstance(locale).parse(trimedValue, parsePosition);
        if (parsePosition.getIndex() != trimedValue.length()) {
            return value;
        } else {
            return parsedValue;
        }
    }

}
