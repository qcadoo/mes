package com.qcadoo.plugin.internal.manager;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.springframework.core.io.Resource;

import com.qcadoo.plugin.api.Plugin;
import com.qcadoo.plugin.api.PluginAccessor;
import com.qcadoo.plugin.api.PluginDependencyInformation;
import com.qcadoo.plugin.api.PluginState;
import com.qcadoo.plugin.api.Version;
import com.qcadoo.plugin.api.VersionOfDependency;
import com.qcadoo.plugin.internal.PluginException;
import com.qcadoo.plugin.internal.api.PluginArtifact;
import com.qcadoo.plugin.internal.api.PluginDao;
import com.qcadoo.plugin.internal.api.PluginDependencyManager;
import com.qcadoo.plugin.internal.api.PluginDescriptorParser;
import com.qcadoo.plugin.internal.api.PluginFileManager;
import com.qcadoo.plugin.internal.api.PluginOperationResult;
import com.qcadoo.plugin.internal.api.PluginOperationStatus;
import com.qcadoo.plugin.internal.api.PluginServerManager;
import com.qcadoo.plugin.internal.dependencymanager.PluginDependencyResult;

public class PluginManagerInstallTest {

    private final Plugin plugin = mock(Plugin.class);

    private final Plugin anotherPlugin = mock(Plugin.class);

    private final PluginAccessor pluginAccessor = mock(PluginAccessor.class);

    private final PluginDao pluginDao = mock(PluginDao.class);

    private final PluginDependencyManager pluginDependencyManager = mock(PluginDependencyManager.class);

    private final PluginFileManager pluginFileManager = mock(PluginFileManager.class);

    private final PluginServerManager pluginServerManager = mock(PluginServerManager.class);

    private final PluginDescriptorParser pluginDescriptorParser = mock(PluginDescriptorParser.class);

    private final PluginArtifact pluginArtifact = mock(PluginArtifact.class);

    private DefaultPluginManager pluginManager;

    private final Resource resource = mock(Resource.class, RETURNS_DEEP_STUBS);

    @Before
    public void init() {
        given(pluginAccessor.getPlugin("pluginname")).willReturn(plugin);

        given(anotherPlugin.getIdentifier()).willReturn("pluginname");
        given(anotherPlugin.getVersion()).willReturn(new Version("1.2.5"));
        given(plugin.getVersion()).willReturn(new Version("1.2.4"));

        pluginManager = new DefaultPluginManager();
        pluginManager.setPluginAccessor(pluginAccessor);
        pluginManager.setPluginDao(pluginDao);
        pluginManager.setPluginDependencyManager(pluginDependencyManager);
        pluginManager.setPluginFileManager(pluginFileManager);
        pluginManager.setPluginServerManager(pluginServerManager);
        pluginManager.setPluginDescriptorParser(pluginDescriptorParser);
    }

    @Test
    public void shouldInstallTemporaryPlugin() throws Exception {
        // given

        given(plugin.hasState(PluginState.TEMPORARY)).willReturn(true);
        given(plugin.getPluginState()).willReturn(PluginState.TEMPORARY);
        given(pluginDescriptorParser.parse(resource)).willReturn(anotherPlugin);
        given(pluginFileManager.uploadPlugin(pluginArtifact)).willReturn(resource);

        given(plugin.getFilename()).willReturn("filename");

        PluginDependencyResult pluginDependencyResult = PluginDependencyResult.satisfiedDependencies();
        given(pluginDependencyManager.getDependenciesToEnable(newArrayList(anotherPlugin))).willReturn(pluginDependencyResult);

        // when
        PluginOperationResult pluginOperationResult = pluginManager.installPlugin(pluginArtifact);

        // then
        verify(pluginDao).save(anotherPlugin);
        verify(pluginAccessor).savePlugin(anotherPlugin);
        verify(anotherPlugin).changeStateTo(plugin.getPluginState());
        verify(pluginFileManager).uninstallPlugin("filename");
        assertTrue(pluginOperationResult.isSuccess());
        assertEquals(PluginOperationStatus.SUCCESS, pluginOperationResult.getStatus());
        assertEquals(0, pluginOperationResult.getPluginDependencyResult().getUnsatisfiedDependencies().size());
    }

