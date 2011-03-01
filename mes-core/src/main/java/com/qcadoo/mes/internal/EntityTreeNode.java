/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.3.0
 *
 * This file is part of Qcadoo.
 *
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */

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

    public String getEntityType() {
        return getStringField("entityType");
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
    public String toString() {
        return entity.toString();
    }

    @Override
    public EntityTreeNode copy() {
        return new EntityTreeNode(entity.copy());
    }

    @Override
    public int hashCode() {
        return entity.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        return entity.equals(obj);
    }

}
