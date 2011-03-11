package com.qcadoo.plugin.internal.api;

import java.io.File;


public interface PluginFileManager {

    public boolean installPlugin(final String... keys);

    public File uploadPlugin(final PluginArtifact pluginArtifact);

    public void uninstallPlugin(final String... keys);

}
