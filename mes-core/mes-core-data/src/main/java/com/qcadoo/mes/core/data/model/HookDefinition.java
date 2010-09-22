package com.qcadoo.mes.core.data.model;

import com.qcadoo.mes.core.data.beans.Entity;
import com.qcadoo.mes.core.data.view.ViewValue;

public interface HookDefinition {

    boolean callWithObjectAndGetBoolean(final DataDefinition dataDefinition, final Object value);

    boolean callWithEntityAndGetBoolean(final DataDefinition dataDefinition, final Entity entity);

    void callWithEntity(final DataDefinition dataDefinition, final Entity entity);

    void callWithViewValue(ViewValue<Object> value);

}