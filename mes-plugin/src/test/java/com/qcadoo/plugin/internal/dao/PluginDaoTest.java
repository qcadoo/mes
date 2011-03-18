package com.qcadoo.plugin.internal.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.Set;

import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.classic.Session;
import org.hibernate.criterion.Criterion;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.qcadoo.model.beans.plugins.PluginsPlugin;
import com.qcadoo.plugin.api.Plugin;
import com.qcadoo.plugin.api.PluginState;
import com.qcadoo.plugin.api.Version;

public class PluginDaoTest {

    private DefaultPluginDao pluginDao;

    private final SessionFactory sessionFactory = mock(SessionFactory.class);

    private final PluginsPlugin plugin1 = new PluginsPlugin();

    private final PluginsPlugin plugin2 = new PluginsPlugin();

    private final Plugin plugin11 = mock(Plugin.class);

    private final Session session = mock(Session.class);

    private final Criteria criteria = mock(Criteria.class);

    @Before
    public void init() {
        plugin1.setIdentifier("plugin1");
        plugin2.setIdentifier("plugin2");

        given(sessionFactory.getCurrentSession()).willReturn(session);

        given(plugin11.getIdentifier()).willReturn("identifier1");
        given(session.createCriteria(PluginsPlugin.class)).willReturn(criteria);
        given(criteria.add(any(Criterion.class))).willReturn(criteria);

        pluginDao = new DefaultPluginDao();
        pluginDao.setSessionFactory(sessionFactory);
    }

    @Test
    public void shouldSavePersistentPlugin() throws Exception {
        // given

        // when
        pluginDao.save(plugin1);

        // then
        verify(session).save(plugin1);
    }

    @Test
    public void shouldSaveNotPersistentExistingPlugin() throws Exception {
        // given
        given(criteria.uniqueResult()).willReturn(plugin1);
        given(plugin11.getState()).willReturn(PluginState.ENABLED);
        given(plugin11.getVersion()).willReturn(new Version("0.0.0"));

        // when
        pluginDao.save(plugin11);

        // then
        assertEquals(plugin1.getState(), PluginState.ENABLED.toString());
        assertEquals(plugin1.getVersion(), "0.0.0");
        verify(session).save(plugin1);
    }

    @Test
    public void shouldSaveNotPersistentPlugin() throws Exception {
        // given
        given(plugin11.getState()).willReturn(PluginState.ENABLED);
        given(plugin11.getVersion()).willReturn(new Version("0.0.0"));

        // when
        pluginDao.save(plugin11);

        // then
        verify(session, never()).save(plugin1);
        verify(session).save(any(PluginsPlugin.class));
    }

    @Test
    public void shouldDeletePersistentPlugin() throws Exception {
        // given

        // when
        pluginDao.delete(plugin1);

        // then
        verify(session).delete(plugin1);
    }

    @Test
    public void shouldDeleteNotPersistentPlugin() throws Exception {
        // given
        given(criteria.uniqueResult()).willReturn(plugin1);

        // when
        pluginDao.delete(plugin11);

        // then
        verify(session).delete(plugin1);
    }

    @Test
    public void shouldListPlugin() throws Exception {
        // given
        given(criteria.list()).willReturn(Lists.newArrayList(plugin1, plugin2));

        // when
        Set<PluginsPlugin> plugins = pluginDao.list();

        // then
        assertEquals(2, plugins.size());
        assertTrue(plugins.contains(plugin1));
        assertTrue(plugins.contains(plugin2));
    }
}
