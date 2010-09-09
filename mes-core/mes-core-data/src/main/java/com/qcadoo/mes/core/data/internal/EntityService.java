package com.qcadoo.mes.core.data.internal;

import java.util.Map.Entry;

import org.apache.commons.beanutils.PropertyUtils;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.core.data.beans.Entity;
import com.qcadoo.mes.core.data.definition.DataDefinition;
import com.qcadoo.mes.core.data.definition.DataFieldDefinition;
import com.qcadoo.mes.core.data.internal.types.BelongsToType;
import com.qcadoo.mes.core.data.internal.types.PasswordType;

@Service
public final class EntityService {

    public static final String FIELD_ID = "id";

    public static final String FIELD_DELETED = "deleted";

    public Long getId(final Object databaseEntity) {
        return (Long) getField(databaseEntity, FIELD_ID);
    }

    public void setId(final Object databaseEntity, final Long id) {
        setField(databaseEntity, FIELD_ID, id);
    }

    public void setDeleted(final Object databaseEntity) {
        setField(databaseEntity, FIELD_DELETED, true);
    }

    public void addDeletedRestriction(final Criteria criteria) {
        criteria.add(Restrictions.ne(EntityService.FIELD_DELETED, true));
    }

    public void setField(final Object databaseEntity, final DataFieldDefinition fieldDefinition, final Object value) {
        if (fieldDefinition.isCustomField()) {
            throw new UnsupportedOperationException("custom fields are not supported");
        } else if (!(fieldDefinition.getType() instanceof PasswordType && value == null)) {
            setField(databaseEntity, fieldDefinition.getName(), value);
        }
    }

    public Object getField(final Object databaseEntity, final DataFieldDefinition fieldDefinition) {
        if (fieldDefinition.isCustomField()) {
            throw new UnsupportedOperationException("custom fields are not supported");
        } else if (fieldDefinition.getType() instanceof BelongsToType) {
            return getBelongsToField(databaseEntity, fieldDefinition);
        } else {
            return getPrimitiveField(databaseEntity, fieldDefinition);
        }
    }

    public Entity convertToGenericEntity(final DataDefinition dataDefinition, final Object databaseEntity) {
        Entity genericEntity = new Entity(getId(databaseEntity));

        for (Entry<String, DataFieldDefinition> fieldDefinitionEntry : dataDefinition.getFields().entrySet()) {
            genericEntity.setField(fieldDefinitionEntry.getKey(), getField(databaseEntity, fieldDefinitionEntry.getValue()));
        }

        if (dataDefinition.isPrioritizable()) {
            genericEntity.setField(dataDefinition.getPriorityField().getName(),
                    getField(databaseEntity, dataDefinition.getPriorityField()));
        }

        return genericEntity;
    }

    public Object convertToDatabaseEntity(final DataDefinition dataDefinition, final Entity genericEntity,
            final Object existingDatabaseEntity) {
        Object databaseEntity = getDatabaseEntity(dataDefinition, genericEntity, existingDatabaseEntity);

        for (Entry<String, DataFieldDefinition> fieldDefinitionEntry : dataDefinition.getFields().entrySet()) {
            setField(databaseEntity, fieldDefinitionEntry.getValue(), genericEntity.getField(fieldDefinitionEntry.getKey()));
        }

        return databaseEntity;
    }

    private Object getDatabaseEntity(final DataDefinition dataDefinition, final Entity genericEntity,
            final Object existingDatabaseEntity) {
        Object databaseEntity = null;

        if (existingDatabaseEntity != null) {
            databaseEntity = existingDatabaseEntity;
        } else {
            databaseEntity = dataDefinition.getInstanceForEntity();
            setId(databaseEntity, genericEntity.getId());
        }
        return databaseEntity;
    }

    private Object getPrimitiveField(final Object databaseEntity, final DataFieldDefinition fieldDefinition) {
        return getField(databaseEntity, fieldDefinition.getName());
    }

    private Object getBelongsToField(final Object databaseEntity, final DataFieldDefinition fieldDefinition) {
        BelongsToType belongsToFieldType = (BelongsToType) fieldDefinition.getType();
        DataDefinition referencedDataDefinition = belongsToFieldType.getDataDefinition();
        if (belongsToFieldType.isEagerFetch()) {
            Object value = getField(databaseEntity, fieldDefinition.getName());
            if (value != null) {
                return convertToGenericEntity(referencedDataDefinition, value);
            } else {
                return null;
            }
        } else {
            throw new IllegalStateException("belongsTo type with lazy loading is not supported yet");
        }
    }

    private void setField(final Object databaseEntity, final String fieldName, final Object value) {
        try {
            PropertyUtils.setProperty(databaseEntity, fieldName, value);
        } catch (Exception e) {
            throw new IllegalStateException("cannot set value of the property: " + databaseEntity.getClass().getSimpleName()
                    + ", " + fieldName, e);
        }
    }

    private Object getField(final Object databaseEntity, final String fieldName) {
        try {
            return PropertyUtils.getProperty(databaseEntity, fieldName);
        } catch (Exception e) {
            throw new IllegalStateException("cannot get value of the property: " + databaseEntity.getClass().getSimpleName()
                    + ", " + fieldName, e);
        }
    }

}
