/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.3.0
 *
 * This file is part of Qcadoo.
 *
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */

package com.qcadoo.mes.view.xml;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.google.common.base.Preconditions;
import com.qcadoo.mes.api.TranslationService;
import com.qcadoo.mes.api.ViewDefinitionService;
import com.qcadoo.mes.view.ComponentDefinition;
import com.qcadoo.mes.view.ComponentOption;
import com.qcadoo.mes.view.ComponentPattern;
import com.qcadoo.mes.view.ContainerPattern;
import com.qcadoo.mes.view.HookDefinition;
import com.qcadoo.mes.view.ViewDefinition;
import com.qcadoo.mes.view.hooks.internal.HookDefinitionImpl;
import com.qcadoo.mes.view.hooks.internal.HookFactory;
import com.qcadoo.mes.view.internal.ComponentCustomEvent;
import com.qcadoo.mes.view.internal.ViewComponentsResolver;
import com.qcadoo.mes.view.internal.ViewDefinitionImpl;
import com.qcadoo.mes.view.patterns.AbstractComponentPattern;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;

@Service
public final class ViewDefinitionParserImpl implements ViewDefinitionParser {

    private static final Logger LOG = LoggerFactory.getLogger(ViewDefinitionParserImpl.class);

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private ViewDefinitionService viewDefinitionService;

    @Autowired
    private ViewComponentsResolver viewComponentsResolver;

    @Autowired
    private TranslationService translationService;

    @Autowired
    private HookFactory hookFactory;

    // @Autowired
    // private ApplicationContext applicationContext;

    @Autowired
    private ViewComponentsResolver viewComponentResolver;

    private int currentIndexOrder;

    @PostConstruct
    public void initializeService() {
        viewComponentResolver.refreshAvaliebleComponentsList();
    }

    @Override
    public List<ViewDefinition> parseViewXml(final Resource viewXml) {
        try {
            return parse(viewXml.getInputStream());
        } catch (IOException e) {
            throw new IllegalStateException("Error while reading view resource", e);
        }
    }

    // @Override
    // public void init() {
    // viewComponentResolver.refreshAvaliebleComponentsList();
    //
    // LOG.info("Reading view definitions ...");
    //
    // try {
    // Resource[] resources = applicationContext.getResources("classpath*:view/*.xml");
    // for (Resource resource : resources) {
    // parse(resource.getInputStream());
    // }
    // } catch (IOException e) {
    // LOG.error("Cannot read view definition", e);
    // }
    // }

