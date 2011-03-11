package com.qcadoo.plugin.internal.filemanager;

import static java.lang.System.getProperty;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.qcadoo.plugin.internal.PluginException;
import com.qcadoo.plugin.internal.api.PluginArtifact;
import com.qcadoo.plugin.internal.api.PluginFileManager;

@Service
public final class DefaultPluginFileManager implements PluginFileManager {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultPluginFileManager.class);

    @Value("qcadoo.plugin.pluginsPath")
    private String pluginsPath;

    @Value("qcadoo.plugin.pluginsTmpPath")
    private String pluginsTmpPath;

    @Override
    public boolean installPlugin(final String... keys) {
        if (!checkFileRightsToWrite(pluginsPath)) {
            return false;
        }
        for (String key : keys) {
            if (!checkFileExists(key, pluginsTmpPath)) {
                return false;
            }
        }
        for (String key : keys) {
            try {
                FileUtils.moveToDirectory(new File(pluginsTmpPath + getProperty("file.separator") + key), new File(pluginsPath),
                        false);
            } catch (IOException e) {
                LOG.error("Problem with moving plugin file - " + e.getMessage());
                throw new PluginException(e.getMessage(), e);
            }
        }
        return true;
    }

    @Override
    public File uploadPlugin(final PluginArtifact pluginArtifact) {
        InputStream input = pluginArtifact.getInputStream();
        File pluginFile = new File(pluginsTmpPath + getProperty("file.separator") + pluginArtifact.getName());
        OutputStream output = null;
        try {
            output = new FileOutputStream(pluginFile);
            IOUtils.copy(input, output);

            output.flush();
        } catch (IOException e) {
            LOG.error("Problem with upload plugin file - " + e.getMessage());
            throw new PluginException(e.getMessage(), e);
        } finally {
            IOUtils.closeQuietly(input);
            IOUtils.closeQuietly(output);
        }
        return pluginFile;
    }

    @Override
    public void uninstallPlugin(final String... keys) {
        for (String key : keys) {
            File file = new File((pluginsTmpPath + getProperty("file.separator") + key));
            if (!file.exists()) {
                file = new File(pluginsPath + getProperty("file.separator") + key);
            }
            try {
                FileUtils.forceDelete(file);
            } catch (IOException e) {
                LOG.error("Problem with removing plugin file - " + e.getMessage());
                if (file.exists()) {
                    LOG.info("Trying delete file after JVM stop");
                    file.deleteOnExit();
                }
            }
        }
    }

    private boolean checkFileExists(final String key, final String path) {
        File file = new File(path + getProperty("file.separator") + key);
        if (!file.exists()) {
            return false;
        }
        return true;
    }

    private boolean checkFileRightsToWrite(final String pluginsPath) {
        File file = new File(pluginsPath);
        if (!file.exists() || !file.canWrite()) {
            return false;
        }
        return true;
    }

    void setPluginsPath(final String pluginsPath) {
        this.pluginsPath = pluginsPath;
    }

    void setPluginsTmpPath(final String pluginsTmpPath) {
        this.pluginsTmpPath = pluginsTmpPath;
    }

}
