package com.qcadoo.mes.core.data.validation;

import java.util.HashMap;
import java.util.Map;

import com.qcadoo.mes.core.data.definition.FieldDefinition;

public final class ValidationResults {

    private final Map<FieldDefinition, ValidationError> errors = new HashMap<FieldDefinition, ValidationError>();

    public void addError(final FieldDefinition fieldDefinition, final String message, final String... vars) {
        errors.put(fieldDefinition, new ValidationError(message, vars));
    }

    public Map<FieldDefinition, ValidationError> getErrors() {
        return errors;
    }

    public ValidationError getErrorForField(final FieldDefinition fieldDefinition) {
        return errors.get(fieldDefinition);
    }

    public boolean isValid() {
        return errors.isEmpty();
    }

    public boolean isNotValid() {
        return !errors.isEmpty();
    }

    public boolean isFieldValid(final FieldDefinition fieldDefinition) {
        return errors.get(fieldDefinition) == null;
    }

    public boolean isFieldNotValid(final FieldDefinition fieldDefinition) {
        return errors.get(fieldDefinition) != null;
    }

}
