package com.qcadoo.plugin;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.qcadoo.plugin.manager.DefaultPluginDependencyManager;

public class PluginDependencyManagerTest {

    private Map<String, PluginInformation> dependencyPluginInformations;

    private Plugin dependencyPlugin1 = mock(Plugin.class);

    private PluginInformation dependencyPluginInformation1 = new PluginInformation("defaultPlugin", "", "defaultPlugin_vendor",
            "", "defaultPlugin_version", "");;

    private PluginInformation dependencyPluginInformation1_2 = new PluginInformation("defaultPlugin", "", "defaultPlugin_vendor",
            "", "defaultPlugin_version", "");

    private Plugin dependencyPlugin2 = mock(Plugin.class);

    private PluginInformation dependencyPluginInformation2 = new PluginInformation("defaultPlugin2", "", "defaultPlugin2_vendor",
            "", "defaultPlugin2_version", "");

    private PluginInformation dependencyPluginInformation2_2 = new PluginInformation("defaultPlugin2", "",
            "defaultPlugin2_vendor", "", "defaultPlugin2_version", "");

    private Plugin dependencyPlugin3 = mock(Plugin.class);

    private PluginInformation dependencyPluginInformation3 = new PluginInformation("defaultPlugin3", "", "defaultPlugin3_vendor",
            "", "defaultPlugin3_version", "");

    private PluginInformation dependencyPluginInformation3_2 = new PluginInformation("defaultPlugin3", "",
            "defaultPlugin3_vendor", "", "defaultPlugin3_version", "");

    private PluginInformation dependencyPluginInformation4 = new PluginInformation("defaultPlugin4", "", "defaultPlugin4_vendor",
            "", "defaultPlugin4_version", "");

    private PluginInformation dependencyPluginInformation4_2 = new PluginInformation("defaultPlugin4", "",
            "defaultPlugin4_vendor", "", "defaultPlugin4_version", "");

    private Plugin dependencyPlugin5 = mock(Plugin.class);

    private PluginInformation dependencyPluginInformation5 = new PluginInformation("defaultPlugin5", "", "defaultPlugin5_vendor",
            "", "defaultPlugin5_version", "");

    private PluginAccessor pluginAccessor = mock(PluginAccessor.class);

    @Before
    public void init() {

        dependencyPluginInformations = new HashMap<String, PluginInformation>();

        given(pluginAccessor.getPlugin("defaultPlugin:defaultPlugin_vendor:defaultPlugin_version")).willReturn(dependencyPlugin1);
        given(pluginAccessor.getPlugin("defaultPlugin2:defaultPlugin2_vendor:defaultPlugin2_version")).willReturn(
                dependencyPlugin2);
        given(pluginAccessor.getPlugin("defaultPlugin3:defaultPlugin3_vendor:defaultPlugin3_version")).willReturn(
                dependencyPlugin3);
        given(pluginAccessor.getPlugin("defaultPlugin4:defaultPlugin4_vendor:defaultPlugin4_version")).willReturn(null);
        given(pluginAccessor.getPlugin("defaultPlugin5:defaultPlugin5_vendor:defaultPlugin5_version")).willReturn(
                dependencyPlugin5);

        createDependencyPlugin(dependencyPlugin1, dependencyPluginInformation1, "defaultPlugin");
        createDependencyPlugin(dependencyPlugin2, dependencyPluginInformation2, "defaultPlugin2");
        createDependencyPlugin(dependencyPlugin3, dependencyPluginInformation3, "defaultPlugin3");
        createDependencyPlugin(null, dependencyPluginInformation4, "defaultPlugin4");
        createDependencyPlugin(dependencyPlugin5, dependencyPluginInformation5, "defaultPlugin5");
        createDependencyPlugin(null, dependencyPluginInformation1_2, "defaultPlugin");
        createDependencyPlugin(null, dependencyPluginInformation2_2, "defaultPlugin2");
        createDependencyPlugin(null, dependencyPluginInformation3_2, "defaultPlugin3");
        createDependencyPlugin(null, dependencyPluginInformation4_2, "defaultPlugin4");
    }

    private void createDependencyPlugin(final Plugin plugin, final PluginInformation dependencyPluginInformation,
            final String pluginName) {
        if (plugin != null) {
            given(plugin.getPluginInformation()).willReturn(dependencyPluginInformation);
            dependencyPluginInformations.put(pluginName, dependencyPluginInformation);
        }
    }

