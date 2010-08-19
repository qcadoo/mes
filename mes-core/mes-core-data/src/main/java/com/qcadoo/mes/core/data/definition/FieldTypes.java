package com.qcadoo.mes.core.data.definition;

import com.qcadoo.mes.core.data.internal.definition.BooleanFieldType;
import com.qcadoo.mes.core.data.internal.definition.IntFieldType;
import com.qcadoo.mes.core.data.internal.definition.StringFieldType;

/**
 * @apiviz.uses com.qcadoo.mes.core.data.definition.FieldType
 */
public final class FieldTypes {

    private FieldTypes() {
    }

    public static FieldType booleanType() {
        return new BooleanFieldType();
    }

    public static FieldType stringType() {
        return new StringFieldType();
    }

    public static FieldType intType() {
        return new IntFieldType();
    }

}
