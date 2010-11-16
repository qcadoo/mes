/**
 * ********************************************************************
 * Code developed by amazing QCADOO developers team.
 * Copyright © Qcadoo Limited sp. z o.o. (2010)
 * ********************************************************************
 */

package com.qcadoo.mes.internal;

import java.util.Map;
import java.util.Map.Entry;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.model.DataDefinition;
import com.qcadoo.mes.model.FieldDefinition;
import com.qcadoo.mes.model.internal.InternalDataDefinition;
import com.qcadoo.mes.model.types.BelongsToType;
import com.qcadoo.mes.model.types.internal.BelongsToEntityType;
import com.qcadoo.mes.model.validators.EntityValidator;
import com.qcadoo.mes.model.validators.FieldValidator;

@Service
public final class ValidationService {

    @Autowired
    private SessionFactory sessionFactory;

    public void validateGenericEntity(final InternalDataDefinition dataDefinition, final Entity genericEntity,
            final Entity existingGenericEntity) {

        copyReadOnlyAndMissingFields(dataDefinition, genericEntity, existingGenericEntity);

        parseAndValidateEntity(dataDefinition, genericEntity);

        if (genericEntity.getId() != null) {
            dataDefinition.callUpdateHook(genericEntity);
        } else {
            dataDefinition.callCreateHook(genericEntity);
        }
    }

    private void copyReadOnlyAndMissingFields(final DataDefinition dataDefinition, final Entity genericEntity,
            final Entity existingGenericEntity) {
        for (Map.Entry<String, FieldDefinition> field : dataDefinition.getFields().entrySet()) {
            Object value = existingGenericEntity != null ? existingGenericEntity.getField(field.getKey()) : null;
            if (field.getValue().isReadOnly()) {
                genericEntity.setField(field.getKey(), value);
            }
            if (field.getValue().isReadOnlyOnUpdate() && genericEntity.getId() != null) {
                genericEntity.setField(field.getKey(), value);
            }
            if (!genericEntity.getFields().containsKey(field.getKey()) && genericEntity.getId() != null) {
                genericEntity.setField(field.getKey(), value);
            }
        }
    }

    private void parseAndValidateEntity(final InternalDataDefinition dataDefinition, final Entity genericEntity) {
        for (Entry<String, FieldDefinition> fieldDefinitionEntry : dataDefinition.getFields().entrySet()) {
            Object validateFieldValue = parseAndValidateField(dataDefinition, fieldDefinitionEntry.getValue(),
                    genericEntity.getField(fieldDefinitionEntry.getKey()), genericEntity);
            genericEntity.setField(fieldDefinitionEntry.getKey(), validateFieldValue);
        }

        for (Entry<String, FieldDefinition> fieldDefinitionEntry : dataDefinition.getFields().entrySet()) {
            if (!genericEntity.isFieldValid(fieldDefinitionEntry.getKey())) {
                continue;
            }
            for (FieldValidator fieldValidator : fieldDefinitionEntry.getValue().getValidators()) {
                fieldValidator.validate(dataDefinition, fieldDefinitionEntry.getValue(), genericEntity);
                if (!genericEntity.isFieldValid(fieldDefinitionEntry.getKey())) {
                    break;
                }
            }
        }

        if (genericEntity.isValid()) {
            for (EntityValidator entityValidator : dataDefinition.getValidators()) {
                entityValidator.validate(dataDefinition, genericEntity);
            }
        }
    }

    private Object parseAndValidateValue(final DataDefinition dataDefinition, final FieldDefinition fieldDefinition,
            final Object value, final Entity validatedEntity) {
        Object fieldValue = null;
        if (value != null) {
            fieldValue = fieldDefinition.getType().toObject(fieldDefinition, value, validatedEntity);
            if (!validatedEntity.isFieldValid(fieldDefinition.getName())) {
                return null;
            }
        }
        for (FieldValidator fieldValidator : fieldDefinition.getValidators()) {
            if (!fieldValidator.validate(dataDefinition, fieldDefinition, fieldValue, validatedEntity)) {
                return null;
            }
        }
        return fieldValue;
    }

    private Object parseAndValidateBelongsToField(final DataDefinition dataDefinition, final FieldDefinition fieldDefinition,
            final Object value, final Entity validatedEntity) {
        Object referencedEntity;

        if (value != null) {
            Long referencedEntityId = null;
            if (value instanceof String) {
                try {
                    referencedEntityId = Long.valueOf((String) value);
                } catch (NumberFormatException e) {
                    validatedEntity.addError(fieldDefinition, "core.validate.field.error.wrongType", value.getClass()
                            .getSimpleName(), fieldDefinition.getType().getType().getSimpleName());
                }
            } else if (value instanceof Long) {
                referencedEntityId = (Long) value;
            } else if (value instanceof Entity) {
                referencedEntityId = ((Entity) value).getId();
            } else {
                validatedEntity.addError(fieldDefinition, "core.validate.field.error.wrongType",
                        value.getClass().getSimpleName(), fieldDefinition.getType().getType().getSimpleName());
            }
            if (referencedEntityId == null) {
                referencedEntity = null;
            } else {
                BelongsToType belongsToFieldType = (BelongsToType) fieldDefinition.getType();
                InternalDataDefinition referencedDataDefinition = (InternalDataDefinition) belongsToFieldType.getDataDefinition();
                Class<?> referencedClass = referencedDataDefinition.getClassForEntity();
                referencedEntity = sessionFactory.getCurrentSession().load(referencedClass, referencedEntityId);
            }
        } else {
            referencedEntity = null;
        }

        return parseAndValidateValue(dataDefinition, fieldDefinition, referencedEntity, validatedEntity);
    }

    private Object parseAndValidateField(final DataDefinition dataDefinition, final FieldDefinition fieldDefinition,
            final Object value, final Entity validatedEntity) {
        if (fieldDefinition.getType() instanceof BelongsToEntityType) {
            return parseAndValidateBelongsToField(dataDefinition, fieldDefinition, trimAndNullIfEmpty(value), validatedEntity);
        } else {
            return parseAndValidateValue(dataDefinition, fieldDefinition, trimAndNullIfEmpty(value), validatedEntity);
        }
    }

    private Object trimAndNullIfEmpty(final Object value) {
        if (value instanceof String && !StringUtils.hasText((String) value)) {
            return null;
        }
        if (value instanceof String) {
            return ((String) value).trim();
        }
        return value;
    }

}
