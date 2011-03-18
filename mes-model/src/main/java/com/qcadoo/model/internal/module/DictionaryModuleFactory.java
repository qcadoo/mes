package com.qcadoo.model.internal.module;

import org.jdom.Element;
import org.springframework.beans.factory.annotation.Autowired;

import com.qcadoo.model.internal.api.InternalDictionaryService;
import com.qcadoo.plugin.internal.api.ModuleFactory;

public class DictionaryModuleFactory implements ModuleFactory<DictionaryModule> {

    @Autowired
    private InternalDictionaryService dictionaryService;

    @Override
    public void init() {
        // ignore
    }

    @Override
    public DictionaryModule parse(final String pluginIdentifier, final Element element) {
        String name = element.getAttributeValue("name");

        if (name == null) {
            throw new IllegalStateException("Missing name attribute of dictionary module");
        }

        return new DictionaryModule(name, dictionaryService);
    }

    @Override
    public String getIdentifier() {
        return "dictionary";
    }

}
