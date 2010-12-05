/**
 * ********************************************************************
 * Code developed by amazing QCADOO developers team.
 * Copyright (c) Qcadoo Limited sp. z o.o. (2010)
 * ********************************************************************
 */

package com.qcadoo.mes.view.xml;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.google.common.base.Preconditions;
import com.qcadoo.mes.api.DataDefinitionService;
import com.qcadoo.mes.api.TranslationService;
import com.qcadoo.mes.api.ViewDefinitionService;
import com.qcadoo.mes.model.DataDefinition;
import com.qcadoo.mes.model.HookDefinition;
import com.qcadoo.mes.model.hooks.internal.HookFactory;
import com.qcadoo.mes.view.ComponentDefinition;
import com.qcadoo.mes.view.ComponentOption;
import com.qcadoo.mes.view.ComponentPattern;
import com.qcadoo.mes.view.ContainerPattern;
import com.qcadoo.mes.view.ViewDefinition;
import com.qcadoo.mes.view.components.WindowComponentPattern;
import com.qcadoo.mes.view.internal.ViewComponentsResolver;
import com.qcadoo.mes.view.internal.ViewDefinitionImpl;
import com.qcadoo.mes.view.patterns.AbstractComponentPattern;
import com.qcadoo.mes.view.patterns.AbstractContainerPattern;
import com.qcadoo.mes.view.ribbon.Ribbon;
import com.qcadoo.mes.view.ribbon.RibbonActionItem;
import com.qcadoo.mes.view.ribbon.RibbonComboItem;
import com.qcadoo.mes.view.ribbon.RibbonGroup;

@Service
public final class ViewDefinitionParser {

    private static final Logger LOG = LoggerFactory.getLogger(ViewDefinitionParser.class);

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

    @Autowired
    private ApplicationContext applicationContext;

    public void parse() {
        LOG.info("Reading view definitions ...");

        try {
            Resource[] resources = applicationContext.getResources("classpath*:view.xml");
            // Resource[] resources = applicationContext.getResources("classpath*:testView.xml");
            for (Resource resource : resources) {
                parse(resource.getInputStream());
            }
        } catch (IOException e) {
            LOG.error("Cannot read view definition", e);
        }
    }

