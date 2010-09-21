package com.qcadoo.mes.core.data.view;

import java.util.Map;
import java.util.Set;

import com.qcadoo.mes.core.data.beans.Entity;

public interface ViewDefinition extends CastableComponent<Object> {

    String getName();

    String getPluginIdentifier();

    RootComponent getRoot();

    ViewEntity<Object> getValue(final Entity entity, final Map<String, Entity> selectedEntities,
            final ViewEntity<Object> globalViewEntity, final Set<String> pathsToUpdate);

}