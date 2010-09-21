package com.qcadoo.mes.core.data.model;

import java.util.List;

import com.qcadoo.mes.core.data.types.FieldType;
import com.qcadoo.mes.core.data.validation.FieldValidator;

public interface FieldDefinition {

    public abstract String getName();

    public abstract String getValue(final Object value);

    public abstract FieldType getType();

    public abstract List<FieldValidator> getValidators();

    public abstract boolean isReadOnlyOnUpdate();

    public abstract boolean isReadOnly();

    public abstract boolean isRequired();

    public abstract boolean isRequiredOnCreate();

    public abstract boolean isCustomField();

    public abstract Object getDefaultValue();

    public abstract boolean isUnique();

}