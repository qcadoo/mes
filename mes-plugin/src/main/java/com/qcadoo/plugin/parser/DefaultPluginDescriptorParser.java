package com.qcadoo.plugin.parser;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.google.common.base.Preconditions;
import com.qcadoo.plugin.DefaultPlugin.Builder;
import com.qcadoo.plugin.ModuleFactory;
import com.qcadoo.plugin.ModuleFactoryAccessor;
import com.qcadoo.plugin.Plugin;
import com.qcadoo.plugin.PluginDescriptorParser;
import com.qcadoo.plugin.PluginException;

public class DefaultPluginDescriptorParser implements PluginDescriptorParser {

    private final ModuleFactoryAccessor moduleFactoryAccessor;

    private final PluginXmlResolver pluginXmlResolver;

    public DefaultPluginDescriptorParser(final ModuleFactoryAccessor moduleFactoryAccessor,
            final PluginXmlResolver pluginXmlResolver) {
        this.moduleFactoryAccessor = moduleFactoryAccessor;
        this.pluginXmlResolver = pluginXmlResolver;
    }

    @Override
    public Plugin parse(final File file) throws PluginException {

        DocumentBuilder documentBuilder;
        try {
            documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document document = documentBuilder.parse(file);

            Node root = document.getDocumentElement();

            return parsePluginNode(root);

        } catch (ParserConfigurationException e) {
            throw new PluginException(e.getMessage(), e);
        } catch (SAXException e) {
            throw new PluginException(e.getMessage(), e);
        } catch (IOException e) {
            throw new PluginException(e.getMessage(), e);
        } catch (Exception e) {
            throw new PluginException(e.getMessage(), e);
        }
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
            } else {
                ModuleFactory<?> moduleFactory = moduleFactoryAccessor.getModuleFactory(child.getNodeName());
                pluginBuilder.withModule(moduleFactory.parse(child));
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
        // pluginBuilder.withDependency(dependencyPluginIdentifier, dependencyPluginVersion);
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

    @Override
    public Set<Plugin> loadPlugins() {
        Set<Plugin> loadedplugins = new HashSet<Plugin>();
        for (File xmlFile : pluginXmlResolver.getPluginXmlFiles()) {
            loadedplugins.add(parse(xmlFile));
        }
        return loadedplugins;
    }

}
