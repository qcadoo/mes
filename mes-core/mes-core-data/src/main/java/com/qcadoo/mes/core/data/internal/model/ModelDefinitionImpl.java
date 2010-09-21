package com.qcadoo.mes.core.data.internal.model;

import static com.google.common.base.Preconditions.checkState;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.qcadoo.mes.core.data.api.DataAccessService;
import com.qcadoo.mes.core.data.beans.Entity;
import com.qcadoo.mes.core.data.internal.search.SearchCriteriaBuilder;
import com.qcadoo.mes.core.data.internal.search.SearchCriteriaImpl;
import com.qcadoo.mes.core.data.internal.types.PriorityType;
import com.qcadoo.mes.core.data.model.FieldDefinition;
import com.qcadoo.mes.core.data.model.HookDefinition;
import com.qcadoo.mes.core.data.model.ModelDefinition;
import com.qcadoo.mes.core.data.search.SearchCriteria;
import com.qcadoo.mes.core.data.search.SearchResult;
import com.qcadoo.mes.core.data.validation.EntityValidator;

/**
 * Object defines database structure and its representation on grids and forms. The {@link ModelDefinitionImpl#getName()} points
 * to virtual table ("virtual.tablename"), plugin table ("pluginname.tablename") or core table ("core.tablename").
 * 
 * The method {@link ModelDefinitionImpl#getFullyQualifiedClassName()} returns the full name of the class that is used for mapping
 * table.
 * 
 * The method {@link ModelDefinitionImpl#getDiscriminator()} returns value of the column that discriminate which virtual table is
 * used.
 * 
 * @apiviz.owns com.qcadoo.mes.core.data.definition.FieldDefinition
 * @apiviz.owns com.qcadoo.mes.core.data.definition.GridDefinition
 */
public final class ModelDefinitionImpl implements ModelDefinition {

    private final DataAccessService dataAccessService;

    private final String name;

    private String fullyQualifiedClassName;

    // private String discriminator;

    private final Map<String, FieldDefinition> fields = new LinkedHashMap<String, FieldDefinition>();

    private FieldDefinition priorityField;

    private final List<EntityValidator> validators = new ArrayList<EntityValidator>();

    private HookDefinition onCreate;

    private HookDefinition onUpdate;

    private HookDefinition onSave;

    // TODO masz onGet, onDelete, onFind?

    private boolean deletable = true;

    private Class<?> classForEntity;

    public ModelDefinitionImpl(final String name, final DataAccessService dataAccessService) {
        this.name = name;
        this.dataAccessService = dataAccessService;
    }

    @Override
    public Entity get(final Long id) {
        return dataAccessService.get(this, id);
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

    public void addField(final FieldDefinition field) {
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
    public boolean isVirtualTable() {
        return name.startsWith("virtual.");
    }

    @Override
    public boolean isCoreTable() {
        return name.startsWith("core.");
    }

    @Override
    public boolean isPluginTable() {
        return !isCoreTable() && !isVirtualTable();
    }

    @Override
    public List<EntityValidator> getValidators() {
        return validators;
    }

    public void addValidator(final EntityValidator validator) {
        this.validators.add(validator);
    }

    public void setOnCreate(final HookDefinition onCreateCallback) {
        this.onCreate = onCreateCallback;
    }

    public void setOnUpdate(final HookDefinition onUpdateCallback) {
        this.onUpdate = onUpdateCallback;
    }

    public void setOnSave(final HookDefinition onSaveCallback) {
        this.onSave = onSaveCallback;
    }

    @Override
    public void callOnCreate(final Entity entity) {
        if (onCreate != null) {
            onCreate.callWithEntity(this, entity);
        }
        if (onSave != null) {
            onSave.callWithEntity(this, entity);
        }
    }

    @Override
    public void callOnUpdate(final Entity entity) {
        if (onUpdate != null) {
            onUpdate.callWithEntity(this, entity);
        }
        if (onSave != null) {
            onSave.callWithEntity(this, entity);
        }
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

    public void setPriorityField(final FieldDefinition priorityField) {
        checkState(priorityField.getType() instanceof PriorityType, "priority field has wrong type");
        checkState(!priorityField.isCustomField(), "priority field cannot be custom field");
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

    private Class<?> loadClassForEntity() {
        if (isVirtualTable()) {
            throw new UnsupportedOperationException("virtual tables are not supported");
        } else {
            try {
                return ModelDefinition.class.getClassLoader().loadClass(getFullyQualifiedClassName());
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException("cannot find mapping class for definition: " + getFullyQualifiedClassName(), e);
            }
        }
    }

}
