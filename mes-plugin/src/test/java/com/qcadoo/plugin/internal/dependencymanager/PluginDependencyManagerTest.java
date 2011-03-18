package com.qcadoo.plugin.internal.dependencymanager;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.qcadoo.plugin.api.Plugin;
import com.qcadoo.plugin.api.PluginAccessor;
import com.qcadoo.plugin.api.PluginDependencyInformation;
import com.qcadoo.plugin.api.PluginState;
import com.qcadoo.plugin.api.Version;
import com.qcadoo.plugin.api.VersionOfDependency;

public class PluginDependencyManagerTest {

    private PluginDependencyInformation dependencyInfo1 = new PluginDependencyInformation("testPlugin1");

    private PluginDependencyInformation dependencyInfo2 = new PluginDependencyInformation("testPlugin2");

    private PluginDependencyInformation dependencyInfo3 = new PluginDependencyInformation("testPlugin3");

    private PluginDependencyInformation dependencyInfo4 = new PluginDependencyInformation("testPlugin4");

    private Plugin plugin1;

    private Plugin plugin2;

    private Plugin plugin3;

    private Plugin plugin4;

    private final PluginAccessor pluginAccessor = mock(PluginAccessor.class);

    private DefaultPluginDependencyManager manager = null;

    @Before
    public void init() {
        manager = new DefaultPluginDependencyManager();
        manager.setPluginAccessor(pluginAccessor);

        plugin1 = mock(Plugin.class, RETURNS_DEEP_STUBS);
        given(plugin1.getIdentifier()).willReturn("testPlugin1");
        given(plugin1.getVersion()).willReturn(new Version("1.1"));
        given(plugin1.toString()).willReturn("plugin1");

        plugin2 = mock(Plugin.class, RETURNS_DEEP_STUBS);
        given(plugin2.getIdentifier()).willReturn("testPlugin2");
        given(plugin2.getVersion()).willReturn(new Version("1.1"));
        given(plugin2.toString()).willReturn("plugin2");

        plugin3 = mock(Plugin.class, RETURNS_DEEP_STUBS);
        given(plugin3.getIdentifier()).willReturn("testPlugin3");
        given(plugin3.getVersion()).willReturn(new Version("1.1"));
        given(plugin3.toString()).willReturn("plugin3");

        plugin4 = mock(Plugin.class, RETURNS_DEEP_STUBS);
        given(plugin4.getIdentifier()).willReturn("testPlugin4");
        given(plugin4.getVersion()).willReturn(new Version("1.1"));
        given(plugin4.toString()).willReturn("plugin4");
    }

    @Test
    public void shouldReturnEmptyDependencyToEnableWhenNoDependenciesSpecifiedInOnePlugin() throws Exception {
        // given
        given(plugin1.getRequiredPlugins()).willReturn(Collections.<PluginDependencyInformation> emptySet());

        // when
        PluginDependencyResult result = manager.getDependenciesToEnable(singletonList(plugin1));

        // then
        assertFalse(result.isCyclic());
        assertEquals(0, result.getDependenciesToEnable().size());
        assertEquals(0, result.getUnsatisfiedDependencies().size());
        assertEquals(0, result.getDependenciesToDisable().size());
    }

    @Test
    public void shouldReturnSingleDisabledDependencyForOnePlugin() throws Exception {
        // given
        given(plugin2.getState()).willReturn(PluginState.DISABLED);

        given(pluginAccessor.getPlugin("testPlugin2")).willReturn(plugin2);

        given(plugin1.getRequiredPlugins()).willReturn(Collections.singleton(dependencyInfo2));

        // when
        PluginDependencyResult result = manager.getDependenciesToEnable(singletonList(plugin1));

        // then
        assertFalse(result.isCyclic());
        assertEquals(1, result.getDependenciesToEnable().size());
        assertTrue(result.getDependenciesToEnable().contains(new PluginDependencyInformation("testPlugin2")));
        assertEquals(0, result.getUnsatisfiedDependencies().size());
        assertEquals(0, result.getDependenciesToDisable().size());
    }

    @Test
    public void shouldReturnMultipleDisabledDependencyForOnePlugin() throws Exception {
        // given
        given(plugin1.getState()).willReturn(PluginState.DISABLED);
        given(plugin2.getState()).willReturn(PluginState.TEMPORARY);
        given(plugin3.getState()).willReturn(PluginState.ENABLED);

        given(pluginAccessor.getPlugin("testPlugin1")).willReturn(plugin1);
        given(pluginAccessor.getPlugin("testPlugin2")).willReturn(plugin2);
        given(pluginAccessor.getPlugin("testPlugin3")).willReturn(plugin3);

        Plugin pluginToEnable = mock(Plugin.class);
        Set<PluginDependencyInformation> disabledRequiredPlugins = new HashSet<PluginDependencyInformation>();
        disabledRequiredPlugins.add(dependencyInfo1);
        disabledRequiredPlugins.add(dependencyInfo2);
        disabledRequiredPlugins.add(dependencyInfo3);
        given(pluginToEnable.getRequiredPlugins()).willReturn(disabledRequiredPlugins);

        // when
        PluginDependencyResult result = manager.getDependenciesToEnable(singletonList(pluginToEnable));

        // then
        assertFalse(result.isCyclic());
        assertEquals(2, result.getDependenciesToEnable().size());
        assertTrue(result.getDependenciesToEnable().contains(new PluginDependencyInformation("testPlugin1")));
        assertTrue(result.getDependenciesToEnable().contains(new PluginDependencyInformation("testPlugin2")));
        assertEquals(0, result.getUnsatisfiedDependencies().size());
        assertEquals(0, result.getDependenciesToDisable().size());
    }

