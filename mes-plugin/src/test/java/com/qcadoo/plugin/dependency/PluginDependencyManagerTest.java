package com.qcadoo.plugin.dependency;

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

import com.qcadoo.plugin.Plugin;
import com.qcadoo.plugin.PluginAccessor;
import com.qcadoo.plugin.PluginDependencyManager;
import com.qcadoo.plugin.PluginState;
import com.qcadoo.plugin.VersionUtils;

public class PluginDependencyManagerTest {

    PluginDependencyInformation dependencyInfo1 = new PluginDependencyInformation("testPlugin1");

    PluginDependencyInformation dependencyInfo2 = new PluginDependencyInformation("testPlugin2");

    PluginDependencyInformation dependencyInfo3 = new PluginDependencyInformation("testPlugin3");

    PluginDependencyInformation dependencyInfo4 = new PluginDependencyInformation("testPlugin4");

    Plugin plugin1;

    Plugin plugin2;

    Plugin plugin3;

    Plugin plugin4;

    PluginAccessor pluginAccessor = mock(PluginAccessor.class);

    PluginDependencyManager manager = new DefaultPluginDependencyManager(pluginAccessor);

    @Before
    public void init() {
        plugin1 = mock(Plugin.class, RETURNS_DEEP_STUBS);
        given(plugin1.getIdentifier()).willReturn("testPlugin1");
        given(plugin1.getPluginInformation().getVersion()).willReturn(VersionUtils.parse("1.1"));

        plugin2 = mock(Plugin.class, RETURNS_DEEP_STUBS);
        given(plugin2.getIdentifier()).willReturn("testPlugin2");
        given(plugin2.getPluginInformation().getVersion()).willReturn(VersionUtils.parse("1.1"));

        plugin3 = mock(Plugin.class, RETURNS_DEEP_STUBS);
        given(plugin3.getIdentifier()).willReturn("testPlugin3");
        given(plugin3.getPluginInformation().getVersion()).willReturn(VersionUtils.parse("1.1"));

        plugin4 = mock(Plugin.class, RETURNS_DEEP_STUBS);
        given(plugin4.getIdentifier()).willReturn("testPlugin4");
        given(plugin4.getPluginInformation().getVersion()).willReturn(VersionUtils.parse("1.1"));
    }

    @Test
    public void shouldReturnEmptyDependencyToEnableWhenNoDependenciesSpecifiedInOnePlugin() throws Exception {
        // given
        given(plugin1.getRequiredPlugins()).willReturn(Collections.<PluginDependencyInformation> emptySet());

        // when
        PluginDependencyResult result = manager.getDependenciesToEnable(singletonList(plugin1));

        // then
        assertFalse(result.isCyclic());
        assertEquals(0, result.getDisabledDependencies().size());
        assertEquals(0, result.getUnsatisfiedDependencies().size());
        assertEquals(0, result.getEnabledDependencies().size());
    }

    @Test
    public void shouldReturnSingleDisabledDependencyForOnePlugin() throws Exception {
        // given
        given(plugin2.getPluginState()).willReturn(PluginState.DISABLED);

        given(pluginAccessor.getPlugin("testPlugin2")).willReturn(plugin2);

        given(plugin1.getRequiredPlugins()).willReturn(Collections.singleton(dependencyInfo2));

        // when
        PluginDependencyResult result = manager.getDependenciesToEnable(singletonList(plugin1));

        // then
        assertFalse(result.isCyclic());
        assertEquals(1, result.getDisabledDependencies().size());
        assertTrue(result.getDisabledDependencies().contains(new PluginDependencyInformation("testPlugin2")));
        assertEquals(0, result.getUnsatisfiedDependencies().size());
        assertEquals(0, result.getEnabledDependencies().size());
    }

    @Test
    public void shouldReturnMultipleDisabledDependencyForOnePlugin() throws Exception {
        // given
        given(plugin1.getPluginState()).willReturn(PluginState.DISABLED);
        given(plugin2.getPluginState()).willReturn(PluginState.TEMPORARY);
        given(plugin3.getPluginState()).willReturn(PluginState.ENABLED);

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
        assertEquals(2, result.getDisabledDependencies().size());
        assertTrue(result.getDisabledDependencies().contains(new PluginDependencyInformation("testPlugin1")));
        assertTrue(result.getDisabledDependencies().contains(new PluginDependencyInformation("testPlugin2")));
        assertEquals(0, result.getUnsatisfiedDependencies().size());
        assertEquals(0, result.getEnabledDependencies().size());
    }

