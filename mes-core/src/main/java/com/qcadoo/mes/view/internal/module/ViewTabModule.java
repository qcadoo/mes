package com.qcadoo.mes.view.internal.module;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.springframework.core.io.Resource;
import org.w3c.dom.Node;

import com.google.common.base.Preconditions;
import com.qcadoo.mes.api.ViewDefinitionService;
import com.qcadoo.mes.view.ComponentPattern;
import com.qcadoo.mes.view.ViewDefinition;
import com.qcadoo.mes.view.components.tabWindow.WindowComponentPattern;
import com.qcadoo.mes.view.components.tabWindow.WindowTabComponentPattern;
import com.qcadoo.mes.view.xml.ViewDefinitionParser;
import com.qcadoo.mes.view.xml.ViewExtension;
import com.qcadoo.plugin.api.PluginState;
import com.qcadoo.plugin.internal.api.Module;

public class ViewTabModule extends Module {

    private final ViewDefinitionService viewDefinitionService;

    private final ViewDefinitionParser viewDefinitionParser;

    private final List<ViewExtension> viewExtensions;

    private Map<WindowComponentPattern, ComponentPattern> addedTabs;

    public ViewTabModule(final List<Resource> xmlFiles, final ViewDefinitionService viewDefinitionService,
            final ViewDefinitionParser viewDefinitionParser) {
        this.viewDefinitionService = viewDefinitionService;
        this.viewDefinitionParser = viewDefinitionParser;
        viewExtensions = new LinkedList<ViewExtension>();
        try {
            for (Resource xmlFile : xmlFiles) {
                viewExtensions.addAll(viewDefinitionParser.getViewExtensionNodes(xmlFile.getInputStream(), "windowTabExtension"));
            }
        } catch (IOException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    @Override
    public void init(final PluginState state) {
        if (PluginState.ENABLED.equals(state)) {
            enable();
        }
    }

    @Override
    public void enable() {
        addedTabs = new HashMap<WindowComponentPattern, ComponentPattern>();
        for (ViewExtension viewExtension : viewExtensions) {

            ViewDefinition viewDefinition = viewDefinitionService.getWithoutSession(viewExtension.getPluginName(),
                    viewExtension.getViewName());
            Preconditions.checkNotNull(viewDefinition, getErrorMessage("reference to view which not exists", viewExtension));

            for (Node tabNode : viewDefinitionParser.geElementChildren(viewExtension.getExtesionNode())) {

                WindowComponentPattern window = viewDefinition.getRootWindow();
                Preconditions.checkNotNull(window, getErrorMessage("cannot add ribbon element to view", viewExtension));

                ComponentPattern tabPattern = new WindowTabComponentPattern(viewDefinitionParser.getComponentDefinition(tabNode,
                        window, viewDefinition));
                tabPattern.parse(tabNode, viewDefinitionParser);

                window.addChild(tabPattern);
                addedTabs.put(window, tabPattern);

                tabPattern.registerViews(viewDefinitionService);
                tabPattern.initializeAll();
            }
        }
    }

    @Override
    public void disable() {
        for (Map.Entry<WindowComponentPattern, ComponentPattern> addedGroupEntry : addedTabs.entrySet()) {
            addedGroupEntry.getValue().unregisterComponent(viewDefinitionService);
            addedGroupEntry.getKey().removeChild(addedGroupEntry.getValue().getName());
        }
    }

    private String getErrorMessage(final String msg, final ViewExtension viewExtension) {
        return "View window tab extension error [to " + viewExtension.getPluginName() + "-" + viewExtension.getViewName() + "]: "
                + msg;
    }

}
