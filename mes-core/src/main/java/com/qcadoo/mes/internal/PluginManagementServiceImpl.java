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
                    return new PluginManagementOperationStatus(true, "plugins.messages.error.pluginExists");
                }
                PluginsPlugin databasePlugin = getByNameAndVendor(plugin.getName(), plugin.getVendor());
                if (databasePlugin != null) {
                    deleteFile = true;
                    LOG.info("Plugin was installed");
                    return new PluginManagementOperationStatus(true, "plugins.messages.error.pluginAlreadyInstalled");
                } else {
                    plugin.setDeleted(false);
                    plugin.setStatus(PluginStatus.DOWNLOADED.getValue());
                    plugin.setBase(false);
                    plugin.setFileName(file.getOriginalFilename());
                    save(plugin);
                    return new PluginManagementOperationStatus(false, "plugins.messages.success.downloadSuccess");
                }
            } catch (IllegalStateException e) {
                deleteFile = true;
                LOG.error("Problem with installing file - " + e.getMessage());
                return new PluginManagementOperationStatus(true, "plugins.messages.error.fileError");
            } catch (IOException e) {
                deleteFile = true;
                LOG.error("Problem with installing file - " + e.getMessage());
                return new PluginManagementOperationStatus(true, "plugins.messages.error.fileError");
            } catch (ParserConfigurationException e) {
                deleteFile = true;
                LOG.error("Problem with parsing descriptor - " + e.getMessage());
                return new PluginManagementOperationStatus(true, "plugins.messages.error.descriptorError");
            } catch (SAXException e) {
                deleteFile = true;
                LOG.error("Problem with parsing descriptor - " + e.getMessage());
                return new PluginManagementOperationStatus(true, "plugins.messages.error.descriptorError");
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
            return new PluginManagementOperationStatus(true, "plugins.messages.error.emptyFile");
        }
    }

    @Override
    @Transactional
    @Monitorable
    public PluginManagementOperationStatus removePlugin(final String entityId) {
        PluginsPlugin databasePlugin = getByEntityId(entityId);
        if (databasePlugin.isBase()) {
            LOG.info("Plugin is base");
            return new PluginManagementOperationStatus(true, "plugins.messages.error.isBase");
        } else if (!databasePlugin.getStatus().equals(PluginStatus.DOWNLOADED.getValue())) {
            LOG.info("Plugin hasn't apropriate status");
            return new PluginManagementOperationStatus(true, "plugins.messages.error.wrongStatusToRemove");
        }
        try {
            databasePlugin.setDeleted(true);

            save(databasePlugin);

            PluginUtil.removePluginFile(webappPath + TMP_PATH + databasePlugin.getFileName());
        } catch (PluginException e) {
            LOG.error("Problem with removing plugin file - " + e.getMessage());
            return new PluginManagementOperationStatus(true, "plugins.messages.error.fileRemoveError");
        }

        return new PluginManagementOperationStatus(false, "plugins.messages.success.removeSuccess");
    }

    @Override
    @Transactional
    @Monitorable
    public PluginManagementOperationStatus enablePlugin(final String entityId) {
        PluginsPlugin plugin = getByEntityId(entityId);
        if (plugin.isBase()) {
            LOG.info("Plugin is base");
            return new PluginManagementOperationStatus(true, "plugins.messages.error.isBase");
        } else if (plugin.getStatus().equals(PluginStatus.ACTIVE.getValue())) {
            LOG.info("Plugin hasn't apropriate status");
            return new PluginManagementOperationStatus(true, "plugins.messages.error.wrongStatusToEnable");
        }
        String pluginStatus = plugin.getStatus();
        plugin.setStatus(PluginStatus.ACTIVE.getValue());
        save(plugin);
        if (pluginStatus.equals(PluginStatus.INSTALLED.getValue())) {
            return new PluginManagementOperationStatus(false, "plugins.messages.success.enableSuccess");
        } else {
            try {
                PluginUtil.movePluginFile(webappPath + TMP_PATH + plugin.getFileName(), webappPath + LIB_PATH);
            } catch (PluginException e) {
                plugin.setStatus(PluginStatus.DOWNLOADED.getValue());
                save(plugin);
                LOG.error("Problem with moving plugin file - " + e.getMessage());
                return new PluginManagementOperationStatus(true, "plugins.messages.error.fileMoveError");
            }

            PluginManagementOperationStatus status = new PluginManagementOperationStatus(false,
                    "plugins.messages.success.enableSuccess");
            status.setRestartRequired(true);
            return status;
        }
    }

    @Override
    @Monitorable
    public String restartServer() {
        try {
            PluginUtil.restartServer(webappPath);
        } catch (PluginException e) {
            LOG.error("Problem with restart server - " + e.getMessage());
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
            return new PluginManagementOperationStatus(true, "plugins.messages.error.isBase");
        } else if (!plugin.getStatus().equals(PluginStatus.ACTIVE.getValue())) {
            LOG.info("Plugin hasn't apropriate status");
            return new PluginManagementOperationStatus(true, "plugins.messages.error.wrongStatusToDisable");
        }
        plugin.setStatus(PluginStatus.INSTALLED.getValue());
        save(plugin);
        return new PluginManagementOperationStatus(false, "plugins.messages.success.disableSuccess");
    }

    @Override
    @Transactional
    @Monitorable
    public PluginManagementOperationStatus deinstallPlugin(final String entityId) {
        PluginsPlugin databasePlugin = getByEntityId(entityId);
        if (databasePlugin.isBase()) {
            LOG.info("Plugin is base");
            return new PluginManagementOperationStatus(true, "plugins.messages.error.isBase");
        } else if (databasePlugin.getStatus().equals(PluginStatus.DOWNLOADED.getValue())) {
            LOG.info("Plugin hasn't apropriate status");
            return new PluginManagementOperationStatus(true, "plugins.messages.error.wrongStatusToUninstall");
        }
        try {
            databasePlugin.setDeleted(true);
            save(databasePlugin);
            PluginUtil.removePluginFile(webappPath + LIB_PATH + databasePlugin.getFileName());
        } catch (PluginException e) {
            LOG.error("Problem with removing plugin file - " + e.getMessage());
            return new PluginManagementOperationStatus(true, "plugins.messages.error.fileRemoveError");
        }
        PluginUtil.removeResources("js", webappPath + "/" + "js" + "/" + databasePlugin.getIdentifier());
        PluginUtil.removeResources("css", webappPath + "/" + "css" + "/" + databasePlugin.getIdentifier());
        PluginUtil.removeResources("img", webappPath + "/" + "img" + "/" + databasePlugin.getIdentifier());
        PluginUtil.removeResources("jsp", webappPath + "/" + "WEB-INF/jsp" + "/" + databasePlugin.getIdentifier());

        PluginManagementOperationStatus status = new PluginManagementOperationStatus(false,
                "plugins.messages.success.deinstallSuccess");
        status.setRestartRequired(true);
        return status;
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
                    return new PluginManagementOperationStatus(true, "plugins.messages.error.noPlugin");
                } else if (databasePlugin.isBase()) {
                    deleteFile = true;
                    LOG.info("Plugin is base");
                    return new PluginManagementOperationStatus(true, "plugins.messages.error.isBase");
                } else if (databasePlugin.getStatus().equals(PluginStatus.DOWNLOADED.getValue())) {
                    deleteFile = true;
                    LOG.info("Plugin hasn't apropriate status");
                    return new PluginManagementOperationStatus(true, "plugins.messages.error.wrongStatusToUpdate");
                } else if (databasePlugin.getVersion().compareTo(plugin.getVersion()) >= 0) {
                    deleteFile = true;
                    LOG.info("Plugin has actual version");
                    return new PluginManagementOperationStatus(true, "plugins.messages.error.noPlugin");
                } else {
                    plugin.setFileName(file.getOriginalFilename());
                    movePlugin(plugin, databasePlugin);
                    PluginManagementOperationStatus status = new PluginManagementOperationStatus(false,
                            "plugins.messages.success.updateSuccess");
                    status.setRestartRequired(true);
                    return status;
                }
            } catch (IllegalStateException e) {
                deleteFile = true;
                LOG.error("Problem with installing file - " + e.getMessage());
                return new PluginManagementOperationStatus(true, "plugins.messages.error.fileError");
            } catch (IOException e) {
                deleteFile = true;
                LOG.error("Problem with installing file - " + e.getMessage());
                return new PluginManagementOperationStatus(true, "plugins.messages.error.fileError");
            } catch (ParserConfigurationException e) {
                deleteFile = true;
                LOG.error("Problem with parsing descriptor - " + e.getMessage());
                return new PluginManagementOperationStatus(true, "plugins.messages.error.descriptorError");
            } catch (SAXException e) {
                deleteFile = true;
                LOG.error("Problem with parsing descriptor - " + e.getMessage());
                return new PluginManagementOperationStatus(true, "plugins.messages.error.descriptorError");
            } catch (PluginException e) {
                deleteFile = true;
                LOG.error("Problem with moving/removing plugin file - " + e.getMessage());
                return new PluginManagementOperationStatus(true, "plugins.messages.error.fileRemoveMoveError");
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
            return new PluginManagementOperationStatus(true, "plugins.messages.error.emptyFile");
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
