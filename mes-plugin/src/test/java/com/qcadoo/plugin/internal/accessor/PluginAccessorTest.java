package com.qcadoo.plugin.internal.accessor;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static org.hamcrest.CoreMatchers.not;
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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.qcadoo.model.beans.plugins.PluginsPlugin;
import com.qcadoo.plugin.api.Plugin;
import com.qcadoo.plugin.api.PluginState;
import com.qcadoo.plugin.api.Version;
import com.qcadoo.plugin.internal.api.ModuleFactoryAccessor;
import com.qcadoo.plugin.internal.api.PluginDao;
import com.qcadoo.plugin.internal.api.PluginDependencyManager;
import com.qcadoo.plugin.internal.api.PluginDescriptorParser;

public class PluginAccessorTest {

    private final PluginDescriptorParser pluginDescriptorParser = mock(PluginDescriptorParser.class);

    private final PluginDao pluginDao = Mockito.mock(PluginDao.class);

    private final PluginDependencyManager pluginDependencyManager = mock(PluginDependencyManager.class);

    private final ModuleFactoryAccessor moduleFactoryAccessor = mock(ModuleFactoryAccessor.class);

    private DefaultPluginAccessor pluginAccessor;

    @Before
    public void init() {
        pluginAccessor = new DefaultPluginAccessor();
        pluginAccessor.setPluginDescriptorParser(pluginDescriptorParser);
        pluginAccessor.setPluginDao(pluginDao);
        pluginAccessor.setPluginDependencyManager(pluginDependencyManager);
        pluginAccessor.setModuleFactoryAccessor(moduleFactoryAccessor);
    }

    @Test
    public void shouldname() throws Exception {
        Assert.assertTrue(true);
    }

    @Test
    public void shouldSynchronizePluginsFromClasspathAndDatabase() throws Exception {
        // given
        Plugin plugin1 = mock(Plugin.class);
        PluginsPlugin pluginsPlugin1 = mock(PluginsPlugin.class);
        given(plugin1.getIdentifier()).willReturn("identifier1");
        given(pluginsPlugin1.getIdentifier()).willReturn("identifier1");
        given(pluginsPlugin1.getState()).willReturn("ENABLED");
        given(pluginsPlugin1.getVersion()).willReturn("0.0.0");
        given(plugin1.compareVersion(new Version(pluginsPlugin1.getVersion()))).willReturn(0);

        PluginsPlugin pluginsPlugin21 = mock(PluginsPlugin.class);
        given(pluginsPlugin21.getIdentifier()).willReturn("identifier21");
        given(pluginsPlugin21.getIdentifier()).willReturn("identifier21");
        given(pluginsPlugin21.getState()).willReturn("ENABLED");
        Plugin plugin22 = mock(Plugin.class);
        given(plugin22.getIdentifier()).willReturn("identifier21");
        given(pluginsPlugin21.getVersion()).willReturn("0.0.0");
        given(plugin22.compareVersion(new Version(pluginsPlugin21.getVersion()))).willReturn(1);

        Plugin plugin3 = mock(Plugin.class);
        given(plugin3.getIdentifier()).willReturn("identifier3");
        PluginsPlugin pluginsPlugin4 = mock(PluginsPlugin.class);
        given(pluginsPlugin4.getIdentifier()).willReturn("identifier4");
        given(pluginsPlugin4.getState()).willReturn("ENABLED");

        Set<Plugin> pluginsFromDescriptor = Sets.newHashSet(plugin1, plugin22, plugin3);
        Set<PluginsPlugin> pluginsFromDatabase = Sets.newHashSet(pluginsPlugin1, pluginsPlugin21, pluginsPlugin4);

        given(pluginDescriptorParser.loadPlugins()).willReturn(pluginsFromDescriptor);

        given(pluginDao.list()).willReturn(pluginsFromDatabase);

        // when
        pluginAccessor.init();

        // then
        verify(pluginDao, never()).save(plugin1);
        verify(plugin22).changeStateTo(PluginState.ENABLED);
        verify(pluginDao).save(plugin22);
        verify(pluginDao).save(plugin3);
        verify(pluginDao).delete(pluginsPlugin4);
    }

