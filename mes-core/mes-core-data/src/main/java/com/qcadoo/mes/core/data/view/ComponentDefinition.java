package com.qcadoo.mes.core.data.view;

import java.util.Map;
import java.util.Set;

import com.qcadoo.mes.core.data.beans.Entity;
import com.qcadoo.mes.core.data.model.DataDefinition;

public interface ComponentDefinition<T> extends CastableComponent<T>, ListenableComponent, InitializableComponent {

    String getType();

    ViewEntity<T> getValue(Entity entity, Map<String, Entity> selectedEntities, ViewEntity<?> viewEntity,
            Set<String> pathsToUpdate);

    String getName();

    String getSourceFieldPath();

    String getPath();

    String getFieldPath();

    DataDefinition getDataDefinition();

}