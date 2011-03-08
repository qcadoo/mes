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

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.FieldDefinition;
import com.qcadoo.model.internal.api.ErrorMessageDefinition;
import com.qcadoo.model.internal.api.FieldHookDefinition;

public final class PrecisionValidator implements FieldHookDefinition, ErrorMessageDefinition {

    private static final String INVALID_PRECISION_ERROR = "core.validate.field.error.invalidPrecision";

    private final Integer max;

    private final Integer min;

    private final Integer is;

    private String errorMessage = INVALID_PRECISION_ERROR;

    private FieldDefinition fieldDefinition;

    public PrecisionValidator(final Integer min, final Integer is, final Integer max) {
        this.min = min;
        this.is = is;
        this.max = max;
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

        if (!(fieldClass.equals(Integer.class) || fieldClass.equals(BigDecimal.class))) {
            return true;
        }
        if (fieldClass.equals(BigDecimal.class)) {
            return validatePresicion(fieldDefinition, entity,
                    ((BigDecimal) newValue).precision() - ((BigDecimal) newValue).scale());
        } else {
            return validatePresicion(fieldDefinition, entity, newValue.toString().length());
        }
    }

    private boolean validatePresicion(final FieldDefinition fieldDefinition, final Entity validatedEntity, final int presicion) {
        if (max != null && presicion > max) {
            validatedEntity.addError(fieldDefinition, errorMessage);
            return false;
        }
        if (min != null && presicion < min) {
            validatedEntity.addError(fieldDefinition, errorMessage);
            return false;
        }
        if (is != null && !is.equals(presicion)) {
            validatedEntity.addError(fieldDefinition, errorMessage);
            return false;
        }

        return true;
    }

    @Override
    public void setErrorMessage(final String errorMessage) {
        this.errorMessage = errorMessage;
    }

}
