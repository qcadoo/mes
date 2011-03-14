package com.qcadoo.plugin.internal.manager;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

import com.qcadoo.plugin.api.Plugin;
import com.qcadoo.plugin.api.PluginAccessor;
import com.qcadoo.plugin.api.PluginDependencyInformation;
import com.qcadoo.plugin.api.PluginState;
import com.qcadoo.plugin.api.VersionOfDependency;
import com.qcadoo.plugin.internal.api.PluginDao;
import com.qcadoo.plugin.internal.api.PluginDependencyManager;
import com.qcadoo.plugin.internal.api.PluginDescriptorParser;
import com.qcadoo.plugin.internal.api.PluginFileManager;
import com.qcadoo.plugin.internal.api.PluginOperationResult;
import com.qcadoo.plugin.internal.api.PluginOperationStatus;
import com.qcadoo.plugin.internal.api.PluginServerManager;
import com.qcadoo.plugin.internal.dependencymanager.PluginDependencyResult;

public class PluginManagerTest {

    private final Plugin plugin = mock(Plugin.class);

    private final Plugin anotherPlugin = mock(Plugin.class);

    private final PluginAccessor pluginAccessor = mock(PluginAccessor.class);

    private final PluginDao pluginDao = mock(PluginDao.class);

    private final PluginDependencyManager pluginDependencyManager = mock(PluginDependencyManager.class);

    private final PluginFileManager pluginFileManager = mock(PluginFileManager.class);

    private final PluginServerManager pluginServerManager = mock(PluginServerManager.class);

    private final PluginDescriptorParser pluginDescriptorParser = mock(PluginDescriptorParser.class);

    private DefaultPluginManager pluginManager;

    @Before
    public void init() {
        given(pluginAccessor.getPlugin("pluginname")).willReturn(plugin);
        given(pluginAccessor.getPlugin("anotherPluginname")).willReturn(anotherPlugin);

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
        assertEquals(0, pluginOperationResult.getPluginDependencyResult().getDependenciesToEnable().size());
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
        assertEquals(0, pluginOperationResult.getPluginDependencyResult().getDependenciesToEnable().size());
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
        assertEquals(0, pluginOperationResult.getPluginDependencyResult().getDependenciesToEnable().size());
        assertEquals(0, pluginOperationResult.getPluginDependencyResult().getUnsatisfiedDependencies().size());
    }

    @Test
    public void shouldNotEnablePluginWithDisabledDependencies() throws Exception {
        // given
        given(plugin.hasState(PluginState.DISABLED)).willReturn(true);

        PluginDependencyResult pluginDependencyResult = PluginDependencyResult.dependenciesToEnable(Collections
                .singleton(new PluginDependencyInformation("unknownplugin", new VersionOfDependency(""))));
        given(pluginDependencyManager.getDependenciesToEnable(singletonList(plugin))).willReturn(pluginDependencyResult);

        // when
        PluginOperationResult pluginOperationResult = pluginManager.enablePlugin("pluginname");

        // then
        verify(plugin, never()).changeStateTo(PluginState.ENABLED);
        verify(pluginDao, never()).save(plugin);
        assertFalse(pluginOperationResult.isSuccess());
        assertEquals(PluginOperationStatus.DISABLED_DEPENDENCIES, pluginOperationResult.getStatus());
        assertEquals(0, pluginOperationResult.getPluginDependencyResult().getUnsatisfiedDependencies().size());
        assertEquals(1, pluginOperationResult.getPluginDependencyResult().getDependenciesToEnable().size());
        assertEquals(1, pluginOperationResult.getPluginDependencyResult().getDependenciesToEnable().size());
        assertTrue(pluginOperationResult.getPluginDependencyResult().getDependenciesToEnable()
                .contains(new PluginDependencyInformation("unknownplugin", new VersionOfDependency(""))));
    }

