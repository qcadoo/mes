package com.qcadoo.mes.view.internal.module;

import org.springframework.beans.factory.annotation.Autowired;
import org.w3c.dom.Node;

import com.qcadoo.mes.api.ViewDefinitionService;
import com.qcadoo.mes.view.xml.ViewDefinitionParser;
import com.qcadoo.plugin.internal.api.ModuleFactory;

public class ViewModuleFactory implements ModuleFactory<ViewModule> {

    @Autowired
    private ViewDefinitionParser viewDefinitionParser;

    @Autowired
    private ViewDefinitionService viewDefinitionService;

    @Override
    public void postInitialize() {
        // viewDefinitionParser.init();
    }

    @Override
    public ViewModule parse(final String pluginIdentifier, final Node node) {

        // TODO parse 'view' tag:

        // <view>
        // <resource> - wiele tagow, content to sciezka do xml

        return new ViewModule(null, viewDefinitionParser, viewDefinitionService);
    }

    @Override
    public String getIdentifier() {
        return "view";
    }

}
