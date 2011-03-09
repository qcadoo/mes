package com.qcadoo.model.internal.hooks;

import org.springframework.context.ApplicationContext;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.FieldDefinition;
import com.qcadoo.model.internal.api.FieldHookDefinition;

public final class FieldHookDefinitionImpl extends HookDefinitionImpl implements FieldHookDefinition {

    public FieldHookDefinitionImpl(final String className, final String methodName, final ApplicationContext applicationContext) {
        super(className, methodName, applicationContext);
    }

    @Override
    public boolean call(final Entity entity, final Object oldValue, final Object newValue) {
        return call(getDataDefinition(), getFieldDefinition(), entity, oldValue, newValue);
    }

    @Override
    public Class<?>[] getParameterTypes() {
        return new Class[] { DataDefinition.class, FieldDefinition.class, Entity.class, Object.class, Object.class };
    }

}
