package com.qcadoo.mes.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.model.FieldDefinition;
import com.qcadoo.mes.model.validators.ErrorMessage;

public class EntityTreeNode implements Entity {

    private final List<EntityTreeNode> children = new ArrayList<EntityTreeNode>();

    private final Entity entity;

    public EntityTreeNode(final Entity entity) {
        this.entity = entity;
    }

    public List<EntityTreeNode> getChildren() {
        return children;
    }

    public void addChild(final EntityTreeNode entityTreeNode) {
        children.add(entityTreeNode);
    }

    @Override
    public void setId(final Long id) {
        entity.setId(id);
    }

    @Override
    public Long getId() {
        return entity.getId();
    }

    @Override
    public String getName() {
        return entity.getName();
    }

    @Override
    public String getPluginIdentifier() {
        return entity.getPluginIdentifier();
    }

    @Override
    public Object getField(final String fieldName) {
        return entity.getField(fieldName);
    }

    @Override
    public String getStringField(final String fieldName) {
        return entity.getStringField(fieldName);
    }

    @Override
    public Entity getBelongsToField(final String fieldName) {
        return entity.getBelongsToField(fieldName);
    }

    @Override
    public EntityList getHasManyField(final String fieldName) {
        return entity.getHasManyField(fieldName);
    }

    @Override
    public EntityTree getTreeField(final String fieldName) {
        return entity.getTreeField(fieldName);
    }

    @Override
    public void setField(final String fieldName, final Object fieldValue) {
        entity.setField(fieldName, fieldValue);
    }

    @Override
    public Map<String, Object> getFields() {
        return entity.getFields();
    }

    @Override
    public void addGlobalError(final String message, final String... vars) {
        entity.addGlobalError(message, vars);
    }

    @Override
    public void addError(final FieldDefinition fieldDefinition, final String message, final String... vars) {
        entity.addError(fieldDefinition, message, vars);
    }

    @Override
    public List<ErrorMessage> getGlobalErrors() {
        return entity.getGlobalErrors();
    }

    @Override
    public Map<String, ErrorMessage> getErrors() {
        return entity.getErrors();
    }

    @Override
    public ErrorMessage getError(final String fieldName) {
        return entity.getError(fieldName);
    }

    @Override
    public boolean isValid() {
        return entity.isValid();
    }

    @Override
    public void setNotValid() {
        entity.setNotValid();
    }

    @Override
    public boolean isFieldValid(final String fieldName) {
        return entity.isFieldValid(fieldName);
    }

    @Override
    public EntityTreeNode copy() {
        return new EntityTreeNode(entity.copy());
    }

}
