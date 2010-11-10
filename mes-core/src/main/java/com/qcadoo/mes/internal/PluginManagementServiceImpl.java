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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.SAXException;

import com.qcadoo.mes.api.PluginManagementOperationStatus;
import com.qcadoo.mes.api.PluginManagementService;
import com.qcadoo.mes.beans.plugins.PluginsPlugin;
import com.qcadoo.mes.model.aop.internal.Monitorable;
import com.qcadoo.mes.plugins.internal.enums.PluginStatus;
import com.qcadoo.mes.plugins.internal.exceptions.PluginException;
import com.qcadoo.mes.plugins.internal.util.PluginUtil;

@Service
public final class PluginManagementServiceImpl implements PluginManagementService {

    private static final Logger LOG = LoggerFactory.getLogger(PluginManagementServiceImpl.class);

    private static final String FIELD_DELETED = "deleted";

    @Value("${QCADOO_PLUGINS_PATH}")
    private String pluginsPath;

    @Value("${QCADOO_PLUGINS_TMP_PATH}")
    private String pluginsTmpPath;

    @Value("${QCADOO_WEBAPP_PATH}")
    private String webappPath;

    @Autowired
    private SessionFactory sessionFactory;

    @Autowired
    private PluginUtil pluginUtil;

    @PostConstruct
    public void init() {
        LOG.info("Plugins path: " + pluginsPath);
        LOG.info("Plugins tmpPath: " + pluginsTmpPath);
        LOG.info("Webapp path: " + webappPath);
    }

