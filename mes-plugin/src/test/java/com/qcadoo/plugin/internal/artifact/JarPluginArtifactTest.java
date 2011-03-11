package com.qcadoo.plugin.internal.artifact;

import static org.apache.commons.io.FileUtils.writeStringToFile;
import static org.apache.commons.io.IOUtils.contentEquals;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileInputStream;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.qcadoo.plugin.internal.api.PluginArtifact;
import com.qcadoo.plugin.internal.artifact.JarPluginArtifact;

public class JarPluginArtifactTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void shouldHaveFileName() throws Exception {
        // given
        File file = folder.newFile("plugin.jar");

        // when
        PluginArtifact pluginArtifact = new JarPluginArtifact(file);

        // then
        assertEquals("plugin.jar", pluginArtifact.getName());
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowAnExceptionIfFileNotExist() throws Exception {
        // given
        File file = new File("xxxx");

        // when
        new JarPluginArtifact(file);
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowAnExceptionIfFileIsNotReadable() throws Exception {
        // given
        File file = folder.newFile("plugin.jar");
        file.setReadable(false);

        // when
        new JarPluginArtifact(file);
    }

    @Test
    public void shouldHaveFileInputStream() throws Exception {
        // given
        File file = folder.newFile("plugin.jar");
        writeStringToFile(file, "content");

        // when
        PluginArtifact pluginArtifact = new JarPluginArtifact(file);

        // then
        Assert.assertTrue(contentEquals(new FileInputStream(file), pluginArtifact.getInputStream()));
    }

}
