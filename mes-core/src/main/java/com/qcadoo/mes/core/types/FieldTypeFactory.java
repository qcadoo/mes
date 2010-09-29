package com.qcadoo.mes.core.types;

import com.qcadoo.mes.core.model.FieldDefinition;

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

    EnumeratedType enumType(final String... values);

    EnumeratedType dictionaryType(final String dictionaryName);

    LookupedType lazyBelongsToType(String pluginIdentifier, final String entityName, final String lookupFieldName);

    LookupedType eagerBelongsToType(String pluginIdentifier, final String entityName, final String lookupFieldName);

    FieldType hasManyType(String pluginIdentifier, final String entityName, final String fieldName);

}
