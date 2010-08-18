package com.qcadoo.mes.core.data.definition;

import java.util.Set;

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
public final class FieldDefinition {

    private String name;

    private FieldType type;

    private Set<FieldValidator> validators;

    private boolean editable;

    private boolean required;

    private boolean customField;

    private boolean hidden;

    private Object defaultValue;

    private boolean unique;

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public FieldType getType() {
        return type;
    }

    public void setType(final FieldType type) {
        this.type = type;
    }

    public Set<FieldValidator> getValidators() {
        return validators;
    }

    public void setValidators(final Set<FieldValidator> validators) {
        this.validators = validators;
    }

    public boolean isEditable() {
        return editable;
    }

    public void setEditable(final boolean editable) {
        this.editable = editable;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(final boolean required) {
        this.required = required;
    }

    public boolean isCustomField() {
        return customField;
    }

    public void setCustomField(final boolean customField) {
        this.customField = customField;
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(final boolean hidden) {
        this.hidden = hidden;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(final Object defaultValue) {
        this.defaultValue = defaultValue;
    }

    public boolean isUnique() {
        return unique;
    }

    public void setUnique(final boolean unique) {
        this.unique = unique;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (customField ? 1231 : 1237);
        result = prime * result + ((defaultValue == null) ? 0 : defaultValue.hashCode());
        result = prime * result + (editable ? 1231 : 1237);
        result = prime * result + (hidden ? 1231 : 1237);
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + (required ? 1231 : 1237);
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        result = prime * result + (unique ? 1231 : 1237);
        result = prime * result + ((validators == null) ? 0 : validators.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        FieldDefinition other = (FieldDefinition) obj;
        if (customField != other.customField)
            return false;
        if (defaultValue == null) {
            if (other.defaultValue != null)
                return false;
        } else if (!defaultValue.equals(other.defaultValue))
            return false;
        if (editable != other.editable)
            return false;
        if (hidden != other.hidden)
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (required != other.required)
            return false;
        if (type == null) {
            if (other.type != null)
                return false;
        } else if (!type.equals(other.type))
            return false;
        if (unique != other.unique)
            return false;
        if (validators == null) {
            if (other.validators != null)
                return false;
        } else if (!validators.equals(other.validators))
            return false;
        return true;
    }

}