    @Test
    public void shouldReturnUnsatisfiedDependenciesForOnePluginWhenNoDependencyPluginFoundOrVersionIsNotMet() throws Exception {
        // given
        given(plugin1.getState()).willReturn(PluginState.DISABLED);
        given(plugin2.getState()).willReturn(PluginState.TEMPORARY);
        given(plugin3.getState()).willReturn(PluginState.TEMPORARY);

        given(pluginAccessor.getPlugin("testPlugin1")).willReturn(plugin1);
        given(pluginAccessor.getPlugin("testPlugin2")).willReturn(plugin2);
        given(pluginAccessor.getPlugin("testPlugin3")).willReturn(plugin3);
        given(pluginAccessor.getPlugin("testPlugin4")).willReturn(null);

        dependencyInfo3 = new PluginDependencyInformation("testPlugin3", new VersionOfDependency("[2"));

        Plugin pluginToEnable = mock(Plugin.class);
        Set<PluginDependencyInformation> disabledRequiredPlugins = new HashSet<PluginDependencyInformation>();
        disabledRequiredPlugins.add(dependencyInfo1);
        disabledRequiredPlugins.add(dependencyInfo2);
        disabledRequiredPlugins.add(dependencyInfo3);
        disabledRequiredPlugins.add(dependencyInfo4);
        given(pluginToEnable.getRequiredPlugins()).willReturn(disabledRequiredPlugins);

        // when
        PluginDependencyResult result = manager.getDependenciesToEnable(singletonList(pluginToEnable));

        // then
        assertFalse(result.isCyclic());
        assertEquals(0, result.getDependenciesToEnable().size());
        assertEquals(2, result.getUnsatisfiedDependencies().size());
        assertTrue(result.getUnsatisfiedDependencies().contains(
                new PluginDependencyInformation("testPlugin3", new VersionOfDependency("[2"))));
        assertTrue(result.getUnsatisfiedDependencies().contains(new PluginDependencyInformation("testPlugin4")));
        assertEquals(0, result.getDependenciesToDisable().size());
    }

    @Test
    public void shouldReturnDisabledDependenciesForMultiplePlugins() throws Exception {
        // given
        Set<PluginDependencyInformation> disabledRequiredPlugins = new HashSet<PluginDependencyInformation>();
        disabledRequiredPlugins.add(dependencyInfo2);
        disabledRequiredPlugins.add(dependencyInfo4);
        given(plugin1.getRequiredPlugins()).willReturn(disabledRequiredPlugins);

        Set<PluginDependencyInformation> disabledRequiredPlugins2 = new HashSet<PluginDependencyInformation>();
        disabledRequiredPlugins2.add(dependencyInfo3);
        given(plugin2.getRequiredPlugins()).willReturn(disabledRequiredPlugins2);

        given(plugin3.getState()).willReturn(PluginState.TEMPORARY);

        given(plugin4.getState()).willReturn(PluginState.TEMPORARY);

        given(pluginAccessor.getPlugin("testPlugin3")).willReturn(plugin3);
        given(pluginAccessor.getPlugin("testPlugin4")).willReturn(plugin4);

        List<Plugin> plugins = new ArrayList<Plugin>();
        plugins.add(plugin1);
        plugins.add(plugin2);

        // when
        PluginDependencyResult result = manager.getDependenciesToEnable(plugins);

        // then
        assertFalse(result.isCyclic());
        assertEquals(2, result.getDependenciesToEnable().size());
        assertTrue(result.getDependenciesToEnable().contains(new PluginDependencyInformation("testPlugin3")));
        assertTrue(result.getDependenciesToEnable().contains(new PluginDependencyInformation("testPlugin4")));
        assertEquals(0, result.getUnsatisfiedDependencies().size());
        assertEquals(0, result.getDependenciesToDisable().size());
    }

    @Test
    public void shouldReturnUnsatisfiedDependenciesForMultiplePlugins() throws Exception {
        // given
        Set<PluginDependencyInformation> disabledRequiredPlugins = new HashSet<PluginDependencyInformation>();
        disabledRequiredPlugins.add(dependencyInfo2);
        disabledRequiredPlugins.add(dependencyInfo4);
        given(plugin1.getRequiredPlugins()).willReturn(disabledRequiredPlugins);

        Set<PluginDependencyInformation> disabledRequiredPlugins2 = new HashSet<PluginDependencyInformation>();
        disabledRequiredPlugins2.add(dependencyInfo3);
        given(plugin2.getRequiredPlugins()).willReturn(disabledRequiredPlugins2);

        given(plugin3.getState()).willReturn(PluginState.TEMPORARY);

        given(plugin4.getState()).willReturn(PluginState.TEMPORARY);

        given(pluginAccessor.getPlugin("testPlugin3")).willReturn(plugin3);

        List<Plugin> plugins = new ArrayList<Plugin>();
        plugins.add(plugin1);
        plugins.add(plugin2);

        // when
        PluginDependencyResult result = manager.getDependenciesToEnable(plugins);

        // then
        assertFalse(result.isCyclic());
        assertEquals(0, result.getDependenciesToEnable().size());
        assertEquals(1, result.getUnsatisfiedDependencies().size());
        assertTrue(result.getUnsatisfiedDependencies().contains(new PluginDependencyInformation("testPlugin4")));
        assertEquals(0, result.getDependenciesToDisable().size());
    }

