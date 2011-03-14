package com.qcadoo.plugin.internal.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.Set;

import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.classic.Session;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.qcadoo.plugin.api.PersistentPlugin;
import com.qcadoo.plugin.internal.DefaultPersistentPlugin;

public class PluginDaoTest {

    private DefaultPluginDao pluginDao;

    private final SessionFactory sessionFactory = mock(SessionFactory.class);

    private final PersistentPlugin plugin1 = mock(PersistentPlugin.class);

    private final PersistentPlugin plugin2 = mock(PersistentPlugin.class);

    private final Session session = mock(Session.class);

    @Before
    public void init() {
        given(sessionFactory.getCurrentSession()).willReturn(session);

        pluginDao = new DefaultPluginDao();
        pluginDao.setSessionFactory(sessionFactory);
    }

    @Test
    public void shouldSavePlugin() throws Exception {
        // given

        // when
        pluginDao.save(plugin1);

        // then
        verify(session).save(DefaultPersistentPlugin.class.getCanonicalName(), plugin1);
    }

    @Test
    public void shouldDeletePlugin() throws Exception {
        // given

        // when
        pluginDao.delete(plugin1);

        // then
        verify(session).delete(DefaultPersistentPlugin.class.getCanonicalName(), plugin1);
    }

    @Test
    public void shouldListPlugin() throws Exception {
        // given
        Criteria criteria = mock(Criteria.class);
        given(session.createCriteria(DefaultPersistentPlugin.class)).willReturn(criteria);
        given(criteria.list()).willReturn(Lists.newArrayList(plugin1, plugin2));

        // when
        Set<PersistentPlugin> plugins = pluginDao.list();

        // then
        assertEquals(2, plugins.size());
        assertTrue(plugins.contains(plugin1));
        assertTrue(plugins.contains(plugin2));
    }
}