    @Test
    public void shouldReturnEmptyDependencyToEnableWhenNoDependenciesSpecifiedInOnePlugin() throws Exception {
        // given
        Plugin p = mock(Plugin.class);
        given(p.getRequiredPlugins()).willReturn(Collections.<PluginInformation> emptySet());

        PluginDependencyManager m = new DefaultPluginDependencyManager(pluginAccessor);

        // when
        PluginDependencyResult result = m.getDependenciesToEnable(singletonList(p));

        // then
        assertEquals(0, result.getDisabledDependencies().size());
        assertEquals(0, result.getUnsatisfiedDependencies().size());
        assertEquals(0, result.getEnabledDependencies().size());
    }

    @Test
    public void shouldReturnSingleDisabledDependencyForOnePlugin() throws Exception {
        // given
        Plugin p = mock(Plugin.class);
        Set<PluginInformation> disabledRequiredPlugins = Collections.singleton(dependencyPluginInformation1);
        given(p.getRequiredPlugins()).willReturn(disabledRequiredPlugins);

        PluginDependencyManager m = new DefaultPluginDependencyManager(pluginAccessor);

        given(dependencyPlugin1.getPluginState()).willReturn(PluginState.DISABLED);

        // when
        PluginDependencyResult result = m.getDependenciesToEnable(singletonList(p));

        // then
        assertEquals(1, result.getDisabledDependencies().size());
        assertEquals("defaultPlugin", result.getDisabledDependencies().get(0).getName());
        assertEquals("defaultPlugin_vendor", result.getDisabledDependencies().get(0).getVendor());
        assertEquals("defaultPlugin_version", result.getDisabledDependencies().get(0).getVersion());
        assertEquals(0, result.getUnsatisfiedDependencies().size());
        assertEquals(0, result.getEnabledDependencies().size());
    }

    @Test
    public void shouldReturnMultipleDisabledDependencyForOnePlugin() throws Exception {
        // given
        Plugin p = mock(Plugin.class);
        Set<PluginInformation> disabledRequiredPlugins = new HashSet<PluginInformation>();
        disabledRequiredPlugins.add(dependencyPluginInformation1);
        disabledRequiredPlugins.add(dependencyPluginInformation2);
        disabledRequiredPlugins.add(dependencyPluginInformation3);
        given(p.getRequiredPlugins()).willReturn(disabledRequiredPlugins);

        PluginDependencyManager m = new DefaultPluginDependencyManager(pluginAccessor);

        given(dependencyPlugin1.getPluginState()).willReturn(PluginState.DISABLED);
        given(dependencyPlugin2.getPluginState()).willReturn(PluginState.DISABLED);
        given(dependencyPlugin3.getPluginState()).willReturn(PluginState.TEMPORARY);

        // when
        PluginDependencyResult result = m.getDependenciesToEnable(singletonList(p));

        // then
        assertEquals(3, result.getDisabledDependencies().size());

        for (PluginInformation information : result.getDisabledDependencies()) {
            assertNotNull(dependencyPluginInformations.get(information.getName()));

            assertEquals(information.getName(), dependencyPluginInformations.get(information.getName()).getName());
            assertEquals(information.getName() + "_vendor", dependencyPluginInformations.get(information.getName()).getVendor());
            assertEquals(information.getName() + "_version", dependencyPluginInformations.get(information.getName()).getVersion());

            dependencyPluginInformations.remove(information.getName());
        }

        assertEquals(0, result.getUnsatisfiedDependencies().size());
        assertEquals(0, result.getEnabledDependencies().size());
    }

    @Test
    public void shouldReturnDisabledDependenciesForOnePluginWhenSomeAreSatisfied() throws Exception {
        // given
        Plugin p = mock(Plugin.class);
        Set<PluginInformation> disabledRequiredPlugins = new HashSet<PluginInformation>();
        disabledRequiredPlugins.add(dependencyPluginInformation1);
        disabledRequiredPlugins.add(dependencyPluginInformation2);
        disabledRequiredPlugins.add(dependencyPluginInformation3);
        given(p.getRequiredPlugins()).willReturn(disabledRequiredPlugins);

        PluginDependencyManager m = new DefaultPluginDependencyManager(pluginAccessor);

        given(dependencyPlugin1.getPluginState()).willReturn(PluginState.DISABLED);
        given(dependencyPlugin2.getPluginState()).willReturn(PluginState.ENABLING);
        given(dependencyPlugin3.getPluginState()).willReturn(PluginState.ENABLED);

        // when
        PluginDependencyResult result = m.getDependenciesToEnable(singletonList(p));

        // then
        assertEquals(1, result.getDisabledDependencies().size());
        assertEquals("defaultPlugin", result.getDisabledDependencies().get(0).getName());
        assertEquals("defaultPlugin_vendor", result.getDisabledDependencies().get(0).getVendor());
        assertEquals("defaultPlugin_version", result.getDisabledDependencies().get(0).getVersion());
        assertEquals(0, result.getUnsatisfiedDependencies().size());
        assertEquals(0, result.getEnabledDependencies().size());
    }

