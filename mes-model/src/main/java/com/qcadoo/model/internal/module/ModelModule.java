package com.qcadoo.model.internal.module;

import com.qcadoo.model.internal.api.InternalDataDefinition;
import com.qcadoo.model.internal.api.InternalDataDefinitionService;
import com.qcadoo.plugin.api.PluginState;
import com.qcadoo.plugin.internal.api.Module;

public class ModelModule implements Module {

    private final String pluginIdentifier;

    private final String modelName;

    private final InternalDataDefinitionService dataDefinitionService;

    public ModelModule(final String pluginIdentifier, final String modelName,
            final InternalDataDefinitionService dataDefinitionService) {
        this.pluginIdentifier = pluginIdentifier;
        this.modelName = modelName;
        this.dataDefinitionService = dataDefinitionService;
    }

    @Override
    public void init(final PluginState state) {
        if (!PluginState.ENABLED.equals(state)) {
            disable();
        }
    }

    @Override
    public void enable() {
        ((InternalDataDefinition) dataDefinitionService.get(pluginIdentifier, modelName)).enable();
    }

    @Override
    public void disable() {
        ((InternalDataDefinition) dataDefinitionService.get(pluginIdentifier, modelName)).disable();
    }

}
