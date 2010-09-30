package com.qcadoo.mes.core.internal;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;
import java.util.Map;

import com.qcadoo.mes.core.api.Entity;
import com.qcadoo.mes.model.DataDefinition;
import com.qcadoo.mes.model.FieldDefinition;
import com.qcadoo.mes.model.validation.ErrorMessage;

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

}