    @Override
    @Transactional
    @Monitorable
    public PluginManagementOperationStatus downloadPlugin(final MultipartFile file) {
        if (!file.isEmpty()) {
            File pluginFile = null;
            boolean deleteFile = false;
            try {
                pluginFile = pluginUtil.transferFileToTmp(file, pluginsTmpPath);
                PluginsPlugin plugin = pluginUtil.readDescriptor(pluginFile);
                PluginsPlugin pluginWithIdentifier = getByIdentifier(plugin.getIdentifier());
                if (pluginWithIdentifier != null) {
                    deleteFile = true;
                    return new PluginManagementOperationStatusImpl(true, "plugins.messages.error.pluginExists");
                }
                PluginsPlugin databasePlugin = getByNameAndVendor(plugin.getName(), plugin.getVendor());
                if (databasePlugin != null) {
                    deleteFile = true;
                    LOG.info("Plugin was installed");
                    return new PluginManagementOperationStatusImpl(true, "plugins.messages.error.pluginAlreadyInstalled");
                } else {
                    plugin.setDeleted(false);
                    plugin.setStatus(PluginStatus.DOWNLOADED.getValue());
                    plugin.setBase(false);
                    plugin.setFileName(file.getOriginalFilename());
                    save(plugin);
                    return new PluginManagementOperationStatusImpl(false, "plugins.messages.success.downloadSuccess");
                }
            } catch (IllegalStateException e) {
                deleteFile = true;
                LOG.error("Problem with installing file - " + e.getMessage());
                return new PluginManagementOperationStatusImpl(true, "plugins.messages.error.fileError");
            } catch (IOException e) {
                deleteFile = true;
                LOG.error("Problem with installing file - " + e.getMessage());
                return new PluginManagementOperationStatusImpl(true, "plugins.messages.error.fileError");
            } catch (ParserConfigurationException e) {
                deleteFile = true;
                LOG.error("Problem with parsing descriptor - " + e.getMessage());
                return new PluginManagementOperationStatusImpl(true, "plugins.messages.error.descriptorError");
            } catch (SAXException e) {
                deleteFile = true;
                LOG.error("Problem with parsing descriptor - " + e.getMessage());
                return new PluginManagementOperationStatusImpl(true, "plugins.messages.error.descriptorError");
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
            return new PluginManagementOperationStatusImpl(true, "plugins.messages.error.emptyFile");
        }
    }

    @Override
    @Transactional
    @Monitorable
    public PluginManagementOperationStatus removePlugin(final Long id) {
        PluginsPlugin databasePlugin = get(id);
        if (databasePlugin.isBase()) {
            LOG.info("Plugin is base");
            return new PluginManagementOperationStatusImpl(true, "plugins.messages.error.isBase");
        } else if (!databasePlugin.getStatus().equals(PluginStatus.DOWNLOADED.getValue())) {
            LOG.info("Plugin hasn't apropriate status");
            return new PluginManagementOperationStatusImpl(true, "plugins.messages.error.wrongStatusToRemove");
        }
        try {
            databasePlugin.setDeleted(true);

            save(databasePlugin);

            pluginUtil.removePluginFile(pluginsTmpPath + "/" + databasePlugin.getFileName(), false);
        } catch (IOException e) {
            LOG.error("Problem with removing plugin file - " + e.getMessage());
            return new PluginManagementOperationStatusImpl(true, "plugins.messages.error.fileRemoveError");
        }

        return new PluginManagementOperationStatusImpl(false, "plugins.messages.success.removeSuccess");
    }

    @Override
    @Transactional
    @Monitorable
    public PluginManagementOperationStatus enablePlugin(final Long id) {
        PluginsPlugin plugin = get(id);
        if (plugin.isBase()) {
            LOG.info("Plugin is base");
            return new PluginManagementOperationStatusImpl(true, "plugins.messages.error.isBase");
        } else if (plugin.getStatus().equals(PluginStatus.ACTIVE.getValue())) {
            LOG.info("Plugin hasn't apropriate status");
            return new PluginManagementOperationStatusImpl(true, "plugins.messages.error.wrongStatusToEnable");
        }
        String pluginStatus = plugin.getStatus();
        plugin.setStatus(PluginStatus.ACTIVE.getValue());
        save(plugin);
        if (pluginStatus.equals(PluginStatus.INSTALLED.getValue())) {
            return new PluginManagementOperationStatusImpl(false, "plugins.messages.success.enableSuccess");
        } else {
            try {
                pluginUtil.movePluginFile(pluginsTmpPath + "/" + plugin.getFileName(), pluginsPath);
            } catch (IOException e) {
                plugin.setStatus(PluginStatus.DOWNLOADED.getValue());
                save(plugin);
                LOG.error("Problem with moving plugin file - " + e.getMessage());
                return new PluginManagementOperationStatusImpl(true, "plugins.messages.error.fileMoveError");
            }

            PluginManagementOperationStatusImpl status = new PluginManagementOperationStatusImpl(false,
                    "plugins.messages.success.enableSuccess");
            status.setRestartRequired(true);
            return status;
        }
    }

    @Override
    @Monitorable
    public void restartServer() {
        try {
            pluginUtil.restartServer();
        } catch (PluginException e) {
            LOG.error("Problem with restart server - " + e.getMessage());
        }
    }

    @Override
    @Transactional
    @Monitorable
    public PluginManagementOperationStatus disablePlugin(final Long id) {
        PluginsPlugin plugin = get(id);
        if (plugin.isBase()) {
            LOG.info("Plugin is base");
            return new PluginManagementOperationStatusImpl(true, "plugins.messages.error.isBase");
        } else if (!plugin.getStatus().equals(PluginStatus.ACTIVE.getValue())) {
            LOG.info("Plugin hasn't apropriate status");
            return new PluginManagementOperationStatusImpl(true, "plugins.messages.error.wrongStatusToDisable");
        }
        plugin.setStatus(PluginStatus.INSTALLED.getValue());
        save(plugin);
        return new PluginManagementOperationStatusImpl(false, "plugins.messages.success.disableSuccess");
    }

    @Override
    @Transactional
    @Monitorable
    public PluginManagementOperationStatus deinstallPlugin(final Long id) {
        PluginsPlugin databasePlugin = get(id);
        if (databasePlugin.isBase()) {
            LOG.info("Plugin is base");
            return new PluginManagementOperationStatusImpl(true, "plugins.messages.error.isBase");
        } else if (databasePlugin.getStatus().equals(PluginStatus.DOWNLOADED.getValue())) {
            LOG.info("Plugin hasn't apropriate status");
            return new PluginManagementOperationStatusImpl(true, "plugins.messages.error.wrongStatusToUninstall");
        }
        try {
            databasePlugin.setDeleted(true);
            save(databasePlugin);
            pluginUtil.removePluginFile(pluginsPath + "/" + databasePlugin.getFileName(), true);
            pluginUtil.removeResources("js", webappPath + "/" + "js" + "/" + databasePlugin.getIdentifier());
            pluginUtil.removeResources("css", webappPath + "/" + "css" + "/" + databasePlugin.getIdentifier());
            pluginUtil.removeResources("img", webappPath + "/" + "img" + "/" + databasePlugin.getIdentifier());
            pluginUtil.removeResources("jsp", webappPath + "/" + "WEB-INF/jsp" + "/" + databasePlugin.getIdentifier());
        } catch (IOException e) {
            LOG.error("Problem with removing plugin file - " + e.getMessage());
            return new PluginManagementOperationStatusImpl(true, "plugins.messages.error.fileRemoveError");
        }

        PluginManagementOperationStatusImpl status = new PluginManagementOperationStatusImpl(false,
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
                pluginFile = pluginUtil.transferFileToTmp(file, pluginsTmpPath);
                PluginsPlugin plugin = pluginUtil.readDescriptor(pluginFile);
                PluginsPlugin databasePlugin = getByNameAndVendor(plugin.getName(), plugin.getVendor());
                if (databasePlugin == null) {
                    deleteFile = true;
                    LOG.info("Plugin not found in database");
                    return new PluginManagementOperationStatusImpl(true, "plugins.messages.error.noPlugin");
                } else if (databasePlugin.isBase()) {
                    deleteFile = true;
                    LOG.info("Plugin is base");
                    return new PluginManagementOperationStatusImpl(true, "plugins.messages.error.isBase");
                } else if (databasePlugin.getStatus().equals(PluginStatus.DOWNLOADED.getValue())) {
                    deleteFile = true;
                    LOG.info("Plugin hasn't apropriate status");
                    return new PluginManagementOperationStatusImpl(true, "plugins.messages.error.wrongStatusToUpdate");
                } else if (databasePlugin.getVersion().compareTo(plugin.getVersion()) >= 0) {
                    deleteFile = true;
                    LOG.info("Plugin has actual version");
                    return new PluginManagementOperationStatusImpl(true, "plugins.messages.error.noPlugin");
                } else {
                    plugin.setFileName(file.getOriginalFilename());
                    movePlugin(plugin, databasePlugin);
                    PluginManagementOperationStatusImpl status = new PluginManagementOperationStatusImpl(false,
                            "plugins.messages.success.updateSuccess");
                    status.setRestartRequired(true);
                    return status;
                }
            } catch (IllegalStateException e) {
                deleteFile = true;
                LOG.error("Problem with installing file - " + e.getMessage());
                return new PluginManagementOperationStatusImpl(true, "plugins.messages.error.fileError");
            } catch (IOException e) {
                deleteFile = true;
                LOG.error("Problem with installing/moving file - " + e.getMessage());
                return new PluginManagementOperationStatusImpl(true, "plugins.messages.error.fileInstallMoveError");
            } catch (ParserConfigurationException e) {
                deleteFile = true;
                LOG.error("Problem with parsing descriptor - " + e.getMessage());
                return new PluginManagementOperationStatusImpl(true, "plugins.messages.error.descriptorError");
            } catch (SAXException e) {
                deleteFile = true;
                LOG.error("Problem with parsing descriptor - " + e.getMessage());
                return new PluginManagementOperationStatusImpl(true, "plugins.messages.error.descriptorError");
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
            return new PluginManagementOperationStatusImpl(true, "plugins.messages.error.emptyFile");
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
    public PluginsPlugin get(final Long id) {
        checkNotNull(id, "id must be given");
        Criteria criteria = getCurrentSession().createCriteria(PluginsPlugin.class).add(Restrictions.idEq(id))
                .add(Restrictions.eq(FIELD_DELETED, false));
        if (LOG.isDebugEnabled()) {
            LOG.debug("get plugin with id: " + id);
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

    private void movePlugin(final PluginsPlugin plugin, final PluginsPlugin databasePlugin) throws IOException {
        plugin.setDeleted(false);
        plugin.setStatus(databasePlugin.getStatus());
        plugin.setBase(false);
        databasePlugin.setDeleted(true);
        save(databasePlugin);

        save(plugin);

        pluginUtil.removePluginFile(pluginsPath + "/" + databasePlugin.getFileName(), true);

        pluginUtil.movePluginFile(pluginsTmpPath + "/" + plugin.getFileName(), pluginsPath);

        pluginUtil.removeResources("js", webappPath + "/" + "js" + "/" + databasePlugin.getIdentifier());
        pluginUtil.removeResources("css", webappPath + "/" + "css" + "/" + databasePlugin.getIdentifier());
        pluginUtil.removeResources("img", webappPath + "/" + "img" + "/" + databasePlugin.getIdentifier());
        pluginUtil.removeResources("jsp", webappPath + "/" + "WEB-INF/jsp" + "/" + databasePlugin.getIdentifier());
    }

    private Session getCurrentSession() {
        return sessionFactory.getCurrentSession();
    }

}