    @Test
    public void shouldListAllPlugins() throws Exception {
        // given
        Plugin plugin1 = mock(Plugin.class, "plugin1");
        PluginsPlugin pluginsPlugin1 = mock(PluginsPlugin.class, "plugin1");
        given(plugin1.getIdentifier()).willReturn("identifier1");
        given(pluginsPlugin1.getIdentifier()).willReturn("identifier1");
        given(pluginsPlugin1.getVersion()).willReturn("0.0.0");
        given(plugin1.compareVersion(new Version(pluginsPlugin1.getVersion()))).willReturn(0);
        given(plugin1.hasState(PluginState.ENABLED)).willReturn(false);
        given(pluginsPlugin1.getState()).willReturn("DISABLED");

        PluginsPlugin pluginsPlugin21 = mock(PluginsPlugin.class, "plugin21");
        given(pluginsPlugin21.getIdentifier()).willReturn("identifier21");
        given(pluginsPlugin21.getState()).willReturn("ENABLED");
        Plugin plugin22 = mock(Plugin.class, "plugin22");
        given(plugin22.getIdentifier()).willReturn("identifier21");
        given(plugin22.hasState(PluginState.ENABLED)).willReturn(true);
        given(pluginsPlugin21.getVersion()).willReturn("0.0.0");
        given(plugin22.compareVersion(new Version(pluginsPlugin21.getVersion()))).willReturn(1);

        Plugin plugin3 = mock(Plugin.class, "plugin3");
        PluginsPlugin pluginsPlugin3 = mock(PluginsPlugin.class, "plugin3");
        given(plugin3.getIdentifier()).willReturn("identifier3");
        given(pluginsPlugin3.getIdentifier()).willReturn("identifier3");
        given(pluginsPlugin3.getVersion()).willReturn("0.0.0");
        given(plugin3.hasState(PluginState.ENABLED)).willReturn(true);
        given(pluginsPlugin3.getState()).willReturn("ENABLED");

        Plugin plugin4 = mock(Plugin.class, "plugin4");
        given(plugin4.getIdentifier()).willReturn("identifier4");
        given(plugin4.hasState(PluginState.ENABLED)).willReturn(false);

        Set<Plugin> pluginsFromDescriptor = Sets.newHashSet(plugin1, plugin22, plugin3, plugin4);
        Set<PluginsPlugin> pluginsFromDatabase = Sets.newHashSet(pluginsPlugin1, pluginsPlugin21, pluginsPlugin3);

        given(pluginDescriptorParser.loadPlugins()).willReturn(pluginsFromDescriptor);

        given(pluginDao.list()).willReturn(pluginsFromDatabase);

        // when
        pluginAccessor.init();

        // then
        verify(plugin1).changeStateTo(PluginState.DISABLED);
        verify(plugin22).changeStateTo(PluginState.ENABLED);
        verify(plugin3).changeStateTo(PluginState.ENABLED);
        verify(plugin4).changeStateTo(PluginState.DISABLED);
        verify(plugin4).changeStateTo(PluginState.ENABLED);

        assertThat(pluginAccessor.getPlugins(), hasItems(plugin1, plugin22, plugin3, plugin4));

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
        PluginsPlugin pluginsPlugin12 = mock(PluginsPlugin.class);
        given(pluginsPlugin12.getIdentifier()).willReturn("identifier11");
        given(pluginsPlugin12.getVersion()).willReturn("0.0.0");
        given(pluginsPlugin12.getState()).willReturn("ENABLED");
        given(plugin11.compareVersion(new Version(pluginsPlugin12.getVersion()))).willReturn(-1);

        Set<Plugin> pluginsFromDescriptor = Sets.newHashSet(plugin11);
        Set<PluginsPlugin> pluginsFromDatabase = Sets.newHashSet(pluginsPlugin12);

        given(pluginDescriptorParser.loadPlugins()).willReturn(pluginsFromDescriptor);

        given(pluginDao.list()).willReturn(pluginsFromDatabase);

        // when
        pluginAccessor.init();
    }

