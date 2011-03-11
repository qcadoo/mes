package com.qcadoo.plugin.internal.descriptorparser;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.google.common.base.Preconditions;
import com.qcadoo.plugin.api.Plugin;
import com.qcadoo.plugin.internal.DefaultPlugin.Builder;
import com.qcadoo.plugin.internal.api.ModuleFactory;
import com.qcadoo.plugin.internal.api.ModuleFactoryAccessor;
import com.qcadoo.plugin.internal.api.PluginDescriptorParser;
import com.qcadoo.plugin.internal.api.PluginDescriptorResolver;

@Service
public class DefaultPluginDescriptorParser implements PluginDescriptorParser {

    @Autowired
    private ModuleFactoryAccessor moduleFactoryAccessor;

    @Autowired
    private PluginDescriptorResolver pluginXmlResolver;

    public DefaultPluginDescriptorParser() {
        // remove me
    }

    public DefaultPluginDescriptorParser(final ModuleFactoryAccessor moduleFactoryAccessor,
            final PluginDescriptorResolver pluginXmlResolver) {
        this.moduleFactoryAccessor = moduleFactoryAccessor;
        this.pluginXmlResolver = pluginXmlResolver;
    }

    @Override
    public Plugin parse(final File file) {

        DocumentBuilder documentBuilder;
        try {
            documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document document = documentBuilder.parse(file);

            Node root = document.getDocumentElement();

            return parsePluginNode(root);

        } catch (ParserConfigurationException e) {
            throw new IllegalStateException(e.getMessage(), e);
        } catch (SAXException e) {
            throw new IllegalStateException(e.getMessage(), e);
        } catch (IOException e) {
            throw new IllegalStateException(e.getMessage(), e);
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
                pluginBuilder.withName(child.getTextContent());
            } else if ("description".equals(child.getNodeName())) {
                pluginBuilder.withDescription(child.getTextContent());
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
                pluginBuilder.withVendor(child.getTextContent());
            } else if ("url".equals(child.getNodeName())) {
                pluginBuilder.withVendorUrl(child.getTextContent());
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
                dependencyPluginIdentifier = child.getTextContent();
            } else if ("version".equals(child.getNodeName())) {
                dependencyPluginVersion = child.getTextContent();
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
        for (File xmlFile : pluginXmlResolver.getDescriptors()) {
            loadedplugins.add(parse(xmlFile));
        }
        return loadedplugins;
    }

}
