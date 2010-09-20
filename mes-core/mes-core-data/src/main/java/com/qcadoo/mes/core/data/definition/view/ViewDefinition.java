package com.qcadoo.mes.core.data.definition.view;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

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

    public ViewEntity<Object> castValue(final Entity entity, final Map<String, List<Entity>> selectedEntities,
            final JSONObject viewObject) throws JSONException {
        ViewEntity<Object> value = new ViewEntity<Object>();
        value.addComponent(root.getName(),
                root.castValue(entity, selectedEntities, viewObject != null ? viewObject.getJSONObject(root.getName()) : null));
        return value;
    }

    public ViewEntity<Object> getValue(final Entity entity, final Map<String, List<Entity>> selectedEntities,
            final ViewEntity<Object> globalViewEntity, final Set<String> pathsToUpdate) {
        ViewEntity<Object> value = new ViewEntity<Object>();
        value.addComponent(
                root.getName(),
                root.getValue(entity, selectedEntities, globalViewEntity,
                        globalViewEntity != null ? globalViewEntity.getComponent(root.getName()) : null, pathsToUpdate));
        return value;
    }

}
