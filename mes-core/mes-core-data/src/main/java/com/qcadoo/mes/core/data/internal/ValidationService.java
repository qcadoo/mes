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
public class ValidationService {

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
        if (fieldValue instanceof String && !StringUtils.hasText((String) fieldValue)) {
            fieldValue = null;
        }
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
            if (!fieldValidator.validate(dataDefinition, fieldDefinition, fieldValue, validationResults)) {
                return null;
            }
        }
        return fieldValue;
    }

    private Object parseAndValidateBelongsToField(final DataDefinition dataDefinition, final FieldDefinition fieldDefinition,
            final Object value, final ValidationResults validationResults) {
        if (value != null) {
            Long referencedEntityId = ((Entity) value).getId();
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
            return parseAndValidateBelongsToField(dataDefinition, fieldDefinition, value, validationResults);
        } else {
            return parseAndValidateValue(dataDefinition, fieldDefinition, value, validationResults);
        }
    }

}