    @Test
    public void shouldFailureWithCorruptedPluginOnInstall() throws Exception {
        // given
        given(pluginDescriptorParser.parse(resource)).willThrow(new PluginException());
        given(pluginFileManager.uploadPlugin(pluginArtifact)).willReturn(resource);
        given(resource.getFile().getName()).willReturn("filename");

        // when
        PluginOperationResult pluginOperationResult = pluginManager.installPlugin(pluginArtifact);

        // then
        verify(pluginDao, never()).save(Mockito.any(Plugin.class));
        verify(pluginAccessor, never()).savePlugin(Mockito.any(Plugin.class));
        verify(pluginFileManager).uninstallPlugin("filename");
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
        verify(pluginAccessor, never()).savePlugin(Mockito.any(Plugin.class));
        assertFalse(pluginOperationResult.isSuccess());
        assertEquals(PluginOperationStatus.CANNOT_UPLOAD_PLUGIN, pluginOperationResult.getStatus());
    }

    @Test
    public void shouldInstallTemporaryPluginAndNotifyAboutMissingDependencies() throws Exception {
        // given

        given(plugin.hasState(PluginState.TEMPORARY)).willReturn(true);
        given(plugin.getPluginState()).willReturn(PluginState.TEMPORARY);
        given(pluginDescriptorParser.parse(resource)).willReturn(anotherPlugin);
        given(pluginFileManager.uploadPlugin(pluginArtifact)).willReturn(resource);

        given(plugin.getFilename()).willReturn("filename");

        PluginDependencyResult pluginDependencyResult = PluginDependencyResult.unsatisfiedDependencies(Collections
                .singleton(new PluginDependencyInformation("unknownplugin", new VersionOfDependency(null))));
        given(pluginDependencyManager.getDependenciesToEnable(newArrayList(anotherPlugin))).willReturn(pluginDependencyResult);

        // when
        PluginOperationResult pluginOperationResult = pluginManager.installPlugin(pluginArtifact);

        // then
        verify(pluginDao).save(anotherPlugin);
        verify(pluginAccessor).savePlugin(anotherPlugin);
        verify(anotherPlugin).changeStateTo(plugin.getPluginState());
        verify(pluginFileManager).uninstallPlugin("filename");
        assertTrue(pluginOperationResult.isSuccess());
        assertEquals(PluginOperationStatus.SUCCESS_WITH_MISSING_DEPENDENCIES, pluginOperationResult.getStatus());
        assertEquals(1, pluginOperationResult.getPluginDependencyResult().getUnsatisfiedDependencies().size());
        assertEquals(1, pluginOperationResult.getPluginDependencyResult().getUnsatisfiedDependencies().size());
        assertTrue(pluginOperationResult.getPluginDependencyResult().getUnsatisfiedDependencies()
                .contains(new PluginDependencyInformation("unknownplugin", new VersionOfDependency(""))));
    }

    @Test
    public void shouldInstallDisabledPlugin() throws Exception {
        // given

        given(plugin.hasState(PluginState.DISABLED)).willReturn(true);
        given(plugin.getPluginState()).willReturn(PluginState.DISABLED);
        given(pluginDescriptorParser.parse(resource)).willReturn(anotherPlugin);
        given(pluginFileManager.uploadPlugin(pluginArtifact)).willReturn(resource);

        given(plugin.getFilename()).willReturn("filename");
        given(anotherPlugin.getFilename()).willReturn("anotherFilename");
        given(pluginFileManager.installPlugin("anotherFilename")).willReturn(true);

        PluginDependencyResult pluginDependencyResult = PluginDependencyResult.satisfiedDependencies();
        given(pluginDependencyManager.getDependenciesToEnable(newArrayList(anotherPlugin))).willReturn(pluginDependencyResult);

        // when
        PluginOperationResult pluginOperationResult = pluginManager.installPlugin(pluginArtifact);

        // then
        verify(pluginDao).save(anotherPlugin);
        verify(pluginAccessor).savePlugin(anotherPlugin);
        verify(anotherPlugin).changeStateTo(plugin.getPluginState());
        verify(pluginFileManager).uninstallPlugin("filename");
        verify(pluginServerManager).restart();
        assertTrue(pluginOperationResult.isSuccess());
        assertEquals(PluginOperationStatus.SUCCESS_WITH_RESTART, pluginOperationResult.getStatus());
        assertEquals(0, pluginOperationResult.getPluginDependencyResult().getUnsatisfiedDependencies().size());
    }

