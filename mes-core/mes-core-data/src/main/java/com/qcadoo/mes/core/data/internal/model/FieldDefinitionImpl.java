package com.qcadoo.mes.core.data.internal.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import com.qcadoo.mes.core.data.internal.validators.RequiredOnCreateValidator;
import com.qcadoo.mes.core.data.internal.validators.RequiredValidator;
import com.qcadoo.mes.core.data.internal.validators.UniqueValidator;
import com.qcadoo.mes.core.data.model.FieldDefinition;
import com.qcadoo.mes.core.data.types.FieldType;
import com.qcadoo.mes.core.data.validation.FieldValidator;

/**
 * Field defines database field or custom field (according to {@link FieldDefinition#isCustomField()}).
 * 
 * Not editable field can't be changed after entity creation.
 * 
 * Definition of database field can't be modified using RAD.
 * 
 * @apiviz.has com.qcadoo.mes.core.data.definition.FieldType
 * @apiviz.owns com.qcadoo.mes.core.data.definition.FieldValidator
 */
public class FieldDefinitionImpl implements FieldDefinition {

    private final String name;

    private FieldType type;

    private final List<FieldValidator> validators = new ArrayList<FieldValidator>();

    private boolean readOnlyOnUpdate;

    private boolean readOnly;

    private boolean required;

    private boolean requiredOnCreate;

    private boolean customField;

    private boolean unique;

    private Object defaultValue;

    public FieldDefinitionImpl(final String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getValue(final Object value) {
        if (value == null) {
            return null;
        } else {
            return type.toString(value);
        }
    }

    @Override
    public FieldType getType() {
        return type;
    }

    public FieldDefinitionImpl withType(final FieldType type) {
        this.type = type;
        return this;
    }

    @Override
    public List<FieldValidator> getValidators() {
        return validators;
    }

    public FieldDefinitionImpl withValidator(final FieldValidator validator) {
        if (validator instanceof RequiredValidator) {
            required = true;
        }
        if (validator instanceof RequiredOnCreateValidator) {
            requiredOnCreate = true;
        }
        if (validator instanceof UniqueValidator) {
            unique = true;
        }
        this.validators.add(validator);
        return this;
    }

    @Override
    public boolean isReadOnlyOnUpdate() {
        return readOnlyOnUpdate;
    }

    public FieldDefinition readOnlyOnUpdate() {
        this.readOnlyOnUpdate = true;
        return this;
    }

    public FieldDefinitionImpl readOnly() {
        this.readOnly = true;
        return this;
    }

    @Override
    public boolean isReadOnly() {
        return readOnly;
    }

    @Override
    public boolean isRequired() {
        return required;
    }

    @Override
    public boolean isRequiredOnCreate() {
        return requiredOnCreate;
    }

    @Override
    public boolean isCustomField() {
        return customField;
    }

    public void setCustomField(final boolean customField) {
        this.customField = customField;
    }

    @Override
    public Object getDefaultValue() {
        return defaultValue;
    }

    public FieldDefinition withDefaultValue(final Object defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }

    @Override
    public boolean isUnique() {
        return unique;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(13, 31).append(customField).append(defaultValue).append(readOnlyOnUpdate).append(name)
                .append(required).append(type).append(unique).append(validators).append(readOnly).append(requiredOnCreate)
                .toHashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof FieldDefinitionImpl)) {
            return false;
        }
        FieldDefinitionImpl other = (FieldDefinitionImpl) obj;
        return new EqualsBuilder().append(customField, other.customField).append(defaultValue, other.defaultValue)
                .append(readOnlyOnUpdate, other.readOnlyOnUpdate).append(name, other.name).append(required, other.required)
                .append(type, other.type).append(unique, other.unique).append(validators, other.validators)
                .append(readOnly, other.readOnly).append(requiredOnCreate, other.requiredOnCreate).isEquals();
    }

}
