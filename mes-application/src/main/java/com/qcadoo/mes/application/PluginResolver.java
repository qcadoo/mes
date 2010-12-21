package com.qcadoo.mes.application;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.qcadoo.mes.api.PluginManagementService;
import com.qcadoo.mes.beans.plugins.PluginsPlugin;

@Component
public final class PluginResolver implements ApplicationListener<ContextRefreshedEvent> {

    private static final Logger LOG = LoggerFactory.getLogger(PluginResolver.class);

    @Autowired
    private SessionFactory sessionFactory;

    @Autowired
    private PluginManagementService pluginManagementService;

    private final Set<String> identifiers = new HashSet<String>();

    private static final Pattern pattern = Pattern.compile(".*/([^/]+\\.jar).*");

    @Override
    @Transactional
    public void onApplicationEvent(final ContextRefreshedEvent event) {
        LOG.info("Reading plugins definitions ...");

        try {
            Resource[] resources = event.getApplicationContext().getResources("classpath*:plugin.xml");
            for (Resource resource : resources) {
                if (resource.isReadable()) {
                    parse(resource.getInputStream(), resource.getURL().toString());
                }
            }

        } catch (IOException e) {
            LOG.error("Cannot read view definition", e);
        }
    }

    private void parse(final InputStream inputStream, final String path) {
        try {
            DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document document = documentBuilder.parse(inputStream);

            NodeList childNodes = document.getDocumentElement().getChildNodes();

            String identifier = null;
            String name = null;
            String packageName = null;
            String version = null;
            String vendor = null;
            String description = null;

            for (int i = 0; i < childNodes.getLength(); i++) {
                Node child = childNodes.item(i);

                if ("identifier".equals(child.getNodeName())) {
                    identifier = getTextValue(child);
                } else if ("name".equals(child.getNodeName())) {
                    name = getTextValue(child);
                } else if ("packageName".equals(child.getNodeName())) {
                    packageName = getTextValue(child);
                } else if ("version".equals(child.getNodeName())) {
                    version = getTextValue(child);
                } else if ("vendor".equals(child.getNodeName())) {
                    vendor = getTextValue(child);
                } else if ("description".equals(child.getNodeName())) {
                    description = getTextValue(child);
                }
            }

            if (identifier == null) {
                return;
            }

            if (identifiers.contains(identifier)) {
                throw new IllegalStateException("Duplicated plugin identifier : " + identifier);
            }

            identifiers.add(identifier);

            Matcher matcher = pattern.matcher(path);

            if (matcher.find()) {
                addPlugin(identifier, name, packageName, version, vendor, description, matcher.group(1));
            } else {
                throw new IllegalStateException("Cannot find jar file for path " + path);
            }
        } catch (ParserConfigurationException e) {
            throw new IllegalStateException(e.getMessage(), e);
        } catch (SAXException e) {
            throw new IllegalStateException(e.getMessage(), e);
        } catch (IOException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    private String getTextValue(final Node node) {
        NodeList childNodes = node.getChildNodes();

        for (int i = 0; i < childNodes.getLength(); i++) {
            Node child = childNodes.item(i);

            if (child.getNodeType() == Node.TEXT_NODE) {
                return child.getNodeValue().trim();
            }
        }

        return null;
    }

    private void addPlugin(final String identifier, final String name, final String packageName, final String version,
            final String vendor, final String description, final String filename) {
        PluginsPlugin plugin = getPlugin(identifier);

        if (plugin == null) {
            LOG.info("Adding plugin \"" + identifier + "\"");

            plugin = new PluginsPlugin();
            if ("crud".equals(identifier) || "plugins".equals(identifier)) {
                plugin.setBase(true);
            } else {
                plugin.setBase(false);
            }
            plugin.setIdentifier(identifier);
            plugin.setStatus("active");
        } else {
            LOG.info("Updating plugin \"" + identifier + "\"");

            if (pluginManagementService.compareVersions(plugin.getVersion(), version) > 0) {
                throw new IllegalStateException("Plugin cannot be automatically downgraded from " + plugin.getVersion() + " to "
                        + version);
            }
        }

        plugin.setDescription(description);
        plugin.setVersion(version);
        plugin.setVendor(vendor);
        plugin.setPackageName(packageName);
        plugin.setName(name);
        plugin.setFileName(filename);

        sessionFactory.getCurrentSession().save(plugin);
    }

    private PluginsPlugin getPlugin(final String identifier) {
        return (PluginsPlugin) sessionFactory.getCurrentSession().createCriteria(PluginsPlugin.class)
                .add(Restrictions.eq("identifier", identifier)).uniqueResult();
    }

}