    @Test
    public void shouldReturnUnsatisfiedDependenciesForOnePluginWhenNoDependencyPluginFound() throws Exception {
        // given
        Plugin p = mock(Plugin.class);
        Set<PluginInformation> disabledRequiredPlugins = new HashSet<PluginInformation>();
        disabledRequiredPlugins.add(dependencyPluginInformation1);
        disabledRequiredPlugins.add(dependencyPluginInformation2);
        disabledRequiredPlugins.add(dependencyPluginInformation3);
        disabledRequiredPlugins.add(dependencyPluginInformation4);
        given(p.getRequiredPlugins()).willReturn(disabledRequiredPlugins);

        PluginDependencyManager m = new DefaultPluginDependencyManager(pluginAccessor);

        given(dependencyPlugin1.getPluginState()).willReturn(PluginState.DISABLED);
        given(dependencyPlugin2.getPluginState()).willReturn(PluginState.ENABLING);
        given(dependencyPlugin3.getPluginState()).willReturn(PluginState.ENABLED);

        // when
        PluginDependencyResult result = m.getDependenciesToEnable(singletonList(p));

        // then
        assertEquals(0, result.getDisabledDependencies().size());
        assertEquals(1, result.getUnsatisfiedDependencies().size());
        assertEquals("defaultPlugin4", result.getUnsatisfiedDependencies().get(0).getName());
        assertEquals("defaultPlugin4_vendor", result.getUnsatisfiedDependencies().get(0).getVendor());
        assertEquals("defaultPlugin4_version", result.getUnsatisfiedDependencies().get(0).getVersion());
        assertEquals(0, result.getEnabledDependencies().size());
    }

    @Test
    public void shouldReturnDisabledDependenciesForMultiplePlugins() throws Exception {
        // given
        Plugin p = mock(Plugin.class);
        Set<PluginInformation> disabledRequiredPlugins = new HashSet<PluginInformation>();
        disabledRequiredPlugins.add(dependencyPluginInformation1);
        disabledRequiredPlugins.add(dependencyPluginInformation2);
        given(p.getRequiredPlugins()).willReturn(disabledRequiredPlugins);

        Plugin p2 = mock(Plugin.class);
        Set<PluginInformation> disabledRequiredPlugins2 = new HashSet<PluginInformation>();
        disabledRequiredPlugins2.add(dependencyPluginInformation2_2);
        disabledRequiredPlugins2.add(dependencyPluginInformation3);
        given(p2.getRequiredPlugins()).willReturn(disabledRequiredPlugins2);

        Plugin p3 = mock(Plugin.class);
        Set<PluginInformation> disabledRequiredPlugins3 = new HashSet<PluginInformation>();
        disabledRequiredPlugins3.add(dependencyPluginInformation1_2);
        disabledRequiredPlugins3.add(dependencyPluginInformation3_2);
        given(p3.getRequiredPlugins()).willReturn(disabledRequiredPlugins3);

        PluginDependencyManager m = new DefaultPluginDependencyManager(pluginAccessor);

        given(dependencyPlugin1.getPluginState()).willReturn(PluginState.DISABLED);
        given(dependencyPlugin2.getPluginState()).willReturn(PluginState.ENABLING);
        given(dependencyPlugin3.getPluginState()).willReturn(PluginState.TEMPORARY);

        List<Plugin> plugins = new ArrayList<Plugin>();
        plugins.add(p);
        plugins.add(p2);
        plugins.add(p3);

        // when
        PluginDependencyResult result = m.getDependenciesToEnable(plugins);

        // then
        assertEquals(2, result.getDisabledDependencies().size());

        dependencyPluginInformations.remove(dependencyPlugin2.getPluginInformation().getName());
        for (PluginInformation information : result.getDisabledDependencies()) {
            assertNotNull(dependencyPluginInformations.get(information.getName()));

            assertEquals(information.getName(), dependencyPluginInformations.get(information.getName()).getName());
            assertEquals(information.getName() + "_vendor", dependencyPluginInformations.get(information.getName()).getVendor());
            assertEquals(information.getName() + "_version", dependencyPluginInformations.get(information.getName()).getVersion());

            dependencyPluginInformations.remove(information.getName());
        }

        assertEquals(0, result.getUnsatisfiedDependencies().size());
        assertEquals(0, result.getEnabledDependencies().size());
    }

