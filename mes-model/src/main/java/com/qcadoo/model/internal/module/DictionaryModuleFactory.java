package com.qcadoo.model.internal.module;

import org.jdom.Element;

import com.qcadoo.plugin.internal.api.ModuleFactory;

public class DictionaryModuleFactory implements ModuleFactory<DictionaryModule> {

    @Override
    public void postInitialize() {
        // TODO Auto-generated method stub
    }

    @Override
    public DictionaryModule parse(final String pluginIdentifier, final Element element) {
        String name = element.getAttributeValue("name");

        if (name == null) {
            throw new IllegalStateException("Missing name attribute of dictionary module");
        }

        // TODO

        return new DictionaryModule();
    }

    @Override
    public String getIdentifier() {
        return "dictionary";
    }

}
