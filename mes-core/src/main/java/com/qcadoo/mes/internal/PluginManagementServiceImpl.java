package com.qcadoo.mes.internal;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.io.IOException;

import javax.annotation.PostConstruct;
import javax.xml.parsers.ParserConfigurationException;

import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.classic.Session;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.SAXException;

import com.qcadoo.mes.api.PluginManagementService;
import com.qcadoo.mes.beans.plugins.PluginsPlugin;
import com.qcadoo.mes.enums.PluginStatus;
import com.qcadoo.mes.exceptions.PluginException;
import com.qcadoo.mes.utils.PluginUtil;

@Service
public final class PluginManagementServiceImpl implements PluginManagementService {

    private static final Logger LOG = LoggerFactory.getLogger(PluginManagementServiceImpl.class);

    private static final String libPath = "WEB-INF/lib/";

    private static final String tmpPath = "WEB-INF/tmp/";

    private static final String pluginView = "redirect:page/plugins/pluginGridView.html?iframe=true";

    public static final String FIELD_DELETED = "deleted";

    @Autowired
    private SessionFactory sessionFactory;

    @Autowired
    private ApplicationContext applicationContext;

    private String webappPath;

    @PostConstruct
    public void init() {
        // TOMCAT
        webappPath = ((WebApplicationContext) applicationContext).getServletContext().getRealPath("/");
        if (!webappPath.endsWith("/")) {
            // JETTY
            webappPath = webappPath + "/";
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug(webappPath);
        }
    }

