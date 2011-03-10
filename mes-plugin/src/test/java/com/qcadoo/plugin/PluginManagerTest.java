package com.qcadoo.plugin;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;

import com.qcadoo.plugin.dependency.PluginDependencyInformation;
import com.qcadoo.plugin.dependency.PluginDependencyResult;

public class PluginManagerTest {

    private Plugin plugin = mock(Plugin.class);

    private Plugin anotherPlugin = mock(Plugin.class);

    private PluginAccessor pluginAccessor = mock(PluginAccessor.class);

    private PluginDao pluginDao = mock(PluginDao.class);

    private PluginDependencyManager pluginDependencyManager = mock(PluginDependencyManager.class);

    private PluginFileManager pluginFileManager = mock(PluginFileManager.class);

    private PluginServerManager pluginServerManager = mock(PluginServerManager.class);

    private PluginInformation pluginInformation = mock(PluginInformation.class);

    private PluginDescriptorParser pluginDescriptorParser = mock(PluginDescriptorParser.class);

    private DefaultPluginManager pluginManager;

    @Before
    public void init() {
        given(pluginAccessor.getPlugin("pluginname")).willReturn(plugin);
        given(pluginAccessor.getPlugin("anotherPluginname")).willReturn(anotherPlugin);

        given(pluginInformation.getName()).willReturn("unknownplugin");

        pluginManager = new DefaultPluginManager();
        pluginManager.setPluginAccessor(pluginAccessor);
        pluginManager.setPluginDao(pluginDao);
        pluginManager.setPluginDependencyManager(pluginDependencyManager);
        pluginManager.setPluginFileManager(pluginFileManager);
        pluginManager.setPluginServerManager(pluginServerManager);
        pluginManager.setPluginDescriptorParser(pluginDescriptorParser);
    }

    @Test
    public void shouldNotEnableEnabledPlugin() throws Exception {
        // given
        given(plugin.hasState(PluginState.ENABLED)).willReturn(true);

        // when
        PluginOperationResult pluginOperationResult = pluginManager.enablePlugin("pluginname");

        // then
        verify(plugin, never()).changeStateTo(PluginState.ENABLED);
        verify(pluginDao, never()).save(plugin);
        assertTrue(pluginOperationResult.isSuccess());
        assertEquals(PluginOperationStatus.SUCCESS, pluginOperationResult.getStatus());
    }

    @Test
    public void shouldEnableDisabledPlugin() throws Exception {
        // given
        PluginDependencyResult pluginDependencyResult = PluginDependencyResult.satisfiedDependencies();
        given(pluginDependencyManager.getDependenciesToEnable(singletonList(plugin))).willReturn(pluginDependencyResult);
        given(pluginDependencyManager.sortPluginsInDependencyOrder(singletonList(plugin))).willReturn(singletonList(plugin));

        // when
        PluginOperationResult pluginOperationResult = pluginManager.enablePlugin("pluginname");

        // then
        verify(plugin).changeStateTo(PluginState.ENABLED);
        verify(pluginDao).save(plugin);
        assertTrue(pluginOperationResult.isSuccess());
        assertEquals(PluginOperationStatus.SUCCESS, pluginOperationResult.getStatus());
        assertEquals(0, pluginOperationResult.getPluginDependencyResult().getDisabledDependencies().size());
        assertEquals(0, pluginOperationResult.getPluginDependencyResult().getUnsatisfiedDependencies().size());
    }

    @Test
    public void shouldEnableUninstalledPlugin() throws Exception {
        // given
        given(anotherPlugin.hasState(PluginState.TEMPORARY)).willReturn(true);
        given(anotherPlugin.getFilename()).willReturn("filename");
        given(pluginFileManager.installPlugin("filename")).willReturn(true);

        PluginDependencyResult pluginDependencyResult = PluginDependencyResult.satisfiedDependencies();
        given(pluginDependencyManager.getDependenciesToEnable(singletonList(anotherPlugin))).willReturn(pluginDependencyResult);
        given(pluginDependencyManager.sortPluginsInDependencyOrder(singletonList(anotherPlugin))).willReturn(
                singletonList(anotherPlugin));

        // when
        PluginOperationResult pluginOperationResult = pluginManager.enablePlugin("anotherPluginname");

        // then
        verify(anotherPlugin).changeStateTo(PluginState.ENABLING);
        verify(pluginDao).save(anotherPlugin);
        verify(pluginServerManager).restart();
        assertTrue(pluginOperationResult.isSuccess());
        assertEquals(PluginOperationStatus.SUCCESS_WITH_RESTART, pluginOperationResult.getStatus());
        assertEquals(0, pluginOperationResult.getPluginDependencyResult().getDisabledDependencies().size());
        assertEquals(0, pluginOperationResult.getPluginDependencyResult().getUnsatisfiedDependencies().size());
    }

