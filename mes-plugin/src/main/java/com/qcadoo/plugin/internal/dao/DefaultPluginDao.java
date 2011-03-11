package com.qcadoo.plugin.internal.dao;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.SessionFactory;

import com.qcadoo.plugin.api.PersistentPlugin;
import com.qcadoo.plugin.internal.api.PluginDao;

public class DefaultPluginDao implements PluginDao {

    private SessionFactory sessionFactory;

    @Override
    public void save(final PersistentPlugin plugin) {
        // TODO Auto-generated method stub

    }

    @Override
    public void delete(final PersistentPlugin plugin) {
        //sessionFactory.getCurrentSession().delete(plugin);
    }

    @Override
    public Set<PersistentPlugin> list() {
     /*   @SuppressWarnings("unchecked")
        List<PluginsPlugin> plugins = sessionFactory.getCurrentSession().createCriteria(PluginsPlugin.class).list();
        Set<PersistentPlugin> pluginsSet = new HashSet<PersistentPlugin>();
        for (PluginsPlugin plugin : plugins) {
            pluginsSet.add(new DefaultPersistentPlugin(plugin.getIdentifier(), PluginState.valueOf(plugin.getState()),
                    VersionUtils.parse(plugin.getVersion())));
        }*/
        return null;
    }

    void setSessionFactory(final SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

}