    @Test
    public void shouldNotEnablePluginWithUnsatisfiedDependencies() throws Exception {
        // given
        given(plugin.hasState(PluginState.DISABLED)).willReturn(true);

        PluginDependencyResult pluginDependencyResult = PluginDependencyResult.unsatisfiedDependencies(Collections
                .singleton(new PluginDependencyInformation("unknownplugin", new VersionOfDependency(""))));
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
                .contains(new PluginDependencyInformation("unknownplugin", new VersionOfDependency(""))));
    }

    @Test
    public void shouldEnableMultiplePlugins() throws Exception {
        // given
        given(plugin.hasState(PluginState.DISABLED)).willReturn(true);

        Plugin nextPlugin = mock(Plugin.class, "nextPlugin");
        given(nextPlugin.hasState(PluginState.DISABLED)).willReturn(true);
        given(pluginAccessor.getPlugin("nextPluginname")).willReturn(nextPlugin);

        given(anotherPlugin.hasState(PluginState.TEMPORARY)).willReturn(true);
        given(anotherPlugin.getFilename()).willReturn("filename");
        given(pluginFileManager.installPlugin("filename")).willReturn(true);

        PluginDependencyResult pluginDependencyResult = PluginDependencyResult.satisfiedDependencies();
        given(pluginDependencyManager.getDependenciesToEnable(newArrayList(plugin, anotherPlugin, nextPlugin))).willReturn(
                pluginDependencyResult);
        given(pluginDependencyManager.sortPluginsInDependencyOrder(newArrayList(plugin, anotherPlugin, nextPlugin))).willReturn(
                newArrayList(plugin, anotherPlugin, nextPlugin));

        // when
        PluginOperationResult pluginOperationResult = pluginManager.enablePlugin("pluginname", "anotherPluginname",
                "nextPluginname");

        // then
        InOrder inOrder = inOrder(plugin, anotherPlugin, nextPlugin);
        inOrder.verify(plugin).changeStateTo(PluginState.ENABLED);
        inOrder.verify(anotherPlugin).changeStateTo(PluginState.ENABLING);
        inOrder.verify(nextPlugin).changeStateTo(PluginState.ENABLED);
        verify(pluginDao).save(plugin);
        verify(pluginDao).save(nextPlugin);
        verify(pluginDao).save(anotherPlugin);
        assertTrue(pluginOperationResult.isSuccess());
        assertEquals(PluginOperationStatus.SUCCESS_WITH_RESTART, pluginOperationResult.getStatus());
        assertEquals(0, pluginOperationResult.getPluginDependencyResult().getDependenciesToEnable().size());
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
        assertEquals(0, pluginOperationResult.getPluginDependencyResult().getDependenciesToDisable().size());
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
        assertEquals(0, pluginOperationResult.getPluginDependencyResult().getDependenciesToDisable().size());
    }

    @Test
    public void shouldNotDisablePluginWithEnabledDependencies() throws Exception {
        // given
        given(plugin.hasState(PluginState.ENABLED)).willReturn(true);

        PluginDependencyResult pluginDependencyResult = PluginDependencyResult.dependenciesToDisable(Collections
                .singleton(new PluginDependencyInformation("unknownplugin", new VersionOfDependency(""))));
        given(pluginDependencyManager.getDependenciesToDisable(singletonList(plugin))).willReturn(pluginDependencyResult);

        // when
        PluginOperationResult pluginOperationResult = pluginManager.disablePlugin("pluginname");

        // then
        verify(plugin, never()).changeStateTo(PluginState.DISABLED);
        verify(pluginDao, never()).save(plugin);
        assertFalse(pluginOperationResult.isSuccess());
        assertEquals(PluginOperationStatus.ENABLED_DEPENDENCIES, pluginOperationResult.getStatus());
        assertEquals(1, pluginOperationResult.getPluginDependencyResult().getDependenciesToDisable().size());
        assertEquals(1, pluginOperationResult.getPluginDependencyResult().getDependenciesToDisable().size());
        assertTrue(pluginOperationResult.getPluginDependencyResult().getDependenciesToDisable()
                .contains(new PluginDependencyInformation("unknownplugin", new VersionOfDependency(""))));
    }

    @Test
    public void shouldUninstallNotTemporaryPlugin() throws Exception {
        // given
        given(plugin.hasState(PluginState.TEMPORARY)).willReturn(false);
        given(plugin.hasState(PluginState.ENABLED)).willReturn(true);

        PluginDependencyResult pluginDependencyResult = PluginDependencyResult.satisfiedDependencies();
        given(pluginDependencyManager.getDependenciesToUninstall(singletonList(plugin))).willReturn(pluginDependencyResult);
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
        assertEquals(0, pluginOperationResult.getPluginDependencyResult().getDependenciesToUninstall().size());
    }

    @Test
    public void shouldUninstallTemporaryPlugin() throws Exception {
        // given
        given(plugin.hasState(PluginState.TEMPORARY)).willReturn(true);

        PluginDependencyResult pluginDependencyResult = PluginDependencyResult.satisfiedDependencies();
        given(pluginDependencyManager.getDependenciesToUninstall(singletonList(plugin))).willReturn(pluginDependencyResult);
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
        assertEquals(0, pluginOperationResult.getPluginDependencyResult().getDependenciesToUninstall().size());
    }

    @Test
    public void shouldNotUninstallPluginWithEnabledDependencies() throws Exception {
        // given
        PluginDependencyResult pluginDependencyResult = PluginDependencyResult.dependenciesToUninstall(Collections
                .singleton(new PluginDependencyInformation("unknownplugin", new VersionOfDependency(""))));
        given(pluginDependencyManager.getDependenciesToUninstall(singletonList(plugin))).willReturn(pluginDependencyResult);

        given(plugin.getFilename()).willReturn("filename");

        // when
        PluginOperationResult pluginOperationResult = pluginManager.uninstallPlugin("pluginname");

        // then
        verify(plugin, never()).changeStateTo(PluginState.DISABLED);
        verify(pluginDao, never()).delete(plugin);
        verify(pluginServerManager, never()).restart();
        verify(pluginFileManager, never()).uninstallPlugin("filename");
        assertFalse(pluginOperationResult.isSuccess());
        assertEquals(PluginOperationStatus.DEPENDENCIES_TO_UNINSTALL, pluginOperationResult.getStatus());
        assertEquals(1, pluginOperationResult.getPluginDependencyResult().getDependenciesToUninstall().size());
        assertTrue(pluginOperationResult.getPluginDependencyResult().getDependenciesToUninstall()
                .contains(new PluginDependencyInformation("unknownplugin", new VersionOfDependency(""))));
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
        assertEquals(0, pluginOperationResult.getPluginDependencyResult().getDependenciesToUninstall().size());
    }

    @Test
    public void shouldDisableMultipleEnabledPlugin() throws Exception {
        // given
        given(plugin.hasState(PluginState.ENABLED)).willReturn(true);

        Plugin nextPlugin = mock(Plugin.class, "nextPlugin");
        given(nextPlugin.hasState(PluginState.ENABLED)).willReturn(true);
        given(pluginAccessor.getPlugin("nextPluginname")).willReturn(nextPlugin);

        PluginDependencyResult pluginDependencyResult = PluginDependencyResult.satisfiedDependencies();
        given(pluginDependencyManager.getDependenciesToDisable(newArrayList(plugin, nextPlugin))).willReturn(
                pluginDependencyResult);
        given(pluginDependencyManager.sortPluginsInDependencyOrder(newArrayList(plugin, nextPlugin))).willReturn(
                newArrayList(plugin, nextPlugin));

        // when
        PluginOperationResult pluginOperationResult = pluginManager.disablePlugin("pluginname", "nextPluginname");

        // then
        InOrder inOrder = inOrder(nextPlugin, plugin);
        inOrder.verify(nextPlugin).changeStateTo(PluginState.DISABLED);
        inOrder.verify(plugin).changeStateTo(PluginState.DISABLED);
        verify(pluginDao).save(nextPlugin);
        verify(pluginDao).save(plugin);
        assertTrue(pluginOperationResult.isSuccess());
        assertEquals(PluginOperationStatus.SUCCESS, pluginOperationResult.getStatus());
        assertEquals(0, pluginOperationResult.getPluginDependencyResult().getDependenciesToDisable().size());
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

        Plugin nextPlugin = mock(Plugin.class, "nextPlugin");
        given(nextPlugin.hasState(PluginState.TEMPORARY)).willReturn(false);
        given(nextPlugin.hasState(PluginState.ENABLED)).willReturn(true);
        given(nextPlugin.getFilename()).willReturn("nextPluginFilename");
        given(pluginAccessor.getPlugin("nextPluginname")).willReturn(nextPlugin);

        PluginDependencyResult pluginDependencyResult = PluginDependencyResult.satisfiedDependencies();
        given(pluginDependencyManager.getDependenciesToUninstall(newArrayList(plugin, anotherPlugin, nextPlugin))).willReturn(
                pluginDependencyResult);
        given(pluginDependencyManager.sortPluginsInDependencyOrder(newArrayList(plugin, anotherPlugin, nextPlugin))).willReturn(
                newArrayList(nextPlugin, plugin, anotherPlugin));

        // when
        PluginOperationResult pluginOperationResult = pluginManager.uninstallPlugin("pluginname", "anotherPluginname",
                "nextPluginname");

        // then
        InOrder inOrder = inOrder(plugin, nextPlugin);
        inOrder.verify(plugin).changeStateTo(PluginState.DISABLED);
        inOrder.verify(nextPlugin).changeStateTo(PluginState.DISABLED);
        verify(anotherPlugin, never()).changeStateTo(PluginState.DISABLED);
        verify(pluginDao).delete(plugin);
        verify(pluginDao).delete(nextPlugin);
        verify(pluginDao).delete(anotherPlugin);
        verify(pluginFileManager).uninstallPlugin("filename", "anotherFilename", "nextPluginFilename");
        verify(pluginServerManager).restart();
        assertTrue(pluginOperationResult.isSuccess());
        assertEquals(PluginOperationStatus.SUCCESS_WITH_RESTART, pluginOperationResult.getStatus());
        assertEquals(0, pluginOperationResult.getPluginDependencyResult().getDependenciesToUninstall().size());
    }

}