    @Test
    public void shouldNotEnablePluginIfCannotInstall() throws Exception {
        // given
        given(anotherPlugin.hasState(PluginState.TEMPORARY)).willReturn(true);
        given(anotherPlugin.getFilename()).willReturn("filename");
        given(pluginFileManager.installPlugin("filename")).willReturn(false);

        PluginDependencyResult pluginDependencyResult = PluginDependencyResult.satisfiedDependencies();
        given(pluginDependencyManager.getDependenciesToEnable(singletonList(anotherPlugin))).willReturn(pluginDependencyResult);

        // when
        PluginOperationResult pluginOperationResult = pluginManager.enablePlugin("anotherPluginname");

        // then
        verify(anotherPlugin, never()).changeStateTo(PluginState.ENABLING);
        verify(pluginDao, never()).save(anotherPlugin);
        verify(pluginServerManager, never()).restart();
        assertFalse(pluginOperationResult.isSuccess());
        assertEquals(PluginOperationStatus.CANNOT_INSTALL_PLUGIN, pluginOperationResult.getStatus());
        assertEquals(0, pluginOperationResult.getPluginDependencyResult().getDisabledDependencies().size());
        assertEquals(0, pluginOperationResult.getPluginDependencyResult().getUnsatisfiedDependencies().size());
    }

    @Test
    public void shouldNotEnablePluginWithDisabledDependencies() throws Exception {
        // given
        given(plugin.hasState(PluginState.DISABLED)).willReturn(true);

        PluginDependencyResult pluginDependencyResult = PluginDependencyResult.disabledDependencies(Collections
                .singleton(new PluginDependencyInformation("unknownplugin", null, false, null, false)));
        given(pluginDependencyManager.getDependenciesToEnable(singletonList(plugin))).willReturn(pluginDependencyResult);

        // when
        PluginOperationResult pluginOperationResult = pluginManager.enablePlugin("pluginname");

        // then
        verify(plugin, never()).changeStateTo(PluginState.ENABLED);
        verify(pluginDao, never()).save(plugin);
        assertFalse(pluginOperationResult.isSuccess());
        assertEquals(PluginOperationStatus.DISABLED_DEPENDENCIES, pluginOperationResult.getStatus());
        assertEquals(0, pluginOperationResult.getPluginDependencyResult().getUnsatisfiedDependencies().size());
        assertEquals(1, pluginOperationResult.getPluginDependencyResult().getDisabledDependencies().size());
        assertEquals(1, pluginOperationResult.getPluginDependencyResult().getDisabledDependencies().size());
        assertTrue(pluginOperationResult.getPluginDependencyResult().getDisabledDependencies()
                .contains(new PluginDependencyInformation("unknownplugin", null, false, null, false)));
    }

    @Test
    public void shouldNotEnablePluginWithUnsatisfiedDependencies() throws Exception {
        // given
        given(plugin.hasState(PluginState.DISABLED)).willReturn(true);

        PluginDependencyResult pluginDependencyResult = PluginDependencyResult.unsatisfiedDependencies(Collections
                .singleton(new PluginDependencyInformation("unknownplugin", null, false, null, false)));
        given(pluginDependencyManager.getDependenciesToEnable(singletonList(plugin))).willReturn(pluginDependencyResult);

        // when
        PluginOperationResult pluginOperationResult = pluginManager.enablePlugin("pluginname");

        // then
        verify(plugin, never()).changeStateTo(PluginState.ENABLED);
        verify(pluginDao, never()).save(plugin);
        assertFalse(pluginOperationResult.isSuccess());
        assertEquals(PluginOperationStatus.UNSATISFIED_DEPENDENCIES, pluginOperationResult.getStatus());
        assertEquals(1, pluginOperationResult.getPluginDependencyResult().getUnsatisfiedDependencies().size());
        assertEquals(1, pluginOperationResult.getPluginDependencyResult().getUnsatisfiedDependencies().size());
        assertTrue(pluginOperationResult.getPluginDependencyResult().getUnsatisfiedDependencies()
                .contains(new PluginDependencyInformation("unknownplugin", null, false, null, false)));
    }

