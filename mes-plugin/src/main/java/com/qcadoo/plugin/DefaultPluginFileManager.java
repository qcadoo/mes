package com.qcadoo.plugin;

import static java.lang.System.getProperty;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultPluginFileManager implements PluginFileManager {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultPluginFileManager.class);

    private String pluginsPath;

    private String pluginsTmpPath;

    @Override
    public boolean installPlugin(final String... keys) throws PluginException {
        if (!checkFileRightsToWrite(pluginsPath)) {
            return false;
        }
        for (String key : keys) {
            if (!checkFileRightsToRead(key)) {
                return false;
            }
        }
        for (String key : keys) {
            try {
                FileUtils.moveToDirectory(new File(pluginsTmpPath + getProperty("file.separator") + key), new File(pluginsPath),
                        false);
            } catch (IOException e) {
                LOG.error("Problem with moving plugin file - " + e.getMessage());
                throw new PluginException(e.getMessage(), e.getCause());
            }
        }
        return true;
    }

    @Override
    public File uploadPlugin(final PluginArtifact pluginArtifact) throws PluginException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean uninstallPlugin(final String... keys) throws PluginException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void removePlugin(final String key) {
        // TODO Auto-generated method stub

    }

    private boolean checkFileRightsToRead(final String key) {
        File file = new File(pluginsTmpPath + getProperty("file.separator") + key);
        if (!file.exists() || !file.canRead()) {
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

    public void setPluginsPath(final String pluginsPath) {
        this.pluginsPath = pluginsPath;
    }

    public void setPluginsTmpPath(final String pluginsTmpPath) {
        this.pluginsTmpPath = pluginsTmpPath;
    }

}
