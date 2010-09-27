package com.qcadoo.mes.core.internal.xml;

import static com.google.common.base.Preconditions.checkState;
import static org.springframework.util.StringUtils.hasText;

import java.io.IOException;
import java.io.InputStream;

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

import com.qcadoo.mes.core.api.DataDefinitionService;
import com.qcadoo.mes.core.api.ViewDefinitionService;
import com.qcadoo.mes.core.internal.hooks.HookFactory;
import com.qcadoo.mes.core.internal.view.ViewDefinitionImpl;
import com.qcadoo.mes.core.model.DataDefinition;
import com.qcadoo.mes.core.model.HookDefinition;
import com.qcadoo.mes.core.types.FieldTypeFactory;
import com.qcadoo.mes.core.validation.ValidatorFactory;
import com.qcadoo.mes.core.view.AbstractComponent;
import com.qcadoo.mes.core.view.Component;
import com.qcadoo.mes.core.view.ContainerComponent;
import com.qcadoo.mes.core.view.RootComponent;
import com.qcadoo.mes.core.view.containers.FormComponent;
import com.qcadoo.mes.core.view.containers.WindowComponent;
import com.qcadoo.mes.core.view.elements.CheckBoxComponent;
import com.qcadoo.mes.core.view.elements.DynamicComboBox;
import com.qcadoo.mes.core.view.elements.EntityComboBox;
import com.qcadoo.mes.core.view.elements.GridComponent;
import com.qcadoo.mes.core.view.elements.LinkButtonComponent;
import com.qcadoo.mes.core.view.elements.TextInputComponent;

@Service
public final class ViewDefinitionParser {

    private static final Logger LOG = LoggerFactory.getLogger(ViewDefinitionParser.class);

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private ViewDefinitionService viewDefinitionService;

    @Autowired
    private FieldTypeFactory fieldTypeFactory;

    @Autowired
    private HookFactory hookFactory;

    @Autowired
    private ValidatorFactory validatorFactory;

    @Autowired
    private ApplicationContext applicationContext;

    // @PostConstruct
    public void init() {
        LOG.info("Reading view definitions ...");

        try {
            Resource[] resources = applicationContext.getResources("classpath*:view.xml");
            for (Resource resource : resources) {
                parse(resource.getInputStream());
            }
        } catch (IOException e) {
            LOG.error("Cannot read view definition", e);
        }

        viewDefinitionService.initViews();
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

        DataDefinition dataDefinition = dataDefinitionService.get(pluginIdentifier, getStringAttribute(reader, "forEntity"));

        while (reader.hasNext() && reader.next() > 0) {
            if (isTagStarted(reader, "window")) {
                viewDefinition.setRoot(getRootComponentDefinition(reader, viewName, dataDefinition));
            } else if (isTagStarted(reader, "onView")) {
                viewDefinition.setViewHook(getHookDefinition(reader));
            }
        }

        viewDefinitionService.save(viewDefinition);
    }

    private RootComponent getRootComponentDefinition(final XMLStreamReader reader, final String viewName,
            final DataDefinition dataDefinition) throws XMLStreamException {
        String componentType = reader.getLocalName();
        String componentName = getStringAttribute(reader, "name");

        RootComponent component = null;

        if ("window".equals(componentType)) {
            component = new WindowComponent(componentName, dataDefinition, viewName);
        } else {
            throw new IllegalStateException("Unsupported component: " + componentType);
        }

        addChildComponents(reader, component);

        addComponentOptions(reader, component);

        return component;
    }

    private Component<?> getComponentDefinition(final XMLStreamReader reader, final ContainerComponent<?> parentComponent)
            throws XMLStreamException {
        String componentType = reader.getLocalName();
        String componentName = getStringAttribute(reader, "name");
        String fieldName = getStringAttribute(reader, "field");
        String dataSource = getStringAttribute(reader, "dataSource");

        Component<?> component = null;

        if ("input".equals(componentType)) {
            component = new TextInputComponent(componentName, parentComponent, fieldName, dataSource);
        } else if ("grid".equals(componentType)) {
            component = new GridComponent(componentName, parentComponent, fieldName, dataSource);
        } else if ("form".equals(componentType)) {
            component = new FormComponent(componentName, parentComponent, fieldName, dataSource);
        } else if ("checkbox".equals(componentType)) {
            component = new CheckBoxComponent(componentName, parentComponent, fieldName, dataSource);
        } else if ("select".equals(componentType)) {
            component = new DynamicComboBox(componentName, parentComponent, fieldName, dataSource);
        } else if ("lookup".equals(componentType)) {
            component = new EntityComboBox(componentName, parentComponent, fieldName, dataSource);
        } else if ("button".equals(componentType)) {
            component = new LinkButtonComponent(componentName, parentComponent, fieldName, dataSource);
        } else {
            throw new IllegalStateException("Unsupported component: " + componentType);
        }

        if (component instanceof ContainerComponent<?>) {
            addChildComponents(reader, (ContainerComponent<?>) component);
        }

        addComponentOptions(reader, component);

        return component;
    }

    private void addComponentOptions(final XMLStreamReader reader, final Component<?> component) throws XMLStreamException {
        ((AbstractComponent<?>) component).setDefaultEnabled(getBooleanAttribute(reader, "enabled", true));
        ((AbstractComponent<?>) component).setDefaultVisible(getBooleanAttribute(reader, "visible", true));

        while (reader.hasNext() && reader.next() > 0) {
            if (isTagEnded(reader, "options")) {
                break;
                // } else if (isTagStarted(reader, "validatesPresence")) {
                // fieldDefinition.withValidator(getValidatorDefinition(reader, validatorFactory.required()));
            }
        }
    }

    private void addChildComponents(final XMLStreamReader reader, final ContainerComponent<?> component)
            throws XMLStreamException {
        while (reader.hasNext() && reader.next() > 0) {
            if (isTagStarted(reader, "options")) {
                break;
            } else if (isTagStarted(reader, "input")) {
                component.addComponent(getComponentDefinition(reader, component));
            } else if (isTagStarted(reader, "grid")) {
                component.addComponent(getComponentDefinition(reader, component));
            } else if (isTagStarted(reader, "form")) {
                component.addComponent(getComponentDefinition(reader, component));
            } else if (isTagStarted(reader, "checkbox")) {
                component.addComponent(getComponentDefinition(reader, component));
            } else if (isTagStarted(reader, "select")) {
                component.addComponent(getComponentDefinition(reader, component));
            } else if (isTagStarted(reader, "lookup")) {
                component.addComponent(getComponentDefinition(reader, component));
            } else if (isTagStarted(reader, "button")) {
                component.addComponent(getComponentDefinition(reader, component));
            }
        }
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
        return (reader.getEventType() == XMLStreamConstants.START_ELEMENT && tagName.equals(reader.getLocalName()));
    }

    private boolean isTagEnded(final XMLStreamReader reader, final String tagName) {
        return (reader.getEventType() == XMLStreamConstants.END_ELEMENT && tagName.equals(reader.getLocalName()));
    }

}
