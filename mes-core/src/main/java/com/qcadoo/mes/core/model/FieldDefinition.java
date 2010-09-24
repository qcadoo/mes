package com.qcadoo.mes.core.model;

import java.util.List;

import com.qcadoo.mes.core.types.FieldType;
import com.qcadoo.mes.core.validation.FieldValidator;

public interface FieldDefinition {

    String getName();

    String getValue(final Object value);

    FieldType getType();

    List<FieldValidator> getValidators();

    boolean isReadOnlyOnUpdate();

    boolean isReadOnly();

    boolean isRequired();

    boolean isRequiredOnCreate();

    boolean isCustomField();

    Object getDefaultValue();

    boolean isUnique();

}
