package com.qcadoo.plugin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Test;

public class PluginTest {

    @Test
    public void shouldCallInitOnModules() throws Exception {
        // given
        Module module1 = mock(Module.class);
        Module module2 = mock(Module.class);

        DefaultPlugin plugin = new DefaultPlugin();
        plugin.addModule(module1);
        plugin.addModule(module2);

        // when
        plugin.init();

        // then
        verify(module1).init();
        verify(module2).init();
    }

    @Test
    public void shouldHaveUnknownStateByDefault() throws Exception {
        // given
        DefaultPlugin plugin = new DefaultPlugin();

        // then
        assertTrue(plugin.hasState(PluginState.UNKNOWN));
        assertEquals(PluginState.UNKNOWN, plugin.getPluginState());
    }

}
