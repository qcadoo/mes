package com.qcadoo.mes.core.types;

import com.qcadoo.mes.core.model.DataDefinition;

public interface BelongsToType extends LookupedFieldType {

    DataDefinition getDataDefinition();

    String getLookupFieldName();

}