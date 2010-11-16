/**
 * ********************************************************************
 * Code developed by amazing QCADOO developers team.
 * Copyright © Qcadoo Limited sp. z o.o. (2010)
 * ********************************************************************
 */

package com.qcadoo.mes.internal;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.model.DataDefinition;
import com.qcadoo.mes.model.FieldDefinition;
import com.qcadoo.mes.model.validators.ErrorMessage;

public final class ProxyEntity implements Entity {

    private final DataDefinition dataDefinition;

    private final Long id;

    private Entity entity = null;

    public ProxyEntity(final DataDefinition dataDefinition, final Long id) {
        this.dataDefinition = dataDefinition;
        this.id = id;
    }

    private void loadEntity() {
        if (entity == null) {
            entity = dataDefinition.get(id);
            checkNotNull(entity, "Proxy can't load entity");
        }
    }

    @Override
    public void setId(final Long id) {
        if (entity == null) {
            loadEntity();
        }
        entity.setId(id);
    }

    @Override
    public Long getId() {
        if (entity == null) {
            return id;
        } else {
            return entity.getId();
        }
    }

    @Override
    public Object getField(final String fieldName) {
        if (entity == null) {
            loadEntity();
        }
        return entity.getField(fieldName);
    }

    @Override
    public void setField(final String fieldName, final Object fieldValue) {
        if (entity == null) {
            loadEntity();
        }
        entity.setField(fieldName, fieldValue);
    }

    @Override
    public Map<String, Object> getFields() {
        if (entity == null) {
            loadEntity();
        }
        return entity.getFields();
    }

    @Override
    public void addGlobalError(final String message, final String... vars) {
        if (entity == null) {
            loadEntity();
        }
        entity.addGlobalError(message, vars);
    }

    @Override
    public void addError(final FieldDefinition fieldDefinition, final String message, final String... vars) {
        if (entity == null) {
            loadEntity();
        }
        entity.addError(fieldDefinition, message, vars);
    }

    @Override
    public List<ErrorMessage> getGlobalErrors() {
        if (entity == null) {
            loadEntity();
        }
        return entity.getGlobalErrors();
    }

    @Override
    public Map<String, ErrorMessage> getErrors() {
        if (entity == null) {
            loadEntity();
        }
        return entity.getErrors();
    }

    @Override
    public ErrorMessage getError(final String fieldName) {
        if (entity == null) {
            loadEntity();
        }
        return entity.getError(fieldName);
    }

    @Override
    public boolean isValid() {
        if (entity == null) {
            loadEntity();
        }
        return entity.isValid();
    }

    @Override
    public boolean isFieldValid(final String fieldName) {
        if (entity == null) {
            loadEntity();
        }
        return entity.isFieldValid(fieldName);
    }

    @Override
    public Entity copy() {
        if (entity == null) {
            loadEntity();
        }
        return entity.copy();
    }

    @Override
    public String getStringField(final String fieldName) {
        if (entity == null) {
            loadEntity();
        }
        return entity.getStringField(fieldName);
    }

    @Override
    public Entity getBelongsToField(final String fieldName) {
        if (entity == null) {
            loadEntity();
        }
        return entity.getBelongsToField(fieldName);
    }

    @Override
    public List<Entity> getHasManyField(final String fieldName) {
        if (entity == null) {
            loadEntity();
        }
        return entity.getHasManyField(fieldName);
    }

    @Override
    public String getName() {
        return dataDefinition.getName();
    }

    @Override
    public String getPluginIdentifier() {
        return dataDefinition.getPluginIdentifier();
    }

    @Override
    public String toString() {
        return "EntityProxy[" + dataDefinition.getPluginIdentifier() + "." + dataDefinition.getName() + "][id=" + id + "]";
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(23, 41).append(id).toHashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof ProxyEntity)) {
            return false;
        }
        ProxyEntity other = (ProxyEntity) obj;
        return new EqualsBuilder().append(id, other.id).isEquals();
    }
}