    @Test
    public void shouldReturnUnsatisfiedDependenciesForMultiplePlugins() throws Exception {
        // given
        Plugin p = mock(Plugin.class);
        Set<PluginInformation> disabledRequiredPlugins = new HashSet<PluginInformation>();
        disabledRequiredPlugins.add(dependencyPluginInformation1);
        disabledRequiredPlugins.add(dependencyPluginInformation4);
        given(p.getRequiredPlugins()).willReturn(disabledRequiredPlugins);

        Plugin p2 = mock(Plugin.class);
        Set<PluginInformation> disabledRequiredPlugins2 = new HashSet<PluginInformation>();
        disabledRequiredPlugins2.add(dependencyPluginInformation2_2);
        disabledRequiredPlugins2.add(dependencyPluginInformation3);
        given(p2.getRequiredPlugins()).willReturn(disabledRequiredPlugins2);

        Plugin p3 = mock(Plugin.class);
        Set<PluginInformation> disabledRequiredPlugins3 = new HashSet<PluginInformation>();
        disabledRequiredPlugins3.add(dependencyPluginInformation1_2);
        disabledRequiredPlugins3.add(dependencyPluginInformation4_2);
        given(p3.getRequiredPlugins()).willReturn(disabledRequiredPlugins3);

        PluginDependencyManager m = new DefaultPluginDependencyManager(pluginAccessor);

        given(dependencyPlugin1.getPluginState()).willReturn(PluginState.DISABLED);
        given(dependencyPlugin2.getPluginState()).willReturn(PluginState.ENABLING);
        given(dependencyPlugin3.getPluginState()).willReturn(PluginState.TEMPORARY);

        List<Plugin> plugins = new ArrayList<Plugin>();
        plugins.add(p);
        plugins.add(p2);
        plugins.add(p3);

        // when
        PluginDependencyResult result = m.getDependenciesToEnable(plugins);

        // then
        assertEquals(0, result.getDisabledDependencies().size());
        assertEquals(1, result.getUnsatisfiedDependencies().size());
        assertEquals("defaultPlugin4", result.getUnsatisfiedDependencies().get(0).getName());
        assertEquals("defaultPlugin4_vendor", result.getUnsatisfiedDependencies().get(0).getVendor());
        assertEquals("defaultPlugin4_version", result.getUnsatisfiedDependencies().get(0).getVersion());
        assertEquals(0, result.getEnabledDependencies().size());
    }

    @Test
    public void shouldReturnValidResultForCyclicDependencies() throws Exception {
        // given
        Plugin p = mock(Plugin.class);
        given(p.getPluginInformation()).willReturn(dependencyPluginInformation1);
        Set<PluginInformation> disabledRequiredPlugins = new HashSet<PluginInformation>();
        disabledRequiredPlugins.add(dependencyPluginInformation2);
        given(p.getRequiredPlugins()).willReturn(disabledRequiredPlugins);

        Plugin p2 = mock(Plugin.class);
        given(p2.getPluginInformation()).willReturn(dependencyPluginInformation2);
        Set<PluginInformation> disabledRequiredPlugins2 = new HashSet<PluginInformation>();
        disabledRequiredPlugins2.add(dependencyPluginInformation1_2);
        disabledRequiredPlugins2.add(dependencyPluginInformation4_2);
        given(p2.getRequiredPlugins()).willReturn(disabledRequiredPlugins2);

        Plugin p3 = mock(Plugin.class);
        given(p3.getPluginInformation()).willReturn(dependencyPluginInformation4);
        Set<PluginInformation> disabledRequiredPlugins3 = new HashSet<PluginInformation>();
        disabledRequiredPlugins3.add(dependencyPluginInformation2_2);
        given(p3.getRequiredPlugins()).willReturn(disabledRequiredPlugins3);

        PluginDependencyManager m = new DefaultPluginDependencyManager(pluginAccessor);

        given(dependencyPlugin1.getPluginState()).willReturn(PluginState.DISABLED);
        given(dependencyPlugin2.getPluginState()).willReturn(PluginState.TEMPORARY);
        given(dependencyPlugin3.getPluginState()).willReturn(PluginState.TEMPORARY);

        List<Plugin> plugins = new ArrayList<Plugin>();
        plugins.add(p);
        plugins.add(p2);
        plugins.add(p3);
        // when
        PluginDependencyResult result = m.getDependenciesToEnable(plugins);

        // then
        assertEquals(0, result.getDisabledDependencies().size());
        assertEquals(1, result.getUnsatisfiedDependencies().size());
        assertEquals(0, result.getEnabledDependencies().size());
    }

    @Test
    public void shouldReturnEmptyDependencyToDisableWhenNoDependenciesSpecifiedInOnePlugin() throws Exception {
        // given
        Plugin p = mock(Plugin.class);
        given(p.getRequiredPlugins()).willReturn(Collections.<PluginInformation> emptySet());

        PluginDependencyManager m = new DefaultPluginDependencyManager(pluginAccessor);

        // when
        PluginDependencyResult result = m.getDependenciesToDisable(singletonList(p));

        // then
        assertEquals(0, result.getDisabledDependencies().size());
        assertEquals(0, result.getUnsatisfiedDependencies().size());
        assertEquals(0, result.getEnabledDependencies().size());
    }

