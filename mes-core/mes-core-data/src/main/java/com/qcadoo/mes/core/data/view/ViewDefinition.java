package com.qcadoo.mes.core.data.view;

import java.util.Map;
import java.util.Set;

import com.qcadoo.mes.core.data.beans.Entity;

public interface ViewDefinition extends CastableComponent<Object> {

    String getName();

    String getPluginIdentifier();

    RootComponent getRoot();

    ViewValue<Object> getValue(final Entity entity, final Map<String, Entity> selectedEntities,
            final ViewValue<Object> globalViewEntity, final Set<String> pathsToUpdate);

}