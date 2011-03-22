package com.qcadoo.model.internal.module;

import com.qcadoo.model.internal.api.InternalDataDefinition;
import com.qcadoo.model.internal.api.InternalDataDefinitionService;
import com.qcadoo.plugin.api.PluginState;
import com.qcadoo.plugin.internal.api.Module;

public class HookModule extends Module {

    private final String pluginIdentifier;

    private final String modelName;

    private final String hookType;

    private final String hookClassName;

    private final String hookMethodName;

    private final InternalDataDefinitionService dataDefinitionService;

    public HookModule(final String pluginIdentifier, final String modelName, final String hookType, final String hookClassName,
            final String hookMethodName, final InternalDataDefinitionService dataDefinitionService) {
        this.pluginIdentifier = pluginIdentifier;
        this.modelName = modelName;
        this.hookType = hookType;
        this.hookClassName = hookClassName;
        this.hookMethodName = hookMethodName;
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
        ((InternalDataDefinition) dataDefinitionService.get(pluginIdentifier, modelName)).getHook(hookType, hookClassName,
                hookMethodName).enable();
    }

    @Override
    public void disable() {
        ((InternalDataDefinition) dataDefinitionService.get(pluginIdentifier, modelName)).getHook(hookType, hookClassName,
                hookMethodName).disable();
    }

}
