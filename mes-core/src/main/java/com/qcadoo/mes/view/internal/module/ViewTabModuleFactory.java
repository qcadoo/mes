package com.qcadoo.mes.view.internal.module;

import org.jdom.Element;

import com.qcadoo.plugin.internal.api.ModuleFactory;

public class ViewTabModuleFactory implements ModuleFactory<ViewTabModule> {

    @Override
    public void init() {
    }

    @Override
    public ViewTabModule parse(final String pluginIdentifier, final Element element) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getIdentifier() {
        return "viewWindowTabExtension";
    }

}
