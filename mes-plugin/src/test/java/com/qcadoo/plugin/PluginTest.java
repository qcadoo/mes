package com.qcadoo.plugin;

import static junit.framework.Assert.assertEquals;
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

        Plugin plugin = DefaultPlugin.Builder.identifier("identifier").withModule(module1).withModule(module2).build();

        plugin.changeStateTo(PluginState.ENABLED);

        // when
        plugin.init();

        // then
        verify(module1).init();
        verify(module2).init();
    }

    @Test(expected = IllegalStateException.class)
    public void shouldFailInitializingPluginWithUnknownState() throws Exception {
        // given
        Plugin plugin = DefaultPlugin.Builder.identifier("identifier").build();

        // when
        plugin.init();
    }

    @Test
    public void shouldHaveUnknownStateByDefault() throws Exception {
        // given
        Plugin plugin = DefaultPlugin.Builder.identifier("identifier").build();

        // then
        assertTrue(plugin.hasState(PluginState.UNKNOWN));
        assertEquals(PluginState.UNKNOWN, plugin.getPluginState());
    }

    @Test
    public void shouldCompareVersions() throws Exception {
        // given
        Plugin plugin1 = DefaultPlugin.Builder.identifier("identifier").withVersion("2.3.4").build();
        Plugin plugin2 = DefaultPlugin.Builder.identifier("identifier").withVersion("2.3.4").build();
        Plugin plugin3 = DefaultPlugin.Builder.identifier("identifier").withVersion("2.3.5").build();
        Plugin plugin4 = DefaultPlugin.Builder.identifier("identifier").withVersion("2.4.4").build();
        Plugin plugin5 = DefaultPlugin.Builder.identifier("identifier").withVersion("3.3.4").build();

        // then
        assertEquals(0, plugin1.compareVersion(plugin2));
        assertEquals(0, plugin2.compareVersion(plugin1));
        // assertEquals(1, plugin1.compareVersion(plugin3));
        // assertEquals(1, plugin1.compareVersion(plugin4));
        // assertEquals(1, plugin1.compareVersion(plugin5));
        // assertEquals(1, plugin3.compareVersion(plugin4));
        // assertEquals(1, plugin3.compareVersion(plugin5));
        // assertEquals(1, plugin4.compareVersion(plugin5));
        assertEquals(-1, plugin5.compareVersion(plugin1));
        assertEquals(-1, plugin5.compareVersion(plugin3));
        assertEquals(-1, plugin5.compareVersion(plugin4));
        assertEquals(-1, plugin4.compareVersion(plugin1));
        assertEquals(-1, plugin4.compareVersion(plugin3));
        assertEquals(-1, plugin3.compareVersion(plugin1));
    }

    @Test(expected = IllegalStateException.class)
    public void shouldFailComparingVersionsOfDifferentPlugins() throws Exception {
        // given
        Plugin plugin1 = DefaultPlugin.Builder.identifier("identifier1").withVersion("2.3.4").build();
        Plugin plugin2 = DefaultPlugin.Builder.identifier("identifier2").withVersion("2.3.4").build();

        // then
        plugin1.compareVersion(plugin2);
    }

}
