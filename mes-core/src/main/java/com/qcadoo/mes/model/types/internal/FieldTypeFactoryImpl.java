/**
 * ********************************************************************
 * Code developed by amazing QCADOO developers team.
 * Copyright (c) Qcadoo Limited sp. z o.o. (2010)
 * ********************************************************************
 */

package com.qcadoo.mes.model.types.internal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.encoding.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.api.DataDefinitionService;
import com.qcadoo.mes.api.DictionaryService;
import com.qcadoo.mes.model.FieldDefinition;
import com.qcadoo.mes.model.types.EnumeratedType;
import com.qcadoo.mes.model.types.FieldType;
import com.qcadoo.mes.model.types.HasManyType;
import com.qcadoo.mes.model.types.LookupedType;

@Service
public final class FieldTypeFactoryImpl implements FieldTypeFactory {

    @Autowired
    private DictionaryService dictionaryService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final FieldType INTEGER_FIELD_TYPE = new IntegerType();

    private static final FieldType DECIMAL_FIELD_TYPE = new DecimalType();

    private static final FieldType STRING_FIELD_TYPE = new StringType();

    private static final FieldType TEXT_FIELD_TYPE = new TextType();

    private static final FieldType BOOLEAN_FIELD_TYPE = new BooleanType();

    private static final FieldType DATE_FIELD_TYPE = new DateType();

    private static final FieldType DATE_TIME_FIELD_TYPE = new DateTimeType();

    @Override
    public FieldType booleanType() {
        return BOOLEAN_FIELD_TYPE;
    }

    @Override
    public FieldType stringType() {
        return STRING_FIELD_TYPE;
    }

    @Override
    public FieldType textType() {
        return TEXT_FIELD_TYPE;
    }

    @Override
    public FieldType integerType() {
        return INTEGER_FIELD_TYPE;
    }

    @Override
    public FieldType decimalType() {
        return DECIMAL_FIELD_TYPE;
    }

    @Override
    public FieldType dateType() {
        return DATE_FIELD_TYPE;
    }

    @Override
    public FieldType dateTimeType() {
        return DATE_TIME_FIELD_TYPE;
    }

    @Override
    public FieldType passwordType() {
        return new PasswordType(passwordEncoder);
    }

    @Override
    public EnumeratedType enumType(final String... values) {
        return new EnumType(values);
    }

    @Override
    public EnumeratedType dictionaryType(final String dictionaryName) {
        return new DictionaryType(dictionaryName, dictionaryService);
    }

    @Override
    public LookupedType lazyBelongsToType(final String pluginIdentifier, final String entityName, final String lookupFieldName) {
        return new BelongsToEntityType(pluginIdentifier, entityName, lookupFieldName, dataDefinitionService, true);
    }

    @Override
    public LookupedType eagerBelongsToType(final String pluginIdentifier, final String entityName, final String lookupFieldName) {
        return new BelongsToEntityType(pluginIdentifier, entityName, lookupFieldName, dataDefinitionService, false);
    }

    @Override
    public FieldType priorityType(final FieldDefinition scopeFieldDefinition) {
        return new PriorityType(scopeFieldDefinition);
    }

    @Override
    public FieldType hasManyType(final String pluginIdentifier, final String entityName, final String fieldName,
            final HasManyType.Cascade cascade) {
        return new HasManyEntitiesType(pluginIdentifier, entityName, fieldName, cascade, dataDefinitionService);
    }
}
