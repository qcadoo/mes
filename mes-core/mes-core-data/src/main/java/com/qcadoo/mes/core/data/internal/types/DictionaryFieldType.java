package com.qcadoo.mes.core.data.internal.types;

import java.util.List;

import com.qcadoo.mes.core.data.api.DictionaryService;
import com.qcadoo.mes.core.data.definition.FieldDefinition;
import com.qcadoo.mes.core.data.types.EnumeratedFieldType;
import com.qcadoo.mes.core.data.types.FieldTypeFactory;
import com.qcadoo.mes.core.data.validation.ValidationResults;

public final class DictionaryFieldType implements EnumeratedFieldType {

    private final String dictionaryName;

    private final DictionaryService dictionaryService;

    public DictionaryFieldType(final String dictionaryName, final DictionaryService dictionaryService) {
        this.dictionaryName = dictionaryName;
        this.dictionaryService = dictionaryService;
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
        return dictionaryService.values(dictionaryName);
    }

    @Override
    public int getNumericType() {
        return FieldTypeFactory.NUMERIC_TYPE_DICTIONARY;
    }

    @Override
    public Class<?> getType() {
        return String.class;
    }

    @Override
    public Object fromString(final FieldDefinition fieldDefinition, final String value, final ValidationResults validationResults) {
        return value;
    }

    @Override
    public boolean validate(final FieldDefinition fieldDefinition, final Object value, final ValidationResults validationResults) {
        if (!values().contains(value)) {
            validationResults.addError(fieldDefinition, "form.validate.errors.invalidDictionaryItem", String.valueOf(values()));
            return false;
        }
        return true;
    }
}
