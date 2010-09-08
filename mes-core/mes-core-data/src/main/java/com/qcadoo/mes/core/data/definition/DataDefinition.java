package com.qcadoo.mes.core.data.definition;

import static com.google.common.base.Preconditions.checkState;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.qcadoo.mes.core.data.beans.Entity;
import com.qcadoo.mes.core.data.internal.types.PriorityFieldType;
import com.qcadoo.mes.core.data.validation.EntityValidator;

/**
 * Object defines database structure and its representation on grids and forms. The {@link DataDefinition#getName()} points to
 * virtual table ("virtual.tablename"), plugin table ("pluginname.tablename") or core table ("core.tablename").
 * 
 * The method {@link DataDefinition#getFullyQualifiedClassName()} returns the full name of the class that is used for mapping
 * table.
 * 
 * The method {@link DataDefinition#getDiscriminator()} returns value of the column that discriminate which virtual table is used.
 * 
 * @apiviz.owns com.qcadoo.mes.core.data.definition.FieldDefinition
 * @apiviz.owns com.qcadoo.mes.core.data.definition.GridDefinition
 */
public final class DataDefinition {

    private final String name;

    private String fullyQualifiedClassName;

    private String discriminator;

    private final Map<String, DataFieldDefinition> fields = new LinkedHashMap<String, DataFieldDefinition>();

    private DataFieldDefinition priorityField;

    private List<EntityValidator> validators = new ArrayList<EntityValidator>();

    private CallbackDefinition onCreate;

    private CallbackDefinition onUpdate;

    private CallbackDefinition onSave;

    // TODO masz onGet, onDelete, onFind?

    private boolean deletable = true;

    private Class<?> classForEntity;

    public DataDefinition(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getFullyQualifiedClassName() {
        return fullyQualifiedClassName;
    }

    public void setFullyQualifiedClassName(final String fullyQualifiedClassName) {
        this.fullyQualifiedClassName = fullyQualifiedClassName;
        this.classForEntity = loadClassForEntity();
    }

    public String getDiscriminator() {
        return discriminator;
    }

    public void setDiscriminator(final String discriminator) {
        this.discriminator = discriminator;
    }

    public Map<String, DataFieldDefinition> getFields() {
        return fields;
    }

    public void addField(final DataFieldDefinition field) {
        fields.put(field.getName(), field);
    }

    public DataFieldDefinition getField(final String fieldName) {
        if (fields.containsKey(fieldName)) {
            return fields.get(fieldName);
        } else if (priorityField != null && priorityField.getName().equals(fieldName)) {
            return priorityField;
        } else {
            return null;
        }
    }

    public boolean isVirtualTable() {
        return name.startsWith("virtual.");
    }

    public boolean isCoreTable() {
        return name.startsWith("core.");
    }

    public boolean isPluginTable() {
        return !isCoreTable() && !isVirtualTable();
    }

    public List<EntityValidator> getValidators() {
        return validators;
    }

    public void setValidators(final EntityValidator... validators) {
        this.validators = Lists.newArrayList(validators);
    }

    public void setOnCreate(final CallbackDefinition onCreateCallback) {
        this.onCreate = onCreateCallback;
    }

    public void setOnUpdate(final CallbackDefinition onUpdateCallback) {
        this.onUpdate = onUpdateCallback;
    }

    public void setOnSave(final CallbackDefinition onSaveCallback) {
        this.onSave = onSaveCallback;
    }

    public void callOnCreate(final Entity entity) {
        if (onCreate != null) {
            onCreate.callWithEntity(entity);
        }
        if (onSave != null) {
            onSave.callWithEntity(entity);
        }
    }

    public void callOnUpdate(final Entity entity) {
        if (onUpdate != null) {
            onUpdate.callWithEntity(entity);
        }
        if (onSave != null) {
            onSave.callWithEntity(entity);
        }
    }

    public Class<?> getClassForEntity() {
        return classForEntity;
    }

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

    public boolean isPrioritizable() {
        return priorityField != null;
    }

    public void setPriorityField(final DataFieldDefinition priorityField) {
        checkState(priorityField.getType() instanceof PriorityFieldType, "priority field has wrong type");
        checkState(!priorityField.isCustomField(), "priority field cannot be custom field");
        this.priorityField = priorityField;
    }

    public DataFieldDefinition getPriorityField() {
        return priorityField;
    }

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
                return DataDefinition.class.getClassLoader().loadClass(getFullyQualifiedClassName());
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException("cannot find mapping class for definition: " + getFullyQualifiedClassName(), e);
            }
        }
    }

}