    @Test
    public void shouldReturnUnsatisfiedDependenciesForOnePluginWhenNoDependencyPluginFoundOrVersionIsNotMet() throws Exception {
        // given
        given(plugin1.getPluginState()).willReturn(PluginState.DISABLED);
        given(plugin2.getPluginState()).willReturn(PluginState.TEMPORARY);
        given(plugin3.getPluginState()).willReturn(PluginState.TEMPORARY);

        given(pluginAccessor.getPlugin("testPlugin1")).willReturn(plugin1);
        given(pluginAccessor.getPlugin("testPlugin2")).willReturn(plugin2);
        given(pluginAccessor.getPlugin("testPlugin3")).willReturn(plugin3);
        given(pluginAccessor.getPlugin("testPlugin4")).willReturn(null);

        dependencyInfo3 = new PluginDependencyInformation("testPlugin3", VersionUtils.parse("2"), true, null, false);

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
        assertEquals(0, result.getDisabledDependencies().size());
        assertEquals(2, result.getUnsatisfiedDependencies().size());
        assertTrue(result.getUnsatisfiedDependencies().contains(
                new PluginDependencyInformation("testPlugin3", VersionUtils.parse("2"), true, null, false)));
        assertTrue(result.getUnsatisfiedDependencies().contains(new PluginDependencyInformation("testPlugin4")));
        assertEquals(0, result.getEnabledDependencies().size());
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

        given(plugin3.getPluginState()).willReturn(PluginState.TEMPORARY);

        given(plugin4.getPluginState()).willReturn(PluginState.TEMPORARY);

        given(pluginAccessor.getPlugin("testPlugin3")).willReturn(plugin3);
        given(pluginAccessor.getPlugin("testPlugin4")).willReturn(plugin4);

        List<Plugin> plugins = new ArrayList<Plugin>();
        plugins.add(plugin1);
        plugins.add(plugin2);

        // when
        PluginDependencyResult result = manager.getDependenciesToEnable(plugins);

        // then
        assertFalse(result.isCyclic());
        assertEquals(2, result.getDisabledDependencies().size());
        assertTrue(result.getDisabledDependencies().contains(new PluginDependencyInformation("testPlugin3")));
        assertTrue(result.getDisabledDependencies().contains(new PluginDependencyInformation("testPlugin4")));
        assertEquals(0, result.getUnsatisfiedDependencies().size());
        assertEquals(0, result.getEnabledDependencies().size());
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

        given(plugin3.getPluginState()).willReturn(PluginState.TEMPORARY);

        given(plugin4.getPluginState()).willReturn(PluginState.TEMPORARY);

        given(pluginAccessor.getPlugin("testPlugin3")).willReturn(plugin3);

        List<Plugin> plugins = new ArrayList<Plugin>();
        plugins.add(plugin1);
        plugins.add(plugin2);

        // when
        PluginDependencyResult result = manager.getDependenciesToEnable(plugins);

        // then
        assertFalse(result.isCyclic());
        assertEquals(0, result.getDisabledDependencies().size());
        assertEquals(1, result.getUnsatisfiedDependencies().size());
        assertTrue(result.getUnsatisfiedDependencies().contains(new PluginDependencyInformation("testPlugin4")));
        assertEquals(0, result.getEnabledDependencies().size());
    }

