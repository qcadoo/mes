package com.qcadoo.model.internal.module;

import com.qcadoo.plugin.api.PluginState;
import com.qcadoo.plugin.internal.api.Module;

public class DictionaryModule implements Module {

    @Override
    public void init(final PluginState state) {
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
