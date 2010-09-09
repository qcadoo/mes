package com.qcadoo.mes.core.data.internal.types;

import org.apache.commons.lang.StringUtils;

import com.qcadoo.mes.core.data.definition.DataFieldDefinition;
import com.qcadoo.mes.core.data.types.FieldType;
import com.qcadoo.mes.core.data.validation.ValidationResults;

public final class TextType implements FieldType {

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
    public Class<?> getType() {
        return String.class;
    }

    @Override
    public Object toObject(final DataFieldDefinition fieldDefinition, final Object value,
            final ValidationResults validationResults) {
        String stringValue = String.valueOf(value);
        if (StringUtils.length(stringValue) > 2048) {
            validationResults.addError(fieldDefinition, "commons.validate.field.error.stringIsTooLong", String.valueOf(2048));
            return null;
        }
        return stringValue;
    }

    @Override
    public String toString(final Object value) {
        return String.valueOf(value);
    }

}
