package com.qcadoo.mes.core.data.validation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.qcadoo.mes.core.data.definition.FieldDefinition;

public final class ValidationResults {

    private final Map<FieldDefinition, List<ValidationError>> errors = new HashMap<FieldDefinition, List<ValidationError>>();

    public void addError(final FieldDefinition fieldDefinition, final String message, final String... vars) {
        ValidationError error = new ValidationError(message, vars);
        if (!errors.containsKey(fieldDefinition)) {
            errors.put(fieldDefinition, Lists.<ValidationError> newArrayList());
        }
        errors.get(fieldDefinition).add(error);
    }

    public Map<FieldDefinition, List<ValidationError>> getErrors() {
        return errors;
    }

    public List<ValidationError> getErrorsForField(final FieldDefinition fieldDefinition) {
        List<ValidationError> errorsForField = errors.get(fieldDefinition);
        if (errorsForField != null) {
            return errorsForField;
        } else {
            return Lists.newArrayList();
        }

    }

    public boolean hasErrorForField(final FieldDefinition fieldDefinition) {
        return errors.get(fieldDefinition) != null && !errors.get(fieldDefinition).isEmpty();
    }

    public boolean hasError() {
        return !errors.isEmpty();
    }

    public boolean isValid() {
        return errors.isEmpty();
    }

}