    @Test
    public void shouldEnableMultiplePlugins() throws Exception {
        // given
        given(plugin.hasState(PluginState.DISABLED)).willReturn(true);

        given(anotherPlugin.hasState(PluginState.TEMPORARY)).willReturn(true);
        given(anotherPlugin.getFilename()).willReturn("filename");
        given(pluginFileManager.installPlugin("filename")).willReturn(true);

        PluginDependencyResult pluginDependencyResult = PluginDependencyResult.satisfiedDependencies();
        given(pluginDependencyManager.getDependenciesToEnable(newArrayList(plugin, anotherPlugin))).willReturn(
                pluginDependencyResult);
        given(pluginDependencyManager.sortPluginsInDependencyOrder(newArrayList(plugin, anotherPlugin))).willReturn(
                newArrayList(plugin, anotherPlugin));

        // when
        PluginOperationResult pluginOperationResult = pluginManager.enablePlugin("pluginname", "anotherPluginname");

        // then
        verify(plugin).changeStateTo(PluginState.ENABLED);
        verify(pluginDao).save(plugin);
        verify(anotherPlugin).changeStateTo(PluginState.ENABLING);
        verify(pluginDao).save(anotherPlugin);
        assertTrue(pluginOperationResult.isSuccess());
        assertEquals(PluginOperationStatus.SUCCESS_WITH_RESTART, pluginOperationResult.getStatus());
        assertEquals(0, pluginOperationResult.getPluginDependencyResult().getDisabledDependencies().size());
        assertEquals(0, pluginOperationResult.getPluginDependencyResult().getUnsatisfiedDependencies().size());
        verify(pluginServerManager).restart();
    }

    @Test
    public void shouldNotDisableNotEnabledPlugin() throws Exception {
        // given
        given(plugin.hasState(PluginState.ENABLED)).willReturn(false);

        // when
        PluginOperationResult pluginOperationResult = pluginManager.disablePlugin("pluginname");

        // then
        verify(plugin, never()).changeStateTo(PluginState.DISABLED);
        verify(pluginDao, never()).save(plugin);
        assertTrue(pluginOperationResult.isSuccess());
        assertEquals(PluginOperationStatus.SUCCESS, pluginOperationResult.getStatus());
    }

    @Test
    public void shouldDisableEnabledPlugin() throws Exception {
        // given
        given(plugin.hasState(PluginState.ENABLED)).willReturn(true);

        PluginDependencyResult pluginDependencyResult = PluginDependencyResult.satisfiedDependencies();
        given(pluginDependencyManager.getDependenciesToDisable(singletonList(plugin))).willReturn(pluginDependencyResult);
        given(pluginDependencyManager.sortPluginsInDependencyOrder(singletonList(plugin))).willReturn(singletonList(plugin));

        // when
        PluginOperationResult pluginOperationResult = pluginManager.disablePlugin("pluginname");

        // then
        verify(plugin).changeStateTo(PluginState.DISABLED);
        verify(pluginDao).save(plugin);
        assertTrue(pluginOperationResult.isSuccess());
        assertEquals(PluginOperationStatus.SUCCESS, pluginOperationResult.getStatus());
        assertEquals(0, pluginOperationResult.getPluginDependencyResult().getEnabledDependencies().size());
    }

    @Test
    public void shouldNotDisableSystemPlugin() throws Exception {
        // given
        given(plugin.hasState(PluginState.ENABLED)).willReturn(true);
        given(plugin.isSystemPlugin()).willReturn(true);

        given(anotherPlugin.hasState(PluginState.ENABLED)).willReturn(true);

        PluginDependencyResult pluginDependencyResult = PluginDependencyResult.satisfiedDependencies();
        given(pluginDependencyManager.getDependenciesToDisable(newArrayList(plugin, anotherPlugin))).willReturn(
                pluginDependencyResult);

        // when
        PluginOperationResult pluginOperationResult = pluginManager.disablePlugin("pluginname", "anotherPluginname");

        // then
        verify(plugin, never()).changeStateTo(PluginState.DISABLED);
        verify(anotherPlugin, never()).changeStateTo(PluginState.DISABLED);
        verify(pluginDao, never()).save(plugin);
        verify(pluginDao, never()).save(anotherPlugin);
        assertFalse(pluginOperationResult.isSuccess());
        assertEquals(PluginOperationStatus.SYSTEM_PLUGIN_DISABLING, pluginOperationResult.getStatus());
        assertEquals(0, pluginOperationResult.getPluginDependencyResult().getEnabledDependencies().size());
    }