    @Test
    public void shouldReturnSingleEnabledDependencyForOnePlugin() throws Exception {
        // given
        Plugin p = mock(Plugin.class);
        Set<PluginInformation> disabledRequiredPlugins = Collections.singleton(dependencyPluginInformation1);
        given(p.getRequiredPlugins()).willReturn(disabledRequiredPlugins);

        PluginDependencyManager m = new DefaultPluginDependencyManager(pluginAccessor);

        given(dependencyPlugin1.getPluginState()).willReturn(PluginState.ENABLED);

        // when
        PluginDependencyResult result = m.getDependenciesToDisable(singletonList(p));

        // then
        assertEquals(0, result.getDisabledDependencies().size());
        assertEquals(0, result.getUnsatisfiedDependencies().size());
        assertEquals(1, result.getEnabledDependencies().size());
        assertEquals("defaultPlugin", result.getEnabledDependencies().get(0).getName());
        assertEquals("defaultPlugin_vendor", result.getEnabledDependencies().get(0).getVendor());
        assertEquals("defaultPlugin_version", result.getEnabledDependencies().get(0).getVersion());
    }

    @Test
    public void shouldReturnMultipleEnabledDependencyForOnePlugin() throws Exception {
        // given
        Plugin p = mock(Plugin.class);
        Set<PluginInformation> disabledRequiredPlugins = new HashSet<PluginInformation>();
        disabledRequiredPlugins.add(dependencyPluginInformation1);
        disabledRequiredPlugins.add(dependencyPluginInformation2);
        disabledRequiredPlugins.add(dependencyPluginInformation3);
        given(p.getRequiredPlugins()).willReturn(disabledRequiredPlugins);

        PluginDependencyManager m = new DefaultPluginDependencyManager(pluginAccessor);

        given(dependencyPlugin1.getPluginState()).willReturn(PluginState.ENABLED);
        given(dependencyPlugin2.getPluginState()).willReturn(PluginState.ENABLED);
        given(dependencyPlugin3.getPluginState()).willReturn(PluginState.ENABLED);

        // when
        PluginDependencyResult result = m.getDependenciesToDisable(singletonList(p));

        // then
        assertEquals(3, result.getEnabledDependencies().size());

        for (PluginInformation information : result.getEnabledDependencies()) {
            assertNotNull(dependencyPluginInformations.get(information.getName()));

            assertEquals(information.getName(), dependencyPluginInformations.get(information.getName()).getName());
            assertEquals(information.getName() + "_vendor", dependencyPluginInformations.get(information.getName()).getVendor());
            assertEquals(information.getName() + "_version", dependencyPluginInformations.get(information.getName()).getVersion());

            dependencyPluginInformations.remove(information.getName());
        }

        assertEquals(0, result.getUnsatisfiedDependencies().size());
        assertEquals(0, result.getDisabledDependencies().size());
    }

    @Test
    public void shouldReturnEnabledDependenciesForOnePluginWhenSomeAreSatisfied() throws Exception {
        // given
        Plugin p = mock(Plugin.class);
        Set<PluginInformation> disabledRequiredPlugins = new HashSet<PluginInformation>();
        disabledRequiredPlugins.add(dependencyPluginInformation1);
        disabledRequiredPlugins.add(dependencyPluginInformation2);
        disabledRequiredPlugins.add(dependencyPluginInformation3);
        given(p.getRequiredPlugins()).willReturn(disabledRequiredPlugins);

        PluginDependencyManager m = new DefaultPluginDependencyManager(pluginAccessor);

        given(dependencyPlugin1.getPluginState()).willReturn(PluginState.DISABLED);
        given(dependencyPlugin2.getPluginState()).willReturn(PluginState.ENABLED);
        given(dependencyPlugin3.getPluginState()).willReturn(PluginState.TEMPORARY);

        // when
        PluginDependencyResult result = m.getDependenciesToDisable(singletonList(p));

        // then
        assertEquals(0, result.getDisabledDependencies().size());
        assertEquals(0, result.getUnsatisfiedDependencies().size());
        assertEquals(1, result.getEnabledDependencies().size());
        assertEquals("defaultPlugin2", result.getEnabledDependencies().get(0).getName());
        assertEquals("defaultPlugin2_vendor", result.getEnabledDependencies().get(0).getVendor());
        assertEquals("defaultPlugin2_version", result.getEnabledDependencies().get(0).getVersion());
    }

