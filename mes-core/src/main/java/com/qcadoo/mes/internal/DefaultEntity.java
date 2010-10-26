package com.qcadoo.mes.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.model.FieldDefinition;
import com.qcadoo.mes.model.validators.ErrorMessage;

public final class DefaultEntity implements Entity {

    private Long id;

    private final String pluginIdentifier;

    private final String name;

    private final Map<String, Object> fields;

    private final List<ErrorMessage> globalErrors = new ArrayList<ErrorMessage>();

    private final Map<String, ErrorMessage> errors = new HashMap<String, ErrorMessage>();

    public DefaultEntity(final String pluginIdentifier, final String name, final Long id, final Map<String, Object> fields) {
        this.pluginIdentifier = pluginIdentifier;
        this.name = name;
        this.id = id;
        this.fields = fields;
    }

    public DefaultEntity(final String pluginIdentifier, final String name, final Long id) {
        this(pluginIdentifier, name, id, new HashMap<String, Object>());
    }

    public DefaultEntity(final String pluginIdentifier, final String name) {
        this(pluginIdentifier, name, null, new HashMap<String, Object>());
    }

    @Override
    public void setId(final Long id) {
        this.id = id;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setField(final String fieldName, final Object fieldValue) {
        fields.put(fieldName, fieldValue);
    }

    @Override
    public Map<String, Object> getFields() {
        return fields;
    }

    @Override
    public void addGlobalError(final String message, final String... vars) {
        globalErrors.add(new ErrorMessage(message, vars));
    }

    @Override
    public void addError(final FieldDefinition fieldDefinition, final String message, final String... vars) {
        errors.put(fieldDefinition.getName(), new ErrorMessage(message, vars));
    }

    @Override
    public List<ErrorMessage> getGlobalErrors() {
        return globalErrors;
    }

    @Override
    public Map<String, ErrorMessage> getErrors() {
        return errors;
    }

    @Override
    public ErrorMessage getError(final String fieldName) {
        return errors.get(fieldName);
    }

    @Override
    public boolean isValid() {
        return errors.isEmpty() && globalErrors.isEmpty();
    }

    @Override
    public boolean isFieldValid(final String fieldName) {
        return errors.get(fieldName) == null;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(23, 41).append(id).append(name).append(pluginIdentifier).append(fields).append(globalErrors)
                .append(errors).toHashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof DefaultEntity)) {
            return false;
        }
        DefaultEntity other = (DefaultEntity) obj;
        return new EqualsBuilder().append(id, other.id).append(name, other.name).append(pluginIdentifier, other.pluginIdentifier)
                .append(fields, other.fields).append(globalErrors, other.globalErrors).append(errors, this.errors).isEquals();
    }

    @Override
    public DefaultEntity copy() {
        DefaultEntity entity = new DefaultEntity(pluginIdentifier, name, id);
        for (Map.Entry<String, Object> field : fields.entrySet()) {
            if (field.getValue() instanceof Entity) {
                entity.setField(field.getKey(), ((Entity) field.getValue()).copy());
            } else {
                entity.setField(field.getKey(), field.getValue());
            }
        }
        return entity;
    }

    @Override
    public Object getField(final String fieldName) {
        return fields.get(fieldName);
    }

    @Override
    public String getStringField(final String fieldName) {
        return (String) getField(fieldName);
    }

    @Override
    public EntityList getHasManyField(final String fieldName) {
        return (EntityList) getField(fieldName);
    }

    @Override
    public Entity getBelongsToField(final String fieldName) {
        return (Entity) getField(fieldName);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getPluginIdentifier() {
        return pluginIdentifier;
    }

    @Override
    public String toString() {
        return "Entity[" + pluginIdentifier + "." + name + "][id=" + id + ", " + getFields() + "]";
    }

}
