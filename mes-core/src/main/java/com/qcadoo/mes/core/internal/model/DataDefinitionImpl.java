package com.qcadoo.mes.core.internal.model;

import static com.google.common.base.Preconditions.checkState;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.qcadoo.mes.core.api.Entity;
import com.qcadoo.mes.core.internal.DataAccessService;
import com.qcadoo.mes.core.internal.search.SearchCriteriaImpl;
import com.qcadoo.mes.core.internal.types.PriorityType;
import com.qcadoo.mes.model.FieldDefinition;
import com.qcadoo.mes.model.HookDefinition;
import com.qcadoo.mes.model.search.SearchCriteria;
import com.qcadoo.mes.model.search.SearchCriteriaBuilder;
import com.qcadoo.mes.model.search.SearchResult;
import com.qcadoo.mes.model.validation.EntityValidator;

/**
 * Object defines database structure and its representation on grids and forms. The {@link DataDefinitionImpl#getName()} points to
 * virtual table ("virtual.tablename"), plugin table ("pluginname.tablename") or core table ("core.tablename").
 * 
 * The method {@link DataDefinitionImpl#getFullyQualifiedClassName()} returns the full name of the class that is used for mapping
 * table.
 * 
 * The method {@link DataDefinitionImpl#getDiscriminator()} returns value of the column that discriminate which virtual table is
 * used.
 * 
 * @apiviz.owns com.qcadoo.mes.core.data.definition.FieldDefinition
 * @apiviz.owns com.qcadoo.mes.core.data.definition.GridDefinition
 */
public final class DataDefinitionImpl implements InternalDataDefinition {

    private final DataAccessService dataAccessService;

    private final String pluginIdentifier;

    private final String name;

    private String fullyQualifiedClassName;

    // private String discriminator;

    private final Map<String, FieldDefinition> fields = new LinkedHashMap<String, FieldDefinition>();

    private FieldDefinition priorityField;

    private final List<EntityValidator> validators = new ArrayList<EntityValidator>();

    private HookDefinition createHook;

    private HookDefinition updateHook;

    private HookDefinition saveHook;

    // TODO masz onGet, onDelete, onFind?

    private boolean deletable = true;

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

    public void withValidator(final EntityValidator validator) {
        this.validators.add(validator);
    }

    public void withCreateHook(final HookDefinition createHook) {
        this.createHook = createHook;
    }

    public void withUpdateHook(final HookDefinition updateHook) {
        this.updateHook = updateHook;
    }

    public void withSaveHook(final HookDefinition saveHook) {
        this.saveHook = saveHook;
    }

    @Override
    public void callCreateHook(final Entity entity) {
        if (createHook != null) {
            createHook.callWithEntity(this, entity);
        }
        if (saveHook != null) {
            saveHook.callWithEntity(this, entity);
        }
    }

    @Override
    public void callUpdateHook(final Entity entity) {
        if (updateHook != null) {
            updateHook.callWithEntity(this, entity);
        }
        if (saveHook != null) {
            saveHook.callWithEntity(this, entity);
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

    public void withPriorityField(final FieldDefinition priorityField) {
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
                return getClass().getClassLoader().loadClass(getFullyQualifiedClassName());
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException("cannot find mapping class for definition: " + getFullyQualifiedClassName(), e);
            }
        }
    }

}
