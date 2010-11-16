/**
 * ********************************************************************
 * Code developed by amazing QCADOO developers team.
 * Copyright (c) Qcadoo Limited sp. z o.o. (2010)
 * ********************************************************************
 */

package com.qcadoo.mes.model.validators.internal;

import java.math.BigDecimal;

import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.model.DataDefinition;
import com.qcadoo.mes.model.FieldDefinition;
import com.qcadoo.mes.model.validators.FieldValidator;

public final class PrecisionValidator implements FieldValidator {

    private static final String INVALID_PRECISION_ERROR = "core.validate.field.error.invalidPrecision";

    private final Integer max;

    private final Integer min;

    private final Integer is;

    private String errorMessage = INVALID_PRECISION_ERROR;

    public PrecisionValidator(final Integer min, final Integer is, final Integer max) {
        this.min = min;
        this.is = is;
        this.max = max;
    }

    @Override
    public boolean validate(final DataDefinition dataDefinition, final FieldDefinition fieldDefinition, final Object value,
            final Entity validatedEntity) {
        if (value == null) {
            return true;
        }

        Class<?> fieldClass = fieldDefinition.getType().getType();

        if (!(fieldClass.equals(Integer.class) || fieldClass.equals(BigDecimal.class))) {
            return true;
        }

        if (fieldClass.equals(BigDecimal.class)) {
            return validatePresicion(fieldDefinition, validatedEntity,
                    ((BigDecimal) value).precision() - ((BigDecimal) value).scale());
        } else {
            return validatePresicion(fieldDefinition, validatedEntity, value.toString().length());
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
    public boolean validate(final DataDefinition dataDefinition, final FieldDefinition fieldDefinition, final Entity entity) {
        return true;
    }

    @Override
    public FieldValidator customErrorMessage(final String errorMessage) {
        this.errorMessage = errorMessage;
        return this;
    }

}
