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

    private boolean unique;

    private Object defaultValue;

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

}
