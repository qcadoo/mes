package com.qcadoo.mes.core.data.definition.view;

import java.util.HashMap;
import java.util.Map;

import com.qcadoo.mes.core.data.beans.Entity;

public class ViewDefinition {

    private final ContainerDefinition root;

    private final String pluginIdentifier;

    private final String name;

    public ViewDefinition(final String name, final ContainerDefinition root, final String pluginIdentifier) {
        this.name = name;
        this.root = root;
        this.pluginIdentifier = pluginIdentifier;
    }

    public String getPluginIdentifier() {
        return pluginIdentifier;
    }

    public ContainerDefinition getRoot() {
        return root;
    }

    public String getName() {
        return name;
    }

    public Object getValue(final Entity entity, final Map<String, Object> selectableValues, final Object viewEntity) {
        Map<String, Object> values = new HashMap<String, Object>();
        values.put(root.getPath(), root.getValue(entity, selectableValues, viewEntity));
        return values;
    }

}
