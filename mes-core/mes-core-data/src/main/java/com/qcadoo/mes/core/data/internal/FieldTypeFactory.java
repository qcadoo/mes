package com.qcadoo.mes.core.data.internal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.core.data.api.DictionaryService;
import com.qcadoo.mes.core.data.definition.EnumeratedFieldType;
import com.qcadoo.mes.core.data.definition.FieldType;
import com.qcadoo.mes.core.data.internal.definition.BooleanFieldType;
import com.qcadoo.mes.core.data.internal.definition.DateFieldType;
import com.qcadoo.mes.core.data.internal.definition.DateTimeFieldType;
import com.qcadoo.mes.core.data.internal.definition.DictionaryFieldType;
import com.qcadoo.mes.core.data.internal.definition.EnumFieldType;
import com.qcadoo.mes.core.data.internal.definition.NumericFieldType;
import com.qcadoo.mes.core.data.internal.definition.StringFieldType;
import com.qcadoo.mes.core.data.internal.definition.TextFieldType;

@Service
public final class FieldTypeFactory {

    @Autowired
    private DictionaryService dictionaryService;

    private static final FieldType INTEGER_FIELD_TYPE = new NumericFieldType(10, 0);

    private static final FieldType DECIMAL_FIELD_TYPE = new NumericFieldType(7, 3);

    private static final FieldType STRING_FIELD_TYPE = new StringFieldType();

    private static final FieldType TEXT_FIELD_TYPE = new TextFieldType();

    private static final FieldType BOOLEAN_FIELD_TYPE = new BooleanFieldType();

    private static final FieldType DATE_FIELD_TYPE = new DateFieldType();

    private static final FieldType DATE_TIME_FIELD_TYPE = new DateTimeFieldType();

    public FieldType booleanType() {
        return BOOLEAN_FIELD_TYPE;
    }

    public FieldType stringType() {
        return STRING_FIELD_TYPE;
    }

    public FieldType integerType() {
        return INTEGER_FIELD_TYPE;
    }

    public FieldType decimalType() {
        return DECIMAL_FIELD_TYPE;
    }

    public FieldType dateType() {
        return DATE_FIELD_TYPE;
    }

    public FieldType dateTimeType() {
        return DATE_TIME_FIELD_TYPE;
    }

    public FieldType textType() {
        return TEXT_FIELD_TYPE;
    }

    public EnumeratedFieldType enumType(final String... values) {
        // TODO masz don't create new fieltType every time, use some cache
        return new EnumFieldType(values);
    }

    public EnumeratedFieldType dictionaryType(final String dictionaryName) {
        // TODO masz don't create new fieltType every time, use some cache
        return new DictionaryFieldType(dictionaryName, dictionaryService);
    }

}
