package com.qcadoo.mes.core.data.types;

public interface FieldTypeFactory {

    int NUMERIC_TYPE_BOOLEAN = 1;

    int NUMERIC_TYPE_DATE = 2;

    int NUMERIC_TYPE_DATE_TIME = 3;

    int NUMERIC_TYPE_DICTIONARY = 4;

    int NUMERIC_TYPE_ENUM = 5;

    int NUMERIC_TYPE_INTEGER = 6;

    int NUMERIC_TYPE_DECIMAL = 7;

    int NUMERIC_TYPE_STRING = 8;

    int NUMERIC_TYPE_TEXT = 9;

    int NUMERIC_TYPE_BELONGS_TO = 10;

    int NUMERIC_TYPE_PASSWORD = 11;

    FieldType booleanType();

    FieldType stringType();

    FieldType integerType();

    FieldType decimalType();

    FieldType dateType();

    FieldType dateTimeType();

    FieldType textType();

    FieldType passwordType();

    EnumeratedFieldType enumType(final String... values);

    EnumeratedFieldType dictionaryType(final String dictionaryName);

    LookupedFieldType lazyBelongsToType(final String entityName, final String lookupFieldName);

    LookupedFieldType eagerBelongsToType(final String entityName, final String lookupFieldName);
}
