package com.qcadoo.mes.core.data.internal;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Map.Entry;

import org.apache.commons.beanutils.PropertyUtils;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.core.data.api.DataDefinitionService;
import com.qcadoo.mes.core.data.beans.Entity;
import com.qcadoo.mes.core.data.definition.DataDefinition;
import com.qcadoo.mes.core.data.definition.FieldDefinition;
import com.qcadoo.mes.core.data.internal.types.BelongsToFieldType;
import com.qcadoo.mes.core.data.validation.FieldValidator;
import com.qcadoo.mes.core.data.validation.ValidationResults;

@Service
public final class EntityServiceImpl {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private SessionFactory sessionFactory;

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

    public Long getId(final Object databaseEntity) {
        return (Long) getField(databaseEntity, FIELD_ID);
    }

    public void setId(final Object databaseEntity, final Long id) {
        setField(databaseEntity, FIELD_ID, id);
    }

    public void setDeleted(final Object databaseEntity) {
        setField(databaseEntity, FIELD_DELETED, true);
    }

    public void setField(final Object databaseEntity, final FieldDefinition fieldDefinition, final Object value,
            final ValidationResults validationResults) {
        if (fieldDefinition.isCustomField()) {
            throw new UnsupportedOperationException("custom fields are not supported");
        } else if (fieldDefinition.getType() instanceof BelongsToFieldType) {
            setBelongsToField(databaseEntity, fieldDefinition, value, validationResults);
        } else {
            setPrimitiveField(databaseEntity, fieldDefinition, value, validationResults);
        }
    }

    public Object getField(final Object databaseEntity, final FieldDefinition fieldDefinition) {
        if (fieldDefinition.isCustomField()) {
            throw new UnsupportedOperationException("custom fields are not supported");
        } else if (fieldDefinition.getType() instanceof BelongsToFieldType) {
            return getBelongsToField(databaseEntity, fieldDefinition);
        } else {
            return getPrimitiveField(databaseEntity, fieldDefinition);
        }
    }

    public Entity convertToGenericEntity(final DataDefinition dataDefinition, final Object databaseEntity) {
        Entity genericEntity = new Entity(getId(databaseEntity));

        for (Entry<String, FieldDefinition> fieldDefinitionEntry : dataDefinition.getFields().entrySet()) {
            genericEntity.setField(fieldDefinitionEntry.getKey(), getField(databaseEntity, fieldDefinitionEntry.getValue()));
        }

        return genericEntity;
    }

    public Object convertToDatabaseEntity(final DataDefinition dataDefinition, final Entity genericEntity,
            final Object existingDatabaseEntity, final ValidationResults validationResults) {
        Object databaseEntity = null;

        if (existingDatabaseEntity != null) {
            databaseEntity = existingDatabaseEntity;
        } else {
            databaseEntity = getInstanceForEntity(dataDefinition);
            setId(databaseEntity, genericEntity.getId());
        }

        for (Entry<String, FieldDefinition> fieldDefinitionEntry : dataDefinition.getFields().entrySet()) {
            setField(databaseEntity, fieldDefinitionEntry.getValue(), genericEntity.getField(fieldDefinitionEntry.getKey()),
                    validationResults);

        }

        return databaseEntity;
    }

    private void setPrimitiveField(final Object databaseEntity, final FieldDefinition fieldDefinition, final Object value,
            final ValidationResults validationResults) {
        Object parsedValue = parseAndValidateValue(fieldDefinition, value, validationResults);
        if (validationResults.isFieldValid(fieldDefinition)) {
            setField(databaseEntity, fieldDefinition.getName(), parsedValue);
        }
    }

    private Object parseAndValidateValue(final FieldDefinition fieldDefinition, final Object value,
            final ValidationResults validationResults) {
        Object fieldValue = value;
        if (fieldValue != null) {
            if (fieldValue instanceof String) {
                fieldValue = ((String) fieldValue).trim();
            }
            if (!fieldDefinition.getType().getType().isInstance(fieldValue)) {
                if (fieldValue instanceof String) {
                    fieldValue = fieldDefinition.getType().fromString(fieldDefinition, (String) fieldValue, validationResults);
                } else {
                    validationResults.addError(fieldDefinition, "core.validation.error.wrongType", fieldValue.getClass()
                            .getSimpleName(), fieldDefinition.getType().getType().getSimpleName());
                    return null;
                }
                if (validationResults.isFieldNotValid(fieldDefinition)) {
                    return null;
                }
                if (!fieldDefinition.getType().validate(fieldDefinition, fieldValue, validationResults)) {
                    return null;
                }
            }
        }
        for (FieldValidator fieldValidator : fieldDefinition.getValidators()) {
            if (!fieldValidator.validate(fieldDefinition, fieldValue, validationResults)) {
                return null;
            }
        }
        return fieldValue;
    }

    private void setBelongsToField(final Object databaseEntity, final FieldDefinition fieldDefinition, final Object value,
            final ValidationResults validationResults) {
        if (value != null) {
            Long referencedEntityId = ((Entity) value).getId();
            BelongsToFieldType belongsToFieldType = (BelongsToFieldType) fieldDefinition.getType();
            DataDefinition referencedDataDefinition = getDataDefinitionForEntity(belongsToFieldType.getEntityName());
            Class<?> referencedClass = getClassForEntity(referencedDataDefinition);
            Object referencedEntity = sessionFactory.getCurrentSession().get(referencedClass, referencedEntityId);
            referencedEntity = parseAndValidateValue(fieldDefinition, referencedEntity, validationResults);
            if (validationResults.isFieldValid(fieldDefinition)) {
                setField(databaseEntity, fieldDefinition.getName(), referencedEntity);
            }
        } else {
            setField(databaseEntity, fieldDefinition.getName(), null);
        }
    }

    private Object getPrimitiveField(final Object databaseEntity, final FieldDefinition fieldDefinition) {
        return getField(databaseEntity, fieldDefinition.getName());
    }

    private Object getBelongsToField(final Object databaseEntity, final FieldDefinition fieldDefinition) {
        BelongsToFieldType belongsToFieldType = (BelongsToFieldType) fieldDefinition.getType();
        DataDefinition dataDefinition = getDataDefinitionForEntity(belongsToFieldType.getEntityName());
        if (belongsToFieldType.isEagerFetch()) {
            Object value = getField(databaseEntity, fieldDefinition.getName());
            if (value != null) {
                return convertToGenericEntity(dataDefinition, value);
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