    @Override
    @Transactional
    public String downloadPlugin(final MultipartFile file) {
        if (!file.isEmpty()) {
            File pluginFile = null;
            boolean deleteFile = false;
            try {
                pluginFile = PluginUtil.transferFileToTmp(file, webappPath + tmpPath);
                PluginsPlugin plugin = PluginUtil.readDescriptor(pluginFile);
                PluginsPlugin pluginWithIdentifier = getByIdentifier(plugin.getIdentifier());
                if (pluginWithIdentifier != null) {
                    deleteFile = true;
                    LOG.info("Plugin with identifier existed");
                    return pluginView + "&message=Istnieje plugin z podanym identyfikatorem";
                }
                PluginsPlugin databasePlugin = getByNameAndVendor(plugin.getName(), plugin.getVendor());
                if (databasePlugin != null) {
                    deleteFile = true;
                    LOG.info("Plugin was installed");
                    return pluginView + "&message=Plugin jest zainstalowany";
                } else {
                    plugin.setDeleted(false);
                    plugin.setStatus(PluginStatus.DOWNLOADED.getValue());
                    plugin.setBase(false);
                    plugin.setFileName(file.getOriginalFilename());
                    save(plugin);
                    return pluginView;
                }
            } catch (IllegalStateException e) {
                deleteFile = true;
                LOG.error("Problem with installing file - " + e.getMessage());
                return pluginView + "&message=Blad instalacji pliku";
            } catch (IOException e) {
                deleteFile = true;
                LOG.error("Problem with installing file - " + e.getMessage());
                return pluginView + "&message=Blad instalacji pliku";
            } catch (ParserConfigurationException e) {
                deleteFile = true;
                LOG.error("Problem with parsing descriptor - " + e.getMessage());
                return pluginView + "&message=Blad odczytu deskryptora";
            } catch (SAXException e) {
                deleteFile = true;
                LOG.error("Problem with parsing descriptor - " + e.getMessage());
                return pluginView + "&message=Blad odczytu deskryptora";
            } finally {
                if (deleteFile && pluginFile != null && pluginFile.exists()) {
                    boolean success = pluginFile.delete();
                    if (!success) {
                        LOG.error("Problem with removing plugin file");
                    }
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Plugin file delete");
                    }
                }
            }
        } else {
            LOG.info("Chosen file is empty");
            return pluginView + "&message=Plik jest pusty";
        }
    }

    @Override
    @Transactional
    public String removePlugin(final String entityId) {
        PluginsPlugin databasePlugin = getByEntityId(entityId);
        if (databasePlugin.isBase()) {
            LOG.info("Plugin is base");
            return pluginView + "&message=Plugin jest bazowy";
        } else if (!databasePlugin.getStatus().equals(PluginStatus.DOWNLOADED.getValue())) {
            LOG.info("Plugin hasn't apropriate status");
            return pluginView + "&message=Niepoprawny status do usuwania";
        }
        try {
            databasePlugin.setDeleted(true);

            save(databasePlugin);

            PluginUtil.removePluginFile(webappPath + tmpPath + databasePlugin.getFileName());
        } catch (PluginException e) {
            LOG.error("Problem with removing plugin file - " + e.getMessage());
            return pluginView + "&message=Blad usuwania pliku";
        }

        return "redirect:removePage.html";
    }

    @Override
    @Transactional
    public String enablePlugin(final String entityId) {
        PluginsPlugin plugin = getByEntityId(entityId);
        if (plugin.isBase()) {
            LOG.info("Plugin is base");
            return pluginView + "&message=Plugin jest bazowy";
        } else if (plugin.getStatus().equals(PluginStatus.ACTIVE.getValue())) {
            LOG.info("Plugin hasn't apropriate status");
            return pluginView + "&message=Niepoprawny status do włączania";
        }
        String pluginStatus = plugin.getStatus();
        plugin.setStatus(PluginStatus.ACTIVE.getValue());
        save(plugin);
        if (pluginStatus.equals(PluginStatus.INSTALLED.getValue())) {
            return pluginView;
        } else {
            try {
                PluginUtil.movePluginFile(webappPath + tmpPath + plugin.getFileName(), webappPath + libPath);
            } catch (PluginException e) {
                plugin.setStatus(PluginStatus.DOWNLOADED.getValue());
                save(plugin);
                LOG.error("Problem with moving plugin file - " + e.getMessage());
                return pluginView + "&message=Blad przenoszenia pliku";
            }
            return "redirect:enablePage.html";
        }
    }

    @Override
    public String restartServer() {
        try {
            PluginUtil.restartServer(webappPath);
        } catch (PluginException e) {
            LOG.error("Problem with restart server - " + e.getMessage());
            return pluginView + "&message=Blad restartu serwera";
        }
        return "ok";
    }

    @Override
    @Transactional
    public String disablePlugin(final String entityId) {
        PluginsPlugin plugin = getByEntityId(entityId);
        if (plugin.isBase()) {
            LOG.info("Plugin is base");
            return pluginView + "&message=Plugin jest bazowy";
        } else if (!plugin.getStatus().equals(PluginStatus.ACTIVE.getValue())) {
            LOG.info("Plugin hasn't apropriate status");
            return pluginView + "&message=Niepoprawny status do wyłączania";
        }
        plugin.setStatus(PluginStatus.INSTALLED.getValue());
        save(plugin);
        return pluginView;
    }

    @Override
    @Transactional
    public String deinstallPlugin(final String entityId) {
        PluginsPlugin databasePlugin = getByEntityId(entityId);
        if (databasePlugin.isBase()) {
            LOG.info("Plugin is base");
            return pluginView + "&message=Plugin jest bazowy";
        } else if (databasePlugin.getStatus().equals(PluginStatus.DOWNLOADED.getValue())) {
            LOG.info("Plugin hasn't apropriate status");
            return pluginView + "&message=Niepoprawny status do odinstalowania";
        }
        try {
            databasePlugin.setDeleted(true);
            save(databasePlugin);
            PluginUtil.removePluginFile(webappPath + libPath + databasePlugin.getFileName());
        } catch (PluginException e) {
            LOG.error("Problem with removing plugin file - " + e.getMessage());
            return pluginView + "&message=Blad usuwania pliku";
        }
        PluginUtil.removeResources("js", webappPath + "/" + "js" + "/" + databasePlugin.getIdentifier());
        PluginUtil.removeResources("css", webappPath + "/" + "css" + "/" + databasePlugin.getIdentifier());
        PluginUtil.removeResources("img", webappPath + "/" + "img" + "/" + databasePlugin.getIdentifier());
        PluginUtil.removeResources("jsp", webappPath + "/" + "WEB-INF/jsp" + "/" + databasePlugin.getIdentifier());
        return "redirect:deinstallPage.html";
    }

    @Override
    @Transactional
    public String updatePlugin(final MultipartFile file) {
        if (!file.isEmpty()) {
            File pluginFile = null;
            boolean deleteFile = false;
            try {
                pluginFile = PluginUtil.transferFileToTmp(file, webappPath + tmpPath);
                PluginsPlugin plugin = PluginUtil.readDescriptor(pluginFile);
                PluginsPlugin databasePlugin = getByNameAndVendor(plugin.getName(), plugin.getVendor());
                if (databasePlugin == null) {
                    deleteFile = true;
                    LOG.info("Plugin not found in database");
                    return pluginView + "&message=Brak pluginu";
                } else if (databasePlugin.isBase()) {
                    deleteFile = true;
                    LOG.info("Plugin is base");
                    return pluginView + "&message=Plugin jest bazowy";
                } else if (databasePlugin.getStatus().equals(PluginStatus.DOWNLOADED.getValue())) {
                    deleteFile = true;
                    LOG.info("Plugin hasn't apropriate status");
                    return pluginView + "&message=Niepoprawny status do aktualizacji";
                } else if (databasePlugin.getVersion().compareTo(plugin.getVersion()) >= 0) {
                    deleteFile = true;
                    LOG.info("Plugin has actual version");
                    return pluginView + "&message=Plugin jest aktualny";
                } else {
                    plugin.setFileName(file.getOriginalFilename());
                    movePlugin(plugin, databasePlugin);
                    return "redirect:enablePage.html";
                }
            } catch (IllegalStateException e) {
                deleteFile = true;
                LOG.error("Problem with installing file - " + e.getMessage());
                return pluginView + "&message=Blad instalacji pliku";
            } catch (IOException e) {
                deleteFile = true;
                LOG.error("Problem with installing file - " + e.getMessage());
                return pluginView + "&message=Blad instalacji pliku";
            } catch (ParserConfigurationException e) {
                deleteFile = true;
                LOG.error("Problem with parsing descriptor - " + e.getMessage());
                return pluginView + "&message=Blad odczytu deskryptora";
            } catch (SAXException e) {
                deleteFile = true;
                LOG.error("Problem with parsing descriptor - " + e.getMessage());
                return pluginView + "&message=Blad odczytu deskryptora";
            } catch (PluginException e) {
                deleteFile = true;
                LOG.error("Problem with moving/removing plugin file - " + e.getMessage());
                return pluginView + "&message=Blad usuwania/przenoszenia pliku";
            } finally {
                if (deleteFile && pluginFile != null && pluginFile.exists()) {
                    boolean success = pluginFile.delete();
                    if (!success) {
                        LOG.error("Problem with removing plugin file");
                    }
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Plugin file delete");
                    }
                }
            }
        } else {
            LOG.info("Chosen file is empty");
            return pluginView + "&message=Plik jest pusty";
        }
    }

    @Override
    public PluginsPlugin getByIdentifierAndStatus(final String identifier, final String status) {
        checkNotNull(identifier, "identifier must be given");
        checkNotNull(status, "status must be given");
        Criteria criteria = getCurrentSession().createCriteria(PluginsPlugin.class)
                .add(Restrictions.eq("identifier", identifier)).add(Restrictions.eq("status", status))
                .add(Restrictions.eq(FIELD_DELETED, false));
        if (LOG.isDebugEnabled()) {
            LOG.debug("get plugin with identifier: " + identifier + " and status: " + status);
        }
        return (PluginsPlugin) criteria.uniqueResult();
    }

    @Override
    public PluginsPlugin getByIdentifier(final String identifier) {
        checkNotNull(identifier, "identifier must be given");
        Criteria criteria = getCurrentSession().createCriteria(PluginsPlugin.class)
                .add(Restrictions.eq("identifier", identifier)).add(Restrictions.eq(FIELD_DELETED, false));
        if (LOG.isDebugEnabled()) {
            LOG.debug("get plugin with identifier: " + identifier);
        }
        return (PluginsPlugin) criteria.uniqueResult();
    }

    @Override
    public PluginsPlugin getByEntityId(final String entityId) {
        checkNotNull(entityId, "entityId must be given");
        Criteria criteria = getCurrentSession().createCriteria(PluginsPlugin.class)
                .add(Restrictions.idEq(Long.valueOf(entityId))).add(Restrictions.eq(FIELD_DELETED, false));
        if (LOG.isDebugEnabled()) {
            LOG.debug("get plugin with id: " + entityId);
        }

        return (PluginsPlugin) criteria.uniqueResult();
    }

    @Override
    public PluginsPlugin getByNameAndVendor(final String name, final String vendor) {
        checkNotNull(vendor, "vendor must be given");
        checkNotNull(name, "name must be given");
        Criteria criteria = getCurrentSession().createCriteria(PluginsPlugin.class).add(Restrictions.eq("name", name))
                .add(Restrictions.eq("vendor", vendor)).add(Restrictions.eq(FIELD_DELETED, false));
        if (LOG.isDebugEnabled()) {
            LOG.debug("get plugin with name: " + name + " and vendor: " + vendor);
        }
        return (PluginsPlugin) criteria.uniqueResult();
    }

    @Override
    public void save(final PluginsPlugin plugin) {
        checkNotNull(plugin, "plugin must be given");
        getCurrentSession().save(plugin);
    }

    private void movePlugin(final PluginsPlugin plugin, final PluginsPlugin databasePlugin) throws PluginException {
        plugin.setDeleted(false);
        plugin.setStatus(databasePlugin.getStatus());
        plugin.setBase(false);
        databasePlugin.setDeleted(true);
        save(databasePlugin);

        save(plugin);

        PluginUtil.removePluginFile(webappPath + libPath + databasePlugin.getFileName());

        PluginUtil.movePluginFile(webappPath + tmpPath + plugin.getFileName(), webappPath + libPath);

        PluginUtil.removeResources("js", webappPath + "/" + "js" + "/" + databasePlugin.getIdentifier());
        PluginUtil.removeResources("css", webappPath + "/" + "css" + "/" + databasePlugin.getIdentifier());
        PluginUtil.removeResources("img", webappPath + "/" + "img" + "/" + databasePlugin.getIdentifier());
        PluginUtil.removeResources("jsp", webappPath + "/" + "WEB-INF/jsp" + "/" + databasePlugin.getIdentifier());
    }

    private Session getCurrentSession() {
        return sessionFactory.getCurrentSession();
    }

}
