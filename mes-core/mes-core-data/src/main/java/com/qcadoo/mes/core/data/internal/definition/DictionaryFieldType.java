package com.qcadoo.mes.core.data.internal.definition;

import java.util.List;

import com.qcadoo.mes.core.data.api.DictionaryService;
import com.qcadoo.mes.core.data.definition.EnumeratedFieldType;

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
    public boolean isValidType(final Object value) {
        if (!(value instanceof String)) {
            return false;
        }
        if (!values().contains(value)) {
            return false;
        }
        return true;
    }

    @Override
    public List<String> values() {
        return dictionaryService.values(dictionaryName);
    }

    @Override
    public int getNumericType() {
        return NUMERIC_TYPE_DICTIONARY;
    }

}
