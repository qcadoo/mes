package com.qcadoo.mes.core.data.validation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import com.qcadoo.mes.core.data.beans.Entity;
import com.qcadoo.mes.core.data.definition.FieldDefinition;

public final class ValidationResults {

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

    @Override
    public int hashCode() {
        return new HashCodeBuilder(13, 31).append(errors).append(globalErrors).append(entity).toHashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof ValidationResults)) {
            return false;
        }
        ValidationResults other = (ValidationResults) obj;
        return new EqualsBuilder().append(errors, other.errors).append(globalErrors, other.globalErrors)
                .append(entity, other.entity).isEquals();
    }

}
