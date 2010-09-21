package com.qcadoo.mes.core.data.internal.view;

import java.util.Map;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

import com.qcadoo.mes.core.data.beans.Entity;
import com.qcadoo.mes.core.data.view.RootComponent;
import com.qcadoo.mes.core.data.view.ViewDefinition;
import com.qcadoo.mes.core.data.view.ViewEntity;

public class ViewDefinitionImpl implements ViewDefinition {

    private final RootComponent root;

    private final String pluginIdentifier;

    private final String name;

    public ViewDefinitionImpl(final String name, final RootComponent root, final String pluginIdentifier) {
        this.name = name;
        this.root = root;
        this.pluginIdentifier = pluginIdentifier;
    }

    @Override
    public String getPluginIdentifier() {
        return pluginIdentifier;
    }

    @Override
    public RootComponent getRoot() {
        return root;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public ViewEntity<Object> castValue(final Entity entity, final Map<String, Entity> selectedEntities,
            final JSONObject viewObject) throws JSONException {
        ViewEntity<Object> value = new ViewEntity<Object>();
        value.addComponent(root.getName(),
                root.castValue(entity, selectedEntities, viewObject != null ? viewObject.getJSONObject(root.getName()) : null));
        return value;
    }

    @Override
    public ViewEntity<Object> getValue(final Entity entity, final Map<String, Entity> selectedEntities,
            final ViewEntity<Object> globalViewEntity, final Set<String> pathsToUpdate) {
        ViewEntity<Object> value = new ViewEntity<Object>();
        value.addComponent(root.getName(), root.getValue(entity, selectedEntities,
                globalViewEntity != null ? globalViewEntity.getComponent(root.getName()) : null, pathsToUpdate));
        return value;
    }

}
