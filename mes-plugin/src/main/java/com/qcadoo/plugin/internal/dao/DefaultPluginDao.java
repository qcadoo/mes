package com.qcadoo.plugin.internal.dao;

import java.util.Collections;
import java.util.Set;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.plugin.api.PersistentPlugin;
import com.qcadoo.plugin.internal.api.PluginDao;

@Service
public class DefaultPluginDao implements PluginDao {

    @Autowired
    private SessionFactory sessionFactory;

    @Override
    public void save(final PersistentPlugin plugin) {
        // TODO Auto-generated method stub

    }

    @Override
    public void delete(final PersistentPlugin plugin) {
        // sessionFactory.getCurrentSession().delete(plugin);
    }

    @Override
    public Set<PersistentPlugin> list() {
        /*
         * @SuppressWarnings("unchecked") List<PluginsPlugin> plugins =
         * sessionFactory.getCurrentSession().createCriteria(PluginsPlugin.class).list(); Set<PersistentPlugin> pluginsSet = new
         * HashSet<PersistentPlugin>(); for (PluginsPlugin plugin : plugins) { pluginsSet.add(new
         * DefaultPersistentPlugin(plugin.getIdentifier(), PluginState.valueOf(plugin.getState()),
         * VersionUtils.parse(plugin.getVersion()))); }
         */
        return Collections.emptySet();
    }

    void setSessionFactory(final SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

}
