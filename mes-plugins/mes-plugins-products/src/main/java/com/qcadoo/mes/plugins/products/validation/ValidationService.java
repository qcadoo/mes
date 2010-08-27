package com.qcadoo.mes.plugins.products.validation;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.core.data.beans.Entity;
import com.qcadoo.mes.core.data.definition.EnumeratedFieldType;
import com.qcadoo.mes.core.data.definition.FieldDefinition;
import com.qcadoo.mes.core.data.definition.FieldTypeFactory;

@Service
public class ValidationService {

    public ValidationResult validateEntity(Entity entity, List<FieldDefinition> fields) {
        ValidationResult validationResult = translateEntity(entity, fields);
        if (validationResult.isValid()) {
            validationResult = validateRequiredFields(validationResult.getValidEntity(), fields);
        }
        return validationResult;
    }

    public ValidationResult translateEntity(Entity entity, List<FieldDefinition> fields) {
        Entity validEntity = new Entity(entity.getId());
        Map<String, String> fieldMessages = new HashMap<String, String>();
        for (FieldDefinition fieldDefinition : fields) {
            Object fieldValue = entity.getField(fieldDefinition.getName());
            String fieldValueStr = (String) fieldValue;
            if (fieldValueStr == null || "".equals(fieldValueStr.trim())) {
                validEntity.setField(fieldDefinition.getName(), null);
                continue;
            }
            switch (fieldDefinition.getType().getNumericType()) {

                case FieldTypeFactory.NUMERIC_TYPE_BOOLEAN:
                    validEntity.setField(fieldDefinition.getName(), fieldValue); // TODO mina add boolean conversion
                    break;

                case FieldTypeFactory.NUMERIC_TYPE_DATE:
                    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    try {
                        Date date = dateFormat.parse((String) fieldValue);
                        validEntity.setField(fieldDefinition.getName(), date);
                    } catch (ParseException e) {
                        fieldMessages.put(fieldDefinition.getName(), "form.validate.notDateValidateMessage");
                    }
                    break;

                case FieldTypeFactory.NUMERIC_TYPE_DATE_TIME:
                    DateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                    try {
                        Date date = dateTimeFormat.parse((String) fieldValue);
                        validEntity.setField(fieldDefinition.getName(), date);
                    } catch (ParseException e) {
                        fieldMessages.put(fieldDefinition.getName(), "form.validate.notDateTimeValidateMessage");
                    }
                    break;

                case FieldTypeFactory.NUMERIC_TYPE_DICTIONARY:
                case FieldTypeFactory.NUMERIC_TYPE_ENUM:
                    EnumeratedFieldType enumeratedField = (EnumeratedFieldType) fieldDefinition.getType();
                    List<String> options = enumeratedField.values();
                    if (!options.contains(fieldValue)) {
                        fieldMessages.put(fieldDefinition.getName(), "form.validate.notInDictionaryValidateMessage");
                    } else {
                        validEntity.setField(fieldDefinition.getName(), (String) fieldValue);
                    }
                    break;

                case FieldTypeFactory.NUMERIC_TYPE_INTEGER:
                    try {
                        Integer intVal = Integer.parseInt((String) fieldValue);
                        validEntity.setField(fieldDefinition.getName(), intVal);
                    } catch (NumberFormatException e) {
                        fieldMessages.put(fieldDefinition.getName(), "form.validate.notIntegerValidateMessage");
                    }
                    break;

                case FieldTypeFactory.NUMERIC_TYPE_DECIMAL:
                    try {
                        BigDecimal bd = new BigDecimal((String) fieldValue);
                        validEntity.setField(fieldDefinition.getName(), bd);
                    } catch (NumberFormatException e) {
                        fieldMessages.put(fieldDefinition.getName(), "form.validate.notBigDecimalValidateMessage");
                    }
                    break;

                case FieldTypeFactory.NUMERIC_TYPE_TEXT:
                case FieldTypeFactory.NUMERIC_TYPE_STRING:
                    validEntity.setField(fieldDefinition.getName(), (String) fieldValue);
                    break;

                case FieldTypeFactory.NUMERIC_TYPE_BELONGS_TO:
                    try {
                        Object ifFieldValue = entity.getField(fieldDefinition.getName());
                        Long entityId = Long.parseLong((String) ifFieldValue);
                        validEntity.setField(fieldDefinition.getName(), new Entity(entityId));
                    } catch (NumberFormatException e) {
                        fieldMessages.put(fieldDefinition.getName(), "form.validate.notIdValidateMessage");
                    }
                    break;
            }
        }
        if (fieldMessages.size() == 0) {
            return ValidationResultFactory.createValidResult(validEntity);
        } else {
            return ValidationResultFactory.createInvalidResult("form.validate.wrongFieldTypesValidateMessage", fieldMessages);
        }
    }

    public ValidationResult validateRequiredFields(Entity entity, List<FieldDefinition> fields) {
        Map<String, String> fieldMessages = new HashMap<String, String>();
        for (FieldDefinition fieldDefinition : fields) {
            if (fieldDefinition.isRequired()) {
                if (entity.getField(fieldDefinition.getName()) == null) {
                    fieldMessages.put(fieldDefinition.getName(), "form.validate.nullFieldValidateMessage");
                }
            }
        }
        if (fieldMessages.size() == 0) {
            return ValidationResultFactory.createValidResult(entity);
        } else {
            return ValidationResultFactory.createInvalidResult("form.validate.nullMandatoryFieldsValidateMessage", fieldMessages);
        }
    }
}