    @Test
    public void shouldPerformInit() throws Exception {
        // given
        Plugin plugin1 = mock(Plugin.class, "plugin1");
        PluginsPlugin pluginsPlugin1 = mock(PluginsPlugin.class, "plugin1");
        given(plugin1.getIdentifier()).willReturn("identifier1");
        given(pluginsPlugin1.getIdentifier()).willReturn("identifier1");
        given(pluginsPlugin1.getState()).willReturn("ENABLED");
        given(pluginsPlugin1.getVersion()).willReturn("0.0.0");
        Plugin plugin2 = mock(Plugin.class, "plugin2");
        PluginsPlugin pluginsPlugin2 = mock(PluginsPlugin.class, "plugin2");
        given(plugin2.getIdentifier()).willReturn("identifier2");
        given(pluginsPlugin2.getIdentifier()).willReturn("identifier2");
        given(pluginsPlugin2.getVersion()).willReturn("0.0.0");
        given(pluginsPlugin2.getState()).willReturn("ENABLED");

        Set<Plugin> pluginsFromDescriptor = Sets.newHashSet(plugin1, plugin2);
        Set<PluginsPlugin> pluginsFromDatabase = Sets.newHashSet(pluginsPlugin1, pluginsPlugin2);
        List<Plugin> sortedPluginsToInitialize = Lists.newArrayList(plugin2, plugin1);

        given(pluginDependencyManager.sortPluginsInDependencyOrder(Mockito.anyCollectionOf(Plugin.class))).willReturn(
                sortedPluginsToInitialize);

        given(pluginDescriptorParser.loadPlugins()).willReturn(pluginsFromDescriptor);

        given(pluginDao.list()).willReturn(pluginsFromDatabase);

        // when
        pluginAccessor.init();

        // then
        InOrder inOrder = inOrder(plugin2, plugin1, moduleFactoryAccessor);
        inOrder.verify(moduleFactoryAccessor).init();
        inOrder.verify(plugin2).init();
        inOrder.verify(plugin1).init();
    }

    @Test
    public void shouldPerformEnableOnEnablingPlugins() throws Exception {
        // given
        Plugin plugin1 = mock(Plugin.class);
        PluginsPlugin pluginsPlugin1 = mock(PluginsPlugin.class);
        given(plugin1.getIdentifier()).willReturn("identifier1");
        given(pluginsPlugin1.getIdentifier()).willReturn("identifier1");
        given(pluginsPlugin1.getState()).willReturn("ENABLING");
        given(pluginsPlugin1.getVersion()).willReturn("0.0.0");
        given(plugin1.hasState(PluginState.ENABLING)).willReturn(true);
        Plugin plugin2 = mock(Plugin.class);
        PluginsPlugin pluginsPlugin2 = mock(PluginsPlugin.class);
        given(plugin2.getIdentifier()).willReturn("identifier2");
        given(pluginsPlugin2.getIdentifier()).willReturn("identifier2");
        given(pluginsPlugin2.getState()).willReturn("ENABLING");
        given(pluginsPlugin2.getVersion()).willReturn("0.0.0");
        given(plugin2.hasState(PluginState.ENABLING)).willReturn(true);
        Plugin plugin3 = mock(Plugin.class);
        PluginsPlugin pluginsPlugin3 = mock(PluginsPlugin.class);
        given(pluginsPlugin3.getIdentifier()).willReturn("identifier3");
        given(pluginsPlugin3.getVersion()).willReturn("0.0.0");
        given(pluginsPlugin3.getState()).willReturn("DISABLED");
        given(plugin3.getIdentifier()).willReturn("identifier3");

        Set<Plugin> pluginsFromDescriptor = Sets.newHashSet(plugin1, plugin2, plugin3);
        Set<PluginsPlugin> pluginsFromDatabase = Sets.newHashSet(pluginsPlugin1, pluginsPlugin2, pluginsPlugin3);
        List<Plugin> sortedPluginsToInitialize = Lists.newArrayList(plugin2, plugin1);

        given(pluginDependencyManager.sortPluginsInDependencyOrder(Mockito.anyCollectionOf(Plugin.class))).willReturn(
                sortedPluginsToInitialize);

        given(pluginDescriptorParser.loadPlugins()).willReturn(pluginsFromDescriptor);

        given(pluginDao.list()).willReturn(pluginsFromDatabase);

        // when
        pluginAccessor.init();

        // then
        InOrder inOrder = inOrder(plugin2, plugin1, moduleFactoryAccessor);
        inOrder.verify(moduleFactoryAccessor).init();
        inOrder.verify(plugin2).init();
        inOrder.verify(plugin1).init();
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
        PluginsPlugin pluginsPlugin1 = mock(PluginsPlugin.class);
        given(pluginsPlugin1.getState()).willReturn(PluginState.TEMPORARY.toString());
        PluginsPlugin pluginsPlugin2 = mock(PluginsPlugin.class);
        given(pluginsPlugin2.getState()).willReturn(PluginState.ENABLED.toString());

        Set<Plugin> pluginsFromDescriptor = Sets.newHashSet();
        Set<PluginsPlugin> pluginsFromDatabase = Sets.newHashSet(pluginsPlugin1, pluginsPlugin2);

        given(pluginDescriptorParser.loadPlugins()).willReturn(pluginsFromDescriptor);
        given(pluginDao.list()).willReturn(pluginsFromDatabase);

        // when
        pluginAccessor.init();

        verify(pluginDao, never()).delete(pluginsPlugin1);
        verify(pluginDao).delete(pluginsPlugin2);
    }

