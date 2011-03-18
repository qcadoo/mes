package com.qcadoo.model.internal.module;

import com.qcadoo.model.internal.api.InternalDataDefinitionService;
import com.qcadoo.plugin.api.PluginState;
import com.qcadoo.plugin.internal.api.Module;

public class HookModule implements Module {

    private final String pluginIdentifier;

    private final String modelName;

    private final String hookType;

    private final String hookClass;

    private final String hookMethod;

    private final InternalDataDefinitionService dataDefinitionService;

    public HookModule(final String pluginIdentifier, final String modelName, final String hookType, final String hookClass,
            final String hookMethod, final InternalDataDefinitionService dataDefinitionService) {
        this.pluginIdentifier = pluginIdentifier;
        this.modelName = modelName;
        this.hookType = hookType;
        this.hookClass = hookClass;
        this.hookMethod = hookMethod;
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
        // TODO
        // włącza hook w dataDefinitionService
    }

    @Override
    public void disable() {
        // TODO
        // wyłącza hook w dataDefinitionService

    }

}