    @Test
    public void shouldCheckDependenciesDependenciesAndReturnMultipleDependencies() throws Exception {
        // given
        Set<PluginDependencyInformation> disabledRequiredPlugins = new HashSet<PluginDependencyInformation>();
        disabledRequiredPlugins.add(dependencyInfo2);
        given(plugin1.getRequiredPlugins()).willReturn(disabledRequiredPlugins);

        given(plugin2.getState()).willReturn(PluginState.DISABLED);
        Set<PluginDependencyInformation> disabledRequiredPlugins2 = new HashSet<PluginDependencyInformation>();
        disabledRequiredPlugins2.add(dependencyInfo3);
        given(plugin2.getRequiredPlugins()).willReturn(disabledRequiredPlugins2);

        given(plugin3.getState()).willReturn(PluginState.TEMPORARY);

        given(pluginAccessor.getPlugin("testPlugin2")).willReturn(plugin2);
        given(pluginAccessor.getPlugin("testPlugin3")).willReturn(plugin3);

        // when
        PluginDependencyResult result = manager.getDependenciesToEnable(singletonList(plugin1));

        // then
        assertFalse(result.isCyclic());
        assertEquals(2, result.getDependenciesToEnable().size());
        assertTrue(result.getDependenciesToEnable().contains(new PluginDependencyInformation("testPlugin2")));
        assertTrue(result.getDependenciesToEnable().contains(new PluginDependencyInformation("testPlugin3")));
        assertEquals(0, result.getUnsatisfiedDependencies().size());
        assertEquals(0, result.getDependenciesToDisable().size());
    }

    @Test
    public void shouldReturnDisabledDependenciesForMultiplePluginsHardExample() throws Exception {
        // given

        given(plugin1.getRequiredPlugins()).willReturn(Collections.singleton(dependencyInfo2));
        given(plugin2.getRequiredPlugins()).willReturn(Collections.singleton(dependencyInfo3));
        given(plugin3.getRequiredPlugins()).willReturn(Collections.singleton(dependencyInfo4));

        given(plugin2.getState()).willReturn(PluginState.TEMPORARY);
        given(plugin3.getState()).willReturn(PluginState.TEMPORARY);
        given(plugin4.getState()).willReturn(PluginState.TEMPORARY);

        given(pluginAccessor.getPlugin("testPlugin2")).willReturn(plugin2);
        given(pluginAccessor.getPlugin("testPlugin3")).willReturn(plugin3);
        given(pluginAccessor.getPlugin("testPlugin4")).willReturn(plugin4);

        List<Plugin> plugins = new ArrayList<Plugin>();
        plugins.add(plugin1);
        plugins.add(plugin3);

        // when
        PluginDependencyResult result = manager.getDependenciesToEnable(plugins);

        // then
        assertFalse(result.isCyclic());
        assertEquals(2, result.getDependenciesToEnable().size());
        assertTrue(result.getDependenciesToEnable().contains(new PluginDependencyInformation("testPlugin2")));
        assertTrue(result.getDependenciesToEnable().contains(new PluginDependencyInformation("testPlugin4")));
        assertEquals(0, result.getUnsatisfiedDependencies().size());
        assertEquals(0, result.getDependenciesToDisable().size());
    }

    @Test
    public void shouldReturnValidResultForCyclicDependencies() throws Exception {
        // given
        Set<PluginDependencyInformation> disabledRequiredPlugins1 = new HashSet<PluginDependencyInformation>();
        disabledRequiredPlugins1.add(dependencyInfo2);
        given(plugin1.getRequiredPlugins()).willReturn(disabledRequiredPlugins1);

        Set<PluginDependencyInformation> disabledRequiredPlugins2 = new HashSet<PluginDependencyInformation>();
        disabledRequiredPlugins2.add(dependencyInfo3);
        disabledRequiredPlugins2.add(dependencyInfo4);
        given(plugin2.getRequiredPlugins()).willReturn(disabledRequiredPlugins2);

        Set<PluginDependencyInformation> disabledRequiredPlugins3 = new HashSet<PluginDependencyInformation>();
        disabledRequiredPlugins3.add(dependencyInfo1);
        given(plugin3.getRequiredPlugins()).willReturn(disabledRequiredPlugins3);

        List<Plugin> plugins = new ArrayList<Plugin>();
        plugins.add(plugin1);
        plugins.add(plugin2);
        plugins.add(plugin3);

        // when
        PluginDependencyResult result = manager.getDependenciesToEnable(plugins);

        // then
        assertTrue(result.isCyclic());
        assertEquals(0, result.getDependenciesToEnable().size());
        assertEquals(0, result.getUnsatisfiedDependencies().size());
        assertEquals(0, result.getDependenciesToDisable().size());
    }

    @Test
    public void shouldSetCyclicFlagWhenCyclicDependenciesDependencies() throws Exception {
        // given
        Set<PluginDependencyInformation> disabledRequiredPlugins1 = new HashSet<PluginDependencyInformation>();
        disabledRequiredPlugins1.add(dependencyInfo2);
        given(plugin1.getRequiredPlugins()).willReturn(disabledRequiredPlugins1);

        Set<PluginDependencyInformation> disabledRequiredPlugins2 = new HashSet<PluginDependencyInformation>();
        disabledRequiredPlugins2.add(dependencyInfo3);
        given(plugin2.getState()).willReturn(PluginState.DISABLED);
        given(plugin2.getRequiredPlugins()).willReturn(disabledRequiredPlugins2);

        Set<PluginDependencyInformation> disabledRequiredPlugins3 = new HashSet<PluginDependencyInformation>();
        disabledRequiredPlugins3.add(dependencyInfo1);
        given(plugin3.getState()).willReturn(PluginState.TEMPORARY);
        given(plugin3.getRequiredPlugins()).willReturn(disabledRequiredPlugins3);

        given(pluginAccessor.getPlugin("testPlugin2")).willReturn(plugin2);
        given(pluginAccessor.getPlugin("testPlugin3")).willReturn(plugin3);

        // when
        PluginDependencyResult result = manager.getDependenciesToEnable(singletonList(plugin1));

        // then
        assertTrue(result.isCyclic());
        assertEquals(0, result.getDependenciesToEnable().size());
        assertEquals(0, result.getUnsatisfiedDependencies().size());
        assertEquals(0, result.getDependenciesToDisable().size());
    }

