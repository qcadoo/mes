package com.qcadoo.plugin.internal.descriptorparser;

import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.google.common.base.Preconditions;
import com.qcadoo.plugin.api.Plugin;
import com.qcadoo.plugin.internal.DefaultPlugin.Builder;
import com.qcadoo.plugin.internal.PluginException;
import com.qcadoo.plugin.internal.api.ModuleFactory;
import com.qcadoo.plugin.internal.api.ModuleFactoryAccessor;
import com.qcadoo.plugin.internal.api.PluginDescriptorParser;
import com.qcadoo.plugin.internal.api.PluginDescriptorResolver;

@Service
public class DefaultPluginDescriptorParser implements PluginDescriptorParser {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultPluginDescriptorParser.class);

    @Autowired
    private ModuleFactoryAccessor moduleFactoryAccessor;

    @Autowired
    private PluginDescriptorResolver pluginDescriptorResolver;

    private DocumentBuilder documentBuilder;

    public DefaultPluginDescriptorParser() {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setValidating(true);
            factory.setNamespaceAware(true);

            // TODOq

            // SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            // schemaFactory.setFeature("http://apache.org/xml/features/validation/schema-full-checking", false);
            //
            // Schema schema = schemaFactory.newSchema(new StreamSource(new ClassPathResource("com/qcadoo/plugin/plugin.xsd")
            // .getInputStream()));
            // factory.setSchema(schema);

            documentBuilder = factory.newDocumentBuilder();

            documentBuilder.setErrorHandler(new ValidationErrorHandler());

            // } catch (SAXException e) {
            // throw new IllegalStateException("Error while parsing plugin xml schema", e);
        } catch (ParserConfigurationException e) {
            throw new IllegalStateException("Error while parsing plugin xml schema", e);
            // } catch (IOException e) {
            // throw new IllegalStateException("Error while parsing plugin xml schema", e);
        }
    }

    @Override
    public Plugin parse(final Resource resource) throws PluginException {

        try {
            LOG.info("Parsing: " + resource);

            Document document = documentBuilder.parse(resource.getInputStream());

            Node root = document.getDocumentElement();

            Plugin plugin = parsePluginNode(root);

            LOG.info("Parse complete");

            return plugin;

        } catch (SAXException e) {
            throw new PluginException(e.getMessage(), e);
        } catch (IOException e) {
            throw new PluginException(e.getMessage(), e);
        } catch (Exception e) {
            throw new PluginException(e.getMessage(), e);
        }
    }

    @Override
    public Set<Plugin> loadPlugins() {
        Set<Plugin> loadedplugins = new HashSet<Plugin>();
        for (Resource resource : pluginDescriptorResolver.getDescriptors()) {
            loadedplugins.add(parse(resource));
        }
        return loadedplugins;
    }

    private Plugin parsePluginNode(final Node pluginNode) {
        Preconditions.checkState("plugin".equals(pluginNode.getNodeName()), "Wrong plugin description root tag");

        String pluginIdentifier = getStringAttribute(pluginNode, "plugin");
        Preconditions.checkNotNull(pluginIdentifier, "No plugin identifier");
        Builder pluginBuilder = new Builder(pluginIdentifier);

        String pluginVersionStr = getStringAttribute(pluginNode, "version");
        Preconditions.checkNotNull(pluginVersionStr, "No plugin version");
        pluginBuilder.withVersion(pluginVersionStr);

        String isSystemPluginStr = getStringAttribute(pluginNode, "system");
        if (isSystemPluginStr != null && Boolean.parseBoolean(isSystemPluginStr)) {
            pluginBuilder.asSystem();
        }

        for (Node child : getChildNodes(pluginNode)) {
            if ("information".equals(child.getNodeName())) {
                addPluginInformation(child, pluginBuilder);
            } else if ("dependencies".equals(child.getNodeName())) {
                addDependenciesInformation(child, pluginBuilder);
            } else if ("modules".equals(child.getNodeName())) {
                addModules(child, pluginBuilder);
            } else {
                throw new IllegalStateException("Wrong plugin tag: " + child.getNodeName());
            }
        }

        return pluginBuilder.build();
    }

    private void addPluginInformation(final Node informationsNode, final Builder pluginBuilder) {
        for (Node child : getChildNodes(informationsNode)) {
            if ("name".equals(child.getNodeName())) {
                pluginBuilder.withName(getTextContent(child));
            } else if ("description".equals(child.getNodeName())) {
                pluginBuilder.withDescription(getTextContent(child));
            } else if ("vendor".equals(child.getNodeName())) {
                addPluginVendorInformation(child, pluginBuilder);
            } else {
                throw new IllegalStateException("Wrong plugin information tag: " + child.getNodeName());
            }
        }
    }

    private void addPluginVendorInformation(final Node vendorInformationsNode, final Builder pluginBuilder) {
        for (Node child : getChildNodes(vendorInformationsNode)) {
            if ("name".equals(child.getNodeName())) {
                pluginBuilder.withVendor(getTextContent(child));
            } else if ("url".equals(child.getNodeName())) {
                pluginBuilder.withVendorUrl(getTextContent(child));
            } else {
                throw new IllegalStateException("Wrong plugin vendor tag: " + child.getNodeName());
            }
        }
    }

    private void addDependenciesInformation(final Node dependenciesNode, final Builder pluginBuilder) {
        for (Node child : getChildNodes(dependenciesNode)) {
            if ("dependency".equals(child.getNodeName())) {
                addDependencyInformation(child, pluginBuilder);
            } else {
                throw new IllegalStateException("Wrong plugin dependency tag: " + child.getNodeName());
            }
        }
    }

    private void addDependencyInformation(final Node dependencyNode, final Builder pluginBuilder) {
        String dependencyPluginIdentifier = null;
        String dependencyPluginVersion = null;

        for (Node child : getChildNodes(dependencyNode)) {
            if ("plugin".equals(child.getNodeName())) {
                dependencyPluginIdentifier = getTextContent(child);
            } else if ("version".equals(child.getNodeName())) {
                dependencyPluginVersion = getTextContent(child);
            } else {
                throw new IllegalStateException("Wrong plugin dependency tag: " + child.getNodeName());
            }
        }

        Preconditions.checkNotNull(dependencyPluginIdentifier, "No plugin dependency identifier");
        pluginBuilder.withDependency(dependencyPluginIdentifier, dependencyPluginVersion);
    }

    private void addModules(final Node modulesNode, final Builder pluginBuilder) {
        for (Node child : getChildNodes(modulesNode)) {
            ModuleFactory<?> moduleFactory = moduleFactoryAccessor.getModuleFactory(child.getNodeName());
            pluginBuilder.withModule(moduleFactory.parse(child));
        }
    }

    private List<Node> getChildNodes(final Node node) {
        List<Node> result = new LinkedList<Node>();
        NodeList childNodes = node.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node child = childNodes.item(i);
            if (child.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            result.add(child);
        }
        return result;
    }

    private String getTextContent(final Node node) {
        String result = node.getTextContent();
        if (result != null) {
            result = result.trim();
            if (result.isEmpty()) {
                return null;
            }
            return result;
        }
        return null;
    }

    private String getStringAttribute(final Node node, final String name) {
        if (node != null && node.getAttributes() != null) {
            Node attribute = node.getAttributes().getNamedItem(name);
            if (attribute != null) {
                return attribute.getNodeValue();
            }
        }
        return null;
    }

    private class ValidationErrorHandler implements ErrorHandler {

        @Override
        public void warning(final SAXParseException e) throws SAXException {
            LOG.debug(e.getMessage());
        }

        @Override
        public void error(final SAXParseException e) throws SAXException {
            LOG.debug(e.getMessage());
        }

        @Override
        public void fatalError(final SAXParseException e) throws SAXException {
            LOG.error(e.getMessage());
        }
    }

    public void setModuleFactoryAccessor(final ModuleFactoryAccessor moduleFactoryAccessor) {
        this.moduleFactoryAccessor = moduleFactoryAccessor;
    }

    public void setPluginDescriptorResolver(final PluginDescriptorResolver pluginDescriptorResolver) {
        this.pluginDescriptorResolver = pluginDescriptorResolver;
    }

}
