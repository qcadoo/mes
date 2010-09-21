package com.qcadoo.mes.core.data.internal;

import java.util.Map;
import java.util.Map.Entry;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.qcadoo.mes.core.data.beans.Entity;
import com.qcadoo.mes.core.data.internal.types.BelongsToType;
import com.qcadoo.mes.core.data.model.ModelDefinition;
import com.qcadoo.mes.core.data.model.FieldDefinition;
import com.qcadoo.mes.core.data.validation.EntityValidator;
import com.qcadoo.mes.core.data.validation.FieldValidator;

@Service
public final class ValidationService {

    @Autowired
    private SessionFactory sessionFactory;

    public void validateGenericEntity(final ModelDefinition dataDefinition, final Entity genericEntity,
            final Entity existingGenericEntity) {
        parseAndValidateEntity(dataDefinition, genericEntity);

        nullifyReadOnlyFields(dataDefinition, genericEntity, existingGenericEntity);

        if (genericEntity.getId() != null) {
            dataDefinition.callOnUpdate(genericEntity);
        } else {
            dataDefinition.callOnCreate(genericEntity);
        }
    }

    private void nullifyReadOnlyFields(final ModelDefinition dataDefinition, final Entity genericEntity,
            final Entity existingGenericEntity) {
        for (Map.Entry<String, FieldDefinition> field : dataDefinition.getFields().entrySet()) {
            if (field.getValue().isReadOnly()) {
                genericEntity.setField(field.getKey(),
                        existingGenericEntity != null ? existingGenericEntity.getField(field.getKey()) : null);
            }
            if (field.getValue().isReadOnlyOnUpdate() && genericEntity.getId() != null) {
                genericEntity.setField(field.getKey(), existingGenericEntity.getField(field.getKey()));
            }
        }
    }

    private void parseAndValidateEntity(final ModelDefinition dataDefinition, final Entity genericEntity) {
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

    private Object parseAndValidateValue(final ModelDefinition dataDefinition, final FieldDefinition fieldDefinition,
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

    private Object parseAndValidateBelongsToField(final ModelDefinition dataDefinition, final FieldDefinition fieldDefinition,
            final Object value, final Entity validatedEntity) {
        if (value != null) {
            Long referencedEntityId = null;
            if (value instanceof String) {
                try {
                    referencedEntityId = Long.valueOf((String) value);
                } catch (NumberFormatException e) {
                    validatedEntity.addError(fieldDefinition, "commons.validate.field.error.wrongType", value.getClass()
                            .getSimpleName(), fieldDefinition.getType().getType().getSimpleName());
                }
            } else if (value instanceof Long) {
                referencedEntityId = (Long) value;
            } else if (value instanceof Entity) {
                referencedEntityId = ((Entity) value).getId();
            } else {
                validatedEntity.addError(fieldDefinition, "commons.validate.field.error.wrongType", value.getClass()
                        .getSimpleName(), fieldDefinition.getType().getType().getSimpleName());
                return null;
            }
            BelongsToType belongsToFieldType = (BelongsToType) fieldDefinition.getType();
            ModelDefinition referencedDataDefinition = belongsToFieldType.getDataDefinition();
            Class<?> referencedClass = referencedDataDefinition.getClassForEntity();
            Object referencedEntity = sessionFactory.getCurrentSession().get(referencedClass, referencedEntityId);
            return parseAndValidateValue(dataDefinition, fieldDefinition, referencedEntity, validatedEntity);
        } else {
            return null;
        }
    }

    private Object parseAndValidateField(final ModelDefinition dataDefinition, final FieldDefinition fieldDefinition,
            final Object value, final Entity validatedEntity) {
        if (fieldDefinition.isCustomField()) {
            throw new UnsupportedOperationException("custom fields are not supported");
        } else if (fieldDefinition.getType() instanceof BelongsToType) {
            return parseAndValidateBelongsToField(dataDefinition, fieldDefinition, trimAndNullIfEmpty(value), validatedEntity);
        } else {
            return parseAndValidateValue(dataDefinition, fieldDefinition, trimAndNullIfEmpty(value), validatedEntity);
        }
    }

    private Object trimAndNullIfEmpty(final Object value) {
        if (value instanceof String && !StringUtils.hasText((String) value)) {
            return null;
        }
        if (value != null && value instanceof String) {
            return ((String) value).trim();
        }
        return value;
    }

}
