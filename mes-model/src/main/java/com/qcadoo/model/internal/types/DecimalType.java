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

package com.qcadoo.model.internal.types;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.Locale;

import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.FieldDefinition;
import com.qcadoo.model.api.types.FieldType;

public final class DecimalType implements FieldType {

    @Override
    public Class<?> getType() {
        return BigDecimal.class;
    }

    @Override
    public Object toObject(final FieldDefinition fieldDefinition, final Object value, final Entity validatedEntity) {
        BigDecimal decimal = null;

        if (value instanceof BigDecimal) {
            decimal = (BigDecimal) value;
        } else {
            try {
                decimal = new BigDecimal(String.valueOf(value));
            } catch (NumberFormatException e) {
                validatedEntity.addError(fieldDefinition, "core.validate.field.error.invalidNumericFormat");
                return null;
            }
        }
        return decimal;
    }

    @Override
    public String toString(final Object value, final Locale locale) {
        NumberFormat format = null;
        if (locale != null) {
            format = NumberFormat.getNumberInstance(locale);
        } else {
            format = NumberFormat.getNumberInstance();
        }
        format.setMinimumFractionDigits(3);
        format.setMaximumFractionDigits(3);
        return format.format(value);
    }

    @Override
    public Object fromString(final String value, final Locale locale) {
        ParsePosition parsePosition = new ParsePosition(0);
        String trimedValue = value.replace(" ", "");
        Object parsedValue = NumberFormat.getNumberInstance(locale).parse(trimedValue, parsePosition);
        if (parsePosition.getIndex() != trimedValue.length()) {
            return value;
        } else {
            return parsedValue;
        }
    }

}