    //
    @Test
    public void shouldSetCyclicFlagWhenCyclicDependenciesDependenciesButNotDependencyToArgument() throws Exception {
        // given
        Set<PluginDependencyInformation> disabledRequiredPlugins1 = new HashSet<PluginDependencyInformation>();
        disabledRequiredPlugins1.add(dependencyInfo2);
        given(plugin1.getRequiredPlugins()).willReturn(disabledRequiredPlugins1);

        Set<PluginDependencyInformation> disabledRequiredPlugins2 = new HashSet<PluginDependencyInformation>();
        disabledRequiredPlugins2.add(dependencyInfo3);
        given(plugin2.getState()).willReturn(PluginState.DISABLED);
        given(plugin2.getRequiredPlugins()).willReturn(disabledRequiredPlugins2);

        Set<PluginDependencyInformation> disabledRequiredPlugins3 = new HashSet<PluginDependencyInformation>();
        disabledRequiredPlugins3.add(dependencyInfo2);
        given(plugin3.getState()).willReturn(PluginState.TEMPORARY);
        given(plugin3.getRequiredPlugins()).willReturn(disabledRequiredPlugins3);

        given(pluginAccessor.getPlugin("testPlugin2")).willReturn(plugin2);
        given(pluginAccessor.getPlugin("testPlugin3")).willReturn(plugin3);

        // when
        PluginDependencyResult result = manager.getDependenciesToEnable(singletonList(plugin1));

        // then
        assertTrue(result.isCyclic());
        assertEquals(0, result.getDependenciesToEnable().size());
        assertEquals(0, result.getUnsatisfiedDependencies().size());
        assertEquals(0, result.getDependenciesToDisable().size());
    }

    @Test
    public void shouldSetCyclicFlagWhenCyclicDependenciesDependenciesToArgumentsDependency() throws Exception {
        // given
        Set<PluginDependencyInformation> disabledRequiredPlugins1 = new HashSet<PluginDependencyInformation>();
        disabledRequiredPlugins1.add(dependencyInfo2);
        given(plugin1.getRequiredPlugins()).willReturn(disabledRequiredPlugins1);

        Set<PluginDependencyInformation> disabledRequiredPlugins2 = new HashSet<PluginDependencyInformation>();
        disabledRequiredPlugins2.add(dependencyInfo3);
        given(plugin2.getState()).willReturn(PluginState.DISABLED);
        given(plugin2.getRequiredPlugins()).willReturn(disabledRequiredPlugins2);

        Set<PluginDependencyInformation> disabledRequiredPlugins3 = new HashSet<PluginDependencyInformation>();
        disabledRequiredPlugins3.add(dependencyInfo4);
        given(plugin3.getState()).willReturn(PluginState.TEMPORARY);
        given(plugin3.getRequiredPlugins()).willReturn(disabledRequiredPlugins3);

        Set<PluginDependencyInformation> disabledRequiredPlugins4 = new HashSet<PluginDependencyInformation>();
        disabledRequiredPlugins4.add(dependencyInfo2);
        given(plugin4.getState()).willReturn(PluginState.TEMPORARY);
        given(plugin4.getRequiredPlugins()).willReturn(disabledRequiredPlugins4);

        given(pluginAccessor.getPlugin("testPlugin2")).willReturn(plugin2);
        given(pluginAccessor.getPlugin("testPlugin3")).willReturn(plugin3);
        given(pluginAccessor.getPlugin("testPlugin4")).willReturn(plugin4);

        // when
        PluginDependencyResult result = manager.getDependenciesToEnable(singletonList(plugin1));

        // then
        assertTrue(result.isCyclic());
        assertEquals(0, result.getDependenciesToEnable().size());
        assertEquals(0, result.getUnsatisfiedDependencies().size());
        assertEquals(0, result.getDependenciesToDisable().size());
    }

    @Test
    public void shouldReturnEmptyDependencyToDisableWhenNoDependenciesSpecifiedInOnePlugin() throws Exception {
        // given
        given(plugin1.getRequiredPlugins()).willReturn(Collections.<PluginDependencyInformation> emptySet());

        // when
        PluginDependencyResult result = manager.getDependenciesToDisable(singletonList(plugin1));

        // then
        assertEquals(0, result.getDependenciesToEnable().size());
        assertEquals(0, result.getUnsatisfiedDependencies().size());
        assertEquals(0, result.getDependenciesToDisable().size());
    }

    @Test
    public void shouldReturnSingleEnabledDependencyForOnePlugin() throws Exception {
        // given
        given(plugin2.getRequiredPlugins()).willReturn(Collections.singleton(dependencyInfo1));
        given(plugin2.getState()).willReturn(PluginState.ENABLED);

        Set<Plugin> plugins = new HashSet<Plugin>();
        plugins.add(plugin1);
        plugins.add(plugin2);
        plugins.add(plugin3);
        given(pluginAccessor.getPlugins()).willReturn(plugins);

        // when
        PluginDependencyResult result = manager.getDependenciesToDisable(singletonList(plugin1));

        // then
        assertEquals(0, result.getDependenciesToEnable().size());
        assertEquals(0, result.getUnsatisfiedDependencies().size());
        assertEquals(1, result.getDependenciesToDisable().size());
        assertTrue(result.getDependenciesToDisable().contains(new PluginDependencyInformation("testPlugin2")));
    }

