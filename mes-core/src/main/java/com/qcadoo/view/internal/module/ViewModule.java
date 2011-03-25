package com.qcadoo.view.internal.module;

import java.util.List;

import org.springframework.core.io.Resource;

import com.qcadoo.plugin.api.Module;
import com.qcadoo.plugin.api.PluginState;
import com.qcadoo.view.api.ViewDefinition;
import com.qcadoo.view.internal.api.InternalViewDefinitionService;
import com.qcadoo.view.internal.xml.ViewDefinitionParser;

public class ViewModule extends Module {

    private final ViewDefinitionParser viewDefinitionParser;

    private final InternalViewDefinitionService viewDefinitionService;

    private final List<Resource> xmlFiles;

    public ViewModule(final List<Resource> xmlFiles, final ViewDefinitionParser viewDefinitionParser,
            final InternalViewDefinitionService viewDefinitionService) {
        this.xmlFiles = xmlFiles;
        this.viewDefinitionParser = viewDefinitionParser;
        this.viewDefinitionService = viewDefinitionService;
    }

    @Override
    public void init(final PluginState state) {
        if (PluginState.ENABLED.equals(state)) {
            enable();
        }
    }

    @Override
    public void enable() {
        for (Resource xmlFile : xmlFiles) {
            List<ViewDefinition> viewDefinitions = viewDefinitionParser.parseViewXml(xmlFile);
            for (ViewDefinition viewDefinition : viewDefinitions) {
                viewDefinitionService.save(viewDefinition);
            }
        }
    }

    @Override
    public void disable() {
        for (Resource xmlFile : xmlFiles) {
            List<ViewDefinition> viewDefinitions = viewDefinitionParser.parseViewXml(xmlFile);
            for (ViewDefinition viewDefinition : viewDefinitions) {
                viewDefinitionService.delete(viewDefinition);
            }
        }
    }

}
