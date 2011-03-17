package com.qcadoo.mes.view.internal.module;

import org.springframework.beans.factory.annotation.Autowired;
import org.w3c.dom.Node;

import com.qcadoo.mes.view.xml.ViewDefinitionParser;
import com.qcadoo.plugin.internal.api.ModuleFactory;

public class ViewModuleFactory implements ModuleFactory<ViewModule> {

    @Autowired
    private ViewDefinitionParser viewDefinitionParser;

    @Override
    public void postInitialize() {
        viewDefinitionParser.init();
    }

    @Override
    public ViewModule parse(final String pluginIdentifier, final Node node) {
        // TODO Auto-generated method stub
        return new ViewModule();
    }

    @Override
    public String getIdentifier() {
        return "view";
    }

}
