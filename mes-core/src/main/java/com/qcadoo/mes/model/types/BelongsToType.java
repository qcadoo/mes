package com.qcadoo.mes.model.types;

import com.qcadoo.mes.model.DataDefinition;

public interface BelongsToType extends LookupedType {

    DataDefinition getDataDefinition();

    String getLookupFieldName();

    boolean isLazyLoading();

}