    @Test
    public void shouldReturnEnabledDependenciesForMultiplePlugins() throws Exception {
        // given
        Plugin p = mock(Plugin.class);
        Set<PluginInformation> disabledRequiredPlugins = new HashSet<PluginInformation>();
        disabledRequiredPlugins.add(dependencyPluginInformation1);
        disabledRequiredPlugins.add(dependencyPluginInformation2);
        given(p.getRequiredPlugins()).willReturn(disabledRequiredPlugins);

        Plugin p2 = mock(Plugin.class);
        Set<PluginInformation> disabledRequiredPlugins2 = new HashSet<PluginInformation>();
        disabledRequiredPlugins2.add(dependencyPluginInformation2_2);
        disabledRequiredPlugins2.add(dependencyPluginInformation3);
        given(p2.getRequiredPlugins()).willReturn(disabledRequiredPlugins2);

        Plugin p3 = mock(Plugin.class);
        Set<PluginInformation> disabledRequiredPlugins3 = new HashSet<PluginInformation>();
        disabledRequiredPlugins3.add(dependencyPluginInformation1_2);
        disabledRequiredPlugins3.add(dependencyPluginInformation3_2);
        given(p3.getRequiredPlugins()).willReturn(disabledRequiredPlugins3);

        PluginDependencyManager m = new DefaultPluginDependencyManager(pluginAccessor);

        given(dependencyPlugin1.getPluginState()).willReturn(PluginState.ENABLED);
        given(dependencyPlugin2.getPluginState()).willReturn(PluginState.ENABLED);
        given(dependencyPlugin3.getPluginState()).willReturn(PluginState.TEMPORARY);

        List<Plugin> plugins = new ArrayList<Plugin>();
        plugins.add(p);
        plugins.add(p2);
        plugins.add(p3);

        // when
        PluginDependencyResult result = m.getDependenciesToDisable(plugins);

        // then
        assertEquals(0, result.getDisabledDependencies().size());
        assertEquals(0, result.getUnsatisfiedDependencies().size());
        assertEquals(2, result.getEnabledDependencies().size());
        dependencyPluginInformations.remove(dependencyPlugin3.getPluginInformation().getName());
        for (PluginInformation information : result.getEnabledDependencies()) {
            assertNotNull(dependencyPluginInformations.get(information.getName()));

            assertEquals(information.getName(), dependencyPluginInformations.get(information.getName()).getName());
            assertEquals(information.getName() + "_vendor", dependencyPluginInformations.get(information.getName()).getVendor());
            assertEquals(information.getName() + "_version", dependencyPluginInformations.get(information.getName()).getVersion());

            dependencyPluginInformations.remove(information.getName());
        }
    }

    @Test
    public void shouldCheckDependenciesDependenciesAndReturnMultipleDependencies() throws Exception {
        // given
        Plugin p = mock(Plugin.class);
        given(p.getPluginInformation()).willReturn(dependencyPluginInformation1);
        Set<PluginInformation> disabledRequiredPlugins = new HashSet<PluginInformation>();
        disabledRequiredPlugins.add(dependencyPluginInformation2);
        given(p.getRequiredPlugins()).willReturn(disabledRequiredPlugins);

        Plugin p2 = mock(Plugin.class);
        given(p2.getPluginInformation()).willReturn(dependencyPluginInformation2);
        Set<PluginInformation> disabledRequiredPlugins2 = new HashSet<PluginInformation>();
        disabledRequiredPlugins2.add(dependencyPluginInformation3);
        given(dependencyPlugin2.getRequiredPlugins()).willReturn(disabledRequiredPlugins2);

        given(dependencyPlugin1.getPluginState()).willReturn(PluginState.TEMPORARY);
        given(dependencyPlugin2.getPluginState()).willReturn(PluginState.DISABLED);
        given(dependencyPlugin3.getPluginState()).willReturn(PluginState.TEMPORARY);

        PluginDependencyManager m = new DefaultPluginDependencyManager(pluginAccessor);

        // when
        PluginDependencyResult result = m.getDependenciesToEnable(singletonList(p));

        // then
        assertEquals(2, result.getDisabledDependencies().size());

        for (PluginInformation information : result.getDisabledDependencies()) {
            assertNotNull(dependencyPluginInformations.get(information.getName()));

            assertEquals(information.getName(), dependencyPluginInformations.get(information.getName()).getName());
            assertEquals(information.getName() + "_vendor", dependencyPluginInformations.get(information.getName()).getVendor());
            assertEquals(information.getName() + "_version", dependencyPluginInformations.get(information.getName()).getVersion());

            dependencyPluginInformations.remove(information.getName());
        }

        assertEquals(0, result.getUnsatisfiedDependencies().size());
        assertEquals(0, result.getEnabledDependencies().size());
    }

