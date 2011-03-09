package com.qcadoo.plugin;

import java.io.File;

public interface PluginFileManager {

    public boolean installPlugin(final String... keys);

    public File uploadPlugin(final PluginArtifact pluginArtifact);

    public boolean uninstallPlugin(final String... keys);

    public void removePlugin(final String key);
}
