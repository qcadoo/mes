package com.qcadoo.plugin;

import org.junit.Before;
import org.junit.Test;

public class PluginServerTest {

    private DefaultPluginServerManager defaultPluginServerManager;

    @Before
    public void init() {
        defaultPluginServerManager = new DefaultPluginServerManager();
        defaultPluginServerManager.setRestartCommand("restart");
    }

    @Test
    public void shouldRestartServer() throws Exception {
        // given

        // when
        defaultPluginServerManager.restart();

        // then

    }

}