    @Test
    public void shouldCheckDependenciesDependenciesAndReturnMultipleDependenciesForDisable() throws Exception {
        // given
        Plugin p = mock(Plugin.class);
        given(p.getPluginInformation()).willReturn(dependencyPluginInformation1);
        Set<PluginInformation> disabledRequiredPlugins = new HashSet<PluginInformation>();
        disabledRequiredPlugins.add(dependencyPluginInformation2);
        given(p.getRequiredPlugins()).willReturn(disabledRequiredPlugins);

        Plugin p2 = mock(Plugin.class);
        given(p2.getPluginInformation()).willReturn(dependencyPluginInformation2);
        Set<PluginInformation> disabledRequiredPlugins2 = new HashSet<PluginInformation>();
        disabledRequiredPlugins2.add(dependencyPluginInformation3);
        given(dependencyPlugin2.getRequiredPlugins()).willReturn(disabledRequiredPlugins2);

        given(dependencyPlugin1.getPluginState()).willReturn(PluginState.ENABLED);
        given(dependencyPlugin2.getPluginState()).willReturn(PluginState.ENABLED);
        given(dependencyPlugin3.getPluginState()).willReturn(PluginState.ENABLED);

        PluginDependencyManager m = new DefaultPluginDependencyManager(pluginAccessor);

        // when
        PluginDependencyResult result = m.getDependenciesToDisable(singletonList(p));

        // then
        assertEquals(2, result.getEnabledDependencies().size());

        for (PluginInformation information : result.getDisabledDependencies()) {
            assertNotNull(dependencyPluginInformations.get(information.getName()));

            assertEquals(information.getName(), dependencyPluginInformations.get(information.getName()).getName());
            assertEquals(information.getName() + "_vendor", dependencyPluginInformations.get(information.getName()).getVendor());
            assertEquals(information.getName() + "_version", dependencyPluginInformations.get(information.getName()).getVersion());

            dependencyPluginInformations.remove(information.getName());
        }

        assertEquals(0, result.getUnsatisfiedDependencies().size());
        assertEquals(0, result.getDisabledDependencies().size());
    }

    @Test
    public void shouldSetCyclicFlagOnCyclicDependencies() throws Exception {
        // given
        Plugin p = mock(Plugin.class);
        given(p.getPluginInformation()).willReturn(dependencyPluginInformation1);
        Set<PluginInformation> disabledRequiredPlugins = new HashSet<PluginInformation>();
        disabledRequiredPlugins.add(dependencyPluginInformation2);
        given(p.getRequiredPlugins()).willReturn(disabledRequiredPlugins);

        Plugin p2 = mock(Plugin.class);
        given(p2.getPluginInformation()).willReturn(dependencyPluginInformation2);
        Set<PluginInformation> disabledRequiredPlugins2 = new HashSet<PluginInformation>();
        disabledRequiredPlugins2.add(dependencyPluginInformation1);
        given(p2.getRequiredPlugins()).willReturn(disabledRequiredPlugins2);

        PluginDependencyManager m = new DefaultPluginDependencyManager(pluginAccessor);

        given(dependencyPlugin1.getPluginState()).willReturn(PluginState.TEMPORARY);
        given(dependencyPlugin2.getPluginState()).willReturn(PluginState.TEMPORARY);

        List<Plugin> plugins = new ArrayList<Plugin>();
        plugins.add(p);
        plugins.add(p2);

        // when
        PluginDependencyResult result = m.getDependenciesToEnable(plugins);

        // then
        assertTrue(result.isCyclic());
    }

    @Test
    public void shouldSetCyclicFlagWhenCyclicDependenciesDependencies() throws Exception {
        // given
        Plugin p = mock(Plugin.class);
        given(p.getPluginInformation()).willReturn(dependencyPluginInformation1);
        Set<PluginInformation> disabledRequiredPlugins = new HashSet<PluginInformation>();
        disabledRequiredPlugins.add(dependencyPluginInformation2);
        given(p.getRequiredPlugins()).willReturn(disabledRequiredPlugins);

        Plugin p2 = mock(Plugin.class);
        given(p2.getPluginInformation()).willReturn(dependencyPluginInformation2);
        Set<PluginInformation> disabledRequiredPlugins2 = new HashSet<PluginInformation>();
        disabledRequiredPlugins2.add(dependencyPluginInformation3);
        given(dependencyPlugin2.getRequiredPlugins()).willReturn(disabledRequiredPlugins2);

        Plugin p3 = mock(Plugin.class);
        given(p3.getPluginInformation()).willReturn(dependencyPluginInformation3);
        Set<PluginInformation> disabledRequiredPlugins3 = new HashSet<PluginInformation>();
        disabledRequiredPlugins3.add(dependencyPluginInformation1);
        given(dependencyPlugin3.getRequiredPlugins()).willReturn(disabledRequiredPlugins3);

        given(dependencyPlugin1.getPluginState()).willReturn(PluginState.TEMPORARY);
        given(dependencyPlugin2.getPluginState()).willReturn(PluginState.TEMPORARY);
        given(dependencyPlugin3.getPluginState()).willReturn(PluginState.TEMPORARY);

        PluginDependencyManager m = new DefaultPluginDependencyManager(pluginAccessor);

        // when
        PluginDependencyResult result = m.getDependenciesToEnable(singletonList(p));

        // then
        assertTrue(result.isCyclic());
    }

