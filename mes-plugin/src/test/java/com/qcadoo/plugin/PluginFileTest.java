package com.qcadoo.plugin;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class PluginFileTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private File pluginTmpFile;

    private File source;

    private File destination;

    private DefaultPluginFileManager defaultPluginFileManager;

    @Before
    public void init() throws IOException {
        source = folder.newFolder("source");
        destination = folder.newFolder("destination");
        pluginTmpFile = new File(source, "pluginname.jar");
        FileUtils.touch(pluginTmpFile);
        defaultPluginFileManager = new DefaultPluginFileManager();
        defaultPluginFileManager.setPluginsPath(destination.getAbsolutePath());
        defaultPluginFileManager.setPluginsTmpPath(source.getAbsolutePath());
    }

    @Test
    public void shouldFailureInstallPluginFileWhenDestinationNotExist() throws Exception {
        // given
        defaultPluginFileManager.setPluginsPath("notExistingFolder");

        // when
        boolean result = defaultPluginFileManager.installPlugin("pluginname.jar");

        // then
        assertFalse(result);
    }

    @Test
    public void shouldFailureInstallPluginFileWhenDestinationCannotWrite() throws Exception {
        // given
        destination.setWritable(false);

        // when
        boolean result = defaultPluginFileManager.installPlugin("pluginname.jar");

        // then
        assertFalse(result);
    }

    @Test(expected = PluginException.class)
    public void shouldThrowExceptionOnInstallingPluginFileWhenDestinationFileExist() throws Exception {
        // given
        defaultPluginFileManager.setPluginsPath(source.getAbsolutePath());

        // when
        defaultPluginFileManager.installPlugin("pluginname.jar");

        // then
    }

    @Test
    public void shouldInstallPluginFile() throws Exception {
        // given
        File pluginFile = new File(destination, "pluginname.jar");

        // when
        boolean result = defaultPluginFileManager.installPlugin("pluginname.jar");

        // then
        assertTrue(result);
        assertFalse(pluginTmpFile.exists());
        assertTrue(pluginFile.exists());
    }

    @Test
    public void shouldFailureInstallPluginFileWhenSourceNotExist() throws Exception {
        // given

        // when
        boolean result = defaultPluginFileManager.installPlugin("notExistingPluginname.jar");

        // then
        assertFalse(result);
    }

    @Test
    public void shouldFailureInstallPluginFileWhenSourceCannotRead() throws Exception {
        // given
        pluginTmpFile.setReadable(false);

        // when
        boolean result = defaultPluginFileManager.installPlugin("pluginname.jar");

        // then
        assertFalse(result);
    }

    @Test
    public void shouldRemoveTemporaryPluginFile() throws Exception {
        // given

        // when
        defaultPluginFileManager.removePlugin("pluginname.jar");

        // then
        assertFalse(pluginTmpFile.exists());
    }

    @Test(expected = PluginException.class)
    public void shouldThrowExceptionOnRemovingPluginFileWhenOperationFail() throws Exception {
        // given

        // when
        defaultPluginFileManager.removePlugin("removepluginname.jar");

        // then
    }

    @Test
    public void shouldRemoveNotTemporaryPluginFile() throws Exception {
        // given
        File pluginFile = new File(destination, "removepluginname.jar");
        FileUtils.touch(pluginFile);

        // when
        defaultPluginFileManager.removePlugin("removepluginname.jar");

        // then
        assertFalse(pluginFile.exists());
    }

    @Test
    public void shouldUninstallTemporaryPluginFile() throws Exception {
        // given

        // when
        boolean result = defaultPluginFileManager.uninstallPlugin("pluginname.jar");

        // then
        assertTrue(result);
        assertFalse(pluginTmpFile.exists());
    }

    @Test
    public void shouldFailureUninstallTemporaryPluginFileWhenPluginDoesntExist() throws Exception {
        // given

        // when
        boolean result = defaultPluginFileManager.uninstallPlugin("notExistingPluginname.jar");

        // then
        assertFalse(result);
    }

    @Test
    public void shouldFailureUninstallTemporaryPluginFileWhenPluginCannotRead() throws Exception {
        // given
        pluginTmpFile.setReadable(false);

        // when
        boolean result = defaultPluginFileManager.installPlugin("pluginname.jar");

        // then
        assertFalse(result);
        assertTrue(pluginTmpFile.exists());
    }

    @Test
    public void shouldFailureUninstallNotTemporaryPluginFileWhenPluginDoesntExist() throws Exception {
        // given

        // when
        boolean result = defaultPluginFileManager.uninstallPlugin("notExistingPluginname.jar");

        // then
        assertFalse(result);
    }

    @Test
    public void shouldFailureUninstallNotTemporaryPluginFileWhenPluginCannotRead() throws Exception {
        // given
        File pluginFile = new File(destination, "uninstallpluginname.jar");
        FileUtils.touch(pluginFile);
        pluginFile.setReadable(false);

        // when
        boolean result = defaultPluginFileManager.uninstallPlugin("uninstallpluginname.jar");

        // then
        assertFalse(result);
        assertTrue(pluginFile.exists());
    }

    @Test
    public void shouldUninstallNotTemporaryPluginFile() throws Exception {
        // given
        File pluginFile = new File(destination, "uninstallpluginname.jar");
        FileUtils.touch(pluginFile);

        // when
        boolean result = defaultPluginFileManager.uninstallPlugin("uninstallpluginname.jar");

        // then
        assertTrue(result);
        assertFalse(pluginFile.exists());
    }

    @After
    public void cleanUp() {
        folder.delete();
    }
}
