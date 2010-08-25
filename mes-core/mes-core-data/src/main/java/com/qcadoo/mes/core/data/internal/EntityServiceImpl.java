package com.qcadoo.mes.core.data.internal;

import static com.google.common.base.Preconditions.checkNotNull;

import org.apache.commons.beanutils.PropertyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.core.data.api.DataDefinitionService;
import com.qcadoo.mes.core.data.beans.Entity;
import com.qcadoo.mes.core.data.definition.DataDefinition;
import com.qcadoo.mes.core.data.definition.FieldDefinition;
import com.qcadoo.mes.core.data.internal.definition.BelongsToFieldType;

@Service
public final class EntityServiceImpl {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public static final String FIELD_ID = "id";

    public static final String FIELD_DELETED = "deleted";

    public DataDefinition getDataDefinitionForEntity(final String entityName) {
        DataDefinition dataDefinition = dataDefinitionService.get(entityName);
        checkNotNull(dataDefinition, "data definition for %s cannot be found", entityName);
        return dataDefinition;
    }

    public Class<?> getClassForEntity(final DataDefinition dataDefinition) {
        if (dataDefinition.isVirtualTable()) {
            throw new UnsupportedOperationException("virtual tables are not supported");
        } else {
            String fullyQualifiedClassName = dataDefinition.getFullyQualifiedClassName();

            try {
                return EntityServiceImpl.class.getClassLoader().loadClass(fullyQualifiedClassName);
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException("cannot find mapping class for definition: "
                        + dataDefinition.getFullyQualifiedClassName(), e);
            }
        }
    }

    public Entity convertToGenericEntity(final DataDefinition dataDefinition, final Object entity) {
        Entity genericEntity = new Entity(getId(entity));

        for (FieldDefinition fieldDefinition : dataDefinition.getFields()) {
            genericEntity.setField(fieldDefinition.getName(), getField(entity, fieldDefinition));
        }

        return genericEntity;
    }

    public Object convertToDatabaseEntity(final DataDefinition dataDefinition, final Entity genericEntity,
            final Object existingDatabaseEntity) {
        Object databaseEntity = null;

        if (existingDatabaseEntity != null) {
            databaseEntity = existingDatabaseEntity;
        } else {
            databaseEntity = getInstanceForEntity(dataDefinition);
            setId(databaseEntity, genericEntity.getId());
        }

        for (FieldDefinition fieldDefinition : dataDefinition.getFields()) {
            setField(databaseEntity, fieldDefinition, genericEntity.getField(fieldDefinition.getName()));
        }

        return databaseEntity;
    }

    public Long getId(final Object entity) {
        return (Long) getField(entity, FIELD_ID);
    }

    public void setId(final Object entity, final Long id) {
        setField(entity, FIELD_ID, id);
    }

    public void setDeleted(final Object entity) {
        setField(entity, FIELD_DELETED, true);
    }

    public void setField(final Object entity, final FieldDefinition fieldDefinition, final Object value) {
        if (fieldDefinition.isCustomField()) {
            throw new UnsupportedOperationException("custom fields are not supported");
        } else {
            setPrimitiveField(entity, fieldDefinition, value);
        }
    }

    private void setPrimitiveField(final Object entity, final FieldDefinition fieldDefinition, final Object value) {
        if (!fieldDefinition.getType().isValidType(value)) {
            throw new IllegalStateException("value of the property " + entity.getClass().getSimpleName() + "#"
                    + fieldDefinition.getName() + " has invalid type: " + value.getClass().getSimpleName());
        }
        setField(entity, fieldDefinition.getName(), value);
    }

    public Object getField(final Object entity, final FieldDefinition fieldDefinition) {
        if (fieldDefinition.isCustomField()) {
            throw new UnsupportedOperationException("custom fields are not supported");
        } else if (fieldDefinition.getType() instanceof BelongsToFieldType) {
            return getBelongsToField(entity, fieldDefinition);
        } else {
            return getPrimitiveField(entity, fieldDefinition);
        }
    }

    private Object getPrimitiveField(final Object entity, final FieldDefinition fieldDefinition) {
        Object value = getField(entity, fieldDefinition.getName());
        if (!fieldDefinition.getType().isValidType(value)) {
            throw new IllegalStateException("value of the property " + entity.getClass().getSimpleName() + "#"
                    + fieldDefinition.getName() + " has invalid type: " + value.getClass().getSimpleName());
        }
        return value;
    }

    private Object getBelongsToField(final Object entity, final FieldDefinition fieldDefinition) {
        BelongsToFieldType belongsToFieldType = (BelongsToFieldType) fieldDefinition.getType();
        DataDefinition dataDefinition = getDataDefinitionForEntity(belongsToFieldType.getEntityName());
        if (belongsToFieldType.isEagerFetch()) {
            Object value = getField(entity, fieldDefinition.getName());
            return convertToGenericEntity(dataDefinition, value);
        } else {
            throw new IllegalStateException("belongsTo type with lazy loading is not supported yet");
        }
    }

    private void setField(final Object entity, final String fieldName, final Object value) {
        try {
            PropertyUtils.setProperty(entity, fieldName, value);
        } catch (Exception e) {
            throw new IllegalStateException("cannot set value of the property: " + entity.getClass().getSimpleName() + ", "
                    + fieldName, e);
        }
    }

    private Object getField(final Object entity, final String fieldName) {
        try {
            return PropertyUtils.getProperty(entity, fieldName);
        } catch (Exception e) {
            throw new IllegalStateException("cannot get value of the property: " + entity.getClass().getSimpleName() + ", "
                    + fieldName, e);
        }
    }

    private Object getInstanceForEntity(final DataDefinition dataDefinition) {
        Class<?> entityClass = getClassForEntity(dataDefinition);
        try {
            return entityClass.newInstance();
        } catch (InstantiationException e) {
            throw new IllegalStateException("cannot instantiate class: " + dataDefinition.getFullyQualifiedClassName(), e);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("cannot instantiate class: " + dataDefinition.getFullyQualifiedClassName(), e);
        }
    }

}