    @Test
    public void shouldSetCyclicFlagWhenCyclicDependenciesDependenciesButNotDependencyToArgument() throws Exception {
        // given
        Plugin p = mock(Plugin.class);
        given(p.getPluginInformation()).willReturn(dependencyPluginInformation1);
        Set<PluginInformation> disabledRequiredPlugins = new HashSet<PluginInformation>();
        disabledRequiredPlugins.add(dependencyPluginInformation2);
        given(p.getRequiredPlugins()).willReturn(disabledRequiredPlugins);

        Plugin p2 = mock(Plugin.class);
        given(p2.getPluginInformation()).willReturn(dependencyPluginInformation2);
        Set<PluginInformation> disabledRequiredPlugins2 = new HashSet<PluginInformation>();
        disabledRequiredPlugins2.add(dependencyPluginInformation3_2);
        given(dependencyPlugin2.getRequiredPlugins()).willReturn(disabledRequiredPlugins2);

        Plugin p3 = mock(Plugin.class);
        given(p3.getPluginInformation()).willReturn(dependencyPluginInformation3);
        Set<PluginInformation> disabledRequiredPlugins3 = new HashSet<PluginInformation>();
        disabledRequiredPlugins3.add(dependencyPluginInformation2_2);
        given(dependencyPlugin3.getRequiredPlugins()).willReturn(disabledRequiredPlugins3);

        given(dependencyPlugin1.getPluginState()).willReturn(PluginState.TEMPORARY);
        given(dependencyPlugin2.getPluginState()).willReturn(PluginState.TEMPORARY);
        given(dependencyPlugin3.getPluginState()).willReturn(PluginState.TEMPORARY);

        PluginDependencyManager m = new DefaultPluginDependencyManager(pluginAccessor);

        // when
        PluginDependencyResult result = m.getDependenciesToEnable(singletonList(p));

        // then
        assertTrue(result.isCyclic());
    }

    @Test
    public void shouldSetCyclicFlagWhenCyclicDependenciesDependenciesToArgumentsDependency() throws Exception {
        // given
        Plugin p = mock(Plugin.class);
        given(p.getPluginInformation()).willReturn(dependencyPluginInformation1);
        Set<PluginInformation> disabledRequiredPlugins = new HashSet<PluginInformation>();
        disabledRequiredPlugins.add(dependencyPluginInformation2);
        given(p.getRequiredPlugins()).willReturn(disabledRequiredPlugins);

        Plugin p2 = mock(Plugin.class);
        given(p2.getPluginInformation()).willReturn(dependencyPluginInformation2);
        Set<PluginInformation> disabledRequiredPlugins2 = new HashSet<PluginInformation>();
        disabledRequiredPlugins2.add(dependencyPluginInformation3);
        given(dependencyPlugin2.getRequiredPlugins()).willReturn(disabledRequiredPlugins2);

        Plugin p3 = mock(Plugin.class);
        given(p3.getPluginInformation()).willReturn(dependencyPluginInformation3);
        Set<PluginInformation> disabledRequiredPlugins3 = new HashSet<PluginInformation>();
        disabledRequiredPlugins3.add(dependencyPluginInformation5);
        given(dependencyPlugin3.getRequiredPlugins()).willReturn(disabledRequiredPlugins3);

        Plugin p5 = mock(Plugin.class);
        given(p5.getPluginInformation()).willReturn(dependencyPluginInformation5);
        Set<PluginInformation> disabledRequiredPlugins5 = new HashSet<PluginInformation>();
        disabledRequiredPlugins5.add(dependencyPluginInformation2);
        given(dependencyPlugin5.getRequiredPlugins()).willReturn(disabledRequiredPlugins5);

        given(dependencyPlugin1.getPluginState()).willReturn(PluginState.TEMPORARY);
        given(dependencyPlugin2.getPluginState()).willReturn(PluginState.TEMPORARY);
        given(dependencyPlugin3.getPluginState()).willReturn(PluginState.TEMPORARY);
        given(dependencyPlugin5.getPluginState()).willReturn(PluginState.TEMPORARY);

        PluginDependencyManager m = new DefaultPluginDependencyManager(pluginAccessor);

        // when
        PluginDependencyResult result = m.getDependenciesToEnable(singletonList(p));

        // then
        assertTrue(result.isCyclic());
    }
}