    @Test
    public void shouldReturnMultipleEnabledDependencyForOnePlugin() throws Exception {
        // given
        given(plugin2.getRequiredPlugins()).willReturn(Collections.singleton(dependencyInfo1));
        given(plugin2.getState()).willReturn(PluginState.ENABLED);

        given(plugin3.getRequiredPlugins()).willReturn(Collections.singleton(dependencyInfo1));
        given(plugin3.getState()).willReturn(PluginState.ENABLED);

        given(plugin4.getRequiredPlugins()).willReturn(Collections.singleton(dependencyInfo1));
        given(plugin4.getState()).willReturn(PluginState.ENABLED);

        Set<Plugin> plugins = new HashSet<Plugin>();
        plugins.add(plugin1);
        plugins.add(plugin2);
        plugins.add(plugin3);
        plugins.add(plugin4);
        given(pluginAccessor.getPlugins()).willReturn(plugins);

        // when
        PluginDependencyResult result = manager.getDependenciesToDisable(singletonList(plugin1));

        // then
        assertEquals(0, result.getDependenciesToEnable().size());
        assertEquals(0, result.getUnsatisfiedDependencies().size());
        assertEquals(3, result.getDependenciesToDisable().size());
        assertTrue(result.getDependenciesToDisable().contains(new PluginDependencyInformation("testPlugin2")));
        assertTrue(result.getDependenciesToDisable().contains(new PluginDependencyInformation("testPlugin3")));
        assertTrue(result.getDependenciesToDisable().contains(new PluginDependencyInformation("testPlugin4")));
    }

    @Test
    public void shouldReturnEnabledDependenciesForOnePluginWhenSomeAreSatisfied() throws Exception {
        // given
        given(plugin2.getRequiredPlugins()).willReturn(Collections.singleton(dependencyInfo1));
        given(plugin2.getState()).willReturn(PluginState.DISABLED);

        given(plugin3.getRequiredPlugins()).willReturn(Collections.singleton(dependencyInfo1));
        given(plugin3.getState()).willReturn(PluginState.ENABLED);

        given(plugin4.getRequiredPlugins()).willReturn(Collections.singleton(dependencyInfo1));
        given(plugin4.getState()).willReturn(PluginState.TEMPORARY);

        Set<Plugin> plugins = new HashSet<Plugin>();
        plugins.add(plugin1);
        plugins.add(plugin2);
        plugins.add(plugin3);
        plugins.add(plugin4);
        given(pluginAccessor.getPlugins()).willReturn(plugins);

        // when
        PluginDependencyResult result = manager.getDependenciesToDisable(singletonList(plugin1));

        // then
        assertEquals(0, result.getDependenciesToEnable().size());
        assertEquals(0, result.getUnsatisfiedDependencies().size());
        assertEquals(1, result.getDependenciesToDisable().size());
        assertTrue(result.getDependenciesToDisable().contains(new PluginDependencyInformation("testPlugin3")));
    }

    @Test
    public void shouldReturnEnabledDependenciesForMultiplePlugins() throws Exception {
        // given
        given(plugin2.getRequiredPlugins()).willReturn(Collections.singleton(dependencyInfo1));
        given(plugin2.getState()).willReturn(PluginState.ENABLED);

        Set<Plugin> plugins = new HashSet<Plugin>();
        plugins.add(plugin1);
        plugins.add(plugin2);
        plugins.add(plugin3);
        plugins.add(plugin4);
        given(pluginAccessor.getPlugins()).willReturn(plugins);

        List<Plugin> argumentPlugins = new ArrayList<Plugin>();
        argumentPlugins.add(plugin1);
        argumentPlugins.add(plugin2);

        // when
        PluginDependencyResult result = manager.getDependenciesToDisable(argumentPlugins);

        // then
        assertEquals(0, result.getDependenciesToEnable().size());
        assertEquals(0, result.getUnsatisfiedDependencies().size());
        assertEquals(0, result.getDependenciesToDisable().size());
    }

    @Test
    public void shouldCheckDependenciesDependenciesAndReturnMultipleDependenciesForDisable() throws Exception {
        // given
        given(plugin2.getRequiredPlugins()).willReturn(Collections.singleton(dependencyInfo1));
        given(plugin2.getState()).willReturn(PluginState.ENABLED);

        given(plugin3.getRequiredPlugins()).willReturn(Collections.singleton(dependencyInfo2));
        given(plugin3.getState()).willReturn(PluginState.ENABLED);

        given(plugin4.getRequiredPlugins()).willReturn(Collections.singleton(dependencyInfo3));
        given(plugin4.getState()).willReturn(PluginState.ENABLED);

        Set<Plugin> plugins = new HashSet<Plugin>();
        plugins.add(plugin1);
        plugins.add(plugin2);
        plugins.add(plugin3);
        plugins.add(plugin4);
        given(pluginAccessor.getPlugins()).willReturn(plugins);

        // when
        PluginDependencyResult result = manager.getDependenciesToDisable(singletonList(plugin1));

        // then
        assertEquals(0, result.getDependenciesToEnable().size());
        assertEquals(0, result.getUnsatisfiedDependencies().size());
        assertEquals(3, result.getDependenciesToDisable().size());
        assertTrue(result.getDependenciesToDisable().contains(new PluginDependencyInformation("testPlugin2")));
        assertTrue(result.getDependenciesToDisable().contains(new PluginDependencyInformation("testPlugin3")));
        assertTrue(result.getDependenciesToDisable().contains(new PluginDependencyInformation("testPlugin4")));
    }

