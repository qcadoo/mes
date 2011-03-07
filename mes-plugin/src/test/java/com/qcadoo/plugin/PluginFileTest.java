package com.qcadoo.plugin;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class PluginFileTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private File pluginFile;

    private File destination;

    private DefaultPluginFileManager defaultPluginFileManager;

    @Before
    public void init() throws IOException {
        pluginFile = folder.newFile("pluginname.jar");
        destination = folder.newFolder("destination");
        defaultPluginFileManager = new DefaultPluginFileManager();
        defaultPluginFileManager.setPluginsPath(destination.getAbsolutePath());
        defaultPluginFileManager.setPluginsTmpPath(folder.getRoot().getAbsolutePath());
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
        defaultPluginFileManager.setPluginsPath(folder.getRoot().getAbsolutePath());

        // when
        defaultPluginFileManager.installPlugin("pluginname.jar");

        // then
    }

    @Test
    public void shouldInstallPluginFile() throws Exception {
        // given

        // when
        boolean result = defaultPluginFileManager.installPlugin("pluginname.jar");

        // then
        assertTrue(result);
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
        pluginFile.setReadable(false);

        // when
        boolean result = defaultPluginFileManager.installPlugin("pluginname.jar");

        // then
        assertFalse(result);
    }

    @After
    public void cleanUp() {
        folder.delete();
    }
}
