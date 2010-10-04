package com.qcadoo.mes.model.types.internal;

import com.qcadoo.mes.model.FieldDefinition;
import com.qcadoo.mes.model.types.EnumeratedType;
import com.qcadoo.mes.model.types.FieldType;
import com.qcadoo.mes.model.types.HasManyType;
import com.qcadoo.mes.model.types.LookupedType;
import com.qcadoo.mes.model.types.HasManyType.Cascade;

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

    FieldType hasManyType(String pluginIdentifier, final String entityName, final String fieldName, HasManyType.Cascade cascade);

}
