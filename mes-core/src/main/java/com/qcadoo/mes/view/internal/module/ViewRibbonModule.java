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
import com.qcadoo.mes.view.ViewDefinition;
import com.qcadoo.mes.view.components.tabWindow.WindowComponentPattern;
import com.qcadoo.mes.view.ribbon.RibbonGroup;
import com.qcadoo.mes.view.xml.ViewDefinitionParser;
import com.qcadoo.mes.view.xml.ViewExtension;
import com.qcadoo.plugin.api.PluginState;
import com.qcadoo.plugin.internal.api.Module;

public class ViewRibbonModule extends Module {

    private final ViewDefinitionService viewDefinitionService;

    private final ViewDefinitionParser viewDefinitionParser;

    private final List<ViewExtension> viewExtensions;

    private Map<WindowComponentPattern, RibbonGroup> addedGroups;

    public ViewRibbonModule(final List<Resource> xmlFiles, final ViewDefinitionService viewDefinitionService,
            final ViewDefinitionParser viewDefinitionParser) {
        this.viewDefinitionService = viewDefinitionService;
        this.viewDefinitionParser = viewDefinitionParser;
        viewExtensions = new LinkedList<ViewExtension>();
        try {
            for (Resource xmlFile : xmlFiles) {
                viewExtensions.addAll(viewDefinitionParser.getViewExtensionNodes(xmlFile.getInputStream(), "ribbonExtension"));
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
        addedGroups = new HashMap<WindowComponentPattern, RibbonGroup>();
        for (ViewExtension viewExtension : viewExtensions) {

            ViewDefinition viewDefinition = viewDefinitionService.getWithoutSession(viewExtension.getPluginName(),
                    viewExtension.getViewName());
            Preconditions.checkNotNull(viewDefinition, getErrorMessage("reference to view which not exists", viewExtension));

            for (Node groupNode : viewDefinitionParser.geElementChildren(viewExtension.getExtesionNode())) {

                RibbonGroup group = viewDefinitionParser.parseRibbonGroup(groupNode, viewDefinition);

                WindowComponentPattern window = viewDefinition.getRootWindow();
                Preconditions.checkNotNull(window, getErrorMessage("cannot add ribbon element to view", viewExtension));

                window.getRibbon().addGroup(group);
                addedGroups.put(window, group);
            }
        }
    }

    @Override
    public void disable() {
        for (Map.Entry<WindowComponentPattern, RibbonGroup> addedGroupEntry : addedGroups.entrySet()) {
            addedGroupEntry.getKey().getRibbon().removeGroup(addedGroupEntry.getValue());
        }
    }

    private String getErrorMessage(final String msg, final ViewExtension viewExtension) {
        return "View ribbon extension error [to " + viewExtension.getPluginName() + "-" + viewExtension.getViewName() + "]: "
                + msg;
    }
}
