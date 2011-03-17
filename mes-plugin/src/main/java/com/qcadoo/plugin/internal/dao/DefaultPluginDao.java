package com.qcadoo.plugin.internal.dao;

import java.util.Set;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Sets;
import com.qcadoo.model.beans.plugins.PluginsPlugin;
import com.qcadoo.plugin.api.Plugin;
import com.qcadoo.plugin.internal.api.PluginDao;

@Service
public class DefaultPluginDao implements PluginDao {

    @Autowired
    @Qualifier("plugin")
    private SessionFactory sessionFactory;

    @Override
    @Transactional
    public void save(final PluginsPlugin plugin) {
        sessionFactory.getCurrentSession().save(plugin);
    }

    @Override
    @Transactional
    public void save(final Plugin plugin) {
        save(new PluginsPlugin(plugin));
    }

    @Override
    @Transactional
    public void delete(final PluginsPlugin plugin) {
        sessionFactory.getCurrentSession().delete(plugin);
    }

    @Override
    @Transactional
    public void delete(final Plugin plugin) {
        delete(new PluginsPlugin(plugin));
    }

    @Override
    @SuppressWarnings("unchecked")
    @Transactional(readOnly = true)
    public Set<PluginsPlugin> list() {
        return Sets.newHashSet(sessionFactory.getCurrentSession().createCriteria(PluginsPlugin.class).list());
    }

    void setSessionFactory(final SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

}