    public void parse(final InputStream dataDefinitionInputStream) {
        try {
            XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(dataDefinitionInputStream);

            String pluginIdentifier = null;

            while (reader.hasNext() && reader.next() > 0) {
                if (isTagStarted(reader, "views")) {
                    pluginIdentifier = getPluginIdentifier(reader);
                } else if (isTagStarted(reader, "view")) {
                    getViewDefinition(reader, pluginIdentifier);
                }
            }

            reader.close();
        } catch (XMLStreamException e) {
            throw new IllegalStateException(e.getMessage(), e);
        } catch (FactoryConfigurationError e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    private void getViewDefinition(final XMLStreamReader reader, final String pluginIdentifier) throws XMLStreamException {
        String name = getStringAttribute(reader, "name");

        LOG.info("Reading view " + name + " for plugin " + pluginIdentifier);

        boolean menuAccessible = getBooleanAttribute(reader, "menuAccessible", false);

        DataDefinition dataDefinition = null;

        if (getStringAttribute(reader, "model") != null) {
            dataDefinition = dataDefinitionService.get(pluginIdentifier, getStringAttribute(reader, "model"));
        }

        ViewDefinitionImpl viewDefinition = new ViewDefinitionImpl(name, pluginIdentifier, dataDefinition, menuAccessible);

        ComponentPattern root = null;

        while (reader.hasNext() && reader.next() > 0) {
            if (isTagStarted(reader, "component")) {
                root = getComponentPattern(reader, null, viewDefinition);
            } else if (isTagStarted(reader, "preInitializeHook")) {
                viewDefinition.addPreInitializeHook(getHookDefinition(reader));
            } else if (isTagStarted(reader, "postInitializeHook")) {
                viewDefinition.addPostInitializeHook(getHookDefinition(reader));
            } else if (isTagStarted(reader, "preRenderHook")) {
                viewDefinition.addPreRenderHook(getHookDefinition(reader));
            } else if (isTagEnded(reader, "view")) {
                break;
            }
        }

        viewDefinition.addComponentPattern(root);

        viewDefinition.initialize();

        viewDefinitionService.save(viewDefinition);
    }

    private ComponentPattern getComponentPattern(final XMLStreamReader reader, final ContainerPattern parent,
            final ViewDefinition viewDefinition) throws XMLStreamException {
        String type = getStringAttribute(reader, "type");
        String name = getStringAttribute(reader, "name");
        String fieldPath = getStringAttribute(reader, "field");
        String sourceFieldPath = getStringAttribute(reader, "source");

        if (parent == null && !"window".equals(type)) {
            throw new IllegalStateException("Unsupported component: " + type);
        }

        ComponentDefinition componentDefinition = new ComponentDefinition();
        componentDefinition.setName(name);
        componentDefinition.setFieldPath(fieldPath);
        componentDefinition.setSourceFieldPath(sourceFieldPath);
        componentDefinition.setParent(parent);
        componentDefinition.setTranslationService(translationService);
        componentDefinition.setViewDefinition(viewDefinition);
        componentDefinition.setReference(getStringAttribute(reader, "reference"));
        componentDefinition.setDefaultEnabled(getBooleanAttribute(reader, "defaultEnabled", true));
        componentDefinition.setDefaultVisible(getBooleanAttribute(reader, "defaultVisible", true));
        componentDefinition.setHasDescription(getBooleanAttribute(reader, "hasDescription", false));

        ComponentPattern component = viewComponentsResolver.getViewComponentInstance(type, componentDefinition);

        addMenuAndChildrenComponentsAndOptions(reader, component, viewDefinition);

        return component;
    }

    private void addMenuAndChildrenComponentsAndOptions(final XMLStreamReader reader, final ComponentPattern component,
            final ViewDefinition viewDefinition) throws XMLStreamException {
        while (reader.hasNext() && reader.next() > 0) {
            if (isTagStarted(reader, "ribbon")) {
                if (component instanceof WindowComponentPattern) {
                    ((WindowComponentPattern) component).setRibbon(getRibbon(reader));
                }
            } else if (isTagStarted(reader, "option")) {
                ((AbstractComponentPattern) component).addOption(getOption(reader));
            } else if (isTagStarted(reader, "component")) {
                if (component instanceof ContainerPattern) {
                    ((AbstractContainerPattern) component).addChild(getComponentPattern(reader, (ContainerPattern) component,
                            viewDefinition));
                }
            } else if (isTagEnded(reader, "component")) {
                break;
            }
        }
    }

    private Ribbon getRibbon(final XMLStreamReader reader) throws XMLStreamException {
        Ribbon ribbon = new Ribbon();
        while (reader.hasNext() && reader.next() > 0) {
            if (isTagEnded(reader, "ribbon")) {
                break;
            } else if (isTagStarted(reader, "group")) {
                ribbon.addGroup(getRibbonGroup(reader));
            }
        }
        return ribbon;
    }

    private RibbonGroup getRibbonGroup(final XMLStreamReader reader) throws XMLStreamException {
        RibbonGroup ribbonGroup = new RibbonGroup();
        ribbonGroup.setName(getStringAttribute(reader, "name"));
        while (reader.hasNext() && reader.next() > 0) {
            if (isTagEnded(reader, "group")) {
                break;
            } else if (isTagStarted(reader)) {
                ribbonGroup.addItem(getRibbonItem(reader));
            }
        }
        return ribbonGroup;
    }

    private RibbonActionItem getRibbonItem(final XMLStreamReader reader) throws XMLStreamException {
        String stringType = reader.getLocalName();
        boolean combo = ("bigButtons".equals(stringType) || "smallButtons".equals(stringType));
        RibbonActionItem.Type type = null;
        if ("bigButtons".equals(stringType) || "bigButton".equals(stringType)) {
            type = RibbonActionItem.Type.BIG_BUTTON;
        } else if ("smallButtons".equals(stringType) || "smallButton".equals(stringType)) {
            type = RibbonActionItem.Type.SMALL_BUTTON;
        }
        RibbonActionItem item = null;
        if (combo) {
            item = new RibbonComboItem();
        } else {
            item = new RibbonActionItem();
        }
        item.setIcon(getStringAttribute(reader, "icon"));
        item.setName(getStringAttribute(reader, "name"));
        item.setAction(getStringAttribute(reader, "action"));
        item.setType(type);
        if (combo) {
            while (reader.hasNext() && reader.next() > 0) {
                if (isTagEnded(reader, stringType)) {
                    break;
                } else if (isTagStarted(reader) && item instanceof RibbonComboItem) {
                    ((RibbonComboItem) item).addItem(getRibbonItem(reader));
                }
            }
        } else {
            (item).setAction(getStringAttribute(reader, "action"));
        }
        return item;
    }

    private ComponentOption getOption(final XMLStreamReader reader) throws XMLStreamException {
        Map<String, String> attributes = new HashMap<String, String>();
        for (int i = 0; i < reader.getAttributeCount(); i++) {
            attributes.put(reader.getAttributeLocalName(i), reader.getAttributeValue(i));
        }
        return new ComponentOption(getStringAttribute(reader, "type"), attributes);
    }

    private HookDefinition getHookDefinition(final XMLStreamReader reader) {
        String fullyQualifiedClassName = getStringAttribute(reader, "bean");
        String methodName = getStringAttribute(reader, "method");
        Preconditions.checkState(StringUtils.hasText(fullyQualifiedClassName), "Hook bean name is required");
        Preconditions.checkState(StringUtils.hasText(methodName), "Hook method name is required");
        return hookFactory.getHook(fullyQualifiedClassName, methodName);
    }

    private String getPluginIdentifier(final XMLStreamReader reader) {
        return getStringAttribute(reader, "plugin");
    }

    private boolean getBooleanAttribute(final XMLStreamReader reader, final String name, final boolean defaultValue) {
        String stringValue = reader.getAttributeValue(null, name);
        if (stringValue != null) {
            return Boolean.valueOf(stringValue);
        } else {
            return defaultValue;
        }
    }

    private String getStringAttribute(final XMLStreamReader reader, final String name) {
        return reader.getAttributeValue(null, name);
    }

    private boolean isTagStarted(final XMLStreamReader reader, final String tagName) {
        return (isTagStarted(reader) && tagName.equals(reader.getLocalName()));
    }

    private boolean isTagStarted(final XMLStreamReader reader) {
        return (reader.getEventType() == XMLStreamConstants.START_ELEMENT);
    }

    private boolean isTagEnded(final XMLStreamReader reader, final String tagName) {
        return (reader.getEventType() == XMLStreamConstants.END_ELEMENT && tagName.equals(reader.getLocalName()));
    }

}