    @Test
    public void shouldNotDisablePluginWithEnabledDependencies() throws Exception {
        // given
        given(plugin.hasState(PluginState.ENABLED)).willReturn(true);

        PluginDependencyResult pluginDependencyResult = PluginDependencyResult.enabledDependencies(Collections
                .singleton(new PluginDependencyInformation("unknownplugin", null, false, null, false)));
        given(pluginDependencyManager.getDependenciesToDisable(singletonList(plugin))).willReturn(pluginDependencyResult);

        // when
        PluginOperationResult pluginOperationResult = pluginManager.disablePlugin("pluginname");

        // then
        verify(plugin, never()).changeStateTo(PluginState.DISABLED);
        verify(pluginDao, never()).save(plugin);
        assertFalse(pluginOperationResult.isSuccess());
        assertEquals(PluginOperationStatus.ENABLED_DEPENDENCIES, pluginOperationResult.getStatus());
        assertEquals(1, pluginOperationResult.getPluginDependencyResult().getEnabledDependencies().size());
        assertEquals(1, pluginOperationResult.getPluginDependencyResult().getEnabledDependencies().size());
        assertTrue(pluginOperationResult.getPluginDependencyResult().getEnabledDependencies()
                .contains(new PluginDependencyInformation("unknownplugin", null, false, null, false)));
    }

    @Test
    public void shouldUninstallNotTemporaryPlugin() throws Exception {
        // given
        given(plugin.hasState(PluginState.TEMPORARY)).willReturn(false);
        given(plugin.hasState(PluginState.ENABLED)).willReturn(true);

        PluginDependencyResult pluginDependencyResult = PluginDependencyResult.satisfiedDependencies();
        given(pluginDependencyManager.getDependenciesToDisable(singletonList(plugin))).willReturn(pluginDependencyResult);
        given(pluginDependencyManager.sortPluginsInDependencyOrder(singletonList(plugin))).willReturn(singletonList(plugin));

        given(plugin.getFilename()).willReturn("filename");

        // when
        PluginOperationResult pluginOperationResult = pluginManager.uninstallPlugin("pluginname");

        // then
        verify(plugin).changeStateTo(PluginState.DISABLED);
        verify(pluginDao).delete(plugin);
        verify(pluginServerManager).restart();
        verify(pluginFileManager).uninstallPlugin("filename");
        assertTrue(pluginOperationResult.isSuccess());
        assertEquals(PluginOperationStatus.SUCCESS_WITH_RESTART, pluginOperationResult.getStatus());
        assertEquals(0, pluginOperationResult.getPluginDependencyResult().getEnabledDependencies().size());
    }

    @Test
    public void shouldUninstallTemporaryPlugin() throws Exception {
        // given
        given(plugin.hasState(PluginState.TEMPORARY)).willReturn(true);

        PluginDependencyResult pluginDependencyResult = PluginDependencyResult.satisfiedDependencies();
        given(pluginDependencyManager.getDependenciesToDisable(singletonList(plugin))).willReturn(pluginDependencyResult);
        given(pluginDependencyManager.sortPluginsInDependencyOrder(singletonList(plugin))).willReturn(singletonList(plugin));

        given(plugin.getFilename()).willReturn("filename");

        // when
        PluginOperationResult pluginOperationResult = pluginManager.uninstallPlugin("pluginname");

        // then
        verify(plugin, never()).changeStateTo(PluginState.DISABLED);
        verify(pluginDao).delete(plugin);
        verify(pluginServerManager, never()).restart();
        verify(pluginFileManager).uninstallPlugin("filename");
        assertTrue(pluginOperationResult.isSuccess());
        assertEquals(PluginOperationStatus.SUCCESS, pluginOperationResult.getStatus());
        assertEquals(0, pluginOperationResult.getPluginDependencyResult().getEnabledDependencies().size());
    }