    @Test
    public void shouldFailureInstallDisabledPluginWithMissingDependencies() throws Exception {
        // given

        given(plugin.hasState(PluginState.DISABLED)).willReturn(true);
        given(pluginDescriptorParser.parse(resource)).willReturn(anotherPlugin);
        given(pluginFileManager.uploadPlugin(pluginArtifact)).willReturn(resource);

        given(anotherPlugin.getFilename()).willReturn("filename");

        PluginDependencyResult pluginDependencyResult = PluginDependencyResult.unsatisfiedDependencies(Collections
                .singleton(new PluginDependencyInformation("unknownplugin", new VersionOfDependency(""))));
        given(pluginDependencyManager.getDependenciesToEnable(newArrayList(anotherPlugin))).willReturn(pluginDependencyResult);

        // when
        PluginOperationResult pluginOperationResult = pluginManager.installPlugin(pluginArtifact);

        // then
        verify(pluginDao, never()).save(anotherPlugin);
        verify(pluginAccessor, never()).savePlugin(anotherPlugin);
        verify(anotherPlugin, never()).changeStateTo(plugin.getPluginState());
        verify(pluginFileManager).uninstallPlugin("filename");
        assertFalse(pluginOperationResult.isSuccess());
        assertEquals(PluginOperationStatus.UNSATISFIED_DEPENDENCIES, pluginOperationResult.getStatus());
        assertEquals(1, pluginOperationResult.getPluginDependencyResult().getUnsatisfiedDependencies().size());
        assertEquals(1, pluginOperationResult.getPluginDependencyResult().getUnsatisfiedDependencies().size());
        assertTrue(pluginOperationResult.getPluginDependencyResult().getUnsatisfiedDependencies()
                .contains(new PluginDependencyInformation("unknownplugin", new VersionOfDependency(""))));
    }

