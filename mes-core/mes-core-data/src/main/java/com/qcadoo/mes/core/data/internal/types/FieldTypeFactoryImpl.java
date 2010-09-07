package com.qcadoo.mes.core.data.internal.types;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.core.data.api.DataAccessService;
import com.qcadoo.mes.core.data.api.DataDefinitionService;
import com.qcadoo.mes.core.data.api.DictionaryService;
import com.qcadoo.mes.core.data.definition.FieldDefinition;
import com.qcadoo.mes.core.data.types.EnumeratedFieldType;
import com.qcadoo.mes.core.data.types.FieldType;
import com.qcadoo.mes.core.data.types.FieldTypeFactory;
import com.qcadoo.mes.core.data.types.LookupedFieldType;

@Service
public final class FieldTypeFactoryImpl implements FieldTypeFactory {

    @Autowired
    private DictionaryService dictionaryService;

    @Autowired
    private DataAccessService dataAccessService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    private static final FieldType INTEGER_FIELD_TYPE = new NumericFieldType(10, 0);

    private static final FieldType DECIMAL_FIELD_TYPE = new NumericFieldType(10, 3);

    private static final FieldType STRING_FIELD_TYPE = new StringFieldType(255);

    private static final FieldType PASSWORD_FIELD_TYPE = new PasswordFieldType(255);

    private static final FieldType TEXT_FIELD_TYPE = new StringFieldType(2048);

    private static final FieldType BOOLEAN_FIELD_TYPE = new BooleanFieldType();

    private static final FieldType DATE_FIELD_TYPE = new DateFieldType(false);

    private static final FieldType DATE_TIME_FIELD_TYPE = new DateFieldType(true);

    @Override
    public FieldType booleanType() {
        return BOOLEAN_FIELD_TYPE;
    }

    @Override
    public FieldType stringType() {
        return STRING_FIELD_TYPE;
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
    public FieldType textType() {
        return TEXT_FIELD_TYPE;
    }

    @Override
    public FieldType passwordType() {
        return PASSWORD_FIELD_TYPE;
    }

    @Override
    public EnumeratedFieldType enumType(final String... values) {
        // TODO masz don't create new fieltType every time, use some cache
        return new EnumFieldType(values);
    }

    @Override
    public EnumeratedFieldType dictionaryType(final String dictionaryName) {
        // TODO masz don't create new fieltType every time, use some cache
        return new DictionaryFieldType(dictionaryName, dictionaryService);
    }

    @Override
    public LookupedFieldType lazyBelongsToType(final String entityName, final String lookupFieldName) {
        // TODO masz don't create new fieltType every time, use some cache
        return new BelongsToFieldType(dataDefinitionService.get(entityName), lookupFieldName, false, dataAccessService);
    }

    @Override
    public LookupedFieldType eagerBelongsToType(final String entityName, final String lookupFieldName) {
        // TODO masz don't create new fieltType every time, use some cache
        return new BelongsToFieldType(dataDefinitionService.get(entityName), lookupFieldName, true, dataAccessService);
    }

    @Override
    public FieldType priorityType(final FieldDefinition scopeFieldDefinition) {
        return new PriorityFieldType(scopeFieldDefinition);
    }

}
