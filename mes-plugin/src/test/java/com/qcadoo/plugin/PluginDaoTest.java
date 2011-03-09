package com.qcadoo.plugin;

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

        // when
        pluginDao.save(plugin);

        // then

    }

}
