package com.qcadoo.mes.core.data.view;

import java.util.Map;
import java.util.Set;

import com.qcadoo.mes.core.data.beans.Entity;
import com.qcadoo.mes.core.data.internal.view.RootContainerDefinitionImpl;

public interface ViewDefinition extends CastableComponent<Object> {

    String getPluginIdentifier();

    RootContainerDefinitionImpl getRoot();

    String getName();

    ViewEntity<Object> getValue(final Entity entity, final Map<String, Entity> selectedEntities,
            final ViewEntity<Object> globalViewEntity, final Set<String> pathsToUpdate);

}