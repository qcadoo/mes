package com.qcadoo.mes.core.internal;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.classic.Session;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.qcadoo.mes.beans.plugins.PluginsPlugin;
import com.qcadoo.mes.core.api.PluginManagementService;
import com.qcadoo.mes.core.enums.PluginStatus;

@Service
public final class PluginManagementServiceImpl implements PluginManagementService {

    private static final Logger LOG = LoggerFactory.getLogger(PluginManagementServiceImpl.class);

    @Autowired
    private SessionFactory sessionFactory;

    @Override
    @SuppressWarnings("unchecked")
    @Transactional(readOnly = true)
    public List<PluginsPlugin> getActivePlugins() {
        Criteria criteria = getCurrentSession().createCriteria(PluginsPlugin.class)
                .add(Restrictions.eq("status", PluginStatus.ACTIVE.getValue())).add(Restrictions.eq("deleted", false));
        LOG.debug("get plugins with status: " + PluginStatus.ACTIVE.getValue());
        return criteria.list();
    }

    @Override
    @Transactional(readOnly = true)
    public PluginsPlugin getPluginByIdentifierAndStatus(final String identifier, final String status) {
        checkNotNull(identifier, "identifier must be given");
        checkNotNull(status, "status must be given");
        Criteria criteria = getCurrentSession().createCriteria(PluginsPlugin.class)
                .add(Restrictions.eq("identifier", identifier)).add(Restrictions.eq("status", status))
                .add(Restrictions.eq("deleted", false));
        LOG.debug("get plugin with identifier: " + identifier + " and status: " + status);
        return (PluginsPlugin) criteria.uniqueResult();
    }

    @Override
    @Transactional(readOnly = true)
    public PluginsPlugin getPluginByIdentifier(final String identifier) {
        checkNotNull(identifier, "identifier must be given");
        Criteria criteria = getCurrentSession().createCriteria(PluginsPlugin.class)
                .add(Restrictions.eq("identifier", identifier)).add(Restrictions.eq("deleted", false));
        LOG.debug("get plugin with identifier: " + identifier);
        return (PluginsPlugin) criteria.uniqueResult();
    }

    @Override
    @Transactional(readOnly = true)
    public PluginsPlugin getPluginById(final String entityId) {
        checkNotNull(entityId, "entityId must be given");
        Criteria criteria = getCurrentSession().createCriteria(PluginsPlugin.class)
                .add(Restrictions.idEq(Long.valueOf(entityId))).add(Restrictions.eq("deleted", false));

        LOG.debug("get plugin with id: " + entityId);

        return (PluginsPlugin) criteria.uniqueResult();
    }

    @Override
    @Transactional(readOnly = true)
    public PluginsPlugin getPluginByNameAndVendor(final String name, final String vendor) {
        checkNotNull(vendor, "vendor must be given");
        checkNotNull(name, "name must be given");
        Criteria criteria = getCurrentSession().createCriteria(PluginsPlugin.class).add(Restrictions.eq("name", name))
                .add(Restrictions.eq("vendor", vendor)).add(Restrictions.eq("deleted", false));
        LOG.debug("get plugin with name: " + name + " and vendor: " + vendor);
        return (PluginsPlugin) criteria.uniqueResult();
    }

    @Override
    @Transactional
    public void savePlugin(final PluginsPlugin plugin) {
        checkNotNull(plugin, "plugin must be given");
        getCurrentSession().save(plugin);
    }

    private Session getCurrentSession() {
        return sessionFactory.getCurrentSession();
    }

}
