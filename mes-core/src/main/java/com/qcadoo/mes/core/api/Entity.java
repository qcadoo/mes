package com.qcadoo.mes.core.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import com.qcadoo.mes.core.model.FieldDefinition;
import com.qcadoo.mes.core.validation.ErrorMessage;

/**
 * Object represents data from the database tables - with and without custom fields - and virtual tables - build using only custom
 * fields. All fields - database's fields and custom fields - are aggregated into key-value map. The key is the name of the field
 * from its definition - {@link com.qcadoo.mes.core.data.definition.FieldDefinition#getName()}.
 * 
 * Value type must be the same as the type defined in {@link com.qcadoo.mes.core.data.definition.FieldDefinition#getType()}.
 */
public final class Entity {

    private Long id;

    private final Map<String, Object> fields;

    private final List<ErrorMessage> globalErrors = new ArrayList<ErrorMessage>();

    private final Map<String, ErrorMessage> errors = new HashMap<String, ErrorMessage>();

    public Entity(final Long id, final Map<String, Object> fields) {
        this.id = id;
        this.fields = fields;
    }

    public Entity(final Long id) {
        this(id, new HashMap<String, Object>());
    }

    public Entity() {
        this(null, new HashMap<String, Object>());
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public Object getField(final String fieldName) {
        return fields.get(fieldName);
    }

    public void setField(final String fieldName, final Object fieldValue) {
        fields.put(fieldName, fieldValue);
    }

    public Map<String, Object> getFields() {
        return fields;
    }

    public void addGlobalError(final String message, final String... vars) {
        globalErrors.add(new ErrorMessage(message, vars));
    }

    public void addError(final FieldDefinition fieldDefinition, final String message, final String... vars) {
        errors.put(fieldDefinition.getName(), new ErrorMessage(message, vars));
    }

    public List<ErrorMessage> getGlobalErrors() {
        return globalErrors;
    }

    public Map<String, ErrorMessage> getErrors() {
        return errors;
    }

    public ErrorMessage getError(final String fieldName) {
        return errors.get(fieldName);
    }

    public boolean isValid() {
        return errors.isEmpty() && globalErrors.isEmpty();
    }

    public boolean isFieldValid(final String fieldName) {
        return errors.get(fieldName) == null;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(23, 41).append(id).append(fields).append(globalErrors).append(errors).toHashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof Entity)) {
            return false;
        }
        Entity other = (Entity) obj;
        return new EqualsBuilder().append(id, other.id).append(fields, other.fields).append(fields, other.fields)
                .append(globalErrors, this.globalErrors).isEquals();
    }

    public Entity copy() {
        Entity entity = new Entity(id);
        for (Map.Entry<String, Object> field : fields.entrySet()) {
            entity.setField(field.getKey(), field.getValue());
        }
        return entity;
    }

    @Override
    public String toString() {
        return "Entity #" + id;
    }

}
