package com.qcadoo.mes.core.internal;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.classic.Session;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.beans.plugins.PluginsPlugin;
import com.qcadoo.mes.core.api.PluginManagementService;
import com.qcadoo.mes.core.enums.PluginStatus;

@Service
public final class PluginManagementServiceImpl implements PluginManagementService {

    @Autowired
    private SessionFactory sessionFactory;

    @Override
<<<<<<< HEAD
    public List<PluginsPlugin> getActivePlugins() {
        Criteria criteria = getCurrentSession().createCriteria(PluginsPlugin.class)
                .add(Restrictions.eq("status", PluginStatus.ACTIVE.getValue())).add(Restrictions.eq("deleted", false));
        LOG.debug("get plugins with status: " + PluginStatus.ACTIVE.getValue());
=======
    @SuppressWarnings("unchecked")
    public List<PluginsPlugin> getPluginsWithStatus(final String status) {
        Criteria criteria = getCurrentSession().createCriteria(PluginsPlugin.class).add(Restrictions.eq("status", status))
                .add(Restrictions.eq("deleted", false));

>>>>>>> 64573e239e1924a643ee6b0cfbb5b26b4d7aaab5
        return criteria.list();
    }

    @Override
    public PluginsPlugin getPluginByIdentifierAndStatus(final String identifier, final String status) {
        checkNotNull(identifier, "identifier must be given");
<<<<<<< HEAD
        checkNotNull(status, "status must be given");
        Criteria criteria = getCurrentSession().createCriteria(PluginsPlugin.class)
                .add(Restrictions.eq("identifier", identifier)).add(Restrictions.eq("status", status))
                .add(Restrictions.eq("deleted", false));
        LOG.debug("get plugin with identifier: " + identifier + " and status: " + status);
        return (PluginsPlugin) criteria.uniqueResult();
=======
        Criteria criteria = getCurrentSession().createCriteria(PluginsPlugin.class)
                .add(Restrictions.eq("identifier", identifier)).add(Restrictions.eq("deleted", false));
        if (status != null) {
            criteria.add(Restrictions.eq("status", status));
        }

        PluginsPlugin databaseEntity = (PluginsPlugin) criteria.uniqueResult();

        if (databaseEntity == null) {
            return null;
        }

        return databaseEntity;
>>>>>>> 64573e239e1924a643ee6b0cfbb5b26b4d7aaab5
    }

    @Override
    public PluginsPlugin getPluginById(final String entityId) {
        checkNotNull(entityId, "entityId must be given");
        Criteria criteria = getCurrentSession().createCriteria(PluginsPlugin.class)
                .add(Restrictions.idEq(Long.valueOf(entityId))).add(Restrictions.eq("deleted", false));
<<<<<<< HEAD
        LOG.debug("get plugin with id: " + entityId);
=======

>>>>>>> 64573e239e1924a643ee6b0cfbb5b26b4d7aaab5
        return (PluginsPlugin) criteria.uniqueResult();
    }

    @Override
    public PluginsPlugin getPluginByNameAndVendor(final String name, final String vendor) {
        checkNotNull(vendor, "vendor must be given");
        checkNotNull(name, "name must be given");
        Criteria criteria = getCurrentSession().createCriteria(PluginsPlugin.class).add(Restrictions.eq("name", name))
                .add(Restrictions.eq("vendor", vendor)).add(Restrictions.eq("deleted", false));
        LOG.debug("get plugin with name: " + name + " and vendor: " + vendor);
        return (PluginsPlugin) criteria.uniqueResult();
    }

    @Override
<<<<<<< HEAD
    public void savePlugin(final PluginsPlugin plugin) {
        getCurrentSession().save(plugin);
=======
    public PluginsPlugin getInstalledPlugin(final PluginsPlugin plugin) {
        Criteria criteria = getCurrentSession().createCriteria(PluginsPlugin.class)
                .add(Restrictions.eq("name", plugin.getName())).add(Restrictions.eq("vendor", plugin.getVendor()))
                .add(Restrictions.eq("deleted", false));
        return (PluginsPlugin) criteria.uniqueResult();
>>>>>>> 64573e239e1924a643ee6b0cfbb5b26b4d7aaab5
    }

    private Session getCurrentSession() {
        return sessionFactory.getCurrentSession();
    }

}
