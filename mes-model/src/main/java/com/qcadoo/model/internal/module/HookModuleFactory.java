package com.qcadoo.model.internal.module;

import org.jdom.Element;

import com.qcadoo.plugin.internal.api.ModuleFactory;

public class HookModuleFactory implements ModuleFactory<HookModule> {

    @Override
    public void postInitialize() {
        // empty
    }

    @Override
    public HookModule parse(final String pluginIdentifier, final Element element) {
        return new HookModule();
    }

    @Override
    public String getIdentifier() {
        return "hook";
    }

}
