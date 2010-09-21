package com.qcadoo.mes.core.data.model;

import com.qcadoo.mes.core.data.beans.Entity;

public interface HookDefinition {

    public abstract boolean callWithObjectAndGetBoolean(final DataDefinition dataDefinition, final Object value);

    public abstract boolean callWithEntityAndGetBoolean(final DataDefinition dataDefinition, final Entity entity);

    public abstract void callWithEntity(final DataDefinition dataDefinition, final Entity entity);

}