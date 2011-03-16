package com.qcadoo.model.internal.module;

import org.w3c.dom.Node;

import com.qcadoo.plugin.internal.api.ModuleFactory;

public class DictionaryModuleFactory implements ModuleFactory<DictionaryModule> {

    @Override
    public void postInitialize() {
        // TODO Auto-generated method stub

    }

    @Override
    public DictionaryModule parse(final String pluginIdentifier, final Node node) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getIdentifier() {
        return "dictionary";
    }

}
