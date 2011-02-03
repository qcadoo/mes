/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.2.0
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

package com.qcadoo.mes.model.internal;

import static com.google.common.base.Preconditions.checkState;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.internal.DataAccessService;
import com.qcadoo.mes.model.FieldDefinition;
import com.qcadoo.mes.model.HookDefinition;
import com.qcadoo.mes.model.search.SearchCriteria;
import com.qcadoo.mes.model.search.SearchCriteriaBuilder;
import com.qcadoo.mes.model.search.SearchResult;
import com.qcadoo.mes.model.search.internal.SearchCriteriaImpl;
import com.qcadoo.mes.model.types.internal.PriorityType;
import com.qcadoo.mes.model.validators.EntityValidator;

public final class DataDefinitionImpl implements InternalDataDefinition {

    private final DataAccessService dataAccessService;

    private final String pluginIdentifier;

    private final String name;

    private String fullyQualifiedClassName;

    private final Map<String, FieldDefinition> fields = new LinkedHashMap<String, FieldDefinition>();

    private FieldDefinition priorityField;

    private final List<EntityValidator> validators = new ArrayList<EntityValidator>();

    private final List<HookDefinition> createHooks = new ArrayList<HookDefinition>();

    private final List<HookDefinition> updateHooks = new ArrayList<HookDefinition>();

    private final List<HookDefinition> saveHooks = new ArrayList<HookDefinition>();

    private final List<HookDefinition> copyHooks = new ArrayList<HookDefinition>();

    private boolean deletable = true;

    private boolean creatable = true;

    private boolean updatable = true;

    private Class<?> classForEntity;

    public DataDefinitionImpl(final String pluginIdentifier, final String name, final DataAccessService dataAccessService) {
        this.pluginIdentifier = pluginIdentifier;
        this.name = name;
        this.dataAccessService = dataAccessService;
    }

    @Override
    public Entity get(final Long id) {
        return dataAccessService.get(this, id);
    }

    @Override
    public Entity copy(final Long id) {
        return dataAccessService.copy(this, id);
    }

    @Override
    public void delete(final Long id) {
        dataAccessService.delete(this, id);
    }

    @Override
    public Entity save(final Entity entity) {
        return dataAccessService.save(this, entity);
    }

    @Override
    public SearchCriteriaBuilder find() {
        return new SearchCriteriaImpl(this);
    }

    @Override
    public SearchResult find(final SearchCriteria searchCriteria) {
        return dataAccessService.find(searchCriteria);
    }

    @Override
    public void move(final Long id, final int offset) {
        dataAccessService.move(this, id, offset);

    }

    @Override
    public void moveTo(final Long id, final int position) {
        dataAccessService.moveTo(this, id, position);
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
    public String getFullyQualifiedClassName() {
        return fullyQualifiedClassName;
    }

    public void setFullyQualifiedClassName(final String fullyQualifiedClassName) {
        this.fullyQualifiedClassName = fullyQualifiedClassName;
        this.classForEntity = loadClassForEntity();
    }

    @Override
    public Map<String, FieldDefinition> getFields() {
        return fields;
    }

    public void withField(final FieldDefinition field) {
        fields.put(field.getName(), field);
    }

    @Override
    public FieldDefinition getField(final String fieldName) {
        if (fields.containsKey(fieldName)) {
            return fields.get(fieldName);
        } else if (priorityField != null && priorityField.getName().equals(fieldName)) {
            return priorityField;
        } else {
            return null;
        }
    }

    @Override
    public List<EntityValidator> getValidators() {
        return validators;
    }

    public void withValidator(final EntityValidator validator) {
        this.validators.add(validator);
    }

    public void withCreateHook(final HookDefinition createHook) {
        createHooks.add(createHook);
    }

    public void withUpdateHook(final HookDefinition updateHook) {
        updateHooks.add(updateHook);
    }

    public void withSaveHook(final HookDefinition saveHook) {
        saveHooks.add(saveHook);
    }

    public void withCopyHook(final HookDefinition copyHook) {
        copyHooks.add(copyHook);
    }

    @Override
    public void callCreateHook(final Entity entity) {
        for (HookDefinition hook : createHooks) {
            hook.callWithEntity(this, entity);
        }
        for (HookDefinition hook : saveHooks) {
            hook.callWithEntity(this, entity);
        }
    }

    @Override
    public void callUpdateHook(final Entity entity) {
        for (HookDefinition hook : updateHooks) {
            hook.callWithEntity(this, entity);
        }
        for (HookDefinition hook : saveHooks) {
            hook.callWithEntity(this, entity);
        }
    }

    @Override
    public boolean callCopyHook(final Entity entity) {
        for (HookDefinition hook : copyHooks) {
            if (!hook.callWithEntityAndGetBoolean(this, entity)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Class<?> getClassForEntity() {
        return classForEntity;
    }

    @Override
    public Object getInstanceForEntity() {
        Class<?> entityClass = getClassForEntity();
        try {
            return entityClass.newInstance();
        } catch (InstantiationException e) {
            throw new IllegalStateException("cannot instantiate class: " + getFullyQualifiedClassName(), e);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("cannot instantiate class: " + getFullyQualifiedClassName(), e);
        }
    }

    @Override
    public boolean isPrioritizable() {
        return priorityField != null;
    }

    public void withPriorityField(final FieldDefinition priorityField) {
        checkState(priorityField.getType() instanceof PriorityType, "priority field has wrong type");
        this.priorityField = priorityField;
    }

    @Override
    public FieldDefinition getPriorityField() {
        return priorityField;
    }

    @Override
    public boolean isDeletable() {
        return deletable;
    }

    public void setDeletable(final boolean deletable) {
        this.deletable = deletable;
    }

    @Override
    public boolean isUpdatable() {
        return updatable;
    }

    @Override
    public boolean isCreatable() {
        return creatable;
    }

    public void setCreatable(final boolean creatable) {
        this.creatable = creatable;
    }

    public void setUpdatable(final boolean updatable) {
        this.updatable = updatable;
    }

    private Class<?> loadClassForEntity() {
        try {
            return getClass().getClassLoader().loadClass(getFullyQualifiedClassName());
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("cannot find mapping class for definition: " + getFullyQualifiedClassName(), e);
        }
    }

}
