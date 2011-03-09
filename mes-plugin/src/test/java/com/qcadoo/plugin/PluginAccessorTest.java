package com.qcadoo.plugin;

import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.hasItem;
import static org.junit.matchers.JUnitMatchers.hasItems;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.Set;

import org.junit.Test;
import org.mockito.Mockito;

import com.google.common.collect.Sets;

public class PluginAccessorTest {

    @Test
    public void shouldSynchronizePluginsFromClasspathAndDatabase() throws Exception {
        // given
        Plugin plugin1 = mock(Plugin.class);
        given(plugin1.compareVersion(plugin1)).willReturn(0);

        Plugin plugin21 = mock(Plugin.class);
        given(plugin21.getPluginState()).willReturn(PluginState.ENABLED);
        Plugin plugin22 = mock(Plugin.class);
        given(plugin22.compareVersion(plugin21)).willReturn(1);

        Plugin plugin3 = mock(Plugin.class);
        Plugin plugin4 = mock(Plugin.class);

        Set<Plugin> pluginsFromDescriptor = Sets.newHashSet(plugin1, plugin22, plugin3);
        Set<Plugin> pluginsFromDatabase = Sets.newHashSet(plugin1, plugin21, plugin4);

        PluginDescriptorParser pluginDescriptorParser = mock(PluginDescriptorParser.class);
        given(pluginDescriptorParser.loadPlugins()).willReturn(pluginsFromDescriptor);

        PluginDao pluginDao = Mockito.mock(PluginDao.class);
        given(pluginDao.list()).willReturn(pluginsFromDatabase);

        DefaultPluginAccessor pluginAccessor = new DefaultPluginAccessor();
        pluginAccessor.setPluginDescriptorParser(pluginDescriptorParser);
        pluginAccessor.setPluginDao(pluginDao);

        // when
        pluginAccessor.init();

        // then
        verify(pluginDao, never()).save(plugin1);
        verify(plugin22).changeStateTo(PluginState.ENABLED);
        verify(pluginDao).save(plugin22);
        verify(pluginDao).save(plugin3);
        verify(pluginDao).delete(plugin4);
    }

    @Test
    public void shouldListAllPlugins() throws Exception {
        // given
        Plugin plugin1 = mock(Plugin.class);
        given(plugin1.compareVersion(plugin1)).willReturn(0);
        given(plugin1.getPluginState()).willReturn(PluginState.DISABLED);

        Plugin plugin21 = mock(Plugin.class);
        given(plugin21.getPluginState()).willReturn(PluginState.ENABLED);
        Plugin plugin22 = mock(Plugin.class);
        given(plugin22.compareVersion(plugin21)).willReturn(1);

        Plugin plugin3 = mock(Plugin.class);
        given(plugin3.getPluginState()).willReturn(PluginState.ENABLED);
        Plugin plugin4 = mock(Plugin.class);

        Set<Plugin> pluginsFromDescriptor = Sets.newHashSet(plugin1, plugin22, plugin3);
        Set<Plugin> pluginsFromDatabase = Sets.newHashSet(plugin1, plugin21, plugin4);

        PluginDescriptorParser pluginDescriptorParser = mock(PluginDescriptorParser.class);
        given(pluginDescriptorParser.loadPlugins()).willReturn(pluginsFromDescriptor);

        PluginDao pluginDao = Mockito.mock(PluginDao.class);
        given(pluginDao.list()).willReturn(pluginsFromDatabase);

        DefaultPluginAccessor pluginAccessor = new DefaultPluginAccessor();
        pluginAccessor.setPluginDescriptorParser(pluginDescriptorParser);
        pluginAccessor.setPluginDao(pluginDao);

        // when
        pluginAccessor.init();

        // then
        assertThat(pluginAccessor.getPlugins(), hasItems(plugin1, plugin22, plugin3));
        assertThat(pluginAccessor.getPlugins(), not(hasItem(plugin21)));
        assertThat(pluginAccessor.getPlugins(), not(hasItem(plugin4)));

        assertThat(pluginAccessor.getEnabledPlugins(), hasItems(plugin22, plugin3));
        assertThat(pluginAccessor.getEnabledPlugins(), not(hasItem(plugin1)));

        assertEquals(plugin1, pluginAccessor.getPlugin("plugin1"));
        assertEquals(plugin22, pluginAccessor.getPlugin("plugin2"));
        assertEquals(plugin3, pluginAccessor.getPlugin("plugin3"));
        assertNull(pluginAccessor.getEnabledPlugin("plugin1"));
        assertEquals(plugin22, pluginAccessor.getEnabledPlugin("plugin2"));
        assertEquals(plugin3, pluginAccessor.getEnabledPlugin("plugin3"));
    }

