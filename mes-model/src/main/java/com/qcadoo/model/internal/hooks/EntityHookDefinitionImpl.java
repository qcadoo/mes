package com.qcadoo.model.internal.hooks;

import org.springframework.context.ApplicationContext;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.internal.api.EntityHookDefinition;

public final class EntityHookDefinitionImpl extends HookDefinitionImpl implements EntityHookDefinition {

    public EntityHookDefinitionImpl(final String className, final String methodName, final ApplicationContext applicationContext) {
        super(className, methodName, applicationContext);
    }

    @Override
    public boolean call(final Entity entity) {
        return call(getDataDefinition(), entity);
    }

    @Override
    public Class<?>[] getParameterTypes() {
        return new Class[] { DataDefinition.class, Entity.class };
    }

}