    @Test
    public void shouldCheckDependenciesDependenciesAndReturnMultipleDependencies() throws Exception {
        // given
        Set<PluginDependencyInformation> disabledRequiredPlugins = new HashSet<PluginDependencyInformation>();
        disabledRequiredPlugins.add(dependencyInfo2);
        given(plugin1.getRequiredPlugins()).willReturn(disabledRequiredPlugins);

        given(plugin2.getPluginState()).willReturn(PluginState.DISABLED);
        Set<PluginDependencyInformation> disabledRequiredPlugins2 = new HashSet<PluginDependencyInformation>();
        disabledRequiredPlugins2.add(dependencyInfo3);
        given(plugin2.getRequiredPlugins()).willReturn(disabledRequiredPlugins2);

        given(plugin3.getPluginState()).willReturn(PluginState.TEMPORARY);

        given(pluginAccessor.getPlugin("testPlugin2")).willReturn(plugin2);
        given(pluginAccessor.getPlugin("testPlugin3")).willReturn(plugin3);

        // when
        PluginDependencyResult result = manager.getDependenciesToEnable(singletonList(plugin1));

        // then
        assertFalse(result.isCyclic());
        assertEquals(2, result.getDisabledDependencies().size());
        assertTrue(result.getDisabledDependencies().contains(new PluginDependencyInformation("testPlugin2")));
        assertTrue(result.getDisabledDependencies().contains(new PluginDependencyInformation("testPlugin3")));
        assertEquals(0, result.getUnsatisfiedDependencies().size());
        assertEquals(0, result.getEnabledDependencies().size());
    }

    @Test
    public void shouldReturnDisabledDependenciesForMultiplePluginsHardExample() throws Exception {
        // given

        given(plugin1.getRequiredPlugins()).willReturn(Collections.singleton(dependencyInfo2));
        given(plugin2.getRequiredPlugins()).willReturn(Collections.singleton(dependencyInfo3));
        given(plugin3.getRequiredPlugins()).willReturn(Collections.singleton(dependencyInfo4));

        given(plugin2.getPluginState()).willReturn(PluginState.TEMPORARY);
        given(plugin3.getPluginState()).willReturn(PluginState.TEMPORARY);
        given(plugin4.getPluginState()).willReturn(PluginState.TEMPORARY);

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
        assertEquals(2, result.getDisabledDependencies().size());
        assertTrue(result.getDisabledDependencies().contains(new PluginDependencyInformation("testPlugin2")));
        assertTrue(result.getDisabledDependencies().contains(new PluginDependencyInformation("testPlugin4")));
        assertEquals(0, result.getUnsatisfiedDependencies().size());
        assertEquals(0, result.getEnabledDependencies().size());
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
        assertEquals(0, result.getDisabledDependencies().size());
        assertEquals(0, result.getUnsatisfiedDependencies().size());
        assertEquals(0, result.getEnabledDependencies().size());
    }

    @Test
    public void shouldSetCyclicFlagWhenCyclicDependenciesDependencies() throws Exception {
        // given
        Set<PluginDependencyInformation> disabledRequiredPlugins1 = new HashSet<PluginDependencyInformation>();
        disabledRequiredPlugins1.add(dependencyInfo2);
        given(plugin1.getRequiredPlugins()).willReturn(disabledRequiredPlugins1);

        Set<PluginDependencyInformation> disabledRequiredPlugins2 = new HashSet<PluginDependencyInformation>();
        disabledRequiredPlugins2.add(dependencyInfo3);
        given(plugin2.getPluginState()).willReturn(PluginState.DISABLED);
        given(plugin2.getRequiredPlugins()).willReturn(disabledRequiredPlugins2);

        Set<PluginDependencyInformation> disabledRequiredPlugins3 = new HashSet<PluginDependencyInformation>();
        disabledRequiredPlugins3.add(dependencyInfo1);
        given(plugin3.getPluginState()).willReturn(PluginState.TEMPORARY);
        given(plugin3.getRequiredPlugins()).willReturn(disabledRequiredPlugins3);

        given(pluginAccessor.getPlugin("testPlugin2")).willReturn(plugin2);
        given(pluginAccessor.getPlugin("testPlugin3")).willReturn(plugin3);

        // when
        PluginDependencyResult result = manager.getDependenciesToEnable(singletonList(plugin1));

        // then
        assertTrue(result.isCyclic());
        assertEquals(0, result.getDisabledDependencies().size());
        assertEquals(0, result.getUnsatisfiedDependencies().size());
        assertEquals(0, result.getEnabledDependencies().size());
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
        given(plugin2.getPluginState()).willReturn(PluginState.DISABLED);
        given(plugin2.getRequiredPlugins()).willReturn(disabledRequiredPlugins2);

        Set<PluginDependencyInformation> disabledRequiredPlugins3 = new HashSet<PluginDependencyInformation>();
        disabledRequiredPlugins3.add(dependencyInfo2);
        given(plugin3.getPluginState()).willReturn(PluginState.TEMPORARY);
        given(plugin3.getRequiredPlugins()).willReturn(disabledRequiredPlugins3);

        given(pluginAccessor.getPlugin("testPlugin2")).willReturn(plugin2);
        given(pluginAccessor.getPlugin("testPlugin3")).willReturn(plugin3);

        // when
        PluginDependencyResult result = manager.getDependenciesToEnable(singletonList(plugin1));

        // then
        assertTrue(result.isCyclic());
        assertEquals(0, result.getDisabledDependencies().size());
        assertEquals(0, result.getUnsatisfiedDependencies().size());
        assertEquals(0, result.getEnabledDependencies().size());
    }