    @Test
    public void shouldInstallEnabledPlugin() throws Exception {
        // given
        given(plugin.hasState(PluginState.ENABLED)).willReturn(true);
        given(plugin.getPluginState()).willReturn(PluginState.ENABLED);
        given(pluginDescriptorParser.parse(resource)).willReturn(anotherPlugin);
        given(pluginFileManager.uploadPlugin(pluginArtifact)).willReturn(resource);

        given(pluginFileManager.installPlugin("anotherFilename")).willReturn(true);
        given(anotherPlugin.getFilename()).willReturn("anotherFilename");
        given(plugin.getFilename()).willReturn("filename");

        PluginDependencyResult pluginDependencyResult = PluginDependencyResult.satisfiedDependencies();
        given(pluginDependencyManager.getDependenciesToEnable(newArrayList(anotherPlugin))).willReturn(pluginDependencyResult);

        Plugin dependencyPlugin = mock(Plugin.class);
        given(pluginAccessor.getPlugin("dependencyplugin")).willReturn(dependencyPlugin);
        given(dependencyPlugin.getIdentifier()).willReturn("dependencyplugin");

        Plugin dependencyPlugin2 = mock(Plugin.class);
        given(pluginAccessor.getPlugin("dependencyplugin2")).willReturn(dependencyPlugin2);
        given(dependencyPlugin2.getIdentifier()).willReturn("dependencyplugin2");

        PluginDependencyResult installPluginDependencyResult = PluginDependencyResult.dependenciesToDisable(newHashSet(
                new PluginDependencyInformation("dependencyplugin", new VersionOfDependency("")),
                new PluginDependencyInformation("dependencyplugin2", new VersionOfDependency(""))));

        given(pluginDependencyManager.getDependenciesToUpdate(plugin, anotherPlugin)).willReturn(installPluginDependencyResult);
        given(pluginDependencyManager.sortPluginsInDependencyOrder(newArrayList(dependencyPlugin2, dependencyPlugin)))
                .willReturn(newArrayList(dependencyPlugin, dependencyPlugin2));

        // when
        PluginOperationResult pluginOperationResult = pluginManager.installPlugin(pluginArtifact);

        // then
        InOrder inOrderDisable = inOrder(dependencyPlugin2, dependencyPlugin, plugin);
        inOrderDisable.verify(dependencyPlugin2).changeStateTo(PluginState.DISABLED);
        inOrderDisable.verify(dependencyPlugin).changeStateTo(PluginState.DISABLED);
        inOrderDisable.verify(plugin).changeStateTo(PluginState.DISABLED);
        InOrder inOrderEnabling = inOrder(anotherPlugin, dependencyPlugin, dependencyPlugin2);
        inOrderEnabling.verify(anotherPlugin).changeStateTo(PluginState.ENABLING);
        inOrderEnabling.verify(dependencyPlugin).changeStateTo(PluginState.ENABLING);
        inOrderEnabling.verify(dependencyPlugin2).changeStateTo(PluginState.ENABLING);
        verify(pluginDao).save(anotherPlugin);
        verify(pluginAccessor).savePlugin(anotherPlugin);
        verify(pluginDao).save(dependencyPlugin);
        verify(pluginDao).save(dependencyPlugin2);
        verify(pluginFileManager).uninstallPlugin("filename");
        verify(pluginServerManager).restart();
        assertTrue(pluginOperationResult.isSuccess());
        assertEquals(PluginOperationStatus.SUCCESS_WITH_RESTART, pluginOperationResult.getStatus());
        assertEquals(0, pluginOperationResult.getPluginDependencyResult().getDependenciesToEnable().size());
        assertEquals(0, pluginOperationResult.getPluginDependencyResult().getUnsatisfiedDependencies().size());
    }

    @Test
    public void shouldFailureInstallEnabledPluginWithUnsatisfiedDependenciesAfterUpdate() throws Exception {
        // given
        given(plugin.hasState(PluginState.ENABLED)).willReturn(true);
        given(plugin.getPluginState()).willReturn(PluginState.ENABLED);
        given(pluginDescriptorParser.parse(resource)).willReturn(anotherPlugin);
        given(pluginFileManager.uploadPlugin(pluginArtifact)).willReturn(resource);

        given(pluginFileManager.installPlugin("anotherFilename")).willReturn(true);
        given(anotherPlugin.getFilename()).willReturn("anotherFilename");
        given(plugin.getFilename()).willReturn("filename");

        PluginDependencyResult pluginDependencyResult = PluginDependencyResult.satisfiedDependencies();
        given(pluginDependencyManager.getDependenciesToEnable(newArrayList(anotherPlugin))).willReturn(pluginDependencyResult);

        PluginDependencyResult installPluginDependencyResult = PluginDependencyResult.dependenciesToUpdate(Collections
                .<PluginDependencyInformation> emptySet(), newHashSet(new PluginDependencyInformation("dependencyplugin",
                new VersionOfDependency(""))));

        given(pluginDependencyManager.getDependenciesToUpdate(plugin, anotherPlugin)).willReturn(installPluginDependencyResult);

        // when
        PluginOperationResult pluginOperationResult = pluginManager.installPlugin(pluginArtifact);

        // then
        verify(pluginDao, never()).save(anotherPlugin);
        verify(pluginAccessor, never()).savePlugin(anotherPlugin);
        verify(anotherPlugin, never()).changeStateTo(plugin.getPluginState());
        verify(pluginFileManager).uninstallPlugin("anotherFilename");
        assertFalse(pluginOperationResult.isSuccess());
        assertEquals(PluginOperationStatus.UNSATISFIED_DEPENDENCIES_AFTER_UPDATE, pluginOperationResult.getStatus());
        assertEquals(1, pluginOperationResult.getPluginDependencyResult().getDependenciesToDisableUnsatisfiedAfterUpdate().size());
        assertTrue(pluginOperationResult.getPluginDependencyResult().getDependenciesToDisableUnsatisfiedAfterUpdate()
                .contains(new PluginDependencyInformation("dependencyplugin", new VersionOfDependency(""))));
    }

