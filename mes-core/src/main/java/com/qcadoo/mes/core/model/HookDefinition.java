package com.qcadoo.mes.core.model;

import com.qcadoo.mes.core.api.Entity;
import com.qcadoo.mes.core.view.ViewValue;

public interface HookDefinition {

    boolean callWithObjectAndGetBoolean(final DataDefinition dataDefinition, final Object value);

    boolean callWithEntityAndGetBoolean(final DataDefinition dataDefinition, final Entity entity);

    void callWithEntity(final DataDefinition dataDefinition, final Entity entity);

    void callWithViewValue(ViewValue<Object> value, String triggerComponentName);

}
