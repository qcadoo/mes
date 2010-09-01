package com.qcadoo.mes.core.data.validation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.qcadoo.mes.core.data.beans.Entity;
import com.qcadoo.mes.core.data.definition.FieldDefinition;

public final class ValidationResults {

    private static final ValidationError GLOBAL_ERROR = new ValidationError("core.validation.error.global");

    private final Map<String, ValidationError> errors = new HashMap<String, ValidationError>();

    private final List<ValidationError> globalErrors = new ArrayList<ValidationError>();

    private Entity entity;

    public void addGlobalError(final String message, final String... vars) {
        globalErrors.add(new ValidationError(message, vars));
    }

    public void addError(final FieldDefinition fieldDefinition, final String message, final String... vars) {
        errors.put(fieldDefinition.getName(), new ValidationError(message, vars));
    }

    public Map<String, ValidationError> getErrors() {
        return errors;
    }

    public List<ValidationError> getGlobalErrors() {
        if (globalErrors.isEmpty() && errors.isEmpty()) {
            return Collections.emptyList();
        }
        List<ValidationError> errorMessages = new ArrayList<ValidationError>();
        errorMessages.add(GLOBAL_ERROR);
        errorMessages.addAll(globalErrors);
        return errorMessages;
    }

    public ValidationError getErrorForField(final String fieldName) {
        return errors.get(fieldName);
    }

    public boolean isValid() {
        return errors.isEmpty() && globalErrors.isEmpty();
    }

    public boolean isNotValid() {
        return !isValid();
    }

    public boolean isFieldValid(final FieldDefinition fieldDefinition) {
        return errors.get(fieldDefinition.getName()) == null;
    }

    public boolean isFieldNotValid(final FieldDefinition fieldDefinition) {
        return !isFieldValid(fieldDefinition);
    }

    public void setEntity(final Entity entity) {
        this.entity = entity;
    }

    public Entity getEntity() {
        return entity;
    }

}