    @Test
    public void shouldNotUninstallPluginWithEnabledDependencies() throws Exception {
        // given
        PluginDependencyResult pluginDependencyResult = PluginDependencyResult.enabledDependencies(Collections
                .singleton(new PluginDependencyInformation("unknownplugin", null, false, null, false)));
        given(pluginDependencyManager.getDependenciesToDisable(singletonList(plugin))).willReturn(pluginDependencyResult);

        given(plugin.getFilename()).willReturn("filename");

        // when
        PluginOperationResult pluginOperationResult = pluginManager.uninstallPlugin("pluginname");

        // then
        verify(plugin, never()).changeStateTo(PluginState.DISABLED);
        verify(pluginDao, never()).delete(plugin);
        verify(pluginServerManager, never()).restart();
        verify(pluginFileManager, never()).uninstallPlugin("filename");
        assertFalse(pluginOperationResult.isSuccess());
        assertEquals(PluginOperationStatus.ENABLED_DEPENDENCIES, pluginOperationResult.getStatus());
        assertEquals(1, pluginOperationResult.getPluginDependencyResult().getEnabledDependencies().size());
        assertEquals(1, pluginOperationResult.getPluginDependencyResult().getEnabledDependencies().size());
        assertTrue(pluginOperationResult.getPluginDependencyResult().getEnabledDependencies()
                .contains(new PluginDependencyInformation("unknownplugin", null, false, null, false)));
    }

    @Test
    public void shouldNotUninstallSystemPlugin() throws Exception {
        // given
        given(plugin.hasState(PluginState.TEMPORARY)).willReturn(false);
        given(plugin.isSystemPlugin()).willReturn(true);

        PluginDependencyResult pluginDependencyResult = PluginDependencyResult.satisfiedDependencies();
        given(pluginDependencyManager.getDependenciesToDisable(newArrayList(plugin))).willReturn(pluginDependencyResult);

        // when
        PluginOperationResult pluginOperationResult = pluginManager.uninstallPlugin("pluginname");

        // then
        verify(plugin, never()).changeStateTo(PluginState.DISABLED);
        verify(pluginDao, never()).delete(plugin);
        verify(pluginServerManager, never()).restart();
        assertFalse(pluginOperationResult.isSuccess());
        assertEquals(PluginOperationStatus.SYSTEM_PLUGIN_UNINSTALLING, pluginOperationResult.getStatus());
        assertEquals(0, pluginOperationResult.getPluginDependencyResult().getEnabledDependencies().size());
    }

    @Test
    public void shouldUninstallMultiplePlugins() throws Exception {
        // given
        given(plugin.hasState(PluginState.TEMPORARY)).willReturn(false);
        given(plugin.hasState(PluginState.ENABLED)).willReturn(true);
        given(plugin.getFilename()).willReturn("filename");

        given(anotherPlugin.hasState(PluginState.TEMPORARY)).willReturn(true);
        given(anotherPlugin.hasState(PluginState.ENABLED)).willReturn(false);
        given(anotherPlugin.getFilename()).willReturn("anotherFilename");

        PluginDependencyResult pluginDependencyResult = PluginDependencyResult.satisfiedDependencies();
        given(pluginDependencyManager.getDependenciesToDisable(newArrayList(plugin, anotherPlugin))).willReturn(
                pluginDependencyResult);
        given(pluginDependencyManager.sortPluginsInDependencyOrder(newArrayList(plugin, anotherPlugin))).willReturn(
                newArrayList(plugin, anotherPlugin));

        // when
        PluginOperationResult pluginOperationResult = pluginManager.uninstallPlugin("pluginname", "anotherPluginname");

        // then
        verify(plugin).changeStateTo(PluginState.DISABLED);
        verify(anotherPlugin, never()).changeStateTo(PluginState.DISABLED);
        verify(pluginDao).delete(plugin);
        verify(pluginDao).delete(anotherPlugin);
        verify(pluginFileManager).uninstallPlugin("filename", "anotherFilename");
        verify(pluginServerManager).restart();
        assertTrue(pluginOperationResult.isSuccess());
        assertEquals(PluginOperationStatus.SUCCESS_WITH_RESTART, pluginOperationResult.getStatus());
        assertEquals(0, pluginOperationResult.getPluginDependencyResult().getEnabledDependencies().size());
    }

}