    @Test
    public void shouldFailureInstallEnabledPluginWithUnsitisfiedDependencies() throws Exception {
        // given

        given(plugin.hasState(PluginState.ENABLED)).willReturn(true);
        given(pluginDescriptorParser.parse(resource)).willReturn(anotherPlugin);
        given(pluginFileManager.uploadPlugin(pluginArtifact)).willReturn(resource);

        given(anotherPlugin.getFilename()).willReturn("filename");

        PluginDependencyResult pluginDependencyResult = PluginDependencyResult.unsatisfiedDependencies(Collections
                .singleton(new PluginDependencyInformation("unknownplugin", new VersionOfDependency(""))));
        given(pluginDependencyManager.getDependenciesToEnable(newArrayList(anotherPlugin))).willReturn(pluginDependencyResult);

        // when
        PluginOperationResult pluginOperationResult = pluginManager.installPlugin(pluginArtifact);

        // then
        verify(pluginDao, never()).save(anotherPlugin);
        verify(pluginAccessor, never()).savePlugin(anotherPlugin);
        verify(anotherPlugin, never()).changeStateTo(plugin.getPluginState());
        verify(pluginFileManager).uninstallPlugin("filename");
        assertFalse(pluginOperationResult.isSuccess());
        assertEquals(PluginOperationStatus.UNSATISFIED_DEPENDENCIES, pluginOperationResult.getStatus());
        assertEquals(1, pluginOperationResult.getPluginDependencyResult().getUnsatisfiedDependencies().size());
        assertTrue(pluginOperationResult.getPluginDependencyResult().getUnsatisfiedDependencies()
                .contains(new PluginDependencyInformation("unknownplugin", new VersionOfDependency(""))));
    }

    @Test
    public void shouldFailureInstallEnabledPluginWithDisabledDependencies() throws Exception {
        // given

        given(plugin.hasState(PluginState.ENABLED)).willReturn(true);
        given(pluginDescriptorParser.parse(resource)).willReturn(anotherPlugin);
        given(pluginFileManager.uploadPlugin(pluginArtifact)).willReturn(resource);

        given(anotherPlugin.getFilename()).willReturn("filename");

        PluginDependencyResult pluginDependencyResult = PluginDependencyResult.dependenciesToEnable(Collections
                .singleton(new PluginDependencyInformation("unknownplugin", new VersionOfDependency(""))));
        given(pluginDependencyManager.getDependenciesToEnable(newArrayList(anotherPlugin))).willReturn(pluginDependencyResult);

        // when
        PluginOperationResult pluginOperationResult = pluginManager.installPlugin(pluginArtifact);

        // then
        verify(pluginDao, never()).save(anotherPlugin);
        verify(pluginAccessor, never()).savePlugin(anotherPlugin);
        verify(anotherPlugin, never()).changeStateTo(plugin.getPluginState());
        verify(pluginFileManager).uninstallPlugin("filename");
        assertFalse(pluginOperationResult.isSuccess());
        assertEquals(PluginOperationStatus.DISABLED_DEPENDENCIES, pluginOperationResult.getStatus());
        assertEquals(0, pluginOperationResult.getPluginDependencyResult().getUnsatisfiedDependencies().size());
        assertEquals(1, pluginOperationResult.getPluginDependencyResult().getDependenciesToEnable().size());
        assertEquals(1, pluginOperationResult.getPluginDependencyResult().getDependenciesToEnable().size());
        assertTrue(pluginOperationResult.getPluginDependencyResult().getDependenciesToEnable()
                .contains(new PluginDependencyInformation("unknownplugin", new VersionOfDependency(""))));
    }