    @Test
    public void shouldCheckDependenciesDependenciesAndReturnMultipleDependenciesForDisableWhenSomePluginDisabled()
            throws Exception {
        // given
        given(plugin2.getRequiredPlugins()).willReturn(Collections.singleton(dependencyInfo1));
        given(plugin2.getState()).willReturn(PluginState.ENABLED);

        given(plugin3.getRequiredPlugins()).willReturn(Collections.singleton(dependencyInfo2));
        given(plugin3.getState()).willReturn(PluginState.DISABLED);

        given(plugin4.getRequiredPlugins()).willReturn(Collections.singleton(dependencyInfo3));
        given(plugin4.getState()).willReturn(PluginState.ENABLED);

        Set<Plugin> plugins = new HashSet<Plugin>();
        plugins.add(plugin1);
        plugins.add(plugin2);
        plugins.add(plugin3);
        plugins.add(plugin4);
        given(pluginAccessor.getPlugins()).willReturn(plugins);

        // when
        PluginDependencyResult result = manager.getDependenciesToDisable(singletonList(plugin1));

        // then
        assertEquals(0, result.getDependenciesToEnable().size());
        assertEquals(0, result.getUnsatisfiedDependencies().size());
        assertEquals(1, result.getDependenciesToDisable().size());
        assertTrue(result.getDependenciesToDisable().contains(new PluginDependencyInformation("testPlugin2")));
    }

    @Test
    public void shouldCheckDependenciesDependenciesAndReturnMultipleDependenciesForDisableWhenMultiplePlugins() throws Exception {
        // given
        given(plugin2.getRequiredPlugins()).willReturn(Collections.singleton(dependencyInfo1));
        given(plugin2.getState()).willReturn(PluginState.ENABLED);

        given(plugin3.getRequiredPlugins()).willReturn(Collections.singleton(dependencyInfo2));
        given(plugin3.getState()).willReturn(PluginState.ENABLED);

        given(plugin4.getRequiredPlugins()).willReturn(Collections.singleton(dependencyInfo3));
        given(plugin4.getState()).willReturn(PluginState.ENABLED);

        Set<Plugin> plugins = new HashSet<Plugin>();
        plugins.add(plugin1);
        plugins.add(plugin2);
        plugins.add(plugin3);
        plugins.add(plugin4);
        given(pluginAccessor.getPlugins()).willReturn(plugins);

        List<Plugin> argumentPlugins = new ArrayList<Plugin>();
        argumentPlugins.add(plugin1);
        argumentPlugins.add(plugin3);

        // when
        PluginDependencyResult result = manager.getDependenciesToDisable(argumentPlugins);

        // then
        assertEquals(0, result.getDependenciesToEnable().size());
        assertEquals(0, result.getUnsatisfiedDependencies().size());
        assertEquals(2, result.getDependenciesToDisable().size());
        assertTrue(result.getDependenciesToDisable().contains(new PluginDependencyInformation("testPlugin2")));
        assertTrue(result.getDependenciesToDisable().contains(new PluginDependencyInformation("testPlugin4")));
    }

    @Test
    public void shouldSortPluginsWithNoDependency() {
        // given
        given(pluginAccessor.getPlugin("testPlugin1")).willReturn(plugin1);
        given(pluginAccessor.getPlugin("testPlugin2")).willReturn(plugin2);
        given(pluginAccessor.getPlugin("testPlugin3")).willReturn(plugin3);
        given(pluginAccessor.getPlugin("testPlugin4")).willReturn(plugin4);

        List<Plugin> argumentPlugins = new ArrayList<Plugin>();
        argumentPlugins.add(plugin4);
        argumentPlugins.add(plugin1);
        argumentPlugins.add(plugin2);
        argumentPlugins.add(plugin3);

        // when
        List<Plugin> sortedPlugins = manager.sortPluginsInDependencyOrder(argumentPlugins);

        // then
        assertEquals(4, sortedPlugins.size());
    }

    @Test
    public void shouldSortPluginsWithOneDependency() {
        // given
        given(plugin1.getRequiredPlugins()).willReturn(Collections.singleton(dependencyInfo2));
        given(plugin2.getRequiredPlugins()).willReturn(Collections.singleton(dependencyInfo3));
        given(plugin3.getRequiredPlugins()).willReturn(Collections.singleton(dependencyInfo4));

        given(pluginAccessor.getPlugin("testPlugin1")).willReturn(plugin1);
        given(pluginAccessor.getPlugin("testPlugin2")).willReturn(plugin2);
        given(pluginAccessor.getPlugin("testPlugin3")).willReturn(plugin3);
        given(pluginAccessor.getPlugin("testPlugin4")).willReturn(plugin4);

        List<Plugin> argumentPlugins = new ArrayList<Plugin>();
        argumentPlugins.add(plugin4);
        argumentPlugins.add(plugin1);
        argumentPlugins.add(plugin2);
        argumentPlugins.add(plugin3);

        // when
        List<Plugin> sortedPlugins = manager.sortPluginsInDependencyOrder(argumentPlugins);

        // then
        assertEquals(4, sortedPlugins.size());
        assertEquals(plugin4, sortedPlugins.get(0));
        assertEquals(plugin3, sortedPlugins.get(1));
        assertEquals(plugin2, sortedPlugins.get(2));
        assertEquals(plugin1, sortedPlugins.get(3));
    }

