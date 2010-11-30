/**
 * ********************************************************************
 * Code developed by amazing QCADOO developers team.
 * Copyright (c) Qcadoo Limited sp. z o.o. (2010)
 * ********************************************************************
 */

package com.qcadoo.mes.internal;

import java.util.Collections;
import java.util.Map.Entry;

import org.apache.commons.beanutils.PropertyUtils;
import org.hibernate.proxy.HibernateProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.model.FieldDefinition;
import com.qcadoo.mes.model.internal.InternalDataDefinition;
import com.qcadoo.mes.model.types.BelongsToType;
import com.qcadoo.mes.model.types.HasManyType;
import com.qcadoo.mes.model.types.internal.PasswordType;

@Service
public final class EntityService {

    private static final Logger LOG = LoggerFactory.getLogger(EntityService.class);

    public static final String FIELD_ID = "id";

    public Long getId(final Object databaseEntity) {
        return (Long) getField(databaseEntity, FIELD_ID);
    }

    public void setId(final Object databaseEntity, final Long id) {
        setField(databaseEntity, FIELD_ID, id);
    }

    public void setField(final Object databaseEntity, final FieldDefinition fieldDefinition, final Object value) {
        if (!(fieldDefinition.getType() instanceof PasswordType && value == null)) {
            setField(databaseEntity, fieldDefinition.getName(), value);
        }
    }

    public Object getField(final Object databaseEntity, final FieldDefinition fieldDefinition) {
        if (fieldDefinition.getType() instanceof BelongsToType) {
            return getBelongsToField(databaseEntity, fieldDefinition);
        } else if (fieldDefinition.getType() instanceof HasManyType) {
            return getHasManyField(databaseEntity, fieldDefinition);
        } else {
            return getPrimitiveField(databaseEntity, fieldDefinition);
        }
    }

    public Entity convertToGenericEntity(final InternalDataDefinition dataDefinition, final Object databaseEntity) {
        Entity genericEntity = new DefaultEntity(dataDefinition.getPluginIdentifier(), dataDefinition.getName(),
                getId(databaseEntity));

        for (Entry<String, FieldDefinition> fieldDefinitionEntry : dataDefinition.getFields().entrySet()) {
            if (fieldDefinitionEntry.getValue().isPersistent()) {
                genericEntity.setField(fieldDefinitionEntry.getKey(), getField(databaseEntity, fieldDefinitionEntry.getValue()));
            }
        }

        if (dataDefinition.isPrioritizable()) {
            genericEntity.setField(dataDefinition.getPriorityField().getName(),
                    getField(databaseEntity, dataDefinition.getPriorityField()));
        }

        return genericEntity;
    }

    public Object convertToDatabaseEntity(final InternalDataDefinition dataDefinition, final Entity genericEntity,
            final Object existingDatabaseEntity) {
        Object databaseEntity = getDatabaseEntity(dataDefinition, genericEntity, existingDatabaseEntity);

        for (Entry<String, FieldDefinition> fieldDefinitionEntry : dataDefinition.getFields().entrySet()) {
            if (fieldDefinitionEntry.getValue().isPersistent()) {
                setField(databaseEntity, fieldDefinitionEntry.getValue(), genericEntity.getField(fieldDefinitionEntry.getKey()));
            }
        }

        if (dataDefinition.isPrioritizable()) {
            genericEntity.setField(dataDefinition.getPriorityField().getName(), null);
        }

        return databaseEntity;
    }

    private Object getDatabaseEntity(final InternalDataDefinition dataDefinition, final Entity genericEntity,
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

    private Object getPrimitiveField(final Object databaseEntity, final FieldDefinition fieldDefinition) {
        return getField(databaseEntity, fieldDefinition.getName());
    }

    private Object getHasManyField(final Object databaseEntity, final FieldDefinition fieldDefinition) {
        Long parentId = getId(databaseEntity);

        if (parentId == null) {
            return Collections.<Entity> emptyList();
        }

        HasManyType hasManyFieldType = (HasManyType) fieldDefinition.getType();
        InternalDataDefinition referencedDataDefinition = (InternalDataDefinition) hasManyFieldType.getDataDefinition();

        return new EntityList(referencedDataDefinition, hasManyFieldType.getJoinFieldName(), parentId);
    }

    private Object getBelongsToField(final Object databaseEntity, final FieldDefinition fieldDefinition) {
        BelongsToType belongsToFieldType = (BelongsToType) fieldDefinition.getType();
        InternalDataDefinition referencedDataDefinition = (InternalDataDefinition) belongsToFieldType.getDataDefinition();

        Object value = getField(databaseEntity, fieldDefinition.getName());

        if (value == null) {
            return null;
        }

        if (belongsToFieldType.isLazyLoading()) {
            Long id = null;

            if (value instanceof HibernateProxy) {
                id = (Long) ((HibernateProxy) value).getHibernateLazyInitializer().getIdentifier();
            } else {
                LOG.warn("Laziness of " + databaseEntity.getClass().getCanonicalName() + "#" + fieldDefinition.getName()
                        + " in model.xml and hibernate bean is different.");
                id = getId(getField(databaseEntity, fieldDefinition.getName()));
            }

            if (id == null) {
                return null;
            }

            return new ProxyEntity(referencedDataDefinition, id);
        } else {
            return convertToGenericEntity(referencedDataDefinition, value);
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