    @Test
    public void shouldNotInstallDisabledPluginIfCannotInstall() throws Exception {
        // given

        given(plugin.hasState(PluginState.DISABLED)).willReturn(true);
        given(pluginDescriptorParser.parse(resource)).willReturn(anotherPlugin);
        given(pluginFileManager.uploadPlugin(pluginArtifact)).willReturn(resource);

        given(plugin.getFilename()).willReturn("filename");
        given(anotherPlugin.getFilename()).willReturn("anotherFilename");
        given(pluginFileManager.installPlugin("anotherFilename")).willReturn(false);

        PluginDependencyResult pluginDependencyResult = PluginDependencyResult.satisfiedDependencies();
        given(pluginDependencyManager.getDependenciesToEnable(newArrayList(anotherPlugin))).willReturn(pluginDependencyResult);

        // when
        PluginOperationResult pluginOperationResult = pluginManager.installPlugin(pluginArtifact);

        // then
        verify(pluginDao, never()).save(anotherPlugin);
        verify(pluginAccessor, never()).savePlugin(anotherPlugin);
        verify(anotherPlugin, never()).changeStateTo(plugin.getPluginState());
        verify(pluginFileManager, never()).uninstallPlugin("filename");
        verify(pluginFileManager).uninstallPlugin("anotherFilename");
        verify(pluginServerManager, never()).restart();
        assertFalse(pluginOperationResult.isSuccess());
        assertEquals(PluginOperationStatus.CANNOT_INSTALL_PLUGIN, pluginOperationResult.getStatus());
        assertEquals(0, pluginOperationResult.getPluginDependencyResult().getUnsatisfiedDependencies().size());
    }

    @Test
    public void shouldNotInstallEnabledPluginIfCannotInstall() throws Exception {
        // given

        given(plugin.hasState(PluginState.ENABLED)).willReturn(true);
        given(pluginDescriptorParser.parse(resource)).willReturn(anotherPlugin);
        given(pluginFileManager.uploadPlugin(pluginArtifact)).willReturn(resource);

        given(plugin.getFilename()).willReturn("filename");
        given(anotherPlugin.getFilename()).willReturn("anotherFilename");
        given(pluginFileManager.installPlugin("anotherFilename")).willReturn(false);

        PluginDependencyResult pluginDependencyResult = PluginDependencyResult.satisfiedDependencies();
        given(pluginDependencyManager.getDependenciesToEnable(newArrayList(anotherPlugin))).willReturn(pluginDependencyResult);

        // when
        PluginOperationResult pluginOperationResult = pluginManager.installPlugin(pluginArtifact);

        // then
        verify(pluginDao, never()).save(anotherPlugin);
        verify(pluginAccessor, never()).savePlugin(anotherPlugin);
        verify(anotherPlugin, never()).changeStateTo(plugin.getPluginState());
        verify(pluginFileManager, never()).uninstallPlugin("filename");
        verify(pluginFileManager).uninstallPlugin("anotherFilename");
        verify(pluginServerManager, never()).restart();
        assertFalse(pluginOperationResult.isSuccess());
        assertEquals(PluginOperationStatus.CANNOT_INSTALL_PLUGIN, pluginOperationResult.getStatus());
        assertEquals(0, pluginOperationResult.getPluginDependencyResult().getUnsatisfiedDependencies().size());
    }

    @Test
    public void shouldNotInstallSystemPlugin() throws Exception {
        // given
        given(pluginDescriptorParser.parse(resource)).willReturn(anotherPlugin);
        given(pluginFileManager.uploadPlugin(pluginArtifact)).willReturn(resource);
        given(anotherPlugin.isSystemPlugin()).willReturn(true);
        given(anotherPlugin.getFilename()).willReturn("anotherFilename");

        // when
        PluginOperationResult pluginOperationResult = pluginManager.installPlugin(pluginArtifact);

        // then
        verify(pluginDao, never()).save(anotherPlugin);
        verify(pluginAccessor, never()).savePlugin(anotherPlugin);
        verify(pluginFileManager).uninstallPlugin("anotherFilename");
        verify(pluginServerManager, never()).restart();
        assertFalse(pluginOperationResult.isSuccess());
        assertEquals(PluginOperationStatus.SYSTEM_PLUGIN_UPDATING, pluginOperationResult.getStatus());
    }