    @Test
    public void shouldSortPluginsWithMultipleDependencies() {
        // given
        Set<PluginDependencyInformation> requiredPlugins1 = new HashSet<PluginDependencyInformation>();
        requiredPlugins1.add(dependencyInfo2);
        requiredPlugins1.add(dependencyInfo3);
        given(plugin1.getRequiredPlugins()).willReturn(requiredPlugins1);

        Set<PluginDependencyInformation> requiredPlugins2 = new HashSet<PluginDependencyInformation>();
        requiredPlugins2.add(dependencyInfo3);
        requiredPlugins2.add(dependencyInfo4);
        given(plugin2.getRequiredPlugins()).willReturn(requiredPlugins2);

        given(plugin3.getRequiredPlugins()).willReturn(Collections.singleton(dependencyInfo4));

        given(pluginAccessor.getPlugin("testPlugin1")).willReturn(plugin1);
        given(pluginAccessor.getPlugin("testPlugin2")).willReturn(plugin2);
        given(pluginAccessor.getPlugin("testPlugin3")).willReturn(plugin3);
        given(pluginAccessor.getPlugin("testPlugin4")).willReturn(plugin4);

        List<Plugin> argumentPlugins = new ArrayList<Plugin>();
        argumentPlugins.add(plugin1);
        argumentPlugins.add(plugin2);
        argumentPlugins.add(plugin3);
        argumentPlugins.add(plugin4);

        // when
        List<Plugin> sortedPlugins = manager.sortPluginsInDependencyOrder(argumentPlugins);

        // then
        assertEquals(4, sortedPlugins.size());
        assertEquals(plugin4, sortedPlugins.get(0));
        assertEquals(plugin3, sortedPlugins.get(1));
        assertEquals(plugin2, sortedPlugins.get(2));
        assertEquals(plugin1, sortedPlugins.get(3));
    }

    @Test
    public void shouldSortPluginsWithMissingDependencies() {
        // given
        Set<PluginDependencyInformation> rp1 = new HashSet<PluginDependencyInformation>();
        rp1.add(new PluginDependencyInformation("p2"));

        Set<PluginDependencyInformation> rp2 = new HashSet<PluginDependencyInformation>();
        rp2.add(new PluginDependencyInformation("p3"));

        Set<PluginDependencyInformation> rp3 = new HashSet<PluginDependencyInformation>();

        Set<PluginDependencyInformation> rp4 = new HashSet<PluginDependencyInformation>();
        rp4.add(new PluginDependencyInformation("p2"));
        rp4.add(new PluginDependencyInformation("p3"));

        Set<PluginDependencyInformation> rp5 = new HashSet<PluginDependencyInformation>();
        rp5.add(new PluginDependencyInformation("p1"));
        rp5.add(new PluginDependencyInformation("p3"));

        Plugin p1 = mock(Plugin.class, "p1");
        given(p1.getIdentifier()).willReturn("p1");
        given(p1.getVersion()).willReturn(new Version("1.1"));
        given(p1.getRequiredPlugins()).willReturn(rp1);

        Plugin p2 = mock(Plugin.class, "p2");
        given(p2.getIdentifier()).willReturn("p2");
        given(p2.getVersion()).willReturn(new Version("1.1"));
        given(p2.getRequiredPlugins()).willReturn(rp2);

        Plugin p3 = mock(Plugin.class, "p3");
        given(p3.getIdentifier()).willReturn("p3");
        given(p3.getVersion()).willReturn(new Version("1.1"));
        given(p3.getRequiredPlugins()).willReturn(rp3);

        Plugin p4 = mock(Plugin.class, "p4");
        given(p4.getIdentifier()).willReturn("p4");
        given(p4.getVersion()).willReturn(new Version("1.1"));
        given(p4.getRequiredPlugins()).willReturn(rp4);

        Plugin p5 = mock(Plugin.class, "p5");
        given(p5.getIdentifier()).willReturn("p5");
        given(p5.getVersion()).willReturn(new Version("1.1"));
        given(p5.getRequiredPlugins()).willReturn(rp5);

        given(pluginAccessor.getPlugin("p1")).willReturn(p1);
        given(pluginAccessor.getPlugin("p2")).willReturn(p2);
        given(pluginAccessor.getPlugin("p3")).willReturn(p3);
        given(pluginAccessor.getPlugin("p4")).willReturn(p4);
        given(pluginAccessor.getPlugin("p5")).willReturn(p5);

        List<Plugin> argumentPlugins = new ArrayList<Plugin>();
        argumentPlugins.add(p1);
        argumentPlugins.add(p2);
        argumentPlugins.add(p4);
        argumentPlugins.add(p5);

        // when
        List<Plugin> sortedPlugins = manager.sortPluginsInDependencyOrder(argumentPlugins);

        // then
        assertEquals(4, sortedPlugins.size());
        assertEquals(p2, sortedPlugins.get(0));
        assertEquals(p1, sortedPlugins.get(1));
        assertEquals(p5, sortedPlugins.get(2));
        assertEquals(p4, sortedPlugins.get(3));
    }

    @Test
    public void shouldCheckDependenciesDependenciesAndReturnMultipleDependenciesForUninstallWhenMultiplePlugins()
            throws Exception {
        // given
        given(plugin2.getRequiredPlugins()).willReturn(Collections.singleton(dependencyInfo1));
        given(plugin2.getState()).willReturn(PluginState.DISABLED);

        given(plugin3.getRequiredPlugins()).willReturn(Collections.singleton(dependencyInfo2));
        given(plugin3.getState()).willReturn(PluginState.ENABLED);

        given(plugin4.getRequiredPlugins()).willReturn(Collections.singleton(dependencyInfo3));
        given(plugin4.getState()).willReturn(PluginState.DISABLED);

        Set<Plugin> plugins = new HashSet<Plugin>();
        plugins.add(plugin1);
        plugins.add(plugin2);
        plugins.add(plugin3);
        plugins.add(plugin4);
        given(pluginAccessor.getPlugins()).willReturn(plugins);

        List<Plugin> argumentPlugins = new ArrayList<Plugin>();
        argumentPlugins.add(plugin1);
        argumentPlugins.add(plugin3);

        // when
        PluginDependencyResult result = manager.getDependenciesToUninstall(argumentPlugins);

        // then
        assertEquals(0, result.getDependenciesToEnable().size());
        assertEquals(0, result.getUnsatisfiedDependencies().size());
        assertEquals(0, result.getDependenciesToDisable().size());
        assertEquals(2, result.getDependenciesToUninstall().size());
        assertTrue(result.getDependenciesToUninstall().contains(new PluginDependencyInformation("testPlugin2")));
        assertTrue(result.getDependenciesToUninstall().contains(new PluginDependencyInformation("testPlugin4")));
    }

