package com.qcadoo.mes.view.internal;

import static com.google.common.base.Preconditions.checkState;
import static org.springframework.util.StringUtils.hasText;

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

import com.qcadoo.mes.api.DataDefinitionService;
import com.qcadoo.mes.api.TranslationService;
import com.qcadoo.mes.api.ViewDefinitionService;
import com.qcadoo.mes.model.DataDefinition;
import com.qcadoo.mes.model.HookDefinition;
import com.qcadoo.mes.model.hooks.internal.HookFactory;
import com.qcadoo.mes.view.AbstractComponent;
import com.qcadoo.mes.view.AbstractContainerComponent;
import com.qcadoo.mes.view.Component;
import com.qcadoo.mes.view.ComponentOption;
import com.qcadoo.mes.view.ContainerComponent;
import com.qcadoo.mes.view.RootComponent;
import com.qcadoo.mes.view.ViewDefinition;
import com.qcadoo.mes.view.components.CheckBoxComponent;
import com.qcadoo.mes.view.components.DynamicComboBoxComponent;
import com.qcadoo.mes.view.components.EntityComboBoxComponent;
import com.qcadoo.mes.view.components.GridComponent;
import com.qcadoo.mes.view.components.LinkButtonComponent;
import com.qcadoo.mes.view.components.TextInputComponent;
import com.qcadoo.mes.view.containers.FormComponent;
import com.qcadoo.mes.view.containers.WindowComponent;
import com.qcadoo.mes.view.menu.ribbon.Ribbon;
import com.qcadoo.mes.view.menu.ribbon.RibbonActionItem;
import com.qcadoo.mes.view.menu.ribbon.RibbonComboItem;
import com.qcadoo.mes.view.menu.ribbon.RibbonGroup;

@Service
public final class ViewDefinitionParser {

    private static final Logger LOG = LoggerFactory.getLogger(ViewDefinitionParser.class);

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private ViewDefinitionService viewDefinitionService;

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
        String viewName = getStringAttribute(reader, "name");

        LOG.info("Reading view " + viewName + " for plugin " + pluginIdentifier);

        ViewDefinitionImpl viewDefinition = new ViewDefinitionImpl(pluginIdentifier, viewName);

        DataDefinition dataDefinition = dataDefinitionService.get(pluginIdentifier, getStringAttribute(reader, "model"));
        RootComponent root = null;

        while (reader.hasNext() && reader.next() > 0) {
            if (isTagStarted(reader, "component")) {
                root = getRootComponentDefinition(reader, viewDefinition, dataDefinition);
            } else if (isTagStarted(reader, "onView")) {
                viewDefinition.setViewHook(getHookDefinition(reader));
            } else if (isTagEnded(reader, "view")) {
                break;
            }
        }

        viewDefinition.setRoot(root);
        root.initialize();

        viewDefinitionService.save(viewDefinition);
    }

    private RootComponent getRootComponentDefinition(final XMLStreamReader reader, final ViewDefinition viewDefinition,
            final DataDefinition dataDefinition) throws XMLStreamException {
        String componentType = getStringAttribute(reader, "type");
        String componentName = getStringAttribute(reader, "name");

        RootComponent component = null;

        if ("window".equals(componentType)) {
            component = new WindowComponent(componentName, dataDefinition, viewDefinition, translationService);
        } else {
            throw new IllegalStateException("Unsupported component: " + componentType);
        }

        addMenuAndChildrenComponentsAndOptions(reader, (AbstractComponent<?>) component);

        return component;
    }

    private Component<?> getComponentDefinition(final XMLStreamReader reader, final ContainerComponent<?> parentComponent)
            throws XMLStreamException {
        String componentType = getStringAttribute(reader, "type");
        String componentName = getStringAttribute(reader, "name");
        String fieldName = getStringAttribute(reader, "field");
        String dataSource = getStringAttribute(reader, "source");

        Component<?> component = null;

        if ("input".equals(componentType)) {
            component = new TextInputComponent(componentName, parentComponent, fieldName, dataSource, translationService);
        } else if ("grid".equals(componentType)) {
            component = new GridComponent(componentName, parentComponent, fieldName, dataSource, translationService);
        } else if ("form".equals(componentType)) {
            component = new FormComponent(componentName, parentComponent, fieldName, dataSource, translationService);
        } else if ("checkbox".equals(componentType)) {
            component = new CheckBoxComponent(componentName, parentComponent, fieldName, dataSource, translationService);
        } else if ("select".equals(componentType)) {
            component = new DynamicComboBoxComponent(componentName, parentComponent, fieldName, dataSource, translationService);
        } else if ("lookup".equals(componentType)) {
            component = new EntityComboBoxComponent(componentName, parentComponent, fieldName, dataSource, translationService);
        } else if ("button".equals(componentType)) {
            component = new LinkButtonComponent(componentName, parentComponent, fieldName, dataSource, translationService);
        } else {
            throw new IllegalStateException("Unsupported component: " + componentType);
        }

        addMenuAndChildrenComponentsAndOptions(reader, (AbstractComponent<?>) component);

        return component;
    }

    private void addMenuAndChildrenComponentsAndOptions(final XMLStreamReader reader, final AbstractComponent<?> component)
            throws XMLStreamException {
        component.setDefaultEnabled(getBooleanAttribute(reader, "enabled", true));
        component.setDefaultVisible(getBooleanAttribute(reader, "visible", true));

        while (reader.hasNext() && reader.next() > 0) {
            if (isTagStarted(reader, "ribbon")) {
                component.setRibbon(getRibbon(reader));
            } else if (isTagStarted(reader, "option")) {
                component.addRawOption(getOption(reader));
            } else if (isTagStarted(reader, "component")) {
                if (component instanceof AbstractContainerComponent) {
                    ((AbstractContainerComponent<?>) component).addComponent(getComponentDefinition(reader,
                            (AbstractContainerComponent<?>) component));
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
        checkState(hasText(fullyQualifiedClassName), "Hook bean name is required");
        checkState(hasText(methodName), "Hook method name is required");
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
