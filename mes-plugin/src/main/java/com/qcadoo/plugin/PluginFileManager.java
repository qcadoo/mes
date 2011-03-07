package com.qcadoo.plugin;

import java.io.File;

public interface PluginFileManager {

    public boolean installPlugin(final String... keys) throws PluginException;

    public File uploadPlugin(final PluginArtifact pluginArtifact) throws PluginException;

    public boolean uninstallPlugin(final String... keys) throws PluginException;

    public void removePlugin(final String key);
}
