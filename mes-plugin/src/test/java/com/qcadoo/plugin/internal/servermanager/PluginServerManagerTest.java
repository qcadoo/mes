package com.qcadoo.plugin.internal.servermanager;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import com.qcadoo.plugin.internal.PluginException;
import com.qcadoo.plugin.internal.servermanager.DefaultPluginServerManager;

public class PluginServerManagerTest {

    private DefaultPluginServerManager defaultPluginServerManager;

    @Before
    public void init() throws IOException {
        defaultPluginServerManager = new DefaultPluginServerManager();

    }

    @Test
    public void shouldRestartServer() throws Exception {
        // given
        defaultPluginServerManager.setRestartCommand("cd");

        // when
        defaultPluginServerManager.restart();

        // then
    }

    @Test(expected = PluginException.class)
    public void shouldThrowExceptionOnRestartServerWhenIOExceptionThrown() throws Exception {
        // given
        defaultPluginServerManager.setRestartCommand("restart");

        // when
        defaultPluginServerManager.restart();

        // then

    }

}
