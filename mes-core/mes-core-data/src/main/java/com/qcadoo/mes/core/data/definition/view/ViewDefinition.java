package com.qcadoo.mes.core.data.definition.view;


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

}