    private List<ViewDefinition> parse(final InputStream dataDefinitionInputStream) {
        try {
            DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document document = documentBuilder.parse(dataDefinitionInputStream);

            Node root = document.getDocumentElement();

            String pluginIdentifier = getPluginIdentifier(root);

            NodeList childNodes = root.getChildNodes();

            List<ViewDefinition> views = new LinkedList<ViewDefinition>();

            for (int i = 0; i < childNodes.getLength(); i++) {
                Node child = childNodes.item(i);

                if ("view".equals(child.getNodeName())) {
                    String name = getStringAttribute(child, "name");
                    views.add(parseViewDefinition(child, pluginIdentifier, name));
                }
            }

            return views;
        } catch (ParserConfigurationException e) {
            throw new IllegalStateException(e.getMessage(), e);
        } catch (SAXException e) {
            throw new IllegalStateException(e.getMessage(), e);
        } catch (IOException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    private ViewDefinition parseViewDefinition(final Node viewNode, final String pluginIdentifier, final String name) {
        currentIndexOrder = 1;

        LOG.info("Reading view " + name + " for plugin " + pluginIdentifier);

        boolean menuAccessible = getBooleanAttribute(viewNode, "menuAccessible", false);
        boolean dynamic = getBooleanAttribute(viewNode, "dynamic", false);

        DataDefinition dataDefinition = null;

        if (getStringAttribute(viewNode, "model") != null) {
            String modelPluginIdentifier = getStringAttribute(viewNode, "plugin") != null ? getStringAttribute(viewNode, "plugin")
                    : pluginIdentifier;
            dataDefinition = dataDefinitionService.get(modelPluginIdentifier, getStringAttribute(viewNode, "model"));
        }

        ViewDefinitionImpl viewDefinition = new ViewDefinitionImpl(name, pluginIdentifier, dataDefinition, menuAccessible,
                translationService);

        ComponentPattern root = null;

        NodeList childNodes = viewNode.getChildNodes();

        for (int i = 0; i < childNodes.getLength(); i++) {
            Node child = childNodes.item(i);

            if ("component".equals(child.getNodeName())) {
                root = parseComponent(child, viewDefinition, null, pluginIdentifier);
            } else if ("preInitializeHook".equals(child.getNodeName())) {
                viewDefinition.addPreInitializeHook(parseHook(child));
            } else if ("postConstructHook".equals(child.getNodeName())) {
                if (!dynamic) {
                    throw new IllegalStateException("PostConstructHook can be used only with dynamic views");
                }
                viewDefinition.addPostConstructHook(parseHook(child));
            } else if ("postInitializeHook".equals(child.getNodeName())) {
                viewDefinition.addPostInitializeHook(parseHook(child));
            } else if ("preRenderHook".equals(child.getNodeName())) {
                viewDefinition.addPreRenderHook(parseHook(child));
            }
        }

        viewDefinition.addComponentPattern(root);

        viewDefinition.initialize();

        viewDefinition.registerViews(viewDefinitionService);

        return viewDefinition;
    }

    // private void parseView(final Node viewNode, final String pluginIdentifier) {
    // String name = getStringAttribute(viewNode, "name");
    //
    // boolean menuAccessible = getBooleanAttribute(viewNode, "menuAccessible", false);
    // boolean dynamic = getBooleanAttribute(viewNode, "dynamic", false);
    //
    // if (dynamic) {
    // viewDefinitionService.saveDynamic(pluginIdentifier, name, menuAccessible, viewNode);
    // } else {
    // viewDefinitionService.save(parseViewDefinition(viewNode, pluginIdentifier, name));
    // }
    // }

    private String getPluginIdentifier(final Node node) {
        return getStringAttribute(node, "plugin");
    }

    @Override
    public Boolean getBooleanAttribute(final Node node, final String name, final boolean defaultValue) {
        Node attribute = getAttribute(node, name);
        if (attribute != null) {
            return Boolean.valueOf(attribute.getNodeValue());
        } else {
            return defaultValue;
        }
    }

    @Override
    public String getStringAttribute(final Node node, final String name) {
        Node attribute = getAttribute(node, name);
        if (attribute != null) {
            return attribute.getNodeValue();
        } else {
            return null;
        }
    }

    @Override
    public String getStringNodeContent(final Node node) {
        NodeList childNodes = node.getChildNodes();
        StringBuilder contentSB = new StringBuilder();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node child = childNodes.item(i);
            if (child.getNodeType() == Node.CDATA_SECTION_NODE || child.getNodeType() == Node.TEXT_NODE) {
                contentSB.append(child.getNodeValue());
            }
        }
        return contentSB.toString().trim();
    }

    private Node getAttribute(final Node node, final String name) {
        if (node != null && node.getAttributes() != null) {
            return node.getAttributes().getNamedItem(name);
        } else {
            return null;
        }
    }

    @Override
    public ComponentOption parseOption(final Node optionNode) {
        Map<String, String> attributes = new HashMap<String, String>();

        NamedNodeMap attributesNodes = optionNode.getAttributes();

        for (int i = 0; i < attributesNodes.getLength(); i++) {
            attributes.put(attributesNodes.item(i).getNodeName(), attributesNodes.item(i).getNodeValue());
        }
        return new ComponentOption(getStringAttribute(optionNode, "type"), attributes);
    }

    public ComponentPattern parseComponent(final Node componentNode, final ViewDefinition viewDefinition,
            final ContainerPattern parent, final String pluginIdentifier) {
        String type = getStringAttribute(componentNode, "type");

        if (parent == null && !("window".equals(type) || "tabWindow".equals(type))) {
            throw new IllegalStateException("Unsupported component: " + type);
        }

        ComponentPattern component = viewComponentsResolver.getViewComponentInstance(type,
                getComponentDefinition(componentNode, parent, viewDefinition));

        component.parse(componentNode, this);

        return component;
    }

    @Override
    public ComponentDefinition getComponentDefinition(final Node componentNode, final ContainerPattern parent,
            final ViewDefinition viewDefinition) {
        String name = getStringAttribute(componentNode, "name");
        String fieldPath = getStringAttribute(componentNode, "field");
        String sourceFieldPath = getStringAttribute(componentNode, "source");
        String plugin = getStringAttribute(componentNode, "plugin");
        String model = getStringAttribute(componentNode, "model");

        DataDefinition customDataDefinition = null;

        if (model != null) {
            String modelPluginIdentifier = plugin != null ? plugin : viewDefinition.getPluginIdentifier();
            customDataDefinition = dataDefinitionService.get(modelPluginIdentifier, model);
        }

        ComponentDefinition componentDefinition = new ComponentDefinition();
        componentDefinition.setName(name);
        componentDefinition.setFieldPath(fieldPath);
        componentDefinition.setSourceFieldPath(sourceFieldPath);
        componentDefinition.setParent(parent);
        componentDefinition.setTranslationService(translationService);
        componentDefinition.setViewDefinition(viewDefinition);
        componentDefinition.setReference(getStringAttribute(componentNode, "reference"));
        componentDefinition.setDefaultEnabled(getBooleanAttribute(componentNode, "defaultEnabled", true));
        componentDefinition.setDefaultVisible(getBooleanAttribute(componentNode, "defaultVisible", true));
        componentDefinition.setHasLabel(getBooleanAttribute(componentNode, "hasLabel", true));
        componentDefinition.setHasDescription(getBooleanAttribute(componentNode, "hasDescription", false));
        componentDefinition.setDataDefinition(customDataDefinition);

        return componentDefinition;
    }

    @Override
    public ComponentPattern parseComponent(final Node componentNode, final ContainerPattern parent) {
        return parseComponent(componentNode, ((AbstractComponentPattern) parent).getViewDefinition(), parent,
                ((AbstractComponentPattern) parent).getViewDefinition().getPluginIdentifier());
    }

    @Override
    public ComponentCustomEvent parseCustomEvent(final Node listenerNode) {
        HookDefinitionImpl hookDefinition = (HookDefinitionImpl) parseHook(listenerNode);
        return new ComponentCustomEvent(getStringAttribute(listenerNode, "event"), hookDefinition.getObject(),
                hookDefinition.getMethod());
    }

    public HookDefinition parseHook(final Node hookNode) {
        String fullyQualifiedClassName = getStringAttribute(hookNode, "bean");
        String methodName = getStringAttribute(hookNode, "method");
        Preconditions.checkState(StringUtils.hasText(fullyQualifiedClassName), "Hook bean name is required");
        Preconditions.checkState(StringUtils.hasText(methodName), "Hook method name is required");
        return hookFactory.getHook(fullyQualifiedClassName, methodName);
    }

    public int getCurrentIndexOrder() {
        return currentIndexOrder++;
    }

}