    @Test
    public void shouldSetCyclicFlagWhenCyclicDependenciesDependenciesToArgumentsDependency() throws Exception {
        // given
        Set<PluginDependencyInformation> disabledRequiredPlugins1 = new HashSet<PluginDependencyInformation>();
        disabledRequiredPlugins1.add(dependencyInfo2);
        given(plugin1.getRequiredPlugins()).willReturn(disabledRequiredPlugins1);

        Set<PluginDependencyInformation> disabledRequiredPlugins2 = new HashSet<PluginDependencyInformation>();
        disabledRequiredPlugins2.add(dependencyInfo3);
        given(plugin2.getPluginState()).willReturn(PluginState.DISABLED);
        given(plugin2.getRequiredPlugins()).willReturn(disabledRequiredPlugins2);

        Set<PluginDependencyInformation> disabledRequiredPlugins3 = new HashSet<PluginDependencyInformation>();
        disabledRequiredPlugins3.add(dependencyInfo4);
        given(plugin3.getPluginState()).willReturn(PluginState.TEMPORARY);
        given(plugin3.getRequiredPlugins()).willReturn(disabledRequiredPlugins3);

        Set<PluginDependencyInformation> disabledRequiredPlugins4 = new HashSet<PluginDependencyInformation>();
        disabledRequiredPlugins4.add(dependencyInfo2);
        given(plugin4.getPluginState()).willReturn(PluginState.TEMPORARY);
        given(plugin4.getRequiredPlugins()).willReturn(disabledRequiredPlugins4);

        given(pluginAccessor.getPlugin("testPlugin2")).willReturn(plugin2);
        given(pluginAccessor.getPlugin("testPlugin3")).willReturn(plugin3);
        given(pluginAccessor.getPlugin("testPlugin4")).willReturn(plugin4);

        // when
        PluginDependencyResult result = manager.getDependenciesToEnable(singletonList(plugin1));

        // then
        assertTrue(result.isCyclic());
        assertEquals(0, result.getDisabledDependencies().size());
        assertEquals(0, result.getUnsatisfiedDependencies().size());
        assertEquals(0, result.getEnabledDependencies().size());
    }

    @Test
    public void shouldReturnEmptyDependencyToDisableWhenNoDependenciesSpecifiedInOnePlugin() throws Exception {
        // given
        given(plugin1.getRequiredPlugins()).willReturn(Collections.<PluginDependencyInformation> emptySet());

        // when
        PluginDependencyResult result = manager.getDependenciesToDisable(singletonList(plugin1));

        // then
        assertEquals(0, result.getDisabledDependencies().size());
        assertEquals(0, result.getUnsatisfiedDependencies().size());
        assertEquals(0, result.getEnabledDependencies().size());
    }

