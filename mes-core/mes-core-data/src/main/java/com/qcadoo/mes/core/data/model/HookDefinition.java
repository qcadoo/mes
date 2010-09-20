package com.qcadoo.mes.core.data.model;

import com.qcadoo.mes.core.data.beans.Entity;

public interface HookDefinition {

    public abstract boolean callWithObjectAndGetBoolean(final ModelDefinition dataDefinition, final Object value);

    public abstract boolean callWithEntityAndGetBoolean(final ModelDefinition dataDefinition, final Entity entity);

    public abstract void callWithEntity(final ModelDefinition dataDefinition, final Entity entity);

}