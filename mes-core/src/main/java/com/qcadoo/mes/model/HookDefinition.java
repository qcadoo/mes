package com.qcadoo.mes.model;

import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.view.ViewValue;

public interface HookDefinition {

    boolean callWithObjectAndGetBoolean(final DataDefinition dataDefinition, final Object value);

    boolean callWithEntityAndGetBoolean(final DataDefinition dataDefinition, final Entity entity);

    void callWithEntity(final DataDefinition dataDefinition, final Entity entity);

    void callWithViewValue(ViewValue<Object> value, String triggerComponentName);

}
