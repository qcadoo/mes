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
        given(plugin1.getName()).willReturn("testPlugin1");
        given(plugin1.getPluginInformation().getVersion()).willReturn("1.1");

        plugin2 = mock(Plugin.class, RETURNS_DEEP_STUBS);
        given(plugin2.getName()).willReturn("testPlugin2");
        given(plugin2.getPluginInformation().getVersion()).willReturn("1.1");

        plugin3 = mock(Plugin.class, RETURNS_DEEP_STUBS);
        given(plugin3.getName()).willReturn("testPlugin3");
        given(plugin3.getPluginInformation().getVersion()).willReturn("1.1");

        plugin4 = mock(Plugin.class, RETURNS_DEEP_STUBS);
        given(plugin4.getName()).willReturn("testPlugin4");
        given(plugin4.getPluginInformation().getVersion()).willReturn("1.1");
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

        dependencyInfo3 = new PluginDependencyInformation("testPlugin3", "2", true, null, false);

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
                new PluginDependencyInformation("testPlugin3", "2", true, null, false)));
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

    //
    // @Test
    // public void shouldReturnEmptyDependencyToDisableWhenNoDependenciesSpecifiedInOnePlugin() throws Exception {
    // // given
    // Plugin p = mock(Plugin.class);
    // given(p.getRequiredPlugins()).willReturn(Collections.<PluginInformation> emptySet());
    //
    // PluginDependencyManager m = new DefaultPluginDependencyManager(pluginAccessor);
    //
    // // when
    // PluginDependencyResult result = m.getDependenciesToDisable(singletonList(p));
    //
    // // then
    // assertEquals(0, result.getDisabledDependencies().size());
    // assertEquals(0, result.getUnsatisfiedDependencies().size());
    // assertEquals(0, result.getEnabledDependencies().size());
    // }
    //
    // @Test
    // public void shouldReturnSingleEnabledDependencyForOnePlugin() throws Exception {
    // // given
    // Plugin p = mock(Plugin.class);
    // Set<PluginInformation> disabledRequiredPlugins = Collections.singleton(dependencyPluginInformation1);
    // given(p.getRequiredPlugins()).willReturn(disabledRequiredPlugins);
    //
    // PluginDependencyManager m = new DefaultPluginDependencyManager(pluginAccessor);
    //
    // given(dependencyPlugin1.getPluginState()).willReturn(PluginState.ENABLED);
    //
    // // when
    // PluginDependencyResult result = m.getDependenciesToDisable(singletonList(p));
    //
    // // then
    // assertEquals(0, result.getDisabledDependencies().size());
    // assertEquals(0, result.getUnsatisfiedDependencies().size());
    // assertEquals(1, result.getEnabledDependencies().size());
    // assertEquals("defaultPlugin", result.getEnabledDependencies().get(0).getName());
    // assertEquals("defaultPlugin_vendor", result.getEnabledDependencies().get(0).getVendor());
    // assertEquals("defaultPlugin_version", result.getEnabledDependencies().get(0).getVersion());
    // }
    //
    // @Test
    // public void shouldReturnMultipleEnabledDependencyForOnePlugin() throws Exception {
    // // given
    // Plugin p = mock(Plugin.class);
    // Set<PluginInformation> disabledRequiredPlugins = new HashSet<PluginInformation>();
    // disabledRequiredPlugins.add(dependencyPluginInformation1);
    // disabledRequiredPlugins.add(dependencyPluginInformation2);
    // disabledRequiredPlugins.add(dependencyPluginInformation3);
    // given(p.getRequiredPlugins()).willReturn(disabledRequiredPlugins);
    //
    // PluginDependencyManager m = new DefaultPluginDependencyManager(pluginAccessor);
    //
    // given(dependencyPlugin1.getPluginState()).willReturn(PluginState.ENABLED);
    // given(dependencyPlugin2.getPluginState()).willReturn(PluginState.ENABLED);
    // given(dependencyPlugin3.getPluginState()).willReturn(PluginState.ENABLED);
    //
    // // when
    // PluginDependencyResult result = m.getDependenciesToDisable(singletonList(p));
    //
    // // then
    // assertEquals(3, result.getEnabledDependencies().size());
    //
    // for (PluginInformation information : result.getEnabledDependencies()) {
    // assertNotNull(dependencyPluginInformations.get(information.getName()));
    //
    // assertEquals(information.getName(), dependencyPluginInformations.get(information.getName()).getName());
    // assertEquals(information.getName() + "_vendor", dependencyPluginInformations.get(information.getName()).getVendor());
    // assertEquals(information.getName() + "_version", dependencyPluginInformations.get(information.getName()).getVersion());
    //
    // dependencyPluginInformations.remove(information.getName());
    // }
    //
    // assertEquals(0, result.getUnsatisfiedDependencies().size());
    // assertEquals(0, result.getDisabledDependencies().size());
    // }
    //
    // @Test
    // public void shouldReturnEnabledDependenciesForOnePluginWhenSomeAreSatisfied() throws Exception {
    // // given
    // Plugin p = mock(Plugin.class);
    // Set<PluginInformation> disabledRequiredPlugins = new HashSet<PluginInformation>();
    // disabledRequiredPlugins.add(dependencyPluginInformation1);
    // disabledRequiredPlugins.add(dependencyPluginInformation2);
    // disabledRequiredPlugins.add(dependencyPluginInformation3);
    // given(p.getRequiredPlugins()).willReturn(disabledRequiredPlugins);
    //
    // PluginDependencyManager m = new DefaultPluginDependencyManager(pluginAccessor);
    //
    // given(dependencyPlugin1.getPluginState()).willReturn(PluginState.DISABLED);
    // given(dependencyPlugin2.getPluginState()).willReturn(PluginState.ENABLED);
    // given(dependencyPlugin3.getPluginState()).willReturn(PluginState.TEMPORARY);
    //
    // // when
    // PluginDependencyResult result = m.getDependenciesToDisable(singletonList(p));
    //
    // // then
    // assertEquals(0, result.getDisabledDependencies().size());
    // assertEquals(0, result.getUnsatisfiedDependencies().size());
    // assertEquals(1, result.getEnabledDependencies().size());
    // assertEquals("defaultPlugin2", result.getEnabledDependencies().get(0).getName());
    // assertEquals("defaultPlugin2_vendor", result.getEnabledDependencies().get(0).getVendor());
    // assertEquals("defaultPlugin2_version", result.getEnabledDependencies().get(0).getVersion());
    // }
    //
    // @Test
    // public void shouldReturnEnabledDependenciesForMultiplePlugins() throws Exception {
    // // given
    // Plugin p = mock(Plugin.class);
    // Set<PluginInformation> disabledRequiredPlugins = new HashSet<PluginInformation>();
    // disabledRequiredPlugins.add(dependencyPluginInformation1);
    // disabledRequiredPlugins.add(dependencyPluginInformation2);
    // given(p.getRequiredPlugins()).willReturn(disabledRequiredPlugins);
    //
    // Plugin p2 = mock(Plugin.class);
    // Set<PluginInformation> disabledRequiredPlugins2 = new HashSet<PluginInformation>();
    // disabledRequiredPlugins2.add(dependencyPluginInformation2_2);
    // disabledRequiredPlugins2.add(dependencyPluginInformation3);
    // given(p2.getRequiredPlugins()).willReturn(disabledRequiredPlugins2);
    //
    // Plugin p3 = mock(Plugin.class);
    // Set<PluginInformation> disabledRequiredPlugins3 = new HashSet<PluginInformation>();
    // disabledRequiredPlugins3.add(dependencyPluginInformation1_2);
    // disabledRequiredPlugins3.add(dependencyPluginInformation3_2);
    // given(p3.getRequiredPlugins()).willReturn(disabledRequiredPlugins3);
    //
    // PluginDependencyManager m = new DefaultPluginDependencyManager(pluginAccessor);
    //
    // given(dependencyPlugin1.getPluginState()).willReturn(PluginState.ENABLED);
    // given(dependencyPlugin2.getPluginState()).willReturn(PluginState.ENABLED);
    // given(dependencyPlugin3.getPluginState()).willReturn(PluginState.TEMPORARY);
    //
    // List<Plugin> plugins = new ArrayList<Plugin>();
    // plugins.add(p);
    // plugins.add(p2);
    // plugins.add(p3);
    //
    // // when
    // PluginDependencyResult result = m.getDependenciesToDisable(plugins);
    //
    // // then
    // assertEquals(0, result.getDisabledDependencies().size());
    // assertEquals(0, result.getUnsatisfiedDependencies().size());
    // assertEquals(2, result.getEnabledDependencies().size());
    // dependencyPluginInformations.remove(dependencyPlugin3.getPluginInformation().getName());
    // for (PluginInformation information : result.getEnabledDependencies()) {
    // assertNotNull(dependencyPluginInformations.get(information.getName()));
    //
    // assertEquals(information.getName(), dependencyPluginInformations.get(information.getName()).getName());
    // assertEquals(information.getName() + "_vendor", dependencyPluginInformations.get(information.getName()).getVendor());
    // assertEquals(information.getName() + "_version", dependencyPluginInformations.get(information.getName()).getVersion());
    //
    // dependencyPluginInformations.remove(information.getName());
    // }
    // }
    //

    //
    // @Test
    // public void shouldCheckDependenciesDependenciesAndReturnMultipleDependenciesForDisable() throws Exception {
    // // given
    // Plugin p = mock(Plugin.class);
    // given(p.getPluginInformation()).willReturn(dependencyPluginInformation1);
    // Set<PluginInformation> disabledRequiredPlugins = new HashSet<PluginInformation>();
    // disabledRequiredPlugins.add(dependencyPluginInformation2);
    // given(p.getRequiredPlugins()).willReturn(disabledRequiredPlugins);
    //
    // Plugin p2 = mock(Plugin.class);
    // given(p2.getPluginInformation()).willReturn(dependencyPluginInformation2);
    // Set<PluginInformation> disabledRequiredPlugins2 = new HashSet<PluginInformation>();
    // disabledRequiredPlugins2.add(dependencyPluginInformation3);
    // given(dependencyPlugin2.getRequiredPlugins()).willReturn(disabledRequiredPlugins2);
    //
    // given(dependencyPlugin1.getPluginState()).willReturn(PluginState.ENABLED);
    // given(dependencyPlugin2.getPluginState()).willReturn(PluginState.ENABLED);
    // given(dependencyPlugin3.getPluginState()).willReturn(PluginState.ENABLED);
    //
    // PluginDependencyManager m = new DefaultPluginDependencyManager(pluginAccessor);
    //
    // // when
    // PluginDependencyResult result = m.getDependenciesToDisable(singletonList(p));
    //
    // // then
    // assertEquals(2, result.getEnabledDependencies().size());
    //
    // for (PluginInformation information : result.getDisabledDependencies()) {
    // assertNotNull(dependencyPluginInformations.get(information.getName()));
    //
    // assertEquals(information.getName(), dependencyPluginInformations.get(information.getName()).getName());
    // assertEquals(information.getName() + "_vendor", dependencyPluginInformations.get(information.getName()).getVendor());
    // assertEquals(information.getName() + "_version", dependencyPluginInformations.get(information.getName()).getVersion());
    //
    // dependencyPluginInformations.remove(information.getName());
    // }
    //
    // assertEquals(0, result.getUnsatisfiedDependencies().size());
    // assertEquals(0, result.getDisabledDependencies().size());
    // }
    //
    // @Test
    // public void shouldSetCyclicFlagOnCyclicDependencies() throws Exception {
    // // given
    // Plugin p = mock(Plugin.class);
    // given(p.getPluginInformation()).willReturn(dependencyPluginInformation1);
    // Set<PluginInformation> disabledRequiredPlugins = new HashSet<PluginInformation>();
    // disabledRequiredPlugins.add(dependencyPluginInformation2);
    // given(p.getRequiredPlugins()).willReturn(disabledRequiredPlugins);
    //
    // Plugin p2 = mock(Plugin.class);
    // given(p2.getPluginInformation()).willReturn(dependencyPluginInformation2);
    // Set<PluginInformation> disabledRequiredPlugins2 = new HashSet<PluginInformation>();
    // disabledRequiredPlugins2.add(dependencyPluginInformation1);
    // given(p2.getRequiredPlugins()).willReturn(disabledRequiredPlugins2);
    //
    // PluginDependencyManager m = new DefaultPluginDependencyManager(pluginAccessor);
    //
    // given(dependencyPlugin1.getPluginState()).willReturn(PluginState.TEMPORARY);
    // given(dependencyPlugin2.getPluginState()).willReturn(PluginState.TEMPORARY);
    //
    // List<Plugin> plugins = new ArrayList<Plugin>();
    // plugins.add(p);
    // plugins.add(p2);
    //
    // // when
    // PluginDependencyResult result = m.getDependenciesToEnable(plugins);
    //
    // // then
    // assertTrue(result.isCyclic());
    // }
    //

}