    @Test
    public void shouldInstallNotExistingPlugin() throws Exception {
        // given
        given(pluginDescriptorParser.parse(resource)).willReturn(anotherPlugin);
        given(pluginFileManager.uploadPlugin(pluginArtifact)).willReturn(resource);
        given(anotherPlugin.getIdentifier()).willReturn("notExistingPluginname");

        PluginDependencyResult pluginDependencyResult = PluginDependencyResult.satisfiedDependencies();
        given(pluginDependencyManager.getDependenciesToEnable(newArrayList(anotherPlugin))).willReturn(pluginDependencyResult);

        // when
        PluginOperationResult pluginOperationResult = pluginManager.installPlugin(pluginArtifact);

        // then
        verify(pluginDao).save(anotherPlugin);
        verify(pluginAccessor).savePlugin(anotherPlugin);
        verify(anotherPlugin).changeStateTo(PluginState.TEMPORARY);
        assertTrue(pluginOperationResult.isSuccess());
        assertEquals(PluginOperationStatus.SUCCESS, pluginOperationResult.getStatus());
        assertEquals(0, pluginOperationResult.getPluginDependencyResult().getDependenciesToEnable().size());
        assertEquals(0, pluginOperationResult.getPluginDependencyResult().getUnsatisfiedDependencies().size());
    }

    @Test
    public void shouldInstallNotExistingPluginAndNotifyAboutMissingDependencies() throws Exception {
        // given
        given(pluginDescriptorParser.parse(resource)).willReturn(anotherPlugin);
        given(pluginFileManager.uploadPlugin(pluginArtifact)).willReturn(resource);
        given(anotherPlugin.getIdentifier()).willReturn("notExistingPluginname");

        PluginDependencyResult pluginDependencyResult = PluginDependencyResult.unsatisfiedDependencies(Collections
                .singleton(new PluginDependencyInformation("unknownplugin", new VersionOfDependency(""))));
        given(pluginDependencyManager.getDependenciesToEnable(newArrayList(anotherPlugin))).willReturn(pluginDependencyResult);

        // when
        PluginOperationResult pluginOperationResult = pluginManager.installPlugin(pluginArtifact);

        // then
        verify(pluginDao).save(anotherPlugin);
        verify(pluginAccessor).savePlugin(anotherPlugin);
        assertTrue(pluginOperationResult.isSuccess());
        assertEquals(PluginOperationStatus.SUCCESS_WITH_MISSING_DEPENDENCIES, pluginOperationResult.getStatus());
        assertEquals(0, pluginOperationResult.getPluginDependencyResult().getDependenciesToEnable().size());
        assertEquals(1, pluginOperationResult.getPluginDependencyResult().getUnsatisfiedDependencies().size());
        assertTrue(pluginOperationResult.getPluginDependencyResult().getUnsatisfiedDependencies()
                .contains(new PluginDependencyInformation("unknownplugin")));
    }

    @Test
    public void shouldNotInstallPluginWithIncorrectVersion() throws Exception {
        // given
        given(pluginDescriptorParser.parse(resource)).willReturn(anotherPlugin);
        given(pluginFileManager.uploadPlugin(pluginArtifact)).willReturn(resource);
        given(anotherPlugin.getVersion()).willReturn(new Version("1.2.0"));
        given(anotherPlugin.getFilename()).willReturn("anotherFilename");

        // when
        PluginOperationResult pluginOperationResult = pluginManager.installPlugin(pluginArtifact);

        // then
        verify(pluginDao, never()).save(anotherPlugin);
        verify(pluginAccessor, never()).savePlugin(anotherPlugin);
        verify(pluginFileManager).uninstallPlugin("anotherFilename");
        verify(pluginServerManager, never()).restart();
        assertFalse(pluginOperationResult.isSuccess());
        assertEquals(PluginOperationStatus.INCORRECT_VERSION_PLUGIN, pluginOperationResult.getStatus());
    }

}
