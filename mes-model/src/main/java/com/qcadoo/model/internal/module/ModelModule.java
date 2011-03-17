package com.qcadoo.model.internal.module;

import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.plugin.internal.api.Module;

public class ModelModule implements Module {

    private final String pluginIdentifier;

    private final String name;

    private final DataDefinitionService dataDefinitionService;

    public ModelModule(final String pluginIdentifier, final String name, final DataDefinitionService dataDefinitionService) {
        this.pluginIdentifier = pluginIdentifier;
        this.name = name;
        this.dataDefinitionService = dataDefinitionService;
    }

    @Override
    public void init() {
        // ignore
    }

    @Override
    public void enable() {
        // TODO
        // włącza model w dataDefinitionService
    }

    @Override
    public void disable() {
        // TODO
        // wyłącza model w dataDefinitionService

    }

}
