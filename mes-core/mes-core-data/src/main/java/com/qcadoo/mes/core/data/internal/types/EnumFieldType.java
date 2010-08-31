package com.qcadoo.mes.core.data.internal.types;

import java.util.Arrays;
import java.util.List;

import com.qcadoo.mes.core.data.definition.FieldDefinition;
import com.qcadoo.mes.core.data.types.EnumeratedFieldType;
import com.qcadoo.mes.core.data.types.FieldTypeFactory;
import com.qcadoo.mes.core.data.validation.ValidationResults;

public final class EnumFieldType implements EnumeratedFieldType {

    private final List<String> values;

    public EnumFieldType(final String... values) {
        this.values = Arrays.asList(values);
    }

    @Override
    public boolean isSearchable() {
        return true;
    }

    @Override
    public boolean isOrderable() {
        return true;
    }

    @Override
    public boolean isAggregable() {
        return false;
    }

    @Override
    public List<String> values() {
        return values;
    }

    @Override
    public int getNumericType() {
        return FieldTypeFactory.NUMERIC_TYPE_ENUM;
    }

    @Override
    public Class<?> getType() {
        return String.class;
    }

    @Override
    public Object fromString(FieldDefinition fieldDefinition, String value, ValidationResults validationResults) {
        return value;
    }

    @Override
    public boolean validate(FieldDefinition fieldDefinition, Object value, ValidationResults validationResults) {
        if (!values().contains(value)) {
            validationResults.addError(fieldDefinition, "form.validate.errors.invalidDictionaryItem", String.valueOf(values()));
            return false;
        }
        return true;
    }

}
