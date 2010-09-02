package com.qcadoo.mes.core.data.internal;

import java.util.Map.Entry;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.qcadoo.mes.core.data.api.DataDefinitionService;
import com.qcadoo.mes.core.data.beans.Entity;
import com.qcadoo.mes.core.data.definition.DataDefinition;
import com.qcadoo.mes.core.data.definition.FieldDefinition;
import com.qcadoo.mes.core.data.internal.types.BelongsToFieldType;
import com.qcadoo.mes.core.data.validation.EntityValidator;
import com.qcadoo.mes.core.data.validation.FieldValidator;
import com.qcadoo.mes.core.data.validation.ValidationResults;

@Service
public final class ValidationService {

    @Autowired
    private SessionFactory sessionFactory;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public Entity parseAndValidateEntity(final DataDefinition dataDefinition, final Entity genericEntity,
            final ValidationResults validationResults) {
        Entity validatedEntity = new Entity(genericEntity.getId());

        for (Entry<String, FieldDefinition> fieldDefinitionEntry : dataDefinition.getFields().entrySet()) {
            Object validateFieldValue = parseAndValidateField(dataDefinition, fieldDefinitionEntry.getValue(),
                    genericEntity.getField(fieldDefinitionEntry.getKey()), validationResults);
            validatedEntity.setField(fieldDefinitionEntry.getKey(), validateFieldValue);
        }

        for (Entry<String, FieldDefinition> fieldDefinitionEntry : dataDefinition.getFields().entrySet()) {
            if (validationResults.isFieldNotValid(fieldDefinitionEntry.getValue())) {
                continue;
            }
            for (FieldValidator fieldValidator : fieldDefinitionEntry.getValue().getValidators()) {
                fieldValidator.validate(dataDefinition, fieldDefinitionEntry.getValue(), validatedEntity, validationResults);
                if (validationResults.isFieldNotValid(fieldDefinitionEntry.getValue())) {
                    break;
                }
            }
        }

        if (validationResults.isValid()) {
            for (EntityValidator entityValidator : dataDefinition.getValidators()) {
                entityValidator.validate(dataDefinition, validatedEntity, validationResults);
            }
        }
        return validatedEntity;
    }

    private Object parseAndValidateValue(final DataDefinition dataDefinition, final FieldDefinition fieldDefinition,
            final Object value, final ValidationResults validationResults) {
        Object fieldValue = value;
        if (fieldValue != null) {
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
            if (!fieldValidator.validate(dataDefinition, fieldDefinition, fieldValue, validationResults)) {
                return null;
            }
        }
        return fieldValue;
    }

    private Object parseAndValidateBelongsToField(final DataDefinition dataDefinition, final FieldDefinition fieldDefinition,
            final Object value, final ValidationResults validationResults) {
        if (value != null) {
            Long referencedEntityId = null;
            if (value instanceof String) {
                try {
                    referencedEntityId = Long.valueOf((String) value);
                } catch (NumberFormatException e) {
                    validationResults.addError(fieldDefinition, "core.validation.error.wrongType", value.getClass()
                            .getSimpleName(), fieldDefinition.getType().getType().getSimpleName());
                }
            } else if (value instanceof Long) {
                referencedEntityId = (Long) value;
            } else if (value instanceof Entity) {
                referencedEntityId = ((Entity) value).getId();
            } else {
                validationResults.addError(fieldDefinition, "core.validation.error.wrongType", value.getClass().getSimpleName(),
                        fieldDefinition.getType().getType().getSimpleName());
                return null;
            }
            BelongsToFieldType belongsToFieldType = (BelongsToFieldType) fieldDefinition.getType();
            DataDefinition referencedDataDefinition = dataDefinitionService.get(belongsToFieldType.getEntityName());
            Class<?> referencedClass = referencedDataDefinition.getClassForEntity();
            Object referencedEntity = sessionFactory.getCurrentSession().get(referencedClass, referencedEntityId);
            return parseAndValidateValue(dataDefinition, fieldDefinition, referencedEntity, validationResults);
        } else {
            return null;
        }
    }

    private Object parseAndValidateField(final DataDefinition dataDefinition, final FieldDefinition fieldDefinition,
            final Object value, final ValidationResults validationResults) {
        if (fieldDefinition.isCustomField()) {
            throw new UnsupportedOperationException("custom fields are not supported");
        } else if (fieldDefinition.getType() instanceof BelongsToFieldType) {
            return parseAndValidateBelongsToField(dataDefinition, fieldDefinition, trimAndNullIfEmpty(value), validationResults);
        } else {
            return parseAndValidateValue(dataDefinition, fieldDefinition, trimAndNullIfEmpty(value), validationResults);
        }
    }

    private Object trimAndNullIfEmpty(final Object value) {
        if (value instanceof String && !StringUtils.hasText((String) value)) {
            return null;
        }
        if (value != null) {
            if (value instanceof String) {
                return ((String) value).trim();
            }
        }
        return value;
    }

}
