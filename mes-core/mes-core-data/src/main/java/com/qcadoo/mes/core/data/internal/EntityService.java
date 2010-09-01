package com.qcadoo.mes.core.data.internal;

import java.util.Map.Entry;

import org.apache.commons.beanutils.PropertyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.core.data.api.DataDefinitionService;
import com.qcadoo.mes.core.data.beans.Entity;
import com.qcadoo.mes.core.data.definition.DataDefinition;
import com.qcadoo.mes.core.data.definition.FieldDefinition;
import com.qcadoo.mes.core.data.internal.types.BelongsToFieldType;
import com.qcadoo.mes.core.data.validation.ValidationResults;

@Service
public final class EntityService {

    @Autowired
    private ValidationService validationService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

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

    public void setField(final Object databaseEntity, final DataDefinition dataDefinition, final FieldDefinition fieldDefinition,
            final Object value) {
        if (fieldDefinition.isCustomField()) {
            throw new UnsupportedOperationException("custom fields are not supported");
        } else {
            setField(databaseEntity, fieldDefinition.getName(), value);
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

        Entity validatedEntity = validationService.parseAndValidateEntity(dataDefinition, genericEntity, validationResults);

        if (validationResults.isValid()) {
            Object databaseEntity = null;

            if (existingDatabaseEntity != null) {
                databaseEntity = existingDatabaseEntity;
            } else {
                databaseEntity = dataDefinition.getInstanceForEntity();
                setId(databaseEntity, validatedEntity.getId());
            }

            for (Entry<String, FieldDefinition> fieldDefinitionEntry : dataDefinition.getFields().entrySet()) {
                setField(databaseEntity, dataDefinition, fieldDefinitionEntry.getValue(),
                        validatedEntity.getField(fieldDefinitionEntry.getKey()));
            }

            return databaseEntity;
        } else {
            return null;
        }
    }

    private Object getPrimitiveField(final Object databaseEntity, final FieldDefinition fieldDefinition) {
        return getField(databaseEntity, fieldDefinition.getName());
    }

    private Object getBelongsToField(final Object databaseEntity, final FieldDefinition fieldDefinition) {
        BelongsToFieldType belongsToFieldType = (BelongsToFieldType) fieldDefinition.getType();
        DataDefinition referencedDataDefinition = dataDefinitionService.get(belongsToFieldType.getEntityName());
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