    @Test(expected = IllegalStateException.class)
    public void shouldFailOnPluginDowngrade() throws Exception {
        // given
        Plugin plugin11 = mock(Plugin.class);
        Plugin plugin12 = mock(Plugin.class);
        given(plugin11.compareVersion(plugin12)).willReturn(-1);

        Set<Plugin> pluginsFromDescriptor = Sets.newHashSet(plugin11);
        Set<Plugin> pluginsFromDatabase = Sets.newHashSet(plugin12);

        PluginDescriptorParser pluginDescriptorParser = mock(PluginDescriptorParser.class);
        given(pluginDescriptorParser.loadPlugins()).willReturn(pluginsFromDescriptor);

        PluginDao pluginDao = Mockito.mock(PluginDao.class);
        given(pluginDao.list()).willReturn(pluginsFromDatabase);

        DefaultPluginAccessor pluginAccessor = new DefaultPluginAccessor();
        pluginAccessor.setPluginDescriptorParser(pluginDescriptorParser);
        pluginAccessor.setPluginDao(pluginDao);

        // when
        pluginAccessor.init();
    }

    @Test
    public void shouldPerformInit() throws Exception {
        // given
        Plugin plugin1 = mock(Plugin.class);
        Plugin plugin2 = mock(Plugin.class);

        Set<Plugin> pluginsFromDescriptor = Sets.newHashSet();
        Set<Plugin> pluginsFromDatabase = Sets.newHashSet(plugin1, plugin2);

        PluginDescriptorParser pluginDescriptorParser = mock(PluginDescriptorParser.class);
        given(pluginDescriptorParser.loadPlugins()).willReturn(pluginsFromDescriptor);

        PluginDao pluginDao = Mockito.mock(PluginDao.class);
        given(pluginDao.list()).willReturn(pluginsFromDatabase);

        DefaultPluginAccessor pluginAccessor = new DefaultPluginAccessor();
        pluginAccessor.setPluginDescriptorParser(pluginDescriptorParser);
        pluginAccessor.setPluginDao(pluginDao);

        // when
        pluginAccessor.init();

        // then
        verify(plugin1).init();
        verify(plugin2).init();
    }

    @Test
    public void shouldPerformEnableOnEnablingPlugins() throws Exception {
        // given
        Plugin plugin1 = mock(Plugin.class);
        given(plugin1.hasState(PluginState.ENABLING)).willReturn(true);
        Plugin plugin2 = mock(Plugin.class);

        Set<Plugin> pluginsFromDescriptor = Sets.newHashSet();
        Set<Plugin> pluginsFromDatabase = Sets.newHashSet(plugin1, plugin2);

        PluginDescriptorParser pluginDescriptorParser = mock(PluginDescriptorParser.class);
        given(pluginDescriptorParser.loadPlugins()).willReturn(pluginsFromDescriptor);

        PluginDao pluginDao = Mockito.mock(PluginDao.class);
        given(pluginDao.list()).willReturn(pluginsFromDatabase);

        DefaultPluginAccessor pluginAccessor = new DefaultPluginAccessor();
        pluginAccessor.setPluginDescriptorParser(pluginDescriptorParser);
        pluginAccessor.setPluginDao(pluginDao);

        // when
        pluginAccessor.init();

        // then
        verify(plugin1).changeStateTo(PluginState.ENABLED);
        verify(plugin2, never()).changeStateTo(PluginState.ENABLED);
        verify(pluginDao).save(plugin1);
        verify(pluginDao, never()).save(plugin2);
    }

}
