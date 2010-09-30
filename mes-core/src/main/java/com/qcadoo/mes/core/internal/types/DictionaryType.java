package com.qcadoo.mes.core.internal.types;

import java.util.List;

import com.qcadoo.mes.core.api.DictionaryService;
import com.qcadoo.mes.core.api.Entity;
import com.qcadoo.mes.model.FieldDefinition;
import com.qcadoo.mes.model.types.EnumeratedType;

public final class DictionaryType implements EnumeratedType {

    private final String dictionaryName;

    private final DictionaryService dictionaryService;

    public DictionaryType(final String dictionaryName, final DictionaryService dictionaryService) {
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
    public Class<?> getType() {
        return String.class;
    }

    @Override
    public Object toObject(final FieldDefinition fieldDefinition, final Object value, final Entity validatedEntity) {
        String stringValue = String.valueOf(value);
        if (!values().contains(stringValue)) {
            validatedEntity.addError(fieldDefinition, "commons.validate.field.error.invalidDictionaryItem",
                    String.valueOf(values()));
            return null;
        }
        return stringValue;
    }

    @Override
    public String toString(final Object value) {
        return String.valueOf(value);
    }

}
