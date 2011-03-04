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

package com.qcadoo.model.internal.validators;

import java.math.BigDecimal;
import java.util.Date;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.FieldDefinition;
import com.qcadoo.model.internal.api.ErrorMessageDefinition;
import com.qcadoo.model.internal.api.FieldHookDefinition;

public final class RangeValidator implements FieldHookDefinition, ErrorMessageDefinition {

    private static final String OUT_OF_RANGE_ERROR_SMALL = "core.validate.field.error.outOfRange.toSmall";

    private static final String OUT_OF_RANGE_ERROR_LARGE = "core.validate.field.error.outOfRange.toLarge";

    private final String errorMessageSmall = OUT_OF_RANGE_ERROR_SMALL;

    private final String errorMessageLarge = OUT_OF_RANGE_ERROR_LARGE;

    private String customErrorMessage;

    private final Object from;

    private final Object to;

    private final boolean inclusively;

    private FieldDefinition fieldDefinition;

    public RangeValidator(final Object from, final Object to, final boolean exclusively) {
        this.from = from;
        this.to = to;
        this.inclusively = !exclusively;
    }

    @Override
    public void initialize(final DataDefinition dataDefinition, final FieldDefinition fieldDefinition) {
        this.fieldDefinition = fieldDefinition;
    }

    @Override
    public boolean call(final Entity entity, final Object oldValue, final Object newValue) {
        if (newValue == null) {
            return true;
        }

        Class<?> fieldClass = fieldDefinition.getType().getType();

        if (fieldClass.equals(String.class)) {
            return validateStringRange(fieldDefinition, (String) newValue, entity);
        } else if (fieldClass.equals(Integer.class) || fieldClass.equals(BigDecimal.class)) {
            return validateNumberRange(fieldDefinition, (Number) newValue, entity);
        } else if (fieldClass.equals(Date.class)) {
            return validateDateRange(fieldDefinition, (Date) newValue, entity);
        }

        return true;
    }

    private boolean validateDateRange(final FieldDefinition fieldDefinition, final Date value, final Entity validatedEntity) {
        if (from != null && ((!inclusively && !value.after((Date) from)) || (inclusively && value.before((Date) from)))) {
            addToSmallError(fieldDefinition, validatedEntity);
            return false;
        }
        if (to != null && ((!inclusively && !value.before((Date) to)) || (inclusively && value.after((Date) to)))) {
            addToLargeError(fieldDefinition, validatedEntity);
            return false;
        }
        return true;
    }

    private boolean validateNumberRange(final FieldDefinition fieldDefinition, final Number value, final Entity validatedEntity) {
        if (from != null
                && ((!inclusively && value.doubleValue() <= ((Number) from).doubleValue()) || (inclusively && value.doubleValue() < ((Number) from)
                        .doubleValue()))) {
            addToSmallError(fieldDefinition, validatedEntity);
            return false;
        }
        if (to != null
                && ((!inclusively && value.doubleValue() >= ((Number) to).doubleValue()) || (inclusively && value.doubleValue() > ((Number) to)
                        .doubleValue()))) {
            addToLargeError(fieldDefinition, validatedEntity);
            return false;
        }
        return true;
    }

    private boolean validateStringRange(final FieldDefinition fieldDefinition, final String value, final Entity validatedEntity) {
        if (from != null
                && ((!inclusively && value.compareTo((String) from) < 0) || (inclusively && value.compareTo((String) from) <= 0))) {
            addToSmallError(fieldDefinition, validatedEntity);
            return false;
        }
        if (to != null && ((!inclusively && value.compareTo((String) to) > 0) || (inclusively && value.compareTo((String) to) >= 0))) {
            addToLargeError(fieldDefinition, validatedEntity);
            return false;
        }
        return true;
    }

    private void addToSmallError(final FieldDefinition fieldDefinition, final Entity validatedEntity) {
        if (customErrorMessage != null) {
            validatedEntity.addError(fieldDefinition, customErrorMessage, String.valueOf(from), String.valueOf(to));
        } else {
            validatedEntity.addError(fieldDefinition, errorMessageSmall, String.valueOf(from), String.valueOf(to));
        }
    }

    private void addToLargeError(final FieldDefinition fieldDefinition, final Entity validatedEntity) {
        if (customErrorMessage != null) {
            validatedEntity.addError(fieldDefinition, customErrorMessage, String.valueOf(from), String.valueOf(to));
        } else {
            validatedEntity.addError(fieldDefinition, errorMessageLarge, String.valueOf(from), String.valueOf(to));
        }
    }

    @Override
    public void setErrorMessage(final String errorMessage) {
        this.customErrorMessage = errorMessage;
    }
}
