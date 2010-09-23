package com.qcadoo.mes.core.data.internal;

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

import com.qcadoo.mes.beans.plugins.PluginsPlugin;
import com.qcadoo.mes.core.data.api.PluginManagementService;

@Service
public final class PluginManagementServiceImpl implements PluginManagementService {

    @Autowired
    private SessionFactory sessionFactory;

    private static final Logger LOG = LoggerFactory.getLogger(PluginManagementServiceImpl.class);

    @Override
    public List<PluginsPlugin> getPluginsWithStatus(final String status) {
        Criteria criteria = getCurrentSession().createCriteria(PluginsPlugin.class).add(Restrictions.eq("status", status))
                .add(Restrictions.eq("deleted", false));

        return criteria.list();
    }

    @Override
    public PluginsPlugin getPluginWithStatus(final String identifier, final String status) {
        checkNotNull(identifier, "identifier must be given");
        Criteria criteria = getCurrentSession().createCriteria(PluginsPlugin.class).add(Restrictions.eq("identifier", identifier))
                .add(Restrictions.eq("deleted", false));
        if (status != null) {
            criteria.add(Restrictions.eq("status", status));
        }

        PluginsPlugin databaseEntity = (PluginsPlugin) criteria.uniqueResult();

        if (databaseEntity == null) {
            return null;
        }

        return databaseEntity;
    }

    @Override
    public PluginsPlugin getPlugin(final String entityId) {
        checkNotNull(entityId, "entityId must be given");
        Criteria criteria = getCurrentSession().createCriteria(PluginsPlugin.class).add(Restrictions.idEq(Long.valueOf(entityId)))
                .add(Restrictions.eq("deleted", false));

        return (PluginsPlugin) criteria.uniqueResult();
    }

    @Override
    public void savePlugin(final PluginsPlugin plugin) {
        getCurrentSession().save(plugin);
    }

    @Override
    public PluginsPlugin getInstalledPlugin(final PluginsPlugin plugin) {
        Criteria criteria = getCurrentSession().createCriteria(PluginsPlugin.class).add(Restrictions.eq("name", plugin.getName()))
                .add(Restrictions.eq("vendor", plugin.getVendor())).add(Restrictions.eq("deleted", false));
        return (PluginsPlugin) criteria.uniqueResult();
    }

    private Session getCurrentSession() {
        return sessionFactory.getCurrentSession();
    }

}