    @Test
    public void shouldReturnValidListToDisableWhenUpdate() throws Exception {
        // given
        Set<Plugin> plugins = new HashSet<Plugin>();
        plugins.add(plugin1);
        plugins.add(plugin2);
        plugins.add(plugin3);
        plugins.add(plugin4);
        given(pluginAccessor.getPlugins()).willReturn(plugins);

        given(plugin2.getRequiredPlugins()).willReturn(Collections.singleton(dependencyInfo1));
        given(plugin2.getState()).willReturn(PluginState.ENABLED);

        given(plugin3.getRequiredPlugins()).willReturn(Collections.singleton(dependencyInfo2));
        given(plugin3.getState()).willReturn(PluginState.DISABLED);

        // when
        PluginDependencyResult result = manager.getDependenciesToUpdate(plugin1, plugin4);

        // then
        assertEquals(0, result.getDependenciesToEnable().size());
        assertEquals(0, result.getUnsatisfiedDependencies().size());
        assertEquals(1, result.getDependenciesToDisable().size());
        assertEquals(0, result.getDependenciesToDisableUnsatisfiedAfterUpdate().size());
        assertEquals(0, result.getDependenciesToUninstall().size());
        assertTrue(result.getDependenciesToDisable().contains(new PluginDependencyInformation("testPlugin2")));
    }

    @Test
    public void shouldReturnValidListToDisableAndDisabeUnsatisfiedWhenUpdate() throws Exception {
        // given
        Set<Plugin> plugins = new HashSet<Plugin>();
        plugins.add(plugin1);
        plugins.add(plugin2);
        plugins.add(plugin3);
        plugins.add(plugin4);
        given(pluginAccessor.getPlugins()).willReturn(plugins);

        PluginDependencyInformation dependency = new PluginDependencyInformation("testPlugin1", new VersionOfDependency(
                "[1.0.0,2.0.0]"));
        given(plugin2.getRequiredPlugins()).willReturn(Collections.singleton(dependency));
        given(plugin2.getState()).willReturn(PluginState.ENABLED);

        given(plugin3.getRequiredPlugins()).willReturn(Collections.singleton(dependencyInfo2));
        given(plugin3.getState()).willReturn(PluginState.ENABLED);

        given(plugin1.getVersion()).willReturn(new Version("1.1.0"));
        given(plugin4.getVersion()).willReturn(new Version("2.1.0"));

        // when
        PluginDependencyResult result = manager.getDependenciesToUpdate(plugin1, plugin4);

        // then
        assertEquals(0, result.getDependenciesToEnable().size());
        assertEquals(0, result.getUnsatisfiedDependencies().size());
        assertEquals(2, result.getDependenciesToDisable().size());
        assertEquals(1, result.getDependenciesToDisableUnsatisfiedAfterUpdate().size());
        assertEquals(0, result.getDependenciesToUninstall().size());
        assertTrue(result.getDependenciesToDisable().contains(new PluginDependencyInformation("testPlugin2")));
        assertTrue(result.getDependenciesToDisable().contains(new PluginDependencyInformation("testPlugin3")));
        assertTrue(result.getDependenciesToDisableUnsatisfiedAfterUpdate().contains(
                new PluginDependencyInformation("testPlugin2")));
    }

    @Test
    public void shouldReturnValidListToDisableAndDisabeUnsatisfiedWhenUpdateAndDependentPluginIsDisabled() throws Exception {
        // given
        Set<Plugin> plugins = new HashSet<Plugin>();
        plugins.add(plugin1);
        plugins.add(plugin2);
        plugins.add(plugin3);
        plugins.add(plugin4);
        given(pluginAccessor.getPlugins()).willReturn(plugins);

        PluginDependencyInformation dependency = new PluginDependencyInformation("testPlugin1", new VersionOfDependency(
                "[1.0.0,2.0.0]"));
        given(plugin2.getRequiredPlugins()).willReturn(Collections.singleton(dependency));
        given(plugin2.getState()).willReturn(PluginState.DISABLED);

        given(plugin3.getRequiredPlugins()).willReturn(Collections.singleton(dependencyInfo2));
        given(plugin3.getState()).willReturn(PluginState.DISABLED);

        given(plugin1.getVersion()).willReturn(new Version("1.1.0"));
        given(plugin4.getVersion()).willReturn(new Version("2.1.0"));

        // when
        PluginDependencyResult result = manager.getDependenciesToUpdate(plugin1, plugin4);

        // then
        assertEquals(0, result.getDependenciesToEnable().size());
        assertEquals(0, result.getUnsatisfiedDependencies().size());
        assertEquals(0, result.getDependenciesToDisable().size());
        assertEquals(1, result.getDependenciesToDisableUnsatisfiedAfterUpdate().size());
        assertEquals(0, result.getDependenciesToUninstall().size());
        assertTrue(result.getDependenciesToDisableUnsatisfiedAfterUpdate().contains(
                new PluginDependencyInformation("testPlugin2")));
    }
}
