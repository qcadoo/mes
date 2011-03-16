package com.qcadoo.mes.localization.internal.module;

import org.w3c.dom.Node;

import com.qcadoo.plugin.internal.api.ModuleFactory;

public class TranslationModuleFactory implements ModuleFactory<TranslationModule> {

    @Override
    public void postInitialize() {
        // TODO Auto-generated method stub
    }

    @Override
    public TranslationModule parse(final String pluginIdentifier, final Node node) {
        // TODO Auto-generated method stub
        return new TranslationModule();
    }

    @Override
    public String getIdentifier() {
        return "translation";
    }

}