    @Test
    public void shouldRemovePlugin() throws Exception {
        // given
        Plugin plugin1 = mock(Plugin.class, "plugin1");

        given(plugin1.getIdentifier()).willReturn("identifier1");

        PluginsPlugin pluginsPlugin1 = mock(PluginsPlugin.class);
        given(pluginsPlugin1.getIdentifier()).willReturn("identifier1");
        given(pluginsPlugin1.getState()).willReturn("ENABLED");
        given(pluginsPlugin1.getVersion()).willReturn("0.0.0");

        Plugin plugin2 = mock(Plugin.class);
        given(plugin2.getIdentifier()).willReturn("identifier2");

        PluginsPlugin pluginsPlugin2 = mock(PluginsPlugin.class);
        given(pluginsPlugin2.getIdentifier()).willReturn("identifier2");
        given(pluginsPlugin2.getState()).willReturn("ENABLED");
        given(pluginsPlugin2.getVersion()).willReturn("0.0.0");

        given(pluginDescriptorParser.loadPlugins()).willReturn(Sets.newHashSet(plugin1, plugin2));

        given(pluginDao.list()).willReturn(Sets.<PluginsPlugin> newHashSet(pluginsPlugin1, pluginsPlugin2));

        pluginAccessor.init();

        // when
        pluginAccessor.removePlugin(plugin1);

        // then
        assertEquals(1, pluginAccessor.getPlugins().size());
        assertThat(pluginAccessor.getPlugins(), hasItems(plugin2));
    }

    @Test
    public void shouldSaveNewPlugin() throws Exception {
        // given
        Plugin plugin1 = mock(Plugin.class, "plugin1");

        given(plugin1.getIdentifier()).willReturn("identifier1");

        PluginsPlugin pluginsPlugin1 = mock(PluginsPlugin.class);
        given(pluginsPlugin1.getIdentifier()).willReturn("identifier1");
        given(pluginsPlugin1.getState()).willReturn("ENABLED");
        given(pluginsPlugin1.getVersion()).willReturn("0.0.0");

        Plugin plugin2 = mock(Plugin.class);
        given(plugin2.getIdentifier()).willReturn("identifier2");

        given(pluginDescriptorParser.loadPlugins()).willReturn(Sets.newHashSet(plugin1));

        given(pluginDao.list()).willReturn(Sets.<PluginsPlugin> newHashSet(pluginsPlugin1));

        pluginAccessor.init();

        // when
        pluginAccessor.savePlugin(plugin2);

        // then
        assertThat(pluginAccessor.getPlugins(), hasItems(plugin1, plugin2));
    }

    @Test
    public void shouldSaveExistingPlugin() throws Exception {
        // given
        Plugin plugin1 = mock(Plugin.class, "plugin1");

        given(plugin1.getIdentifier()).willReturn("identifier1");

        Plugin plugin2 = mock(Plugin.class);
        given(plugin2.getIdentifier()).willReturn("identifier1");

        PluginsPlugin pluginsPlugin2 = mock(PluginsPlugin.class);
        given(pluginsPlugin2.getIdentifier()).willReturn("identifier1");
        given(pluginsPlugin2.getState()).willReturn("ENABLED");
        given(pluginsPlugin2.getVersion()).willReturn("0.0.0");

        given(pluginDescriptorParser.loadPlugins()).willReturn(Sets.newHashSet(plugin1));

        given(pluginDao.list()).willReturn(Sets.<PluginsPlugin> newHashSet(pluginsPlugin2));

        pluginAccessor.init();

        // when
        pluginAccessor.savePlugin(plugin2);

        // then
        assertEquals(1, pluginAccessor.getPlugins().size());
        assertThat(pluginAccessor.getPlugins(), hasItems(plugin2));
    }

}
