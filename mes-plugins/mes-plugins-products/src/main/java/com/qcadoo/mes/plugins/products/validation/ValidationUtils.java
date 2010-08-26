package com.qcadoo.mes.plugins.products.validation;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.qcadoo.mes.core.data.beans.Entity;
import com.qcadoo.mes.core.data.definition.EnumeratedFieldType;
import com.qcadoo.mes.core.data.definition.FieldDefinition;
import com.qcadoo.mes.core.data.definition.FieldTypeFactory;

public class ValidationUtils {

    public static ValidationResult validateEntity(Entity entity, List<FieldDefinition> fields) {
        Entity validEntity = new Entity(entity.getId());
        Map<String, String> fieldMessages = new HashMap<String, String>();

        for (FieldDefinition fieldDefinition : fields) {
            Object fieldValue = entity.getField(fieldDefinition.getName());
            switch (fieldDefinition.getType().getNumericType()) {

                case FieldTypeFactory.NUMERIC_TYPE_BOOLEAN:
                    validEntity.setField(fieldDefinition.getName(), fieldValue); // to do
                    break;

                case FieldTypeFactory.NUMERIC_TYPE_DATE:
                    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    try {
                        Date date = dateFormat.parse((String) fieldValue);
                        validEntity.setField(fieldDefinition.getName(), date);
                    } catch (ParseException e) {
                        fieldMessages.put(fieldDefinition.getName(), "notDateValidateMessage");
                    }
                    break;

                case FieldTypeFactory.NUMERIC_TYPE_DATE_TIME:
                    DateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                    try {
                        Date date = dateTimeFormat.parse((String) fieldValue);
                        validEntity.setField(fieldDefinition.getName(), date);
                    } catch (ParseException e) {
                        fieldMessages.put(fieldDefinition.getName(), "notDateValidateMessage");
                    }
                    break;

                case FieldTypeFactory.NUMERIC_TYPE_DICTIONARY:
                case FieldTypeFactory.NUMERIC_TYPE_ENUM:
                    EnumeratedFieldType enumeratedField = (EnumeratedFieldType) fieldDefinition.getType();
                    List<String> options = enumeratedField.values();
                    if (!options.contains(fieldValue)) {
                        fieldMessages.put(fieldDefinition.getName(), "notInDictionaryValidateMessage");
                    } else {
                        validEntity.setField(fieldDefinition.getName(), (String) fieldValue);
                    }
                    break;

                case FieldTypeFactory.NUMERIC_TYPE_INTEGER:
                    try {
                        Integer intVal = Integer.parseInt((String) fieldValue);
                        validEntity.setField(fieldDefinition.getName(), intVal);
                    } catch (NumberFormatException e) {
                        fieldMessages.put(fieldDefinition.getName(), "notIntegerValidateMessage");
                    }
                    break;

                case FieldTypeFactory.NUMERIC_TYPE_DECIMAL:
                    try {
                        BigDecimal bd = new BigDecimal((String) fieldValue);
                        validEntity.setField(fieldDefinition.getName(), bd);
                    } catch (NumberFormatException e) {
                        fieldMessages.put(fieldDefinition.getName(), "notBigDecimalValidateMessage");
                    }
                    break;

                case FieldTypeFactory.NUMERIC_TYPE_STRING:
                    validEntity.setField(fieldDefinition.getName(), (String) fieldValue);
                    break;

                case FieldTypeFactory.NUMERIC_TYPE_TEXT:
                    validEntity.setField(fieldDefinition.getName(), (String) fieldValue);
                    break;

                case FieldTypeFactory.NUMERIC_TYPE_BELONGS_TO:
                    try {
                        Object ifFieldValue = entity.getField(fieldDefinition.getName() + ".id");
                        Long entityId = Long.parseLong((String) ifFieldValue);
                        validEntity.setField(fieldDefinition.getName(), new Entity(entityId));
                    } catch (NumberFormatException e) {
                        fieldMessages.put(fieldDefinition.getName(), "notIdValidateMessage");
                    }
                    break;
            }
        }
        if (fieldMessages.size() == 0) {
            return ValidationResultFactory.getInstance().createValidResult(validEntity);
        } else {
            return ValidationResultFactory.getInstance().createInvalidResult("wrongFieldTypesValidateMessage", fieldMessages);
        }
    }
}
