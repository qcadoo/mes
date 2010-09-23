package com.qcadoo.mes.core.data.types;

import com.qcadoo.mes.core.data.model.FieldDefinition;

public interface FieldTypeFactory {

    FieldType booleanType();

    FieldType stringType();

    FieldType textType();

    FieldType integerType();

    FieldType decimalType();

    FieldType dateType();

    FieldType dateTimeType();

    FieldType passwordType();

    FieldType priorityType(final FieldDefinition scopeFieldDefinition);

    EnumeratedFieldType enumType(final String... values);

    EnumeratedFieldType dictionaryType(final String dictionaryName);

    LookupedFieldType lazyBelongsToType(String pluginIdentifier, final String entityName, final String lookupFieldName);

    LookupedFieldType eagerBelongsToType(String pluginIdentifier, final String entityName, final String lookupFieldName);

    FieldType hasManyType(String pluginIdentifier, final String entityName, final String fieldName);

}
