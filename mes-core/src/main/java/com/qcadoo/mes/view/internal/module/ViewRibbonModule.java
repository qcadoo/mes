package com.qcadoo.mes.view.internal.module;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.springframework.core.io.Resource;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.google.common.base.Preconditions;
import com.qcadoo.mes.api.ViewDefinitionService;
import com.qcadoo.mes.view.ViewDefinition;
import com.qcadoo.mes.view.components.tabWindow.WindowComponentPattern;
import com.qcadoo.mes.view.ribbon.RibbonGroup;
import com.qcadoo.mes.view.ribbon.RibbonUtils;
import com.qcadoo.mes.view.xml.ViewDefinitionParser;
import com.qcadoo.plugin.api.PluginState;
import com.qcadoo.plugin.internal.api.Module;

public class ViewRibbonModule extends Module {

    private final ViewDefinitionService viewDefinitionService;

    private final ViewDefinitionParser viewDefinitionParser;

    private final List<Resource> xmlFiles;

    private Map<WindowComponentPattern, RibbonGroup> addedGroups;

    public ViewRibbonModule(final List<Resource> xmlFiles, final ViewDefinitionService viewDefinitionService,
            final ViewDefinitionParser viewDefinitionParser) {
        this.xmlFiles = xmlFiles;
        this.viewDefinitionService = viewDefinitionService;
        this.viewDefinitionParser = viewDefinitionParser;
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
        for (Resource xmlFile : xmlFiles) {
            try {
                DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                Document document = documentBuilder.parse(xmlFile.getInputStream());

                Node root = document.getDocumentElement();

                NodeList childNodes = root.getChildNodes();

                for (int i = 0; i < childNodes.getLength(); i++) {
                    Node child = childNodes.item(i);

                    if ("ribbonExtension".equals(child.getNodeName())) {
                        String plugin = viewDefinitionParser.getStringAttribute(child, "plugin");
                        String view = viewDefinitionParser.getStringAttribute(child, "view");

                        Preconditions.checkNotNull(plugin, "View ribbon extension error: plugin not defined");
                        Preconditions.checkNotNull(view, "View ribbon extension error: view not defined");

                        ViewDefinition viewDefinition = viewDefinitionService.getWithoutSession(plugin, view);
                        Preconditions.checkNotNull(viewDefinition, "View ribbon extension referes to view which not exists ("
                                + plugin + " - " + view + ")");

                        NodeList groupNodes = child.getChildNodes();
                        for (int j = 0; j < groupNodes.getLength(); j++) {
                            Node groupNode = groupNodes.item(j);
                            if (groupNode.getNodeType() != Node.ELEMENT_NODE) {
                                continue;
                            }
                            RibbonGroup group = RibbonUtils.parseRibbonGroup(groupNode, viewDefinitionParser, viewDefinition);

                            // TODO root insteed reference name
                            WindowComponentPattern window = (WindowComponentPattern) viewDefinition
                                    .getComponentByReference("window");
                            window.getRibbon().addGroup(group);
                            addedGroups.put(window, group);
                        }
                    }
                }

            } catch (ParserConfigurationException e) {
                throw new IllegalStateException(e.getMessage(), e);
            } catch (SAXException e) {
                throw new IllegalStateException(e.getMessage(), e);
            } catch (IOException e) {
                throw new IllegalStateException(e.getMessage(), e);
            }
        }
    }

    @Override
    public void disable() {
        for (Map.Entry<WindowComponentPattern, RibbonGroup> addedGroupEntry : addedGroups.entrySet()) {
            addedGroupEntry.getKey().getRibbon().removeGroup(addedGroupEntry.getValue());
        }
    }

}
