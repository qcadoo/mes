package com.qcadoo.mes.core.data.definition.view;

import com.qcadoo.mes.core.data.definition.DataDefinition;

public class ViewDefinition {

    private final ContainerDefinition root;

    private final String pluginIdentifier;

    private final String name;

    private final DataDefinition dataDefinition;

    public ViewDefinition(final String name, final ContainerDefinition root, final String pluginIdentifier,
            final DataDefinition dataDefinition) {
        this.name = name;
        this.root = root;
        this.pluginIdentifier = pluginIdentifier;
        this.dataDefinition = dataDefinition;
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

    public DataDefinition getDataDefinition() {
        return dataDefinition;
    }

}
