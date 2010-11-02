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

import com.qcadoo.mes.api.PluginManagementOperationStatus;
import com.qcadoo.mes.api.PluginManagementService;
import com.qcadoo.mes.beans.plugins.PluginsPlugin;
import com.qcadoo.mes.enums.PluginStatus;
import com.qcadoo.mes.exceptions.PluginException;
import com.qcadoo.mes.model.aop.internal.Monitorable;
import com.qcadoo.mes.utils.PluginUtil;

@Service
public final class PluginManagementServiceImpl implements PluginManagementService {

    private static final Logger LOG = LoggerFactory.getLogger(PluginManagementServiceImpl.class);

    private static final String LIB_PATH = "WEB-INF/lib/";

    private static final String TMP_PATH = "WEB-INF/tmp/";

    private static final String PLUGIN_VIEW = "redirect:page/plugins/pluginGridView.html?iframe=true";

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
    @Monitorable
    public PluginManagementOperationStatus downloadPlugin(final MultipartFile file) {
        if (!file.isEmpty()) {
            File pluginFile = null;
            boolean deleteFile = false;
            try {
                pluginFile = PluginUtil.transferFileToTmp(file, webappPath + TMP_PATH);
                PluginsPlugin plugin = PluginUtil.readDescriptor(pluginFile);
                PluginsPlugin pluginWithIdentifier = getByIdentifier(plugin.getIdentifier());
                if (pluginWithIdentifier != null) {
                    deleteFile = true;
                    // LOG.info("Plugin with identifier existed");
                    return new PluginManagementOperationStatus(true, "Istnieje plugin z podanym identyfikatorem");
                    // return PLUGIN_VIEW + "&message=Istnieje plugin z podanym identyfikatorem";
                }
                PluginsPlugin databasePlugin = getByNameAndVendor(plugin.getName(), plugin.getVendor());
                if (databasePlugin != null) {
                    deleteFile = true;
                    LOG.info("Plugin was installed");
                    return new PluginManagementOperationStatus(true, "Plugin jest zainstalowany");
                    // return PLUGIN_VIEW + "&message=Plugin jest zainstalowany";
                } else {
                    plugin.setDeleted(false);
                    plugin.setStatus(PluginStatus.DOWNLOADED.getValue());
                    plugin.setBase(false);
                    plugin.setFileName(file.getOriginalFilename());
                    save(plugin);
                    // return PLUGIN_VIEW;
                    return new PluginManagementOperationStatus(false, "jest ok");
                }
            } catch (IllegalStateException e) {
                deleteFile = true;
                LOG.error("Problem with installing file - " + e.getMessage());
                return new PluginManagementOperationStatus(true, "Blad instalacji pliku");
                // return PLUGIN_VIEW + "&message=Blad instalacji pliku";
            } catch (IOException e) {
                deleteFile = true;
                LOG.error("Problem with installing file - " + e.getMessage());
                // return PLUGIN_VIEW + "&message=Blad instalacji pliku";
                return new PluginManagementOperationStatus(true, "Blad instalacji pliku");
            } catch (ParserConfigurationException e) {
                deleteFile = true;
                LOG.error("Problem with parsing descriptor - " + e.getMessage());
                return new PluginManagementOperationStatus(true, "Blad odczytu deskryptora");
                // return PLUGIN_VIEW + "&message=Blad odczytu deskryptora";
            } catch (SAXException e) {
                deleteFile = true;
                LOG.error("Problem with parsing descriptor - " + e.getMessage());
                return new PluginManagementOperationStatus(true, "Blad odczytu deskryptora");
                // return PLUGIN_VIEW + "&message=Blad odczytu deskryptora";
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
            return new PluginManagementOperationStatus(true, "Plik jest pusty");
            // return PLUGIN_VIEW + "&message=Plik jest pusty";
        }
    }

    @Override
    @Transactional
    @Monitorable
    public PluginManagementOperationStatus removePlugin(final String entityId) {
        PluginsPlugin databasePlugin = getByEntityId(entityId);
        if (databasePlugin.isBase()) {
            LOG.info("Plugin is base");
            // return PLUGIN_VIEW + "&message=Plugin jest bazowy";
            return new PluginManagementOperationStatus(true, "Plugin jest bazowy");
        } else if (!databasePlugin.getStatus().equals(PluginStatus.DOWNLOADED.getValue())) {
            LOG.info("Plugin hasn't apropriate status");
            return new PluginManagementOperationStatus(true, "Niepoprawny status do usuwania");
            // return PLUGIN_VIEW + "&message=Niepoprawny status do usuwania";
        }
        try {
            databasePlugin.setDeleted(true);

            save(databasePlugin);

            PluginUtil.removePluginFile(webappPath + TMP_PATH + databasePlugin.getFileName());
        } catch (PluginException e) {
            LOG.error("Problem with removing plugin file - " + e.getMessage());
            return new PluginManagementOperationStatus(true, "Blad usuwania pliku");
            // return PLUGIN_VIEW + "&message=Blad usuwania pliku";
        }

        return new PluginManagementOperationStatus(false, "usunieto");
        // return "redirect:removePage.html";
    }

    @Override
    @Transactional
    @Monitorable
    public PluginManagementOperationStatus enablePlugin(final String entityId) {
        PluginsPlugin plugin = getByEntityId(entityId);
        if (plugin.isBase()) {
            LOG.info("Plugin is base");
            return new PluginManagementOperationStatus(true, "Plugin jest bazowy");
            // return "redirect:page/plugins/errorEnableView.html?iframe=true";
            // return PLUGIN_VIEW + "&message=Plugin jest bazowy";
        } else if (plugin.getStatus().equals(PluginStatus.ACTIVE.getValue())) {
            LOG.info("Plugin hasn't apropriate status");
            // return PLUGIN_VIEW + "&message=Niepoprawny status do włączania";
            return new PluginManagementOperationStatus(true, "Niepoprawny status do włączania");
        }
        String pluginStatus = plugin.getStatus();
        plugin.setStatus(PluginStatus.ACTIVE.getValue());
        save(plugin);
        if (pluginStatus.equals(PluginStatus.INSTALLED.getValue())) {
            // return PLUGIN_VIEW;
            return new PluginManagementOperationStatus(false, "OK");
        } else {
            try {
                PluginUtil.movePluginFile(webappPath + TMP_PATH + plugin.getFileName(), webappPath + LIB_PATH);
            } catch (PluginException e) {
                plugin.setStatus(PluginStatus.DOWNLOADED.getValue());
                save(plugin);
                LOG.error("Problem with moving plugin file - " + e.getMessage());
                // return PLUGIN_VIEW + "&message=Blad przenoszenia pliku";
                return new PluginManagementOperationStatus(true, "Blad przenoszenia pliku");
            }

            PluginManagementOperationStatus status = new PluginManagementOperationStatus(false, "enable complete");
            status.setRestartRequired(true);
            return status;
            // return "redirect:enablePage.html";
        }
    }

    @Override
    @Monitorable
    public String restartServer() {
        try {
            PluginUtil.restartServer(webappPath);
        } catch (PluginException e) {
            LOG.error("Problem with restart server - " + e.getMessage());
            return PLUGIN_VIEW + "&message=Blad restartu serwera";
        }
        return "ok";
    }

    @Override
    @Transactional
    @Monitorable
    public PluginManagementOperationStatus disablePlugin(final String entityId) {
        PluginsPlugin plugin = getByEntityId(entityId);
        if (plugin.isBase()) {
            LOG.info("Plugin is base");
            // return PLUGIN_VIEW + "&message=Plugin jest bazowy";
            return new PluginManagementOperationStatus(true, "Plugin jest bazowy");
        } else if (!plugin.getStatus().equals(PluginStatus.ACTIVE.getValue())) {
            LOG.info("Plugin hasn't apropriate status");
            // return PLUGIN_VIEW + "&message=Niepoprawny status do wyłączania";
            return new PluginManagementOperationStatus(true, "Niepoprawny status do wyłączania");
        }
        plugin.setStatus(PluginStatus.INSTALLED.getValue());
        save(plugin);
        return new PluginManagementOperationStatus(false, "jest ok");
        // return PLUGIN_VIEW;
    }

    @Override
    @Transactional
    @Monitorable
    public PluginManagementOperationStatus deinstallPlugin(final String entityId) {
        PluginsPlugin databasePlugin = getByEntityId(entityId);
        if (databasePlugin.isBase()) {
            LOG.info("Plugin is base");
            return new PluginManagementOperationStatus(true, "Plugin jest bazowy");
            // return PLUGIN_VIEW + "&message=Plugin jest bazowy";
        } else if (databasePlugin.getStatus().equals(PluginStatus.DOWNLOADED.getValue())) {
            LOG.info("Plugin hasn't apropriate status");
            return new PluginManagementOperationStatus(true, "Niepoprawny status do odinstalowania");
            // return PLUGIN_VIEW + "&message=Niepoprawny status do odinstalowania";
        }
        try {
            databasePlugin.setDeleted(true);
            save(databasePlugin);
            PluginUtil.removePluginFile(webappPath + LIB_PATH + databasePlugin.getFileName());
        } catch (PluginException e) {
            LOG.error("Problem with removing plugin file - " + e.getMessage());
            return new PluginManagementOperationStatus(true, "Blad usuwania pliku");
            // return PLUGIN_VIEW + "&message=Blad usuwania pliku";
        }
        PluginUtil.removeResources("js", webappPath + "/" + "js" + "/" + databasePlugin.getIdentifier());
        PluginUtil.removeResources("css", webappPath + "/" + "css" + "/" + databasePlugin.getIdentifier());
        PluginUtil.removeResources("img", webappPath + "/" + "img" + "/" + databasePlugin.getIdentifier());
        PluginUtil.removeResources("jsp", webappPath + "/" + "WEB-INF/jsp" + "/" + databasePlugin.getIdentifier());

        PluginManagementOperationStatus status = new PluginManagementOperationStatus(false, "deinstal complete");
        status.setRestartRequired(true);
        return status;
        // return "redirect:deinstallPage.html";
    }

    @Override
    @Transactional
    @Monitorable
    public PluginManagementOperationStatus updatePlugin(final MultipartFile file) {
        if (!file.isEmpty()) {
            File pluginFile = null;
            boolean deleteFile = false;
            try {
                pluginFile = PluginUtil.transferFileToTmp(file, webappPath + TMP_PATH);
                PluginsPlugin plugin = PluginUtil.readDescriptor(pluginFile);
                PluginsPlugin databasePlugin = getByNameAndVendor(plugin.getName(), plugin.getVendor());
                if (databasePlugin == null) {
                    deleteFile = true;
                    LOG.info("Plugin not found in database");
                    return new PluginManagementOperationStatus(true, "Brak pluginu");
                    // return PLUGIN_VIEW + "&message=Brak pluginu";
                } else if (databasePlugin.isBase()) {
                    deleteFile = true;
                    LOG.info("Plugin is base");
                    return new PluginManagementOperationStatus(true, "Plugin jest bazowy");
                    // return PLUGIN_VIEW + "&message=Plugin jest bazowy";
                } else if (databasePlugin.getStatus().equals(PluginStatus.DOWNLOADED.getValue())) {
                    deleteFile = true;
                    LOG.info("Plugin hasn't apropriate status");
                    return new PluginManagementOperationStatus(true, "Niepoprawny status do aktualizacji");
                    // return PLUGIN_VIEW + "&message=Niepoprawny status do aktualizacji";
                } else if (databasePlugin.getVersion().compareTo(plugin.getVersion()) >= 0) {
                    deleteFile = true;
                    LOG.info("Plugin has actual version");
                    return new PluginManagementOperationStatus(true, "Brak pluginu");
                    // return PLUGIN_VIEW + "&message=Brak pluginu";
                } else {
                    plugin.setFileName(file.getOriginalFilename());
                    movePlugin(plugin, databasePlugin);
                    PluginManagementOperationStatus status = new PluginManagementOperationStatus(false, "update complete");
                    status.setRestartRequired(true);
                    return status;
                    // return "redirect:enablePage.html";
                }
            } catch (IllegalStateException e) {
                deleteFile = true;
                LOG.error("Problem with installing file - " + e.getMessage());
                return new PluginManagementOperationStatus(true, "Blad instalacji pliku");
                // return PLUGIN_VIEW + "&message=Blad instalacji pliku";
            } catch (IOException e) {
                deleteFile = true;
                LOG.error("Problem with installing file - " + e.getMessage());
                return new PluginManagementOperationStatus(true, "Blad instalacji pliku");
                // return PLUGIN_VIEW + "&message=Blad instalacji pliku";
            } catch (ParserConfigurationException e) {
                deleteFile = true;
                LOG.error("Problem with parsing descriptor - " + e.getMessage());
                return new PluginManagementOperationStatus(true, "Blad odczytu deskryptora");
                // return PLUGIN_VIEW + "&message=Blad odczytu deskryptora";
            } catch (SAXException e) {
                deleteFile = true;
                LOG.error("Problem with parsing descriptor - " + e.getMessage());
                return new PluginManagementOperationStatus(true, "Blad odczytu deskryptora");
                // return PLUGIN_VIEW + "&message=Blad odczytu deskryptora";
            } catch (PluginException e) {
                deleteFile = true;
                LOG.error("Problem with moving/removing plugin file - " + e.getMessage());
                return new PluginManagementOperationStatus(true, "Blad usuwania/przenoszenia pliku");
                // return PLUGIN_VIEW + "&message=Blad usuwania/przenoszenia pliku";
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
            return new PluginManagementOperationStatus(true, "Plik jest pusty");
            // return PLUGIN_VIEW + "&message=Plik jest pusty";
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

        PluginUtil.removePluginFile(webappPath + LIB_PATH + databasePlugin.getFileName());

        PluginUtil.movePluginFile(webappPath + TMP_PATH + plugin.getFileName(), webappPath + LIB_PATH);

        PluginUtil.removeResources("js", webappPath + "/" + "js" + "/" + databasePlugin.getIdentifier());
        PluginUtil.removeResources("css", webappPath + "/" + "css" + "/" + databasePlugin.getIdentifier());
        PluginUtil.removeResources("img", webappPath + "/" + "img" + "/" + databasePlugin.getIdentifier());
        PluginUtil.removeResources("jsp", webappPath + "/" + "WEB-INF/jsp" + "/" + databasePlugin.getIdentifier());
    }

    private Session getCurrentSession() {
        return sessionFactory.getCurrentSession();
    }

}