    @Test
    public void shouldReturnSingleEnabledDependencyForOnePlugin() throws Exception {
        // given
        given(plugin2.getRequiredPlugins()).willReturn(Collections.singleton(dependencyInfo1));
        given(plugin2.getPluginState()).willReturn(PluginState.ENABLED);

        Set<Plugin> plugins = new HashSet<Plugin>();
        plugins.add(plugin1);
        plugins.add(plugin2);
        plugins.add(plugin3);
        given(pluginAccessor.getPlugins()).willReturn(plugins);

        // when
        PluginDependencyResult result = manager.getDependenciesToDisable(singletonList(plugin1));

        // then
        assertEquals(0, result.getDisabledDependencies().size());
        assertEquals(0, result.getUnsatisfiedDependencies().size());
        assertEquals(1, result.getEnabledDependencies().size());
        assertTrue(result.getEnabledDependencies().contains(new PluginDependencyInformation("testPlugin2")));
    }

    @Test
    public void shouldReturnMultipleEnabledDependencyForOnePlugin() throws Exception {
        // given
        given(plugin2.getRequiredPlugins()).willReturn(Collections.singleton(dependencyInfo1));
        given(plugin2.getPluginState()).willReturn(PluginState.ENABLED);

        given(plugin3.getRequiredPlugins()).willReturn(Collections.singleton(dependencyInfo1));
        given(plugin3.getPluginState()).willReturn(PluginState.ENABLED);

        given(plugin4.getRequiredPlugins()).willReturn(Collections.singleton(dependencyInfo1));
        given(plugin4.getPluginState()).willReturn(PluginState.ENABLED);

        Set<Plugin> plugins = new HashSet<Plugin>();
        plugins.add(plugin1);
        plugins.add(plugin2);
        plugins.add(plugin3);
        plugins.add(plugin4);
        given(pluginAccessor.getPlugins()).willReturn(plugins);

        // when
        PluginDependencyResult result = manager.getDependenciesToDisable(singletonList(plugin1));

        // then
        assertEquals(0, result.getDisabledDependencies().size());
        assertEquals(0, result.getUnsatisfiedDependencies().size());
        assertEquals(3, result.getEnabledDependencies().size());
        assertTrue(result.getEnabledDependencies().contains(new PluginDependencyInformation("testPlugin2")));
        assertTrue(result.getEnabledDependencies().contains(new PluginDependencyInformation("testPlugin3")));
        assertTrue(result.getEnabledDependencies().contains(new PluginDependencyInformation("testPlugin4")));
    }

    @Test
    public void shouldReturnEnabledDependenciesForOnePluginWhenSomeAreSatisfied() throws Exception {
        // given
        given(plugin2.getRequiredPlugins()).willReturn(Collections.singleton(dependencyInfo1));
        given(plugin2.getPluginState()).willReturn(PluginState.DISABLED);

        given(plugin3.getRequiredPlugins()).willReturn(Collections.singleton(dependencyInfo1));
        given(plugin3.getPluginState()).willReturn(PluginState.ENABLED);

        given(plugin4.getRequiredPlugins()).willReturn(Collections.singleton(dependencyInfo1));
        given(plugin4.getPluginState()).willReturn(PluginState.TEMPORARY);

        Set<Plugin> plugins = new HashSet<Plugin>();
        plugins.add(plugin1);
        plugins.add(plugin2);
        plugins.add(plugin3);
        plugins.add(plugin4);
        given(pluginAccessor.getPlugins()).willReturn(plugins);

        // when
        PluginDependencyResult result = manager.getDependenciesToDisable(singletonList(plugin1));

        // then
        assertEquals(0, result.getDisabledDependencies().size());
        assertEquals(0, result.getUnsatisfiedDependencies().size());
        assertEquals(1, result.getEnabledDependencies().size());
        assertTrue(result.getEnabledDependencies().contains(new PluginDependencyInformation("testPlugin3")));
    }

    @Test
    public void shouldReturnEnabledDependenciesForMultiplePlugins() throws Exception {
        // given
        given(plugin2.getRequiredPlugins()).willReturn(Collections.singleton(dependencyInfo1));
        given(plugin2.getPluginState()).willReturn(PluginState.ENABLED);

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
        assertEquals(0, result.getDisabledDependencies().size());
        assertEquals(0, result.getUnsatisfiedDependencies().size());
        assertEquals(0, result.getEnabledDependencies().size());
    }

