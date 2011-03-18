package com.qcadoo.mes.view.internal.module;

import java.util.List;

import org.springframework.core.io.Resource;

import com.qcadoo.mes.api.ViewDefinitionService;
import com.qcadoo.mes.view.ViewDefinition;
import com.qcadoo.mes.view.xml.ViewDefinitionParser;
import com.qcadoo.plugin.api.PluginState;
import com.qcadoo.plugin.internal.api.Module;

public class ViewModule implements Module {

    private final ViewDefinitionParser viewDefinitionParser;

    private final ViewDefinitionService viewDefinitionService;

    private final List<Resource> xmlFiles;

    public ViewModule(final List<Resource> xmlFiles, final ViewDefinitionParser viewDefinitionParser,
            final ViewDefinitionService viewDefinitionService) {
        this.xmlFiles = xmlFiles;
        this.viewDefinitionParser = viewDefinitionParser;
        this.viewDefinitionService = viewDefinitionService;
    }

    @Override
    public void init(final PluginState state) {
        // do nothing
    }

    @Override
    public void enable() {
        // parse all xml resources, add to VDS

        for (Resource xmlFile : xmlFiles) {
            List<ViewDefinition> viewDefinitions = viewDefinitionParser.parseViewXml(xmlFile);
            for (ViewDefinition viewDefinition : viewDefinitions) {
                viewDefinitionService.save(viewDefinition);
            }
        }
    }

    @Override
    public void disable() {
        // parse all xml resources, remove from VDS

        for (Resource xmlFile : xmlFiles) {
            List<ViewDefinition> viewDefinitions = viewDefinitionParser.parseViewXml(xmlFile);
            for (ViewDefinition viewDefinition : viewDefinitions) {
                viewDefinitionService.delete(viewDefinition);
            }
        }
    }

}
