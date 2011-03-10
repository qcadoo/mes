package com.qcadoo.plugin;

import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.junit.Test;

public class PluginDaoTest {

    private PluginDao pluginDao;

    @Before
    public void init() {
        pluginDao = new DefaultPluginDao();
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
