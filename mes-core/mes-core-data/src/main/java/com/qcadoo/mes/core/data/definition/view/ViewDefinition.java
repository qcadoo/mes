package com.qcadoo.mes.core.data.definition.view;

import java.util.Map;

import com.qcadoo.mes.core.data.beans.Entity;

public class ViewDefinition {

    private final RootContainerDefinition root;

    private final String pluginIdentifier;

    private final String name;

    public ViewDefinition(final String name, final RootContainerDefinition root, final String pluginIdentifier) {
        this.name = name;
        this.root = root;
        this.pluginIdentifier = pluginIdentifier;
    }

    public String getPluginIdentifier() {
        return pluginIdentifier;
    }

    public RootContainerDefinition getRoot() {
        return root;
    }

    public String getName() {
        return name;
    }

    public ViewEntity<Object> getValue(final Entity entity, final Map<String, Entity> selectableEntities,
            final ViewEntity<Object> globalViewEntity) {
        ViewEntity<Object> value = new ViewEntity<Object>();
        value.addComponent(
                root.getName(),
                root.getValue(entity, selectableEntities, globalViewEntity,
                        globalViewEntity != null ? globalViewEntity.getComponent(root.getName()) : null));
        return value;
    }

}
