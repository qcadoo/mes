package com.qcadoo.plugin;

import static org.mockito.Mockito.mock;

import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Test;

public class PluginDaoTest {

    private DefaultPluginDao pluginDao;

    private SessionFactory sessionFactory = mock(SessionFactory.class);

    @Before
    public void init() {
        pluginDao = new DefaultPluginDao();
        pluginDao.setSessionFactory(sessionFactory);
    }

    @Test
    public void shouldSavePlugin() throws Exception {
        // given
        PersistentPlugin plugin1 = mock(Plugin.class);

        // when
        pluginDao.save(plugin1);

        // then

    }

}
