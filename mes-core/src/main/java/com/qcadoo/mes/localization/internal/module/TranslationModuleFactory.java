package com.qcadoo.mes.localization.internal.module;

import org.jdom.Element;

import com.qcadoo.plugin.api.ModuleFactory;

public class TranslationModuleFactory implements ModuleFactory<TranslationModule> {

    @Override
    public void init() {
        // TODO Auto-generated method stub
    }

    @Override
    public TranslationModule parse(final String pluginIdentifier, final Element element) {
        // TODO Auto-generated method stub
        return new TranslationModule();
    }

    @Override
    public String getIdentifier() {
        return "translation";
    }

}
