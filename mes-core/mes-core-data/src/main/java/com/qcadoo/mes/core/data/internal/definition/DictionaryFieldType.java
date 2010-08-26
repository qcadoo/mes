package com.qcadoo.mes.core.data.internal.definition;

import java.util.List;

import com.qcadoo.mes.core.data.api.DictionaryService;
import com.qcadoo.mes.core.data.definition.EnumeratedFieldType;
import com.qcadoo.mes.core.data.definition.FieldTypeFactory;
import com.qcadoo.mes.core.data.internal.ValidatableFieldType;

public final class DictionaryFieldType implements EnumeratedFieldType, ValidatableFieldType {

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
    public Class<?> getType() {
        return String.class;
    }

    @Override
    public String validateValue(final Object value) {
        if (!values().contains(value)) {
            return String.valueOf(value) + " must be one the " + values();
        }
        return null;
    }

    @Override
    public List<String> values() {
        return dictionaryService.values(dictionaryName);
    }

    @Override
    public int getNumericType() {
        return FieldTypeFactory.NUMERIC_TYPE_DICTIONARY;
    }

}
