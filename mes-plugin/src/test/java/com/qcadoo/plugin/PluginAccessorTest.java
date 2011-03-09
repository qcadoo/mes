package com.qcadoo.plugin;

import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.hasItem;
import static org.junit.matchers.JUnitMatchers.hasItems;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class PluginAccessorTest {

    private PluginDescriptorParser pluginDescriptorParser = mock(PluginDescriptorParser.class);

    private PluginDao pluginDao = Mockito.mock(PluginDao.class);

    private PluginDependencyManager pluginDependencyManager = mock(PluginDependencyManager.class);

    private DefaultPluginAccessor pluginAccessor;

    @Before
    public void init() {
        pluginAccessor = new DefaultPluginAccessor();
        pluginAccessor.setPluginDescriptorParser(pluginDescriptorParser);
        pluginAccessor.setPluginDao(pluginDao);
        pluginAccessor.setPluginDependencyManager(pluginDependencyManager);
    }

    @Test
    public void shouldSynchronizePluginsFromClasspathAndDatabase() throws Exception {
        // given
        Plugin plugin1 = mock(Plugin.class);
        given(plugin1.getIdentifier()).willReturn("identifier1");
        given(plugin1.compareVersion(plugin1)).willReturn(0);

        Plugin plugin21 = mock(Plugin.class);
        given(plugin21.getIdentifier()).willReturn("identifier21");
        given(plugin21.getPluginState()).willReturn(PluginState.ENABLED);
        Plugin plugin22 = mock(Plugin.class);
        given(plugin22.getIdentifier()).willReturn("identifier21");
        given(plugin22.compareVersion(plugin21)).willReturn(1);

        Plugin plugin3 = mock(Plugin.class);
        given(plugin3.getIdentifier()).willReturn("identifier3");
        Plugin plugin4 = mock(Plugin.class);
        given(plugin4.getIdentifier()).willReturn("identifier4");

        Set<Plugin> pluginsFromDescriptor = Sets.newHashSet(plugin1, plugin22, plugin3);
        Set<Plugin> pluginsFromDatabase = Sets.newHashSet(plugin1, plugin21, plugin4);

        given(pluginDescriptorParser.loadPlugins()).willReturn(pluginsFromDescriptor);

        given(pluginDao.list()).willReturn(pluginsFromDatabase);

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
        Plugin plugin1 = mock(Plugin.class, "plugin1");
        given(plugin1.getIdentifier()).willReturn("identifier1");
        given(plugin1.compareVersion(plugin1)).willReturn(0);
        given(plugin1.hasState(PluginState.ENABLED)).willReturn(false);
        given(plugin1.getPluginState()).willReturn(PluginState.DISABLED);

        Plugin plugin21 = mock(Plugin.class, "plugin21");
        given(plugin21.getIdentifier()).willReturn("identifier21");
        given(plugin21.getPluginState()).willReturn(PluginState.ENABLED);
        Plugin plugin22 = mock(Plugin.class, "plugin22");
        given(plugin22.getIdentifier()).willReturn("identifier21");
        given(plugin22.hasState(PluginState.ENABLED)).willReturn(true);
        given(plugin22.compareVersion(plugin21)).willReturn(1);

        Plugin plugin3 = mock(Plugin.class, "plugin3");
        given(plugin3.getIdentifier()).willReturn("identifier3");
        given(plugin3.hasState(PluginState.ENABLED)).willReturn(true);
        given(plugin3.getPluginState()).willReturn(PluginState.ENABLED);

        Plugin plugin4 = mock(Plugin.class, "plugin4");
        given(plugin4.getIdentifier()).willReturn("identifier4");
        given(plugin4.hasState(PluginState.ENABLED)).willReturn(false);

        Set<Plugin> pluginsFromDescriptor = Sets.newHashSet(plugin1, plugin22, plugin3, plugin4);
        Set<Plugin> pluginsFromDatabase = Sets.newHashSet(plugin1, plugin21, plugin3);

        given(pluginDescriptorParser.loadPlugins()).willReturn(pluginsFromDescriptor);

        given(pluginDao.list()).willReturn(pluginsFromDatabase);

        // when
        pluginAccessor.init();

        // then
        verify(plugin1).changeStateTo(PluginState.DISABLED);
        verify(plugin22).changeStateTo(PluginState.ENABLED);
        verify(plugin3).changeStateTo(PluginState.ENABLED);
        verify(plugin4).changeStateTo(PluginState.DISABLED);

        assertThat(pluginAccessor.getPlugins(), hasItems(plugin1, plugin22, plugin3, plugin4));
        assertThat(pluginAccessor.getPlugins(), not(hasItem(plugin21)));

        assertThat(pluginAccessor.getEnabledPlugins(), hasItems(plugin22, plugin3));
        assertThat(pluginAccessor.getEnabledPlugins(), not(hasItem(plugin1)));
        assertThat(pluginAccessor.getEnabledPlugins(), not(hasItem(plugin4)));

        assertEquals(plugin1, pluginAccessor.getPlugin("identifier1"));
        assertEquals(plugin22, pluginAccessor.getPlugin("identifier21"));
        assertEquals(plugin3, pluginAccessor.getPlugin("identifier3"));
        assertEquals(plugin4, pluginAccessor.getPlugin("identifier4"));
        assertNull(pluginAccessor.getEnabledPlugin("identifier1"));
        assertEquals(plugin22, pluginAccessor.getEnabledPlugin("identifier21"));
        assertEquals(plugin3, pluginAccessor.getEnabledPlugin("identifier3"));
        assertNull(pluginAccessor.getEnabledPlugin("identifier4"));
    }

    @Test(expected = IllegalStateException.class)
    public void shouldFailOnPluginDowngrade() throws Exception {
        // given
        Plugin plugin11 = mock(Plugin.class);
        given(plugin11.getIdentifier()).willReturn("identifier11");
        Plugin plugin12 = mock(Plugin.class);
        given(plugin12.getIdentifier()).willReturn("identifier11");
        given(plugin11.compareVersion(plugin12)).willReturn(-1);

        Set<Plugin> pluginsFromDescriptor = Sets.newHashSet(plugin11);
        Set<Plugin> pluginsFromDatabase = Sets.newHashSet(plugin12);

        given(pluginDescriptorParser.loadPlugins()).willReturn(pluginsFromDescriptor);

        given(pluginDao.list()).willReturn(pluginsFromDatabase);

        // when
        pluginAccessor.init();
    }

    @Test
    public void shouldPerformInit() throws Exception {
        // given
        Plugin plugin1 = mock(Plugin.class, "plugin1");
        given(plugin1.getIdentifier()).willReturn("identifier1");
        Plugin plugin2 = mock(Plugin.class, "plugin2");
        given(plugin2.getIdentifier()).willReturn("identifier2");

        Set<Plugin> pluginsFromDescriptor = Sets.newHashSet(plugin1, plugin2);
        Set<Plugin> pluginsFromDatabase = Sets.newHashSet(plugin1, plugin2);
        List<Plugin> sortedPluginsToInitialize = Lists.newArrayList(plugin2, plugin1);

        given(pluginDependencyManager.sortPlugins(Mockito.anyCollectionOf(Plugin.class))).willReturn(sortedPluginsToInitialize);

        given(pluginDescriptorParser.loadPlugins()).willReturn(pluginsFromDescriptor);

        given(pluginDao.list()).willReturn(pluginsFromDatabase);

        // when
        pluginAccessor.init();

        // then
        InOrder inOrder = inOrder(plugin2, plugin1);
        inOrder.verify(plugin2).init();
        inOrder.verify(plugin1).init();
    }

    @Test
    public void shouldPerformEnableOnEnablingPlugins() throws Exception {
        // given
        Plugin plugin1 = mock(Plugin.class);
        given(plugin1.getIdentifier()).willReturn("identifier1");
        given(plugin1.hasState(PluginState.ENABLING)).willReturn(true);
        Plugin plugin2 = mock(Plugin.class);
        given(plugin2.getIdentifier()).willReturn("identifier2");
        given(plugin2.hasState(PluginState.ENABLING)).willReturn(true);
        Plugin plugin3 = mock(Plugin.class);
        given(plugin3.getIdentifier()).willReturn("identifier3");

        Set<Plugin> pluginsFromDescriptor = Sets.newHashSet(plugin1, plugin2, plugin3);
        Set<Plugin> pluginsFromDatabase = Sets.newHashSet(plugin1, plugin2, plugin3);
        List<Plugin> sortedPluginsToInitialize = Lists.newArrayList(plugin2, plugin1);

        given(pluginDependencyManager.sortPlugins(Mockito.anyCollectionOf(Plugin.class))).willReturn(sortedPluginsToInitialize);

        given(pluginDescriptorParser.loadPlugins()).willReturn(pluginsFromDescriptor);

        given(pluginDao.list()).willReturn(pluginsFromDatabase);

        // when
        pluginAccessor.init();

        // then
        InOrder inOrder = inOrder(plugin2, plugin1);
        inOrder.verify(plugin2).changeStateTo(PluginState.ENABLED);
        inOrder.verify(plugin1).changeStateTo(PluginState.ENABLED);
        verify(plugin3, never()).changeStateTo(PluginState.ENABLED);
        verify(pluginDao).save(plugin1);
        verify(pluginDao).save(plugin2);
        verify(pluginDao, never()).save(plugin3);
    }

    @Test
    public void shouldNotDeleteTemporaryPlugins() throws Exception {
        // given
        Plugin plugin1 = mock(Plugin.class);
        given(plugin1.hasState(PluginState.TEMPORARY)).willReturn(true);
        Plugin plugin2 = mock(Plugin.class);
        given(plugin2.hasState(PluginState.TEMPORARY)).willReturn(false);

        Set<Plugin> pluginsFromDescriptor = Sets.newHashSet();
        Set<Plugin> pluginsFromDatabase = Sets.newHashSet(plugin1, plugin2);

        given(pluginDescriptorParser.loadPlugins()).willReturn(pluginsFromDescriptor);
        given(pluginDao.list()).willReturn(pluginsFromDatabase);

        // when
        pluginAccessor.init();

        verify(pluginDao, never()).delete(plugin1);
        verify(pluginDao).delete(plugin2);
    }

}
