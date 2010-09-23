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

import com.qcadoo.mes.core.data.api.PluginManagementService;
import com.qcadoo.mes.core.data.beans.Plugin;

@Service
public final class PluginManagementServiceImpl implements PluginManagementService {

    @Autowired
    private SessionFactory sessionFactory;

    private static final Logger LOG = LoggerFactory.getLogger(PluginManagementServiceImpl.class);

    @Override
    public List<Plugin> getActivePlugins() {
        Criteria criteria = getCurrentSession().createCriteria(Plugin.class).add(Restrictions.eq("active", true))
                .add(Restrictions.eq("deleted", false));

        return criteria.list();
    }

    @Override
    public Plugin getActivePlugin(final String identifier) {
        checkNotNull(identifier, "identifier must be given");
        Criteria criteria = getCurrentSession().createCriteria(Plugin.class).add(Restrictions.eq("identifier", identifier))
                .add(Restrictions.eq("active", true)).add(Restrictions.eq("deleted", false));

        Plugin databaseEntity = (Plugin) criteria.uniqueResult();

        if (databaseEntity == null) {
            return null;
        }

        return databaseEntity;
    }

    @Override
    public Plugin getPlugin(final String entityId) {
        checkNotNull(entityId, "entityId must be given");
        Criteria criteria = getCurrentSession().createCriteria(Plugin.class).add(Restrictions.idEq(Long.valueOf(entityId)))
                .add(Restrictions.eq("deleted", false));

        return (Plugin) criteria.uniqueResult();
    }

    @Override
    public void savePlugin(final Plugin plugin) {
        getCurrentSession().save(plugin);
    }

    @Override
    public Plugin getInstalledPlugin(final Plugin plugin) {
        Criteria criteria = getCurrentSession().createCriteria(Plugin.class).add(Restrictions.eq("name", plugin.getName()))
                .add(Restrictions.eq("publisher", plugin.getVendor())).add(Restrictions.eq("deleted", false));
        return (Plugin) criteria.uniqueResult();
    }

    private Session getCurrentSession() {
        return sessionFactory.getCurrentSession();
    }

}
