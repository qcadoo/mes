package com.qcadoo.mes.core.data.view;

import java.util.Map;
import java.util.Set;

import com.qcadoo.mes.core.data.beans.Entity;
import com.qcadoo.mes.core.data.model.ModelDefinition;

public interface ComponentDefinition<T> extends CastableComponent<T>, ListenableComponent, InitializableComponent {

    String getType();

    ViewEntity<T> getValue(final Entity entity, final Map<String, Entity> selectedEntities, final ViewEntity<?> viewEntity,
            final Set<String> pathsToUpdate);

    String getName();

    String getSourceFieldPath();

    String getPath();

    String getFieldPath();

    ModelDefinition getModelDefinition();

}