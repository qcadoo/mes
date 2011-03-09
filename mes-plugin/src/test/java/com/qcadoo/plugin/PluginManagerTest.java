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

import java.io.File;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;


public class PluginManagerTest {

    private Plugin plugin = mock(Plugin.class);

    private Plugin anotherPlugin = mock(Plugin.class);

    private PluginAccessor pluginAccessor = mock(PluginAccessor.class);

    private PluginDao pluginDao = mock(PluginDao.class);

    private PluginDependencyManager pluginDependencyManager = mock(PluginDependencyManager.class);

    private PluginFileManager pluginFileManager = mock(PluginFileManager.class);

    private PluginServerManager pluginServerManager = mock(PluginServerManager.class);

    private PluginInformation pluginInformation = mock(PluginInformation.class);

    private PluginArtifact pluginArtifact = mock(PluginArtifact.class);

    private PluginDescriptorParser pluginDescriptorParser = mock(PluginDescriptorParser.class);

    private File file = mock(File.class);

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
                .singletonList(pluginInformation));
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
        assertEquals("unknownplugin", pluginOperationResult.getPluginDependencyResult().getDisabledDependencies().get(0)
                .getName());
    }

    @Test
    public void shouldNotEnablePluginWithUnsatisfiedDependencies() throws Exception {
        // given
        given(plugin.hasState(PluginState.DISABLED)).willReturn(true);

        PluginDependencyResult pluginDependencyResult = PluginDependencyResult
                .unsatisfiedDependencies(singletonList(pluginInformation));
        given(pluginDependencyManager.getDependenciesToEnable(singletonList(plugin))).willReturn(pluginDependencyResult);

        // when
        PluginOperationResult pluginOperationResult = pluginManager.enablePlugin("pluginname");

        // then
        verify(plugin, never()).changeStateTo(PluginState.ENABLED);
        verify(pluginDao, never()).save(plugin);
        assertFalse(pluginOperationResult.isSuccess());
        assertEquals(PluginOperationStatus.UNSATISFIED_DEPENDENCIES, pluginOperationResult.getStatus());
        assertEquals(1, pluginOperationResult.getPluginDependencyResult().getUnsatisfiedDependencies().size());
        assertEquals("unknownplugin", pluginOperationResult.getPluginDependencyResult().getUnsatisfiedDependencies().get(0)
                .getName());
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

        PluginDependencyResult pluginDependencyResult = PluginDependencyResult
                .enabledDependencies(singletonList(pluginInformation));
        given(pluginDependencyManager.getDependenciesToDisable(singletonList(plugin))).willReturn(pluginDependencyResult);

        // when
        PluginOperationResult pluginOperationResult = pluginManager.disablePlugin("pluginname");

        // then
        verify(plugin, never()).changeStateTo(PluginState.DISABLED);
        verify(pluginDao, never()).save(plugin);
        assertFalse(pluginOperationResult.isSuccess());
        assertEquals(PluginOperationStatus.ENABLED_DEPENDENCIES, pluginOperationResult.getStatus());
        assertEquals(1, pluginOperationResult.getPluginDependencyResult().getEnabledDependencies().size());
        assertEquals("unknownplugin", pluginOperationResult.getPluginDependencyResult().getEnabledDependencies().get(0).getName());
    }

    @Test
    public void shouldInstallPlugin() throws Exception {
        // given
        given(pluginDescriptorParser.parse(file)).willReturn(plugin);
        given(pluginFileManager.uploadPlugin(pluginArtifact)).willReturn(file);

        PluginDependencyResult pluginDependencyResult = PluginDependencyResult.satisfiedDependencies();
        given(pluginDependencyManager.getDependenciesToEnable(newArrayList(plugin))).willReturn(pluginDependencyResult);

        // when
        PluginOperationResult pluginOperationResult = pluginManager.installPlugin(pluginArtifact);

        // then
        verify(pluginDao).save(plugin);
        assertTrue(pluginOperationResult.isSuccess());
        assertEquals(PluginOperationStatus.SUCCESS, pluginOperationResult.getStatus());
        assertEquals(0, pluginOperationResult.getPluginDependencyResult().getDisabledDependencies().size());
        assertEquals(0, pluginOperationResult.getPluginDependencyResult().getUnsatisfiedDependencies().size());
    }

    @Test
    public void shouldInstallPluginAndNotifyAboutMissingDependencies() throws Exception {
        // given
        given(pluginDescriptorParser.parse(file)).willReturn(plugin);
        given(pluginFileManager.uploadPlugin(pluginArtifact)).willReturn(file);

        PluginDependencyResult pluginDependencyResult = PluginDependencyResult
                .unsatisfiedDependencies(singletonList(pluginInformation));
        given(pluginDependencyManager.getDependenciesToEnable(newArrayList(plugin))).willReturn(pluginDependencyResult);

        // when
        PluginOperationResult pluginOperationResult = pluginManager.installPlugin(pluginArtifact);

        // then
        verify(pluginDao).save(plugin);
        assertTrue(pluginOperationResult.isSuccess());
        assertEquals(PluginOperationStatus.SUCCESS_WITH_MISSING_DEPENDENCIES, pluginOperationResult.getStatus());
        assertEquals(0, pluginOperationResult.getPluginDependencyResult().getDisabledDependencies().size());
        assertEquals(1, pluginOperationResult.getPluginDependencyResult().getUnsatisfiedDependencies().size());
        assertEquals("unknownplugin", pluginOperationResult.getPluginDependencyResult().getUnsatisfiedDependencies().get(0)
                .getName());
    }

    @Test
    public void shouldFailureWithCorruptedPluginOnInstall() throws Exception {
        // given
        given(pluginDescriptorParser.parse(file)).willThrow(new PluginException());
        given(pluginFileManager.uploadPlugin(pluginArtifact)).willReturn(file);
        given(file.getName()).willReturn("filename");

        // when
        PluginOperationResult pluginOperationResult = pluginManager.installPlugin(pluginArtifact);

        // then
        verify(pluginDao, never()).save(Mockito.any(Plugin.class));
        verify(pluginFileManager).removePlugin("filename");
        assertFalse(pluginOperationResult.isSuccess());
        assertEquals(PluginOperationStatus.CORRUPTED_PLUGIN, pluginOperationResult.getStatus());
    }

    @Test
    public void shouldFailureOnUploadingPluginOnInstall() throws Exception {
        // given
        given(pluginFileManager.uploadPlugin(pluginArtifact)).willThrow(new PluginException());

        // when
        PluginOperationResult pluginOperationResult = pluginManager.installPlugin(pluginArtifact);

        // then
        verify(pluginDao, never()).save(Mockito.any(Plugin.class));
        assertFalse(pluginOperationResult.isSuccess());
        assertEquals(PluginOperationStatus.CANNOT_UPLOAD_PLUGIN, pluginOperationResult.getStatus());
    }

    @Test
    public void shouldUninstallNotTemporaryPlugin() throws Exception {
        // given
        given(plugin.hasState(PluginState.TEMPORARY)).willReturn(false);

        PluginDependencyResult pluginDependencyResult = PluginDependencyResult.satisfiedDependencies();
        given(pluginDependencyManager.getDependenciesToDisable(singletonList(plugin))).willReturn(pluginDependencyResult);

        given(plugin.getFilename()).willReturn("filename");
        given(pluginFileManager.uninstallPlugin("filename")).willReturn(true);

        // when
        PluginOperationResult pluginOperationResult = pluginManager.uninstallPlugin("pluginname");

        // then
        verify(pluginDao).delete(plugin);
        verify(pluginServerManager).restart();
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

        given(plugin.getFilename()).willReturn("filename");
        given(pluginFileManager.uninstallPlugin("filename")).willReturn(true);

        // when
        PluginOperationResult pluginOperationResult = pluginManager.uninstallPlugin("pluginname");

        // then
        verify(pluginDao).delete(plugin);
        verify(pluginServerManager, never()).restart();
        assertTrue(pluginOperationResult.isSuccess());
        assertEquals(PluginOperationStatus.SUCCESS, pluginOperationResult.getStatus());
        assertEquals(0, pluginOperationResult.getPluginDependencyResult().getEnabledDependencies().size());
    }

    @Test
    public void shouldNotUninstallPluginWithEnabledDependencies() throws Exception {
        // given
        PluginDependencyResult pluginDependencyResult = PluginDependencyResult
                .enabledDependencies(singletonList(pluginInformation));
        given(pluginDependencyManager.getDependenciesToDisable(singletonList(plugin))).willReturn(pluginDependencyResult);

        given(plugin.getFilename()).willReturn("filename");
        given(pluginFileManager.uninstallPlugin("filename")).willReturn(true);

        // when
        PluginOperationResult pluginOperationResult = pluginManager.uninstallPlugin("pluginname");

        // then
        verify(pluginDao, never()).delete(plugin);
        verify(pluginServerManager, never()).restart();
        assertFalse(pluginOperationResult.isSuccess());
        assertEquals(PluginOperationStatus.ENABLED_DEPENDENCIES, pluginOperationResult.getStatus());
        assertEquals(1, pluginOperationResult.getPluginDependencyResult().getEnabledDependencies().size());
        assertEquals("unknownplugin", pluginOperationResult.getPluginDependencyResult().getEnabledDependencies().get(0).getName());
    }

    @Test
    public void shouldNotUninstallPluginIfCannotUninstall() throws Exception {
        // given
        given(anotherPlugin.hasState(PluginState.TEMPORARY)).willReturn(false);
        given(anotherPlugin.getFilename()).willReturn("filename");
        given(pluginFileManager.uninstallPlugin("filename")).willReturn(false);

        PluginDependencyResult pluginDependencyResult = PluginDependencyResult.satisfiedDependencies();
        given(pluginDependencyManager.getDependenciesToDisable(singletonList(anotherPlugin))).willReturn(pluginDependencyResult);

        // when
        PluginOperationResult pluginOperationResult = pluginManager.uninstallPlugin("anotherPluginname");

        // then
        verify(pluginDao, never()).delete(anotherPlugin);
        verify(pluginServerManager, never()).restart();
        assertFalse(pluginOperationResult.isSuccess());
        assertEquals(PluginOperationStatus.CANNOT_UNINSTALL_PLUGIN, pluginOperationResult.getStatus());
        assertEquals(0, pluginOperationResult.getPluginDependencyResult().getEnabledDependencies().size());
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
        given(plugin.getFilename()).willReturn("filename");

        given(anotherPlugin.hasState(PluginState.TEMPORARY)).willReturn(true);
        given(anotherPlugin.getFilename()).willReturn("anotherFilename");
        given(pluginFileManager.uninstallPlugin(new String[] { "filename", "anotherFilename" })).willReturn(true);

        PluginDependencyResult pluginDependencyResult = PluginDependencyResult.satisfiedDependencies();
        given(pluginDependencyManager.getDependenciesToDisable(newArrayList(plugin, anotherPlugin))).willReturn(
                pluginDependencyResult);

        // when
        PluginOperationResult pluginOperationResult = pluginManager.uninstallPlugin("pluginname", "anotherPluginname");

        // then
        verify(pluginDao).delete(plugin);
        verify(pluginDao).delete(anotherPlugin);
        verify(pluginServerManager).restart();
        assertTrue(pluginOperationResult.isSuccess());
        assertEquals(PluginOperationStatus.SUCCESS_WITH_RESTART, pluginOperationResult.getStatus());
        assertEquals(0, pluginOperationResult.getPluginDependencyResult().getEnabledDependencies().size());
    }

}