    @Test
    public void shouldCheckDependenciesDependenciesAndReturnMultipleDependenciesForDisable() throws Exception {
        // given
        given(plugin2.getRequiredPlugins()).willReturn(Collections.singleton(dependencyInfo1));
        given(plugin2.getPluginState()).willReturn(PluginState.ENABLED);

        given(plugin3.getRequiredPlugins()).willReturn(Collections.singleton(dependencyInfo2));
        given(plugin3.getPluginState()).willReturn(PluginState.ENABLED);

        given(plugin4.getRequiredPlugins()).willReturn(Collections.singleton(dependencyInfo3));
        given(plugin4.getPluginState()).willReturn(PluginState.ENABLED);

        Set<Plugin> plugins = new HashSet<Plugin>();
        plugins.add(plugin1);
        plugins.add(plugin2);
        plugins.add(plugin3);
        plugins.add(plugin4);
        given(pluginAccessor.getPlugins()).willReturn(plugins);

        // when
        PluginDependencyResult result = manager.getDependenciesToDisable(singletonList(plugin1));

        // then
        assertEquals(0, result.getDisabledDependencies().size());
        assertEquals(0, result.getUnsatisfiedDependencies().size());
        assertEquals(3, result.getEnabledDependencies().size());
        assertTrue(result.getEnabledDependencies().contains(new PluginDependencyInformation("testPlugin2")));
        assertTrue(result.getEnabledDependencies().contains(new PluginDependencyInformation("testPlugin3")));
        assertTrue(result.getEnabledDependencies().contains(new PluginDependencyInformation("testPlugin4")));
    }

    @Test
    public void shouldCheckDependenciesDependenciesAndReturnMultipleDependenciesForDisableWhenSomePluginDisabled()
            throws Exception {
        // given
        given(plugin2.getRequiredPlugins()).willReturn(Collections.singleton(dependencyInfo1));
        given(plugin2.getPluginState()).willReturn(PluginState.ENABLED);

        given(plugin3.getRequiredPlugins()).willReturn(Collections.singleton(dependencyInfo2));
        given(plugin3.getPluginState()).willReturn(PluginState.DISABLED);

        given(plugin4.getRequiredPlugins()).willReturn(Collections.singleton(dependencyInfo3));
        given(plugin4.getPluginState()).willReturn(PluginState.ENABLED);

        Set<Plugin> plugins = new HashSet<Plugin>();
        plugins.add(plugin1);
        plugins.add(plugin2);
        plugins.add(plugin3);
        plugins.add(plugin4);
        given(pluginAccessor.getPlugins()).willReturn(plugins);

        // when
        PluginDependencyResult result = manager.getDependenciesToDisable(singletonList(plugin1));

        // then
        assertEquals(0, result.getDisabledDependencies().size());
        assertEquals(0, result.getUnsatisfiedDependencies().size());
        assertEquals(1, result.getEnabledDependencies().size());
        assertTrue(result.getEnabledDependencies().contains(new PluginDependencyInformation("testPlugin2")));
    }

    @Test
    public void shouldCheckDependenciesDependenciesAndReturnMultipleDependenciesForDisableWhenMultiplePlugins() throws Exception {
        // given
        given(plugin2.getRequiredPlugins()).willReturn(Collections.singleton(dependencyInfo1));
        given(plugin2.getPluginState()).willReturn(PluginState.ENABLED);

        given(plugin3.getRequiredPlugins()).willReturn(Collections.singleton(dependencyInfo2));
        given(plugin3.getPluginState()).willReturn(PluginState.ENABLED);

        given(plugin4.getRequiredPlugins()).willReturn(Collections.singleton(dependencyInfo3));
        given(plugin4.getPluginState()).willReturn(PluginState.ENABLED);

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
        assertEquals(0, result.getDisabledDependencies().size());
        assertEquals(0, result.getUnsatisfiedDependencies().size());
        assertEquals(2, result.getEnabledDependencies().size());
        assertTrue(result.getEnabledDependencies().contains(new PluginDependencyInformation("testPlugin2")));
        assertTrue(result.getEnabledDependencies().contains(new PluginDependencyInformation("testPlugin4")));
    }

    @Test
    public void shouldSortPlugins() {
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
}
