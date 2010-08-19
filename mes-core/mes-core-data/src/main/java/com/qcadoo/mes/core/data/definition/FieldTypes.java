package com.qcadoo.mes.core.data.definition;

import com.qcadoo.mes.core.data.internal.definition.BooleanFieldType;
import com.qcadoo.mes.core.data.internal.definition.DateFieldType;
import com.qcadoo.mes.core.data.internal.definition.EnumFieldType;
import com.qcadoo.mes.core.data.internal.definition.NumericFieldType;
import com.qcadoo.mes.core.data.internal.definition.StringFieldType;
import com.qcadoo.mes.core.data.internal.definition.TextFieldType;

/**
 * @apiviz.uses com.qcadoo.mes.core.data.definition.FieldType
 */
public final class FieldTypes {

    private static final FieldType INTEGER_FIELD_TYPE = new NumericFieldType(10, 0);

    private static final FieldType DECIMAL_FIELD_TYPE = new NumericFieldType(8, 2);

    private static final FieldType STRING_FIELD_TYPE = new StringFieldType();

    private static final FieldType TEXT_FIELD_TYPE = new TextFieldType();

    private static final FieldType BOOLEAN_FIELD_TYPE = new BooleanFieldType();

    private static final FieldType DATE_FIELD_TYPE = new DateFieldType();

    private FieldTypes() {
    }

    public static FieldType booleanType() {
        return BOOLEAN_FIELD_TYPE;
    }

    public static FieldType stringType() {
        return STRING_FIELD_TYPE;
    }

    public static FieldType integerType() {
        return INTEGER_FIELD_TYPE;
    }

    public static FieldType decimalType() {
        return DECIMAL_FIELD_TYPE;
    }

    public static FieldType dateType() {
        return DATE_FIELD_TYPE;
    }

    public static FieldType textType() {
        return TEXT_FIELD_TYPE;
    }

    public static FieldType enumType(String... values) {
        return new EnumFieldType(values);
    }

}
